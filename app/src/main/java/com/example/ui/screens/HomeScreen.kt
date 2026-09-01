@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
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
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.platform.LocalContext
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
import com.example.ui.components.AboutAppSheet
import com.example.ui.components.CharacterAvatarCard
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.AccentGold
import com.example.ui.theme.AccentGreen
import com.example.ui.theme.AccentPrimary
import com.example.ui.theme.AccentPrimaryActive
import com.example.ui.theme.AccentPrimaryLight
import com.example.ui.theme.AccentPrimaryMuted
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurfaceCard
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun HomeScreen(
  isLoading: Boolean,
  overlayConfig: CharacterOverlayConfig,
  onCharacterSelected: (String) -> Unit,
  onBehaviorChanged: (AnimationBehavior) -> Unit,
  onVideoSelected: (Uri) -> Unit,
  onSampleVideoRequested: () -> Unit,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  var showAboutSheet by remember { mutableStateOf(false) }

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
  val simDurationMs = 4500 // 4.5 seconds full cycle
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
        .padding(horizontal = 20.dp, vertical = 14.dp)
        .widthIn(max = 600.dp)
    ) {
      // ─── TOP BAR WITH ABOUT BUTTON ───
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth()
      ) {
        Surface(
          color = DarkSurfaceElevated,
          shape = RoundedCornerShape(20.dp),
          border = BorderStroke(1.dp, DarkBorder)
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
          ) {
            Box(
              modifier = Modifier
                .size(7.dp)
                .clip(CircleShape)
                .background(AccentPrimary)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = "Walkbar Studio",
              fontSize = 11.5.sp,
              fontWeight = FontWeight.SemiBold,
              color = TextSecondary
            )
          }
        }

        IconButton(
          onClick = { showAboutSheet = true },
          modifier = Modifier
            .size(38.dp)
            .clip(CircleShape)
            .background(DarkSurfaceElevated)
            .border(1.dp, DarkBorder, CircleShape)
            .testTag("about_app_button")
        ) {
          Icon(
            imageVector = Icons.Default.Info,
            contentDescription = "About App & Developer",
            tint = AccentPrimaryLight,
            modifier = Modifier.size(20.dp)
          )
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

      // ─── HERO TITLE & BADGE ───
      Text(
        text = "Walkbar",
        fontSize = 36.sp,
        fontWeight = FontWeight.Bold,
        color = TextPrimary,
        letterSpacing = (-0.8).sp
      )

      Spacer(modifier = Modifier.height(4.dp))

      Text(
        text = "Sync animated character companions along your video timeline in real-time.",
        fontSize = 13.5.sp,
        color = TextSecondary,
        textAlign = TextAlign.Center,
        lineHeight = 19.sp,
        modifier = Modifier.padding(horizontal = 16.dp)
      )

      Spacer(modifier = Modifier.height(18.dp))

      // ─── INTERACTIVE LIVE SIMULATION HERO CARD ───
      Card(
        colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, DarkBorder),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(14.dp)
        ) {
          // Simulation Header Row
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                  .size(24.dp)
                  .clip(CircleShape)
                  .background(AccentPrimaryMuted)
              ) {
                Icon(
                  imageVector = Icons.Default.AutoAwesome,
                  contentDescription = null,
                  tint = AccentPrimaryLight,
                  modifier = Modifier.size(14.dp)
                )
              }
              Spacer(modifier = Modifier.width(8.dp))
              Text(
                text = "Live Timeline Preview",
                fontSize = 12.5.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
              )
            }

            Surface(
              color = AccentPrimaryMuted,
              shape = RoundedCornerShape(8.dp),
              border = BorderStroke(0.5.dp, AccentPrimaryLight.copy(alpha = 0.5f))
            ) {
              Text(
                text = "${activeCharacter.name} • ${overlayConfig.behavior.displayName}",
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                color = AccentPrimaryLight,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
              )
            }
          }

          Spacer(modifier = Modifier.height(12.dp))

          // ─── SIMULATION STAGE (Black Glass Viewport) ───
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .height(96.dp)
              .clip(RoundedCornerShape(14.dp))
              .background(Color(0xFF0D0D12))
              .border(1.dp, DarkBorder, RoundedCornerShape(14.dp))
              .padding(horizontal = 10.dp, vertical = 6.dp)
          ) {
            Column(
              verticalArrangement = Arrangement.SpaceBetween,
              modifier = Modifier.fillMaxSize()
            ) {
              // Active Character Animation Canvas
              Box(
                modifier = Modifier
                  .fillMaxWidth()
                  .weight(1f)
              ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                  val canvasWidth = size.width
                  val canvasHeight = size.height
                  val charHeight = canvasHeight * 0.85f

                  // Character X position mapped to current progress bar position
                  val charX = simProgress * (canvasWidth - 36f) + 18f
                  val charY = canvasHeight - 1f

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

              // Scrubber Line
              Column {
                Box(
                  modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Color(0x33FFFFFF))
                ) {
                  Box(
                    modifier = Modifier
                      .fillMaxWidth(simProgress)
                      .height(3.dp)
                      .background(
                        Brush.horizontalGradient(
                          listOf(AccentPrimary, AccentPrimaryLight, Color.White)
                        )
                      )
                  )
                }

                Spacer(modifier = Modifier.height(2.dp))

                Row(
                  horizontalArrangement = Arrangement.SpaceBetween,
                  modifier = Modifier.fillMaxWidth()
                ) {
                  Text(
                    text = "0:00",
                    fontSize = 8.sp,
                    fontFamily = FontFamily.Monospace,
                    color = TextMuted
                  )
                  Text(
                    text = "Video Timeline Scrubber",
                    fontSize = 8.sp,
                    color = TextMuted
                  )
                  Text(
                    text = "0:05",
                    fontSize = 8.sp,
                    fontFamily = FontFamily.Monospace,
                    color = TextMuted
                  )
                }
              }
            }
          }

          Spacer(modifier = Modifier.height(12.dp))

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
                  selectedContainerColor = AccentPrimary,
                  selectedLabelColor = Color.White,
                  containerColor = DarkSurfaceCard,
                  labelColor = TextSecondary
                ),
                border = FilterChipDefaults.filterChipBorder(
                  borderColor = DarkBorder,
                  selectedBorderColor = AccentPrimaryLight,
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
              .padding(vertical = 2.dp)
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
          Text(
            text = "GAIT / SPEED:",
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = TextMuted,
            letterSpacing = 0.5.sp
          )

          Spacer(modifier = Modifier.height(6.dp))

          Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.fillMaxWidth()
          ) {
            val behaviors = listOf(
              AnimationBehavior.PACE_SYNC,
              AnimationBehavior.WALK,
              AnimationBehavior.RUN,
              AnimationBehavior.HOP
            )

            behaviors.forEach { behavior ->
              val isBehaviorActive = overlayConfig.behavior == behavior
              Surface(
                color = if (isBehaviorActive) AccentPrimary else DarkSurfaceCard,
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, if (isBehaviorActive) AccentPrimaryLight else DarkBorder),
                modifier = Modifier
                  .weight(1f)
                  .height(38.dp)
                  .clickable { onBehaviorChanged(behavior) }
              ) {
                Box(
                  contentAlignment = Alignment.Center,
                  modifier = Modifier.fillMaxSize().padding(horizontal = 4.dp)
                ) {
                  Text(
                    text = behavior.displayName,
                    fontSize = 11.sp,
                    fontWeight = if (isBehaviorActive) FontWeight.Bold else FontWeight.Medium,
                    color = if (isBehaviorActive) Color.White else TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                  )
                }
              }
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(18.dp))

      // ─── PRIMARY ACTION BUTTONS (SELECT VIDEO / TRY SAMPLE) ───
      if (isLoading) {
        CircularProgressIndicator(
          color = AccentPrimary,
          modifier = Modifier
            .size(44.dp)
            .padding(8.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
          text = "Loading video and analyzing properties...",
          fontSize = 13.sp,
          color = TextSecondary
        )
      } else {
        // Primary: Select Video from Device Gallery (with gradient + 16dp spot shadow)
        Surface(
          onClick = {
            try {
              photoPickerLauncher.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly)
              )
            } catch (e: Exception) {
              filePickerLauncher.launch("video/*")
            }
          },
          shape = RoundedCornerShape(18.dp),
          color = Color.Transparent,
          modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .shadow(16.dp, RoundedCornerShape(18.dp), spotColor = AccentPrimary)
            .testTag("select_video_button")
        ) {
          Box(
            contentAlignment = Alignment.CenterStart,
            modifier = Modifier
              .fillMaxSize()
              .background(Brush.linearGradient(listOf(AccentPrimary, AccentPrimaryActive)))
              .padding(horizontal = 18.dp)
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                imageVector = Icons.Default.VideoLibrary,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(24.dp)
              )
              Spacer(modifier = Modifier.width(12.dp))
              Column {
                Text(
                  text = "Select Video from Gallery",
                  fontSize = 15.sp,
                  fontWeight = FontWeight.Bold,
                  color = Color.White
                )
                Text(
                  text = "Supports Reels, Shorts, TikTok, Clips • MP4, MOV, WEBM",
                  fontSize = 10.5.sp,
                  color = Color.White.copy(alpha = 0.85f)
                )
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Secondary / Alternative file picker & sample video buttons row
        Row(
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          modifier = Modifier.fillMaxWidth()
        ) {
          OutlinedButton(
            onClick = { filePickerLauncher.launch("video/*") },
            colors = ButtonDefaults.outlinedButtonColors(
              containerColor = DarkSurfaceElevated,
              contentColor = TextPrimary
            ),
            border = BorderStroke(1.dp, DarkBorder),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
              .weight(1f)
              .height(48.dp)
              .testTag("browse_files_button")
          ) {
            Text(
              text = "📁 Browse Files",
              fontSize = 12.sp,
              fontWeight = FontWeight.SemiBold,
              maxLines = 1
            )
          }

          OutlinedButton(
            onClick = onSampleVideoRequested,
            colors = ButtonDefaults.outlinedButtonColors(
              containerColor = DarkSurfaceElevated,
              contentColor = TextPrimary
            ),
            border = BorderStroke(1.dp, DarkBorder),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
              .weight(1.1f)
              .height(48.dp)
              .testTag("try_sample_button")
          ) {
            Icon(
              imageVector = Icons.Default.PlayArrow,
              contentDescription = null,
              tint = AccentPrimaryLight,
              modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
              text = "Try Demo Video",
              fontSize = 12.sp,
              fontWeight = FontWeight.SemiBold,
              maxLines = 1
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(22.dp))

      // ─── 3-STEP WORKFLOW CARDS ───
      Text(
        text = "HOW IT WORKS",
        fontSize = 11.5.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 0.8.sp,
        color = TextMuted,
        modifier = Modifier.fillMaxWidth()
      )

      Spacer(modifier = Modifier.height(8.dp))

      WorkflowStepCard(
        stepNumber = "1",
        title = "Choose Animated Companion",
        description = "Select from 16+ pixel sprites, gems, planets, and creatures with real-time stride pace synchronization."
      )

      Spacer(modifier = Modifier.height(8.dp))

      WorkflowStepCard(
        stepNumber = "2",
        title = "Tune Position & Large Preview",
        description = "Adjust size, height above the progress line, and verify alignment with Instagram Reels & YouTube Shorts guides."
      )

      Spacer(modifier = Modifier.height(8.dp))

      WorkflowStepCard(
        stepNumber = "3",
        title = "Fast Local Hardware Export",
        description = "Burn character animations directly into high-quality MP4 video frames with original audio preserved bit-for-bit."
      )

      Spacer(modifier = Modifier.height(20.dp))

      // ─── ABOUT APP & DEVELOPER SECTION (SaddexRnx) ───
      Text(
        text = "ABOUT & DEVELOPER",
        fontSize = 11.5.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 0.8.sp,
        color = TextMuted,
        modifier = Modifier.fillMaxWidth()
      )

      Spacer(modifier = Modifier.height(8.dp))

      Card(
        colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, DarkBorder),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                  .size(38.dp)
                  .clip(CircleShape)
                  .background(Color(0xFF1E293B))
                  .border(1.dp, AccentCyan, CircleShape)
              ) {
                Icon(
                  imageVector = Icons.Default.Code,
                  contentDescription = null,
                  tint = AccentCyan,
                  modifier = Modifier.size(20.dp)
                )
              }

              Spacer(modifier = Modifier.width(10.dp))

              Column {
                Text(
                  text = "Walkbar Studio",
                  fontSize = 14.5.sp,
                  fontWeight = FontWeight.Bold,
                  color = TextPrimary
                )
                Text(
                  text = "Version 1.2.0 • 100% Local Engine",
                  fontSize = 10.5.sp,
                  color = AccentGreen,
                  fontWeight = FontWeight.SemiBold
                )
              }
            }

            Surface(
              color = AccentGold.copy(alpha = 0.15f),
              shape = RoundedCornerShape(8.dp),
              border = BorderStroke(1.dp, AccentGold.copy(alpha = 0.4f))
            ) {
              Text(
                text = "Pro Edition",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = AccentGold,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
              )
            }
          }

          Spacer(modifier = Modifier.height(12.dp))

          Text(
            text = "Created by SaddexRnx. Built with Kotlin, Jetpack Compose, and native Android MediaCodec hardware acceleration.",
            fontSize = 12.sp,
            color = TextSecondary,
            lineHeight = 17.sp
          )

          Spacer(modifier = Modifier.height(12.dp))

          // GitHub Profile Direct Card
          Surface(
            color = DarkSurfaceCard,
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, DarkBorder),
            modifier = Modifier.fillMaxWidth()
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.SpaceBetween,
              modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)
            ) {
              Column {
                Text(
                  text = "Developer GitHub",
                  fontSize = 10.5.sp,
                  color = TextMuted
                )
                Text(
                  text = "https://github.com/SaddexRnx",
                  fontSize = 12.5.sp,
                  fontFamily = FontFamily.Monospace,
                  fontWeight = FontWeight.SemiBold,
                  color = AccentCyan
                )
              }

              Row {
                IconButton(
                  onClick = {
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    val clip = ClipData.newPlainText("GitHub Profile", "https://github.com/SaddexRnx")
                    clipboard.setPrimaryClip(clip)
                    Toast.makeText(context, "GitHub URL copied to clipboard!", Toast.LENGTH_SHORT).show()
                  },
                  modifier = Modifier.size(34.dp)
                ) {
                  Icon(
                    imageVector = Icons.Default.ContentCopy,
                    contentDescription = "Copy GitHub URL",
                    tint = TextSecondary,
                    modifier = Modifier.size(16.dp)
                  )
                }

                IconButton(
                  onClick = {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/SaddexRnx")).apply {
                      flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    try {
                      context.startActivity(intent)
                    } catch (e: Exception) {
                      Toast.makeText(context, "Could not open browser", Toast.LENGTH_SHORT).show()
                    }
                  },
                  modifier = Modifier.size(34.dp)
                ) {
                  Icon(
                    imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                    contentDescription = "Open GitHub",
                    tint = AccentPrimaryLight,
                    modifier = Modifier.size(16.dp)
                  )
                }
              }
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

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
          text = "100% Local On-Device Processing • Zero Data Collection",
          fontSize = 11.sp,
          color = TextMuted
        )
      }

      Spacer(modifier = Modifier.height(14.dp))
    }
  }

  if (showAboutSheet) {
    AboutAppSheet(onDismissRequest = { showAboutSheet = false })
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
      modifier = Modifier.padding(14.dp)
    ) {
      Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
          .size(30.dp)
          .clip(CircleShape)
          .background(AccentPrimaryMuted)
          .border(1.dp, AccentPrimaryLight.copy(alpha = 0.3f), CircleShape)
      ) {
        Text(
          text = stepNumber,
          fontSize = 12.sp,
          fontWeight = FontWeight.Bold,
          color = AccentPrimaryLight
        )
      }

      Spacer(modifier = Modifier.width(12.dp))

      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = title,
          fontSize = 13.5.sp,
          fontWeight = FontWeight.SemiBold,
          color = TextPrimary
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
          text = description,
          fontSize = 11.5.sp,
          color = TextSecondary,
          lineHeight = 16.sp
        )
      }
    }
  }
}
