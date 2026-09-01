@file:OptIn(androidx.media3.common.util.UnstableApi::class)

package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import com.example.characters.CharacterRegistry
import com.example.characters.CharacterRenderer
import com.example.model.CharacterOverlayConfig
import com.example.model.ExportState
import com.example.ui.theme.AccentGold
import com.example.ui.theme.AccentGreen
import com.example.ui.theme.AccentPrimary
import com.example.ui.theme.AccentPrimaryActive
import com.example.ui.theme.AccentPrimaryLight
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkBorderSubtle
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun ExportScreen(
  exportState: ExportState,
  overlayConfig: CharacterOverlayConfig,
  onCancelClicked: () -> Unit,
  onDoneClicked: () -> Unit,
  onRetryClicked: () -> Unit,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val character = remember(overlayConfig.characterId) {
    CharacterRegistry.getById(overlayConfig.characterId)
  }

  val renderStartTime = remember { mutableLongStateOf(System.currentTimeMillis()) }

  Box(
    contentAlignment = Alignment.Center,
    modifier = modifier
      .fillMaxSize()
      .background(DarkBackground)
      .padding(24.dp)
  ) {
    when (exportState) {
      is ExportState.Idle, is ExportState.Preparing -> {
        Column(
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.Center
        ) {
          CircularProgressIndicator(
            color = AccentPrimary,
            strokeWidth = 4.dp,
            modifier = Modifier.size(56.dp)
          )
          Spacer(modifier = Modifier.height(24.dp))
          Text(
            text = "Preparing Export...",
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold,
            color = TextPrimary
          )
          Spacer(modifier = Modifier.height(8.dp))
          Text(
            text = if (exportState is ExportState.Preparing) exportState.statusMessage else "Setting up hardware encoder...",
            fontSize = 13.sp,
            color = TextSecondary,
            textAlign = TextAlign.Center
          )
        }
      }

      is ExportState.Rendering, ExportState.Finalizing -> {
        val progress = if (exportState is ExportState.Rendering) exportState.progress else 0.99f
        val currentFrame = if (exportState is ExportState.Rendering) exportState.currentFrame else 0
        val totalFrames = if (exportState is ExportState.Rendering) exportState.totalFrames else 0
        val statusText = if (exportState is ExportState.Rendering) exportState.statusMessage else "Finalizing MP4 container & saving..."

        // Calculate ETA
        val elapsedMs = System.currentTimeMillis() - renderStartTime.longValue
        val etaText = if (progress > 0.05f && progress < 0.98f && elapsedMs > 1000) {
          val totalEstimatedMs = elapsedMs / progress
          val remainingSec = ((totalEstimatedMs - elapsedMs) / 1000).toInt().coerceAtLeast(1)
          if (remainingSec > 60) {
            "ETA: ~${remainingSec / 60}m ${remainingSec % 60}s remaining"
          } else {
            "ETA: ~${remainingSec}s remaining"
          }
        } else if (progress >= 0.98f) {
          "Finishing up..."
        } else {
          "Estimating time..."
        }

        Column(
          horizontalAlignment = Alignment.CenterHorizontally,
          modifier = Modifier
            .fillMaxWidth()
            .widthIn(max = 480.dp)
        ) {
          Text(
            text = "Rendering Walkbar Video",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary,
            letterSpacing = (-0.5).sp
          )

          Spacer(modifier = Modifier.height(8.dp))

          Text(
            text = "Hardware accelerated rendering in progress",
            fontSize = 13.sp,
            color = TextSecondary,
            textAlign = TextAlign.Center
          )

          Spacer(modifier = Modifier.height(32.dp))

          // Sleek Circular percentage indicator
          Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(160.dp)
          ) {
            CircularProgressIndicator(
              progress = { progress },
              color = AccentPrimary,
              trackColor = DarkBorder,
              strokeWidth = 8.dp,
              modifier = Modifier.fillMaxSize()
            )

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
              Text(
                text = "${(progress * 100).toInt()}%",
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                color = TextPrimary
              )
              if (totalFrames > 0) {
                Text(
                  text = "$currentFrame / $totalFrames",
                  fontSize = 11.sp,
                  fontFamily = FontFamily.Monospace,
                  color = TextMuted
                )
              }
              Spacer(modifier = Modifier.height(2.dp))
              Text(
                text = etaText,
                fontSize = 10.sp,
                color = AccentGold,
                fontWeight = FontWeight.SemiBold
              )
            }
          }

          Spacer(modifier = Modifier.height(28.dp))

          // Animated Walking Character along Progress Line
          Surface(
            color = DarkSurfaceElevated,
            shape = RoundedCornerShape(20.dp),
            border = BorderStroke(1.dp, DarkBorder),
            modifier = Modifier
              .fillMaxWidth()
              .height(90.dp)
          ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
              val w = size.width
              val h = size.height
              val charX = (w * 0.1f) + progress * (w * 0.8f)
              val charY = h * 0.72f
              val charSize = h * 0.45f
              val phase = (progress * 12f) % 1f

              // Draw track line
              drawLine(
                color = Color(0xFF2C2C36),
                start = androidx.compose.ui.geometry.Offset(w * 0.08f, charY + 2f),
                end = androidx.compose.ui.geometry.Offset(w * 0.92f, charY + 2f),
                strokeWidth = 3f
              )
              // Draw active progress line
              drawLine(
                color = AccentPrimary,
                start = androidx.compose.ui.geometry.Offset(w * 0.08f, charY + 2f),
                end = androidx.compose.ui.geometry.Offset(charX, charY + 2f),
                strokeWidth = 4f
              )

              drawContext.canvas.nativeCanvas.let { nativeCanvas ->
                CharacterRenderer.drawCharacter(
                  canvas = nativeCanvas,
                  character = character,
                  behavior = overlayConfig.behavior,
                  centerX = charX,
                  bottomY = charY,
                  size = charSize,
                  phase = phase,
                  facingRight = true,
                  currentTimeMs = (progress * 10000L).toLong()
                )
              }
            }
          }

          Spacer(modifier = Modifier.height(18.dp))

          Text(
            text = statusText,
            fontSize = 12.sp,
            color = TextSecondary,
            textAlign = TextAlign.Center
          )

          Spacer(modifier = Modifier.height(28.dp))

          OutlinedButton(
            onClick = onCancelClicked,
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFEF4444)),
            border = BorderStroke(1.dp, Color(0xFF7F1D1D)),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
              .fillMaxWidth(0.6f)
              .height(44.dp)
              .testTag("cancel_export_button")
          ) {
            Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Cancel", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
          }
        }
      }

      is ExportState.Success -> {
        val scrollState = rememberScrollState()
        Column(
          horizontalAlignment = Alignment.CenterHorizontally,
          modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .widthIn(max = 520.dp)
        ) {
          Spacer(modifier = Modifier.height(10.dp))

          // Success Header
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
          ) {
            Icon(
              imageVector = Icons.Default.CheckCircle,
              contentDescription = null,
              tint = AccentGreen,
              modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
              text = "Export Complete!",
              fontSize = 24.sp,
              fontWeight = FontWeight.Bold,
              color = TextPrimary,
              letterSpacing = (-0.5).sp
            )
          }

          Spacer(modifier = Modifier.height(6.dp))

          Text(
            text = "Your video with ${character.name} walking companion is ready!",
            fontSize = 13.sp,
            color = TextSecondary,
            textAlign = TextAlign.Center
          )

          Spacer(modifier = Modifier.height(16.dp))

          // Video Preview of Exported Result with sleek border
          ExportedVideoPlayerPreview(
            uri = exportState.outputUri,
            aspectRatio = if (exportState.height > 0) exportState.width.toFloat() / exportState.height.toFloat() else 9f / 16f,
            modifier = Modifier
              .fillMaxWidth()
              .height(280.dp)
              .shadow(20.dp, RoundedCornerShape(20.dp))
              .clip(RoundedCornerShape(20.dp))
              .border(1.dp, DarkBorderSubtle, RoundedCornerShape(20.dp))
              .background(Color.Black)
          )

          Spacer(modifier = Modifier.height(16.dp))

          // File Info Chip
          Surface(
            color = DarkSurfaceElevated,
            shape = RoundedCornerShape(20.dp),
            border = BorderStroke(1.dp, DarkBorder),
            modifier = Modifier.fillMaxWidth()
          ) {
            Row(
              horizontalArrangement = Arrangement.SpaceAround,
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier.padding(14.dp)
            ) {
              Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Resolution", fontSize = 10.sp, color = TextMuted)
                Text("${exportState.width} × ${exportState.height}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
              }
              Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Duration", fontSize = 10.sp, color = TextMuted)
                Text("${exportState.durationMs / 1000}s", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
              }
              Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Size", fontSize = 10.sp, color = TextMuted)
                val mb = exportState.fileSizeBytes.toDouble() / (1024.0 * 1024.0)
                Text(String.format("%.1f MB", mb), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = AccentGreen)
              }
            }
          }

          Spacer(modifier = Modifier.height(20.dp))

          // Share Button
          Surface(
            onClick = {
              shareVideo(context, exportState.outputUri)
            },
            shape = RoundedCornerShape(18.dp),
            color = Color.Transparent,
            modifier = Modifier
              .fillMaxWidth()
              .height(52.dp)
              .shadow(16.dp, RoundedCornerShape(18.dp), spotColor = AccentPrimary)
              .testTag("share_video_button")
          ) {
            Box(
              contentAlignment = Alignment.Center,
              modifier = Modifier
                .fillMaxSize()
                .background(Brush.linearGradient(listOf(AccentPrimary, AccentPrimaryActive)))
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                  imageVector = Icons.Default.Share,
                  contentDescription = null,
                  tint = Color.White,
                  modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                  text = "Share Video to Social Apps",
                  fontSize = 15.sp,
                  fontWeight = FontWeight.Bold,
                  color = Color.White
                )
              }
            }
          }

          Spacer(modifier = Modifier.height(10.dp))

          // Done Button
          OutlinedButton(
            onClick = onDoneClicked,
            colors = ButtonDefaults.outlinedButtonColors(
              containerColor = DarkSurfaceElevated,
              contentColor = TextPrimary
            ),
            border = BorderStroke(1.dp, DarkBorder),
            shape = RoundedCornerShape(18.dp),
            modifier = Modifier
              .fillMaxWidth()
              .height(48.dp)
              .testTag("done_export_button")
          ) {
            Icon(
              imageVector = Icons.Default.Home,
              contentDescription = null,
              modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Done / Create Another", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
          }

          Spacer(modifier = Modifier.height(16.dp))
        }
      }

      is ExportState.Error -> {
        Column(
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.Center,
          modifier = Modifier.widthIn(max = 440.dp)
        ) {
          Icon(
            imageVector = Icons.Default.ErrorOutline,
            contentDescription = null,
            tint = Color(0xFFEF4444),
            modifier = Modifier.size(56.dp)
          )

          Spacer(modifier = Modifier.height(16.dp))

          Text(
            text = "Export Problem",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
          )

          Spacer(modifier = Modifier.height(8.dp))

          Text(
            text = exportState.userFriendlyMessage,
            fontSize = 13.sp,
            color = TextSecondary,
            textAlign = TextAlign.Center,
            lineHeight = 18.sp
          )

          Spacer(modifier = Modifier.height(28.dp))

          Button(
            onClick = onRetryClicked,
            colors = ButtonDefaults.buttonColors(
              containerColor = AccentPrimary,
              contentColor = Color.White
            ),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
              .fillMaxWidth()
              .height(48.dp)
              .shadow(12.dp, RoundedCornerShape(16.dp), spotColor = AccentPrimary)
              .testTag("retry_export_button")
          ) {
            Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Retry Export", fontSize = 14.sp, fontWeight = FontWeight.Bold)
          }

          Spacer(modifier = Modifier.height(10.dp))

          OutlinedButton(
            onClick = onCancelClicked,
            colors = ButtonDefaults.outlinedButtonColors(
              containerColor = DarkSurfaceElevated,
              contentColor = TextPrimary
            ),
            border = BorderStroke(1.dp, DarkBorder),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
              .fillMaxWidth()
              .height(44.dp)
          ) {
            Text("Back to Editor", fontSize = 13.sp)
          }
        }
      }
    }
  }
}

@Composable
private fun ExportedVideoPlayerPreview(
  uri: Uri,
  aspectRatio: Float,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val player = remember(uri) {
    ExoPlayer.Builder(context).build().apply {
      setMediaItem(MediaItem.fromUri(uri))
      repeatMode = Player.REPEAT_MODE_ALL
      prepare()
      playWhenReady = true
    }
  }

  DisposableEffect(player) {
    onDispose {
      player.release()
    }
  }

  AndroidView(
    factory = { ctx ->
      PlayerView(ctx).apply {
        this.player = player
        useController = true
        resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
        layoutParams = FrameLayout.LayoutParams(
          ViewGroup.LayoutParams.MATCH_PARENT,
          ViewGroup.LayoutParams.MATCH_PARENT
        )
      }
    },
    modifier = modifier
  )
}

private fun shareVideo(context: Context, videoUri: Uri) {
  try {
    val shareIntent = Intent(Intent.ACTION_SEND).apply {
      type = "video/mp4"
      putExtra(Intent.EXTRA_STREAM, videoUri)
      putExtra(Intent.EXTRA_TEXT, "Look at my video timeline walking companion! 🐾 #walkbar")
      addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(Intent.createChooser(shareIntent, "Share Walkbar Video"))
  } catch (e: Exception) {
    e.printStackTrace()
  }
}
