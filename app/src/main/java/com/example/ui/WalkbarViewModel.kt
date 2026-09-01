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

  private val _errorMessage = MutableStateFlow<String?>(null)
  val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

  private var exportJob: Job? = null

  init {
    detectDeviceScreenAspectRatio()
  }

  private fun detectDeviceScreenAspectRatio() {
    try {
      val metrics = getApplication<Application>().resources.displayMetrics
      val pWidth = minOf(metrics.widthPixels, metrics.heightPixels).coerceAtLeast(320)
      val pHeight = maxOf(metrics.widthPixels, metrics.heightPixels).coerceAtLeast(640)
      val aspect = pWidth.toFloat() / pHeight.toFloat()
      val tallFactor = 9f / aspect
      val formattedRatio = String.format(java.util.Locale.US, "9:%.1f", tallFactor)
      _overlayConfig.value = _overlayConfig.value.copy(
        deviceScreenWidth = pWidth,
        deviceScreenHeight = pHeight,
        deviceScreenRatioFormatted = formattedRatio,
        framingMode = com.example.model.VideoFramingMode.MATCH_DEVICE_SCREEN
      )
    } catch (_: Exception) {}
  }

  fun dismissError() {
    _errorMessage.value = null
  }

  fun selectVideo(uri: Uri) {
    viewModelScope.launch {
      _isLoading.value = true
      _errorMessage.value = null
      try {
        val localUri = VideoMetadataHelper.copyToLocalCacheIfNeeded(getApplication(), uri)
        val meta = VideoMetadataHelper.extractMetadata(getApplication(), localUri)
        _videoMetadata.value = meta
        playerManager.initialize(localUri, meta.durationMs)
      } catch (e: Exception) {
        e.printStackTrace()
        _errorMessage.value = "Unable to load video: ${e.localizedMessage ?: "File format not supported or corrupted"}"
      } finally {
        _isLoading.value = false
      }
    }
  }

  fun loadSampleVideo() {
    viewModelScope.launch {
      _isLoading.value = true
      _errorMessage.value = null
      try {
        val uri = SampleVideoGenerator.getOrCreateSampleVideo(getApplication())
        val localUri = VideoMetadataHelper.copyToLocalCacheIfNeeded(getApplication(), uri)
        val meta = VideoMetadataHelper.extractMetadata(getApplication(), localUri)
        _videoMetadata.value = meta
        playerManager.initialize(localUri, meta.durationMs)
      } catch (e: Exception) {
        e.printStackTrace()
        _errorMessage.value = "Unable to generate demo video: ${e.localizedMessage ?: "Storage error"}"
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

  fun setTargetPlatform(platform: com.example.model.SocialPlatform) {
    _overlayConfig.value = _overlayConfig.value.copy(
      targetPlatform = platform,
      verticalOffsetPercent = platform.defaultVerticalOffset,
      startXPercent = platform.defaultStartX,
      endXPercent = platform.defaultEndX
    )
  }

  fun setFramingMode(mode: com.example.model.VideoFramingMode) {
    _overlayConfig.value = _overlayConfig.value.copy(framingMode = mode)
  }

  fun adjustVerticalOffsetDelta(delta: Float) {
    val current = _overlayConfig.value.verticalOffsetPercent
    val newOffset = (current + delta).coerceIn(0f, 0.25f)
    _overlayConfig.value = _overlayConfig.value.copy(
      verticalOffsetPercent = newOffset,
      targetPlatform = com.example.model.SocialPlatform.CUSTOM
    )
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

  fun toggleSafeZoneGuide() {
    _overlayConfig.value = _overlayConfig.value.copy(
      showSafeZoneGuide = !_overlayConfig.value.showSafeZoneGuide
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
    viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
      try {
        val cacheDir = getApplication<Application>().cacheDir
        cacheDir.listFiles { file -> file.name.startsWith("walkbar_export_") }?.forEach { it.delete() }
      } catch (_: Exception) {}
    }
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
