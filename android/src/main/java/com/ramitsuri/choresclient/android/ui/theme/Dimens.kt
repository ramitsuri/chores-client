package com.ramitsuri.choresclient.android.ui.theme

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

val LocalDimensions = staticCompositionLocalOf { Dimens() }
data class Dimens(
    val extraSmall: Dp = 2.dp,
    val small: Dp = 4.dp,
    val medium: Dp = 8.dp,
    val large: Dp = 16.dp,
    val extraLarge: Dp = 24.dp,
    val doubleExtraLarge: Dp = 32.dp,
    val paddingCardView: Dp = 16.dp,
    val minBottomSheetHeight: Dp = 264.dp,
    val iconWidthSmall: Dp = 18.dp,
    val iconWidthMedium: Dp = 24.dp,
    val iconWidthLarge: Dp = 48.dp,
    val minAssignmentItemHeight: Dp = 64.dp,
    val cardCornerRadius: Dp = 28.dp,
    val fabCornerRadius: Dp = 16.dp,
    val chipCornerRadius: Dp = 50.dp,
)
