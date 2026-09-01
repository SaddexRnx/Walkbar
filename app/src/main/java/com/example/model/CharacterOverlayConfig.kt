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
  val framingMode: VideoFramingMode = VideoFramingMode.MATCH_DEVICE_SCREEN,
  val deviceScreenWidth: Int = 1080,
  val deviceScreenHeight: Int = 2340,
  val deviceScreenRatioFormatted: String = "9:19.5",
  val showInstagramPreviewGuide: Boolean = false,
  val showSafeZoneGuide: Boolean = false,
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

  fun getEffectiveAspectRatio(sourceAspectRatio: Float): Float {
    return when (framingMode) {
      VideoFramingMode.MATCH_DEVICE_SCREEN -> {
        if (deviceScreenHeight > 0 && deviceScreenWidth > 0) {
          (deviceScreenWidth.toFloat() / deviceScreenHeight.toFloat()).coerceIn(0.35f, 1.0f)
        } else {
          9f / 19.5f
        }
      }
      VideoFramingMode.REELS_9_16 -> 9f / 16f
      VideoFramingMode.PHONE_TALL_19_5_9 -> 9f / 19.5f
      VideoFramingMode.ORIGINAL -> sourceAspectRatio.coerceIn(0.35f, 2.5f)
    }
  }

  // Returns true if vertical offset is located in the extreme bottom 1.2% crop-risk band
  fun isInCropRiskZone(): Boolean = verticalOffsetPercent < 0.012f
}
