package com.example.model

enum class CharacterSizePreset(val label: String, val scaleFactor: Float) {
  TINY("Tiny", 0.030f),
  SMALL("Small", 0.045f),
  MEDIUM("Medium", 0.065f),
  LARGE("Large", 0.090f)
}

enum class ExportFpsOption(val displayName: String, val targetFps: Float, val subtitle: String) {
  AUTO("Auto (Native)", 0f, "Matches source video framerate"),
  FPS_60("60 FPS Ultra", 60f, "Maximum 3D & stride smoothness"),
  FPS_30("30 FPS Standard", 30f, "Fast export, standard smoothness"),
  FPS_MAX("Max Phone FPS", 60f, "Highest supported hardware framerate")
}

data class CharacterOverlayConfig(
  val characterId: String = "puppy_01",
  val behavior: AnimationBehavior = AnimationBehavior.WALK,
  val sizePreset: CharacterSizePreset = CharacterSizePreset.SMALL,
  val customScalePercent: Float = 0.045f,
  val verticalOffsetPercent: Float = 0.004f, // 0.00 to 0.25 (0.4% default for TikTok ultra-bottom bar)
  val startXPercent: Float = 0.01f,          // 0.00 to 1.00
  val endXPercent: Float = 0.99f,            // 0.00 to 1.00
  val facingRight: Boolean = true,
  val reverseDirection: Boolean = false,
  val targetPlatform: SocialPlatform = SocialPlatform.TIKTOK,
  val framingMode: VideoFramingMode = VideoFramingMode.ORIGINAL,
  val showInstagramPreviewGuide: Boolean = true,
  val exportFpsOption: ExportFpsOption = ExportFpsOption.FPS_60
) {
  val showPlatformGuide: Boolean
    get() = showInstagramPreviewGuide

  // Effective start and end considering direction
  val effectiveStartX: Float
    get() = if (reverseDirection) endXPercent else startXPercent

  val effectiveEndX: Float
    get() = if (reverseDirection) startXPercent else endXPercent

  val effectiveFacingRight: Boolean
    get() = if (reverseDirection) !facingRight else facingRight
}
