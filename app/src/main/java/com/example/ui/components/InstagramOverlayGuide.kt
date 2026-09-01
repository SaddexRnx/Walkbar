package com.example.ui.components

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.ModeComment
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Visibility
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
import com.example.ui.theme.DarkSurfaceCard
import com.example.ui.theme.InstagramBarWhite

@Composable
fun InstagramOverlayGuide(
  progress: Float,
  modifier: Modifier = Modifier
) {
  Box(
    modifier = modifier
      .fillMaxSize()
  ) {
    // Subtle top banner indicator
    Surface(
      color = Color(0xDD12151C),
      shape = RoundedCornerShape(20.dp),
      modifier = Modifier
        .align(Alignment.TopCenter)
        .padding(top = 12.dp)
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
      ) {
        Icon(
          imageVector = Icons.Default.Visibility,
          contentDescription = null,
          tint = Color(0xFFFF9F1C),
          modifier = Modifier.size(14.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
          text = "Reel Preview Guide • NOT exported",
          fontSize = 11.sp,
          fontWeight = FontWeight.SemiBold,
          color = Color(0xFFF3F4F6)
        )
      }
    }

    // Right Action Rail (Simulated Instagram UI)
    Column(
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.spacedBy(16.dp),
      modifier = Modifier
        .align(Alignment.BottomEnd)
        .padding(end = 12.dp, bottom = 48.dp)
    ) {
      SimulatedActionIcon(Icons.Default.Favorite, "42.8K")
      SimulatedActionIcon(Icons.Default.ModeComment, "318")
      SimulatedActionIcon(Icons.Default.Send, "Share")
      SimulatedActionIcon(Icons.Default.Bookmark, "Save")
      SimulatedActionIcon(Icons.Default.MoreVert, "")

      // Music Album Disc
      Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
          .size(30.dp)
          .clip(CircleShape)
          .background(Color(0xFF262626))
      ) {
        Icon(
          imageVector = Icons.Default.GraphicEq,
          contentDescription = null,
          tint = Color.White,
          modifier = Modifier.size(16.dp)
        )
      }
    }

    // Bottom Caption & Profile simulation
    Column(
      modifier = Modifier
        .align(Alignment.BottomStart)
        .padding(start = 14.dp, end = 70.dp, bottom = 22.dp)
    ) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
          modifier = Modifier
            .size(24.dp)
            .clip(CircleShape)
            .background(
              Brush.linearGradient(listOf(Color(0xFFF58529), Color(0xFFDD2A7B), Color(0xFF8134AF)))
            )
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
          text = "walkbar_user",
          color = Color.White,
          fontSize = 13.sp,
          fontWeight = FontWeight.Bold
        )
      }

      Spacer(modifier = Modifier.height(4.dp))
      Text(
        text = "Watch this tiny buddy walk on Instagram's progress bar 🐾 #walkbar",
        color = Color(0xFFE5E7EB),
        fontSize = 12.sp,
        maxLines = 1
      )
    }

    // SIMULATED NATIVE INSTAGRAM PROGRESS BAR
    // Placed at the very bottom edge where Instagram draws its native scrubber line
    Box(
      modifier = Modifier
        .align(Alignment.BottomCenter)
        .fillMaxWidth()
        .height(3.dp)
        .background(Color(0x55FFFFFF))
    ) {
      Box(
        modifier = Modifier
          .fillMaxWidth(progress.coerceIn(0f, 1f))
          .height(3.dp)
          .background(InstagramBarWhite)
      )
    }
  }
}

@Composable
private fun SimulatedActionIcon(
  icon: androidx.compose.ui.graphics.vector.ImageVector,
  label: String
) {
  Column(
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    Icon(
      imageVector = icon,
      contentDescription = null,
      tint = Color.White,
      modifier = Modifier.size(26.dp)
    )
    if (label.isNotEmpty()) {
      Spacer(modifier = Modifier.height(2.dp))
      Text(
        text = label,
        color = Color.White,
        fontSize = 10.sp,
        fontWeight = FontWeight.Medium
      )
    }
  }
}
