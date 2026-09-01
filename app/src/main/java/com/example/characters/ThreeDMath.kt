package com.example.characters

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Lightweight 3D math & projection engine for Android Canvas rendering.
 * Provides 3D rotation, perspective projection, surface normal lighting,
 * and depth-sorted polygon drawing.
 */
object ThreeDMath {

  data class Vec3(val x: Float, val y: Float, val z: Float) {
    operator fun plus(v: Vec3) = Vec3(x + v.x, y + v.y, z + v.z)
    operator fun minus(v: Vec3) = Vec3(x - v.x, y - v.y, z - v.z)
    operator fun times(s: Float) = Vec3(x * s, y * s, z * s)

    fun length(): Float = sqrt(x * x + y * y + z * z)

    fun normalized(): Vec3 {
      val len = length()
      return if (len > 1e-6f) Vec3(x / len, y / len, z / len) else Vec3(0f, 0f, 1f)
    }

    fun dot(v: Vec3): Float = x * v.x + y * v.y + z * v.z

    fun cross(v: Vec3): Vec3 = Vec3(
      y * v.z - z * v.y,
      z * v.x - x * v.z,
      x * v.y - y * v.x
    )
  }

  data class ProjectedPoint(val screenX: Float, val screenY: Float, val depth: Float)

  data class Face3D(
    val vertices: List<Vec3>,
    val baseColor: Int,
    val isDoubleSided: Boolean = false,
    val specular: Float = 0.3f,
    val emission: Int = 0
  )

  /**
   * Rotate a 3D point with Pitch (X), Yaw (Y), and Roll (Z) angles in radians.
   */
  fun rotate(v: Vec3, pitch: Float, yaw: Float, roll: Float): Vec3 {
    // 1. Yaw (Y-axis)
    val cosY = cos(yaw)
    val sinY = sin(yaw)
    val x1 = v.x * cosY + v.z * sinY
    val y1 = v.y
    val z1 = -v.x * sinY + v.z * cosY

    // 2. Pitch (X-axis)
    val cosP = cos(pitch)
    val sinP = sin(pitch)
    val x2 = x1
    val y2 = y1 * cosP - z1 * sinP
    val z2 = y1 * sinP + z1 * cosP

    // 3. Roll (Z-axis)
    val cosR = cos(roll)
    val sinR = sin(roll)
    val x3 = x2 * cosR - y2 * sinR
    val y3 = x2 * sinR + y2 * cosR
    val z3 = z2

    return Vec3(x3, y3, z3)
  }

  /**
   * Projects 3D coordinate onto 2D screen with camera distance perspective.
   */
  fun project(v: Vec3, cameraDistance: Float = 3.5f, scale: Float): ProjectedPoint {
    val zDist = cameraDistance - v.z
    val fov = if (zDist > 0.1f) cameraDistance / zDist else 1f
    return ProjectedPoint(
      screenX = v.x * fov * scale,
      screenY = -v.y * fov * scale, // Canvas Y is downwards
      depth = v.z
    )
  }

  /**
   * Calculate polygon face normal
   */
  fun computeFaceNormal(p0: Vec3, p1: Vec3, p2: Vec3): Vec3 {
    val v1 = p1 - p0
    val v2 = p2 - p0
    return v1.cross(v2).normalized()
  }

  /**
   * Apply Lambertian diffuse lighting + ambient + specular highlight
   */
  fun shadeColor(
    baseColor: Int,
    normal: Vec3,
    lightDir: Vec3 = Vec3(0.5f, 0.8f, -1.0f).normalized(),
    ambient: Float = 0.35f,
    specularIntensity: Float = 0.3f
  ): Int {
    val a = Color.alpha(baseColor)
    val r = Color.red(baseColor) / 255f
    val g = Color.green(baseColor) / 255f
    val b = Color.blue(baseColor) / 255f

    // Diffuse dot product (camera is at 0, 0, -cameraDistance looking towards +Z)
    val dot = (normal.dot(lightDir)).coerceIn(0f, 1f)
    val lightFactor = ambient + (1f - ambient) * dot

    // View direction vector towards camera
    val viewDir = Vec3(0f, 0f, -1f)
    // Half vector for Blinn-Phong specular
    val halfVec = (lightDir + viewDir).normalized()
    val specDot = (normal.dot(halfVec)).coerceAtLeast(0f)
    val specFactor = if (specularIntensity > 0f) Math.pow(specDot.toDouble(), 16.0).toFloat() * specularIntensity else 0f

    val outR = ((r * lightFactor + specFactor).coerceIn(0f, 1f) * 255f).toInt()
    val outG = ((g * lightFactor + specFactor).coerceIn(0f, 1f) * 255f).toInt()
    val outB = ((b * lightFactor + specFactor).coerceIn(0f, 1f) * 255f).toInt()

    return Color.argb(a, outR, outG, outB)
  }

  /**
   * Render a sorted collection of 3D faces using Painter's algorithm
   */
  fun renderFaces(
    canvas: Canvas,
    faces: List<Face3D>,
    pitch: Float,
    yaw: Float,
    roll: Float,
    scale: Float,
    paint: Paint,
    path: Path,
    cameraDistance: Float = 3.5f,
    lightDir: Vec3 = Vec3(0.5f, 0.8f, -1.0f).normalized()
  ) {
    // 1. Transform vertices and compute center depth for each face
    data class TransformedFace(
      val rotatedVertices: List<Vec3>,
      val projectedPoints: List<ProjectedPoint>,
      val normal: Vec3,
      val avgDepth: Float,
      val originalFace: Face3D
    )

    val transformedList = mutableListOf<TransformedFace>()

    for (face in faces) {
      if (face.vertices.size < 3) continue
      val rotated = face.vertices.map { rotate(it, pitch, yaw, roll) }
      val normal = computeFaceNormal(rotated[0], rotated[1], rotated[2])

      // Backface culling if not double sided (looking along +Z towards camera at -Z)
      // If normal.z < 0, face points towards camera
      if (!face.isDoubleSided && normal.z > 0.05f) {
        continue
      }

      val projected = rotated.map { project(it, cameraDistance, scale) }
      var sumZ = 0f
      for (v in rotated) sumZ += v.z
      val avgDepth = sumZ / rotated.size

      transformedList.add(
        TransformedFace(
          rotatedVertices = rotated,
          projectedPoints = projected,
          normal = normal,
          avgDepth = avgDepth,
          originalFace = face
        )
      )
    }

    // Sort by depth (farthest / lowest depth drawn first)
    transformedList.sortBy { it.avgDepth }

    // 2. Draw each face
    for (tf in transformedList) {
      val face = tf.originalFace
      val shadedColor = shadeColor(
        baseColor = face.baseColor,
        normal = tf.normal,
        lightDir = lightDir,
        specularIntensity = face.specular
      )

      path.reset()
      val pts = tf.projectedPoints
      path.moveTo(pts[0].screenX, pts[0].screenY)
      for (i in 1 until pts.size) {
        path.lineTo(pts[i].screenX, pts[i].screenY)
      }
      path.close()

      paint.style = Paint.Style.FILL
      paint.color = shadedColor
      canvas.drawPath(path, paint)

      // Subtle edge highlighting for clean crisp geometric styling
      paint.style = Paint.Style.STROKE
      paint.strokeWidth = 1.2f
      paint.color = Color.argb(45, 255, 255, 255)
      canvas.drawPath(path, paint)
    }
  }
}
