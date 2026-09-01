package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.example.model.ExportState
import com.example.ui.WalkbarViewModel
import com.example.ui.screens.EditorScreen
import com.example.ui.screens.ExportScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {

  private val viewModel: WalkbarViewModel by viewModels()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    com.example.characters.CharacterRegistry.initialize(this)
    enableEdgeToEdge()
    setContent {
      MyApplicationTheme {
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
          WalkbarApp(
            viewModel = viewModel,
            modifier = Modifier.padding(innerPadding)
          )
        }
      }
    }
  }
}

@Composable
fun WalkbarApp(
  viewModel: WalkbarViewModel,
  modifier: Modifier = Modifier
) {
  val videoMetadata by viewModel.videoMetadata.collectAsState()
  val overlayConfig by viewModel.overlayConfig.collectAsState()
  val exportState by viewModel.exportState.collectAsState()
  val isLoading by viewModel.isLoading.collectAsState()

  // Determine active screen state
  val currentScreen = when {
    exportState !is ExportState.Idle -> AppScreen.EXPORT
    videoMetadata != null -> AppScreen.EDITOR
    else -> AppScreen.HOME
  }

  // Handle system back navigation
  BackHandler(enabled = currentScreen != AppScreen.HOME) {
    when (currentScreen) {
      AppScreen.EXPORT -> {
        if (exportState is ExportState.Rendering) {
          viewModel.cancelExport()
        } else {
          viewModel.resetExport()
        }
      }
      AppScreen.EDITOR -> {
        viewModel.clearVideo()
      }
      AppScreen.HOME -> Unit
    }
  }

  AnimatedContent(
    targetState = currentScreen,
    transitionSpec = { fadeIn() togetherWith fadeOut() },
    label = "screenTransition",
    modifier = modifier.fillMaxSize()
  ) { screen ->
    when (screen) {
      AppScreen.HOME -> {
        HomeScreen(
          isLoading = isLoading,
          overlayConfig = overlayConfig,
          onCharacterSelected = { viewModel.selectCharacter(it) },
          onBehaviorChanged = { viewModel.setBehavior(it) },
          onVideoSelected = { uri ->
            viewModel.selectVideo(uri)
          },
          onSampleVideoRequested = {
            viewModel.loadSampleVideo()
          }
        )
      }

      AppScreen.EDITOR -> {
        val meta = videoMetadata
        if (meta != null) {
          EditorScreen(
            metadata = meta,
            overlayConfig = overlayConfig,
            playerManager = viewModel.playerManager,
            onCharacterSelected = { viewModel.selectCharacter(it) },
            onBehaviorChanged = { viewModel.setBehavior(it) },
            onPlatformSelected = { viewModel.setTargetPlatform(it) },
            onFramingModeChanged = { viewModel.setFramingMode(it) },
            onAdjustVerticalOffsetDelta = { viewModel.adjustVerticalOffsetDelta(it) },
            onSizePresetChanged = { viewModel.setSizePreset(it) },
            onCustomScaleChanged = { viewModel.setCustomScale(it) },
            onVerticalOffsetChanged = { viewModel.setVerticalOffset(it) },
            onHorizontalRangeChanged = { start, end -> viewModel.setHorizontalRange(start, end) },
            onToggleReverseDirection = { viewModel.toggleReverseDirection() },
            onToggleInstagramGuide = { viewModel.toggleInstagramGuide() },
            onExportFpsOptionChanged = { viewModel.setExportFpsOption(it) },
            onBackClicked = { viewModel.clearVideo() },
            onExportClicked = { viewModel.startExport() }
          )
        }
      }

      AppScreen.EXPORT -> {
        ExportScreen(
          exportState = exportState,
          overlayConfig = overlayConfig,
          onCancelClicked = { viewModel.cancelExport() },
          onDoneClicked = {
            viewModel.resetExport()
            viewModel.clearVideo()
          },
          onRetryClicked = { viewModel.startExport() }
        )
      }
    }
  }
}

enum class AppScreen {
  HOME,
  EDITOR,
  EXPORT
}
