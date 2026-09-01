package com.example.characters

import android.content.Context
import android.util.Log
import com.example.model.CharacterAnimationInfo
import com.example.model.CharacterModel
import com.example.model.CharacterType
import com.example.model.ObjectCategory
import com.example.model.RenderMode
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader

/**
 * Parses the structured JSON schema for character metadata.
 * Allows adding or modifying characters without modifying code.
 */
object CharacterManifestLoader {
  private const val TAG = "CharacterManifestLoader"

  fun loadFromAssets(
    context: Context,
    assetPath: String = "characters/characters_manifest.json"
  ): List<CharacterModel> {
    return try {
      val assetManager = context.assets
      val inputStream = assetManager.open(assetPath)
      val reader = BufferedReader(InputStreamReader(inputStream))
      val jsonString = reader.use { it.readText() }
      loadFromJsonString(jsonString)
    } catch (e: Exception) {
      Log.e(TAG, "Failed to load character manifest from assets: $assetPath", e)
      emptyList()
    }
  }

  fun loadFromJsonString(jsonString: String): List<CharacterModel> {
    val result = mutableListOf<CharacterModel>()
    try {
      val root = JSONObject(jsonString)
      val charactersArray = root.optJSONArray("characters") ?: return emptyList()

      for (i in 0 until charactersArray.length()) {
        val item = charactersArray.getJSONObject(i)
        val id = item.getString("id")
        val typeStr = item.optString("type", "ROBOT")
        val type = try {
          CharacterType.valueOf(typeStr)
        } catch (e: Exception) {
          CharacterType.ROBOT
        }

        val name = item.getString("name")
        val emoji = item.optString("emoji", "👾")
        val description = item.optString("description", "")
        val categoryStr = item.optString("category", "PIXEL_ART")
        val category = try {
          ObjectCategory.valueOf(categoryStr)
        } catch (e: Exception) {
          ObjectCategory.PIXEL_ART
        }

        val defaultScale = item.optDouble("defaultScale", 0.045).toFloat()
        val recommendedOffset = item.optDouble("recommendedVerticalOffsetPercent", 0.038).toFloat()
        val creator = item.optString("creator", "Walkbar Studio")
        val license = item.optString("license", "CC0")

        val primaryColorHex = parseColorHex(item.optString("primaryColor", "#06B6D4"), 0xFF06B6D4)
        val secondaryColorHex = parseColorHex(item.optString("secondaryColor", "#E0F2FE"), 0xFFE0F2FE)

        val tags = mutableListOf<String>()
        val tagsArray = item.optJSONArray("tags")
        if (tagsArray != null) {
          for (t in 0 until tagsArray.length()) {
            tags.add(tagsArray.getString(t))
          }
        }

        val animObj = item.optJSONObject("animation")
        val animationInfo = if (animObj != null) {
          val frameCount = animObj.optInt("frameCount", 4)
          val stepDurationMs = animObj.optLong("stepDurationMs", 400L)
          val renderModeStr = animObj.optString("renderMode", "SPRITE_3D_PIXEL")
          val renderMode = try {
            RenderMode.valueOf(renderModeStr)
          } catch (e: Exception) {
            RenderMode.SPRITE_3D_PIXEL
          }
          val hasBlink = animObj.optBoolean("hasBlink", true)
          val hasShadow = animObj.optBoolean("hasShadow", true)

          val customFramePaths = mutableListOf<String>()
          val framesArray = animObj.optJSONArray("frames")
          if (framesArray != null) {
            for (f in 0 until framesArray.length()) {
              customFramePaths.add(framesArray.getString(f))
            }
          }

          CharacterAnimationInfo(
            frameCount = frameCount,
            stepDurationMs = stepDurationMs,
            renderMode = renderMode,
            hasBlink = hasBlink,
            hasShadow = hasShadow,
            customFrameAssetPaths = customFramePaths
          )
        } else {
          CharacterAnimationInfo()
        }

        result.add(
          CharacterModel(
            id = id,
            type = type,
            name = name,
            emoji = emoji,
            description = description,
            category = category,
            defaultScale = defaultScale,
            recommendedVerticalOffsetPercent = recommendedOffset,
            creator = creator,
            license = license,
            primaryColorHex = primaryColorHex,
            secondaryColorHex = secondaryColorHex,
            tags = tags,
            animation = animationInfo
          )
        )
      }
    } catch (e: Exception) {
      Log.e(TAG, "Error parsing character manifest JSON", e)
    }
    return result
  }

  private fun parseColorHex(hexStr: String, defaultColor: Long): Long {
    return try {
      val clean = hexStr.removePrefix("#")
      when (clean.length) {
        6 -> 0xFF000000L or clean.toLong(16)
        8 -> clean.toLong(16)
        else -> defaultColor
      }
    } catch (e: Exception) {
      defaultColor
    }
  }
}
