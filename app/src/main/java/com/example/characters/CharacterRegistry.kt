package com.example.characters

import android.content.Context
import android.util.Log
import com.example.model.CharacterAnimationInfo
import com.example.model.CharacterModel
import com.example.model.CharacterType
import com.example.model.ObjectCategory
import com.example.model.RenderMode

/**
 * Central repository for character assets.
 * Contains 3D-styled pixel art characters (Robot, Astronaut, Ninja, Dino),
 * 3D objects, animals, and vehicles, with support for dynamic loading from JSON manifests.
 */
object CharacterRegistry {
  private const val TAG = "CharacterRegistry"

  private val characterList = mutableListOf<CharacterModel>()
  private var isInitializedFromAssets = false

  init {
    loadDefaultRoster()
  }

  /**
   * Initializes the registry by loading from the local asset manifest if available.
   */
  fun initialize(context: Context) {
    if (isInitializedFromAssets) return
    val loaded = CharacterManifestLoader.loadFromAssets(context)
    if (loaded.isNotEmpty()) {
      characterList.clear()
      characterList.addAll(loaded)
      isInitializedFromAssets = true
      Log.d(TAG, "Loaded ${loaded.size} characters from manifest assets.")
    }
  }

  val characters: List<CharacterModel>
    get() = synchronized(characterList) { characterList.toList() }

  fun getAll(): List<CharacterModel> = characters

  fun getById(id: String): CharacterModel {
    return synchronized(characterList) {
      characterList.find { it.id == id } ?: characterList.firstOrNull() ?: defaultFallbackCharacter
    }
  }

  fun getByCategory(category: ObjectCategory): List<CharacterModel> {
    if (category == ObjectCategory.ALL) return characters
    return synchronized(characterList) {
      characterList.filter { it.category == category }
    }
  }

  fun register(character: CharacterModel) {
    synchronized(characterList) {
      val index = characterList.indexOfFirst { it.id == character.id }
      if (index >= 0) {
        characterList[index] = character
      } else {
        characterList.add(character)
      }
    }
  }

  fun registerAll(newList: List<CharacterModel>) {
    newList.forEach { register(it) }
  }

  fun search(query: String): List<CharacterModel> {
    if (query.isBlank()) return characters
    val q = query.trim().lowercase()
    return synchronized(characterList) {
      characterList.filter {
        it.name.lowercase().contains(q) ||
          it.description.lowercase().contains(q) ||
          it.tags.any { tag -> tag.lowercase().contains(q) }
      }
    }
  }

  private val defaultFallbackCharacter = CharacterModel(
    id = "pixel_robot_3d_01",
    type = CharacterType.ROBOT,
    name = "Cyber Bot",
    emoji = "🤖",
    description = "3D-shaded retro mecha-bot with glowing cyan LED optic visor and hydraulic strides.",
    category = ObjectCategory.PIXEL_ART,
    defaultScale = 0.048f,
    recommendedVerticalOffsetPercent = 0.038f,
    primaryColorHex = 0xFF06B6D4,
    secondaryColorHex = 0xFFE0F2FE,
    tags = listOf("3d_pixel", "robot"),
    creator = "Walkbar 3D Pixel Lab",
    license = "CC-BY-4.0"
  )

  private fun loadDefaultRoster() {
    characterList.clear()
    characterList.addAll(
      listOf(
        // ==========================================
        // 1. 3D-STYLED PIXEL ART CHARACTERS
        // ==========================================
        CharacterModel(
          id = "pixel_robot_3d_01",
          type = CharacterType.ROBOT,
          name = "Cyber Bot",
          emoji = "🤖",
          description = "3D-shaded retro mecha-bot with glowing cyan LED optic visor, antenna pulse, and dynamic hydraulic strides.",
          category = ObjectCategory.PIXEL_ART,
          defaultScale = 0.048f,
          recommendedVerticalOffsetPercent = 0.038f,
          primaryColorHex = 0xFF06B6D4,
          secondaryColorHex = 0xFFE0F2FE,
          tags = listOf("3d_pixel", "robot", "cyberpunk", "sci-fi"),
          creator = "Walkbar 3D Pixel Lab",
          license = "CC-BY-4.0",
          animation = CharacterAnimationInfo(
            frameCount = 4,
            stepDurationMs = 400L,
            renderMode = RenderMode.SPRITE_3D_PIXEL,
            hasBlink = true,
            hasShadow = true
          )
        ),
        CharacterModel(
          id = "pixel_astronaut_3d_01",
          type = CharacterType.ASTRONAUT,
          name = "Cosmo Spaceman",
          emoji = "🧑‍🚀",
          description = "3D pixel cosmonaut in deep space EVA suit with gold-reflective visor, life-support backpack, and zero-g strides.",
          category = ObjectCategory.PIXEL_ART,
          defaultScale = 0.048f,
          recommendedVerticalOffsetPercent = 0.038f,
          primaryColorHex = 0xFFF1F5F9,
          secondaryColorHex = 0xFFF59E0B,
          tags = listOf("3d_pixel", "astronaut", "space", "sci-fi"),
          creator = "Walkbar 3D Pixel Lab",
          license = "CC-BY-4.0",
          animation = CharacterAnimationInfo(
            frameCount = 4,
            stepDurationMs = 450L,
            renderMode = RenderMode.SPRITE_3D_PIXEL,
            hasBlink = false,
            hasShadow = true
          )
        ),
        CharacterModel(
          id = "pixel_ninja_3d_01",
          type = CharacterType.NINJA,
          name = "Shadow Ninja",
          emoji = "🥷",
          description = "Stealthy 3D shinobi in midnight garb with flowing crimson headband, glowing eyes, and agile low-stance strides.",
          category = ObjectCategory.PIXEL_ART,
          defaultScale = 0.046f,
          recommendedVerticalOffsetPercent = 0.038f,
          primaryColorHex = 0xFF0F172A,
          secondaryColorHex = 0xFFEF4444,
          tags = listOf("3d_pixel", "ninja", "warrior", "action"),
          creator = "Walkbar 3D Pixel Lab",
          license = "CC-BY-4.0",
          animation = CharacterAnimationInfo(
            frameCount = 4,
            stepDurationMs = 350L,
            renderMode = RenderMode.SPRITE_3D_PIXEL,
            hasBlink = true,
            hasShadow = true
          )
        ),
        CharacterModel(
          id = "pixel_dino_3d_01",
          type = CharacterType.PIXEL_DINO,
          name = "3D Pixel T-Rex",
          emoji = "🦖",
          description = "Multi-shaded 3D pixel velociraptor with animated running claws, bobbing head, and retro green arcade palette.",
          category = ObjectCategory.PIXEL_ART,
          defaultScale = 0.048f,
          recommendedVerticalOffsetPercent = 0.038f,
          primaryColorHex = 0xFF22C55E,
          secondaryColorHex = 0xFF15803D,
          tags = listOf("3d_pixel", "dino", "runner", "retro"),
          creator = "Walkbar 3D Pixel Lab",
          license = "CC0 Public Domain",
          animation = CharacterAnimationInfo(
            frameCount = 4,
            stepDurationMs = 320L,
            renderMode = RenderMode.SPRITE_3D_PIXEL,
            hasBlink = true,
            hasShadow = true
          )
        ),
        CharacterModel(
          id = "pixel_heart_01",
          type = CharacterType.PIXEL_HEART,
          name = "8-Bit Heart",
          emoji = "💖",
          description = "Retro RPG health container pulsating with vibrant crimson glow and subpixel bevel shine.",
          category = ObjectCategory.PIXEL_ART,
          defaultScale = 0.046f,
          recommendedVerticalOffsetPercent = 0.038f,
          primaryColorHex = 0xFFEF4444,
          secondaryColorHex = 0xFFFECACA,
          tags = listOf("pixel", "heart", "retro"),
          creator = "Walkbar Studio",
          license = "CC0",
          animation = CharacterAnimationInfo(frameCount = 2, stepDurationMs = 400L, renderMode = RenderMode.SPRITE_3D_PIXEL)
        ),
        CharacterModel(
          id = "pixel_ghost_01",
          type = CharacterType.PIXEL_GHOST,
          name = "Arcade Ghost",
          emoji = "👻",
          description = "Wobbly neon pink arcade ghost with animated shifting eyes and trailing hem.",
          category = ObjectCategory.PIXEL_ART,
          defaultScale = 0.046f,
          recommendedVerticalOffsetPercent = 0.038f,
          primaryColorHex = 0xFFEC4899,
          secondaryColorHex = 0xFFF472B6,
          tags = listOf("pixel", "ghost", "arcade"),
          creator = "Walkbar Studio",
          license = "CC0",
          animation = CharacterAnimationInfo(frameCount = 2, stepDurationMs = 380L, renderMode = RenderMode.SPRITE_3D_PIXEL)
        ),
        CharacterModel(
          id = "pixel_potion_01",
          type = CharacterType.PIXEL_POTION,
          name = "Mana Potion",
          emoji = "🧪",
          description = "Glass pixel flask with swirling emerald liquid and animated rising bubbles.",
          category = ObjectCategory.PIXEL_ART,
          defaultScale = 0.046f,
          recommendedVerticalOffsetPercent = 0.038f,
          primaryColorHex = 0xFF10B981,
          secondaryColorHex = 0xFFA7F3D0,
          tags = listOf("pixel", "potion", "magic"),
          creator = "Walkbar Studio",
          license = "CC0",
          animation = CharacterAnimationInfo(frameCount = 4, stepDurationMs = 500L, renderMode = RenderMode.SPRITE_3D_PIXEL)
        ),
        CharacterModel(
          id = "pixel_cat_01",
          type = CharacterType.PIXEL_CAT,
          name = "Pixel Kitty",
          emoji = "🐈",
          description = "Orange tabby 8-bit kitten galloping with animated paws and curled tail.",
          category = ObjectCategory.PIXEL_ART,
          defaultScale = 0.045f,
          recommendedVerticalOffsetPercent = 0.038f,
          primaryColorHex = 0xFFF97316,
          secondaryColorHex = 0xFFFED7AA,
          tags = listOf("pixel", "cat", "animal"),
          creator = "Walkbar Studio",
          license = "CC0",
          animation = CharacterAnimationInfo(frameCount = 4, stepDurationMs = 350L, renderMode = RenderMode.SPRITE_3D_PIXEL)
        ),
        CharacterModel(
          id = "pixel_coin_01",
          type = CharacterType.PIXEL_COIN,
          name = "Pixel Coin",
          emoji = "🪙",
          description = "Retro golden arcade coin rotating in smooth 8-bit perspective.",
          category = ObjectCategory.PIXEL_ART,
          defaultScale = 0.044f,
          recommendedVerticalOffsetPercent = 0.038f,
          primaryColorHex = 0xFFEAB308,
          secondaryColorHex = 0xFFFEF08A,
          tags = listOf("pixel", "coin", "gold"),
          creator = "Walkbar Studio",
          license = "CC0",
          animation = CharacterAnimationInfo(frameCount = 4, stepDurationMs = 300L, renderMode = RenderMode.SPRITE_3D_PIXEL)
        ),

        // ==========================================
        // 2. REAL 3D OBJECTS
        // ==========================================
        CharacterModel(
          id = "3d_diamond_01",
          type = CharacterType.THREE_D_DIAMOND,
          name = "3D Crystal Gem",
          emoji = "💎",
          description = "Polyhedral 3D faceted crystal diamond rotating smoothly with specular light glints.",
          category = ObjectCategory.THREE_D,
          defaultScale = 0.052f,
          recommendedVerticalOffsetPercent = 0.038f,
          primaryColorHex = 0xFF6366F1,
          secondaryColorHex = 0xFFE0E7FF,
          tags = listOf("3d", "gem", "diamond"),
          creator = "Walkbar 3D Engine",
          license = "CC0",
          animation = CharacterAnimationInfo(renderMode = RenderMode.VECTOR_3D)
        ),
        CharacterModel(
          id = "3d_gold_coin_01",
          type = CharacterType.THREE_D_GOLD_COIN,
          name = "3D Gold Token",
          emoji = "🪙",
          description = "Cylindrical 3D gold coin spinning continuously with metallic specular reflections.",
          category = ObjectCategory.THREE_D,
          defaultScale = 0.048f,
          recommendedVerticalOffsetPercent = 0.038f,
          primaryColorHex = 0xFFEAB308,
          secondaryColorHex = 0xFFFEF08A,
          tags = listOf("3d", "coin", "gold"),
          creator = "Walkbar 3D Engine",
          license = "CC0",
          animation = CharacterAnimationInfo(renderMode = RenderMode.VECTOR_3D)
        ),
        CharacterModel(
          id = "3d_saturn_01",
          type = CharacterType.THREE_D_SATURN,
          name = "3D Planet Saturn",
          emoji = "🪐",
          description = "3D celestial planet with rotating elliptical ring system and atmospheric terminator.",
          category = ObjectCategory.THREE_D,
          defaultScale = 0.054f,
          recommendedVerticalOffsetPercent = 0.038f,
          primaryColorHex = 0xFFF59E0B,
          secondaryColorHex = 0xFFFEF3C7,
          tags = listOf("3d", "space", "planet"),
          creator = "Walkbar 3D Engine",
          license = "CC0",
          animation = CharacterAnimationInfo(renderMode = RenderMode.VECTOR_3D)
        ),
        CharacterModel(
          id = "3d_ufo_01",
          type = CharacterType.THREE_D_UFO,
          name = "3D Flying UFO",
          emoji = "🛸",
          description = "Futuristic flying saucer with pulsing emerald tractor beam and rotating rim lights.",
          category = ObjectCategory.THREE_D,
          defaultScale = 0.052f,
          recommendedVerticalOffsetPercent = 0.038f,
          primaryColorHex = 0xFF06B6D4,
          secondaryColorHex = 0xFF22C55E,
          tags = listOf("3d", "ufo", "alien", "sci-fi"),
          creator = "Walkbar 3D Engine",
          license = "CC0",
          animation = CharacterAnimationInfo(renderMode = RenderMode.VECTOR_3D)
        ),
        CharacterModel(
          id = "3d_cube_01",
          type = CharacterType.THREE_D_CUBE,
          name = "3D Holo Cube",
          emoji = "🧊",
          description = "Low-poly tesseract hologram cube rotating along XYZ axes with neon cyan edges.",
          category = ObjectCategory.THREE_D,
          defaultScale = 0.048f,
          recommendedVerticalOffsetPercent = 0.038f,
          primaryColorHex = 0xFF06B6D4,
          secondaryColorHex = 0xFFA855F7,
          tags = listOf("3d", "cube", "hologram"),
          creator = "Walkbar 3D Engine",
          license = "CC0",
          animation = CharacterAnimationInfo(renderMode = RenderMode.VECTOR_3D)
        ),
        CharacterModel(
          id = "3d_star_01",
          type = CharacterType.THREE_D_STAR,
          name = "3D Super Star",
          emoji = "⭐",
          description = "10-faceted 3D gold star rotating with ambient lighting and ambient glow aura.",
          category = ObjectCategory.THREE_D,
          defaultScale = 0.050f,
          recommendedVerticalOffsetPercent = 0.038f,
          primaryColorHex = 0xFFFACC15,
          secondaryColorHex = 0xFFFFFFFF,
          tags = listOf("3d", "star", "gold"),
          creator = "Walkbar 3D Engine",
          license = "CC0",
          animation = CharacterAnimationInfo(renderMode = RenderMode.VECTOR_3D)
        ),
        CharacterModel(
          id = "3d_rocket_01",
          type = CharacterType.THREE_D_ROCKET,
          name = "3D Space Rocket",
          emoji = "🚀",
          description = "Aerodynamic space shuttle with pulsating particle thruster flames.",
          category = ObjectCategory.THREE_D,
          defaultScale = 0.050f,
          recommendedVerticalOffsetPercent = 0.038f,
          primaryColorHex = 0xFFEF4444,
          secondaryColorHex = 0xFFF1F5F9,
          tags = listOf("3d", "rocket", "space"),
          creator = "Walkbar 3D Engine",
          license = "CC0",
          animation = CharacterAnimationInfo(renderMode = RenderMode.VECTOR_3D)
        ),

        // ==========================================
        // 3. ANIMALS & COMPANIONS
        // ==========================================
        CharacterModel(
          id = "puppy_01",
          type = CharacterType.PUPPY,
          name = "Golden Pup",
          emoji = "🐶",
          description = "Playful golden retriever puppy with floppy ears and a wagging tail.",
          category = ObjectCategory.ANIMALS,
          defaultScale = 0.046f,
          recommendedVerticalOffsetPercent = 0.038f,
          primaryColorHex = 0xFFF59E0B,
          secondaryColorHex = 0xFFFEF3C7,
          tags = listOf("animal", "dog", "puppy"),
          creator = "Walkbar Studio",
          license = "CC0"
        ),
        CharacterModel(
          id = "cat_01",
          type = CharacterType.CAT,
          name = "Ginger Kitty",
          emoji = "🐱",
          description = "Agile ginger cat with pointed ears, paw steps, and curved tail.",
          category = ObjectCategory.ANIMALS,
          defaultScale = 0.044f,
          recommendedVerticalOffsetPercent = 0.038f,
          primaryColorHex = 0xFFFB923C,
          secondaryColorHex = 0xFFFFFFFF,
          tags = listOf("animal", "cat", "kitty"),
          creator = "Walkbar Studio",
          license = "CC0"
        ),
        CharacterModel(
          id = "bunny_01",
          type = CharacterType.BUNNY,
          name = "Snow Bunny",
          emoji = "🐰",
          description = "Fluffy white bunny with long bouncy ears and cute twitching nose.",
          category = ObjectCategory.ANIMALS,
          defaultScale = 0.048f,
          recommendedVerticalOffsetPercent = 0.038f,
          primaryColorHex = 0xFFF1F5F9,
          secondaryColorHex = 0xFFFDA4AF,
          tags = listOf("animal", "bunny", "rabbit"),
          creator = "Walkbar Studio",
          license = "CC0"
        ),
        CharacterModel(
          id = "fox_01",
          type = CharacterType.FOX,
          name = "Red Fox",
          emoji = "🦊",
          description = "Sleek red fox with bushy white-tipped tail and stealthy trot.",
          category = ObjectCategory.ANIMALS,
          defaultScale = 0.046f,
          recommendedVerticalOffsetPercent = 0.038f,
          primaryColorHex = 0xFFEA580C,
          secondaryColorHex = 0xFFFFFFFF,
          tags = listOf("animal", "fox"),
          creator = "Walkbar Studio",
          license = "CC0"
        ),
        CharacterModel(
          id = "penguin_01",
          type = CharacterType.PENGUIN,
          name = "Pebble Penguin",
          emoji = "🐧",
          description = "Adorable arctic penguin with classic side-to-side waddle gait.",
          category = ObjectCategory.ANIMALS,
          defaultScale = 0.048f,
          recommendedVerticalOffsetPercent = 0.038f,
          primaryColorHex = 0xFF1E293B,
          secondaryColorHex = 0xFFF8FAFC,
          tags = listOf("animal", "penguin", "waddle"),
          creator = "Walkbar Studio",
          license = "CC0"
        ),
        CharacterModel(
          id = "frog_01",
          type = CharacterType.FROG,
          name = "Hoppy Frog",
          emoji = "🐸",
          description = "Charming green treefrog with springy legs and cheerful eyes.",
          category = ObjectCategory.ANIMALS,
          defaultScale = 0.042f,
          recommendedVerticalOffsetPercent = 0.038f,
          primaryColorHex = 0xFF22C55E,
          secondaryColorHex = 0xFF86EFAC,
          tags = listOf("animal", "frog", "jump"),
          creator = "Walkbar Studio",
          license = "CC0"
        ),
        CharacterModel(
          id = "duck_01",
          type = CharacterType.DUCK,
          name = "Sunny Duckling",
          emoji = "🦆",
          description = "Bright yellow duckling with webbed feet and a rhythmic march.",
          category = ObjectCategory.ANIMALS,
          defaultScale = 0.045f,
          recommendedVerticalOffsetPercent = 0.038f,
          primaryColorHex = 0xFFFACC15,
          secondaryColorHex = 0xFFF97316,
          tags = listOf("animal", "duck"),
          creator = "Walkbar Studio",
          license = "CC0"
        ),

        // ==========================================
        // 4. VEHICLES & SCI-FI
        // ==========================================
        CharacterModel(
          id = "cyber_car_01",
          type = CharacterType.CYBER_CAR,
          name = "Cyber Cruiser",
          emoji = "🏎️",
          description = "Low-profile cyberpunk sports car with spinning neon rims and illuminated headlights.",
          category = ObjectCategory.VEHICLES,
          defaultScale = 0.048f,
          recommendedVerticalOffsetPercent = 0.038f,
          primaryColorHex = 0xFFEC4899,
          secondaryColorHex = 0xFF06B6D4,
          tags = listOf("vehicle", "car", "cyberpunk"),
          creator = "Walkbar Studio",
          license = "CC0"
        ),
        CharacterModel(
          id = "hover_drone_01",
          type = CharacterType.HOVER_DRONE,
          name = "Aero Drone",
          emoji = "🚁",
          description = "Sleek quadcopter drone with spinning dual rotors and scanning laser beacon.",
          category = ObjectCategory.VEHICLES,
          defaultScale = 0.046f,
          recommendedVerticalOffsetPercent = 0.038f,
          primaryColorHex = 0xFF3B82F6,
          secondaryColorHex = 0xFF93C5FD,
          tags = listOf("vehicle", "drone", "sci-fi"),
          creator = "Walkbar Studio",
          license = "CC0"
        ),

        // ==========================================
        // 5. LIFESTYLE & FUN OBJECTS
        // ==========================================
        CharacterModel(
          id = "boba_tea_01",
          type = CharacterType.BOBA_TEA,
          name = "Boba Milk Tea",
          emoji = "🧋",
          description = "Cute brown sugar milk tea cup with straw and bouncing tapioca pearls.",
          category = ObjectCategory.LIFESTYLE,
          defaultScale = 0.048f,
          recommendedVerticalOffsetPercent = 0.038f,
          primaryColorHex = 0xFFD97706,
          secondaryColorHex = 0xFFFEF3C7,
          tags = listOf("lifestyle", "food", "tea"),
          creator = "Walkbar Studio",
          license = "CC0"
        ),
        CharacterModel(
          id = "vinyl_record_01",
          type = CharacterType.VINYL_RECORD,
          name = "Neon Vinyl",
          emoji = "💿",
          description = "Retro LP record spinning with groove reflections and glowing center label.",
          category = ObjectCategory.LIFESTYLE,
          defaultScale = 0.046f,
          recommendedVerticalOffsetPercent = 0.038f,
          primaryColorHex = 0xFF8B5CF6,
          secondaryColorHex = 0xFFEC4899,
          tags = listOf("lifestyle", "music", "retro"),
          creator = "Walkbar Studio",
          license = "CC0"
        )
      )
    )
  }
}
