package com.ramitsuri.choresclient.android.ui.theme

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

val LocalDimensions = staticCompositionLocalOf { Dimens() }
data class Dimens(
    val small: Dp = 4.dp,
    val medium: Dp = 8.dp,
    val large: Dp = 16.dp,
    val extraLarge: Dp = 32.dp,
    val paddingCardView: Dp = 12.dp,
    val minBottomSheetHeight: Dp = 264.dp,
    val iconWidth: Dp = 48.dp,
    val assignmentHeaderCornerRadius: Dp = 24.dp,
    val minAssignmentItemHeight: Dp = 64.dp,
)