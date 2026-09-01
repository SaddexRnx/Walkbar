package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
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
import com.example.ui.theme.AccentGold
import com.example.ui.theme.AccentPrimary
import com.example.ui.theme.AccentPrimaryLight
import com.example.ui.theme.AccentPrimaryMuted
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
  modifier: Modifier = Modifier,
  compact: Boolean = false
) {
  val cardScale by animateFloatAsState(
    targetValue = if (isSelected) 1.05f else 1.0f,
    animationSpec = spring(dampingRatio = 0.7f, stiffness = 400f),
    label = "cardScale"
  )

  val borderColor by animateColorAsState(
    targetValue = if (isSelected) AccentPrimaryLight.copy(alpha = 0.8f) else DarkBorder,
    animationSpec = tween(200, easing = FastOutSlowInEasing),
    label = "borderColor"
  )

  val backgroundColor = if (isSelected) AccentPrimaryMuted else DarkSurfaceCard

  // Continuous animation cycle for real-time walk preview
  val infiniteTransition = rememberInfiniteTransition(label = "charAnimLoop")
  val animTimeMs by infiniteTransition.animateFloat(
    initialValue = 0f,
    targetValue = 2400f,
    animationSpec = infiniteRepeatable(
      animation = tween(2400, easing = LinearEasing),
      repeatMode = RepeatMode.Restart
    ),
    label = "animTime"
  )

  val boxSize = if (compact) 48.dp else 58.dp
  val canvasSize = if (compact) 38.dp else 46.dp
  val colWidth = if (compact) 60.dp else 72.dp

  Column(
    horizontalAlignment = Alignment.CenterHorizontally,
    modifier = modifier
      .width(colWidth)
      .defaultMinSize(minHeight = 48.dp)
      .scale(cardScale)
      .alpha(if (isSelected) 1f else 0.72f)
      .clickable(
        interactionSource = remember { MutableInteractionSource() },
        indication = ripple(bounded = false, radius = 28.dp),
        onClick = onClick
      )
      .testTag("character_card_${character.id}")
  ) {
    Box(
      contentAlignment = Alignment.Center,
      modifier = Modifier
        .size(boxSize)
        .clip(RoundedCornerShape(14.dp))
        .background(backgroundColor)
        .border(
          width = if (isSelected) 1.5.dp else 1.dp,
          color = borderColor,
          shape = RoundedCornerShape(14.dp)
        )
    ) {
      Canvas(modifier = Modifier.size(canvasSize)) {
        val w = size.width
        val h = size.height
        val charSize = h * 0.74f
        val stepMs = if (behavior.stepDurationMs > 0L) behavior.stepDurationMs else 480L
        val phase = ((animTimeMs.toLong() % stepMs).toFloat() / stepMs.toFloat())

        drawContext.canvas.nativeCanvas.let { canvas ->
          CharacterRenderer.drawCharacter(
            canvas = canvas,
            character = character,
            behavior = behavior,
            centerX = w * 0.5f,
            bottomY = h * 0.86f,
            size = charSize,
            phase = phase,
            facingRight = true,
            currentTimeMs = animTimeMs.toLong()
          )
        }
      }

      // Small category icon badge in top corner
      Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
          .align(Alignment.TopEnd)
          .offset(x = (-2).dp, y = 2.dp)
          .size(if (compact) 14.dp else 16.dp)
          .clip(CircleShape)
          .background(Color(0xDD1B1B24))
      ) {
        Text(
          text = character.category.iconEmoji,
          fontSize = if (compact) 8.sp else 9.sp,
          textAlign = TextAlign.Center
        )
      }
    }

    Spacer(modifier = Modifier.height(4.dp))

    Text(
      text = character.name,
      fontSize = if (compact) 10.sp else 11.sp,
      fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
      color = if (isSelected) AccentPrimaryLight else TextSecondary,
      maxLines = 1,
      overflow = TextOverflow.Ellipsis,
      textAlign = TextAlign.Center
    )
  }
}
