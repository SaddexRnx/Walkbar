package com.example.media

import android.content.Context
import android.media.MediaExtractor
import android.media.MediaFormat
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.provider.OpenableColumns
import com.example.model.VideoMetadata
import java.io.File
import java.io.FileOutputStream

object VideoMetadataHelper {

  /**
   * Copies content URI to a dedicated app cache file to guarantee permanent read access
   * across ExoPlayer, MediaMetadataRetriever, and MediaCodec hardware encoder.
   */
  fun copyToLocalCacheIfNeeded(context: Context, uri: Uri): Uri {
    if (uri.scheme == "file") return uri
    if (uri.scheme != "content") return uri

    try {
      val extension = when (context.contentResolver.getType(uri)) {
        "video/mp4" -> ".mp4"
        "video/quicktime" -> ".mov"
        "video/3gpp" -> ".3gp"
        "video/webm" -> ".webm"
        else -> ".mp4"
      }
      val targetFile = File(context.cacheDir, "input_source_${System.currentTimeMillis()}$extension")
      context.contentResolver.openInputStream(uri)?.use { input ->
        FileOutputStream(targetFile).use { output ->
          input.copyTo(output)
        }
      }
      if (targetFile.exists() && targetFile.length() > 0) {
        return Uri.fromFile(targetFile)
      }
    } catch (e: Exception) {
      e.printStackTrace()
    }
    return uri
  }

  fun extractMetadata(context: Context, uri: Uri): VideoMetadata {
    val retriever = MediaMetadataRetriever()
    var durationMs = 10000L
    var width = 1080
    var height = 1920
    var rotation = 0
    var fps = 30.0f
    var bitrate = 10_000_000L
    var videoMime: String? = MediaFormat.MIMETYPE_VIDEO_AVC
    var audioMime: String? = null
    var hasAudio = false
    var fileSizeBytes = 0L
    var fileName = "video.mp4"

    try {
      if (uri.scheme == "content" || uri.scheme == "file") {
        retriever.setDataSource(context, uri)
      } else {
        retriever.setDataSource(uri.toString())
      }

      retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLongOrNull()?.let {
        if (it > 0) durationMs = it
      }

      retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)?.toIntOrNull()?.let {
        if (it > 0) width = it
      }

      retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)?.toIntOrNull()?.let {
        if (it > 0) height = it
      }

      retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION)?.toIntOrNull()?.let {
        rotation = it
      }

      retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_BITRATE)?.toLongOrNull()?.let {
        if (it > 0) bitrate = it
      }

      retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_MIMETYPE)?.let {
        videoMime = it
      }

      retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_HAS_AUDIO)?.let {
        hasAudio = it.equals("yes", ignoreCase = true) || it == "1"
      }

      retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_CAPTURE_FRAMERATE)?.toFloatOrNull()?.let {
        if (it > 0) fps = it
      }

      // Query name and size from content resolver
      if (uri.scheme == "content") {
        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
          if (cursor.moveToFirst()) {
            val nameIdx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (nameIdx >= 0) {
              fileName = cursor.getString(nameIdx) ?: fileName
            }
            val sizeIdx = cursor.getColumnIndex(OpenableColumns.SIZE)
            if (sizeIdx >= 0) {
              fileSizeBytes = cursor.getLong(sizeIdx)
            }
          }
        }
      } else if (uri.scheme == "file" || uri.path != null) {
        val f = uri.path?.let { File(it) }
        if (f != null && f.exists()) {
          fileSizeBytes = f.length()
          fileName = f.name
        }
      }

      // If fps wasn't in metadata, try checking track format
      try {
        val extractor = MediaExtractor()
        extractor.setDataSource(context, uri, null)
        for (i in 0 until extractor.trackCount) {
          val format = extractor.getTrackFormat(i)
          val mime = format.getString(MediaFormat.KEY_MIME) ?: ""
          if (mime.startsWith("video/")) {
            if (format.containsKey(MediaFormat.KEY_FRAME_RATE)) {
              val trackFps = format.getInteger(MediaFormat.KEY_FRAME_RATE).toFloat()
              if (trackFps > 0) fps = trackFps
            }
          } else if (mime.startsWith("audio/")) {
            hasAudio = true
            audioMime = mime
          }
        }
        extractor.release()
      } catch (_: Exception) {
      }

    } catch (e: Exception) {
      e.printStackTrace()
    } finally {
      try {
        retriever.release()
      } catch (_: Exception) {}
    }

    return VideoMetadata(
      uri = uri,
      fileName = fileName,
      durationMs = durationMs,
      rawWidth = width,
      rawHeight = height,
      rotationDegrees = rotation,
      fps = fps,
      bitrateBps = bitrate,
      videoMimeType = videoMime,
      audioMimeType = audioMime,
      hasAudio = hasAudio,
      fileSizeBytes = fileSizeBytes
    )
  }
}
