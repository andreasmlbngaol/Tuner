package id.andreasmlbngaol.tuner.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import id.andreasmlbngaol.tuner.R

/**
 * Google Sans Flex, pinned to the rounded axis (ROND 100) that gives PixelPlayer
 * its friendly, soft display voice. The font is licensed under the SIL OFL 1.1
 * (see assets/licenses).
 */
private fun googleSans(weight: FontWeight): Font = Font(
    resId = R.font.google_sans_flex,
    weight = weight,
    variationSettings = FontVariation.Settings(
        FontVariation.weight(weight.weight),
        FontVariation.Setting("ROND", 100f),
    ),
)

val GoogleSansRounded = FontFamily(
    googleSans(FontWeight.Light),
    googleSans(FontWeight.Normal),
    googleSans(FontWeight.Medium),
    googleSans(FontWeight.SemiBold),
    googleSans(FontWeight.Bold),
    googleSans(FontWeight.ExtraBold),
    googleSans(FontWeight.Black),
)

@OptIn(ExperimentalTextApi::class)
private fun style(
    size: Int,
    weight: FontWeight,
    lineHeight: Int,
    tracking: Double,
    tightMetrics: Boolean = true,
) = TextStyle(
    fontFamily = GoogleSansRounded,
    fontWeight = weight,
    fontSize = size.sp,
    lineHeight = lineHeight.sp,
    letterSpacing = tracking.em,
    platformStyle = if (tightMetrics) PlatformTextStyle(includeFontPadding = false) else null,
)

@OptIn(ExperimentalTextApi::class)
val AppTypography = Typography(
    displayLarge = style(64, FontWeight.Bold, 64, -0.03),
    displayMedium = style(48, FontWeight.Bold, 50, -0.025),
    displaySmall = style(38, FontWeight.Bold, 42, -0.02),
    headlineLarge = style(32, FontWeight.SemiBold, 38, -0.015),
    headlineMedium = style(28, FontWeight.SemiBold, 34, -0.01),
    headlineSmall = style(24, FontWeight.SemiBold, 30, -0.005),
    titleLarge = style(22, FontWeight.SemiBold, 28, 0.0),
    titleMedium = style(17, FontWeight.SemiBold, 24, 0.0),
    titleSmall = style(15, FontWeight.SemiBold, 20, 0.0),
    bodyLarge = style(16, FontWeight.Normal, 24, 0.0, tightMetrics = false),
    bodyMedium = style(14, FontWeight.Normal, 20, 0.005, tightMetrics = false),
    bodySmall = style(12, FontWeight.Normal, 17, 0.01, tightMetrics = false),
    labelLarge = style(14, FontWeight.SemiBold, 20, 0.01),
    labelMedium = style(12, FontWeight.SemiBold, 16, 0.02),
    labelSmall = style(11, FontWeight.SemiBold, 15, 0.03),

    displayLargeEmphasized = style(64, FontWeight.ExtraBold, 64, -0.03),
    displayMediumEmphasized = style(48, FontWeight.ExtraBold, 50, -0.025),
    displaySmallEmphasized = style(38, FontWeight.ExtraBold, 42, -0.02),
    headlineLargeEmphasized = style(32, FontWeight.Bold, 38, -0.015),
    headlineMediumEmphasized = style(28, FontWeight.Bold, 34, -0.01),
    headlineSmallEmphasized = style(24, FontWeight.Bold, 30, -0.005),
    titleLargeEmphasized = style(22, FontWeight.Bold, 28, 0.0),
    titleMediumEmphasized = style(17, FontWeight.Bold, 24, 0.0),
    titleSmallEmphasized = style(15, FontWeight.Bold, 20, 0.0),
    bodyLargeEmphasized = style(16, FontWeight.Medium, 24, 0.0, tightMetrics = false),
    bodyMediumEmphasized = style(14, FontWeight.Medium, 20, 0.005, tightMetrics = false),
    bodySmallEmphasized = style(12, FontWeight.Medium, 17, 0.01, tightMetrics = false),
    labelLargeEmphasized = style(14, FontWeight.Bold, 20, 0.01),
    labelMediumEmphasized = style(12, FontWeight.Bold, 16, 0.02),
    labelSmallEmphasized = style(11, FontWeight.Bold, 15, 0.03),
)
