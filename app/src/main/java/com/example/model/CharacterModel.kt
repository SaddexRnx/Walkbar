package com.example.model

enum class ObjectCategory(val displayName: String, val iconEmoji: String) {
  ALL("All", "✨"),
  THREE_D("3D Objects", "🪐"),
  PIXEL_ART("Pixel Art", "👾"),
  ANIMALS("Animals", "🐾"),
  VEHICLES("Vehicles & Sci-Fi", "🚀"),
  LIFESTYLE("Lifestyle & Fun", "🧋")
}

enum class RenderMode {
  SPRITE_3D_PIXEL,
  VECTOR_3D,
  VECTOR_CHARACTER
}

enum class CharacterType {
  // Animals & Companions
  PUPPY,
  CAT,
  BUNNY,
  FOX,
  PENGUIN,
  FROG,
  DUCK,

  // Real 3D Objects
  THREE_D_DIAMOND,
  THREE_D_GOLD_COIN,
  THREE_D_CUBE,
  THREE_D_SATURN,
  THREE_D_UFO,
  THREE_D_STAR,
  THREE_D_ROCKET,

  // Pixel Art & 3D-Styled Retro Sprites
  ROBOT,
  ASTRONAUT,
  NINJA,
  PIXEL_DINO,
  PIXEL_HEART,
  PIXEL_GHOST,
  PIXEL_POTION,
  PIXEL_CAT,
  PIXEL_COIN,

  // Vehicles & Sci-Fi
  CYBER_CAR,
  HOVER_DRONE,

  // Fun & Lifestyle Objects
  BOBA_TEA,
  VINYL_RECORD
}

data class CharacterAnimationInfo(
  val frameCount: Int = 4,
  val stepDurationMs: Long = 400L,
  val renderMode: RenderMode = RenderMode.SPRITE_3D_PIXEL,
  val hasBlink: Boolean = true,
  val hasShadow: Boolean = true,
  val customFrameAssetPaths: List<String> = emptyList()
)

data class CharacterModel(
  val id: String,
  val type: CharacterType,
  val name: String,
  val emoji: String,
  val description: String,
  val category: ObjectCategory = ObjectCategory.ANIMALS,
  val defaultScale: Float = 0.045f, // ~4.5% of video height for crisp small walking look
  val recommendedVerticalOffsetPercent: Float = 0.038f, // 3.8% above bottom sits right on IG progress line
  val license: String = "CC0 / Walkbar Original",
  val creator: String = "Walkbar Studio",
  val primaryColorHex: Long = 0xFFFF9F43,
  val secondaryColorHex: Long = 0xFFFFFFFF,
  val tags: List<String> = emptyList(),
  val animation: CharacterAnimationInfo = CharacterAnimationInfo()
)
