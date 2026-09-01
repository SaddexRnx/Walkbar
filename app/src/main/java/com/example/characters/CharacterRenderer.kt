package com.example.characters

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.nativeCanvas
import com.example.model.AnimationBehavior
import com.example.model.CharacterModel
import com.example.model.CharacterOverlayConfig
import com.example.model.CharacterType
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

object CharacterRenderer {

  private val sharedPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
    style = Paint.Style.FILL
  }
  private val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
    style = Paint.Style.STROKE
    strokeCap = Paint.Cap.ROUND
    strokeJoin = Paint.Join.ROUND
  }
  private val shadowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
    style = Paint.Style.FILL
    color = Color.argb(80, 0, 0, 0)
  }

  /**
   * Render directly onto Compose DrawScope
   */
  fun renderInCompose(
    drawScope: DrawScope,
    character: CharacterModel,
    config: CharacterOverlayConfig,
    currentTimeMs: Long,
    durationMs: Long,
    isPlaying: Boolean = true
  ) {
    val canvasWidth = drawScope.size.width
    val canvasHeight = drawScope.size.height

    val pixelX = WalkCycleMath.calculatePixelX(currentTimeMs, durationMs, config, canvasWidth)
    val pixelY = WalkCycleMath.calculatePixelY(config, canvasHeight)
    val phase = WalkCycleMath.calculatePhase(
      currentTimeMs = currentTimeMs,
      behavior = config.behavior,
      isPlaying = isPlaying,
      durationMs = durationMs,
      config = config,
      canvasWidth = canvasWidth,
      canvasHeight = canvasHeight
    )

    // Scale character relative to video height
    val charSize = canvasHeight * config.customScalePercent

    drawScope.drawContext.canvas.nativeCanvas.let { nativeCanvas ->
      drawCharacter(
        canvas = nativeCanvas,
        character = character,
        behavior = config.behavior,
        centerX = pixelX,
        bottomY = pixelY,
        size = charSize,
        phase = phase,
        facingRight = config.effectiveFacingRight,
        currentTimeMs = currentTimeMs
      )
    }
  }

  /**
   * Render onto native Android Canvas (used for export frame compositing and preview)
   */
  fun drawCharacter(
    canvas: Canvas,
    character: CharacterModel,
    behavior: AnimationBehavior,
    centerX: Float,
    bottomY: Float,
    size: Float,
    phase: Float,
    facingRight: Boolean = true,
    currentTimeMs: Long = 0L
  ) {
    val saveCount = canvas.save()

    // Translate to character position
    canvas.translate(centerX, bottomY)

    // Flip if facing left
    if (!facingRight) {
      canvas.scale(-1f, 1f)
    }

    // Normalized time angle
    val angle = (phase * 2f * PI).toFloat()
    val isBlinking = (currentTimeMs % 3200L) in 100L..250L

    // Ground Shadow
    val shadowWidth = size * 0.75f
    val shadowHeight = size * 0.16f
    canvas.drawOval(
      RectF(-shadowWidth / 2f, -shadowHeight / 2f, shadowWidth / 2f, shadowHeight / 2f),
      shadowPaint
    )

    when (character.type) {
      // Animals
      CharacterType.PUPPY -> drawPuppy(canvas, size, angle, behavior, isBlinking)
      CharacterType.CAT -> drawCat(canvas, size, angle, behavior, isBlinking)
      CharacterType.BUNNY -> drawBunny(canvas, size, angle, behavior, isBlinking)
      CharacterType.FOX -> drawFox(canvas, size, angle, behavior, isBlinking)
      CharacterType.PENGUIN -> drawPenguin(canvas, size, angle, behavior, isBlinking)
      CharacterType.FROG -> drawFrog(canvas, size, angle, behavior, isBlinking)
      CharacterType.DUCK -> drawDuck(canvas, size, angle, behavior, isBlinking)

      // Real 3D Objects
      CharacterType.THREE_D_DIAMOND -> ThreeDObjectRenderer.draw3DDiamond(canvas, size, angle, currentTimeMs)
      CharacterType.THREE_D_GOLD_COIN -> ThreeDObjectRenderer.draw3DGoldCoin(canvas, size, angle, currentTimeMs)
      CharacterType.THREE_D_CUBE -> ThreeDObjectRenderer.draw3DCube(canvas, size, angle)
      CharacterType.THREE_D_SATURN -> ThreeDObjectRenderer.draw3DSaturn(canvas, size, angle)
      CharacterType.THREE_D_UFO -> ThreeDObjectRenderer.draw3DUFO(canvas, size, angle, currentTimeMs)
      CharacterType.THREE_D_STAR -> ThreeDObjectRenderer.draw3DStar(canvas, size, angle)
      CharacterType.THREE_D_ROCKET -> ThreeDObjectRenderer.draw3DRocket(canvas, size, angle, currentTimeMs)

      // 3D-Styled Pixel Art Characters & Sprites
      CharacterType.ROBOT -> PixelArtRenderer.draw3DRobot(canvas, size, angle, isBlinking, currentTimeMs)
      CharacterType.ASTRONAUT -> PixelArtRenderer.draw3DAstronaut(canvas, size, angle, currentTimeMs)
      CharacterType.NINJA -> PixelArtRenderer.draw3DNinja(canvas, size, angle, isBlinking)
      CharacterType.PIXEL_DINO -> PixelArtRenderer.drawPixelDino(canvas, size, angle, isBlinking)
      CharacterType.PIXEL_HEART -> PixelArtRenderer.drawPixelHeart(canvas, size, angle)
      CharacterType.PIXEL_GHOST -> PixelArtRenderer.drawPixelGhost(canvas, size, angle)
      CharacterType.PIXEL_POTION -> PixelArtRenderer.drawPixelPotion(canvas, size, angle)
      CharacterType.PIXEL_CAT -> PixelArtRenderer.drawPixelCat(canvas, size, angle)
      CharacterType.PIXEL_COIN -> PixelArtRenderer.drawPixelCoin(canvas, size, angle)

      // Vehicles & Sci-Fi
      CharacterType.CYBER_CAR -> drawCyberCar(canvas, size, angle, currentTimeMs)
      CharacterType.HOVER_DRONE -> drawHoverDrone(canvas, size, angle, currentTimeMs)

      // Lifestyle & Fun
      CharacterType.BOBA_TEA -> drawBobaTea(canvas, size, angle)
      CharacterType.VINYL_RECORD -> drawVinylRecord(canvas, size, angle)
    }

    canvas.restoreToCount(saveCount)
  }

  // --- 1. PUPPY ---
  private fun drawPuppy(canvas: Canvas, size: Float, angle: Float, behavior: AnimationBehavior, isBlinking: Boolean) {
    val hop = abs(sin(angle)) * size * behavior.hopAmount
    val legSwing = sin(angle) * (size * 0.18f)
    val legSwingOpp = -legSwing

    val bodyY = -size * 0.45f - hop

    // Back legs
    sharedPaint.color = Color.rgb(217, 119, 6) // darker gold
    canvas.drawRoundRect(RectF(-size * 0.28f + legSwingOpp, bodyY + size * 0.2f, -size * 0.16f + legSwingOpp, 0f), 6f, 6f, sharedPaint)
    canvas.drawRoundRect(RectF(size * 0.12f + legSwing, bodyY + size * 0.2f, size * 0.24f + legSwing, 0f), 6f, 6f, sharedPaint)

    // Tail (wagging)
    val tailWag = sin(angle * 2f) * 20f
    canvas.save()
    canvas.translate(-size * 0.3f, bodyY + size * 0.05f)
    canvas.rotate(-35f + tailWag)
    sharedPaint.color = Color.rgb(245, 158, 11)
    canvas.drawRoundRect(RectF(-size * 0.05f, -size * 0.22f, size * 0.05f, 0f), 5f, 5f, sharedPaint)
    canvas.restore()

    // Body
    sharedPaint.color = Color.rgb(245, 158, 11) // Golden
    canvas.drawRoundRect(RectF(-size * 0.35f, bodyY - size * 0.15f, size * 0.22f, bodyY + size * 0.25f), size * 0.18f, size * 0.18f, sharedPaint)

    // Front legs
    sharedPaint.color = Color.rgb(245, 158, 11)
    canvas.drawRoundRect(RectF(-size * 0.24f + legSwing, bodyY + size * 0.2f, -size * 0.12f + legSwing, 0f), 6f, 6f, sharedPaint)
    canvas.drawRoundRect(RectF(size * 0.16f + legSwingOpp, bodyY + size * 0.2f, size * 0.28f + legSwingOpp, 0f), 6f, 6f, sharedPaint)

    // Head
    val headX = size * 0.24f
    val headY = bodyY - size * 0.22f
    canvas.drawCircle(headX, headY, size * 0.24f, sharedPaint)

    // Snout
    sharedPaint.color = Color.rgb(254, 243, 199)
    canvas.drawRoundRect(RectF(headX + size * 0.04f, headY - size * 0.02f, headX + size * 0.28f, headY + size * 0.18f), 8f, 8f, sharedPaint)

    // Nose
    sharedPaint.color = Color.rgb(30, 41, 59)
    canvas.drawCircle(headX + size * 0.24f, headY + size * 0.04f, size * 0.045f, sharedPaint)

    // Floppy Ear (bouncing)
    val earBounce = sin(angle) * 12f
    canvas.save()
    canvas.translate(headX - size * 0.08f, headY - size * 0.12f)
    canvas.rotate(15f + earBounce)
    sharedPaint.color = Color.rgb(217, 119, 6)
    canvas.drawRoundRect(RectF(-size * 0.08f, 0f, size * 0.08f, size * 0.32f), size * 0.08f, size * 0.08f, sharedPaint)
    canvas.restore()

    // Eye
    if (isBlinking) {
      strokePaint.color = Color.rgb(30, 41, 59)
      strokePaint.strokeWidth = size * 0.04f
      canvas.drawLine(headX + size * 0.05f, headY - size * 0.04f, headX + size * 0.15f, headY - size * 0.04f, strokePaint)
    } else {
      sharedPaint.color = Color.rgb(30, 41, 59)
      canvas.drawCircle(headX + size * 0.1f, headY - size * 0.05f, size * 0.045f, sharedPaint)
      sharedPaint.color = Color.WHITE
      canvas.drawCircle(headX + size * 0.12f, headY - size * 0.065f, size * 0.016f, sharedPaint)
    }
  }

  // --- 2. CAT ---
  private fun drawCat(canvas: Canvas, size: Float, angle: Float, behavior: AnimationBehavior, isBlinking: Boolean) {
    val hop = abs(sin(angle)) * size * behavior.hopAmount
    val legSwing = sin(angle) * (size * 0.16f)
    val bodyY = -size * 0.42f - hop

    // Back leg & Front leg far
    sharedPaint.color = Color.rgb(234, 88, 12)
    canvas.drawRoundRect(RectF(-size * 0.26f - legSwing, bodyY + size * 0.18f, -size * 0.15f - legSwing, 0f), 5f, 5f, sharedPaint)
    canvas.drawRoundRect(RectF(size * 0.10f + legSwing, bodyY + size * 0.18f, size * 0.21f + legSwing, 0f), 5f, 5f, sharedPaint)

    // Tail (curved)
    val tailAngle = sin(angle) * 15f
    canvas.save()
    canvas.translate(-size * 0.28f, bodyY + size * 0.08f)
    canvas.rotate(-45f + tailAngle)
    sharedPaint.color = Color.rgb(251, 146, 60)
    val tailPath = Path().apply {
      moveTo(0f, 0f)
      quadTo(-size * 0.1f, -size * 0.2f, size * 0.05f, -size * 0.35f)
      lineTo(size * 0.1f, -size * 0.32f)
      quadTo(-size * 0.05f, -size * 0.18f, size * 0.06f, 0f)
      close()
    }
    canvas.drawPath(tailPath, sharedPaint)
    canvas.restore()

    // Body
    sharedPaint.color = Color.rgb(251, 146, 60)
    canvas.drawRoundRect(RectF(-size * 0.32f, bodyY - size * 0.14f, size * 0.20f, bodyY + size * 0.22f), size * 0.16f, size * 0.16f, sharedPaint)

    // Front leg near
    canvas.drawRoundRect(RectF(-size * 0.22f + legSwing, bodyY + size * 0.18f, -size * 0.11f + legSwing, 0f), 5f, 5f, sharedPaint)
    canvas.drawRoundRect(RectF(size * 0.14f - legSwing, bodyY + size * 0.18f, size * 0.25f - legSwing, 0f), 5f, 5f, sharedPaint)

    // Head
    val headX = size * 0.22f
    val headY = bodyY - size * 0.18f
    canvas.drawCircle(headX, headY, size * 0.21f, sharedPaint)

    // Pointed ears
    val earPath = Path().apply {
      moveTo(headX - size * 0.14f, headY - size * 0.10f)
      lineTo(headX - size * 0.16f, headY - size * 0.32f)
      lineTo(headX - size * 0.02f, headY - size * 0.16f)
      close()
      moveTo(headX + size * 0.02f, headY - size * 0.16f)
      lineTo(headX + size * 0.14f, headY - size * 0.32f)
      lineTo(headX + size * 0.16f, headY - size * 0.10f)
      close()
    }
    canvas.drawPath(earPath, sharedPaint)

    // Inner pink ears
    sharedPaint.color = Color.rgb(253, 164, 175)
    val innerEar = Path().apply {
      moveTo(headX - size * 0.12f, headY - size * 0.12f)
      lineTo(headX - size * 0.14f, headY - size * 0.26f)
      lineTo(headX - size * 0.04f, headY - size * 0.15f)
      close()
      moveTo(headX + size * 0.04f, headY - size * 0.15f)
      lineTo(headX + size * 0.12f, headY - size * 0.26f)
      lineTo(headX + size * 0.14f, headY - size * 0.12f)
      close()
    }
    canvas.drawPath(innerEar, sharedPaint)

    // Eyes
    sharedPaint.color = Color.rgb(30, 41, 59)
    if (isBlinking) {
      strokePaint.color = Color.rgb(30, 41, 59)
      strokePaint.strokeWidth = size * 0.035f
      canvas.drawLine(headX + size * 0.06f, headY - size * 0.02f, headX + size * 0.14f, headY - size * 0.02f, strokePaint)
    } else {
      canvas.drawCircle(headX + size * 0.09f, headY - size * 0.03f, size * 0.04f, sharedPaint)
      sharedPaint.color = Color.WHITE
      canvas.drawCircle(headX + size * 0.11f, headY - size * 0.045f, size * 0.014f, sharedPaint)
    }

    // Whiskers
    strokePaint.color = Color.rgb(203, 213, 225)
    strokePaint.strokeWidth = size * 0.02f
    canvas.drawLine(headX + size * 0.14f, headY + size * 0.04f, headX + size * 0.26f, headY + size * 0.02f, strokePaint)
    canvas.drawLine(headX + size * 0.14f, headY + size * 0.07f, headX + size * 0.25f, headY + size * 0.09f, strokePaint)
  }

  // --- 3. BUNNY ---
  private fun drawBunny(canvas: Canvas, size: Float, angle: Float, behavior: AnimationBehavior, isBlinking: Boolean) {
    val hop = abs(sin(angle)) * size * 0.25f
    val bodyY = -size * 0.40f - hop
    val earTilt = sin(angle) * 15f

    // Fluffy tail
    sharedPaint.color = Color.rgb(241, 245, 249)
    canvas.drawCircle(-size * 0.26f, bodyY + size * 0.10f, size * 0.09f, sharedPaint)

    // Back leg
    sharedPaint.color = Color.rgb(226, 232, 240)
    canvas.drawRoundRect(RectF(-size * 0.20f, bodyY + size * 0.06f, size * 0.02f, bodyY + size * 0.28f), size * 0.1f, size * 0.1f, sharedPaint)
    canvas.drawRoundRect(RectF(-size * 0.18f + sin(angle) * size * 0.1f, bodyY + size * 0.22f, size * 0.05f + sin(angle) * size * 0.1f, 0f), 5f, 5f, sharedPaint)

    // Body
    sharedPaint.color = Color.rgb(248, 250, 252)
    canvas.drawOval(RectF(-size * 0.24f, bodyY - size * 0.15f, size * 0.18f, bodyY + size * 0.24f), sharedPaint)

    // Front paws
    canvas.drawRoundRect(RectF(size * 0.06f - sin(angle) * size * 0.08f, bodyY + size * 0.18f, size * 0.16f - sin(angle) * size * 0.08f, 0f), 5f, 5f, sharedPaint)

    // Head
    val headX = size * 0.16f
    val headY = bodyY - size * 0.18f
    canvas.drawCircle(headX, headY, size * 0.20f, sharedPaint)

    // Long Bunny Ears
    canvas.save()
    canvas.translate(headX - size * 0.04f, headY - size * 0.15f)
    canvas.rotate(-15f - earTilt)
    sharedPaint.color = Color.rgb(241, 245, 249)
    canvas.drawRoundRect(RectF(-size * 0.06f, -size * 0.38f, size * 0.06f, 0f), size * 0.06f, size * 0.06f, sharedPaint)
    sharedPaint.color = Color.rgb(253, 164, 175)
    canvas.drawRoundRect(RectF(-size * 0.035f, -size * 0.32f, size * 0.035f, -size * 0.04f), size * 0.035f, size * 0.035f, sharedPaint)
    canvas.restore()

    // Eye & nose
    sharedPaint.color = Color.rgb(30, 41, 59)
    if (isBlinking) {
      strokePaint.color = Color.rgb(30, 41, 59)
      strokePaint.strokeWidth = size * 0.035f
      canvas.drawLine(headX + size * 0.04f, headY - size * 0.03f, headX + size * 0.12f, headY - size * 0.03f, strokePaint)
    } else {
      canvas.drawCircle(headX + size * 0.08f, headY - size * 0.03f, size * 0.04f, sharedPaint)
      sharedPaint.color = Color.WHITE
      canvas.drawCircle(headX + size * 0.10f, headY - size * 0.045f, size * 0.015f, sharedPaint)
    }

    // Pink nose
    sharedPaint.color = Color.rgb(251, 113, 133)
    canvas.drawCircle(headX + size * 0.18f, headY + size * 0.04f, size * 0.03f, sharedPaint)
  }

  // --- 4. FOX ---
  private fun drawFox(canvas: Canvas, size: Float, angle: Float, behavior: AnimationBehavior, isBlinking: Boolean) {
    val hop = abs(sin(angle)) * size * behavior.hopAmount
    val legSwing = sin(angle) * (size * 0.18f)
    val bodyY = -size * 0.44f - hop

    // Bushy tail with white tip
    val tailWag = sin(angle) * 18f
    canvas.save()
    canvas.translate(-size * 0.28f, bodyY + size * 0.02f)
    canvas.rotate(-30f + tailWag)
    sharedPaint.color = Color.rgb(234, 88, 12)
    val foxTail = Path().apply {
      moveTo(0f, 0f)
      quadTo(-size * 0.24f, -size * 0.15f, -size * 0.26f, -size * 0.35f)
      lineTo(-size * 0.12f, -size * 0.32f)
      quadTo(-size * 0.04f, -size * 0.10f, 0f, 0f)
      close()
    }
    canvas.drawPath(foxTail, sharedPaint)
    sharedPaint.color = Color.WHITE
    canvas.drawCircle(-size * 0.24f, -size * 0.32f, size * 0.07f, sharedPaint)
    canvas.restore()

    // Back legs
    sharedPaint.color = Color.rgb(194, 65, 12)
    canvas.drawRoundRect(RectF(-size * 0.26f - legSwing, bodyY + size * 0.20f, -size * 0.15f - legSwing, 0f), 5f, 5f, sharedPaint)
    canvas.drawRoundRect(RectF(size * 0.10f + legSwing, bodyY + size * 0.20f, size * 0.21f + legSwing, 0f), 5f, 5f, sharedPaint)

    // Body
    sharedPaint.color = Color.rgb(234, 88, 12)
    canvas.drawRoundRect(RectF(-size * 0.32f, bodyY - size * 0.14f, size * 0.22f, bodyY + size * 0.24f), size * 0.16f, size * 0.16f, sharedPaint)

    // Front legs
    canvas.drawRoundRect(RectF(-size * 0.22f + legSwing, bodyY + size * 0.20f, -size * 0.11f + legSwing, 0f), 5f, 5f, sharedPaint)
    canvas.drawRoundRect(RectF(size * 0.15f - legSwing, bodyY + size * 0.20f, size * 0.26f - legSwing, 0f), 5f, 5f, sharedPaint)

    // Head
    val headX = size * 0.24f
    val headY = bodyY - size * 0.18f
    canvas.drawCircle(headX, headY, size * 0.22f, sharedPaint)

    // Snout
    val snoutPath = Path().apply {
      moveTo(headX, headY - size * 0.04f)
      lineTo(headX + size * 0.26f, headY + size * 0.08f)
      lineTo(headX, headY + size * 0.18f)
      close()
    }
    canvas.drawPath(snoutPath, sharedPaint)
    sharedPaint.color = Color.WHITE
    canvas.drawRoundRect(RectF(headX + size * 0.02f, headY + size * 0.06f, headX + size * 0.22f, headY + size * 0.18f), 6f, 6f, sharedPaint)

    // Black nose tip
    sharedPaint.color = Color.BLACK
    canvas.drawCircle(headX + size * 0.26f, headY + size * 0.08f, size * 0.035f, sharedPaint)

    // Fox Ears
    sharedPaint.color = Color.rgb(234, 88, 12)
    val ear = Path().apply {
      moveTo(headX - size * 0.12f, headY - size * 0.10f)
      lineTo(headX - size * 0.12f, headY - size * 0.34f)
      lineTo(headX + size * 0.02f, headY - size * 0.15f)
      close()
    }
    canvas.drawPath(ear, sharedPaint)
    sharedPaint.color = Color.BLACK
    canvas.drawCircle(headX - size * 0.11f, headY - size * 0.30f, size * 0.035f, sharedPaint)

    // Eye
    sharedPaint.color = Color.rgb(30, 41, 59)
    if (isBlinking) {
      strokePaint.color = Color.rgb(30, 41, 59)
      strokePaint.strokeWidth = size * 0.035f
      canvas.drawLine(headX + size * 0.06f, headY - size * 0.02f, headX + size * 0.14f, headY - size * 0.02f, strokePaint)
    } else {
      canvas.drawCircle(headX + size * 0.09f, headY - size * 0.03f, size * 0.04f, sharedPaint)
      sharedPaint.color = Color.WHITE
      canvas.drawCircle(headX + size * 0.11f, headY - size * 0.045f, size * 0.015f, sharedPaint)
    }
  }

  // --- 5. PENGUIN ---
  private fun drawPenguin(canvas: Canvas, size: Float, angle: Float, behavior: AnimationBehavior, isBlinking: Boolean) {
    // Waddling side-to-side tilt
    val waddleTilt = sin(angle) * 12f
    val legSwing = sin(angle) * (size * 0.14f)
    val bodyY = -size * 0.44f

    canvas.save()
    canvas.translate(0f, bodyY + size * 0.35f)
    canvas.rotate(waddleTilt)
    canvas.translate(0f, -(bodyY + size * 0.35f))

    // Feet
    sharedPaint.color = Color.rgb(249, 115, 22)
    canvas.drawRoundRect(RectF(-size * 0.18f + legSwing, bodyY + size * 0.32f, -size * 0.04f + legSwing, 0f), 5f, 5f, sharedPaint)
    canvas.drawRoundRect(RectF(size * 0.04f - legSwing, bodyY + size * 0.32f, size * 0.18f - legSwing, 0f), 5f, 5f, sharedPaint)

    // Tuxedo Body
    sharedPaint.color = Color.rgb(30, 41, 59)
    canvas.drawRoundRect(RectF(-size * 0.22f, bodyY - size * 0.30f, size * 0.22f, bodyY + size * 0.34f), size * 0.20f, size * 0.20f, sharedPaint)

    // White belly
    sharedPaint.color = Color.rgb(248, 250, 252)
    canvas.drawRoundRect(RectF(-size * 0.12f, bodyY - size * 0.18f, size * 0.18f, bodyY + size * 0.30f), size * 0.14f, size * 0.14f, sharedPaint)

    // Flipper
    sharedPaint.color = Color.rgb(15, 23, 42)
    val flipperAngle = sin(angle) * 20f
    canvas.save()
    canvas.translate(size * 0.08f, bodyY - size * 0.05f)
    canvas.rotate(15f + flipperAngle)
    canvas.drawRoundRect(RectF(-size * 0.05f, 0f, size * 0.05f, size * 0.24f), size * 0.05f, size * 0.05f, sharedPaint)
    canvas.restore()

    // Beak
    sharedPaint.color = Color.rgb(249, 115, 22)
    val beak = Path().apply {
      moveTo(size * 0.14f, bodyY - size * 0.18f)
      lineTo(size * 0.28f, bodyY - size * 0.14f)
      lineTo(size * 0.14f, bodyY - size * 0.10f)
      close()
    }
    canvas.drawPath(beak, sharedPaint)

    // Eye
    sharedPaint.color = Color.rgb(15, 23, 42)
    if (isBlinking) {
      strokePaint.color = Color.rgb(15, 23, 42)
      strokePaint.strokeWidth = size * 0.035f
      canvas.drawLine(size * 0.06f, bodyY - size * 0.22f, size * 0.14f, bodyY - size * 0.22f, strokePaint)
    } else {
      canvas.drawCircle(size * 0.10f, bodyY - size * 0.22f, size * 0.038f, sharedPaint)
      sharedPaint.color = Color.WHITE
      canvas.drawCircle(size * 0.12f, bodyY - size * 0.235f, size * 0.015f, sharedPaint)
    }

    canvas.restore()
  }

  // --- 6. FROG ---
  private fun drawFrog(canvas: Canvas, size: Float, angle: Float, behavior: AnimationBehavior, isBlinking: Boolean) {
    val hop = abs(sin(angle)) * size * 0.28f
    val bodyY = -size * 0.32f - hop
    val legStretch = abs(sin(angle)) * size * 0.15f

    // Springy back legs
    sharedPaint.color = Color.rgb(22, 163, 74)
    canvas.drawOval(RectF(-size * 0.28f, bodyY + size * 0.04f, -size * 0.08f, bodyY + size * 0.24f + legStretch), sharedPaint)
    canvas.drawRoundRect(RectF(-size * 0.26f, bodyY + size * 0.20f + legStretch, -size * 0.06f, 0f), 5f, 5f, sharedPaint)

    // Body
    sharedPaint.color = Color.rgb(34, 197, 94)
    canvas.drawOval(RectF(-size * 0.22f, bodyY - size * 0.16f, size * 0.22f, bodyY + size * 0.20f), sharedPaint)

    // Light belly
    sharedPaint.color = Color.rgb(187, 247, 208)
    canvas.drawOval(RectF(-size * 0.10f, bodyY - size * 0.04f, size * 0.18f, bodyY + size * 0.18f), sharedPaint)

    // Front feet
    sharedPaint.color = Color.rgb(34, 197, 94)
    canvas.drawRoundRect(RectF(size * 0.06f, bodyY + size * 0.12f, size * 0.18f, 0f), 4f, 4f, sharedPaint)

    // Bulging Frog Eyes
    canvas.drawCircle(size * 0.08f, bodyY - size * 0.18f, size * 0.09f, sharedPaint)
    sharedPaint.color = Color.WHITE
    canvas.drawCircle(size * 0.09f, bodyY - size * 0.18f, size * 0.07f, sharedPaint)

    if (isBlinking) {
      strokePaint.color = Color.rgb(20, 83, 45)
      strokePaint.strokeWidth = size * 0.035f
      canvas.drawLine(size * 0.04f, bodyY - size * 0.18f, size * 0.14f, bodyY - size * 0.18f, strokePaint)
    } else {
      sharedPaint.color = Color.rgb(20, 83, 45)
      canvas.drawCircle(size * 0.11f, bodyY - size * 0.18f, size * 0.038f, sharedPaint)
      sharedPaint.color = Color.WHITE
      canvas.drawCircle(size * 0.12f, bodyY - size * 0.195f, size * 0.015f, sharedPaint)
    }
  }

  // --- 7. DUCK ---
  private fun drawDuck(canvas: Canvas, size: Float, angle: Float, behavior: AnimationBehavior, isBlinking: Boolean) {
    val waddleTilt = sin(angle) * 10f
    val legSwing = sin(angle) * (size * 0.15f)
    val bodyY = -size * 0.40f

    // Orange webbed feet
    sharedPaint.color = Color.rgb(249, 115, 22)
    canvas.drawRoundRect(RectF(-size * 0.14f + legSwing, bodyY + size * 0.28f, -size * 0.02f + legSwing, 0f), 4f, 4f, sharedPaint)
    canvas.drawRoundRect(RectF(size * 0.04f - legSwing, bodyY + size * 0.28f, size * 0.16f - legSwing, 0f), 4f, 4f, sharedPaint)

    // Body
    sharedPaint.color = Color.rgb(250, 204, 21) // Yellow
    canvas.drawOval(RectF(-size * 0.26f, bodyY - size * 0.12f, size * 0.18f, bodyY + size * 0.30f), sharedPaint)

    // Tail feather tilt
    val tail = Path().apply {
      moveTo(-size * 0.22f, bodyY)
      lineTo(-size * 0.32f, bodyY - size * 0.08f)
      lineTo(-size * 0.20f, bodyY + size * 0.10f)
      close()
    }
    canvas.drawPath(tail, sharedPaint)

    // Wing (flapping)
    val wingAngle = sin(angle) * 14f
    canvas.save()
    canvas.translate(-size * 0.04f, bodyY + size * 0.04f)
    canvas.rotate(wingAngle)
    sharedPaint.color = Color.rgb(234, 179, 8)
    canvas.drawOval(RectF(-size * 0.12f, -size * 0.06f, size * 0.12f, size * 0.12f), sharedPaint)
    canvas.restore()

    // Head
    val headX = size * 0.16f
    val headY = bodyY - size * 0.16f
    sharedPaint.color = Color.rgb(250, 204, 21)
    canvas.drawCircle(headX, headY, size * 0.18f, sharedPaint)

    // Orange Beak
    sharedPaint.color = Color.rgb(249, 115, 22)
    val beak = Path().apply {
      moveTo(headX + size * 0.10f, headY - size * 0.04f)
      lineTo(headX + size * 0.28f, headY)
      lineTo(headX + size * 0.10f, headY + size * 0.08f)
      close()
    }
    canvas.drawPath(beak, sharedPaint)

    // Eye
    sharedPaint.color = Color.rgb(30, 41, 59)
    if (isBlinking) {
      strokePaint.color = Color.rgb(30, 41, 59)
      strokePaint.strokeWidth = size * 0.035f
      canvas.drawLine(headX + size * 0.04f, headY - size * 0.04f, headX + size * 0.12f, headY - size * 0.04f, strokePaint)
    } else {
      canvas.drawCircle(headX + size * 0.08f, headY - size * 0.04f, size * 0.036f, sharedPaint)
      sharedPaint.color = Color.WHITE
      canvas.drawCircle(headX + size * 0.095f, headY - size * 0.055f, size * 0.014f, sharedPaint)
    }
  }

  // --- 8. ROBOT ---
  private fun drawRobot(canvas: Canvas, size: Float, angle: Float, behavior: AnimationBehavior, isBlinking: Boolean, currentTimeMs: Long) {
    val legSwing = sin(angle) * (size * 0.16f)
    val bodyY = -size * 0.44f

    // Mechanical Legs
    sharedPaint.color = Color.rgb(14, 116, 144)
    canvas.drawRoundRect(RectF(-size * 0.18f + legSwing, bodyY + size * 0.22f, -size * 0.06f + legSwing, 0f), 4f, 4f, sharedPaint)
    canvas.drawRoundRect(RectF(size * 0.06f - legSwing, bodyY + size * 0.22f, size * 0.18f - legSwing, 0f), 4f, 4f, sharedPaint)

    // Metal Box Body
    sharedPaint.color = Color.rgb(6, 182, 212)
    canvas.drawRoundRect(RectF(-size * 0.22f, bodyY - size * 0.10f, size * 0.22f, bodyY + size * 0.24f), 6f, 6f, sharedPaint)

    // Chest Screen / Meter
    sharedPaint.color = Color.rgb(15, 23, 42)
    canvas.drawRoundRect(RectF(-size * 0.14f, bodyY - size * 0.02f, size * 0.14f, bodyY + size * 0.16f), 4f, 4f, sharedPaint)
    // Glowing chest meter
    sharedPaint.color = Color.rgb(52, 211, 153)
    val meterWidth = (abs(sin(currentTimeMs / 300.0)).toFloat() * size * 0.20f)
    canvas.drawRect(RectF(-size * 0.10f, bodyY + size * 0.05f, -size * 0.10f + meterWidth, bodyY + size * 0.09f), sharedPaint)

    // Mechanical Arms
    sharedPaint.color = Color.rgb(14, 116, 144)
    val armAngle = -legSwing * 1.5f
    canvas.save()
    canvas.translate(0f, bodyY)
    canvas.rotate(armAngle)
    canvas.drawRoundRect(RectF(-size * 0.04f, 0f, size * 0.04f, size * 0.20f), 4f, 4f, sharedPaint)
    canvas.restore()

    // Robot Head
    val headY = bodyY - size * 0.28f
    sharedPaint.color = Color.rgb(6, 182, 212)
    canvas.drawRoundRect(RectF(-size * 0.18f, headY - size * 0.16f, size * 0.18f, headY + size * 0.14f), 6f, 6f, sharedPaint)

    // Antenna & glowing LED tip
    strokePaint.color = Color.rgb(14, 116, 144)
    strokePaint.strokeWidth = size * 0.035f
    canvas.drawLine(0f, headY - size * 0.16f, 0f, headY - size * 0.32f, strokePaint)
    sharedPaint.color = if ((currentTimeMs / 400L) % 2L == 0L) Color.rgb(239, 68, 68) else Color.rgb(254, 240, 138)
    canvas.drawCircle(0f, headY - size * 0.34f, size * 0.045f, sharedPaint)

    // Visor / Eyes
    sharedPaint.color = Color.rgb(15, 23, 42)
    canvas.drawRoundRect(RectF(-size * 0.14f, headY - size * 0.08f, size * 0.14f, headY + size * 0.06f), 4f, 4f, sharedPaint)

    if (isBlinking) {
      sharedPaint.color = Color.rgb(6, 182, 212)
      canvas.drawRect(RectF(-size * 0.10f, headY - size * 0.02f, size * 0.10f, headY + size * 0.01f), sharedPaint)
    } else {
      sharedPaint.color = Color.rgb(0, 229, 255) // Cyan LED eye
      canvas.drawCircle(-size * 0.05f, headY - size * 0.01f, size * 0.032f, sharedPaint)
      canvas.drawCircle(size * 0.05f, headY - size * 0.01f, size * 0.032f, sharedPaint)
    }
  }

  // --- 9. CYBER CAR ---
  private fun drawCyberCar(canvas: Canvas, size: Float, angle: Float, currentTimeMs: Long) {
    val bodyY = -size * 0.28f
    val carW = size * 0.85f
    val carH = size * 0.26f

    // Car Body Shadow & Neon Underglow
    val underglow = Paint(Paint.ANTI_ALIAS_FLAG).apply {
      style = Paint.Style.FILL
      color = Color.argb(90, 6, 182, 212)
    }
    canvas.drawOval(RectF(-carW * 0.45f, bodyY + carH * 0.3f, carW * 0.45f, bodyY + carH * 0.85f), underglow)

    // Aerodynamic Chassis
    val chassisPath = Path().apply {
      moveTo(-carW * 0.45f, bodyY + carH * 0.4f)
      lineTo(-carW * 0.40f, bodyY - carH * 0.1f)
      lineTo(-carW * 0.10f, bodyY - carH * 0.7f) // Roof slope
      lineTo(carW * 0.18f, bodyY - carH * 0.7f)  // Roof top
      lineTo(carW * 0.42f, bodyY + carH * 0.05f) // Hood slope
      lineTo(carW * 0.48f, bodyY + carH * 0.4f)  // Front bumper
      close()
    }
    sharedPaint.color = Color.rgb(236, 72, 153) // Cyberpunk Pink
    canvas.drawPath(chassisPath, sharedPaint)

    // Cyber Glass Cockpit
    val cockpitPath = Path().apply {
      moveTo(-carW * 0.06f, bodyY - carH * 0.62f)
      lineTo(carW * 0.15f, bodyY - carH * 0.62f)
      lineTo(carW * 0.32f, bodyY + carH * 0.02f)
      lineTo(-carW * 0.02f, bodyY + carH * 0.02f)
      close()
    }
    sharedPaint.color = Color.rgb(15, 23, 42) // Dark glass
    canvas.drawPath(cockpitPath, sharedPaint)

    // Neon Headlight Beam
    val beam = Path().apply {
      moveTo(carW * 0.46f, bodyY + carH * 0.12f)
      lineTo(carW * 0.85f, bodyY + carH * 0.0f)
      lineTo(carW * 0.85f, bodyY + carH * 0.45f)
      lineTo(carW * 0.46f, bodyY + carH * 0.32f)
      close()
    }
    sharedPaint.color = Color.argb(45, 6, 182, 212)
    canvas.drawPath(beam, sharedPaint)

    // Glowing Cyan Headlight
    sharedPaint.color = Color.rgb(6, 182, 212)
    canvas.drawCircle(carW * 0.46f, bodyY + carH * 0.22f, 3.5f, sharedPaint)

    // Spinning Wheels (Front & Back)
    val wheelRadius = size * 0.12f
    val wheelY = bodyY + carH * 0.42f
    val wheel1X = -carW * 0.26f
    val wheel2X = carW * 0.28f
    val wheelSpin = (angle * 6f) * 180f / PI.toFloat()

    listOf(wheel1X, wheel2X).forEach { wx ->
      // Tire
      sharedPaint.color = Color.rgb(30, 41, 59)
      canvas.drawCircle(wx, wheelY, wheelRadius, sharedPaint)
      // Neon Rim
      strokePaint.style = Paint.Style.STROKE
      strokePaint.strokeWidth = 2.5f
      strokePaint.color = Color.rgb(6, 182, 212)
      canvas.drawCircle(wx, wheelY, wheelRadius * 0.65f, strokePaint)

      // Spinning Rim Spokes
      val saveW = canvas.save()
      canvas.translate(wx, wheelY)
      canvas.rotate(wheelSpin)
      strokePaint.strokeWidth = 1.5f
      canvas.drawLine(-wheelRadius * 0.65f, 0f, wheelRadius * 0.65f, 0f, strokePaint)
      canvas.drawLine(0f, -wheelRadius * 0.65f, 0f, wheelRadius * 0.65f, strokePaint)
      canvas.restoreToCount(saveW)
    }
  }

  // --- 10. HOVER DRONE ---
  private fun drawHoverDrone(canvas: Canvas, size: Float, angle: Float, currentTimeMs: Long) {
    val hoverY = -size * 0.52f + sin(angle * 2f) * (size * 0.06f)
    val droneTilt = sin(angle) * 6f

    val saveCount = canvas.save()
    canvas.translate(0f, hoverY)
    canvas.rotate(droneTilt)

    // Drone Body
    sharedPaint.color = Color.rgb(59, 130, 246) // Electric Blue
    canvas.drawRoundRect(RectF(-size * 0.25f, -size * 0.08f, size * 0.25f, size * 0.12f), 8f, 8f, sharedPaint)

    // Central Dome Camera Eye
    sharedPaint.color = Color.rgb(15, 23, 42)
    canvas.drawCircle(0f, 0f, size * 0.09f, sharedPaint)
    sharedPaint.color = Color.rgb(239, 68, 68) // Red camera sensor
    canvas.drawCircle(0f, 0f, size * 0.04f, sharedPaint)

    // Left & Right Rotor Arms
    strokePaint.style = Paint.Style.STROKE
    strokePaint.strokeWidth = 4f
    strokePaint.color = Color.rgb(30, 41, 59)
    canvas.drawLine(-size * 0.22f, 0f, -size * 0.42f, -size * 0.12f, strokePaint)
    canvas.drawLine(size * 0.22f, 0f, size * 0.42f, -size * 0.12f, strokePaint)

    // Spinning Blades (Motion Blur Ellipses)
    val bladePhase = (currentTimeMs % 100L).toFloat() / 100f
    sharedPaint.color = Color.argb(160, 224, 242, 254)
    canvas.drawOval(RectF(-size * 0.58f, -size * 0.16f, -size * 0.26f, -size * 0.08f), sharedPaint)
    canvas.drawOval(RectF(size * 0.26f, -size * 0.16f, size * 0.58f, -size * 0.08f), sharedPaint)

    canvas.restoreToCount(saveCount)
  }

  // --- 11. BOBA TEA ---
  private fun drawBobaTea(canvas: Canvas, size: Float, angle: Float) {
    val bobY = -size * 0.46f + abs(sin(angle)) * (size * 0.04f)
    val saveCount = canvas.save()
    canvas.translate(0f, bobY)

    val cupW = size * 0.42f
    val cupH = size * 0.55f

    // 1. Straw
    strokePaint.style = Paint.Style.STROKE
    strokePaint.strokeWidth = size * 0.06f
    strokePaint.color = Color.rgb(239, 68, 68) // Red diagonal straw
    canvas.drawLine(size * 0.05f, -cupH * 0.45f, size * 0.18f, -cupH * 0.78f, strokePaint)

    // 2. Clear Cup
    val cupPath = Path().apply {
      moveTo(-cupW * 0.5f, -cupH * 0.45f)
      lineTo(cupW * 0.5f, -cupH * 0.45f)
      lineTo(cupW * 0.38f, cupH * 0.45f)
      lineTo(-cupW * 0.38f, cupH * 0.45f)
      close()
    }
    sharedPaint.color = Color.argb(180, 254, 243, 199) // Milk tea color
    canvas.drawPath(cupPath, sharedPaint)

    // Milk tea top foam
    sharedPaint.color = Color.rgb(255, 255, 255)
    canvas.drawOval(RectF(-cupW * 0.48f, -cupH * 0.48f, cupW * 0.48f, -cupH * 0.38f), sharedPaint)

    // Bouncing Tapioca Pearls
    sharedPaint.color = Color.rgb(69, 26, 3) // Dark brown pearls
    val pearlR = size * 0.038f
    val pOffsets = listOf(
      Pair(-cupW * 0.2f, cupH * 0.32f),
      Pair(-cupW * 0.05f, cupH * 0.36f),
      Pair(cupW * 0.12f, cupH * 0.30f),
      Pair(cupW * 0.22f, cupH * 0.35f),
      Pair(-cupW * 0.12f, cupH * 0.20f),
      Pair(cupW * 0.05f, cupH * 0.22f)
    )
    for (p in pOffsets) {
      val pearlBounce = sin(angle * 2f + p.first) * (size * 0.02f)
      canvas.drawCircle(p.first, p.second + pearlBounce, pearlR, sharedPaint)
    }

    // Cup outline
    strokePaint.style = Paint.Style.STROKE
    strokePaint.strokeWidth = 2.5f
    strokePaint.color = Color.argb(120, 255, 255, 255)
    canvas.drawPath(cupPath, strokePaint)

    canvas.restoreToCount(saveCount)
  }

  // --- 12. NEON VINYL RECORD ---
  private fun drawVinylRecord(canvas: Canvas, size: Float, angle: Float) {
    val hoverY = -size * 0.48f + sin(angle) * (size * 0.04f)
    val spin = (angle * 2f) * 180f / PI.toFloat()

    val saveCount = canvas.save()
    canvas.translate(0f, hoverY)

    // Vinyl Disc
    val r = size * 0.42f
    sharedPaint.color = Color.rgb(15, 23, 42) // Black vinyl
    canvas.drawCircle(0f, 0f, r, sharedPaint)

    // Concentric Sound Grooves
    strokePaint.style = Paint.Style.STROKE
    strokePaint.strokeWidth = 1.2f
    strokePaint.color = Color.argb(45, 255, 255, 255)
    canvas.drawCircle(0f, 0f, r * 0.85f, strokePaint)
    canvas.drawCircle(0f, 0f, r * 0.70f, strokePaint)
    canvas.drawCircle(0f, 0f, r * 0.55f, strokePaint)

    // Spinning Center Label with Neon Gradient
    val saveLabel = canvas.save()
    canvas.rotate(spin)
    sharedPaint.color = Color.rgb(236, 72, 153) // Neon Pink Label
    canvas.drawCircle(0f, 0f, r * 0.35f, sharedPaint)

    // Center Spindle Hole
    sharedPaint.color = Color.rgb(15, 23, 42)
    canvas.drawCircle(0f, 0f, r * 0.08f, sharedPaint)
    canvas.restoreToCount(saveLabel)

    canvas.restoreToCount(saveCount)
  }
}

