package com.nivelpro.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

object NivelColors {
    val PureBlack = Color(0xFF000000)
    val SurfaceDark = Color(0xFF0D1014)
    val SurfaceElevated = Color(0xFF151920)
    val BorderSubtle = Color(0xFF232A34)
    val TextPrimary = Color(0xFFF5F7FA)
    val TextSecondary = Color(0xFF8B95A5)
    val PlumbGreen = Color(0xFF00E676)
    val PlumbGreenDim = Color(0xFF004D29)
    val TiltRed = Color(0xFFFF3B30)
    val TiltRedDim = Color(0xFF4A100D)
    val FreezeAmber = Color(0xFFFFB300)
    val ReferenceLine = Color(0xFFE2E8F0)
    val ReticleTrack = Color(0xFF2A313D)
}

private val NivelDarkColorScheme = darkColorScheme(
    primary = NivelColors.PlumbGreen,
    onPrimary = NivelColors.PureBlack,
    secondary = NivelColors.TextPrimary,
    onSecondary = NivelColors.PureBlack,
    background = NivelColors.PureBlack,
    onBackground = NivelColors.TextPrimary,
    surface = NivelColors.SurfaceDark,
    onSurface = NivelColors.TextPrimary,
    surfaceVariant = NivelColors.SurfaceElevated,
    onSurfaceVariant = NivelColors.TextSecondary,
    error = NivelColors.TiltRed,
    outline = NivelColors.BorderSubtle
)

private val NivelTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 54.sp,
        lineHeight = 58.sp,
        fontFeatureSettings = "tnum"
    ),
    headlineSmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp,
        lineHeight = 26.sp
    ),
    titleMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        lineHeight = 22.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp
    ),
    labelMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp
    )
)

@Composable
fun NivelProTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = NivelDarkColorScheme,
        typography = NivelTypography,
        content = content
    )
}
