@file:OptIn(androidx.media3.common.util.UnstableApi::class, androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.example.ui.screens

import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AspectRatio
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Crop
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.CropFree
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RangeSlider
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import com.example.characters.CharacterRegistry
import com.example.characters.CharacterRenderer
import com.example.characters.WalkCycleMath
import com.example.model.AnimationBehavior
import com.example.model.CharacterOverlayConfig
import com.example.model.CharacterSizePreset
import com.example.model.ExportFpsOption
import com.example.model.SocialPlatform
import com.example.model.VideoFramingMode
import com.example.model.VideoMetadata
import com.example.player.WalkbarPlayerManager
import com.example.ui.components.AboutAppSheet
import com.example.ui.components.PlatformOverlayGuide
import com.example.ui.components.SafeZoneOverlayGuide
import com.example.ui.components.SleekCharacterPicker
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.AccentGold
import com.example.ui.theme.AccentGreen
import com.example.ui.theme.AccentPrimary
import com.example.ui.theme.AccentPrimaryActive
import com.example.ui.theme.AccentPrimaryLight
import com.example.ui.theme.AccentPrimaryMuted
import com.example.ui.theme.AccentRed
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkBorderSubtle
import com.example.ui.theme.DarkSurfaceCard
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun EditorScreen(
  metadata: VideoMetadata,
  overlayConfig: CharacterOverlayConfig,
  playerManager: WalkbarPlayerManager,
  onCharacterSelected: (String) -> Unit,
  onBehaviorChanged: (AnimationBehavior) -> Unit,
  onPlatformSelected: (SocialPlatform) -> Unit,
  onFramingModeChanged: (VideoFramingMode) -> Unit,
  onAdjustVerticalOffsetDelta: (Float) -> Unit,
  onSizePresetChanged: (CharacterSizePreset) -> Unit,
  onCustomScaleChanged: (Float) -> Unit,
  onVerticalOffsetChanged: (Float) -> Unit,
  onHorizontalRangeChanged: (Float, Float) -> Unit,
  onToggleReverseDirection: () -> Unit,
  onToggleInstagramGuide: () -> Unit,
  onToggleSafeZoneGuide: () -> Unit,
  onExportFpsOptionChanged: (ExportFpsOption) -> Unit,
  onBackClicked: () -> Unit,
  onExportClicked: () -> Unit,
  modifier: Modifier = Modifier
) {
  val isPlaying by playerManager.isPlaying.collectAsState()
  val currentPositionMs by playerManager.currentPositionMs.collectAsState()
  val durationMs by playerManager.durationMs.collectAsState()

  val effectiveDurationMs = if (durationMs > 0) durationMs else metadata.durationMs.coerceAtLeast(1000L)
  val currentProgress = WalkCycleMath.calculateProgress(currentPositionMs, effectiveDurationMs)

  val selectedCharacter = remember(overlayConfig.characterId) {
    CharacterRegistry.getById(overlayConfig.characterId)
  }

  var selectedTab by remember { mutableIntStateOf(0) }
  var isCleanPreviewMode by remember { mutableStateOf(false) }
  var showAboutSheet by remember { mutableStateOf(false) }
  var isDraggingPosition by remember { mutableStateOf(false) }

  val tabs = listOf(
    "Companion",
    "Platform & Height",
    "Locomotion & FPS",
    "Framing & Specs"
  )

  DisposableEffect(Unit) {
    onDispose {
      playerManager.pause()
    }
  }

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(DarkBackground)
  ) {
    // ─── 1. TOP APP BAR ───
    TopAppBar(
      title = {
        Column {
          Text(
            text = "Walkbar Studio",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary,
            letterSpacing = (-0.2).sp
          )
          Text(
            text = "${selectedCharacter.name} • ${overlayConfig.targetPlatform.displayName}",
            fontSize = 11.sp,
            color = AccentPrimaryLight,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
          )
        }
      },
      navigationIcon = {
        IconButton(
          onClick = onBackClicked,
          modifier = Modifier
            .size(44.dp)
            .testTag("editor_back_button")
        ) {
          Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = "Back",
            tint = TextPrimary
          )
        }
      },
      actions = {
        // About App Sheet Button
        IconButton(
          onClick = { showAboutSheet = true },
          modifier = Modifier.testTag("editor_about_button")
        ) {
          Icon(
            imageVector = Icons.Default.Info,
            contentDescription = "About App & Developer",
            tint = TextSecondary
          )
        }

        // Toggle Clean View vs Overlay
        IconButton(
          onClick = { isCleanPreviewMode = !isCleanPreviewMode },
          modifier = Modifier.testTag("toggle_clean_preview")
        ) {
          Icon(
            imageVector = if (isCleanPreviewMode) Icons.Default.VisibilityOff else Icons.Default.Visibility,
            contentDescription = if (isCleanPreviewMode) "Show Character Overlay" else "Hide Character (Clean)",
            tint = if (isCleanPreviewMode) AccentGold else TextSecondary
          )
        }

        // Toggle Safe Zone Crop Guide
        IconButton(
          onClick = onToggleSafeZoneGuide,
          modifier = Modifier.testTag("toggle_safe_zone_guide")
        ) {
          Icon(
            imageVector = Icons.Default.CropFree,
            contentDescription = "Toggle Safe Zone Crop Guide",
            tint = if (overlayConfig.showSafeZoneGuide) AccentCyan else TextMuted
          )
        }

        // Toggle Reference Guide Simulation
        IconButton(
          onClick = onToggleInstagramGuide,
          modifier = Modifier.testTag("toggle_instagram_guide")
        ) {
          Icon(
            imageVector = Icons.Default.Tune,
            contentDescription = "Toggle Timeline Reference Guide",
            tint = if (overlayConfig.showInstagramPreviewGuide) AccentPrimaryLight else TextMuted
          )
        }

        Spacer(modifier = Modifier.width(4.dp))

        // Save & Export Button with Gradient + Shadow
        Surface(
          onClick = onExportClicked,
          shape = CircleShape,
          color = Color.Transparent,
          modifier = Modifier
            .padding(end = 12.dp)
            .shadow(12.dp, CircleShape, spotColor = AccentPrimary)
            .testTag("export_video_button")
        ) {
          Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
              .background(Brush.linearGradient(listOf(AccentPrimary, AccentPrimaryActive)))
              .padding(horizontal = 16.dp, vertical = 7.dp)
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                imageVector = Icons.Default.Save,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(15.dp)
              )
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = "EXPORT",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.8.sp,
                color = Color.White
              )
            }
          }
        }
      },
      colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkBackground)
    )

    // ─── 2. VIDEO VIEWPORT WITH DIRECT INTERACTIVE TOUCH DRAGGING (1.6f weight) ───
    Box(
      contentAlignment = Alignment.Center,
      modifier = Modifier
        .fillMaxWidth()
        .weight(1.6f)
        .background(Color(0xFF07070A))
        .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
      BoxWithConstraints(
        contentAlignment = Alignment.Center,
        modifier = Modifier.fillMaxSize()
      ) {
        // Calculate target aspect ratio based on Framing Mode
        val targetAspect = overlayConfig.getEffectiveAspectRatio(metadata.aspectRatio)

        val containerAspect = maxWidth / maxHeight
        val (contentWidth, contentHeight) = if (containerAspect > targetAspect) {
          Pair(maxHeight * targetAspect, maxHeight)
        } else {
          Pair(maxWidth, maxWidth / targetAspect)
        }

        Box(
          modifier = Modifier
            .size(contentWidth, contentHeight)
            .shadow(24.dp, RoundedCornerShape(20.dp), spotColor = AccentPrimary.copy(alpha = 0.35f))
            .clip(RoundedCornerShape(20.dp))
            .border(1.dp, DarkBorder, RoundedCornerShape(20.dp))
            .background(Color(0xFF0D0D12))
            .pointerInput(Unit) {
              detectDragGestures(
                onDragStart = { isDraggingPosition = true },
                onDragEnd = { isDraggingPosition = false },
                onDragCancel = { isDraggingPosition = false },
                onDrag = { change, dragAmount ->
                  change.consume()
                  val heightPx = size.height.toFloat()
                  if (heightPx > 0f) {
                    val deltaPercent = -dragAmount.y / heightPx
                    onAdjustVerticalOffsetDelta(deltaPercent)
                  }
                }
              )
            }
            .pointerInput(Unit) {
              detectTapGestures(
                onTap = { offset ->
                  val heightPx = size.height.toFloat()
                  if (heightPx > 0f) {
                    val touchOffsetPercent = (1f - (offset.y / heightPx)).coerceIn(0f, 0.25f)
                    onVerticalOffsetChanged(touchOffsetPercent)
                  }
                },
                onDoubleTap = {
                  playerManager.togglePlayPause()
                }
              )
            }
        ) {
          // Media3 ExoPlayer View (ZOOM mode when center-cropping to fill frame)
          AndroidView(
            factory = { ctx ->
              PlayerView(ctx).apply {
                player = playerManager.getPlayer()
                useController = false
                setShutterBackgroundColor(android.graphics.Color.TRANSPARENT)
                resizeMode = if (overlayConfig.framingMode != VideoFramingMode.ORIGINAL) {
                  AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                } else {
                  AspectRatioFrameLayout.RESIZE_MODE_FIT
                }
                layoutParams = FrameLayout.LayoutParams(
                  ViewGroup.LayoutParams.MATCH_PARENT,
                  ViewGroup.LayoutParams.MATCH_PARENT
                )
              }
            },
            update = { playerView ->
              val currentPlayer = playerManager.getPlayer()
              if (playerView.player != currentPlayer) {
                playerView.player = currentPlayer
              }
              playerView.resizeMode = if (overlayConfig.framingMode != VideoFramingMode.ORIGINAL) {
                AspectRatioFrameLayout.RESIZE_MODE_ZOOM
              } else {
                AspectRatioFrameLayout.RESIZE_MODE_FIT
              }
            },
            modifier = Modifier.fillMaxSize()
          )

          // Subtle vignette effect
          Box(
            modifier = Modifier
              .fillMaxSize()
              .background(
                Brush.radialGradient(
                  colors = listOf(Color.Transparent, Color(0x33000000)),
                  radius = 800f
                )
              )
          )

          // Walking Companion Overlay Canvas with Liquid Smooth Motion
          if (!isCleanPreviewMode) {
            Canvas(modifier = Modifier.fillMaxSize()) {
              CharacterRenderer.renderInCompose(
                drawScope = this,
                character = selectedCharacter,
                config = overlayConfig,
                currentTimeMs = currentPositionMs,
                durationMs = effectiveDurationMs,
                isPlaying = isPlaying
              )
            }
          }

          // Native Platform Reference Guide Overlay (TikTok, YouTube Shorts, Instagram Reels, FB, etc.)
          if (overlayConfig.showPlatformGuide) {
            PlatformOverlayGuide(
              platform = overlayConfig.targetPlatform,
              progress = currentProgress,
              modifier = Modifier.fillMaxSize()
            )
          }

          // Visual Safe Zone Crop-Risk Guide Overlay
          if (overlayConfig.showSafeZoneGuide) {
            SafeZoneOverlayGuide(
              verticalOffsetPercent = overlayConfig.verticalOffsetPercent,
              modifier = Modifier.fillMaxSize()
            )
          }

          // Interactive Dragging Height Indicator Pill
          if (isDraggingPosition) {
            Surface(
              color = Color(0xEE1B1B24),
              shape = RoundedCornerShape(20.dp),
              border = BorderStroke(1.5.dp, AccentPrimary),
              modifier = Modifier
                .align(Alignment.Center)
                .padding(12.dp)
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
              ) {
                Icon(Icons.Default.TouchApp, contentDescription = null, tint = AccentCyan, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                  text = "Height: ${(overlayConfig.verticalOffsetPercent * 100).toInt()}% • Drag or Tap to Align",
                  fontSize = 12.5.sp,
                  fontWeight = FontWeight.Bold,
                  color = Color.White
                )
              }
            }
          }

          // Top Right Platform & Specs Frosted Glass Badge
          Surface(
            color = Color(0xAA15151C),
            shape = RoundedCornerShape(8.dp),
            border = BorderStroke(0.5.dp, Color(0x33FFFFFF)),
            modifier = Modifier
              .align(Alignment.TopEnd)
              .padding(8.dp)
          ) {
            Text(
              text = "${overlayConfig.targetPlatform.iconEmoji} ${overlayConfig.targetPlatform.displayName} (${(overlayConfig.verticalOffsetPercent * 100).toInt()}%)",
              fontSize = 9.5.sp,
              fontFamily = FontFamily.Monospace,
              fontWeight = FontWeight.SemiBold,
              color = AccentCyan,
              modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
            )
          }

          // Clean mode frosted glass badge
          if (isCleanPreviewMode) {
            Surface(
              color = Color(0xDD2D1E0A),
              shape = RoundedCornerShape(8.dp),
              border = BorderStroke(0.5.dp, AccentGold),
              modifier = Modifier
                .align(Alignment.TopStart)
                .padding(8.dp)
            ) {
              Text(
                text = "CLEAN ORIGINAL PREVIEW",
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                color = AccentGold,
                modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
              )
            }
          }

          // Play / Pause center feedback indicator
          if (!isPlaying && !isDraggingPosition) {
            Box(
              contentAlignment = Alignment.Center,
              modifier = Modifier
                .align(Alignment.Center)
                .size(52.dp)
                .clip(CircleShape)
                .background(Color(0x99000000))
                .border(1.dp, Color(0x33FFFFFF), CircleShape)
                .clickable { playerManager.togglePlayPause() }
            ) {
              Icon(
                imageVector = Icons.Default.PlayArrow,
                contentDescription = "Play",
                tint = Color.White,
                modifier = Modifier.size(30.dp)
              )
            }
          }
        }
      }
    }

    // ─── 3. SCRUBBER TIMELINE BAR ───
    Surface(
      color = DarkBackground,
      modifier = Modifier.fillMaxWidth()
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 14.dp, vertical = 2.dp)
      ) {
        IconButton(
          onClick = { playerManager.togglePlayPause() },
          modifier = Modifier
            .size(34.dp)
            .testTag("play_pause_button")
        ) {
          Icon(
            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
            contentDescription = if (isPlaying) "Pause" else "Play",
            tint = AccentPrimary
          )
        }

        Spacer(modifier = Modifier.width(4.dp))

        val currentSec = (currentPositionMs / 1000).toInt()
        val totalSec = (effectiveDurationMs / 1000).toInt()
        val timeLabel = String.format("%d:%02d / %d:%02d", currentSec / 60, currentSec % 60, totalSec / 60, totalSec % 60)

        Text(
          text = timeLabel,
          fontSize = 11.sp,
          fontWeight = FontWeight.Medium,
          fontFamily = FontFamily.Monospace,
          color = TextSecondary,
          modifier = Modifier.width(80.dp)
        )

        Slider(
          value = currentProgress,
          onValueChange = { normPos ->
            val targetMs = (normPos * effectiveDurationMs).toLong()
            playerManager.seekTo(targetMs)
          },
          colors = SliderDefaults.colors(
            thumbColor = Color.White,
            activeTrackColor = AccentPrimary,
            inactiveTrackColor = DarkBorder
          ),
          modifier = Modifier
            .weight(1f)
            .testTag("timeline_slider")
        )
      }
    }

    // ─── 4. BEAUTIFUL SCROLLABLE SETTINGS MENU (0.75f weight) ───
    Surface(
      color = DarkSurfaceElevated,
      shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
      border = BorderStroke(1.dp, DarkBorder),
      modifier = Modifier
        .fillMaxWidth()
        .weight(0.75f)
    ) {
      Column(
        modifier = Modifier.fillMaxSize()
      ) {
        // Tab Headers Row
        ScrollableTabRow(
          selectedTabIndex = selectedTab,
          containerColor = DarkSurfaceElevated,
          contentColor = AccentPrimary,
          edgePadding = 12.dp,
          indicator = { tabPositions ->
            TabRowDefaults.SecondaryIndicator(
              Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
              color = AccentPrimary,
              height = 3.dp
            )
          },
          divider = {}
        ) {
          tabs.forEachIndexed { index, title ->
            Tab(
              selected = selectedTab == index,
              onClick = { selectedTab = index },
              text = {
                Text(
                  text = title,
                  fontSize = 12.5.sp,
                  fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Medium,
                  color = if (selectedTab == index) TextPrimary else TextMuted
                )
              }
            )
          }
        }

        // Scrollable Tab Body
        val scrollState = rememberScrollState()
        Column(
          modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 14.dp, vertical = 10.dp),
          verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          when (selectedTab) {
            // ─── TAB 0: COMPANION & CATEGORIES ───
            0 -> {
              SleekCharacterPicker(
                selectedCharacterId = overlayConfig.characterId,
                behavior = overlayConfig.behavior,
                onCharacterSelected = onCharacterSelected
              )

              // Active Companion Spotlight Detail Card
              Card(
                colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, DarkBorder),
                modifier = Modifier.fillMaxWidth()
              ) {
                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
                ) {
                  // Mini preview Canvas
                  Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                      .size(54.dp)
                      .clip(RoundedCornerShape(12.dp))
                      .background(AccentPrimaryMuted)
                      .border(1.dp, AccentPrimaryLight, RoundedCornerShape(12.dp))
                  ) {
                    Canvas(modifier = Modifier.size(42.dp)) {
                      val w = size.width
                      val h = size.height
                      val charSize = h * 0.8f
                      val phase = ((currentPositionMs % 480L).toFloat() / 480f)
                      drawContext.canvas.nativeCanvas.let { canvas ->
                        CharacterRenderer.drawCharacter(
                          canvas = canvas,
                          character = selectedCharacter,
                          behavior = overlayConfig.behavior,
                          centerX = w * 0.5f,
                          bottomY = h * 0.88f,
                          size = charSize,
                          phase = phase,
                          facingRight = true,
                          currentTimeMs = currentPositionMs
                        )
                      }
                    }
                  }

                  Spacer(modifier = Modifier.width(12.dp))

                  Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                      Text(
                        text = selectedCharacter.name,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                      )
                      Spacer(modifier = Modifier.width(6.dp))
                      Surface(
                        color = AccentPrimaryMuted,
                        shape = RoundedCornerShape(6.dp)
                      ) {
                        Text(
                          text = selectedCharacter.category.displayName,
                          fontSize = 9.5.sp,
                          color = AccentPrimaryLight,
                          fontWeight = FontWeight.SemiBold,
                          modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                      }
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                      text = selectedCharacter.description,
                      fontSize = 11.sp,
                      color = TextSecondary,
                      maxLines = 2,
                      overflow = TextOverflow.Ellipsis
                    )
                  }
                }
              }
            }

            // ─── TAB 1: TARGET PLATFORM & HEIGHT ALIGNMENT ───
            1 -> {
              Text(
                text = "TARGET SOCIAL PLATFORM PRESETS",
                fontSize = 10.5.sp,
                fontWeight = FontWeight.Bold,
                color = TextMuted,
                letterSpacing = 0.6.sp
              )

              LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
              ) {
                items(SocialPlatform.entries.toTypedArray(), key = { it.id }) { platform ->
                  val isSelected = overlayConfig.targetPlatform == platform
                  FilterChip(
                    selected = isSelected,
                    onClick = { onPlatformSelected(platform) },
                    label = {
                      Text(
                        text = "${platform.iconEmoji} ${platform.displayName}",
                        fontSize = 11.5.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                      )
                    },
                    shape = RoundedCornerShape(10.dp),
                    colors = FilterChipDefaults.filterChipColors(
                      selectedContainerColor = AccentPrimary,
                      selectedLabelColor = Color.White,
                      containerColor = DarkSurfaceCard,
                      labelColor = TextSecondary
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                      borderColor = DarkBorder,
                      selectedBorderColor = AccentPrimary,
                      enabled = true,
                      selected = isSelected
                    )
                  )
                }
              }

              // Direct Drag & Height Stepper Card
              Card(
                colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, DarkBorder),
                modifier = Modifier.fillMaxWidth()
              ) {
                Column(
                  modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                  verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                  Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                  ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                      Icon(Icons.Default.TouchApp, contentDescription = null, tint = AccentCyan, modifier = Modifier.size(16.dp))
                      Spacer(modifier = Modifier.width(6.dp))
                      Text(
                        text = "Vertical Height Calibration",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                      )
                    }

                    Text(
                      text = "${String.format("%.1f", overlayConfig.verticalOffsetPercent * 100)}%",
                      fontSize = 13.sp,
                      fontFamily = FontFamily.Monospace,
                      fontWeight = FontWeight.Bold,
                      color = AccentCyan
                    )
                  }

                  // Precision Stepper Row (+0.2% / -0.2%)
                  Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                  ) {
                    Button(
                      onClick = { onAdjustVerticalOffsetDelta(-0.002f) },
                      shape = RoundedCornerShape(8.dp),
                      colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceElevated),
                      contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                      modifier = Modifier.weight(1f)
                    ) {
                      Icon(Icons.Default.Remove, contentDescription = null, tint = TextPrimary, modifier = Modifier.size(14.dp))
                      Spacer(modifier = Modifier.width(4.dp))
                      Text("Down 0.2%", fontSize = 10.5.sp, color = TextPrimary)
                    }

                    Button(
                      onClick = { onAdjustVerticalOffsetDelta(0.002f) },
                      shape = RoundedCornerShape(8.dp),
                      colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceElevated),
                      contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                      modifier = Modifier.weight(1f)
                    ) {
                      Icon(Icons.Default.Add, contentDescription = null, tint = TextPrimary, modifier = Modifier.size(14.dp))
                      Spacer(modifier = Modifier.width(4.dp))
                      Text("Up 0.2%", fontSize = 10.5.sp, color = TextPrimary)
                    }
                  }

                  // Vertical Slider
                  Slider(
                    value = overlayConfig.verticalOffsetPercent,
                    onValueChange = onVerticalOffsetChanged,
                    valueRange = 0.00f..0.22f,
                    colors = SliderDefaults.colors(
                      thumbColor = Color.White,
                      activeTrackColor = AccentPrimary,
                      inactiveTrackColor = DarkBorder
                    ),
                    modifier = Modifier.testTag("vertical_offset_slider")
                  )

                  // Inline Crop Risk Warning when placed in bottom danger zone
                  if (overlayConfig.isInCropRiskZone()) {
                    Surface(
                      color = Color(0x28EF4444),
                      shape = RoundedCornerShape(10.dp),
                      border = BorderStroke(1.dp, Color(0x77EF4444)),
                      modifier = Modifier.fillMaxWidth()
                    ) {
                      Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(10.dp)
                      ) {
                        Icon(
                          imageVector = Icons.Default.WarningAmber,
                          contentDescription = null,
                          tint = AccentRed,
                          modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                          text = "This position may get cropped when uploaded to some apps — consider moving up.",
                          fontSize = 11.sp,
                          color = TextPrimary,
                          lineHeight = 15.sp
                        )
                      }
                    }
                  }

                  Text(
                    text = "💡 Tip: You can also touch or drag anywhere on the video preview above to position the character directly on your progress bar.",
                    fontSize = 10.sp,
                    color = TextSecondary,
                    lineHeight = 14.sp
                  )
                }
              }

              // Safe Zone & Visual Alignment Tools Card
              Card(
                colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, DarkBorder),
                modifier = Modifier.fillMaxWidth()
              ) {
                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.SpaceBetween,
                  modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
                ) {
                  Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                  ) {
                    Icon(
                      imageVector = Icons.Default.CropFree,
                      contentDescription = null,
                      tint = if (overlayConfig.showSafeZoneGuide) AccentCyan else TextMuted,
                      modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                      Text(
                        text = "10-15% Crop Safe Zone Guide",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                      )
                      Text(
                        text = "Visualizes top & bottom areas at risk of platform cropping",
                        fontSize = 10.sp,
                        color = TextSecondary
                      )
                    }
                  }

                  FilterChip(
                    selected = overlayConfig.showSafeZoneGuide,
                    onClick = onToggleSafeZoneGuide,
                    shape = RoundedCornerShape(8.dp),
                    label = {
                      Text(
                        text = if (overlayConfig.showSafeZoneGuide) "Visible" else "Off",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                      )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                      selectedContainerColor = AccentCyan,
                      selectedLabelColor = Color.Black,
                      containerColor = DarkSurfaceElevated,
                      labelColor = TextSecondary
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                      borderColor = DarkBorder,
                      selectedBorderColor = AccentCyan,
                      enabled = true,
                      selected = overlayConfig.showSafeZoneGuide
                    )
                  )
                }
              }

              // Quick Snap Presets Card
              Card(
                colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, DarkBorder),
                modifier = Modifier.fillMaxWidth()
              ) {
                Column(
                  modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                  verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                  Text(
                    text = "🎯 Quick Height Snap Presets",
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                  )

                  Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                  ) {
                    Surface(
                      color = if (overlayConfig.verticalOffsetPercent <= 0.006f) AccentPrimary else DarkSurfaceElevated,
                      shape = RoundedCornerShape(8.dp),
                      border = BorderStroke(1.dp, if (overlayConfig.verticalOffsetPercent <= 0.006f) AccentPrimaryLight else DarkBorder),
                      modifier = Modifier
                        .weight(1f)
                        .clickable { onVerticalOffsetChanged(0.004f) }
                    ) {
                      Column(modifier = Modifier.padding(6.dp)) {
                        Text("TikTok", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        Text("0.4% Bottom", fontSize = 8.5.sp, color = Color.White.copy(alpha = 0.8f))
                      }
                    }

                    Surface(
                      color = if (overlayConfig.verticalOffsetPercent in 0.007f..0.015f) AccentPrimary else DarkSurfaceElevated,
                      shape = RoundedCornerShape(8.dp),
                      border = BorderStroke(1.dp, if (overlayConfig.verticalOffsetPercent in 0.007f..0.015f) AccentPrimaryLight else DarkBorder),
                      modifier = Modifier
                        .weight(1f)
                        .clickable { onVerticalOffsetChanged(0.009f) }
                    ) {
                      Column(modifier = Modifier.padding(6.dp)) {
                        Text("YT Shorts", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        Text("0.9% Red", fontSize = 8.5.sp, color = Color.White.copy(alpha = 0.8f))
                      }
                    }

                    Surface(
                      color = if (overlayConfig.verticalOffsetPercent in 0.016f..0.032f) AccentPrimary else DarkSurfaceElevated,
                      shape = RoundedCornerShape(8.dp),
                      border = BorderStroke(1.dp, if (overlayConfig.verticalOffsetPercent in 0.016f..0.032f) AccentPrimaryLight else DarkBorder),
                      modifier = Modifier
                        .weight(1f)
                        .clickable { onVerticalOffsetChanged(0.024f) }
                    ) {
                      Column(modifier = Modifier.padding(6.dp)) {
                        Text("IG Reels", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        Text("2.4% Reels", fontSize = 8.5.sp, color = Color.White.copy(alpha = 0.8f))
                      }
                    }

                    Surface(
                      color = if (overlayConfig.verticalOffsetPercent > 0.035f) AccentPrimary else DarkSurfaceElevated,
                      shape = RoundedCornerShape(8.dp),
                      border = BorderStroke(1.dp, if (overlayConfig.verticalOffsetPercent > 0.035f) AccentPrimaryLight else DarkBorder),
                      modifier = Modifier
                        .weight(1f)
                        .clickable { onVerticalOffsetChanged(0.075f) }
                    ) {
                      Column(modifier = Modifier.padding(6.dp)) {
                        Text("Mid View", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        Text("7.5% High", fontSize = 8.5.sp, color = Color.White.copy(alpha = 0.8f))
                      }
                    }
                  }
                }
              }
            }

            // ─── TAB 2: LOCOMOTION GAIT & EXPORT FPS ───
            2 -> {
              val effectiveStepMs = WalkCycleMath.getEffectiveStepDurationMs(
                behavior = overlayConfig.behavior,
                durationMs = effectiveDurationMs,
                config = overlayConfig,
                canvasWidth = metadata.effectiveWidth.toFloat(),
                canvasHeight = metadata.effectiveHeight.toFloat()
              )
              val cadenceHz = WalkCycleMath.calculateCadenceStepsPerSecond(effectiveStepMs)
              val velocityPxSec = WalkCycleMath.calculateHorizontalVelocityPxPerSec(
                durationMs = effectiveDurationMs,
                config = overlayConfig,
                canvasWidth = metadata.effectiveWidth.toFloat()
              )

              Text(
                text = "LOCOMOTION GAIT STYLE",
                fontSize = 10.5.sp,
                fontWeight = FontWeight.Bold,
                color = TextMuted,
                letterSpacing = 0.6.sp
              )

              LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
              ) {
                items(AnimationBehavior.entries.toTypedArray(), key = { it.name }) { behavior ->
                  val isSelected = overlayConfig.behavior == behavior
                  FilterChip(
                    selected = isSelected,
                    onClick = { onBehaviorChanged(behavior) },
                    label = {
                      Text(
                        text = behavior.displayName,
                        fontSize = 11.5.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                      )
                    },
                    shape = RoundedCornerShape(10.dp),
                    leadingIcon = {
                      val icon = when (behavior) {
                        AnimationBehavior.PACE_SYNC -> Icons.Default.Sync
                        AnimationBehavior.WALK -> Icons.Default.DirectionsWalk
                        AnimationBehavior.STROLL -> Icons.Default.DirectionsWalk
                        AnimationBehavior.RUN -> Icons.Default.DirectionsRun
                        AnimationBehavior.SPRINT -> Icons.Default.FastForward
                        AnimationBehavior.HOP -> Icons.Default.Pets
                      }
                      Icon(icon, contentDescription = null, modifier = Modifier.size(15.dp))
                    },
                    colors = FilterChipDefaults.filterChipColors(
                      selectedContainerColor = AccentPrimary,
                      selectedLabelColor = Color.White,
                      selectedLeadingIconColor = Color.White,
                      containerColor = DarkSurfaceCard,
                      labelColor = TextSecondary
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                      borderColor = DarkBorder,
                      selectedBorderColor = AccentPrimary,
                      enabled = true,
                      selected = isSelected
                    )
                  )
                }
              }

              // Export Framerate & Smoothness Selector
              Text(
                text = "EXPORT FRAMERATE & 3D SMOOTHNESS",
                fontSize = 10.5.sp,
                fontWeight = FontWeight.Bold,
                color = TextMuted,
                letterSpacing = 0.6.sp
              )

              Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
              ) {
                ExportFpsOption.entries.forEach { option ->
                  val isSelected = overlayConfig.exportFpsOption == option
                  Surface(
                    color = if (isSelected) AccentPrimary else DarkSurfaceCard,
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, if (isSelected) AccentPrimaryLight else DarkBorder),
                    modifier = Modifier
                      .weight(1f)
                      .clickable { onExportFpsOptionChanged(option) }
                  ) {
                    Column(
                      horizontalAlignment = Alignment.CenterHorizontally,
                      modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp)
                    ) {
                      Text(
                        text = option.displayName,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) Color.White else TextPrimary,
                        maxLines = 1
                      )
                      Text(
                        text = if (option == ExportFpsOption.FPS_60) "Max 3D Smooth" else if (option == ExportFpsOption.AUTO) "Native" else if (option == ExportFpsOption.FPS_30) "Fast" else "Max Phone",
                        fontSize = 9.sp,
                        color = if (isSelected) Color.White.copy(alpha = 0.85f) else TextMuted,
                        maxLines = 1
                      )
                    }
                  }
                }
              }

              // Walk Direction & Cadence Telemetry Card
              Card(
                colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, DarkBorder),
                modifier = Modifier.fillMaxWidth()
              ) {
                Column(
                  modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                  verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                  Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                  ) {
                    Text(
                      text = "Walking Direction",
                      fontSize = 12.sp,
                      fontWeight = FontWeight.Medium,
                      color = TextSecondary
                    )

                    FilterChip(
                      selected = overlayConfig.reverseDirection,
                      onClick = onToggleReverseDirection,
                      shape = RoundedCornerShape(8.dp),
                      label = {
                        Text(
                          text = if (overlayConfig.reverseDirection) "Right ➡ Left" else "Left ➡ Right",
                          fontSize = 11.sp,
                          fontWeight = FontWeight.SemiBold
                        )
                      },
                      leadingIcon = {
                        Icon(Icons.Default.SwapHoriz, contentDescription = null, modifier = Modifier.size(14.dp))
                      },
                      colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = AccentPrimary,
                        selectedLabelColor = Color.White,
                        selectedLeadingIconColor = Color.White,
                        containerColor = DarkSurfaceElevated,
                        labelColor = TextSecondary
                      ),
                      border = FilterChipDefaults.filterChipBorder(
                        borderColor = DarkBorder,
                        selectedBorderColor = AccentPrimary,
                        enabled = true,
                        selected = overlayConfig.reverseDirection
                      )
                    )
                  }

                  Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceAround,
                    modifier = Modifier
                      .fillMaxWidth()
                      .padding(top = 4.dp)
                  ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                      Text("Cadence", fontSize = 9.5.sp, color = TextMuted)
                      Text(
                        text = String.format("%.1f st/s", cadenceHz),
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = AccentPrimaryLight
                      )
                    }

                    Box(
                      modifier = Modifier
                        .width(1.dp)
                        .height(24.dp)
                        .background(DarkBorder)
                    )

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                      Text("Step Cycle", fontSize = 9.5.sp, color = TextMuted)
                      Text(
                        text = "${effectiveStepMs}ms",
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                      )
                    }

                    Box(
                      modifier = Modifier
                        .width(1.dp)
                        .height(24.dp)
                        .background(DarkBorder)
                    )

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                      Text("Velocity", fontSize = 9.5.sp, color = TextMuted)
                      Text(
                        text = String.format("%.0f px/s", velocityPxSec),
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = AccentCyan
                      )
                    }
                  }
                }
              }

              // Range Sliders (Start X & End X)
              Card(
                colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, DarkBorder),
                modifier = Modifier.fillMaxWidth()
              ) {
                Column(
                  modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                  verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                  Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                  ) {
                    Text(
                      text = "Horizontal Movement Span",
                      fontSize = 12.sp,
                      fontWeight = FontWeight.Medium,
                      color = TextSecondary
                    )

                    Text(
                      text = "${(overlayConfig.startXPercent * 100).toInt()}% ➡ ${(overlayConfig.endXPercent * 100).toInt()}%",
                      fontSize = 11.sp,
                      fontFamily = FontFamily.Monospace,
                      color = AccentPrimaryLight
                    )
                  }

                  RangeSlider(
                    value = overlayConfig.startXPercent..overlayConfig.endXPercent,
                    onValueChange = { range ->
                      onHorizontalRangeChanged(range.start, range.endInclusive)
                    },
                    valueRange = 0.0f..1.0f,
                    colors = SliderDefaults.colors(
                      thumbColor = Color.White,
                      activeTrackColor = AccentPrimary,
                      inactiveTrackColor = DarkBorder
                    )
                  )
                }
              }
            }

            // ─── TAB 3: FRAMING & BLACK-BAR REMOVAL & SPECS ───
            3 -> {
              Text(
                text = "ASPECT RATIO & BLACK BAR CROPPING",
                fontSize = 10.5.sp,
                fontWeight = FontWeight.Bold,
                color = TextMuted,
                letterSpacing = 0.6.sp
              )

              Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                VideoFramingMode.entries.forEach { mode ->
                  val isSelected = overlayConfig.framingMode == mode
                  Surface(
                    color = if (isSelected) AccentPrimaryMuted else DarkSurfaceCard,
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, if (isSelected) AccentPrimary else DarkBorder),
                    modifier = Modifier
                      .fillMaxWidth()
                      .clickable { onFramingModeChanged(mode) }
                  ) {
                    Row(
                      verticalAlignment = Alignment.CenterVertically,
                      modifier = Modifier.padding(12.dp)
                    ) {
                      Icon(
                        imageVector = if (mode == VideoFramingMode.PHONE_TALL_19_5_9) Icons.Default.Smartphone else if (mode == VideoFramingMode.REELS_9_16) Icons.Default.Crop else Icons.Default.AspectRatio,
                        contentDescription = null,
                        tint = if (isSelected) AccentPrimaryLight else TextSecondary,
                        modifier = Modifier.size(20.dp)
                      )
                      Spacer(modifier = Modifier.width(10.dp))
                      Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                          val modeTitle = if (mode == VideoFramingMode.MATCH_DEVICE_SCREEN) {
                            "Match My Screen (${overlayConfig.deviceScreenRatioFormatted})"
                          } else {
                            mode.displayName
                          }
                          val modeSubtitle = if (mode == VideoFramingMode.MATCH_DEVICE_SCREEN) {
                            "${overlayConfig.deviceScreenWidth}×${overlayConfig.deviceScreenHeight} Auto"
                          } else {
                            mode.subtitle
                          }

                          Text(
                            text = modeTitle,
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                          )
                          Spacer(modifier = Modifier.width(6.dp))
                          Surface(
                            color = AccentPrimaryMuted,
                            shape = RoundedCornerShape(4.dp)
                          ) {
                            Text(
                              text = modeSubtitle,
                              fontSize = 9.sp,
                              color = AccentPrimaryLight,
                              fontWeight = FontWeight.SemiBold,
                              modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                            )
                          }
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                          text = mode.description,
                          fontSize = 10.5.sp,
                          color = TextSecondary,
                          lineHeight = 14.sp
                        )
                      }
                      if (isSelected) {
                        Icon(Icons.Default.Check, contentDescription = null, tint = AccentPrimaryLight, modifier = Modifier.size(18.dp))
                      }
                    }
                  }
                }
              }

              // Explain upload cropping & screen matching
              Card(
                colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, DarkBorder),
                modifier = Modifier.fillMaxWidth()
              ) {
                Column(
                  modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                  verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                  Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Info, contentDescription = null, tint = AccentGold, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                      text = "Why Upload Alignment Matters",
                      fontSize = 12.sp,
                      fontWeight = FontWeight.Bold,
                      color = TextPrimary
                    )
                  }
                  Text(
                    text = "Walkbar positions your character precisely within the exported video. Once uploaded, TikTok, Instagram Reels, and Facebook crop vertical videos to fit the viewing phone's exact screen aspect ratio. For best alignment, export using 'Match My Screen' framing and avoid placing characters at the extreme bottom edge.",
                    fontSize = 10.5.sp,
                    color = TextSecondary,
                    lineHeight = 15.sp
                  )
                }
              }

              // Video Tech Specs Card
              Card(
                colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, DarkBorder),
                modifier = Modifier.fillMaxWidth()
              ) {
                Column(
                  modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                  verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                  Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                  ) {
                    Column {
                      Text("Input Resolution", fontSize = 9.5.sp, color = TextMuted)
                      Text(
                        text = "${metadata.effectiveWidth} × ${metadata.effectiveHeight}",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                      )
                    }

                    Column {
                      Text("Framerate", fontSize = 9.5.sp, color = TextMuted)
                      Text(
                        text = metadata.formattedFps,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = AccentPrimaryLight
                      )
                    }

                    Column {
                      Text("Bitrate", fontSize = 9.5.sp, color = TextMuted)
                      Text(
                        text = metadata.formattedBitrate,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = AccentCyan
                      )
                    }

                    Column {
                      Text("Audio", fontSize = 9.5.sp, color = TextMuted)
                      Text(
                        text = if (metadata.hasAudio) "Passthrough" else "None",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (metadata.hasAudio) AccentGreen else TextMuted
                      )
                    }
                  }
                }
              }

              // Reset Button
              OutlinedButton(
                onClick = {
                  onCustomScaleChanged(selectedCharacter.defaultScale)
                  onVerticalOffsetChanged(0.004f)
                  onHorizontalRangeChanged(0.01f, 0.99f)
                  onPlatformSelected(SocialPlatform.TIKTOK)
                  onFramingModeChanged(VideoFramingMode.ORIGINAL)
                },
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, DarkBorder),
                modifier = Modifier.fillMaxWidth()
              ) {
                Icon(Icons.Default.RestartAlt, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Reset All Sliders to TikTok Ultra-Bottom Defaults", fontSize = 12.sp, color = TextSecondary)
              }
            }
          }
        }
      }
    }
  }

  if (showAboutSheet) {
    AboutAppSheet(onDismissRequest = { showAboutSheet = false })
  }
}
