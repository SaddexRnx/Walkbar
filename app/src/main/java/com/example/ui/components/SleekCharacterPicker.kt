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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.example.characters.CharacterRegistry
import com.example.characters.CharacterRenderer
import com.example.model.AnimationBehavior
import com.example.model.CharacterModel
import com.example.model.ObjectCategory
import com.example.ui.theme.AccentIndigo
import com.example.ui.theme.AccentIndigoLight
import com.example.ui.theme.AccentIndigoMuted
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurfaceCard
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

/**
 * Compact, sleek Character Picker designed specifically for the bottom controls sheet.
 * Takes minimal vertical height so the video viewport above stays large and prominent.
 */
@Composable
fun SleekCharacterPicker(
  selectedCharacterId: String,
  behavior: AnimationBehavior,
  onCharacterSelected: (String) -> Unit,
  modifier: Modifier = Modifier
) {
  var selectedCategory by remember { mutableStateOf(ObjectCategory.ALL) }
  val currentCharacter = remember(selectedCharacterId) {
    CharacterRegistry.getById(selectedCharacterId)
  }

  val filteredCharacters = remember(selectedCategory) {
    CharacterRegistry.getByCategory(selectedCategory)
  }

  // Continuous animation cycle for real-time walk preview
  val infiniteTransition = rememberInfiniteTransition(label = "sleekWalkLoop")
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
    modifier = modifier
      .fillMaxWidth()
      .testTag("sleek_character_picker")
  ) {
    // 1. Compact Category Filter Row + Selected Character Badge
    Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween,
      modifier = Modifier.fillMaxWidth()
    ) {
      LazyRow(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier.weight(1f)
      ) {
        items(ObjectCategory.entries.toTypedArray(), key = { it.name }) { cat ->
          val isCatSelected = selectedCategory == cat
          Surface(
            color = if (isCatSelected) AccentIndigo else DarkSurfaceCard,
            shape = RoundedCornerShape(8.dp),
            border = androidx.compose.foundation.BorderStroke(
              1.dp,
              if (isCatSelected) AccentIndigoLight else DarkBorder
            ),
            modifier = Modifier
              .clickable { selectedCategory = cat }
              .testTag("category_chip_${cat.name}")
          ) {
            Text(
              text = "${cat.iconEmoji} ${cat.displayName}",
              fontSize = 11.sp,
              fontWeight = if (isCatSelected) FontWeight.Bold else FontWeight.Medium,
              color = if (isCatSelected) Color.White else TextSecondary,
              modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            )
          }
        }
      }

      Spacer(modifier = Modifier.width(6.dp))

      // Active character pill
      Surface(
        color = AccentIndigoMuted,
        shape = RoundedCornerShape(8.dp),
        border = androidx.compose.foundation.BorderStroke(0.5.dp, AccentIndigoLight)
      ) {
        Text(
          text = currentCharacter.name,
          fontSize = 10.5.sp,
          fontWeight = FontWeight.Bold,
          color = AccentIndigoLight,
          modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
          maxLines = 1,
          overflow = TextOverflow.Ellipsis
        )
      }
    }

    Spacer(modifier = Modifier.height(6.dp))

    // 2. Compact Scrollable Row of Character Icons
    LazyRow(
      horizontalArrangement = Arrangement.spacedBy(8.dp),
      modifier = Modifier
        .fillMaxWidth()
        .padding(vertical = 2.dp)
    ) {
      items(filteredCharacters, key = { it.id }) { char ->
        val isSelected = char.id == selectedCharacterId
        SleekCharacterCard(
          character = char,
          isSelected = isSelected,
          behavior = behavior,
          currentTimeMs = animTimeMs.toLong(),
          onClick = { onCharacterSelected(char.id) }
        )
      }
    }
  }
}

/**
 * Individual compact character card with live animated walk sprite.
 */
@Composable
fun SleekCharacterCard(
  character: CharacterModel,
  isSelected: Boolean,
  behavior: AnimationBehavior,
  currentTimeMs: Long,
  onClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  val cardScale by animateFloatAsState(
    targetValue = if (isSelected) 1.05f else 1.0f,
    animationSpec = spring(dampingRatio = 0.7f, stiffness = 400f),
    label = "cardScale"
  )

  val backgroundColor = if (isSelected) AccentIndigoMuted else DarkSurfaceCard

  Column(
    horizontalAlignment = Alignment.CenterHorizontally,
    modifier = modifier
      .width(58.dp)
      .scale(cardScale)
      .alpha(if (isSelected) 1f else 0.72f)
      .clickable(onClick = onClick)
      .testTag("sleek_character_card_${character.id}")
  ) {
    Box(
      contentAlignment = Alignment.Center,
      modifier = Modifier
        .size(46.dp)
        .clip(RoundedCornerShape(12.dp))
        .background(backgroundColor)
        .border(
          width = if (isSelected) 1.5.dp else 1.dp,
          color = if (isSelected) AccentIndigo else DarkBorder,
          shape = RoundedCornerShape(12.dp)
        )
    ) {
      Canvas(modifier = Modifier.size(36.dp)) {
        val w = size.width
        val h = size.height
        val charSize = h * 0.76f
        val stepMs = if (behavior.stepDurationMs > 0L) behavior.stepDurationMs else 480L
        val phase = ((currentTimeMs % stepMs).toFloat() / stepMs.toFloat())

        drawContext.canvas.nativeCanvas.let { canvas ->
          CharacterRenderer.drawCharacter(
            canvas = canvas,
            character = character,
            behavior = behavior,
            centerX = w * 0.5f,
            bottomY = h * 0.88f,
            size = charSize,
            phase = phase,
            facingRight = true,
            currentTimeMs = currentTimeMs
          )
        }
      }

      // Small category icon badge
      Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
          .align(Alignment.TopEnd)
          .offset(x = (-2).dp, y = 2.dp)
          .size(13.dp)
          .clip(CircleShape)
          .background(Color(0xCC1E1B4B))
      ) {
        Text(
          text = character.category.iconEmoji,
          fontSize = 7.5.sp,
          textAlign = TextAlign.Center
        )
      }
    }

    Spacer(modifier = Modifier.height(2.dp))

    Text(
      text = character.name,
      fontSize = 9.5.sp,
      fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
      color = if (isSelected) AccentIndigoLight else TextSecondary,
      maxLines = 1,
      overflow = TextOverflow.Ellipsis,
      textAlign = TextAlign.Center
    )
  }
}
