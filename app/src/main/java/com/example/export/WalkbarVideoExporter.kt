package com.example.export

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.Rect
import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaExtractor
import android.media.MediaFormat
import android.media.MediaMetadataRetriever
import android.media.MediaMuxer
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import com.example.characters.CharacterRegistry
import com.example.characters.CharacterRenderer
import com.example.characters.WalkCycleMath
import com.example.model.CharacterOverlayConfig
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
   * Main export flow providing reactive progress updates
   */
  fun exportVideoFlow(
    context: Context,
    metadata: VideoMetadata,
    config: CharacterOverlayConfig
  ): Flow<ExportState> = flow {
    emit(ExportState.Preparing("Reading video properties & preparing encoder..."))

    val character = CharacterRegistry.getById(config.characterId)
    
    // Scale down dimensions if exceeding 1080p to maximize speed and minimize memory
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
    val fps = metadata.fps.coerceIn(15f, 30f) // 30fps is optimal for fast encoding and buttery smooth playback
    val totalFrames = ((durationMs / 1000.0) * fps).toInt().coerceAtLeast(1)
    val frameIntervalUs = (1_000_000.0 / fps).toLong()

    // Calculate quality bitrate appropriate for resolution and frame rate
    val targetBitrate = calculateTargetBitrate(width, height, fps, metadata.bitrateBps)

    val tempOutputFile = File(context.cacheDir, "walkbar_export_${System.currentTimeMillis()}.mp4")

    try {
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
      val inputSurface = encoder.createInputSurface()
      encoder.start()

      val eglEncoder = EglSurfaceEncoder(inputSurface)

      // 2. Setup Audio Extraction if present
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
        if (metadata.uri.scheme == "content" || metadata.uri.scheme == "file") {
          retriever.setDataSource(context, metadata.uri)
        } else {
          retriever.setDataSource(metadata.uri.toString())
        }
      } catch (e: Exception) {
        e.printStackTrace()
      }

      val bufferInfo = MediaCodec.BufferInfo()
      val charSize = height * config.customScalePercent
      val paint = Paint(Paint.ANTI_ALIAS_FLAG)
      val srcRect = Rect()
      val dstRect = Rect(0, 0, width, height)

      val compositeBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
      val canvas = Canvas(compositeBitmap)

      // Start processing frames
      for (frameIndex in 0 until totalFrames) {
        if (!coroutineContext.isActive) {
          throw CancellationException("Export was cancelled by user")
        }

        val frameTimestampUs = frameIndex * frameIntervalUs
        val currentMs = frameTimestampUs / 1000L
        val ptsNs = frameTimestampUs * 1000L

        // Retrieve source video frame at exact timestamp
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
        } catch (e: Exception) {
          null
        }

        // Draw onto the composite bitmap
        canvas.drawColor(Color.BLACK, PorterDuff.Mode.CLEAR)

        // 1. Draw source video frame
        if (sourceBitmap != null) {
          srcRect.set(0, 0, sourceBitmap.width, sourceBitmap.height)
          canvas.drawBitmap(sourceBitmap, srcRect, dstRect, paint)
          sourceBitmap.recycle()
        } else {
          canvas.drawColor(Color.rgb(18, 20, 24))
        }

        // 2. Composite character overlay (ONLY character, NEVER progress bar)
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

        // Render frame to encoder input surface with deterministic PTS timestamp
        eglEncoder.renderBitmapFrame(compositeBitmap, ptsNs, width, height)

        // Drain encoded packets
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

        // Progress notification
        val progress = (frameIndex + 1).toFloat() / totalFrames.toFloat()
        emit(
          ExportState.Rendering(
            progress = progress,
            currentFrame = frameIndex + 1,
            totalFrames = totalFrames,
            fps = fps,
            statusMessage = "Compositing frame ${frameIndex + 1} of $totalFrames (${(progress * 100).toInt()}%)"
          )
        )
      }

      compositeBitmap.recycle()

      // End of Stream
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
            // Codec specific data is already supplied via output format
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

      // 3. Mux Original Audio track without recompression (Bit-for-bit passthrough)
      if (audioExtractor != null && audioTrackIndex >= 0 && muxerStarted) {
        emit(ExportState.Preparing("Copying original audio stream..."))
        val maxBufferSize = 256 * 1024
        val audioBuffer = ByteBuffer.allocateDirect(maxBufferSize)
        val audioBufferInfo = MediaCodec.BufferInfo()
        var firstAudioPtsUs = -1L

        while (true) {
          val sampleSize = audioExtractor.readSampleData(audioBuffer, 0)
          if (sampleSize < 0) break

          val sampleFlags = audioExtractor.sampleFlags
          // Skip codec config samples because the track format already provides CSD
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

      emit(ExportState.Finalizing)

      // Cleanup encoder, extractor, and muxer
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

      // 4. Save file to Gallery / MediaStore so user can share directly
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

    } catch (c: CancellationException) {
      if (tempOutputFile.exists()) tempOutputFile.delete()
      emit(ExportState.Error("Export cancelled."))
    } catch (e: Throwable) {
      e.printStackTrace()
      if (tempOutputFile.exists()) tempOutputFile.delete()
      emit(ExportState.Error("Export failed: ${e.localizedMessage ?: "Unknown error"}", e))
    }
  }.flowOn(Dispatchers.Default)

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
          // Ignore codec specific data buffer because format was configured during addTrack
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
      // Preserve original high bitrate with slight headroom for crisp overlay
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

    // Fallback: Copy to external files directory or return file uri
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
