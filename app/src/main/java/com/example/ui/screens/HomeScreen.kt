package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.SyncAlt
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.characters.CharacterRegistry
import com.example.characters.CharacterRenderer
import com.example.characters.WalkCycleMath
import com.example.model.AnimationBehavior
import com.example.model.CharacterOverlayConfig
import com.example.model.ObjectCategory
import com.example.ui.components.CharacterAvatarCard
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
fun HomeScreen(
  isLoading: Boolean,
  overlayConfig: CharacterOverlayConfig = CharacterOverlayConfig(),
  onCharacterSelected: (String) -> Unit = {},
  onBehaviorChanged: (AnimationBehavior) -> Unit = {},
  onVideoSelected: (Uri) -> Unit,
  onSampleVideoRequested: () -> Unit,
  modifier: Modifier = Modifier
) {
  val photoPickerLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.PickVisualMedia()
  ) { uri: Uri? ->
    if (uri != null) {
      onVideoSelected(uri)
    }
  }

  val filePickerLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.GetContent()
  ) { uri: Uri? ->
    if (uri != null) {
      onVideoSelected(uri)
    }
  }

  val scrollState = rememberScrollState()
  var selectedCategory by remember { mutableStateOf(ObjectCategory.ALL) }
  val activeCharacter = remember(overlayConfig.characterId) {
    CharacterRegistry.getById(overlayConfig.characterId)
  }

  val filteredCharacters = remember(selectedCategory) {
    CharacterRegistry.getByCategory(selectedCategory)
  }

  // Continuous animation transition for live progress bar simulation
  val infiniteTransition = rememberInfiniteTransition(label = "homeProgress")
  val simDurationMs = 5000 // 5 seconds full cycle
  val simTimeMs by infiniteTransition.animateFloat(
    initialValue = 0f,
    targetValue = simDurationMs.toFloat(),
    animationSpec = infiniteRepeatable(
      animation = tween(simDurationMs, easing = LinearEasing),
      repeatMode = RepeatMode.Restart
    ),
    label = "simTime"
  )

  val simProgress = (simTimeMs / simDurationMs.toFloat()).coerceIn(0f, 1f)

  Box(
    modifier = modifier
      .fillMaxSize()
      .background(DarkBackground)
  ) {
    Column(
      horizontalAlignment = Alignment.CenterHorizontally,
      modifier = Modifier
        .fillMaxSize()
        .verticalScroll(scrollState)
        .padding(horizontal = 20.dp, vertical = 20.dp)
        .widthIn(max = 600.dp)
    ) {
      Spacer(modifier = Modifier.height(10.dp))

      // App Brand Pill
      Surface(
        color = DarkSurfaceElevated,
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, DarkBorder)
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
        ) {
          Box(
            modifier = Modifier
              .size(8.dp)
              .clip(CircleShape)
              .background(AccentIndigo)
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = "Instagram Reels Progress Companion",
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = TextSecondary
          )
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

      // App Title & Tagline
      Text(
        text = "Walkbar",
        fontSize = 38.sp,
        fontWeight = FontWeight.Bold,
        color = TextPrimary,
        letterSpacing = (-0.8).sp
      )

      Spacer(modifier = Modifier.height(6.dp))

      Text(
        text = "Composite tiny animated characters that walk across your video progress bar in exact sync with playback duration.",
        fontSize = 14.sp,
        color = TextSecondary,
        textAlign = TextAlign.Center,
        lineHeight = 20.sp,
        modifier = Modifier.padding(horizontal = 12.dp)
      )

      Spacer(modifier = Modifier.height(24.dp))

      // ─── 1. INTERACTIVE CHARACTER SPRITE SELECTION & PROGRESS BAR COMPONENT ───
      Card(
        colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated),
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, DarkBorder),
        modifier = Modifier
          .fillMaxWidth()
          .testTag("home_character_selector_card")
      ) {
        Column(modifier = Modifier.padding(18.dp)) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                imageVector = Icons.Default.AutoAwesome,
                contentDescription = null,
                tint = AccentIndigoLight,
                modifier = Modifier.size(18.dp)
              )
              Spacer(modifier = Modifier.width(8.dp))
              Text(
                text = "Animated Companion Sprites",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
              )
            }

            Surface(
              color = AccentIndigoMuted,
              shape = RoundedCornerShape(8.dp)
            ) {
              Text(
                text = "${CharacterRegistry.characters.size} Sprites",
                fontSize = 10.5.sp,
                fontWeight = FontWeight.SemiBold,
                color = AccentIndigoLight,
                modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
              )
            }
          }

          Spacer(modifier = Modifier.height(14.dp))

          // ─── LIVE INTERACTIVE PROGRESS BAR WALK CYCLE SIMULATOR ───
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .height(120.dp)
              .clip(RoundedCornerShape(18.dp))
              .background(Color(0xFF0C0D11))
              .border(1.dp, Color(0x336366F1), RoundedCornerShape(18.dp))
              .padding(12.dp)
          ) {
            Column(
              verticalArrangement = Arrangement.SpaceBetween,
              modifier = Modifier.fillMaxSize()
            ) {
              // Header tag inside simulator
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
              ) {
                Text(
                  text = "LIVE PROGRESS BAR WALK SIMULATION",
                  fontSize = 9.sp,
                  fontWeight = FontWeight.Bold,
                  letterSpacing = 0.8.sp,
                  color = TextMuted
                )
                Text(
                  text = "${activeCharacter.name} • ${overlayConfig.behavior.displayName}",
                  fontSize = 10.sp,
                  fontWeight = FontWeight.SemiBold,
                  color = AccentIndigoLight
                )
              }

              // Canvas with the character walking along the simulated progress bar
              Box(
                modifier = Modifier
                  .fillMaxWidth()
                  .height(58.dp)
              ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                  val canvasWidth = size.width
                  val canvasHeight = size.height
                  val charHeight = canvasHeight * 0.70f

                  // Character X position mapped to current progress bar position
                  val charX = simProgress * (canvasWidth - 40f) + 20f
                  val charY = canvasHeight - 2f

                  val stepDurationMs = WalkCycleMath.getEffectiveStepDurationMs(
                    behavior = overlayConfig.behavior,
                    durationMs = simDurationMs.toLong(),
                    config = overlayConfig,
                    canvasWidth = canvasWidth,
                    canvasHeight = canvasHeight
                  )
                  val phase = ((simTimeMs.toLong() % stepDurationMs).toFloat() / stepDurationMs.toFloat())

                  drawContext.canvas.nativeCanvas.let { nativeCanvas ->
                    CharacterRenderer.drawCharacter(
                      canvas = nativeCanvas,
                      character = activeCharacter,
                      behavior = overlayConfig.behavior,
                      centerX = charX,
                      bottomY = charY,
                      size = charHeight,
                      phase = phase,
                      facingRight = true,
                      currentTimeMs = simTimeMs.toLong()
                    )
                  }
                }
              }

              // Instagram / Reel Progress Bar line
              Column {
                Box(
                  modifier = Modifier
                    .fillMaxWidth()
                    .height(3.5.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Color(0x33FFFFFF))
                ) {
                  Box(
                    modifier = Modifier
                      .fillMaxWidth(simProgress)
                      .height(3.5.dp)
                      .background(
                        Brush.horizontalGradient(
                          listOf(AccentIndigo, AccentIndigoLight, Color.White)
                        )
                      )
                  )
                }

                Spacer(modifier = Modifier.height(3.dp))

                Row(
                  horizontalArrangement = Arrangement.SpaceBetween,
                  modifier = Modifier.fillMaxWidth()
                ) {
                  Text(
                    text = "0:00",
                    fontSize = 8.5.sp,
                    fontFamily = FontFamily.Monospace,
                    color = TextMuted
                  )
                  Text(
                    text = "Reel Timeline Progress",
                    fontSize = 8.5.sp,
                    color = TextMuted
                  )
                  Text(
                    text = "0:05",
                    fontSize = 8.5.sp,
                    fontFamily = FontFamily.Monospace,
                    color = TextMuted
                  )
                }
              }
            }
          }

          Spacer(modifier = Modifier.height(14.dp))

          // ─── CATEGORY FILTER PILLS ───
          LazyRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.fillMaxWidth()
          ) {
            items(ObjectCategory.entries.toTypedArray(), key = { it.name }) { cat ->
              val isSelected = selectedCategory == cat
              FilterChip(
                selected = isSelected,
                onClick = { selectedCategory = cat },
                shape = RoundedCornerShape(10.dp),
                label = {
                  Text(
                    text = "${cat.iconEmoji} ${cat.displayName}",
                    fontSize = 11.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                  )
                },
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

          Spacer(modifier = Modifier.height(10.dp))

          // ─── HORIZONTAL CHARACTER SELECTION CAROUSEL ───
          LazyRow(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier
              .fillMaxWidth()
              .padding(vertical = 4.dp)
          ) {
            items(filteredCharacters, key = { it.id }) { char ->
              CharacterAvatarCard(
                character = char,
                isSelected = char.id == overlayConfig.characterId,
                behavior = overlayConfig.behavior,
                onClick = { onCharacterSelected(char.id) }
              )
            }
          }

          Spacer(modifier = Modifier.height(12.dp))

          // ─── LOCOMOTION & WALKING SPEED BEHAVIOR SELECTOR ───
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                imageVector = Icons.Default.Speed,
                contentDescription = null,
                tint = TextMuted,
                modifier = Modifier.size(14.dp)
              )
              Spacer(modifier = Modifier.width(5.dp))
              Text(
                text = "Gait / Speed:",
                fontSize = 11.5.sp,
                fontWeight = FontWeight.Medium,
                color = TextSecondary
              )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
              AnimationBehavior.entries.take(4).forEach { behavior ->
                val isBehaviorActive = overlayConfig.behavior == behavior
                FilterChip(
                  selected = isBehaviorActive,
                  onClick = { onBehaviorChanged(behavior) },
                  shape = RoundedCornerShape(8.dp),
                  label = {
                    Text(
                      text = behavior.displayName,
                      fontSize = 10.5.sp,
                      fontWeight = if (isBehaviorActive) FontWeight.Bold else FontWeight.Normal
                    )
                  },
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
                    selected = isBehaviorActive
                  )
                )
              }
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(24.dp))

      // ─── PRIMARY ACTION BUTTONS (SELECT VIDEO / TRY SAMPLE) ───
      if (isLoading) {
        CircularProgressIndicator(
          color = AccentIndigo,
          modifier = Modifier
            .size(48.dp)
            .padding(8.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
          text = "Loading video and extracting specs...",
          fontSize = 13.sp,
          color = TextSecondary
        )
      } else {
        // Primary: Select Instagram Reel / Video from Gallery
        Button(
          onClick = {
            try {
              photoPickerLauncher.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly)
              )
            } catch (e: Exception) {
              filePickerLauncher.launch("video/*")
            }
          },
          colors = ButtonDefaults.buttonColors(
            containerColor = AccentIndigo,
            contentColor = Color.White
          ),
          shape = RoundedCornerShape(18.dp),
          modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .shadow(12.dp, RoundedCornerShape(18.dp), spotColor = AccentIndigo)
            .testTag("select_video_button")
        ) {
          Icon(
            imageVector = Icons.Default.VideoLibrary,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(22.dp)
          )
          Spacer(modifier = Modifier.width(10.dp))
          Column {
            Text(
              text = "Select Instagram Reel / Video",
              fontSize = 15.sp,
              fontWeight = FontWeight.Bold,
              color = Color.White
            )
            Text(
              text = "Device Gallery • Reels (9:16) • MP4 / MOV",
              fontSize = 10.5.sp,
              color = Color.White.copy(alpha = 0.8f)
            )
          }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Secondary / Alternative file picker & sample video buttons row
        Row(
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          modifier = Modifier.fillMaxWidth()
        ) {
          // Browse All Files Button
          OutlinedButton(
            onClick = { filePickerLauncher.launch("video/*") },
            colors = ButtonDefaults.outlinedButtonColors(
              containerColor = DarkSurfaceElevated,
              contentColor = TextPrimary
            ),
            border = BorderStroke(1.dp, DarkBorder),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
              .weight(1f)
              .height(48.dp)
              .testTag("browse_files_button")
          ) {
            Text(
              text = "📁 Files / Storage",
              fontSize = 12.sp,
              fontWeight = FontWeight.SemiBold,
              maxLines = 1
            )
          }

          // Quick Start with Sample Video
          OutlinedButton(
            onClick = onSampleVideoRequested,
            colors = ButtonDefaults.outlinedButtonColors(
              containerColor = DarkSurfaceElevated,
              contentColor = TextPrimary
            ),
            border = BorderStroke(1.dp, DarkBorder),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
              .weight(1.2f)
              .height(48.dp)
              .testTag("try_sample_button")
          ) {
            Icon(
              imageVector = Icons.Default.PlayArrow,
              contentDescription = null,
              tint = AccentIndigoLight,
              modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
              text = "Try Reel Sample",
              fontSize = 12.sp,
              fontWeight = FontWeight.SemiBold,
              maxLines = 1
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(28.dp))

      // ─── 3-STEP WORKFLOW CARDS ───
      Text(
        text = "How It Works",
        fontSize = 13.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 0.5.sp,
        color = TextMuted,
        modifier = Modifier.fillMaxWidth()
      )

      Spacer(modifier = Modifier.height(12.dp))

      WorkflowStepCard(
        stepNumber = "1",
        title = "Choose Animated Companion",
        description = "Select from 3D-styled pixel sprites, spinning gems, planets, or animals with real-time stride pace synchronization."
      )

      Spacer(modifier = Modifier.height(8.dp))

      WorkflowStepCard(
        stepNumber = "2",
        title = "Tune Position & Split-Screen Preview",
        description = "Adjust size and vertical height above the progress bar with dual split-screen side-by-side original comparison."
      )

      Spacer(modifier = Modifier.height(8.dp))

      WorkflowStepCard(
        stepNumber = "3",
        title = "Export with MediaCodec Hardware Engine",
        description = "Burn character animations directly into high-quality MP4 video frames with original audio preserved bit-for-bit."
      )

      Spacer(modifier = Modifier.height(24.dp))

      // Local Privacy Guarantee Badge
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        modifier = Modifier
          .fillMaxWidth()
          .padding(8.dp)
      ) {
        Icon(
          imageVector = Icons.Default.Lock,
          contentDescription = null,
          tint = AccentGreen,
          modifier = Modifier.size(14.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
          text = "100% Local On-Device Processing • Your video never leaves your phone",
          fontSize = 11.sp,
          color = TextMuted
        )
      }

      Spacer(modifier = Modifier.height(16.dp))
    }
  }
}

@Composable
private fun WorkflowStepCard(
  stepNumber: String,
  title: String,
  description: String
) {
  Surface(
    color = DarkSurfaceElevated,
    shape = RoundedCornerShape(16.dp),
    border = BorderStroke(1.dp, DarkBorder),
    modifier = Modifier.fillMaxWidth()
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      modifier = Modifier.padding(16.dp)
    ) {
      Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
          .size(32.dp)
          .clip(CircleShape)
          .background(AccentIndigoMuted)
          .border(1.dp, AccentIndigoLight.copy(alpha = 0.3f), CircleShape)
      ) {
        Text(
          text = stepNumber,
          fontSize = 13.sp,
          fontWeight = FontWeight.Bold,
          color = AccentIndigoLight
        )
      }

      Spacer(modifier = Modifier.width(14.dp))

      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = title,
          fontSize = 14.sp,
          fontWeight = FontWeight.SemiBold,
          color = TextPrimary
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
          text = description,
          fontSize = 12.sp,
          color = TextSecondary,
          lineHeight = 16.sp
        )
      }
    }
  }
}
