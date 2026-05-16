package ru.handhophop.core.design

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.painter.Painter

@Immutable
abstract class ButtonState(
    open val isActive: Boolean,
    //val Icon: Painter? = null,
    open val size: Size,
    //val text: String,
    open val textColor: Color,
    open val buttonColor: Color,
) {


    sealed class Color(
        private val resolver: @Composable (DesignColors) -> androidx.compose.ui.graphics.Color
    ) {

        object Button : Color({ it.primaryAction })
        object Background : Color({ it.surface })

        object White : Color({ it.onPrimaryAction })

        object BottomBar : Color({ it.bottomBar })


        @Composable
        fun getColor() = resolver(HandHopHopDesignSystem.colors)
    }

    enum class Size {
        FIX,
        WRAPCONTENT,
        FILL
    }
}

@Immutable
internal data class TextButtonState(
    val text: String,
    override val isActive: Boolean = true,
    override val size: Size = Size.FIX,
    override val textColor: Color = Color.White,
    override val buttonColor: Color = Color.Button,
) : ButtonState(isActive, size, textColor, buttonColor)

@Immutable
internal data class IconButtonState(
    val icon: Painter? = null,
    val text: String,
    override val isActive: Boolean = true,
    override val size: Size = Size.FIX,
    override val textColor: Color = Color.White,
    override val buttonColor: Color = Color.Button,
) : ButtonState(isActive, size, textColor, buttonColor)

