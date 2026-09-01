package com.example.characters

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

/**
 * High-performance 3D-Styled Pixel Art and Multi-Frame Sprite Renderer for Walkbar.
 *
 * Implements crisp nearest-neighbor multi-frame sprite sampling with 3D isometric shading,
 * specular edge glints, subpixel animation, and dynamic walk states across Tiny/Small/Medium/Large presets.
 */
object PixelArtRenderer {

  private val pixelPaint = Paint().apply {
    isAntiAlias = false
    isFilterBitmap = false
    style = Paint.Style.FILL
  }

  private val glowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
    style = Paint.Style.FILL
  }

  private val strokePaint = Paint().apply {
    isAntiAlias = false
    style = Paint.Style.STROKE
  }

  // =========================================================================
  // 1. 3D-STYLED PIXEL ROBOT (Multi-Frame Mecha Walk & Hydraulic Strides)
  // =========================================================================
  fun draw3DRobot(
    canvas: Canvas,
    size: Float,
    angle: Float,
    isBlinking: Boolean,
    currentTimeMs: Long
  ) {
    // 4-frame walk cycle phase calculation
    val normPhase = ((angle / (2f * PI.toFloat())) % 1.0f).let { if (it < 0) it + 1f else it }
    val frameIndex = (normPhase * 4f).toInt() % 4
    val bobY = -size * 0.48f + abs(sin(angle * 2f)) * (size * 0.04f)

    val grid = 16
    val pixelSize = size / grid.toFloat()
    val startX = -size * 0.5f
    val startY = bobY - size * 0.45f

    // 3D Metallic Palette
    val cPlateLight = Color.rgb(14, 165, 233)   // Highlight cyan
    val cPlateMain = Color.rgb(2, 132, 199)    // Main body cyan
    val cPlateDark = Color.rgb(3, 105, 161)    // Shadow cyan
    val cJointDark = Color.rgb(30, 41, 59)     // Dark mecha joint
    val cVisorGlow = if (isBlinking) Color.rgb(15, 23, 42) else Color.rgb(6, 182, 212)
    val cVisorLight = if (isBlinking) Color.rgb(30, 41, 59) else Color.rgb(224, 242, 254)
    val cAntennaCore = Color.rgb(244, 63, 94)  // Pulsing antenna ruby LED
    val cGleam = Color.rgb(255, 255, 255)      // 3D Specular glint

    // Antenna pulse
    val antennaPulse = (sin(currentTimeMs / 120.0).toFloat() * 0.5f + 0.5f)
    val antennaGlowAlpha = (60 + (antennaPulse * 140)).toInt().coerceIn(0, 255)

    // Draw antenna glow
    glowPaint.color = Color.argb(antennaGlowAlpha, 244, 63, 94)
    canvas.drawCircle(0f, startY + pixelSize * 1.5f, pixelSize * 2.2f, glowPaint)

    // 4 Walk-Cycle Frame Patterns for 3D Pixel Robot
    // Legend:
    // . = transparent
    // A = Antenna tip (pulsing ruby)
    // a = Antenna stem
    // 1 = Light plate (top-left 3D light source)
    // 2 = Main plate
    // 3 = Dark plate (bottom-right 3D shadow)
    // V = Cyan Visor Glow
    // v = Visor highlight glint
    // J = Dark Joint / Core
    // L = Left Leg / Foot
    // R = Right Leg / Foot
    // H = Hand / Arm

    val framePattern = when (frameIndex) {
      0 -> arrayOf(
        ".......A........",
        ".......a........",
        "....1111111.....",
        "...112222223....",
        "...12VVvvVV3....",
        "...12VVVVVV3....",
        "...122222223....",
        "....3333333.....",
        "...112222223....",
        "...H12222223H...",
        "...H12222223H...",
        "....3333333.....",
        "....JJ...JJ.....",
        "...123...123....",
        "...123...123....",
        "..11233.11233..."
      )
      1 -> arrayOf(
        ".......A........",
        ".......a........",
        "....1111111.....",
        "...112222223....",
        "...12VVvvVV3....",
        "...12VVVVVV3....",
        "...122222223....",
        "....3333333.....",
        "...112222223....",
        "..H112222223....",
        "..H112222223H...",
        "....3333333.H...",
        "....JJ...JJ.....",
        "....123.123.....",
        "...1123..123....",
        "...11233..1233.."
      )
      2 -> arrayOf(
        ".......A........",
        ".......a........",
        "....1111111.....",
        "...112222223....",
        "...12VVvvVV3....",
        "...12VVVVVV3....",
        "...122222223....",
        "....3333333.....",
        "...112222223....",
        "...H12222223H...",
        "...H12222223H...",
        "....3333333.....",
        "....JJ...JJ.....",
        "...123...123....",
        "...123...123....",
        "..11233.11233..."
      )
      else -> arrayOf(
        ".......A........",
        ".......a........",
        "....1111111.....",
        "...112222223....",
        "...12VVvvVV3....",
        "...12VVVVVV3....",
        "...122222223....",
        "....3333333.....",
        "...112222223....",
        "....11222223H...",
        "..H.11222223H...",
        "..H.3333333.....",
        "....JJ...JJ.....",
        ".....123.123....",
        "....123..1123...",
        "..11233...11233."
      )
    }

    renderPixelMatrix(canvas, framePattern, startX, startY, pixelSize) { ch ->
      when (ch) {
        'A' -> cAntennaCore
        'a' -> cJointDark
        '1' -> cPlateLight
        '2' -> cPlateMain
        '3' -> cPlateDark
        'V' -> cVisorGlow
        'v' -> cVisorLight
        'J' -> cJointDark
        'H' -> cPlateLight
        'L', 'R' -> cPlateMain
        else -> null
      }
    }
  }

  // =========================================================================
  // 2. 3D-STYLED PIXEL ASTRONAUT (Cosmo Spaceman with Gold Visor Reflection)
  // =========================================================================
  fun draw3DAstronaut(
    canvas: Canvas,
    size: Float,
    angle: Float,
    currentTimeMs: Long
  ) {
    val normPhase = ((angle / (2f * PI.toFloat())) % 1.0f).let { if (it < 0) it + 1f else it }
    val frameIndex = (normPhase * 4f).toInt() % 4
    val floatBob = -size * 0.50f + sin(angle * 1.5f) * (size * 0.05f)

    val grid = 16
    val pixelSize = size / grid.toFloat()
    val startX = -size * 0.5f
    val startY = floatBob - size * 0.44f

    // 3D Space Suit Palette
    val cSuitLight = Color.rgb(248, 250, 252)  // Crisp white highlight
    val cSuitMain = Color.rgb(226, 232, 240)   // Off-white suit
    val cSuitDark = Color.rgb(148, 163, 184)   // Shadow slate
    val cVisorGold = Color.rgb(245, 158, 11)   // Reflective gold helmet visor
    val cVisorGlint = Color.rgb(254, 243, 199)  // Specular light reflection
    val cPackDark = Color.rgb(71, 85, 105)     // Oxygen backpack
    val cBadgeRed = Color.rgb(239, 68, 68)     // Mission badge

    val framePattern = when (frameIndex) {
      0 -> arrayOf(
        "....1111111.....",
        "...112222223....",
        "...12GGggGG3....",
        "..P12GGGGGG3....",
        "..P12GGGGGG3....",
        "...122222223....",
        "....3333333.....",
        "..P.11R22223....",
        "..P112222223....",
        "..P122222223....",
        "....3333333.....",
        "....123.123.....",
        "...1123.123.....",
        "...1123.1123....",
        "...1123.1123....",
        "..111233111233.."
      )
      1 -> arrayOf(
        "....1111111.....",
        "...112222223....",
        "...12GGggGG3....",
        "..P12GGGGGG3....",
        "..P12GGGGGG3....",
        "...122222223....",
        "....3333333.....",
        "..P.11R22223....",
        "..P112222223....",
        "..P1222222233...",
        "....3333333.3...",
        "....123..123....",
        "...1123..1123...",
        "...1123...1123..",
        "..111233...11233",
        "................"
      )
      2 -> arrayOf(
        "....1111111.....",
        "...112222223....",
        "...12GGggGG3....",
        "..P12GGGGGG3....",
        "..P12GGGGGG3....",
        "...122222223....",
        "....3333333.....",
        "..P.11R22223....",
        "..P112222223....",
        "..P122222223....",
        "....3333333.....",
        "....123.123.....",
        "...1123.123.....",
        "...1123.1123....",
        "...1123.1123....",
        "..111233111233.."
      )
      else -> arrayOf(
        "....1111111.....",
        "...112222223....",
        "...12GGggGG3....",
        "..P12GGGGGG3....",
        "..P12GGGGGG3....",
        "...122222223....",
        "....3333333.....",
        "..P.11R22223....",
        "..P112222223....",
        ".3.122222223....",
        ".3..3333333.....",
        "....123..123....",
        "...1123..1123...",
        "..1123....1123..",
        ".111233..111233.",
        "................"
      )
    }

    renderPixelMatrix(canvas, framePattern, startX, startY, pixelSize) { ch ->
      when (ch) {
        '1' -> cSuitLight
        '2' -> cSuitMain
        '3' -> cSuitDark
        'G' -> cVisorGold
        'g' -> cVisorGlint
        'P' -> cPackDark
        'R' -> cBadgeRed
        else -> null
      }
    }
  }

  // =========================================================================
  // 3. 3D-STYLED PIXEL NINJA (Shadow Shinobi with Flowing Crimson Ribbon)
  // =========================================================================
  fun draw3DNinja(
    canvas: Canvas,
    size: Float,
    angle: Float,
    isBlinking: Boolean
  ) {
    val normPhase = ((angle / (2f * PI.toFloat())) % 1.0f).let { if (it < 0) it + 1f else it }
    val frameIndex = (normPhase * 4f).toInt() % 4
    val bobY = -size * 0.46f + abs(sin(angle * 2f)) * (size * 0.035f)

    val grid = 16
    val pixelSize = size / grid.toFloat()
    val startX = -size * 0.5f
    val startY = bobY - size * 0.44f

    // Ninja Palette
    val cLightShade = Color.rgb(51, 65, 85)   // Slate highlight
    val cMainSuit = Color.rgb(15, 23, 42)     // Obsidian midnight suit
    val cDarkShade = Color.rgb(2, 6, 23)      // Pure shadow
    val cHeadbandRed = Color.rgb(239, 68, 68) // Crimson headband & tail
    val cHeadbandLight = Color.rgb(252, 165, 165)
    val cSkin = Color.rgb(254, 215, 170)      // Skin eye slit
    val cEyeGlow = if (isBlinking) cSkin else Color.rgb(255, 255, 255)
    val cKatanaSteel = Color.rgb(203, 213, 225)

    // Flowing ribbon tail behind
    val ribbonWave = sin(angle * 2.5f) * pixelSize * 1.5f

    val framePattern = when (frameIndex) {
      0 -> arrayOf(
        ".....RRRRRRR....",
        "....1RRRRRRR3...",
        "...1122222233...",
        "..R12SSsEEs23...",
        ".R.1222222233...",
        "....122222233...",
        "....K12222233...",
        "...KK11222223...",
        "..K.112222223...",
        "....122222223...",
        ".....33333333...",
        "....123...123...",
        "...1123...1123..",
        "...1123...1123..",
        "..111233.111233.",
        "................"
      )
      1 -> arrayOf(
        ".....RRRRRRR....",
        "....1RRRRRRR3...",
        "...1122222233...",
        "..R12SSsEEs23...",
        ".r.1222222233...",
        "....122222233...",
        "....K12222233...",
        "...KK11222223...",
        "..K.112222223.3.",
        "....122222223.3.",
        ".....33333333...",
        "....123....123..",
        "...1123...1123..",
        "..111233...1123.",
        "..........111233",
        "................"
      )
      2 -> arrayOf(
        ".....RRRRRRR....",
        "....1RRRRRRR3...",
        "...1122222233...",
        "..R12SSsEEs23...",
        ".R.1222222233...",
        "....122222233...",
        "....K12222233...",
        "...KK11222223...",
        "..K.112222223...",
        "....122222223...",
        ".....33333333...",
        "....123...123...",
        "...1123...1123..",
        "...1123...1123..",
        "..111233.111233.",
        "................"
      )
      else -> arrayOf(
        ".....RRRRRRR....",
        "....1RRRRRRR3...",
        "...1122222233...",
        "..R12SSsEEs23...",
        ".r.1222222233...",
        "....122222233...",
        "....K12222233...",
        "...KK11222223...",
        "..K3112222223...",
        "..3.122222223...",
        ".....33333333...",
        "....123....123..",
        "...1123...1123..",
        "..111233.111233.",
        "................",
        "................"
      )
    }

    renderPixelMatrix(canvas, framePattern, startX, startY, pixelSize) { ch ->
      when (ch) {
        '1' -> cLightShade
        '2' -> cMainSuit
        '3' -> cDarkShade
        'R' -> cHeadbandRed
        'r' -> cHeadbandLight
        'S' -> cSkin
        's' -> cSkin
        'E' -> cEyeGlow
        'K' -> cKatanaSteel
        else -> null
      }
    }
  }

  // =========================================================================
  // 4. 3D-STYLED PIXEL DINO RUNNER
  // =========================================================================
  fun drawPixelDino(
    canvas: Canvas,
    size: Float,
    angle: Float,
    isBlinking: Boolean
  ) {
    val normPhase = ((angle / (2f * PI.toFloat())) % 1.0f).let { if (it < 0) it + 1f else it }
    val frameIndex = (normPhase * 4f).toInt() % 4
    val hop = abs(sin(angle * 2f)) * (size * 0.06f)

    val grid = 16
    val pixelSize = size / grid.toFloat()
    val startX = -size * 0.5f
    val startY = -size * 0.92f - hop

    // 3D Emerald Arcade Palette
    val cScaleLight = Color.rgb(74, 222, 128)  // Bright top scales
    val cScaleMain = Color.rgb(34, 197, 94)   // Main green scales
    val cScaleDark = Color.rgb(21, 128, 61)   // Dark underbelly shadow
    val cBellyLight = Color.rgb(187, 247, 208) // Cream yellow-green belly
    val cEye = if (isBlinking) cScaleMain else Color.rgb(255, 255, 255)
    val cPupil = Color.rgb(15, 23, 42)

    val framePattern = when (frameIndex) {
      0 -> arrayOf(
        "........111111..",
        ".......11122223.",
        ".......122EPE223",
        ".......122222223",
        ".......122222...",
        ".......1122233..",
        "1.....1122223...",
        "11...11222223...",
        "111.11222223....",
        ".1111122223.....",
        "..111122233.....",
        "...1122233......",
        "....122233......",
        ".....12..12.....",
        ".....12..12.....",
        "....112.112....."
      )
      1 -> arrayOf(
        "........111111..",
        ".......11122223.",
        ".......122EPE223",
        ".......122222223",
        ".......122222...",
        ".......1122233..",
        "1.....1122223...",
        "11...11222223...",
        "111.11222223....",
        ".1111122223.....",
        "..111122233.....",
        "...1122233......",
        "....122233......",
        ".....12...12....",
        "....112....12...",
        "....112...112...",
      )
      2 -> arrayOf(
        "........111111..",
        ".......11122223.",
        ".......122EPE223",
        ".......122222223",
        ".......122222...",
        ".......1122233..",
        "1.....1122223...",
        "11...11222223...",
        "111.11222223....",
        ".1111122223.....",
        "..111122233.....",
        "...1122233......",
        "....122233......",
        ".....12..12.....",
        ".....12..12.....",
        "....112.112....."
      )
      else -> arrayOf(
        "........111111..",
        ".......11122223.",
        ".......122EPE223",
        ".......122222223",
        ".......122222...",
        ".......1122233..",
        "1.....1122223...",
        "11...11222223...",
        "111.11222223....",
        ".1111122223.....",
        "..111122233.....",
        "...1122233......",
        "....122233......",
        "....12...12.....",
        "...112...12.....",
        "...112..112....."
      )
    }

    renderPixelMatrix(canvas, framePattern, startX, startY, pixelSize) { ch ->
      when (ch) {
        '1' -> cScaleLight
        '2' -> cScaleMain
        '3' -> cScaleDark
        'E' -> cEye
        'P' -> cPupil
        else -> null
      }
    }
  }

  // =========================================================================
  // 5. 8-BIT PULSATING HEART
  // =========================================================================
  fun drawPixelHeart(canvas: Canvas, size: Float, angle: Float) {
    val pulse = (sin(angle * 3f) * 0.08f)
    val actualSize = size * (1f + pulse)
    val grid = 12
    val pixelSize = actualSize / grid.toFloat()
    val startX = -actualSize * 0.5f
    val startY = -actualSize * 0.85f

    val cCore = Color.rgb(239, 68, 68)
    val cDark = Color.rgb(185, 28, 28)
    val cShine = Color.rgb(254, 202, 202)

    val pattern = arrayOf(
      "..11..11..",
      ".13311331.",
      "1333333331",
      "1322333331",
      "1333333331",
      ".13333331.",
      "..133331..",
      "...1331...",
      "....11...."
    )

    glowPaint.color = Color.argb(45, 239, 68, 68)
    canvas.drawCircle(0f, startY + actualSize * 0.4f, actualSize * 0.45f, glowPaint)

    renderPixelMatrix(canvas, pattern, startX, startY, pixelSize) { ch ->
      when (ch) {
        '1' -> cDark
        '2' -> cShine
        '3' -> cCore
        else -> null
      }
    }
  }

  // =========================================================================
  // 6. ARCADE GHOST
  // =========================================================================
  fun drawPixelGhost(canvas: Canvas, size: Float, angle: Float) {
    val floatY = -size * 0.5f + sin(angle * 2f) * (size * 0.05f)
    val legSwing = (sin(angle * 3f) > 0)
    val eyeLook = (cos(angle) * 1.5f).toInt()

    val grid = 14
    val pixelSize = size / grid.toFloat()
    val startX = -size * 0.5f
    val startY = floatY - size * 0.45f

    val cBody = Color.rgb(236, 72, 153)
    val cLight = Color.rgb(244, 114, 182)
    val cEye = Color.WHITE
    val cPupil = Color.rgb(30, 58, 138)

    val pattern = arrayOf(
      "....111111....",
      "..1122222211..",
      ".122222222221.",
      "122EE2222EE221",
      "122PP2222PP221",
      "12222222222221",
      "12222222222221",
      "12222222222221",
      if (legSwing) "1.11.11.11.1.1" else ".1.11.11.11.1."
    )

    renderPixelMatrix(canvas, pattern, startX, startY, pixelSize) { ch ->
      when (ch) {
        '1' -> cBody
        '2' -> cLight
        'E' -> cEye
        'P' -> cPupil
        else -> null
      }
    }
  }

  // =========================================================================
  // 7. MANA POTION
  // =========================================================================
  fun drawPixelPotion(canvas: Canvas, size: Float, angle: Float) {
    val bobY = -size * 0.48f + sin(angle) * (size * 0.03f)
    val grid = 12
    val pixelSize = size / grid.toFloat()
    val startX = -size * 0.5f
    val startY = bobY - size * 0.45f

    val cCork = Color.rgb(180, 83, 9)
    val cGlass = Color.rgb(224, 242, 254)
    val cLiquid = Color.rgb(16, 185, 129)
    val cBubble = Color.rgb(167, 243, 208)

    val bubbleShift = (sin(angle * 3f) > 0)
    val pattern = arrayOf(
      "....1111....",
      ".....11.....",
      "....2222....",
      "...233332...",
      "..23343332..",
      "..23333432..",
      "..23433332..",
      "..23333332..",
      "...222222..."
    )

    glowPaint.color = Color.argb(40, 16, 185, 129)
    canvas.drawCircle(0f, startY + size * 0.5f, size * 0.45f, glowPaint)

    renderPixelMatrix(canvas, pattern, startX, startY, pixelSize) { ch ->
      when (ch) {
        '1' -> cCork
        '2' -> cGlass
        '3' -> cLiquid
        '4' -> cBubble
        else -> null
      }
    }
  }

  // =========================================================================
  // 8. PIXEL CAT
  // =========================================================================
  fun drawPixelCat(canvas: Canvas, size: Float, angle: Float) {
    val hop = abs(sin(angle * 2f)) * (size * 0.05f)
    val legSwing = (sin(angle * 2f) > 0)
    val grid = 14
    val pixelSize = size / grid.toFloat()
    val startX = -size * 0.5f
    val startY = -size * 0.85f - hop

    val cBody = Color.rgb(249, 115, 22)
    val cWhite = Color.rgb(254, 215, 170)
    val cEye = Color.rgb(34, 197, 94)

    val pattern = arrayOf(
      "1.....1.......",
      "11...11.......",
      "1311131.......",
      "1111111111....",
      ".1111111111.1.",
      "..11111111111.",
      "..1221111111..",
      "..111111111...",
      if (legSwing) "...1..1...1..." else "....1...1..1.."
    )

    renderPixelMatrix(canvas, pattern, startX, startY, pixelSize) { ch ->
      when (ch) {
        '1' -> cBody
        '2' -> cWhite
        '3' -> cEye
        else -> null
      }
    }
  }

  // =========================================================================
  // 9. PIXEL COIN
  // =========================================================================
  fun drawPixelCoin(canvas: Canvas, size: Float, angle: Float) {
    val phaseWidth = (abs(cos(angle))).coerceIn(0.2f, 1f)
    val grid = 10
    val pixelSize = size / grid.toFloat()
    val startX = -size * 0.5f * phaseWidth
    val startY = -size * 0.65f

    val cGold = Color.rgb(234, 179, 8)
    val cDarkGold = Color.rgb(161, 98, 7)
    val cLight = Color.rgb(254, 240, 138)

    val pattern = arrayOf(
      "..1111..",
      ".133331.",
      "13111131",
      "13122131",
      "13122131",
      "13111131",
      ".133331.",
      "..1111.."
    )

    glowPaint.color = Color.argb(40, 234, 179, 8)
    canvas.drawCircle(0f, startY + size * 0.4f, size * 0.45f * phaseWidth, glowPaint)

    for (r in pattern.indices) {
      val rowStr = pattern[r]
      for (c in rowStr.indices) {
        val ch = rowStr[c]
        if (ch == '.') continue
        val px = startX + c * (pixelSize * phaseWidth)
        val py = startY + r * pixelSize

        pixelPaint.color = when (ch) {
          '1' -> cDarkGold
          '2' -> cLight
          '3' -> cGold
          else -> cGold
        }
        canvas.drawRect(px, py, px + (pixelSize * phaseWidth), py + pixelSize, pixelPaint)
      }
    }
  }

  private inline fun renderPixelMatrix(
    canvas: Canvas,
    pattern: Array<String>,
    startX: Float,
    startY: Float,
    pixelSize: Float,
    colorMapper: (Char) -> Int?
  ) {
    for (r in pattern.indices) {
      val rowStr = pattern[r]
      for (c in rowStr.indices) {
        val ch = rowStr[c]
        if (ch == '.') continue
        val color = colorMapper(ch) ?: continue
        val px = startX + c * pixelSize
        val py = startY + r * pixelSize
        pixelPaint.color = color
        canvas.drawRect(px, py, px + pixelSize, py + pixelSize, pixelPaint)
      }
    }
  }
}
