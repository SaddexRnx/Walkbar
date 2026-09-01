package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.ModeComment
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.ThumbDown
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.SocialPlatform
import com.example.ui.theme.AccentGold
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurfaceElevated

@Composable
fun InstagramOverlayGuide(
  progress: Float,
  modifier: Modifier = Modifier
) {
  PlatformOverlayGuide(
    platform = SocialPlatform.INSTAGRAM_REELS,
    progress = progress,
    modifier = modifier
  )
}

@Composable
fun PlatformOverlayGuide(
  platform: SocialPlatform,
  progress: Float,
  modifier: Modifier = Modifier
) {
  Box(
    modifier = modifier.fillMaxSize()
  ) {
    // 15% black scrim so simulated chrome doesn't compete with video footage
    Box(
      modifier = Modifier
        .fillMaxSize()
        .background(Color.Black.copy(alpha = 0.15f))
    )

    // Top banner indicator
    Surface(
      color = Color(0xDD15151C),
      shape = RoundedCornerShape(20.dp),
      border = BorderStroke(0.5.dp, Color(0x44FFFFFF)),
      modifier = Modifier
        .align(Alignment.TopCenter)
        .padding(top = 10.dp)
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
      ) {
        Icon(
          imageVector = Icons.Default.Visibility,
          contentDescription = null,
          tint = AccentGold,
          modifier = Modifier.size(13.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
          text = "${platform.iconEmoji} ${platform.displayName} Guide • Reference Only",
          fontSize = 10.5.sp,
          fontWeight = FontWeight.SemiBold,
          color = Color(0xFFF3F4F6)
        )
      }
    }

    when (platform) {
      SocialPlatform.TIKTOK -> TikTokSimulatedUI(progress = progress)
      SocialPlatform.YOUTUBE_SHORTS -> YouTubeShortsSimulatedUI(progress = progress)
      SocialPlatform.INSTAGRAM_REELS -> InstagramSimulatedUI(progress = progress)
      SocialPlatform.FACEBOOK_REELS -> FacebookSimulatedUI(progress = progress)
      SocialPlatform.PHONE_SCREEN_TALL -> PhoneScreenTallSimulatedUI(progress = progress)
      SocialPlatform.CUSTOM -> CustomSimulatedUI(progress = progress)
    }
  }
}

// ─────────────────────────────────────────────────────────────
// 1. TIKTOK SIMULATED UI & ULTRA-BOTTOM SCRUBBER
// ─────────────────────────────────────────────────────────────
@Composable
private fun TikTokSimulatedUI(progress: Float) {
  val infiniteTransition = rememberInfiniteTransition(label = "discSpin")
  val discAngle by infiniteTransition.animateFloat(
    initialValue = 0f,
    targetValue = 360f,
    animationSpec = infiniteRepeatable(
      animation = tween(4000, easing = LinearEasing),
      repeatMode = RepeatMode.Restart
    ),
    label = "discRotation"
  )

  Box(modifier = Modifier.fillMaxSize()) {
    // Right Action Column (TikTok Style)
    Column(
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.spacedBy(14.dp),
      modifier = Modifier
        .align(Alignment.BottomEnd)
        .padding(end = 10.dp, bottom = 28.dp)
    ) {
      // Profile Avatar with Plus badge
      Box(
        contentAlignment = Alignment.BottomCenter,
        modifier = Modifier.size(42.dp)
      ) {
        Box(
          modifier = Modifier
            .size(38.dp)
            .clip(CircleShape)
            .background(Color(0xFF2563EB))
            .border(1.5.dp, Color.White, CircleShape)
        )
        Box(
          contentAlignment = Alignment.Center,
          modifier = Modifier
            .size(16.dp)
            .clip(CircleShape)
            .background(Color(0xFFFE2C55))
        ) {
          Icon(Icons.Default.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(12.dp))
        }
      }

      SimulatedActionIcon(Icons.Default.Favorite, "1.4M")
      SimulatedActionIcon(Icons.Default.ModeComment, "12.8K")
      SimulatedActionIcon(Icons.Default.Bookmark, "84.2K")
      SimulatedActionIcon(Icons.Default.NearMe, "29.1K")

      // Rotating TikTok Vinyl Sound Disc
      Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
          .size(36.dp)
          .rotate(discAngle)
          .clip(CircleShape)
          .background(Color(0xFF1E293B))
          .border(2.dp, Color(0xFF334155), CircleShape)
      ) {
        Box(
          modifier = Modifier
            .size(14.dp)
            .clip(CircleShape)
            .background(Color(0xFFEC4899))
        )
      }
    }

    // Left Caption & Audio (TikTok Style)
    Column(
      modifier = Modifier
        .align(Alignment.BottomStart)
        .padding(start = 12.dp, end = 72.dp, bottom = 12.dp)
    ) {
      Text(
        text = "@walkbar_creator",
        color = Color.White,
        fontSize = 13.5.sp,
        fontWeight = FontWeight.Bold
      )
      Spacer(modifier = Modifier.height(2.dp))
      Text(
        text = "This animation syncs perfectly with your video progress bar! ✨🐾 #walkbar #fyp",
        color = Color(0xFFE2E8F0),
        fontSize = 11.5.sp,
        maxLines = 2,
        overflow = TextOverflow.Ellipsis
      )
      Spacer(modifier = Modifier.height(4.dp))
      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Default.MusicNote, contentDescription = null, tint = Color.White, modifier = Modifier.size(12.dp))
        Spacer(modifier = Modifier.width(4.dp))
        Text(
          text = "Original Sound - SaddexRnx",
          color = Color.White,
          fontSize = 11.sp
        )
      }
    }

    // TIKTOK EXACT ULTRA-BOTTOM PROGRESS BAR (Touching bottom edge)
    Box(
      modifier = Modifier
        .align(Alignment.BottomCenter)
        .fillMaxWidth()
        .height(2.5.dp)
        .background(Color(0x33FFFFFF))
    ) {
      Box(
        modifier = Modifier
          .fillMaxWidth(progress.coerceIn(0f, 1f))
          .height(2.5.dp)
          .background(Color.White)
      )
    }
  }
}

// ─────────────────────────────────────────────────────────────
// 2. YOUTUBE SHORTS SIMULATED UI & RED BOTTOM SCRUBBER
// ─────────────────────────────────────────────────────────────
@Composable
private fun YouTubeShortsSimulatedUI(progress: Float) {
  Box(modifier = Modifier.fillMaxSize()) {
    // Right Action Column (Shorts Style)
    Column(
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.spacedBy(16.dp),
      modifier = Modifier
        .align(Alignment.BottomEnd)
        .padding(end = 10.dp, bottom = 32.dp)
    ) {
      SimulatedActionIcon(Icons.Default.ThumbUp, "428K")
      SimulatedActionIcon(Icons.Default.ThumbDown, "Dislike")
      SimulatedActionIcon(Icons.Default.ModeComment, "3.4K")
      SimulatedActionIcon(Icons.Default.Send, "Share")
      SimulatedActionIcon(Icons.Default.Repeat, "Remix")

      // Sound Box
      Box(
        modifier = Modifier
          .size(30.dp)
          .clip(RoundedCornerShape(6.dp))
          .background(Color(0xFF334155))
      )
    }

    // Bottom Channel & Subscribe Row
    Column(
      modifier = Modifier
        .align(Alignment.BottomStart)
        .padding(start = 12.dp, end = 72.dp, bottom = 14.dp)
    ) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
          modifier = Modifier
            .size(28.dp)
            .clip(CircleShape)
            .background(Color(0xFFDC2626))
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
          text = "@WalkbarOfficial",
          color = Color.White,
          fontSize = 13.sp,
          fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.width(8.dp))
        Surface(
          color = Color(0xFFCC0000),
          shape = RoundedCornerShape(14.dp)
        ) {
          Text(
            text = "Subscribe",
            color = Color.White,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
          )
        }
      }

      Spacer(modifier = Modifier.height(4.dp))
      Text(
        text = "Animated timeline walk companion for YouTube Shorts #Shorts",
        color = Color(0xFFF1F5F9),
        fontSize = 11.5.sp,
        maxLines = 1
      )
    }

    // YOUTUBE SHORTS EXACT RED SCRUBBER BAR
    Box(
      modifier = Modifier
        .align(Alignment.BottomCenter)
        .fillMaxWidth()
        .height(3.dp)
        .background(Color(0x44FFFFFF))
    ) {
      Box(
        modifier = Modifier
          .fillMaxWidth(progress.coerceIn(0f, 1f))
          .height(3.dp)
          .background(Color(0xFFFF0033))
      )
    }
  }
}

// ─────────────────────────────────────────────────────────────
// 3. INSTAGRAM REELS SIMULATED UI
// ─────────────────────────────────────────────────────────────
@Composable
private fun InstagramSimulatedUI(progress: Float) {
  Box(modifier = Modifier.fillMaxSize()) {
    // Right Action Rail
    Column(
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.spacedBy(16.dp),
      modifier = Modifier
        .align(Alignment.BottomEnd)
        .padding(end = 12.dp, bottom = 44.dp)
    ) {
      SimulatedActionIcon(Icons.Default.Favorite, "86.4K")
      SimulatedActionIcon(Icons.Default.ModeComment, "612")
      SimulatedActionIcon(Icons.Default.Send, "Share")
      SimulatedActionIcon(Icons.Default.Bookmark, "Save")
      SimulatedActionIcon(Icons.Default.MoreVert, "")

      // Audio Square
      Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
          .size(28.dp)
          .clip(RoundedCornerShape(6.dp))
          .background(Color(0xFF262626))
      ) {
        Icon(Icons.Default.GraphicEq, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
      }
    }

    // Bottom Caption & Profile
    Column(
      modifier = Modifier
        .align(Alignment.BottomStart)
        .padding(start = 14.dp, end = 70.dp, bottom = 22.dp)
    ) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
          modifier = Modifier
            .size(26.dp)
            .clip(CircleShape)
            .background(Brush.linearGradient(listOf(Color(0xFF8B5CF6), Color(0xFFA855F7), Color(0xFFEC4899))))
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = "walkbar_reels", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.width(6.dp))
        Text(text = "• Follow", color = Color(0xFF93C5FD), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
      }

      Spacer(modifier = Modifier.height(4.dp))
      Text(
        text = "Watch this animated character walk the timeline in real-time 🐾 #reels",
        color = Color(0xFFE5E7EB),
        fontSize = 11.5.sp,
        maxLines = 1
      )
    }

    // INSTAGRAM REELS SCRUBBER BAR
    Box(
      modifier = Modifier
        .align(Alignment.BottomCenter)
        .fillMaxWidth()
        .padding(bottom = 6.dp)
        .height(3.dp)
        .background(Color(0x44FFFFFF))
    ) {
      Box(
        modifier = Modifier
          .fillMaxWidth(progress.coerceIn(0f, 1f))
          .height(3.dp)
          .background(Color.White)
      )
    }
  }
}

// ─────────────────────────────────────────────────────────────
// 4. FACEBOOK REELS SIMULATED UI
// ─────────────────────────────────────────────────────────────
@Composable
private fun FacebookSimulatedUI(progress: Float) {
  Box(modifier = Modifier.fillMaxSize()) {
    Column(
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.spacedBy(16.dp),
      modifier = Modifier
        .align(Alignment.BottomEnd)
        .padding(end = 12.dp, bottom = 38.dp)
    ) {
      SimulatedActionIcon(Icons.Default.ThumbUp, "32K")
      SimulatedActionIcon(Icons.Default.ModeComment, "1.2K")
      SimulatedActionIcon(Icons.Default.Send, "Share")
      SimulatedActionIcon(Icons.Default.MoreVert, "")
    }

    Column(
      modifier = Modifier
        .align(Alignment.BottomStart)
        .padding(start = 14.dp, end = 70.dp, bottom = 18.dp)
    ) {
      Text(text = "Facebook Creator", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
      Spacer(modifier = Modifier.height(2.dp))
      Text(text = "Animated timeline walk companion on Facebook Reels", color = Color(0xFFE5E7EB), fontSize = 11.5.sp, maxLines = 1)
    }

    // FACEBOOK REELS SCRUBBER BAR
    Box(
      modifier = Modifier
        .align(Alignment.BottomCenter)
        .fillMaxWidth()
        .padding(bottom = 4.dp)
        .height(3.dp)
        .background(Color(0x44FFFFFF))
    ) {
      Box(
        modifier = Modifier
          .fillMaxWidth(progress.coerceIn(0f, 1f))
          .height(3.dp)
          .background(Color(0xFF1877F2))
      )
    }
  }
}

// ─────────────────────────────────────────────────────────────
// 5. 6.19" TALL PHONE SCREEN (19.5:9) SIMULATED UI
// ─────────────────────────────────────────────────────────────
@Composable
private fun PhoneScreenTallSimulatedUI(progress: Float) {
  Box(modifier = Modifier.fillMaxSize()) {
    // Phone Bezel Frame indication
    Box(
      modifier = Modifier
        .fillMaxSize()
        .border(1.5.dp, Color(0x6638BDF8), RoundedCornerShape(16.dp))
    )

    // Ultra Bottom Scrubber
    Box(
      modifier = Modifier
        .align(Alignment.BottomCenter)
        .fillMaxWidth()
        .height(2.5.dp)
        .background(Color(0x44FFFFFF))
    ) {
      Box(
        modifier = Modifier
          .fillMaxWidth(progress.coerceIn(0f, 1f))
          .height(2.5.dp)
          .background(Color(0xFF38BDF8))
      )
    }

    // Top pill badge
    Surface(
      color = Color(0xCC15151C),
      shape = RoundedCornerShape(10.dp),
      border = BorderStroke(1.dp, DarkBorder),
      modifier = Modifier
        .align(Alignment.BottomCenter)
        .padding(bottom = 12.dp)
    ) {
      Text(
        text = "19.5:9 Edge-to-Edge Fill • Zero Black Bars on Tall Screens",
        fontSize = 9.5.sp,
        fontWeight = FontWeight.Medium,
        color = Color(0xFF38BDF8),
        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
      )
    }
  }
}

// ─────────────────────────────────────────────────────────────
// 6. CUSTOM SIMULATED UI
// ─────────────────────────────────────────────────────────────
@Composable
private fun CustomSimulatedUI(progress: Float) {
  Box(modifier = Modifier.fillMaxSize()) {
    Box(
      modifier = Modifier
        .align(Alignment.BottomCenter)
        .fillMaxWidth()
        .height(3.dp)
        .background(Color(0x33FFFFFF))
    ) {
      Box(
        modifier = Modifier
          .fillMaxWidth(progress.coerceIn(0f, 1f))
          .height(3.dp)
          .background(Color(0xFF8B5CF6))
      )
    }
  }
}

@Composable
private fun SimulatedActionIcon(
  icon: ImageVector,
  label: String
) {
  Column(
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    Icon(
      imageVector = icon,
      contentDescription = null,
      tint = Color.White,
      modifier = Modifier.size(24.dp)
    )
    if (label.isNotEmpty()) {
      Spacer(modifier = Modifier.height(2.dp))
      Text(
        text = label,
        color = Color.White,
        fontSize = 9.5.sp,
        fontWeight = FontWeight.Medium
      )
    }
  }
}
