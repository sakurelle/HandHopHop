package ru.handhophop.feature.mash.MashCreate

internal interface MashCreateUiAction

internal class InitMashCreateAction(
    val imageUrl: String?,
    val suggestedProjectName: String = "",
) : MashCreateUiAction

internal class ProjectNameChangedAction(
    val value: String,
) : MashCreateUiAction

internal class ClearProjectNameAction : MashCreateUiAction

internal class SchemeTypeChangedAction(
    val value: MashCreateSchemeType,
) : MashCreateUiAction

internal class ColorCountChangedAction(
    val value: Int,
) : MashCreateUiAction

internal class DifficultyChangedAction(
    val value: MashCreateDifficulty,
) : MashCreateUiAction

internal class CreateWorkAction : MashCreateUiAction

internal class ConsumeCreatedConfigAction : MashCreateUiAction