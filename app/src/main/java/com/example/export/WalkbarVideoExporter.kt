package com.example.export

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.Rect
import android.graphics.SurfaceTexture
import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaExtractor
import android.media.MediaFormat
import android.media.MediaMetadataRetriever
import android.media.MediaMuxer
import android.net.Uri
import android.opengl.Matrix
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.view.Surface
import com.example.characters.CharacterRegistry
import com.example.characters.CharacterRenderer
import com.example.characters.WalkCycleMath
import com.example.model.CharacterOverlayConfig
import com.example.model.ExportFpsOption
import com.example.model.ExportState
import com.example.model.VideoMetadata
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.nio.ByteBuffer
import kotlin.coroutines.coroutineContext

object WalkbarVideoExporter {

  /**
   * Main export flow providing reactive progress updates with hardware acceleration.
   */
  fun exportVideoFlow(
    context: Context,
    metadata: VideoMetadata,
    config: CharacterOverlayConfig
  ): Flow<ExportState> = flow {
    emit(ExportState.Preparing("Preparing hardware video encoder & audio streams..."))

    val character = CharacterRegistry.getById(config.characterId)

    // Resolution & Dimension configuration
    val rawW = metadata.effectiveWidth
    val rawH = metadata.effectiveHeight
    val maxDimension = 1920
    val scale = if (rawW > maxDimension || rawH > maxDimension) {
      maxDimension.toFloat() / maxOf(rawW, rawH).toFloat()
    } else {
      1.0f
    }
    val width = (((rawW * scale).toInt() / 2) * 2).coerceAtLeast(160)
    val height = (((rawH * scale).toInt() / 2) * 2).coerceAtLeast(160)
    val durationMs = metadata.durationMs.coerceAtLeast(1000L)

    // Framerate selection based on user preference
    val fps = when (config.exportFpsOption) {
      ExportFpsOption.AUTO -> if (metadata.fps > 0f) metadata.fps.coerceIn(24f, 60f) else 30.0f
      ExportFpsOption.FPS_60 -> 60.0f
      ExportFpsOption.FPS_30 -> 30.0f
      ExportFpsOption.FPS_MAX -> if (metadata.fps >= 50f) 60.0f else 60.0f
    }

    val totalFrames = ((durationMs / 1000.0) * fps).toInt().coerceAtLeast(1)
    val frameIntervalUs = (1_000_000.0 / fps).toLong()

    val targetBitrate = calculateTargetBitrate(width, height, fps, metadata.bitrateBps)
    val tempOutputFile = File(context.cacheDir, "walkbar_export_${System.currentTimeMillis()}.mp4")

    var exportSucceeded = false

    // Try Ultra-Fast Hardware Decoder + OpenGL Compositing first
    try {
      exportWithHardwarePipeline(
        context = context,
        metadata = metadata,
        config = config,
        width = width,
        height = height,
        durationMs = durationMs,
        fps = fps,
        totalFrames = totalFrames,
        frameIntervalUs = frameIntervalUs,
        targetBitrate = targetBitrate,
        tempOutputFile = tempOutputFile,
        onProgress = { progress, currentFrame, total ->
          emit(
            ExportState.Rendering(
              progress = progress,
              currentFrame = currentFrame,
              totalFrames = total,
              fps = fps,
              statusMessage = "Compositing frame $currentFrame of $total (${(progress * 100).toInt()}%)"
            )
          )
        },
        onPreparingAudio = {
          emit(ExportState.Preparing("Muxing high-fidelity audio stream..."))
        },
        onFinalizing = {
          emit(ExportState.Finalizing)
        }
      )
      exportSucceeded = true
    } catch (c: CancellationException) {
      throw c
    } catch (e: Exception) {
      e.printStackTrace()
      // If hardware decoder pipeline hit any device codec limitation, run direct fallback
    }

    // Fallback pipeline if hardware decoder couldn't initialize for exotic codec
    if (!exportSucceeded) {
      exportWithDirectFallbackPipeline(
        context = context,
        metadata = metadata,
        config = config,
        width = width,
        height = height,
        durationMs = durationMs,
        fps = fps,
        totalFrames = totalFrames,
        frameIntervalUs = frameIntervalUs,
        targetBitrate = targetBitrate,
        tempOutputFile = tempOutputFile,
        onProgress = { progress, currentFrame, total ->
          emit(
            ExportState.Rendering(
              progress = progress,
              currentFrame = currentFrame,
              totalFrames = total,
              fps = fps,
              statusMessage = "Compositing frame $currentFrame of $total (${(progress * 100).toInt()}%)"
            )
          )
        },
        onPreparingAudio = {
          emit(ExportState.Preparing("Muxing audio stream..."))
        },
        onFinalizing = {
          emit(ExportState.Finalizing)
        }
      )
    }

    // Save final result to Gallery / App storage
    val finalUri = saveToGalleryOrAppStorage(context, tempOutputFile, metadata.fileName)
    val finalSize = tempOutputFile.length()

    emit(
      ExportState.Success(
        outputUri = finalUri,
        outputPath = tempOutputFile.absolutePath,
        fileSizeBytes = finalSize,
        durationMs = durationMs,
        width = width,
        height = height
      )
    )
  }.flowOn(Dispatchers.Default)

  /**
   * Ultra-Fast Hardware Decoder + OpenGL Surface Pipeline
   * Decodes directly to GPU SurfaceTexture at 200+ FPS, blending character overlay with alpha.
   */
  private suspend fun exportWithHardwarePipeline(
    context: Context,
    metadata: VideoMetadata,
    config: CharacterOverlayConfig,
    width: Int,
    height: Int,
    durationMs: Long,
    fps: Float,
    totalFrames: Int,
    frameIntervalUs: Long,
    targetBitrate: Int,
    tempOutputFile: File,
    onProgress: suspend (Float, Int, Int) -> Unit,
    onPreparingAudio: suspend () -> Unit,
    onFinalizing: suspend () -> Unit
  ) {
    val character = CharacterRegistry.getById(config.characterId)
    val muxer = MediaMuxer(tempOutputFile.absolutePath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
    var videoTrackIndex = -1
    var audioTrackIndex = -1
    var muxerStarted = false
    val firstVideoPtsHolder = longArrayOf(-1L)

    // 1. Setup Video Encoder
    val videoFormat = MediaFormat.createVideoFormat(MediaFormat.MIMETYPE_VIDEO_AVC, width, height).apply {
      setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface)
      setInteger(MediaFormat.KEY_BIT_RATE, targetBitrate)
      setInteger(MediaFormat.KEY_FRAME_RATE, fps.toInt())
      setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 1)
    }

    val encoder = MediaCodec.createEncoderByType(MediaFormat.MIMETYPE_VIDEO_AVC)
    encoder.configure(videoFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
    val encoderInputSurface = encoder.createInputSurface()
    encoder.start()

    val eglEncoder = EglSurfaceEncoder(encoderInputSurface)
    val oesTextureId = eglEncoder.createOesTexture()
    val surfaceTexture = SurfaceTexture(oesTextureId).apply {
      setDefaultBufferSize(width, height)
    }
    val decoderOutputSurface = Surface(surfaceTexture)

    // 2. Setup Video & Audio Extractors
    val videoExtractor = MediaExtractor()
    videoExtractor.setDataSource(context, metadata.uri, null)

    var videoDecoder: MediaCodec? = null
    var videoDecoderFormat: MediaFormat? = null

    for (i in 0 until videoExtractor.trackCount) {
      val format = videoExtractor.getTrackFormat(i)
      val mime = format.getString(MediaFormat.KEY_MIME) ?: ""
      if (mime.startsWith("video/")) {
        videoExtractor.selectTrack(i)
        videoDecoderFormat = format
        videoDecoder = MediaCodec.createDecoderByType(mime)
        videoDecoder.configure(format, decoderOutputSurface, null, 0)
        videoDecoder.start()
        break
      }
    }

    if (videoDecoder == null) {
      throw IllegalStateException("No video track found for hardware decoding")
    }

    val audioExtractor = if (metadata.hasAudio) MediaExtractor() else null
    var audioFormat: MediaFormat? = null
    if (audioExtractor != null) {
      try {
        audioExtractor.setDataSource(context, metadata.uri, null)
        for (i in 0 until audioExtractor.trackCount) {
          val format = audioExtractor.getTrackFormat(i)
          val mime = format.getString(MediaFormat.KEY_MIME) ?: ""
          if (mime.startsWith("audio/")) {
            audioExtractor.selectTrack(i)
            audioFormat = format
            break
          }
        }
      } catch (e: Exception) {
        e.printStackTrace()
      }
    }

    val bufferInfo = MediaCodec.BufferInfo()
    val decoderBufferInfo = MediaCodec.BufferInfo()
    val charSize = height * config.customScalePercent
    val overlayBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val overlayCanvas = Canvas(overlayBitmap)
    val texMatrix = FloatArray(16)
    Matrix.setIdentityM(texMatrix, 0)

    var extractorDone = false
    var decoderDone = false
    var currentDecodedFrameUs = 0L

    try {
      for (frameIndex in 0 until totalFrames) {
        if (!coroutineContext.isActive) {
          throw CancellationException("Export was cancelled by user")
        }

        val targetPtsUs = frameIndex * frameIntervalUs
        val currentMs = targetPtsUs / 1000L
        val ptsNs = targetPtsUs * 1000L

        // Feed decoder until we reach or pass targetPtsUs
        while (!decoderDone && currentDecodedFrameUs < targetPtsUs) {
          // Feed input to decoder
          if (!extractorDone) {
            val inIdx = videoDecoder.dequeueInputBuffer(1000L)
            if (inIdx >= 0) {
              val inBuffer = videoDecoder.getInputBuffer(inIdx)
              if (inBuffer != null) {
                val sampleSize = videoExtractor.readSampleData(inBuffer, 0)
                if (sampleSize < 0) {
                  videoDecoder.queueInputBuffer(inIdx, 0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM)
                  extractorDone = true
                } else {
                  val sampleTime = videoExtractor.sampleTime
                  videoDecoder.queueInputBuffer(inIdx, 0, sampleSize, sampleTime, 0)
                  videoExtractor.advance()
                }
              }
            }
          }

          // Get decoded frame
          val outIdx = videoDecoder.dequeueOutputBuffer(decoderBufferInfo, 1000L)
          if (outIdx >= 0) {
            if ((decoderBufferInfo.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
              decoderDone = true
            }
            val render = decoderBufferInfo.size > 0
            videoDecoder.releaseOutputBuffer(outIdx, render)
            if (render) {
              surfaceTexture.updateTexImage()
              surfaceTexture.getTransformMatrix(texMatrix)
              currentDecodedFrameUs = decoderBufferInfo.presentationTimeUs
            }
          } else if (outIdx == MediaCodec.INFO_TRY_AGAIN_LATER && extractorDone) {
            decoderDone = true
            break
          }
        }

        // 2. Render Character Overlay onto transparent canvas
        overlayCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)
        val pixelX = WalkCycleMath.calculatePixelX(currentMs, durationMs, config, width.toFloat())
        val pixelY = WalkCycleMath.calculatePixelY(config, height.toFloat())
        val phase = WalkCycleMath.calculatePhase(
          currentTimeMs = currentMs,
          behavior = config.behavior,
          isPlaying = true,
          durationMs = durationMs,
          config = config,
          canvasWidth = width.toFloat(),
          canvasHeight = height.toFloat()
        )

        CharacterRenderer.drawCharacter(
          canvas = overlayCanvas,
          character = character,
          behavior = config.behavior,
          centerX = pixelX,
          bottomY = pixelY,
          size = charSize,
          phase = phase,
          facingRight = config.effectiveFacingRight,
          currentTimeMs = currentMs
        )

        // 3. Render hardware OES video texture + 2D overlay directly into encoder input surface
        eglEncoder.renderOesAndOverlayFrame(
          oesTextureId = oesTextureId,
          texMatrix = texMatrix,
          overlayBitmap = overlayBitmap,
          timestampNs = ptsNs,
          width = width,
          height = height
        )

        // Drain encoder
        videoTrackIndex = drainEncoder(
          encoder = encoder,
          bufferInfo = bufferInfo,
          muxer = muxer,
          currentVideoTrackIndex = videoTrackIndex,
          isMuxerStarted = muxerStarted,
          audioFormat = audioFormat,
          firstVideoPtsHolder = firstVideoPtsHolder
        ) { vTrack, aTrack ->
          videoTrackIndex = vTrack
          audioTrackIndex = aTrack
          muxerStarted = true
        }

        if (frameIndex % 3 == 0 || frameIndex == totalFrames - 1) {
          val progress = (frameIndex + 1).toFloat() / totalFrames.toFloat()
          onProgress(progress, frameIndex + 1, totalFrames)
        }
      }

      overlayBitmap.recycle()
      encoder.signalEndOfInputStream()

      // Drain remaining video packets
      var eos = false
      while (!eos) {
        val outIndex = encoder.dequeueOutputBuffer(bufferInfo, 10000)
        if (outIndex == MediaCodec.INFO_TRY_AGAIN_LATER) {
          break
        } else if (outIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
          if (!muxerStarted) {
            videoTrackIndex = muxer.addTrack(encoder.outputFormat)
            if (audioFormat != null) {
              audioTrackIndex = muxer.addTrack(audioFormat)
            }
            muxer.start()
            muxerStarted = true
          }
        } else if (outIndex >= 0) {
          if ((bufferInfo.flags and MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0) {
            bufferInfo.size = 0
          }
          val encodedData = encoder.getOutputBuffer(outIndex)
          if (encodedData != null && bufferInfo.size > 0 && muxerStarted) {
            if (firstVideoPtsHolder[0] < 0L) {
              firstVideoPtsHolder[0] = bufferInfo.presentationTimeUs
            }
            bufferInfo.presentationTimeUs = (bufferInfo.presentationTimeUs - firstVideoPtsHolder[0]).coerceAtLeast(0L)
            encodedData.position(bufferInfo.offset)
            encodedData.limit(bufferInfo.offset + bufferInfo.size)
            muxer.writeSampleData(videoTrackIndex, encodedData, bufferInfo)
          }
          if ((bufferInfo.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
            eos = true
          }
          encoder.releaseOutputBuffer(outIndex, false)
        }
      }

      // 4. Mux Audio Passthrough Stream
      if (audioExtractor != null && audioTrackIndex >= 0 && muxerStarted) {
        onPreparingAudio()
        muxAudioPassthrough(audioExtractor, muxer, audioTrackIndex, durationMs)
      }

      onFinalizing()
    } finally {
      try {
        eglEncoder.release()
        encoder.stop()
        encoder.release()
        encoderInputSurface.release()
        videoDecoder.stop()
        videoDecoder.release()
        decoderOutputSurface.release()
        surfaceTexture.release()
        videoExtractor.release()
        audioExtractor?.release()
        if (muxerStarted) {
          muxer.stop()
          muxer.release()
        }
      } catch (e: Exception) {
        e.printStackTrace()
      }
    }
  }

  /**
   * High-Reliability Fallback Pipeline
   */
  private suspend fun exportWithDirectFallbackPipeline(
    context: Context,
    metadata: VideoMetadata,
    config: CharacterOverlayConfig,
    width: Int,
    height: Int,
    durationMs: Long,
    fps: Float,
    totalFrames: Int,
    frameIntervalUs: Long,
    targetBitrate: Int,
    tempOutputFile: File,
    onProgress: suspend (Float, Int, Int) -> Unit,
    onPreparingAudio: suspend () -> Unit,
    onFinalizing: suspend () -> Unit
  ) {
    val character = CharacterRegistry.getById(config.characterId)
    val muxer = MediaMuxer(tempOutputFile.absolutePath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
    var videoTrackIndex = -1
    var audioTrackIndex = -1
    var muxerStarted = false
    val firstVideoPtsHolder = longArrayOf(-1L)

    val videoFormat = MediaFormat.createVideoFormat(MediaFormat.MIMETYPE_VIDEO_AVC, width, height).apply {
      setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface)
      setInteger(MediaFormat.KEY_BIT_RATE, targetBitrate)
      setInteger(MediaFormat.KEY_FRAME_RATE, fps.toInt())
      setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 1)
    }

    val encoder = MediaCodec.createEncoderByType(MediaFormat.MIMETYPE_VIDEO_AVC)
    encoder.configure(videoFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
    val inputSurface = encoder.createInputSurface()
    encoder.start()

    val eglEncoder = EglSurfaceEncoder(inputSurface)

    val audioExtractor = if (metadata.hasAudio) MediaExtractor() else null
    var audioFormat: MediaFormat? = null
    if (audioExtractor != null) {
      try {
        audioExtractor.setDataSource(context, metadata.uri, null)
        for (i in 0 until audioExtractor.trackCount) {
          val format = audioExtractor.getTrackFormat(i)
          val mime = format.getString(MediaFormat.KEY_MIME) ?: ""
          if (mime.startsWith("audio/")) {
            audioExtractor.selectTrack(i)
            audioFormat = format
            break
          }
        }
      } catch (e: Exception) {
        e.printStackTrace()
      }
    }

    val retriever = MediaMetadataRetriever()
    try {
      retriever.setDataSource(context, metadata.uri)
    } catch (e: Exception) {
      retriever.setDataSource(metadata.uri.toString())
    }

    val bufferInfo = MediaCodec.BufferInfo()
    val charSize = height * config.customScalePercent
    val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG).apply {
      isDither = true
    }
    val srcRect = Rect()
    val dstRect = Rect(0, 0, width, height)
    val compositeBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(compositeBitmap)

    try {
      for (frameIndex in 0 until totalFrames) {
        if (!coroutineContext.isActive) {
          throw CancellationException("Export was cancelled by user")
        }

        val frameTimestampUs = frameIndex * frameIntervalUs
        val currentMs = frameTimestampUs / 1000L
        val ptsNs = frameTimestampUs * 1000L

        val sourceBitmap: Bitmap? = try {
          if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            retriever.getScaledFrameAtTime(
              frameTimestampUs,
              MediaMetadataRetriever.OPTION_CLOSEST,
              width,
              height
            ) ?: retriever.getFrameAtTime(frameTimestampUs, MediaMetadataRetriever.OPTION_CLOSEST)
          } else {
            retriever.getFrameAtTime(frameTimestampUs, MediaMetadataRetriever.OPTION_CLOSEST)
          }
        } catch (_: Exception) {
          null
        }

        canvas.drawColor(Color.BLACK, PorterDuff.Mode.CLEAR)

        if (sourceBitmap != null) {
          srcRect.set(0, 0, sourceBitmap.width, sourceBitmap.height)
          canvas.drawBitmap(sourceBitmap, srcRect, dstRect, paint)
          sourceBitmap.recycle()
        } else {
          canvas.drawColor(Color.rgb(18, 20, 24))
        }

        val pixelX = WalkCycleMath.calculatePixelX(currentMs, durationMs, config, width.toFloat())
        val pixelY = WalkCycleMath.calculatePixelY(config, height.toFloat())
        val phase = WalkCycleMath.calculatePhase(
          currentTimeMs = currentMs,
          behavior = config.behavior,
          isPlaying = true,
          durationMs = durationMs,
          config = config,
          canvasWidth = width.toFloat(),
          canvasHeight = height.toFloat()
        )

        CharacterRenderer.drawCharacter(
          canvas = canvas,
          character = character,
          behavior = config.behavior,
          centerX = pixelX,
          bottomY = pixelY,
          size = charSize,
          phase = phase,
          facingRight = config.effectiveFacingRight,
          currentTimeMs = currentMs
        )

        eglEncoder.renderBitmapFrame(compositeBitmap, ptsNs, width, height)

        videoTrackIndex = drainEncoder(
          encoder = encoder,
          bufferInfo = bufferInfo,
          muxer = muxer,
          currentVideoTrackIndex = videoTrackIndex,
          isMuxerStarted = muxerStarted,
          audioFormat = audioFormat,
          firstVideoPtsHolder = firstVideoPtsHolder
        ) { vTrack, aTrack ->
          videoTrackIndex = vTrack
          audioTrackIndex = aTrack
          muxerStarted = true
        }

        if (frameIndex % 3 == 0 || frameIndex == totalFrames - 1) {
          val progress = (frameIndex + 1).toFloat() / totalFrames.toFloat()
          onProgress(progress, frameIndex + 1, totalFrames)
        }
      }

      compositeBitmap.recycle()
      encoder.signalEndOfInputStream()

      var eos = false
      while (!eos) {
        val outIndex = encoder.dequeueOutputBuffer(bufferInfo, 10000)
        if (outIndex == MediaCodec.INFO_TRY_AGAIN_LATER) {
          break
        } else if (outIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
          if (!muxerStarted) {
            videoTrackIndex = muxer.addTrack(encoder.outputFormat)
            if (audioFormat != null) {
              audioTrackIndex = muxer.addTrack(audioFormat)
            }
            muxer.start()
            muxerStarted = true
          }
        } else if (outIndex >= 0) {
          if ((bufferInfo.flags and MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0) {
            bufferInfo.size = 0
          }
          val encodedData = encoder.getOutputBuffer(outIndex)
          if (encodedData != null && bufferInfo.size > 0 && muxerStarted) {
            if (firstVideoPtsHolder[0] < 0L) {
              firstVideoPtsHolder[0] = bufferInfo.presentationTimeUs
            }
            bufferInfo.presentationTimeUs = (bufferInfo.presentationTimeUs - firstVideoPtsHolder[0]).coerceAtLeast(0L)
            encodedData.position(bufferInfo.offset)
            encodedData.limit(bufferInfo.offset + bufferInfo.size)
            muxer.writeSampleData(videoTrackIndex, encodedData, bufferInfo)
          }
          if ((bufferInfo.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
            eos = true
          }
          encoder.releaseOutputBuffer(outIndex, false)
        }
      }

      if (audioExtractor != null && audioTrackIndex >= 0 && muxerStarted) {
        onPreparingAudio()
        muxAudioPassthrough(audioExtractor, muxer, audioTrackIndex, durationMs)
      }

      onFinalizing()
    } finally {
      try {
        eglEncoder.release()
        encoder.stop()
        encoder.release()
        inputSurface.release()
        retriever.release()
        audioExtractor?.release()
        if (muxerStarted) {
          muxer.stop()
          muxer.release()
        }
      } catch (e: Exception) {
        e.printStackTrace()
      }
    }
  }

  private fun muxAudioPassthrough(
    audioExtractor: MediaExtractor,
    muxer: MediaMuxer,
    audioTrackIndex: Int,
    durationMs: Long
  ) {
    val maxBufferSize = 256 * 1024
    val audioBuffer = ByteBuffer.allocateDirect(maxBufferSize)
    val audioBufferInfo = MediaCodec.BufferInfo()
    var firstAudioPtsUs = -1L

    while (true) {
      val sampleSize = audioExtractor.readSampleData(audioBuffer, 0)
      if (sampleSize < 0) break

      val sampleFlags = audioExtractor.sampleFlags
      if ((sampleFlags and MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0) {
        audioExtractor.advance()
        continue
      }

      val isKey = (sampleFlags and MediaExtractor.SAMPLE_FLAG_SYNC) != 0
      val flags = if (isKey) MediaCodec.BUFFER_FLAG_KEY_FRAME else 0
      val sampleTime = audioExtractor.sampleTime

      if (firstAudioPtsUs < 0L && sampleTime >= 0L) {
        firstAudioPtsUs = sampleTime
      }
      val normalizedAudioPts = if (firstAudioPtsUs >= 0L) (sampleTime - firstAudioPtsUs).coerceAtLeast(0L) else sampleTime

      audioBufferInfo.offset = 0
      audioBufferInfo.size = sampleSize
      audioBufferInfo.presentationTimeUs = normalizedAudioPts
      audioBufferInfo.flags = flags

      if (audioBufferInfo.presentationTimeUs <= durationMs * 1000L) {
        muxer.writeSampleData(audioTrackIndex, audioBuffer, audioBufferInfo)
      }
      audioExtractor.advance()
    }
  }

  private fun drainEncoder(
    encoder: MediaCodec,
    bufferInfo: MediaCodec.BufferInfo,
    muxer: MediaMuxer,
    currentVideoTrackIndex: Int,
    isMuxerStarted: Boolean,
    audioFormat: MediaFormat?,
    firstVideoPtsHolder: LongArray,
    onMuxerStarted: (Int, Int) -> Unit
  ): Int {
    var muxerStarted = isMuxerStarted
    var videoTrackIndex = currentVideoTrackIndex
    var audioTrackIndex = -1

    while (true) {
      val outIndex = encoder.dequeueOutputBuffer(bufferInfo, 0)
      if (outIndex == MediaCodec.INFO_TRY_AGAIN_LATER) {
        break
      } else if (outIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
        if (!muxerStarted) {
          videoTrackIndex = muxer.addTrack(encoder.outputFormat)
          if (audioFormat != null) {
            audioTrackIndex = muxer.addTrack(audioFormat)
          }
          muxer.start()
          muxerStarted = true
          onMuxerStarted(videoTrackIndex, audioTrackIndex)
        }
      } else if (outIndex >= 0) {
        if ((bufferInfo.flags and MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0) {
          bufferInfo.size = 0
        }
        val encodedData = encoder.getOutputBuffer(outIndex)
        if (encodedData != null && bufferInfo.size > 0 && muxerStarted && videoTrackIndex >= 0) {
          if (firstVideoPtsHolder[0] < 0L) {
            firstVideoPtsHolder[0] = bufferInfo.presentationTimeUs
          }
          bufferInfo.presentationTimeUs = (bufferInfo.presentationTimeUs - firstVideoPtsHolder[0]).coerceAtLeast(0L)
          encodedData.position(bufferInfo.offset)
          encodedData.limit(bufferInfo.offset + bufferInfo.size)
          muxer.writeSampleData(videoTrackIndex, encodedData, bufferInfo)
        }
        encoder.releaseOutputBuffer(outIndex, false)
      }
    }
    return videoTrackIndex
  }

  private fun calculateTargetBitrate(width: Int, height: Int, fps: Float, sourceBitrate: Long): Int {
    val pixelCount = width * height
    val is60Fps = fps >= 45f

    val calculatedBitrate = when {
      pixelCount >= 3840 * 2160 -> if (is60Fps) 45_000_000 else 32_000_000 // 4K
      pixelCount >= 2560 * 1440 -> if (is60Fps) 26_000_000 else 18_000_000 // 1440p
      pixelCount >= 1920 * 1080 -> if (is60Fps) 18_000_000 else 12_500_000 // 1080p
      pixelCount >= 1280 * 720  -> if (is60Fps) 10_000_000 else 6_500_000  // 720p
      else -> 4_000_000
    }

    return if (sourceBitrate > 1_000_000L) {
      (sourceBitrate * 1.1).toInt().coerceIn(4_000_000, 50_000_000)
    } else {
      calculatedBitrate
    }
  }

  private suspend fun saveToGalleryOrAppStorage(
    context: Context,
    file: File,
    originalName: String
  ): Uri = withContext(Dispatchers.IO) {
    val baseName = originalName.substringBeforeLast(".")
    val outputFileName = "Walkbar_${baseName}_${System.currentTimeMillis()}.mp4"

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
      val values = ContentValues().apply {
        put(MediaStore.Video.Media.DISPLAY_NAME, outputFileName)
        put(MediaStore.Video.Media.MIME_TYPE, "video/mp4")
        put(MediaStore.Video.Media.RELATIVE_PATH, Environment.DIRECTORY_MOVIES + "/Walkbar")
        put(MediaStore.Video.Media.IS_PENDING, 1)
      }

      val resolver = context.contentResolver
      val collection = MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
      val uri = resolver.insert(collection, values)

      if (uri != null) {
        resolver.openOutputStream(uri)?.use { out ->
          FileInputStream(file).use { input ->
            input.copyTo(out)
          }
        }
        values.clear()
        values.put(MediaStore.Video.Media.IS_PENDING, 0)
        resolver.update(uri, values, null, null)
        return@withContext uri
      }
    }

    val moviesDir = context.getExternalFilesDir(Environment.DIRECTORY_MOVIES) ?: context.filesDir
    val dest = File(moviesDir, outputFileName)
    FileInputStream(file).use { input ->
      FileOutputStream(dest).use { out ->
        input.copyTo(out)
      }
    }

    try {
      androidx.core.content.FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        dest
      )
    } catch (_: Exception) {
      Uri.fromFile(dest)
    }
  }
}
