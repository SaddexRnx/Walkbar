package com.example.model

enum class CharacterSizePreset(val label: String, val scaleFactor: Float) {
  TINY("Tiny", 0.030f),
  SMALL("Small", 0.045f),
  MEDIUM("Medium", 0.065f),
  LARGE("Large", 0.090f)
}

data class CharacterOverlayConfig(
  val characterId: String = "puppy_01",
  val behavior: AnimationBehavior = AnimationBehavior.WALK,
  val sizePreset: CharacterSizePreset = CharacterSizePreset.SMALL,
  val customScalePercent: Float = 0.045f,
  val verticalOffsetPercent: Float = 0.038f, // 0.00 to 0.20 (0% to 20% from bottom)
  val startXPercent: Float = 0.02f,          // 0.00 to 1.00
  val endXPercent: Float = 0.98f,            // 0.00 to 1.00
  val facingRight: Boolean = true,
  val reverseDirection: Boolean = false,
  val showInstagramPreviewGuide: Boolean = true
) {
  // Effective start and end considering direction
  val effectiveStartX: Float
    get() = if (reverseDirection) endXPercent else startXPercent

  val effectiveEndX: Float
    get() = if (reverseDirection) startXPercent else endXPercent

  val effectiveFacingRight: Boolean
    get() = if (reverseDirection) !facingRight else facingRight
}
