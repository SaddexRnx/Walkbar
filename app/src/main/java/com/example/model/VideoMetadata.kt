package com.example.model

import android.net.Uri

data class VideoMetadata(
  val uri: Uri,
  val fileName: String,
  val durationMs: Long,
  val rawWidth: Int,
  val rawHeight: Int,
  val rotationDegrees: Int,
  val fps: Float,
  val bitrateBps: Long,
  val videoMimeType: String?,
  val audioMimeType: String?,
  val hasAudio: Boolean,
  val fileSizeBytes: Long
) {
  // Effective width/height considering orientation rotation (e.g. 90 or 270 degrees)
  val effectiveWidth: Int
    get() = if (rotationDegrees == 90 || rotationDegrees == 270) rawHeight else rawWidth

  val effectiveHeight: Int
    get() = if (rotationDegrees == 90 || rotationDegrees == 270) rawWidth else rawHeight

  val aspectRatio: Float
    get() = if (effectiveHeight > 0) effectiveWidth.toFloat() / effectiveHeight.toFloat() else 9f / 16f

  val formattedDuration: String
    get() {
      val totalSeconds = (durationMs / 1000).toInt()
      val minutes = totalSeconds / 60
      val seconds = totalSeconds % 60
      return String.format("%d:%02d", minutes, seconds)
    }

  val formattedResolution: String
    get() = "${effectiveWidth} × ${effectiveHeight}"

  val formattedFps: String
    get() = "${fps.toInt()} FPS"

  val formattedBitrate: String
    get() {
      val mbps = bitrateBps.toDouble() / 1_000_000.0
      return if (mbps > 0.1) String.format("%.1f Mbps", mbps) else "Auto Bitrate"
    }
}
