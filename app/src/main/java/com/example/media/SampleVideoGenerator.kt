package com.example.media

import android.content.Context
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Shader
import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaFormat
import android.media.MediaMuxer
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.math.sin

object SampleVideoGenerator {

  suspend fun getOrCreateSampleVideo(context: Context): Uri = withContext(Dispatchers.IO) {
    val sampleFile = File(context.cacheDir, "walkbar_sample_reel.mp4")
    if (sampleFile.exists() && sampleFile.length() > 50_000) {
      return@withContext Uri.fromFile(sampleFile)
    }

    try {
      generateSampleClip(sampleFile)
    } catch (e: Exception) {
      e.printStackTrace()
    }
    Uri.fromFile(sampleFile)
  }

  private fun generateSampleClip(outputFile: File) {
    val width = 720
    val height = 1280
    val fps = 30
    val durationSeconds = 12
    val totalFrames = fps * durationSeconds
    val frameIntervalUs = 1_000_000L / fps
    val bitRate = 4_000_000

    val format = MediaFormat.createVideoFormat(MediaFormat.MIMETYPE_VIDEO_AVC, width, height).apply {
      setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface)
      setInteger(MediaFormat.KEY_BIT_RATE, bitRate)
      setInteger(MediaFormat.KEY_FRAME_RATE, fps)
      setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 1)
    }

    val encoder = MediaCodec.createEncoderByType(MediaFormat.MIMETYPE_VIDEO_AVC)
    encoder.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
    val inputSurface = encoder.createInputSurface()
    encoder.start()

    val muxer = MediaMuxer(outputFile.absolutePath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
    var videoTrackIndex = -1
    var muxerStarted = false
    val firstPtsHolder = longArrayOf(-1L)

    val bufferInfo = MediaCodec.BufferInfo()
    val paint = Paint(Paint.ANTI_ALIAS_FLAG)

    for (frame in 0 until totalFrames) {
      val ptsUs = frame * frameIntervalUs

      val canvas = try {
        inputSurface.lockHardwareCanvas()
      } catch (e: Exception) {
        inputSurface.lockCanvas(null)
      }
      try {
        val t = frame.toFloat() / totalFrames.toFloat()

        // Background Gradient (Twilight Sunset to Deep Blue)
        val shader = LinearGradient(
          0f, 0f, 0f, height.toFloat(),
          intArrayOf(
            Color.rgb(15, 23, 42),
            Color.rgb(49, 46, 129),
            Color.rgb(136, 19, 55),
            Color.rgb(251, 146, 60)
          ),
          floatArrayOf(0f, 0.35f, 0.7f, 1f),
          Shader.TileMode.CLAMP
        )
        paint.shader = shader
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)
        paint.shader = null

        // Glowing Sun in background
        paint.color = Color.argb(220, 254, 240, 138)
        canvas.drawCircle(width * 0.5f, height * 0.62f, 90f, paint)

        // Mountain silhouettes
        paint.color = Color.rgb(24, 24, 27)
        val mountainPath = android.graphics.Path().apply {
          moveTo(0f, height * 0.72f)
          lineTo(width * 0.25f, height * 0.64f)
          lineTo(width * 0.5f, height * 0.70f)
          lineTo(width * 0.75f, height * 0.60f)
          lineTo(width.toFloat(), height * 0.68f)
          lineTo(width.toFloat(), height.toFloat())
          lineTo(0f, height.toFloat())
          close()
        }
        canvas.drawPath(mountainPath, paint)

        // Scenic waves / ground line
        paint.color = Color.rgb(9, 9, 11)
        canvas.drawRect(0f, height * 0.88f, width.toFloat(), height.toFloat(), paint)

        // Subtle stars floating
        paint.color = Color.argb(180, 255, 255, 255)
        for (s in 0..15) {
          val starX = ((s * 137.5f) % width)
          val starY = (height * 0.1f + (s * 33f) % (height * 0.4f))
          val twinkle = (sin(t * 10f + s) * 2f + 3f)
          canvas.drawCircle(starX, starY, twinkle, paint)
        }

        // Title text in the center
        paint.color = Color.WHITE
        paint.textSize = 38f
        paint.textAlign = Paint.Align.CENTER
        paint.style = Paint.Style.FILL
        canvas.drawText("Walkbar Timeline Demo", width / 2f, height * 0.28f, paint)

        paint.textSize = 22f
        paint.color = Color.rgb(203, 213, 225)
        canvas.drawText("Your animated companion walks along the video bar", width / 2f, height * 0.33f, paint)

      } finally {
        inputSurface.unlockCanvasAndPost(canvas)
      }

      // Drain encoder
      videoTrackIndex = drainEncoder(
        encoder = encoder,
        bufferInfo = bufferInfo,
        muxer = muxer,
        currentVideoTrackIndex = videoTrackIndex,
        isMuxerStarted = muxerStarted,
        firstPtsUsHolder = firstPtsHolder
      ) { trackIdx ->
        videoTrackIndex = trackIdx
        muxerStarted = true
      }
    }

    // End of stream
    encoder.signalEndOfInputStream()

    // Drain remaining
    var eos = false
    while (!eos) {
      val outIndex = encoder.dequeueOutputBuffer(bufferInfo, 10000)
      if (outIndex == MediaCodec.INFO_TRY_AGAIN_LATER) {
        break
      } else if (outIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
        if (!muxerStarted) {
          videoTrackIndex = muxer.addTrack(encoder.outputFormat)
          muxer.start()
          muxerStarted = true
        }
      } else if (outIndex >= 0) {
        if ((bufferInfo.flags and MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0) {
          bufferInfo.size = 0
        }
        val encodedData = encoder.getOutputBuffer(outIndex)
        if (encodedData != null && bufferInfo.size > 0 && muxerStarted && videoTrackIndex >= 0) {
          if (firstPtsHolder[0] < 0L) {
            firstPtsHolder[0] = bufferInfo.presentationTimeUs
          }
          bufferInfo.presentationTimeUs = (bufferInfo.presentationTimeUs - firstPtsHolder[0]).coerceAtLeast(0L)
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

    try {
      encoder.stop()
      encoder.release()
      inputSurface.release()
      if (muxerStarted) {
        muxer.stop()
        muxer.release()
      }
    } catch (e: Exception) {
      e.printStackTrace()
    }
  }

  private fun drainEncoder(
    encoder: MediaCodec,
    bufferInfo: MediaCodec.BufferInfo,
    muxer: MediaMuxer,
    currentVideoTrackIndex: Int,
    isMuxerStarted: Boolean,
    firstPtsUsHolder: LongArray,
    onTrackAdded: (Int) -> Unit
  ): Int {
    var muxerStarted = isMuxerStarted
    var videoTrackIndex = currentVideoTrackIndex

    while (true) {
      val outIndex = encoder.dequeueOutputBuffer(bufferInfo, 0)
      if (outIndex == MediaCodec.INFO_TRY_AGAIN_LATER) {
        break
      } else if (outIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
        if (!muxerStarted) {
          videoTrackIndex = muxer.addTrack(encoder.outputFormat)
          muxer.start()
          muxerStarted = true
          onTrackAdded(videoTrackIndex)
        }
      } else if (outIndex >= 0) {
        if ((bufferInfo.flags and MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0) {
          bufferInfo.size = 0
        }
        val encodedData = encoder.getOutputBuffer(outIndex)
        if (encodedData != null && bufferInfo.size > 0 && muxerStarted && videoTrackIndex >= 0) {
          if (firstPtsUsHolder[0] < 0L) {
            firstPtsUsHolder[0] = bufferInfo.presentationTimeUs
          }
          bufferInfo.presentationTimeUs = (bufferInfo.presentationTimeUs - firstPtsUsHolder[0]).coerceAtLeast(0L)
          encodedData.position(bufferInfo.offset)
          encodedData.limit(bufferInfo.offset + bufferInfo.size)
          muxer.writeSampleData(videoTrackIndex, encodedData, bufferInfo)
        }
        encoder.releaseOutputBuffer(outIndex, false)
      }
    }
    return videoTrackIndex
  }
}
