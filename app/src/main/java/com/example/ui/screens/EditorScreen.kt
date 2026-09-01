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
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.Height
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.VerticalSplit
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
import androidx.compose.material3.MaterialTheme
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
import com.example.model.ObjectCategory
import com.example.model.VideoMetadata
import com.example.player.WalkbarPlayerManager
import com.example.ui.components.CharacterAvatarCard
import com.example.ui.components.InstagramOverlayGuide
import com.example.ui.components.SleekCharacterPicker
import com.example.ui.theme.AccentAmber
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.AccentGreen
import com.example.ui.theme.AccentIndigo
import com.example.ui.theme.AccentIndigoActive
import com.example.ui.theme.AccentIndigoLight
import com.example.ui.theme.AccentIndigoMuted
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkBorderSubtle
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceCard
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.DarkSurfaceHover
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
  onSizePresetChanged: (CharacterSizePreset) -> Unit,
  onCustomScaleChanged: (Float) -> Unit,
  onVerticalOffsetChanged: (Float) -> Unit,
  onHorizontalRangeChanged: (Float, Float) -> Unit,
  onToggleReverseDirection: () -> Unit,
  onToggleInstagramGuide: () -> Unit,
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
  var isSplitScreenMode by remember { mutableStateOf(false) }
  val tabs = listOf("Character", "Movement & Speed", "Position & Size", "Specs & Save")

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
            text = if (isSplitScreenMode) "Split-Screen Tuning" else "Walkbar Editor",
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            color = TextPrimary,
            letterSpacing = (-0.2).sp
          )
          Text(
            text = metadata.fileName,
            fontSize = 11.sp,
            color = TextSecondary,
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
        // Split-Screen Mode Toggle
        IconButton(
          onClick = { isSplitScreenMode = !isSplitScreenMode },
          modifier = Modifier.testTag("toggle_split_screen")
        ) {
          Icon(
            imageVector = if (isSplitScreenMode) Icons.Default.Fullscreen else Icons.Default.VerticalSplit,
            contentDescription = if (isSplitScreenMode) "Single Screen View" else "Split Screen View",
            tint = if (isSplitScreenMode) AccentIndigoLight else TextSecondary
          )
        }

        // Toggle Reference Guide Simulation
        IconButton(
          onClick = onToggleInstagramGuide,
          modifier = Modifier.testTag("toggle_instagram_guide")
        ) {
          Icon(
            imageVector = if (overlayConfig.showInstagramPreviewGuide) Icons.Default.Visibility else Icons.Default.VisibilityOff,
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
            text = "SAVE",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.8.sp,
            color = Color.White
          )
        }
      },
      colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkBackground)
    )

    // ─── 2. VIDEO VIEWPORT (SINGLE VIEW OR SPLIT-SCREEN VIEW) ───
    Box(
      contentAlignment = Alignment.Center,
      modifier = Modifier
        .fillMaxWidth()
        .weight(1.4f)
        .background(Color(0xFF000000))
        .padding(horizontal = if (isSplitScreenMode) 10.dp else 16.dp, vertical = 4.dp)
    ) {
      if (isSplitScreenMode) {
        // ─── DUAL SPLIT-SCREEN VIEW (ORIGINAL ON TOP, OVERLAY ON BOTTOM) ───
        Column(
          verticalArrangement = Arrangement.spacedBy(8.dp),
          modifier = Modifier
            .fillMaxSize()
            .testTag("split_screen_container")
        ) {
          // TOP PANE: ORIGINAL VIDEO (CLEAN)
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .weight(1f)
              .clip(RoundedCornerShape(16.dp))
              .border(1.dp, DarkBorderSubtle, RoundedCornerShape(16.dp))
              .background(Color(0xFF101014))
              .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
              ) {
                playerManager.togglePlayPause()
              }
          ) {
            AndroidView(
              factory = { ctx ->
                PlayerView(ctx).apply {
                  player = playerManager.getPlayer()
                  useController = false
                  resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
                  layoutParams = FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                  )
                }
              },
              update = { playerView ->
                playerView.player = playerManager.getPlayer()
              },
              modifier = Modifier.fillMaxSize()
            )

            // Top Badge
            Surface(
              color = Color(0x99000000),
              shape = RoundedCornerShape(8.dp),
              border = BorderStroke(0.5.dp, Color(0x33FFFFFF)),
              modifier = Modifier
                .align(Alignment.TopStart)
                .padding(8.dp)
            ) {
              Text(
                text = "1. ORIGINAL VIDEO (CLEAN)",
                fontSize = 9.5.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp,
                color = Color.White,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
              )
            }
          }

          // BOTTOM PANE: OVERLAY PREVIEW (WITH WALKING SPRITE)
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .weight(1f)
              .clip(RoundedCornerShape(16.dp))
              .border(1.5.dp, AccentIndigo.copy(alpha = 0.6f), RoundedCornerShape(16.dp))
              .background(Color(0xFF101014))
              .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
              ) {
                playerManager.togglePlayPause()
              }
          ) {
            AndroidView(
              factory = { ctx ->
                PlayerView(ctx).apply {
                  player = playerManager.getPlayer()
                  useController = false
                  resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
                  layoutParams = FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                  )
                }
              },
              update = { playerView ->
                playerView.player = playerManager.getPlayer()
              },
              modifier = Modifier.fillMaxSize()
            )

            // Character Overlay
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

            // Optional Instagram Guide Overlay
            if (overlayConfig.showInstagramPreviewGuide) {
              InstagramOverlayGuide(
                progress = currentProgress,
                modifier = Modifier.fillMaxSize()
              )
            }

            // Bottom Overlay Badge
            Surface(
              color = Color(0xCC1E1B4B),
              shape = RoundedCornerShape(8.dp),
              border = BorderStroke(0.5.dp, AccentIndigoLight),
              modifier = Modifier
                .align(Alignment.TopStart)
                .padding(8.dp)
            ) {
              Text(
                text = "2. WALKING OVERLAY PREVIEW",
                fontSize = 9.5.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp,
                color = AccentIndigoLight,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
              )
            }

            // Quick live tuning parameters glass pill
            Surface(
              color = Color(0x88000000),
              shape = RoundedCornerShape(8.dp),
              modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(8.dp)
            ) {
              Text(
                text = "Size: ${(overlayConfig.customScalePercent * 100).toInt()}% • Y: ${(overlayConfig.verticalOffsetPercent * 100).toInt()}%",
                fontSize = 9.sp,
                fontFamily = FontFamily.Monospace,
                color = TextSecondary,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
              )
            }
          }
        }
      } else {
        // ─── STANDARD SINGLE VIEWPORT ───
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
              .shadow(24.dp, RoundedCornerShape(24.dp))
              .clip(RoundedCornerShape(24.dp))
              .border(1.dp, DarkBorderSubtle, RoundedCornerShape(24.dp))
              .background(Color(0xFF141416))
              .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
              ) {
                playerManager.togglePlayPause()
              }
          ) {
            // Media3 ExoPlayer View
            AndroidView(
              factory = { ctx ->
                PlayerView(ctx).apply {
                  player = playerManager.getPlayer()
                  useController = false
                  resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
                  layoutParams = FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                  )
                }
              },
              update = { playerView ->
                playerView.player = playerManager.getPlayer()
              },
              modifier = Modifier.fillMaxSize()
            )

            // Character Overlay Canvas
            Canvas(
              modifier = Modifier.fillMaxSize()
            ) {
              CharacterRenderer.renderInCompose(
                drawScope = this,
                character = selectedCharacter,
                config = overlayConfig,
                currentTimeMs = currentPositionMs,
                durationMs = effectiveDurationMs,
                isPlaying = isPlaying
              )
            }

            // Optional Instagram Reel Guide simulation overlay
            if (overlayConfig.showInstagramPreviewGuide) {
              InstagramOverlayGuide(
                progress = currentProgress,
                modifier = Modifier.fillMaxSize()
              )
            }

            // Video Specs Glass Badge
            Surface(
              color = Color(0x73000000),
              shape = RoundedCornerShape(8.dp),
              border = BorderStroke(0.5.dp, Color(0x33FFFFFF)),
              modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(10.dp)
            ) {
              Text(
                text = "${metadata.effectiveWidth} × ${metadata.effectiveHeight} • ${metadata.formattedFps}",
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Medium,
                color = TextSecondary,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
              )
            }

            // Play / Pause center feedback indicator
            if (!isPlaying) {
              Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                  .align(Alignment.Center)
                  .size(56.dp)
                  .clip(CircleShape)
                  .background(Color(0x99000000))
                  .border(1.dp, Color(0x33FFFFFF), CircleShape)
              ) {
                Icon(
                  imageVector = Icons.Default.PlayArrow,
                  contentDescription = "Play",
                  tint = Color.White,
                  modifier = Modifier.size(32.dp)
                )
              }
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
          .padding(horizontal = 16.dp, vertical = 2.dp)
      ) {
        IconButton(
          onClick = { playerManager.togglePlayPause() },
          modifier = Modifier
            .size(36.dp)
            .testTag("play_pause_button")
        ) {
          Icon(
            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
            contentDescription = if (isPlaying) "Pause" else "Play",
            tint = AccentIndigo
          )
        }

        Spacer(modifier = Modifier.width(4.dp))

        // Time Indicator
        val currentSec = (currentPositionMs / 1000).toInt()
        val totalSec = (effectiveDurationMs / 1000).toInt()
        val timeLabel = String.format("%d:%02d / %d:%02d", currentSec / 60, currentSec % 60, totalSec / 60, totalSec % 60)

        Text(
          text = timeLabel,
          fontSize = 11.sp,
          fontWeight = FontWeight.Medium,
          fontFamily = FontFamily.Monospace,
          color = TextSecondary,
          modifier = Modifier.width(82.dp)
        )

        // Seek Slider
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

    // ─── 4. SLEEK BOTTOM CONTROLS PANEL ───
    Surface(
      color = DarkSurfaceElevated,
      shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
      border = BorderStroke(1.dp, DarkBorder),
      modifier = Modifier.fillMaxWidth()
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(top = 8.dp, bottom = 12.dp)
      ) {
        // Tab Headers
        ScrollableTabRow(
          selectedTabIndex = selectedTab,
          containerColor = DarkSurfaceElevated,
          contentColor = AccentIndigo,
          edgePadding = 16.dp,
          indicator = { tabPositions ->
            TabRowDefaults.SecondaryIndicator(
              Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
              color = AccentIndigo,
              height = 2.dp
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
                  fontWeight = if (selectedTab == index) FontWeight.SemiBold else FontWeight.Normal,
                  color = if (selectedTab == index) TextPrimary else TextMuted
                )
              }
            )
          }
        }

        // Tab Contents
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
        ) {
          when (selectedTab) {
            0 -> {
              // TAB 1: Sleek Character Picker with Category Filtering & Instant Walk Preview
              SleekCharacterPicker(
                selectedCharacterId = overlayConfig.characterId,
                behavior = overlayConfig.behavior,
                onCharacterSelected = onCharacterSelected
              )
            }

            1 -> {
              // TAB 2: Movement & Dynamic Stride Duration Sync Logic Layer
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

              Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
              ) {
                // Behavior selector chips
                Text(
                  text = "ANIMATION BEHAVIOR & GAIT",
                  fontSize = 10.sp,
                  fontWeight = FontWeight.Bold,
                  color = TextMuted,
                  letterSpacing = 0.5.sp
                )

                LazyRow(
                  horizontalArrangement = Arrangement.spacedBy(6.dp),
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

                // Dynamic Logic Layer Telemetry Card
                Card(
                  colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard),
                  shape = RoundedCornerShape(12.dp),
                  border = BorderStroke(1.dp, DarkBorder),
                  modifier = Modifier.fillMaxWidth()
                ) {
                  Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceAround,
                    modifier = Modifier
                      .fillMaxWidth()
                      .padding(horizontal = 12.dp, vertical = 8.dp)
                  ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                      Text("Stride Cadence", fontSize = 9.sp, color = TextMuted)
                      Text(
                        text = String.format("%.1f steps/s", cadenceHz),
                        fontSize = 12.sp,
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
                      Text("Step Cycle", fontSize = 9.sp, color = TextMuted)
                      Text(
                        text = "${effectiveStepMs}ms",
                        fontSize = 12.sp,
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
                      Text("Bar Velocity", fontSize = 9.sp, color = TextMuted)
                      Text(
                        text = String.format("%.0f px/s", velocityPxSec),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = AccentCyan
                      )
                    }
                  }
                }

                // Direction Toggle
                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.SpaceBetween,
                  modifier = Modifier.fillMaxWidth()
                ) {
                  Text(
                    text = "Walk Direction",
                    fontSize = 12.sp,
                    color = TextSecondary
                  )

                  FilterChip(
                    selected = overlayConfig.reverseDirection,
                    onClick = onToggleReverseDirection,
                    shape = RoundedCornerShape(10.dp),
                    label = {
                      Text(
                        text = if (overlayConfig.reverseDirection) "Right to Left ⬅" else "Left to Right ➡",
                        fontSize = 11.5.sp
                      )
                    },
                    leadingIcon = {
                      Icon(Icons.Default.SwapHoriz, contentDescription = null, modifier = Modifier.size(15.dp))
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
                      selected = overlayConfig.reverseDirection
                    )
                  )
                }
              }
            }

            2 -> {
              // TAB 3: Position & Size with Live Real-Time Tuning
              val scroll = rememberScrollState()
              Column(
                modifier = Modifier
                  .fillMaxWidth()
                  .verticalScroll(scroll)
              ) {
                // Size Presets & Scale Slider
                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.SpaceBetween,
                  modifier = Modifier.fillMaxWidth()
                ) {
                  Text(
                    text = "SIZE",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextMuted,
                    modifier = Modifier.width(52.dp)
                  )
                  Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    CharacterSizePreset.entries.forEach { preset ->
                      FilterChip(
                        selected = overlayConfig.sizePreset == preset,
                        onClick = { onSizePresetChanged(preset) },
                        shape = RoundedCornerShape(10.dp),
                        label = { Text(preset.label, fontSize = 11.sp) },
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
                          selected = overlayConfig.sizePreset == preset
                        )
                      )
                    }
                  }
                }

                // Fine Scale Slider
                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  modifier = Modifier.fillMaxWidth()
                ) {
                  Text(
                    text = "SCALE",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextMuted,
                    modifier = Modifier.width(52.dp)
                  )
                  Slider(
                    value = overlayConfig.customScalePercent,
                    onValueChange = onCustomScaleChanged,
                    valueRange = 0.02f..0.15f,
                    colors = SliderDefaults.colors(
                      thumbColor = Color.White,
                      activeTrackColor = AccentIndigo,
                      inactiveTrackColor = DarkBorder
                    ),
                    modifier = Modifier
                      .weight(1f)
                      .testTag("character_size_slider")
                  )
                  Text(
                    text = "${(overlayConfig.customScalePercent * 100).toInt()}%",
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    color = TextSecondary,
                    modifier = Modifier.width(36.dp)
                  )
                }

                // Vertical Height Slider
                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  modifier = Modifier.fillMaxWidth()
                ) {
                  Text(
                    text = "HEIGHT",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextMuted,
                    modifier = Modifier.width(52.dp)
                  )
                  Slider(
                    value = overlayConfig.verticalOffsetPercent,
                    onValueChange = onVerticalOffsetChanged,
                    valueRange = 0.00f..0.18f,
                    colors = SliderDefaults.colors(
                      thumbColor = Color.White,
                      activeTrackColor = AccentIndigo,
                      inactiveTrackColor = DarkBorder
                    ),
                    modifier = Modifier
                      .weight(1f)
                      .testTag("vertical_offset_slider")
                  )
                  Text(
                    text = "${(overlayConfig.verticalOffsetPercent * 100).toInt()}%",
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    color = TextSecondary,
                    modifier = Modifier.width(36.dp)
                  )
                }

                // Quick Snap Buttons
                Row(
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically,
                  modifier = Modifier.fillMaxWidth()
                ) {
                  Text(
                    text = "⚡ Snap Presets:",
                    fontSize = 11.sp,
                    color = TextMuted
                  )
                  Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                      text = "Standard Scrubber (3.8%)",
                      fontSize = 11.sp,
                      fontWeight = FontWeight.SemiBold,
                      color = AccentIndigoLight,
                      modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .clickable { onVerticalOffsetChanged(0.038f) }
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                    Text(
                      text = "Bottom Timeline (1.5%)",
                      fontSize = 11.sp,
                      fontWeight = FontWeight.SemiBold,
                      color = AccentCyan,
                      modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .clickable { onVerticalOffsetChanged(0.015f) }
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                  }
                }
              }
            }

            3 -> {
              // TAB 4: Specs & Local MediaCodec Hardware Save
              Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
              ) {
                Row(
                  horizontalArrangement = Arrangement.SpaceBetween,
                  modifier = Modifier.fillMaxWidth()
                ) {
                  Column(modifier = Modifier.weight(1f)) {
                    Text("Original Video", fontSize = 10.sp, color = TextMuted)
                    Text(
                      text = "${metadata.effectiveWidth} × ${metadata.effectiveHeight}",
                      fontSize = 13.sp,
                      fontWeight = FontWeight.Bold,
                      color = TextPrimary
                    )
                    Text(
                      text = "${metadata.formattedFps} • ${metadata.formattedBitrate}",
                      fontSize = 11.sp,
                      color = TextSecondary
                    )
                  }

                  Box(
                    modifier = Modifier
                      .width(1.dp)
                      .height(36.dp)
                      .background(DarkBorder)
                  )

                  Spacer(modifier = Modifier.width(12.dp))

                  Column(modifier = Modifier.weight(1f)) {
                    Text("MediaCodec Export Engine", fontSize = 10.sp, color = AccentIndigoLight)
                    Text(
                      text = "H.264 / AVC • MP4",
                      fontSize = 13.sp,
                      fontWeight = FontWeight.Bold,
                      color = TextPrimary
                    )
                    Text(
                      text = "Bit-for-bit Audio Passthrough",
                      fontSize = 11.sp,
                      color = TextSecondary
                    )
                  }
                }

                Text(
                  text = "Burned character frames are composited directly via hardware surface encoding and saved to your device's Gallery / Movies folder.",
                  fontSize = 11.sp,
                  color = TextMuted,
                  lineHeight = 15.sp
                )
              }
            }
          }
        }

        // ─── 5. BOTTOM PRIMARY SAVE & PREVIEW BUTTONS ───
        Row(
          horizontalArrangement = Arrangement.spacedBy(10.dp),
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
        ) {
          // Play / Pause Toggle
          Button(
            onClick = { playerManager.togglePlayPause() },
            colors = ButtonDefaults.buttonColors(
              containerColor = DarkSurfaceCard,
              contentColor = TextPrimary
            ),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
              .weight(1f)
              .height(48.dp)
          ) {
            Icon(
              imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
              contentDescription = null,
              tint = Color.White,
              modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = if (isPlaying) "Pause" else "Play Preview",
              fontSize = 13.5.sp,
              fontWeight = FontWeight.SemiBold
            )
          }

          // Split Screen Quick Toggle
          IconButton(
            onClick = { isSplitScreenMode = !isSplitScreenMode },
            modifier = Modifier
              .size(48.dp)
              .clip(RoundedCornerShape(16.dp))
              .background(if (isSplitScreenMode) AccentIndigoMuted else DarkSurfaceCard)
              .border(1.dp, if (isSplitScreenMode) AccentIndigo else DarkBorder, RoundedCornerShape(16.dp))
          ) {
            Icon(
              imageVector = if (isSplitScreenMode) Icons.Default.Fullscreen else Icons.Default.VerticalSplit,
              contentDescription = "Toggle Split Screen",
              tint = if (isSplitScreenMode) AccentIndigoLight else TextSecondary,
              modifier = Modifier.size(20.dp)
            )
          }

          // Main 'Save Video' Button
          Button(
            onClick = onExportClicked,
            colors = ButtonDefaults.buttonColors(
              containerColor = AccentIndigo,
              contentColor = Color.White
            ),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
              .weight(1.3f)
              .height(48.dp)
              .testTag("save_video_action_button")
          ) {
            Icon(
              imageVector = Icons.Default.Save,
              contentDescription = null,
              tint = Color.White,
              modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = "Save Video",
              fontSize = 14.sp,
              fontWeight = FontWeight.Bold
            )
          }
        }
      }
    }
  }
}
