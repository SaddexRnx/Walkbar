package com.example.characters

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import com.example.characters.ThreeDMath.Face3D
import com.example.characters.ThreeDMath.Vec3
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

/**
 * Procedural 3D Object Generator & Renderer.
 * Generates dynamic 3D polyhedra meshes and renders them with lighting & depth sorting.
 */
object ThreeDObjectRenderer {

  private val sharedPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
    style = Paint.Style.FILL
  }
  private val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
    style = Paint.Style.STROKE
    strokeCap = Paint.Cap.ROUND
    strokeJoin = Paint.Join.ROUND
  }
  private val glowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
    style = Paint.Style.FILL
  }
  private val reusablePath = Path()

  // --- 1. 3D DIAMOND / GEM ---
  fun draw3DDiamond(
    canvas: Canvas,
    size: Float,
    angle: Float,
    currentTimeMs: Long
  ) {
    val hoverY = -size * 0.55f + sin(angle) * (size * 0.06f)
    val pitch = 0.35f + sin(angle * 0.5f) * 0.1f
    val yaw = angle * 1.2f
    val roll = sin(angle * 0.7f) * 0.1f

    val saveCount = canvas.save()
    canvas.translate(0f, hoverY)

    // Dynamic Glow
    glowPaint.color = Color.argb(45, 99, 102, 241) // Indigo Glow
    canvas.drawCircle(0f, 0f, size * 0.45f, glowPaint)

    // Build Gem Geometry
    val faces = mutableListOf<Face3D>()
    val n = 8 // Octagonal gem
    val topY = 0.45f
    val midY = 0.15f
    val botY = -0.55f
    val rTop = 0.28f
    val rMid = 0.55f

    val topPoints = mutableListOf<Vec3>()
    val midPoints = mutableListOf<Vec3>()

    for (i in 0 until n) {
      val theta = (i * 2f * PI / n).toFloat()
      topPoints.add(Vec3(cos(theta) * rTop, topY, sin(theta) * rTop))
      midPoints.add(Vec3(cos(theta) * rMid, midY, sin(theta) * rMid))
    }
    val botPoint = Vec3(0f, botY, 0f)

    // 1. Top Table Face
    faces.add(
      Face3D(
        vertices = topPoints.reversed(),
        baseColor = Color.rgb(224, 231, 255), // Crystal light
        specular = 0.8f
      )
    )

    // 2. Crown Upper Facets (Quads between top and mid)
    val gemBaseColor = Color.rgb(99, 102, 241)
    val gemAccentColor = Color.rgb(129, 140, 248)

    for (i in 0 until n) {
      val next = (i + 1) % n
      val color = if (i % 2 == 0) gemBaseColor else gemAccentColor
      faces.add(
        Face3D(
          vertices = listOf(topPoints[i], topPoints[next], midPoints[next], midPoints[i]),
          baseColor = color,
          specular = 0.7f
        )
      )
    }

    // 3. Pavilion Lower Facets (Triangles down to bottom point)
    for (i in 0 until n) {
      val next = (i + 1) % n
      val color = if (i % 2 == 0) Color.rgb(67, 56, 202) else Color.rgb(79, 70, 229)
      faces.add(
        Face3D(
          vertices = listOf(midPoints[i], midPoints[next], botPoint),
          baseColor = color,
          specular = 0.9f
        )
      )
    }

    ThreeDMath.renderFaces(
      canvas = canvas,
      faces = faces,
      pitch = pitch,
      yaw = yaw,
      roll = roll,
      scale = size * 0.9f,
      paint = sharedPaint,
      path = reusablePath
    )

    canvas.restoreToCount(saveCount)
  }

  // --- 2. 3D GOLD COIN / TOKEN ---
  fun draw3DGoldCoin(
    canvas: Canvas,
    size: Float,
    angle: Float,
    currentTimeMs: Long
  ) {
    val hoverY = -size * 0.5f + sin(angle * 1.5f) * (size * 0.05f)
    val pitch = 0.2f
    val yaw = angle * 1.8f
    val roll = sin(angle * 0.8f) * 0.1f

    val saveCount = canvas.save()
    canvas.translate(0f, hoverY)

    // Gold Glow
    glowPaint.color = Color.argb(45, 234, 179, 8)
    canvas.drawCircle(0f, 0f, size * 0.42f, glowPaint)

    val faces = mutableListOf<Face3D>()
    val n = 16
    val radius = 0.45f
    val halfThick = 0.08f

    val frontPoints = mutableListOf<Vec3>()
    val backPoints = mutableListOf<Vec3>()

    for (i in 0 until n) {
      val theta = (i * 2f * PI / n).toFloat()
      val x = cos(theta) * radius
      val y = sin(theta) * radius
      frontPoints.add(Vec3(x, y, -halfThick))
      backPoints.add(Vec3(x, y, halfThick))
    }

    val goldMain = Color.rgb(234, 179, 8)
    val goldRim = Color.rgb(202, 138, 4)
    val goldDark = Color.rgb(161, 98, 7)

    // Front Face (Cap)
    faces.add(
      Face3D(
        vertices = frontPoints,
        baseColor = goldMain,
        specular = 0.6f
      )
    )

    // Back Face (Cap)
    faces.add(
      Face3D(
        vertices = backPoints.reversed(),
        baseColor = goldDark,
        specular = 0.5f
      )
    )

    // Cylindrical Rim Facets
    for (i in 0 until n) {
      val next = (i + 1) % n
      faces.add(
        Face3D(
          vertices = listOf(frontPoints[i], frontPoints[next], backPoints[next], backPoints[i]),
          baseColor = if (i % 2 == 0) goldRim else goldDark,
          specular = 0.75f
        )
      )
    }

    ThreeDMath.renderFaces(
      canvas = canvas,
      faces = faces,
      pitch = pitch,
      yaw = yaw,
      roll = roll,
      scale = size * 0.95f,
      paint = sharedPaint,
      path = reusablePath
    )

    canvas.restoreToCount(saveCount)
  }

  // --- 3. 3D HOLOGRAM CUBE ---
  fun draw3DCube(
    canvas: Canvas,
    size: Float,
    angle: Float
  ) {
    val hoverY = -size * 0.52f + sin(angle) * (size * 0.07f)
    val pitch = 0.55f + sin(angle * 0.6f) * 0.15f
    val yaw = angle * 1.3f
    val roll = angle * 0.5f

    val saveCount = canvas.save()
    canvas.translate(0f, hoverY)

    glowPaint.color = Color.argb(45, 6, 182, 212) // Cyan Glow
    canvas.drawCircle(0f, 0f, size * 0.45f, glowPaint)

    val s = 0.35f
    // 8 vertices of cube
    val v0 = Vec3(-s, -s, -s)
    val v1 = Vec3(s, -s, -s)
    val v2 = Vec3(s, s, -s)
    val v3 = Vec3(-s, s, -s)
    val v4 = Vec3(-s, -s, s)
    val v5 = Vec3(s, -s, s)
    val v6 = Vec3(s, s, s)
    val v7 = Vec3(-s, s, s)

    val cCyan = Color.argb(200, 6, 182, 212)
    val cBlue = Color.argb(200, 59, 130, 246)
    val cPurple = Color.argb(200, 168, 85, 247)

    val faces = listOf(
      Face3D(listOf(v0, v1, v2, v3), cCyan, specular = 0.8f),   // Front
      Face3D(listOf(v5, v4, v7, v6), cBlue, specular = 0.8f),   // Back
      Face3D(listOf(v4, v0, v3, v7), cPurple, specular = 0.8f), // Left
      Face3D(listOf(v1, v5, v6, v2), cCyan, specular = 0.8f),   // Right
      Face3D(listOf(v3, v2, v6, v7), cBlue, specular = 0.8f),   // Top
      Face3D(listOf(v4, v5, v1, v0), cPurple, specular = 0.8f)  // Bottom
    )

    ThreeDMath.renderFaces(
      canvas = canvas,
      faces = faces,
      pitch = pitch,
      yaw = yaw,
      roll = roll,
      scale = size * 0.95f,
      paint = sharedPaint,
      path = reusablePath
    )

    canvas.restoreToCount(saveCount)
  }

  // --- 4. 3D PLANET SATURN WITH RINGS ---
  fun draw3DSaturn(
    canvas: Canvas,
    size: Float,
    angle: Float
  ) {
    val hoverY = -size * 0.5f + sin(angle) * (size * 0.05f)
    val planetTilt = 0.45f
    val spin = angle * 0.8f

    val saveCount = canvas.save()
    canvas.translate(0f, hoverY)

    val planetRadius = size * 0.28f

    // Atmosphere Glow
    glowPaint.color = Color.argb(50, 245, 158, 11)
    canvas.drawCircle(0f, 0f, planetRadius * 1.5f, glowPaint)

    // Draw Back half of Ring (Behind Planet)
    val ringMajor = size * 0.62f
    val ringMinor = ringMajor * 0.32f

    val ringPathBack = Path()
    ringPathBack.addOval(
      RectF(-ringMajor, -ringMinor, ringMajor, ringMinor),
      Path.Direction.CW
    )
    val saveRing = canvas.save()
    canvas.rotate(-22f)

    strokePaint.style = Paint.Style.STROKE
    strokePaint.strokeWidth = size * 0.11f
    strokePaint.color = Color.rgb(217, 119, 6) // Amber ring
    canvas.drawPath(ringPathBack, strokePaint)

    strokePaint.strokeWidth = size * 0.04f
    strokePaint.color = Color.rgb(254, 243, 199) // Ring bright groove
    canvas.drawPath(ringPathBack, strokePaint)
    canvas.restoreToCount(saveRing)

    // Draw Planet Sphere
    sharedPaint.color = Color.rgb(245, 158, 11) // Golden planet
    canvas.drawCircle(0f, 0f, planetRadius, sharedPaint)

    // Latitude bands
    val bandPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
      style = Paint.Style.FILL
      color = Color.rgb(217, 119, 6)
    }
    canvas.save()
    canvas.clipPath(Path().apply { addCircle(0f, 0f, planetRadius, Path.Direction.CW) })
    canvas.drawRect(-planetRadius, -planetRadius * 0.35f, planetRadius, -planetRadius * 0.1f, bandPaint)
    canvas.drawRect(-planetRadius, planetRadius * 0.15f, planetRadius, planetRadius * 0.45f, bandPaint)

    // 3D Sphere Spherical Terminator Shadow
    val sphereShade = Paint(Paint.ANTI_ALIAS_FLAG).apply {
      style = Paint.Style.FILL
      color = Color.argb(120, 15, 23, 42) // Dark terminator
    }
    canvas.drawCircle(planetRadius * 0.35f, planetRadius * 0.35f, planetRadius * 1.05f, sphereShade)
    canvas.restore()

    // Draw Front half of Ring (Over Planet)
    val saveRingFront = canvas.save()
    canvas.rotate(-22f)
    canvas.clipRect(-ringMajor * 1.2f, 0f, ringMajor * 1.2f, ringMinor * 1.5f) // Clip lower/front half

    strokePaint.strokeWidth = size * 0.11f
    strokePaint.color = Color.rgb(217, 119, 6)
    canvas.drawPath(ringPathBack, strokePaint)

    strokePaint.strokeWidth = size * 0.04f
    strokePaint.color = Color.rgb(254, 243, 199)
    canvas.drawPath(ringPathBack, strokePaint)

    canvas.restoreToCount(saveRingFront)
    canvas.restoreToCount(saveCount)
  }

  // --- 5. 3D FLYING UFO / SAUCER ---
  fun draw3DUFO(
    canvas: Canvas,
    size: Float,
    angle: Float,
    currentTimeMs: Long
  ) {
    val hoverY = -size * 0.58f + sin(angle * 1.8f) * (size * 0.08f)
    val tilt = sin(angle) * 8f
    val beamPulse = abs(sin(angle * 3f))

    val saveCount = canvas.save()
    canvas.translate(0f, hoverY)
    canvas.rotate(tilt)

    // 1. Tractor Beam (Pulsing cone)
    val beamPath = Path().apply {
      moveTo(-size * 0.15f, size * 0.1f)
      lineTo(size * 0.15f, size * 0.1f)
      lineTo(size * 0.38f, size * 0.75f)
      lineTo(-size * 0.38f, size * 0.75f)
      close()
    }
    val beamAlpha = (60 + beamPulse * 80).toInt()
    sharedPaint.color = Color.argb(beamAlpha, 34, 197, 94) // Emerald Tractor Beam
    canvas.drawPath(beamPath, sharedPaint)

    // 2. Metallic Lower Saucer Hull
    val hullRadiusX = size * 0.48f
    val hullRadiusY = size * 0.16f

    sharedPaint.color = Color.rgb(100, 116, 139) // Slate metal
    canvas.drawOval(RectF(-hullRadiusX, -hullRadiusY, hullRadiusX, hullRadiusY), sharedPaint)

    // Middle Glowing Rim Ring
    strokePaint.style = Paint.Style.STROKE
    strokePaint.strokeWidth = 3f
    strokePaint.color = Color.rgb(6, 182, 212)
    canvas.drawOval(RectF(-hullRadiusX * 0.95f, -hullRadiusY * 0.95f, hullRadiusX * 0.95f, hullRadiusY * 0.95f), strokePaint)

    // 3. Rotating Navigation Lights along the rim
    val numLights = 6
    val lightAngleOffset = (currentTimeMs % 2000L).toFloat() / 2000f * 2f * PI.toFloat()
    for (i in 0 until numLights) {
      val theta = (i * 2f * PI / numLights).toFloat() + lightAngleOffset
      val lx = cos(theta) * (hullRadiusX * 0.85f)
      val ly = sin(theta) * (hullRadiusY * 0.85f)
      val isFront = sin(theta) > 0

      val lightColor = if (i % 2 == 0) Color.rgb(239, 68, 68) else Color.rgb(34, 197, 94)
      sharedPaint.color = if (isFront) lightColor else Color.rgb(71, 85, 105)
      canvas.drawCircle(lx, ly, 3.5f, sharedPaint)
    }

    // 4. Glowing Glass Cabin Dome
    val domeRadiusX = size * 0.24f
    val domeRadiusY = size * 0.22f
    sharedPaint.color = Color.argb(220, 6, 182, 212) // Cyan glass
    canvas.drawOval(RectF(-domeRadiusX, -domeRadiusY - size * 0.06f, domeRadiusX, size * 0.04f), sharedPaint)

    // Alien pilot silhouette inside dome!
    sharedPaint.color = Color.rgb(15, 23, 42)
    canvas.drawCircle(0f, -size * 0.12f, size * 0.06f, sharedPaint)
    sharedPaint.color = Color.rgb(34, 197, 94)
    canvas.drawCircle(-3f, -size * 0.12f, 1.5f, sharedPaint)
    canvas.drawCircle(3f, -size * 0.12f, 1.5f, sharedPaint)

    canvas.restoreToCount(saveCount)
  }

  // --- 6. 3D ROTATING STAR ---
  fun draw3DStar(
    canvas: Canvas,
    size: Float,
    angle: Float
  ) {
    val hoverY = -size * 0.52f + sin(angle) * (size * 0.05f)
    val pitch = 0.25f
    val yaw = angle * 1.5f
    val roll = sin(angle * 0.5f) * 0.1f

    val saveCount = canvas.save()
    canvas.translate(0f, hoverY)

    glowPaint.color = Color.argb(55, 234, 179, 8) // Star gold glow
    canvas.drawCircle(0f, 0f, size * 0.45f, glowPaint)

    val faces = mutableListOf<Face3D>()
    val numPoints = 5
    val outerR = 0.52f
    val innerR = 0.24f
    val depth = 0.14f

    val centerFront = Vec3(0f, 0f, -depth)
    val centerBack = Vec3(0f, 0f, depth)

    val starVertices = mutableListOf<Vec3>()
    for (i in 0 until numPoints * 2) {
      val r = if (i % 2 == 0) outerR else innerR
      val theta = (i * PI / numPoints).toFloat() - (PI / 2f).toFloat()
      starVertices.add(Vec3(cos(theta) * r, -sin(theta) * r, 0f))
    }

    val cGold1 = Color.rgb(250, 204, 21)
    val cGold2 = Color.rgb(234, 179, 8)
    val cGold3 = Color.rgb(202, 138, 4)

    val totalV = starVertices.size
    for (i in 0 until totalV) {
      val next = (i + 1) % totalV
      // Front faceted pyramids
      faces.add(
        Face3D(
          vertices = listOf(starVertices[i], starVertices[next], centerFront),
          baseColor = if (i % 2 == 0) cGold1 else cGold2,
          specular = 0.8f
        )
      )
      // Back faceted pyramids
      faces.add(
        Face3D(
          vertices = listOf(starVertices[next], starVertices[i], centerBack),
          baseColor = if (i % 2 == 0) cGold2 else cGold3,
          specular = 0.6f
        )
      )
    }

    ThreeDMath.renderFaces(
      canvas = canvas,
      faces = faces,
      pitch = pitch,
      yaw = yaw,
      roll = roll,
      scale = size * 0.9f,
      paint = sharedPaint,
      path = reusablePath
    )

    canvas.restoreToCount(saveCount)
  }

  // --- 7. 3D SPACE ROCKET ---
  fun draw3DRocket(
    canvas: Canvas,
    size: Float,
    angle: Float,
    currentTimeMs: Long
  ) {
    val hoverY = -size * 0.55f + sin(angle * 2f) * (size * 0.04f)
    val tilt = 45f + sin(angle) * 4f

    val saveCount = canvas.save()
    canvas.translate(0f, hoverY)
    canvas.rotate(tilt)

    // 1. Thruster Flames & Sparks (Animated)
    val flameLength = size * (0.28f + abs(sin(angle * 4f)) * 0.12f)
    val flamePath = Path().apply {
      moveTo(-size * 0.08f, size * 0.35f)
      lineTo(0f, size * 0.35f + flameLength)
      lineTo(size * 0.08f, size * 0.35f)
      close()
    }
    sharedPaint.color = Color.rgb(239, 68, 68) // Outer flame
    canvas.drawPath(flamePath, sharedPaint)

    val innerFlame = Path().apply {
      moveTo(-size * 0.04f, size * 0.35f)
      lineTo(0f, size * 0.35f + flameLength * 0.6f)
      lineTo(size * 0.04f, size * 0.35f)
      close()
    }
    sharedPaint.color = Color.rgb(253, 224, 71) // Inner yellow core
    canvas.drawPath(innerFlame, sharedPaint)

    // 2. Rocket Fuselage (3D shaded cylinder)
    val bodyWidth = size * 0.22f
    val bodyHeight = size * 0.45f
    val bodyTop = -bodyHeight * 0.5f

    // Main White Hull
    sharedPaint.color = Color.rgb(241, 245, 249)
    canvas.drawRoundRect(RectF(-bodyWidth * 0.5f, bodyTop, bodyWidth * 0.5f, bodyTop + bodyHeight), 8f, 8f, sharedPaint)

    // Red Nose Cone
    val nosePath = Path().apply {
      moveTo(-bodyWidth * 0.5f, bodyTop + 4f)
      lineTo(0f, bodyTop - size * 0.24f)
      lineTo(bodyWidth * 0.5f, bodyTop + 4f)
      close()
    }
    sharedPaint.color = Color.rgb(239, 68, 68)
    canvas.drawPath(nosePath, sharedPaint)

    // Left and Right Fins
    val leftFin = Path().apply {
      moveTo(-bodyWidth * 0.5f, bodyTop + bodyHeight * 0.4f)
      lineTo(-bodyWidth * 1.1f, bodyTop + bodyHeight * 0.95f)
      lineTo(-bodyWidth * 0.5f, bodyTop + bodyHeight * 0.9f)
      close()
    }
    val rightFin = Path().apply {
      moveTo(bodyWidth * 0.5f, bodyTop + bodyHeight * 0.4f)
      lineTo(bodyWidth * 1.1f, bodyTop + bodyHeight * 0.95f)
      lineTo(bodyWidth * 0.5f, bodyTop + bodyHeight * 0.9f)
      close()
    }
    sharedPaint.color = Color.rgb(220, 38, 38)
    canvas.drawPath(leftFin, sharedPaint)
    canvas.drawPath(rightFin, sharedPaint)

    // Porthole Window
    sharedPaint.color = Color.rgb(6, 182, 212)
    canvas.drawCircle(0f, bodyTop + bodyHeight * 0.35f, size * 0.065f, sharedPaint)
    strokePaint.style = Paint.Style.STROKE
    strokePaint.strokeWidth = 2.5f
    strokePaint.color = Color.rgb(148, 163, 184)
    canvas.drawCircle(0f, bodyTop + bodyHeight * 0.35f, size * 0.065f, strokePaint)

    canvas.restoreToCount(saveCount)
  }
}
