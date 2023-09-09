package com.example.pokerunwearos.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Pokemon(
    val abilities: List<PokemonAbility>,
    @SerialName("base_experience")
    val baseExperience: Int,
    val forms: List<NamedApiResource>,
    @SerialName("game_indices")
    val gameIndices: List<VersionGameIndex>,
    val height: Int,
    @SerialName("held_items")
    val heldItems: List<PokemonHeldItem>,
    val id: Int,
    @SerialName("is_default")
    val isDefault: Boolean,
    @SerialName("location_area_encounters")
    val locationAreaEncounters: String,
    val moves: List<PokemonMove>,
    val name: String,
    val order: Int,
    @SerialName("past_types")
    val pastTypes: List<PokemonPastType>,
    val species: NamedApiResource,
    val sprites: PokemonSpritesDefault,
    val stats: List<PokemonStat>,
    val types: List<PokemonType>,
    val weight: Int
)

@Serializable
data class PokemonSpritesDefault(
    @SerialName("back_default")
    val backDefault: String? = null,
    @SerialName("back_shiny")
    val backShiny: String? = null,
    @SerialName("front_default")
    val frontDefault: String? = null,
    @SerialName("front_shiny")
    val frontShiny: String? = null,
    @SerialName("back_female")
    val backFemale: String? = null,
    @SerialName("back_shiny_female")
    val backShinyFemale: String? = null,
    @SerialName("front_female")
    val frontFemale: String? = null,
    @SerialName("front_shiny_female")
    val frontShinyFemale: String? = null,
    val other: PokemonSpritesOther,
    val versions: Versions
)

@Serializable
data class PokemonSpritesOther(
    @SerialName("dream_world")
    val dreamWorld: PokemonSprites? = null,
    val home: PokemonSprites? = null,
    @SerialName("official-artwork")
    val officialArtwork: PokemonSprites? = null
)

@Serializable
data class PokemonSprites(
    val animated: PokemonSprites? = null,
    @SerialName("back_default")
    val backDefault: String? = null,
    @SerialName("back_shiny")
    val backShiny: String? = null,
    @SerialName("front_default")
    val frontDefault: String? = null,
    @SerialName("front_shiny")
    val frontShiny: String? = null,
    @SerialName("back_female")
    val backFemale: String? = null,
    @SerialName("back_shiny_female")
    val backShinyFemale: String? = null,
    @SerialName("front_female")
    val frontFemale: String? = null,
    @SerialName("front_shiny_female")
    val frontShinyFemale: String? = null,
    @SerialName("back_gray")
    val backGray: String? = null,
    @SerialName("front_gray")
    val frontGray: String? = null,
    @SerialName("back_transparent")
    val backTransparent: String? = null,
    @SerialName("back_shiny_transparent")
    val backShinyTransparent: String? = null,
    @SerialName("front_transparent")
    val frontTransparent: String? = null,
    @SerialName("front_shiny_transparent")
    val frontShinyTransparent: String? = null,
)

@Serializable
data class PokemonAbility(
    val ability: NamedApiResource,
    @SerialName("is_hidden")
    val isHidden: Boolean,
    val slot: Int
)

@Serializable
data class PokemonHeldItem(
    val item: NamedApiResource,
    @SerialName("version_details")
    val versionDetails: List<PokemonHeldItemVersion>
)

@Serializable
data class PokemonHeldItemVersion(
    val version: NamedApiResource,
    val rarity: Int
)

@Serializable
data class PokemonMove(
    val move: NamedApiResource,
    @SerialName("version_group_details")
    val versionGroupDetails: List<PokemonMoveVersion>
)

@Serializable
data class PokemonMoveVersion(
    @SerialName("move_learn_method")
    val moveLearnMethod: NamedApiResource,
    @SerialName("version_group")
    val versionGroup: NamedApiResource,
    @SerialName("level_learned_at")
    val levelLearnedAt: Int
)

@Serializable
data class PokemonStat(
    val stat: NamedApiResource,
    val effort: Int,
    @SerialName("base_stat")
    val baseStat: Int
)

@Serializable
data class PokemonPastType(
    val generation: NamedApiResource,
    val types: List<PokemonType>
)

@Serializable
data class PokemonType(
    val slot: Int,
    val type: NamedApiResource
)

@Serializable
data class NamedApiResource(
    val name: String,
    val url: String
)

@Serializable
data class VersionGameIndex(
    @SerialName("game_index")
    val gameIndex: Int,
    val version: NamedApiResource
)

@Serializable
data class Versions(
    @SerialName("generation-i")
    val generationI: GenerationI,
    @SerialName("generation-ii")
    val generationII: GenerationII,
    @SerialName("generation-iii")
    val generationIII: GenerationIII,
    @SerialName("generation-iv")
    val generationIV: GenerationIV,
    @SerialName("generation-v")
    val generationV: GenerationV,
    @SerialName("generation-vi")
    val generationVI: GenerationVI,
    @SerialName("generation-vii")
    val generationVII: GenerationVII,
    @SerialName("generation-viii")
    val generationVIII: GenerationVIII
)

@Serializable
data class GenerationI(
    @SerialName("red-blue")
    val redBlue: PokemonSprites?,
    val yellow: PokemonSprites?
)

@Serializable
data class GenerationII(
    val crystal: PokemonSprites?,
    val gold: PokemonSprites?,
    val silver: PokemonSprites?
)

@Serializable
data class GenerationIII(
    val emerald: PokemonSprites?,
    @SerialName("firered-leafgreen")
    val fireredLeafgreen: PokemonSprites?,
    @SerialName("ruby-sapphire")
    val rubySapphire: PokemonSprites?
)

@Serializable
data class GenerationIV(
    @SerialName("diamond-pearl")
    val diamondPearl: PokemonSprites?,
    @SerialName("heartgold-soulsilver")
    val heartgoldSoulsilver: PokemonSprites?,
    val platinum: PokemonSprites?
)

@Serializable
data class GenerationV(
    @SerialName("black-white")
    val blackWhite: PokemonSprites?
)

@Serializable
data class GenerationVI(
    @SerialName("omegaruby-alphasapphire")
    val omegarubyAlphasapphire: PokemonSprites?,
    @SerialName("x-y")
    val xY: PokemonSprites?
)

@Serializable
data class GenerationVII(
    val icons: PokemonSprites?,
    @SerialName("ultra-sun-ultra-moon")
    val ultraSunUltraMoon: PokemonSprites?
)

@Serializable
data class GenerationVIII(
    val icons: PokemonSprites?
)