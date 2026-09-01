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
import androidx.compose.ui.graphics.Brush
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
import com.example.ui.theme.TextSecondary

/**
 * 'Sleek Interface' Character Picker.
 * Displays a category filter row, a scrollable carousel of character cards,
 * and an immediate live preview displaying the character's walk-cycle state.
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
    // 1. Live Interactive Walk Preview Bar
    LiveWalkPreviewBanner(
      character = currentCharacter,
      behavior = behavior,
      currentTimeMs = animTimeMs.toLong()
    )

    Spacer(modifier = Modifier.height(10.dp))

    // 2. Category Filter Pills
    LazyRow(
      horizontalArrangement = Arrangement.spacedBy(6.dp),
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 2.dp, vertical = 2.dp)
    ) {
      items(ObjectCategory.entries.toTypedArray(), key = { it.name }) { cat ->
        val isCatSelected = selectedCategory == cat
        FilterChip(
          selected = isCatSelected,
          onClick = { selectedCategory = cat },
          shape = RoundedCornerShape(10.dp),
          label = {
            Text(
              text = "${cat.iconEmoji} ${cat.displayName}",
              fontSize = 11.5.sp,
              fontWeight = if (isCatSelected) FontWeight.Bold else FontWeight.Normal
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
            selected = isCatSelected
          ),
          modifier = Modifier.testTag("category_chip_${cat.name}")
        )
      }
    }

    Spacer(modifier = Modifier.height(8.dp))

    // 3. Scrollable Row of Character Icons
    LazyRow(
      horizontalArrangement = Arrangement.spacedBy(10.dp),
      modifier = Modifier
        .fillMaxWidth()
        .padding(vertical = 4.dp)
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
 * Top active preview banner demonstrating the selected character's immediate walk-cycle.
 */
@Composable
private fun LiveWalkPreviewBanner(
  character: CharacterModel,
  behavior: AnimationBehavior,
  currentTimeMs: Long,
  modifier: Modifier = Modifier
) {
  Box(
    modifier = modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(16.dp))
      .background(
        Brush.horizontalGradient(
          colors = listOf(
            Color(0xFF1E1B4B),
            Color(0xFF0F172A),
            Color(0xFF1E293B)
          )
        )
      )
      .border(1.dp, Color(0x446366F1), RoundedCornerShape(16.dp))
      .padding(horizontal = 14.dp, vertical = 10.dp)
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      modifier = Modifier.fillMaxWidth()
    ) {
      // Live Animated Preview Window
      Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
          .size(54.dp)
          .clip(RoundedCornerShape(12.dp))
          .background(Color(0x33000000))
          .border(1.dp, Color(0x336366F1), RoundedCornerShape(12.dp))
      ) {
        Canvas(modifier = Modifier.size(46.dp)) {
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
      }

      Spacer(modifier = Modifier.width(12.dp))

      // Character Info & Attribution
      Column(modifier = Modifier.weight(1f)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Text(
            text = character.name,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
          )
          Spacer(modifier = Modifier.width(6.dp))
          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(6.dp))
              .background(AccentIndigoMuted)
              .padding(horizontal = 5.dp, vertical = 1.dp)
          ) {
            Text(
              text = character.category.displayName,
              fontSize = 9.5.sp,
              fontWeight = FontWeight.SemiBold,
              color = AccentIndigoLight
            )
          }
        }

        Spacer(modifier = Modifier.height(2.dp))

        Text(
          text = character.description,
          fontSize = 11.sp,
          color = TextSecondary,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis
        )

        Spacer(modifier = Modifier.height(3.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
          Text(
            text = "By ${character.creator}",
            fontSize = 10.sp,
            color = TextMuted
          )
          Text(
            text = " • ${character.license}",
            fontSize = 9.5.sp,
            color = TextMuted
          )
        }
      }
    }
  }
}

/**
 * Individual sleek character item in the carousel with live animated canvas preview.
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

  val borderColor by animateColorAsState(
    targetValue = if (isSelected) AccentIndigo else Color.Transparent,
    animationSpec = tween(200, easing = FastOutSlowInEasing),
    label = "borderColor"
  )

  val backgroundColor = if (isSelected) AccentIndigoMuted else DarkSurfaceCard

  Column(
    horizontalAlignment = Alignment.CenterHorizontally,
    modifier = modifier
      .width(70.dp)
      .scale(cardScale)
      .alpha(if (isSelected) 1f else 0.68f)
      .clickable(onClick = onClick)
      .testTag("sleek_character_card_${character.id}")
  ) {
    Box(
      contentAlignment = Alignment.Center,
      modifier = Modifier
        .size(56.dp)
        .clip(RoundedCornerShape(16.dp))
        .background(backgroundColor)
        .border(
          width = if (isSelected) 2.dp else 1.dp,
          color = if (isSelected) AccentIndigo else DarkBorder,
          shape = RoundedCornerShape(16.dp)
        )
    ) {
      Canvas(modifier = Modifier.size(44.dp)) {
        val w = size.width
        val h = size.height
        val charSize = h * 0.72f
        val phase = ((currentTimeMs % behavior.stepDurationMs).toFloat() / behavior.stepDurationMs.toFloat())

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
          .size(15.dp)
          .clip(CircleShape)
          .background(Color(0xCC1E1B4B))
      ) {
        Text(
          text = character.category.iconEmoji,
          fontSize = 8.5.sp,
          textAlign = TextAlign.Center
        )
      }
    }

    Spacer(modifier = Modifier.height(4.dp))

    Text(
      text = character.name,
      fontSize = 10.sp,
      fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
      color = if (isSelected) AccentIndigoLight else TextSecondary,
      maxLines = 1,
      overflow = TextOverflow.Ellipsis,
      textAlign = TextAlign.Center
    )
  }
}
