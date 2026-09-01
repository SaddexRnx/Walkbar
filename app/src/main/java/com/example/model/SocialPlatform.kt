package com.example.model

enum class SocialPlatform(
  val id: String,
  val displayName: String,
  val iconEmoji: String,
  val defaultVerticalOffset: Float, // 0.00 to 0.25
  val defaultStartX: Float,
  val defaultEndX: Float,
  val description: String,
  val progressColorHex: Long = 0xFFFFFFFF
) {
  TIKTOK(
    id = "tiktok",
    displayName = "TikTok",
    iconEmoji = "🎵",
    defaultVerticalOffset = 0.004f, // 0.4% - Ultra bottom edge
    defaultStartX = 0.01f,
    defaultEndX = 0.99f,
    description = "Ultra-bottom scrubber bar (0.4% height)",
    progressColorHex = 0xFFFFFFFF
  ),
  YOUTUBE_SHORTS(
    id = "youtube_shorts",
    displayName = "YouTube Shorts",
    iconEmoji = "▶️",
    defaultVerticalOffset = 0.009f, // 0.9% - Red scrubber bar
    defaultStartX = 0.01f,
    defaultEndX = 0.99f,
    description = "Signature red bottom scrubber (0.9% height)",
    progressColorHex = 0xFFFF0033
  ),
  INSTAGRAM_REELS(
    id = "instagram_reels",
    displayName = "Instagram Reels",
    iconEmoji = "📸",
    defaultVerticalOffset = 0.024f, // 2.4% - Reels timeline position
    defaultStartX = 0.02f,
    defaultEndX = 0.98f,
    description = "Standard Reels bottom timeline (2.4% height)",
    progressColorHex = 0xFFFFFFFF
  ),
  FACEBOOK_REELS(
    id = "facebook_reels",
    displayName = "Facebook Reels",
    iconEmoji = "👥",
    defaultVerticalOffset = 0.020f, // 2.0%
    defaultStartX = 0.02f,
    defaultEndX = 0.98f,
    description = "Facebook Reels bottom bar (2.0% height)",
    progressColorHex = 0xFF1877F2
  ),
  PHONE_SCREEN_TALL(
    id = "phone_screen_tall",
    displayName = "6.19\" Tall Screen",
    iconEmoji = "📱",
    defaultVerticalOffset = 0.002f, // 0.2% - for full screen phone displays (19.5:9)
    defaultStartX = 0.01f,
    defaultEndX = 0.99f,
    description = "19.5:9 display (eliminates black letterbox gap on TikTok)",
    progressColorHex = 0xFF00E5FF
  ),
  CUSTOM(
    id = "custom",
    displayName = "Custom / Direct Drag",
    iconEmoji = "🎯",
    defaultVerticalOffset = 0.038f,
    defaultStartX = 0.02f,
    defaultEndX = 0.98f,
    description = "Free touch drag & custom slider",
    progressColorHex = 0xFF6366F1
  )
}

enum class VideoFramingMode(
  val displayName: String,
  val aspectRatio: Float?, // null = dynamic / keep original
  val targetWidth: Int?,
  val targetHeight: Int?,
  val subtitle: String,
  val description: String
) {
  MATCH_DEVICE_SCREEN(
    displayName = "Match My Screen",
    aspectRatio = null, // Dynamically computed from device DisplayMetrics
    targetWidth = null,
    targetHeight = null,
    subtitle = "Auto-Fit Phone",
    description = "Exports at your phone's exact screen ratio so TikTok/Instagram have nothing to crop upon upload"
  ),
  ORIGINAL(
    displayName = "Original Ratio",
    aspectRatio = null,
    targetWidth = null,
    targetHeight = null,
    subtitle = "Preserve Source",
    description = "Keep original dimensions & aspect ratio from input video file"
  ),
  REELS_9_16(
    displayName = "9:16 Fullscreen Reel",
    aspectRatio = 9f / 16f,
    targetWidth = 1080,
    targetHeight = 1920,
    subtitle = "1080×1920 Fill",
    description = "Standard 9:16 frame. Auto-crops black borders from downloaded reels"
  ),
  PHONE_TALL_19_5_9(
    displayName = "19.5:9 Ultra-Tall",
    aspectRatio = 9f / 19.5f,
    targetWidth = 1080,
    targetHeight = 2340,
    subtitle = "1080×2340 Fill",
    description = "Tall smartphone screen ratio. Prevents black letterbox bars on 19.5:9 phones"
  )
}
