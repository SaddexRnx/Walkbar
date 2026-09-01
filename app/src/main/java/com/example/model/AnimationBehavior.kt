package com.example.model

enum class AnimationBehavior(
  val displayName: String,
  val stepDurationMs: Long,
  val hopAmount: Float,
  val description: String = ""
) {
  PACE_SYNC("Pace-Sync", 0L, 0.09f, "Dynamically maps walking cadence to video duration for zero foot-sliding"),
  WALK("Walk", 500L, 0.08f, "Standard steady walking gait"),
  STROLL("Stroll", 750L, 0.05f, "Relaxed, leisurely strides"),
  RUN("Run", 280L, 0.14f, "Fast energetic running stride"),
  SPRINT("Sprint", 180L, 0.18f, "Rapid high-speed dash"),
  HOP("Hop", 380L, 0.22f, "Bouncy hopping motion");

  val isDynamic: Boolean get() = this == PACE_SYNC

  companion object {
    fun fromName(name: String): AnimationBehavior {
      return entries.find { it.name.equals(name, ignoreCase = true) } ?: WALK
    }
  }
}
