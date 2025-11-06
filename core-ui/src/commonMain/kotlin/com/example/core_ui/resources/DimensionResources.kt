package com.example.core_ui.resources

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

data class DimensionResources(

    val unit: Dp = 1.dp,

    // Spacing values
    val spacingXSmall: Dp = 4.dp,
    val spacingSmall: Dp = 8.dp,
    val spacingMedium: Dp = 16.dp,
    val spacingLarge: Dp = 24.dp,
    val spacingXLarge: Dp = 32.dp,
    val spacingXXLarge: Dp = 40.dp,

    // Padding values
    val paddingXSmall: Dp = 4.dp,
    val paddingSmall: Dp = 8.dp,
    val paddingMedium: Dp = 12.dp,
    val paddingLarge: Dp = 16.dp,
    val paddingXLarge: Dp = 20.dp,
    val paddingXXLarge: Dp = 24.dp,

    // Component sizes
    val buttonHeight: Dp = 48.dp,
    val buttonCornerRadius: Dp = 28.dp,
    val scoreButtonSize: Dp = 60.dp,
    val puttsButtonSize: Dp = 50.dp,
    val dragHandleWidth: Dp = 40.dp,
    val dragHandleHeight: Dp = 4.dp,
    val iconButtonSize: Dp = 32.dp,

    // Icon sizes
    val iconSmall: Dp = 16.dp,
    val iconMedium: Dp = 24.dp,
    val iconLarge: Dp = 32.dp,
    val iconXLarge: Dp = 48.dp,
    val iconXXLarge: Dp = 56.dp,

    // Border radius
    val cornerRadiusSmall: Dp = 8.dp,
    val cornerRadiusMedium: Dp = 12.dp,
    val cornerRadiusLarge: Dp = 16.dp,
    val cornerRadiusXLarge: Dp = 20.dp,

    //Elevation
    val elevationSmall: Dp = 4.dp,
    val elevationMedium: Dp = 8.dp,
)

val LocalDimensionResources = compositionLocalOf { DimensionResources() }