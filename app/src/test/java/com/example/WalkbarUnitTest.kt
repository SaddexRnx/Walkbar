package com.example

import android.net.Uri
import com.example.characters.CharacterManifestLoader
import com.example.characters.CharacterRegistry
import com.example.characters.ThreeDMath
import com.example.characters.WalkCycleMath
import com.example.model.AnimationBehavior
import com.example.model.CharacterOverlayConfig
import com.example.model.CharacterType
import com.example.model.ObjectCategory
import com.example.model.RenderMode
import com.example.model.VideoMetadata
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class WalkbarUnitTest {

  @Test
  fun testTimelineMath_progressCalculation() {
    val duration = 10000L // 10s

    assertEquals(0.0f, WalkCycleMath.calculateProgress(0L, duration), 0.001f)
    assertEquals(0.5f, WalkCycleMath.calculateProgress(5000L, duration), 0.001f)
    assertEquals(1.0f, WalkCycleMath.calculateProgress(10000L, duration), 0.001f)
    assertEquals(1.0f, WalkCycleMath.calculateProgress(15000L, duration), 0.001f)
  }

  @Test
  fun testTimelineMath_pixelPositioning() {
    val duration = 10000L
    val config = CharacterOverlayConfig(
      startXPercent = 0.0f,
      endXPercent = 1.0f,
      verticalOffsetPercent = 0.04f
    )
    val width = 1080f
    val height = 1920f

    // At t = 0
    val x0 = WalkCycleMath.calculatePixelX(0L, duration, config, width)
    assertEquals(0f, x0, 0.01f)

    // At t = 5s (midpoint)
    val xHalf = WalkCycleMath.calculatePixelX(5000L, duration, config, width)
    assertEquals(540f, xHalf, 0.01f)

    // At t = 10s (end)
    val xEnd = WalkCycleMath.calculatePixelX(10000L, duration, config, width)
    assertEquals(1080f, xEnd, 0.01f)

    // Vertical Y position
    val y = WalkCycleMath.calculatePixelY(config, height)
    assertEquals(1920f * 0.96f, y, 0.01f)
  }

  @Test
  fun testReverseDirection_positioning() {
    val duration = 10000L
    val config = CharacterOverlayConfig(
      startXPercent = 0.0f,
      endXPercent = 1.0f,
      reverseDirection = true
    )
    val width = 1000f

    // At t = 0 with reverse direction, character starts on right
    val x0 = WalkCycleMath.calculatePixelX(0L, duration, config, width)
    assertEquals(1000f, x0, 0.01f)

    // At t = 10s with reverse direction, character reaches left
    val xEnd = WalkCycleMath.calculatePixelX(10000L, duration, config, width)
    assertEquals(0f, xEnd, 0.01f)
  }

  @Test
  fun testWalkCyclePhase() {
    val walkBehavior = AnimationBehavior.WALK
    val phase0 = WalkCycleMath.calculatePhase(0L, walkBehavior)
    assertEquals(0.0f, phase0, 0.01f)

    val phaseMid = WalkCycleMath.calculatePhase(walkBehavior.stepDurationMs / 2, walkBehavior)
    assertEquals(0.5f, phaseMid, 0.01f)
  }

  @Test
  fun testThreeDMath_rotationAndProjection() {
    val v = ThreeDMath.Vec3(1f, 0f, 0f)
    val rotated = ThreeDMath.rotate(v, pitch = 0f, yaw = Math.PI.toFloat() / 2f, roll = 0f)
    // Rotating around Y by 90 deg turns X into -Z
    assertEquals(0f, rotated.x, 0.001f)
    assertEquals(-1f, rotated.z, 0.001f)

    val projected = ThreeDMath.project(v, cameraDistance = 3.5f, scale = 100f)
    assertTrue(projected.screenX > 0f)
  }

  @Test
  fun testCharacterRegistry_containsAllCategoriesAnd3DPixelArt() {
    val allCharacters = CharacterRegistry.characters
    assertTrue("Total character roster should have at least 20 items", allCharacters.size >= 20)

    val pixelArt = CharacterRegistry.getByCategory(ObjectCategory.PIXEL_ART)
    assertTrue("Should contain pixel art sprites", pixelArt.size >= 6)
    assertTrue("Should contain Robot", pixelArt.any { it.type == CharacterType.ROBOT })
    assertTrue("Should contain Astronaut", pixelArt.any { it.type == CharacterType.ASTRONAUT })
    assertTrue("Should contain Ninja", pixelArt.any { it.type == CharacterType.NINJA })
    assertTrue("Should contain Dino", pixelArt.any { it.type == CharacterType.PIXEL_DINO })

    val threeDObjects = CharacterRegistry.getByCategory(ObjectCategory.THREE_D)
    assertTrue("Should contain multiple 3D objects", threeDObjects.size >= 6)
    assertTrue(threeDObjects.any { it.name.contains("Gem") || it.name.contains("Diamond") })
    assertTrue(threeDObjects.any { it.name.contains("Saturn") })
    assertTrue(threeDObjects.any { it.name.contains("Rocket") })

    val animals = CharacterRegistry.getByCategory(ObjectCategory.ANIMALS)
    assertTrue("Should contain animal companions", animals.size >= 7)
  }

  @Test
  fun testCharacterManifestLoader_parsesValidJson() {
    val sampleJson = """
      {
        "schemaVersion": "1.0",
        "characters": [
          {
            "id": "custom_robot_01",
            "type": "ROBOT",
            "name": "Mecha Test",
            "emoji": "🤖",
            "category": "PIXEL_ART",
            "description": "Test Robot",
            "defaultScale": 0.05,
            "recommendedVerticalOffsetPercent": 0.04,
            "creator": "Walkbar Test Studio",
            "license": "CC0",
            "primaryColor": "#06B6D4",
            "tags": ["robot", "test"],
            "animation": {
              "frameCount": 4,
              "stepDurationMs": 400,
              "renderMode": "SPRITE_3D_PIXEL"
            }
          }
        ]
      }
    """.trimIndent()

    val parsed = CharacterManifestLoader.loadFromJsonString(sampleJson)
    assertEquals(1, parsed.size)
    val char = parsed[0]
    assertEquals("custom_robot_01", char.id)
    assertEquals(CharacterType.ROBOT, char.type)
    assertEquals("Mecha Test", char.name)
    assertEquals(ObjectCategory.PIXEL_ART, char.category)
    assertEquals(RenderMode.SPRITE_3D_PIXEL, char.animation.renderMode)
    assertEquals(4, char.animation.frameCount)
    assertEquals("Walkbar Test Studio", char.creator)
  }

  @Test
  fun testVideoMetadata_formatting() {
    val meta = VideoMetadata(
      uri = Uri.parse("file:///dummy.mp4"),
      fileName = "sample_reel.mp4",
      durationMs = 15500L,
      rawWidth = 1080,
      rawHeight = 1920,
      rotationDegrees = 0,
      fps = 29.97f,
      bitrateBps = 12_500_000L,
      videoMimeType = "video/mp4",
      audioMimeType = "audio/mp4a-latm",
      hasAudio = true,
      fileSizeBytes = 24_500_000L
    )

    assertEquals("1080 × 1920", meta.formattedResolution)
    assertEquals("29 FPS", meta.formattedFps)
    assertEquals("0:15", meta.formattedDuration)
    assertTrue(meta.aspectRatio < 1f)
    assertEquals(9f / 16f, meta.aspectRatio, 0.01f)
  }
}
