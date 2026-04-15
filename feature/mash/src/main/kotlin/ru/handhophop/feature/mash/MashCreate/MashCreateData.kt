package ru.handhophop.feature.mash.MashCreate

import androidx.annotation.StringRes
import androidx.compose.ui.graphics.Color
import ru.handhophop.feature.mash.R

internal const val MASH_CREATE_MIN_COLORS = 10
internal const val MASH_CREATE_MAX_COLORS = 30
internal const val MASH_CREATE_DEFAULT_COLORS = 20

internal enum class MashCreateSchemeType(
    @StringRes val titleRes: Int,
) {
    COLORING(R.string.mash_create_scheme_coloring),
    EMBROIDERY(R.string.mash_create_scheme_embroidery),
}

internal enum class MashCreateDifficulty(
    val minSidePx: Int,
    @StringRes val titleRes: Int,
) {
    EASY(
        minSidePx = 64,
        titleRes = R.string.mash_create_difficulty_easy,
    ),
    MEDIUM(
        minSidePx = 128,
        titleRes = R.string.mash_create_difficulty_medium,
    ),
    HARD(
        minSidePx = 256,
        titleRes = R.string.mash_create_difficulty_hard,
    ),
}

internal data class MashThread(
    val article: String,
    val name: String,
    val color: Color,
)

internal data class MashCreateConfig(
    val projectName: String,
    val imageUrl: String?,
    val schemeType: MashCreateSchemeType,
    val colorCount: Int,
    val difficulty: MashCreateDifficulty,
    val threads: List<MashThread>,
)

internal object MashCreateData {

    // Ориентируемся на DMC как на стандарт палитры ниток.
    val allThreads: List<MashThread> = listOf(
        MashThread("DMC B5200", "Snow White", Color(0xFFFFFCFA)),
        MashThread("DMC Blanc", "White", Color(0xFFF7F4EF)),
        MashThread("DMC 3865", "Winter White", Color(0xFFF1E9DC)),
        MashThread("DMC Ecru", "Ecru", Color(0xFFD8C8AE)),
        MashThread("DMC 543", "Beige Brown", Color(0xFFF1E6D4)),
        MashThread("DMC 3024", "Brown Gray Light", Color(0xFFE3D4C5)),
        MashThread("DMC 3033", "Mocha Beige Very Light", Color(0xFFD8C0A8)),
        MashThread("DMC 434", "Brown Light", Color(0xFFC7A987)),
        MashThread("DMC 435", "Brown Very Light", Color(0xFFB3906C)),
        MashThread("DMC 436", "Tan", Color(0xFFA17249)),
        MashThread("DMC 780", "Topaz Ultra Very Dark", Color(0xFF8A5A32)),
        MashThread("DMC 3828", "Hazelnut Brown", Color(0xFF5E3F2B)),
        MashThread("DMC 310", "Black", Color(0xFF1D1A17)),
        MashThread("DMC 666", "Bright Red", Color(0xFFD53645)),
        MashThread("DMC 321", "Red", Color(0xFFC53A4A)),
        MashThread("DMC 818", "Baby Pink", Color(0xFFD97C95)),
        MashThread("DMC 819", "Baby Pink Light", Color(0xFFF0BAC5)),
        MashThread("DMC 550", "Violet Very Dark", Color(0xFF5C467C)),
        MashThread("DMC 208", "Lavender Very Dark", Color(0xFF7C5A9B)),
        MashThread("DMC 209", "Lavender Dark", Color(0xFF9072AB)),
        MashThread("DMC 210", "Lavender Medium", Color(0xFFB29BC9)),
        MashThread("DMC 820", "Royal Blue Dark", Color(0xFF28438E)),
        MashThread("DMC 798", "Delft Blue Dark", Color(0xFF4C6EA7)),
        MashThread("DMC 799", "Delft Blue Medium", Color(0xFF7391C4)),
        MashThread("DMC 3843", "Electric Blue", Color(0xFF1E9FD2)),
        MashThread("DMC 3846", "Turquoise Bright", Color(0xFF14C8CC)),
        MashThread("DMC 699", "Green", Color(0xFF0F6B3B)),
        MashThread("DMC 700", "Green Bright", Color(0xFF1C9651)),
        MashThread("DMC 703", "Chartreuse", Color(0xFFBDC84B)),
        MashThread("DMC 704", "Chartreuse Bright", Color(0xFFDDE25B)),
    )

    fun getThreadsByCount(colorCount: Int): List<MashThread> {
        return allThreads.take(
            colorCount.coerceIn(MASH_CREATE_MIN_COLORS, MASH_CREATE_MAX_COLORS)
        )
    }
}