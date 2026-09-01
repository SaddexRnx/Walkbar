package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CropFree
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.AccentGold
import com.example.ui.theme.AccentGreen
import com.example.ui.theme.AccentRed
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

/**
 * Visual Safe Zone guide showing the 10-15% crop-risk areas at the top and bottom
 * of vertical videos when uploaded to destination apps (TikTok, Instagram Reels, YouTube Shorts).
 */
@Composable
fun SafeZoneOverlayGuide(
  verticalOffsetPercent: Float,
  modifier: Modifier = Modifier
) {
  val isRisk = verticalOffsetPercent < 0.012f

  Box(
    modifier = modifier.fillMaxSize()
  ) {
    // 15% dark ambient scrim
    Box(
      modifier = Modifier
        .fillMaxSize()
        .background(Color.Black.copy(alpha = 0.20f))
    )

    Column(
      modifier = Modifier.fillMaxSize()
    ) {
      // ─── 1. TOP CROP RISK AREA (11%) ───
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .weight(0.11f)
          .background(
            Brush.verticalGradient(
              colors = listOf(
                Color(0x88D97706),
                Color(0x44D97706),
                Color(0x22D97706)
              )
            )
          )
          .border(
            border = BorderStroke(1.dp, Color(0x99F59E0B)),
            shape = RoundedCornerShape(0.dp)
          ),
        contentAlignment = Alignment.Center
      ) {
        Surface(
          color = Color(0xDD0D0D12),
          shape = RoundedCornerShape(8.dp),
          border = BorderStroke(1.dp, Color(0x88F59E0B))
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
          ) {
            Icon(
              imageVector = Icons.Default.WarningAmber,
              contentDescription = null,
              tint = AccentGold,
              modifier = Modifier.size(13.dp)
            )
            Spacer(modifier = Modifier.width(5.dp))
            Text(
              text = "TOP CROP ZONE (11%) • App Header / Camera Cutout",
              fontSize = 9.5.sp,
              fontWeight = FontWeight.Bold,
              color = AccentGold
            )
          }
        }
      }

      // ─── 2. CENTRAL SAFE ACTION REGION (76%) ───
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .weight(0.76f)
          .padding(horizontal = 8.dp)
          .border(
            border = BorderStroke(1.5.dp, Color(0x8810B981)),
            shape = RoundedCornerShape(12.dp)
          ),
        contentAlignment = Alignment.Center
      ) {
        Surface(
          color = Color(0xD00D0D12),
          shape = RoundedCornerShape(20.dp),
          border = BorderStroke(1.dp, Color(0x6610B981)),
          modifier = Modifier.padding(12.dp)
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
          ) {
            Icon(
              imageVector = Icons.Default.Security,
              contentDescription = null,
              tint = AccentGreen,
              modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Column {
              Text(
                text = "GUARANTEED SAFE ACTION AREA",
                fontSize = 10.5.sp,
                fontWeight = FontWeight.Bold,
                color = AccentGreen
              )
              Text(
                text = "Visible across all phone screens & aspect ratios",
                fontSize = 9.sp,
                color = TextSecondary
              )
            }
          }
        }
      }

      // ─── 3. BOTTOM CROP RISK AREA (13%) ───
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .weight(0.13f)
          .background(
            Brush.verticalGradient(
              colors = listOf(
                Color(0x22EF4444),
                Color(0x55EF4444),
                Color(0x99EF4444)
              )
            )
          )
          .border(
            border = BorderStroke(1.dp, Color(0x99EF4444)),
            shape = RoundedCornerShape(0.dp)
          ),
        contentAlignment = Alignment.Center
      ) {
        Column(
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.Center,
          modifier = Modifier.padding(horizontal = 8.dp)
        ) {
          Surface(
            color = Color(0xEE0D0D12),
            shape = RoundedCornerShape(8.dp),
            border = BorderStroke(1.dp, if (isRisk) Color(0xAAEF4444) else Color(0x8810B981))
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
              Icon(
                imageVector = if (isRisk) Icons.Default.WarningAmber else Icons.Default.CheckCircle,
                contentDescription = null,
                tint = if (isRisk) AccentRed else AccentGreen,
                modifier = Modifier.size(13.dp)
              )
              Spacer(modifier = Modifier.width(5.dp))
              Text(
                text = if (isRisk) {
                  "BOTTOM CROP ZONE (13%) • Character at ${(verticalOffsetPercent * 100).toInt()}% is in Risk Band"
                } else {
                  "BOTTOM CROP ZONE (13%) • Character at ${(verticalOffsetPercent * 100).toInt()}% is Safe"
                },
                fontSize = 9.5.sp,
                fontWeight = FontWeight.Bold,
                color = if (isRisk) AccentRed else AccentGreen
              )
            }
          }
        }
      }
    }

    // Top Right Safe Zone Badge
    Surface(
      color = Color(0xEE15151C),
      shape = RoundedCornerShape(12.dp),
      border = BorderStroke(1.dp, AccentCyan.copy(alpha = 0.5f)),
      modifier = Modifier
        .align(Alignment.TopEnd)
        .padding(top = 10.dp, end = 10.dp)
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
      ) {
        Icon(
          imageVector = Icons.Default.CropFree,
          contentDescription = null,
          tint = AccentCyan,
          modifier = Modifier.size(13.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
          text = "Safe Zone Active",
          fontSize = 10.sp,
          fontWeight = FontWeight.Bold,
          color = AccentCyan
        )
      }
    }
  }
}
