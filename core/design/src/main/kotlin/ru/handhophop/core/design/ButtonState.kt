package ru.handhophop.core.design

import androidx.annotation.ColorRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.colorResource
import ru.handhophop.design.R

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
        @param:ColorRes val colorRes: Int
    ) {

        object Button : Color(R.color.button)
        object Background : Color(R.color.main_color)

        object White : Color(R.color.white)

        object BottomBar : Color(R.color.bottom_bar)


        @Composable
        fun getColor() = colorResource(this.colorRes)
    }

    enum class Size {
        FIX,
        WRAPCONTENT
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

