package com.example.model

import android.net.Uri

sealed interface ExportState {
  data object Idle : ExportState
  
  data class Preparing(val statusMessage: String = "Analyzing video...") : ExportState
  
  data class Rendering(
    val progress: Float, // 0.0f to 1.0f
    val currentFrame: Int,
    val totalFrames: Int,
    val fps: Float,
    val statusMessage: String = "Compositing character..."
  ) : ExportState
  
  data object Finalizing : ExportState
  
  data class Success(
    val outputUri: Uri,
    val outputPath: String,
    val fileSizeBytes: Long,
    val durationMs: Long,
    val width: Int,
    val height: Int
  ) : ExportState
  
  data class Error(val userFriendlyMessage: String, val throwable: Throwable? = null) : ExportState
}
