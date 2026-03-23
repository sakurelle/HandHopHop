package com.example.handhophop.feature.mash.presentation

import androidx.annotation.ColorRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.colorResource
import ru.handhophop.feature.mash.R

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

        object Default : Color(R.color.button)
        object Accent : Color(R.color.main_color)

        object White : Color(R.color.white)

        @Composable
        fun getColor() = colorResource(this.colorRes)
    }

    enum class Size {
        MAX,
        MIN
    }
}

@Immutable
internal data class TextButtonState(
    val text: String,
    override val isActive: Boolean = true,
    override val size: Size = Size.MAX,
    override val textColor: Color = Color.White,
    override val buttonColor: Color = Color.Default,
) : ButtonState(isActive, size, textColor, buttonColor)

@Immutable
internal data class IconButtonState(
    val icon: Painter? = null,
    val text: String,
    override val isActive: Boolean = true,
    override val size: Size = Size.MAX,
    override val textColor: Color = Color.White,
    override val buttonColor: Color = Color.Default,
) : ButtonState(isActive, size, textColor, buttonColor)

