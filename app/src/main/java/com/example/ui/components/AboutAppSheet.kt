package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.AccentGreen
import com.example.ui.theme.AccentIndigo
import com.example.ui.theme.AccentIndigoLight
import com.example.ui.theme.AccentIndigoMuted
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceCard
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutAppSheet(
  onDismissRequest: () -> Unit
) {
  val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
  val context = LocalContext.current
  val scrollState = rememberScrollState()

  ModalBottomSheet(
    onDismissRequest = onDismissRequest,
    sheetState = sheetState,
    containerColor = DarkSurfaceElevated,
    scrimColor = Color(0x99000000)
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .verticalScroll(scrollState)
        .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
      // ─── 1. APP HERO HEADER ───
      Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
      ) {
        Box(
          contentAlignment = Alignment.Center,
          modifier = Modifier
            .size(52.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Brush.linearGradient(listOf(AccentIndigo, AccentCyan)))
        ) {
          Icon(
            imageVector = Icons.Default.AutoAwesome,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(28.dp)
          )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
              text = "Walkbar Studio",
              fontSize = 19.sp,
              fontWeight = FontWeight.Bold,
              color = TextPrimary
            )
            Spacer(modifier = Modifier.width(8.dp))
            Surface(
              color = AccentIndigoMuted,
              shape = RoundedCornerShape(6.dp)
            ) {
              Text(
                text = "v1.2.0 Pro",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = AccentIndigoLight,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
              )
            }
          }
          Spacer(modifier = Modifier.height(2.dp))
          Text(
            text = "Real-Time Timeline Synchronized Companion Engine",
            fontSize = 12.sp,
            color = TextSecondary
          )
        }
      }

      Spacer(modifier = Modifier.height(18.dp))

      // ─── 2. DEVELOPER PROFILE CARD (GitHub: SaddexRnx) ───
      Card(
        colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard),
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.dp, AccentIndigoLight.copy(alpha = 0.5f)),
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
                  .size(40.dp)
                  .clip(CircleShape)
                  .background(Color(0xFF1E293B))
                  .border(1.dp, AccentCyan, CircleShape)
              ) {
                Icon(
                  imageVector = Icons.Default.Code,
                  contentDescription = null,
                  tint = AccentCyan,
                  modifier = Modifier.size(22.dp)
                )
              }

              Spacer(modifier = Modifier.width(12.dp))

              Column {
                Text(
                  text = "Developer",
                  fontSize = 10.5.sp,
                  color = TextMuted,
                  fontWeight = FontWeight.SemiBold
                )
                Text(
                  text = "SaddexRnx",
                  fontSize = 16.sp,
                  fontWeight = FontWeight.Bold,
                  color = TextPrimary
                )
              }
            }

            Surface(
              color = Color(0x3310B981),
              shape = RoundedCornerShape(8.dp)
            ) {
              Text(
                text = "Author & Creator",
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                color = AccentGreen,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
              )
            }
          }

          Spacer(modifier = Modifier.height(12.dp))

          // GitHub Link Preview Box
          Surface(
            color = DarkSurfaceElevated,
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
                  text = "GitHub Profile",
                  fontSize = 11.sp,
                  color = TextMuted
                )
                Text(
                  text = "https://github.com/SaddexRnx",
                  fontSize = 13.sp,
                  fontFamily = FontFamily.Monospace,
                  fontWeight = FontWeight.SemiBold,
                  color = AccentCyan
                )
              }

              IconButton(
                onClick = {
                  val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                  val clip = ClipData.newPlainText("GitHub Profile", "https://github.com/SaddexRnx")
                  clipboard.setPrimaryClip(clip)
                  Toast.makeText(context, "GitHub URL copied to clipboard!", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier.size(36.dp)
              ) {
                Icon(
                  imageVector = Icons.Default.ContentCopy,
                  contentDescription = "Copy GitHub URL",
                  tint = TextSecondary,
                  modifier = Modifier.size(18.dp)
                )
              }
            }
          }

          Spacer(modifier = Modifier.height(12.dp))

          // Open in Browser Button
          Button(
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
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
              containerColor = AccentIndigo,
              contentColor = Color.White
            ),
            modifier = Modifier
              .fillMaxWidth()
              .height(44.dp)
              .testTag("open_github_button")
          ) {
            Icon(
              imageVector = Icons.AutoMirrored.Filled.OpenInNew,
              contentDescription = null,
              tint = Color.White,
              modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = "Visit GitHub @SaddexRnx",
              fontSize = 13.sp,
              fontWeight = FontWeight.Bold
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(18.dp))

      // ─── 3. HOW WALKBAR WORKS & ARCHITECTURE ───
      Text(
        text = "HOW WALKBAR WORKS",
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 0.8.sp,
        color = TextMuted
      )

      Spacer(modifier = Modifier.height(10.dp))

      FeatureInfoRow(
        icon = Icons.Default.Speed,
        iconTint = AccentIndigoLight,
        title = "Dynamic Stride Kinematics",
        description = "Computes video length, horizontal span, and character stride scale to perfectly pace leg swings without awkward foot sliding across progress bars."
      )

      Spacer(modifier = Modifier.height(10.dp))

      FeatureInfoRow(
        icon = Icons.Default.AutoAwesome,
        iconTint = AccentCyan,
        title = "Hardware MediaCodec Encoding",
        description = "Decodes directly into an OpenGL hardware surface at 200+ FPS, compositing anti-aliased character frames with exact presentation timestamps (PTS)."
      )

      Spacer(modifier = Modifier.height(10.dp))

      FeatureInfoRow(
        icon = Icons.Default.Lock,
        iconTint = AccentGreen,
        title = "100% Offline & Private Local Engine",
        description = "Your video files never leave your device. All rendering, stride calculations, and MP4 generation execute entirely on-device with zero network requests."
      )

      Spacer(modifier = Modifier.height(10.dp))

      FeatureInfoRow(
        icon = Icons.Default.VerifiedUser,
        iconTint = AccentIndigoLight,
        title = "Bit-for-Bit Audio Passthrough",
        description = "Preserves 100% of original AAC/Opus audio quality and volume dynamics without lossy re-encoding or synchronization drift."
      )

      Spacer(modifier = Modifier.height(24.dp))
    }
  }
}

@Composable
private fun FeatureInfoRow(
  icon: androidx.compose.ui.graphics.vector.ImageVector,
  iconTint: Color,
  title: String,
  description: String
) {
  Surface(
    color = DarkSurfaceCard,
    shape = RoundedCornerShape(14.dp),
    border = BorderStroke(1.dp, DarkBorder),
    modifier = Modifier.fillMaxWidth()
  ) {
    Row(
      modifier = Modifier.padding(12.dp)
    ) {
      Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
          .size(34.dp)
          .clip(CircleShape)
          .background(iconTint.copy(alpha = 0.15f))
      ) {
        Icon(
          imageVector = icon,
          contentDescription = null,
          tint = iconTint,
          modifier = Modifier.size(18.dp)
        )
      }

      Spacer(modifier = Modifier.width(12.dp))

      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = title,
          fontSize = 13.sp,
          fontWeight = FontWeight.Bold,
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
