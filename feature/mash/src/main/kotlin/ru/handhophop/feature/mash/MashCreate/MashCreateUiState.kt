package ru.handhophop.feature.mash.MashCreate

internal data class MashCreateUiState(
    val imageUrl: String? = null,
    val projectName: String = "",
    val schemeType: MashCreateSchemeType = MashCreateSchemeType.COLORING,
    val colorCount: Int = MASH_CREATE_DEFAULT_COLORS,
    val difficulty: MashCreateDifficulty = MashCreateDifficulty.MEDIUM,
    val createdConfig: MashCreateConfig? = null,
) {
    val threads: List<MashThread>
        get() = MashCreateData.getThreadsByCount(colorCount)

    val isCreateButtonEnabled: Boolean
        get() = projectName.isNotBlank()
}