package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.characters.CharacterRegistry
import com.example.model.AnimationBehavior
import com.example.model.ObjectCategory
import com.example.ui.theme.AccentPrimary
import com.example.ui.theme.AccentPrimaryLight
import com.example.ui.theme.AccentPrimaryMuted
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurfaceCard
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
        horizontalArrangement = Arrangement.spacedBy(5.dp),
        modifier = Modifier.weight(1f)
      ) {
        items(ObjectCategory.entries.toTypedArray(), key = { it.name }) { cat ->
          val isCatSelected = selectedCategory == cat
          Surface(
            color = if (isCatSelected) AccentPrimary else DarkSurfaceCard,
            shape = RoundedCornerShape(10.dp),
            border = BorderStroke(
              1.dp,
              if (isCatSelected) AccentPrimaryLight else DarkBorder
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
              modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
            )
          }
        }
      }

      Spacer(modifier = Modifier.width(6.dp))

      // Active character pill
      Surface(
        color = AccentPrimaryMuted,
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(0.5.dp, AccentPrimaryLight)
      ) {
        Text(
          text = currentCharacter.name,
          fontSize = 11.sp,
          fontWeight = FontWeight.Bold,
          color = AccentPrimaryLight,
          modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
          maxLines = 1,
          overflow = TextOverflow.Ellipsis
        )
      }
    }

    Spacer(modifier = Modifier.height(8.dp))

    // 2. Compact Scrollable Row of Character Icons
    LazyRow(
      horizontalArrangement = Arrangement.spacedBy(8.dp),
      modifier = Modifier
        .fillMaxWidth()
        .padding(vertical = 2.dp)
    ) {
      items(filteredCharacters, key = { it.id }) { char ->
        CharacterAvatarCard(
          character = char,
          isSelected = char.id == selectedCharacterId,
          behavior = behavior,
          compact = true,
          onClick = { onCharacterSelected(char.id) }
        )
      }
    }
  }
}
