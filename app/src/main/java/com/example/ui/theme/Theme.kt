package com.example.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val GlassDarkColorScheme = darkColorScheme(
    primary = ArcticCyan,
    onPrimary = ObsidianVoid,
    primaryContainer = Color(0xFF16324A),
    onPrimaryContainer = IceWhite,
    secondary = CrystalMint,
    onSecondary = ObsidianVoid,
    secondaryContainer = Color(0xFF13332C),
    onSecondaryContainer = IceWhite,
    tertiary = VioletFrost,
    onTertiary = ObsidianVoid,
    background = ObsidianVoid,
    onBackground = IceWhite,
    surface = ObsidianBase,
    onSurface = IceWhite,
    surfaceVariant = ObsidianElevated,
    onSurfaceVariant = MistSilver,
    outline = GlassBorderSubtle,
    outlineVariant = GlassBorderBright,
    error = DangerCoral,
    onError = ObsidianVoid
)

val GlassShapes = Shapes(
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(20.dp),
    large = RoundedCornerShape(28.dp),
    extraLarge = RoundedCornerShape(32.dp)
)

@Composable
fun GlassCalendarTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = GlassDarkColorScheme,
        typography = GlassTypography,
        shapes = GlassShapes,
        content = content
    )
}
