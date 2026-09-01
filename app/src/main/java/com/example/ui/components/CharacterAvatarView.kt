package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.characters.CharacterRenderer
import com.example.model.AnimationBehavior
import com.example.model.CharacterModel
import com.example.model.ObjectCategory
import com.example.ui.theme.AccentIndigo
import com.example.ui.theme.AccentIndigoLight
import com.example.ui.theme.AccentIndigoMuted
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurfaceCard
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun CharacterAvatarCard(
  character: CharacterModel,
  isSelected: Boolean,
  behavior: AnimationBehavior,
  onClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  val borderColor by animateColorAsState(
    targetValue = if (isSelected) AccentIndigo else Color.Transparent,
    animationSpec = tween(200, easing = FastOutSlowInEasing),
    label = "borderColor"
  )

  val backgroundColor = if (isSelected) AccentIndigoMuted else DarkSurfaceCard

  // Continuous animation cycle for smooth preview
  val infiniteTransition = rememberInfiniteTransition(label = "charPreview")
  val animTimeMs by infiniteTransition.animateFloat(
    initialValue = 0f,
    targetValue = 2400f,
    animationSpec = infiniteRepeatable(
      animation = tween(2400, easing = LinearEasing),
      repeatMode = RepeatMode.Restart
    ),
    label = "animTime"
  )

  Column(
    horizontalAlignment = Alignment.CenterHorizontally,
    modifier = modifier
      .width(72.dp)
      .alpha(if (isSelected) 1f else 0.65f)
      .clickable(onClick = onClick)
      .testTag("character_card_${character.id}")
  ) {
    Box(
      contentAlignment = Alignment.Center,
      modifier = Modifier
        .size(58.dp)
        .clip(RoundedCornerShape(16.dp))
        .background(backgroundColor)
        .border(if (isSelected) 2.dp else 1.dp, if (isSelected) AccentIndigo else DarkBorder, RoundedCornerShape(16.dp))
    ) {
      Canvas(modifier = Modifier.size(46.dp)) {
        val w = size.width
        val h = size.height
        val charSize = h * 0.72f
        val stepMs = if (behavior.stepDurationMs > 0L) behavior.stepDurationMs else 480L
        val phase = ((animTimeMs.toLong() % stepMs).toFloat() / stepMs.toFloat())

        drawContext.canvas.nativeCanvas.let { canvas ->
          CharacterRenderer.drawCharacter(
            canvas = canvas,
            character = character,
            behavior = behavior,
            centerX = w * 0.5f,
            bottomY = h * 0.85f,
            size = charSize,
            phase = phase,
            facingRight = true,
            currentTimeMs = animTimeMs.toLong()
          )
        }
      }

      // Small category icon badge in corner
      Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
          .align(Alignment.TopEnd)
          .offset(x = (-2).dp, y = 2.dp)
          .size(16.dp)
          .clip(CircleShape)
          .background(Color(0xCC1E1B4B))
      ) {
        Text(
          text = character.category.iconEmoji,
          fontSize = 9.sp,
          textAlign = TextAlign.Center
        )
      }
    }

    Spacer(modifier = Modifier.height(5.dp))

    Text(
      text = character.name,
      fontSize = 10.5.sp,
      fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
      color = if (isSelected) AccentIndigoLight else TextSecondary,
      maxLines = 1,
      overflow = TextOverflow.Ellipsis,
      textAlign = TextAlign.Center
    )
  }
}
