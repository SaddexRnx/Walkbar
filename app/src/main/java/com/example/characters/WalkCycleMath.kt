package com.example.characters

import com.example.model.AnimationBehavior
import com.example.model.CharacterOverlayConfig
import kotlin.math.abs

/**
 * Logic layer mapping video playback duration and timeline progression
 * to character locomotion, stride cadence, and pixel positioning.
 */
object WalkCycleMath {

  /**
   * Calculates normalized playback progress (0.0 to 1.0)
   */
  fun calculateProgress(currentTimeMs: Long, durationMs: Long): Float {
    if (durationMs <= 0L) return 0f
    return (currentTimeMs.toDouble() / durationMs.toDouble()).coerceIn(0.0, 1.0).toFloat()
  }

  /**
   * Calculates normalized X position (0.0 to 1.0) on video timeline
   */
  fun calculateNormalizedX(currentTimeMs: Long, durationMs: Long, config: CharacterOverlayConfig): Float {
    val progress = calculateProgress(currentTimeMs, durationMs)
    return config.effectiveStartX + progress * (config.effectiveEndX - config.effectiveStartX)
  }

  /**
   * Calculates pixel X position on video canvas
   */
  fun calculatePixelX(
    currentTimeMs: Long,
    durationMs: Long,
    config: CharacterOverlayConfig,
    canvasWidth: Float
  ): Float {
    val normX = calculateNormalizedX(currentTimeMs, durationMs, config)
    return normX * canvasWidth
  }

  /**
   * Calculates pixel Y position (bottom of character) on video canvas
   * verticalOffsetPercent represents height above video bottom.
   */
  fun calculatePixelY(
    config: CharacterOverlayConfig,
    canvasHeight: Float
  ): Float {
    return canvasHeight * (1f - config.verticalOffsetPercent)
  }

  /**
   * Calculates the optimal step cycle duration (in ms) by mapping video duration
   * and horizontal travel distance to character scale and stride length.
   * This guarantees that the character's feet stride in exact proportion to the
   * horizontal completion rate of the progress bar without foot-sliding.
   */
  fun getEffectiveStepDurationMs(
    behavior: AnimationBehavior,
    durationMs: Long = 0L,
    config: CharacterOverlayConfig = CharacterOverlayConfig(),
    canvasWidth: Float = 1080f,
    canvasHeight: Float = 1920f
  ): Long {
    if (!behavior.isDynamic || durationMs <= 0L) {
      return if (behavior.stepDurationMs > 0L) behavior.stepDurationMs else 480L
    }

    // Dynamic Pace-Sync Algorithm:
    // 1. Calculate physical travel distance in pixels across the progress bar
    val travelSpan = abs(config.effectiveEndX - config.effectiveStartX).coerceIn(0.1f, 1.0f)
    val totalDistancePx = travelSpan * (if (canvasWidth > 0f) canvasWidth else 1080f)

    // 2. Approximate natural stride length (roughly 55% of character height)
    val effectiveHeight = (if (canvasHeight > 0f) canvasHeight else 1920f)
    val charSizePx = effectiveHeight * config.customScalePercent.coerceIn(0.02f, 0.20f)
    val strideLengthPx = (charSizePx * 0.55f).coerceAtLeast(16f)

    // 3. Number of steps required to traverse the progress bar
    val totalSteps = (totalDistancePx / strideLengthPx).coerceIn(4f, 200f)

    // 4. Step cycle time in milliseconds per stride
    val calculatedStepMs = (durationMs.toDouble() / totalSteps).toLong()

    // Clamp between comfortable animated bounds (140ms sprint to 850ms slow stroll)
    return calculatedStepMs.coerceIn(140L, 850L)
  }

  /**
   * Calculates walking animation phase (0.0 to 1.0) for current timestamp,
   * taking into account video duration and dynamic pace synchronization.
   */
  fun calculatePhase(
    currentTimeMs: Long,
    behavior: AnimationBehavior,
    isPlaying: Boolean = true,
    durationMs: Long = 0L,
    config: CharacterOverlayConfig = CharacterOverlayConfig(),
    canvasWidth: Float = 1080f,
    canvasHeight: Float = 1920f
  ): Float {
    val cycleMs = getEffectiveStepDurationMs(behavior, durationMs, config, canvasWidth, canvasHeight)
    val ms = if (currentTimeMs < 0) 0L else currentTimeMs
    return ((ms % cycleMs).toDouble() / cycleMs.toDouble()).toFloat()
  }

  /**
   * Telemetry: Calculates horizontal velocity in pixels per second
   */
  fun calculateHorizontalVelocityPxPerSec(
    durationMs: Long,
    config: CharacterOverlayConfig,
    canvasWidth: Float
  ): Float {
    if (durationMs <= 0L) return 0f
    val distancePx = abs(config.effectiveEndX - config.effectiveStartX) * canvasWidth
    val durationSec = durationMs / 1000f
    return distancePx / durationSec
  }

  /**
   * Telemetry: Calculates cadence in steps per second (Hz)
   */
  fun calculateCadenceStepsPerSecond(stepDurationMs: Long): Float {
    if (stepDurationMs <= 0L) return 0f
    return 1000f / stepDurationMs.toFloat()
  }
}

