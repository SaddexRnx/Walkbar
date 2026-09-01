package com.example.ui

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.characters.CharacterRegistry
import com.example.export.WalkbarVideoExporter
import com.example.media.SampleVideoGenerator
import com.example.media.VideoMetadataHelper
import com.example.model.AnimationBehavior
import com.example.model.CharacterOverlayConfig
import com.example.model.CharacterSizePreset
import com.example.model.ExportState
import com.example.model.VideoMetadata
import com.example.player.WalkbarPlayerManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class WalkbarViewModel(application: Application) : AndroidViewModel(application) {

  val playerManager = WalkbarPlayerManager(application, viewModelScope)

  private val _videoMetadata = MutableStateFlow<VideoMetadata?>(null)
  val videoMetadata: StateFlow<VideoMetadata?> = _videoMetadata.asStateFlow()

  private val _overlayConfig = MutableStateFlow(CharacterOverlayConfig())
  val overlayConfig: StateFlow<CharacterOverlayConfig> = _overlayConfig.asStateFlow()

  private val _exportState = MutableStateFlow<ExportState>(ExportState.Idle)
  val exportState: StateFlow<ExportState> = _exportState.asStateFlow()

  private val _isLoading = MutableStateFlow(false)
  val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

  private var exportJob: Job? = null

  fun selectVideo(uri: Uri) {
    viewModelScope.launch {
      _isLoading.value = true
      try {
        val localUri = VideoMetadataHelper.copyToLocalCacheIfNeeded(getApplication(), uri)
        val meta = VideoMetadataHelper.extractMetadata(getApplication(), localUri)
        _videoMetadata.value = meta
        playerManager.initialize(localUri, meta.durationMs)
      } catch (e: Exception) {
        e.printStackTrace()
      } finally {
        _isLoading.value = false
      }
    }
  }

  fun loadSampleVideo() {
    viewModelScope.launch {
      _isLoading.value = true
      try {
        val uri = SampleVideoGenerator.getOrCreateSampleVideo(getApplication())
        val localUri = VideoMetadataHelper.copyToLocalCacheIfNeeded(getApplication(), uri)
        val meta = VideoMetadataHelper.extractMetadata(getApplication(), localUri)
        _videoMetadata.value = meta
        playerManager.initialize(localUri, meta.durationMs)
      } catch (e: Exception) {
        e.printStackTrace()
      } finally {
        _isLoading.value = false
      }
    }
  }

  fun selectCharacter(id: String) {
    val char = CharacterRegistry.getById(id)
    _overlayConfig.value = _overlayConfig.value.copy(
      characterId = id,
      customScalePercent = char.defaultScale,
      verticalOffsetPercent = char.recommendedVerticalOffsetPercent
    )
  }

  fun setBehavior(behavior: AnimationBehavior) {
    _overlayConfig.value = _overlayConfig.value.copy(behavior = behavior)
  }

  fun setSizePreset(preset: CharacterSizePreset) {
    _overlayConfig.value = _overlayConfig.value.copy(
      sizePreset = preset,
      customScalePercent = preset.scaleFactor
    )
  }

  fun setCustomScale(scale: Float) {
    _overlayConfig.value = _overlayConfig.value.copy(customScalePercent = scale)
  }

  fun setVerticalOffset(offset: Float) {
    _overlayConfig.value = _overlayConfig.value.copy(verticalOffsetPercent = offset.coerceIn(0f, 0.25f))
  }

  fun setHorizontalRange(start: Float, end: Float) {
    _overlayConfig.value = _overlayConfig.value.copy(
      startXPercent = start.coerceIn(0f, 0.95f),
      endXPercent = end.coerceIn(0.05f, 1f)
    )
  }

  fun toggleReverseDirection() {
    _overlayConfig.value = _overlayConfig.value.copy(
      reverseDirection = !_overlayConfig.value.reverseDirection
    )
  }

  fun toggleInstagramGuide() {
    _overlayConfig.value = _overlayConfig.value.copy(
      showInstagramPreviewGuide = !_overlayConfig.value.showInstagramPreviewGuide
    )
  }

  fun setExportFpsOption(option: com.example.model.ExportFpsOption) {
    _overlayConfig.value = _overlayConfig.value.copy(exportFpsOption = option)
  }

  fun startExport() {
    val meta = _videoMetadata.value ?: return
    val config = _overlayConfig.value

    playerManager.pause()
    exportJob?.cancel()

    exportJob = viewModelScope.launch {
      WalkbarVideoExporter.exportVideoFlow(getApplication(), meta, config).collect { state ->
        _exportState.value = state
      }
    }
  }

  fun cancelExport() {
    exportJob?.cancel()
    exportJob = null
    _exportState.value = ExportState.Idle
  }

  fun resetExport() {
    _exportState.value = ExportState.Idle
  }

  fun clearVideo() {
    playerManager.release()
    _videoMetadata.value = null
    _exportState.value = ExportState.Idle
  }

  override fun onCleared() {
    super.onCleared()
    playerManager.release()
  }
}
