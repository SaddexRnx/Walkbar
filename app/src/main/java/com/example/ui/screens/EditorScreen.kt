@file:OptIn(androidx.media3.common.util.UnstableApi::class, androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.example.ui.screens

import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.material.icons.filled.VerticalAlignBottom
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.text.style.TextAlign
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
import com.example.ui.components.SleekCharacterPicker
import com.example.ui.theme.AccentAmber
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.AccentGreen
import com.example.ui.theme.AccentIndigo
import com.example.ui.theme.AccentIndigoLight
import com.example.ui.theme.AccentIndigoMuted
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
            color = AccentIndigoLight,
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
            tint = if (isCleanPreviewMode) AccentAmber else TextSecondary
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
            tint = if (overlayConfig.showInstagramPreviewGuide) AccentIndigoLight else TextMuted
          )
        }

        Spacer(modifier = Modifier.width(4.dp))

        // Save & Export Button
        Button(
          onClick = onExportClicked,
          shape = CircleShape,
          colors = ButtonDefaults.buttonColors(
            containerColor = AccentIndigo,
            contentColor = Color.White
          ),
          contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
          modifier = Modifier
            .padding(end = 12.dp)
            .testTag("export_video_button")
        ) {
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
      },
      colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkBackground)
    )

    // ─── 2. VIDEO VIEWPORT WITH DIRECT INTERACTIVE TOUCH DRAGGING ───
    Box(
      contentAlignment = Alignment.Center,
      modifier = Modifier
        .fillMaxWidth()
        .weight(1.05f)
        .background(Color(0xFF000000))
        .padding(horizontal = 10.dp, vertical = 2.dp)
    ) {
      BoxWithConstraints(
        contentAlignment = Alignment.Center,
        modifier = Modifier.fillMaxSize()
      ) {
        val videoAspect = metadata.aspectRatio.coerceIn(0.4f, 2.4f)
        val containerAspect = maxWidth / maxHeight

        val (contentWidth, contentHeight) = if (containerAspect > videoAspect) {
          Pair(maxHeight * videoAspect, maxHeight)
        } else {
          Pair(maxWidth, maxWidth / videoAspect)
        }

        Box(
          modifier = Modifier
            .size(contentWidth, contentHeight)
            .shadow(20.dp, RoundedCornerShape(20.dp))
            .clip(RoundedCornerShape(20.dp))
            .border(1.dp, DarkBorderSubtle, RoundedCornerShape(20.dp))
            .background(Color(0xFF101014))
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
          // Media3 ExoPlayer View
          AndroidView(
            factory = { ctx ->
              PlayerView(ctx).apply {
                player = playerManager.getPlayer()
                useController = false
                setShutterBackgroundColor(android.graphics.Color.TRANSPARENT)
                resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
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
            },
            modifier = Modifier.fillMaxSize()
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

          // Interactive Dragging Height Indicator Pill
          if (isDraggingPosition) {
            Surface(
              color = Color(0xEE1E293B),
              shape = RoundedCornerShape(20.dp),
              border = BorderStroke(1.5.dp, AccentIndigo),
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

          // Top Right Platform & Specs Badge
          Surface(
            color = Color(0x99000000),
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
              fontWeight = FontWeight.Medium,
              color = AccentCyan,
              modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
            )
          }

          // Clean mode badge
          if (isCleanPreviewMode) {
            Surface(
              color = AccentAmber.copy(alpha = 0.85f),
              shape = RoundedCornerShape(8.dp),
              modifier = Modifier
                .align(Alignment.TopStart)
                .padding(8.dp)
            ) {
              Text(
                text = "CLEAN ORIGINAL PREVIEW",
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
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
            tint = AccentIndigo
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
            activeTrackColor = AccentIndigo,
            inactiveTrackColor = DarkBorder
          ),
          modifier = Modifier
            .weight(1f)
            .testTag("timeline_slider")
        )
      }
    }

    // ─── 4. BEAUTIFUL SCROLLABLE SETTINGS MENU ───
    Surface(
      color = DarkSurfaceElevated,
      shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
      border = BorderStroke(1.dp, DarkBorder),
      modifier = Modifier
        .fillMaxWidth()
        .weight(0.95f)
    ) {
      Column(
        modifier = Modifier.fillMaxSize()
      ) {
        // Tab Headers Row
        ScrollableTabRow(
          selectedTabIndex = selectedTab,
          containerColor = DarkSurfaceElevated,
          contentColor = AccentIndigo,
          edgePadding = 12.dp,
          indicator = { tabPositions ->
            TabRowDefaults.SecondaryIndicator(
              Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
              color = AccentIndigo,
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
                      .background(AccentIndigoMuted)
                      .border(1.dp, AccentIndigoLight, RoundedCornerShape(12.dp))
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
                        color = Color(0x336366F1),
                        shape = RoundedCornerShape(6.dp)
                      ) {
                        Text(
                          text = selectedCharacter.category.displayName,
                          fontSize = 9.5.sp,
                          color = AccentIndigoLight,
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
                      selectedContainerColor = AccentIndigo,
                      selectedLabelColor = Color.White,
                      containerColor = DarkSurfaceCard,
                      labelColor = TextSecondary
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                      borderColor = DarkBorder,
                      selectedBorderColor = AccentIndigo,
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

                  // Precision Stepper Row (+0.1% / -0.1%)
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
                      activeTrackColor = AccentIndigo,
                      inactiveTrackColor = DarkBorder
                    ),
                    modifier = Modifier.testTag("vertical_offset_slider")
                  )

                  Text(
                    text = "💡 Tip: You can also touch or drag anywhere on the video preview above to position the character directly on your progress bar.",
                    fontSize = 10.sp,
                    color = TextSecondary,
                    lineHeight = 14.sp
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
                      color = if (overlayConfig.verticalOffsetPercent <= 0.006f) AccentIndigo else DarkSurfaceElevated,
                      shape = RoundedCornerShape(8.dp),
                      border = BorderStroke(1.dp, if (overlayConfig.verticalOffsetPercent <= 0.006f) AccentIndigoLight else DarkBorder),
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
                      color = if (overlayConfig.verticalOffsetPercent in 0.007f..0.015f) AccentIndigo else DarkSurfaceElevated,
                      shape = RoundedCornerShape(8.dp),
                      border = BorderStroke(1.dp, if (overlayConfig.verticalOffsetPercent in 0.007f..0.015f) AccentIndigoLight else DarkBorder),
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
                      color = if (overlayConfig.verticalOffsetPercent in 0.016f..0.032f) AccentIndigo else DarkSurfaceElevated,
                      shape = RoundedCornerShape(8.dp),
                      border = BorderStroke(1.dp, if (overlayConfig.verticalOffsetPercent in 0.016f..0.032f) AccentIndigoLight else DarkBorder),
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
                      color = if (overlayConfig.verticalOffsetPercent > 0.035f) AccentIndigo else DarkSurfaceElevated,
                      shape = RoundedCornerShape(8.dp),
                      border = BorderStroke(1.dp, if (overlayConfig.verticalOffsetPercent > 0.035f) AccentIndigoLight else DarkBorder),
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
                      selectedContainerColor = AccentIndigo,
                      selectedLabelColor = Color.White,
                      selectedLeadingIconColor = Color.White,
                      containerColor = DarkSurfaceCard,
                      labelColor = TextSecondary
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                      borderColor = DarkBorder,
                      selectedBorderColor = AccentIndigo,
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
                    color = if (isSelected) AccentIndigo else DarkSurfaceCard,
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, if (isSelected) AccentIndigoLight else DarkBorder),
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
                        selectedContainerColor = AccentIndigo,
                        selectedLabelColor = Color.White,
                        selectedLeadingIconColor = Color.White,
                        containerColor = DarkSurfaceElevated,
                        labelColor = TextSecondary
                      ),
                      border = FilterChipDefaults.filterChipBorder(
                        borderColor = DarkBorder,
                        selectedBorderColor = AccentIndigo,
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
                        color = AccentIndigoLight
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
                      color = AccentIndigoLight
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
                      activeTrackColor = AccentIndigo,
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
                    color = if (isSelected) AccentIndigoMuted else DarkSurfaceCard,
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, if (isSelected) AccentIndigo else DarkBorder),
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
                        tint = if (isSelected) AccentIndigoLight else TextSecondary,
                        modifier = Modifier.size(20.dp)
                      )
                      Spacer(modifier = Modifier.width(10.dp))
                      Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                          Text(
                            text = mode.displayName,
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                          )
                          Spacer(modifier = Modifier.width(6.dp))
                          Surface(
                            color = Color(0x336366F1),
                            shape = RoundedCornerShape(4.dp)
                          ) {
                            Text(
                              text = mode.subtitle,
                              fontSize = 9.sp,
                              color = AccentIndigoLight,
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
                        Icon(Icons.Default.Check, contentDescription = null, tint = AccentIndigoLight, modifier = Modifier.size(18.dp))
                      }
                    }
                  }
                }
              }

              // Explain why black bars happen on TikTok
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
                    Icon(Icons.Default.Info, contentDescription = null, tint = AccentAmber, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                      text = "Why Do Black Bars Appear on TikTok?",
                      fontSize = 12.sp,
                      fontWeight = FontWeight.Bold,
                      color = TextPrimary
                    )
                  }
                  Text(
                    text = "When downloading reels from Instagram or posting to TikTok on modern tall smartphones (19.5:9 / 6.19\" screens), TikTok automatically adds black bars at top and bottom if the video isn't full frame. This pushes TikTok's scrubber bar to the very bottom of the screen. Selecting '9:16 Fullscreen' or '6.19\" Tall Screen' above crops black borders and fills your screen completely!",
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
                        color = AccentIndigoLight
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
