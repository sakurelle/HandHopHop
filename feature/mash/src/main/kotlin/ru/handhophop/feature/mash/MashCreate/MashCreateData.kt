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
        minSidePx = 96,
        titleRes = R.string.mash_create_difficulty_medium,
    ),
    HARD(
        minSidePx = 128,
        titleRes = R.string.mash_create_difficulty_hard,
    ),
}

internal data class MashThread(
    val article: String,
    val name: String,
    val color: Color,
    val isCompleted: Boolean = false,
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
        MashThread("DMC 498", "Red Dark", Color(0xFFA7132B)),
        MashThread("DMC 816", "Garnet", Color(0xFF8F1B2D)),
        MashThread("DMC 818", "Baby Pink", Color(0xFFD97C95)),
        MashThread("DMC 819", "Baby Pink Light", Color(0xFFF0BAC5)),
        MashThread("DMC 776", "Pink Medium", Color(0xFFDF8AA0)),
        MashThread("DMC 962", "Dusty Rose Medium", Color(0xFFD56A7B)),
        MashThread("DMC 3831", "Raspberry Dark", Color(0xFFB94A6A)),

        MashThread("DMC 550", "Violet Very Dark", Color(0xFF5C467C)),
        MashThread("DMC 208", "Lavender Very Dark", Color(0xFF7C5A9B)),
        MashThread("DMC 209", "Lavender Dark", Color(0xFF9072AB)),
        MashThread("DMC 210", "Lavender Medium", Color(0xFFB29BC9)),
        MashThread("DMC 211", "Lavender Light", Color(0xFFD6C7E8)),

        MashThread("DMC 820", "Royal Blue Dark", Color(0xFF28438E)),
        MashThread("DMC 798", "Delft Blue Dark", Color(0xFF4C6EA7)),
        MashThread("DMC 799", "Delft Blue Medium", Color(0xFF7391C4)),
        MashThread("DMC 800", "Delft Blue Pale", Color(0xFFB7C7E8)),
        MashThread("DMC 3843", "Electric Blue", Color(0xFF1E9FD2)),
        MashThread("DMC 3846", "Turquoise Bright", Color(0xFF14C8CC)),
        MashThread("DMC 995", "Electric Blue Dark", Color(0xFF0079C2)),
        MashThread("DMC 996", "Electric Blue Medium", Color(0xFF30B8D6)),

        MashThread("DMC 699", "Green", Color(0xFF0F6B3B)),
        MashThread("DMC 700", "Green Bright", Color(0xFF1C9651)),
        MashThread("DMC 701", "Green Light", Color(0xFF3FAE58)),
        MashThread("DMC 702", "Kelly Green", Color(0xFF5ABF45)),
        MashThread("DMC 703", "Chartreuse", Color(0xFFBDC84B)),
        MashThread("DMC 704", "Chartreuse Bright", Color(0xFFDDE25B)),
        MashThread("DMC 906", "Parrot Green Medium", Color(0xFF6FBF3A)),
        MashThread("DMC 907", "Parrot Green Light", Color(0xFF9BD95A)),

        MashThread("DMC 444", "Lemon Dark", Color(0xFFFFD600)),
        MashThread("DMC 307", "Lemon", Color(0xFFFFE76B)),
        MashThread("DMC 742", "Tangerine Light", Color(0xFFFFB84D)),
        MashThread("DMC 741", "Tangerine Medium", Color(0xFFFF8C3A)),
        MashThread("DMC 740", "Tangerine", Color(0xFFFF6F20)),

        MashThread("DMC 168", "Pewter Very Light", Color(0xFFD1D1D1)),
        MashThread("DMC 415", "Pearl Gray", Color(0xFFB8B8B8)),
        MashThread("DMC 318", "Steel Gray Light", Color(0xFF9E9E9E)),
        MashThread("DMC 317", "Pewter Gray", Color(0xFF7A7A7A)),
    )

    fun getThreadsByCount(colorCount: Int): List<MashThread> {
        val normalizedCount = colorCount.coerceIn(MASH_CREATE_MIN_COLORS, MASH_CREATE_MAX_COLORS)
        if (normalizedCount >= allThreads.size) {
            return allThreads
        }

        val lastIndex = allThreads.lastIndex
        val sampledIndices = LinkedHashSet<Int>()

        repeat(normalizedCount) { index ->
            val ratio = if (normalizedCount == 1) {
                0f
            } else {
                index.toFloat() / (normalizedCount - 1).toFloat()
            }
            sampledIndices += (ratio * lastIndex).toInt()
        }

        return sampledIndices.map(allThreads::get)
    }
}
