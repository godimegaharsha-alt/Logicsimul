package com.example.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

enum class AppTheme(
    val id: String,
    val title: String,
    val subtitle: String,
    val primaryColor: Color,
    val secondaryColor: Color,
    val canvasBg: Color,
    val panelBg: Color,
    val isDark: Boolean
) {
    CYBER_OBSIDIAN(
        id = "cyber_obsidian",
        title = "Cyber Dark",
        subtitle = "Obsidian CAD • Neon Cyan",
        primaryColor = Color(0xFF00E5FF),
        secondaryColor = Color(0xFFFFB300),
        canvasBg = Color(0xFF0A0E14),
        panelBg = Color(0xFF131B26),
        isDark = true
    ),
    SILICON_BLUEPRINT(
        id = "silicon_blueprint",
        title = "Silicon Blueprint",
        subtitle = "Navy Engineering • Blue Trace",
        primaryColor = Color(0xFF38BDF8),
        secondaryColor = Color(0xFF90E0EF),
        canvasBg = Color(0xFF071324),
        panelBg = Color(0xFF0E2244),
        isDark = true
    ),
    MATRIX_PHOSPHOR(
        id = "matrix_phosphor",
        title = "Matrix Terminal",
        subtitle = "CRT Phosphor • Hacker Green",
        primaryColor = Color(0xFF00FF66),
        secondaryColor = Color(0xFF39FF14),
        canvasBg = Color(0xFF020D05),
        panelBg = Color(0xFF071F10),
        isDark = true
    ),
    VINTAGE_AMBER(
        id = "vintage_amber",
        title = "Vintage Amber",
        subtitle = "Retro CRT Oscilloscope",
        primaryColor = Color(0xFFFFB300),
        secondaryColor = Color(0xFFFF8F00),
        canvasBg = Color(0xFF120A03),
        panelBg = Color(0xFF241407),
        isDark = true
    ),
    SYNTHWAVE_NEON(
        id = "synthwave_neon",
        title = "Synthwave 80s",
        subtitle = "Cyber Violet • Neon Pink & Cyan",
        primaryColor = Color(0xFFFF2A85),
        secondaryColor = Color(0xFF00F0FF),
        canvasBg = Color(0xFF0D021A),
        panelBg = Color(0xFF190633),
        isDark = true
    ),
    CRIMSON_CORE(
        id = "crimson_core",
        title = "Crimson Core",
        subtitle = "Carbon Fiber • Laser Red Pulse",
        primaryColor = Color(0xFFFF3366),
        secondaryColor = Color(0xFFFF7597),
        canvasBg = Color(0xFF110508),
        panelBg = Color(0xFF210B11),
        isDark = true
    ),
    SOLAR_BRASS(
        id = "solar_brass",
        title = "Industrial Brass",
        subtitle = "Steampunk Bronze • Copper Glow",
        primaryColor = Color(0xFFE6A23C),
        secondaryColor = Color(0xFFF59E0B),
        canvasBg = Color(0xFF140F0A),
        panelBg = Color(0xFF231B12),
        isDark = true
    ),
    MINIMAL_OLED(
        id = "minimal_oled",
        title = "OLED Monochrome",
        subtitle = "Pitch Black • High-Contrast Silver",
        primaryColor = Color(0xFFFFFFFF),
        secondaryColor = Color(0xFF00E5FF),
        canvasBg = Color(0xFF000000),
        panelBg = Color(0xFF0F0F0F),
        isDark = true
    ),
    SOLARIZED_LIGHT(
        id = "solarized_light",
        title = "Solarized Light",
        subtitle = "Clean Daytime Lab • White PCB",
        primaryColor = Color(0xFF0284C7),
        secondaryColor = Color(0xFF0D9488),
        canvasBg = Color(0xFFF1F5F9),
        panelBg = Color(0xFFFFFFFF),
        isDark = false
    )
}

data class LogicLabColors(
    val darkBg: Color,
    val canvasBg: Color,
    val panelBg: Color,
    val cardBg: Color,
    val border: Color,
    val borderSubtle: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textTertiary: Color,
    val accentPrimary: Color,
    val accentSecondary: Color,
    val onAccent: Color,
    val signalHigh: Color,
    val signalLow: Color,
    val signalUnknown: Color,
    val signalError: Color,
    val gridDotColor: Color
)

fun getLogicLabColors(theme: AppTheme): LogicLabColors {
    return when (theme) {
        AppTheme.CYBER_OBSIDIAN -> LogicLabColors(
            darkBg = Color(0xFF0A0E14),
            canvasBg = Color(0xFF0D1219),
            panelBg = Color(0xFF131B26),
            cardBg = Color(0xFF1A2433),
            border = Color(0xFF26364D),
            borderSubtle = Color(0xFF1B283A),
            textPrimary = Color(0xFFF0F6FC),
            textSecondary = Color(0xFF8B949E),
            textTertiary = Color(0xFF566275),
            accentPrimary = Color(0xFF00E5FF),
            accentSecondary = Color(0xFFFFB300),
            onAccent = Color(0xFF0A0E14),
            signalHigh = Color(0xFF00E676),
            signalLow = Color(0xFF484F58),
            signalUnknown = Color(0xFFD29922),
            signalError = Color(0xFFF85149),
            gridDotColor = Color(0xFF1F2B3E)
        )
        AppTheme.SILICON_BLUEPRINT -> LogicLabColors(
            darkBg = Color(0xFF071324),
            canvasBg = Color(0xFF0A182F),
            panelBg = Color(0xFF0E2244),
            cardBg = Color(0xFF152E59),
            border = Color(0xFF1D447F),
            borderSubtle = Color(0xFF133261),
            textPrimary = Color(0xFFF0F9FF),
            textSecondary = Color(0xFF7DD3FC),
            textTertiary = Color(0xFF38BDF8),
            accentPrimary = Color(0xFF38BDF8),
            accentSecondary = Color(0xFF90E0EF),
            onAccent = Color(0xFF071324),
            signalHigh = Color(0xFF34D399),
            signalLow = Color(0xFF334155),
            signalUnknown = Color(0xFFFBBF24),
            signalError = Color(0xFFF87171),
            gridDotColor = Color(0xFF163259)
        )
        AppTheme.MATRIX_PHOSPHOR -> LogicLabColors(
            darkBg = Color(0xFF020D05),
            canvasBg = Color(0xFF041408),
            panelBg = Color(0xFF071F10),
            cardBg = Color(0xFF0C2C17),
            border = Color(0xFF144725),
            borderSubtle = Color(0xFF0B331A),
            textPrimary = Color(0xFFECFDF5),
            textSecondary = Color(0xFF6EE7B7),
            textTertiary = Color(0xFF34D399),
            accentPrimary = Color(0xFF00FF66),
            accentSecondary = Color(0xFF39FF14),
            onAccent = Color(0xFF020D05),
            signalHigh = Color(0xFF00FF66),
            signalLow = Color(0xFF064E3B),
            signalUnknown = Color(0xFFFBBF24),
            signalError = Color(0xFFEF4444),
            gridDotColor = Color(0xFF0D3B1B)
        )
        AppTheme.VINTAGE_AMBER -> LogicLabColors(
            darkBg = Color(0xFF120A03),
            canvasBg = Color(0xFF1A0E05),
            panelBg = Color(0xFF241407),
            cardBg = Color(0xFF331D0A),
            border = Color(0xFF522E10),
            borderSubtle = Color(0xFF3D220C),
            textPrimary = Color(0xFFFFFBEB),
            textSecondary = Color(0xFFFCD34D),
            textTertiary = Color(0xFFF59E0B),
            accentPrimary = Color(0xFFFFB300),
            accentSecondary = Color(0xFFFF8F00),
            onAccent = Color(0xFF120A03),
            signalHigh = Color(0xFFFFB300),
            signalLow = Color(0xFF78350F),
            signalUnknown = Color(0xFFFDE68A),
            signalError = Color(0xFFDC2626),
            gridDotColor = Color(0xFF3D220C)
        )
        AppTheme.SYNTHWAVE_NEON -> LogicLabColors(
            darkBg = Color(0xFF0D021A),
            canvasBg = Color(0xFF120324),
            panelBg = Color(0xFF190633),
            cardBg = Color(0xFF240B48),
            border = Color(0xFF4C1D95),
            borderSubtle = Color(0xFF3B0764),
            textPrimary = Color(0xFFFAF5FF),
            textSecondary = Color(0xFFD8B4FE),
            textTertiary = Color(0xFFA855F7),
            accentPrimary = Color(0xFFFF2A85),
            accentSecondary = Color(0xFF00F0FF),
            onAccent = Color.White,
            signalHigh = Color(0xFF00F0FF),
            signalLow = Color(0xFF581C87),
            signalUnknown = Color(0xFFFDE047),
            signalError = Color(0xFFFF0055),
            gridDotColor = Color(0xFF3B0764)
        )
        AppTheme.CRIMSON_CORE -> LogicLabColors(
            darkBg = Color(0xFF110508),
            canvasBg = Color(0xFF17070B),
            panelBg = Color(0xFF210B11),
            cardBg = Color(0xFF2F1019),
            border = Color(0xFF5C1D2E),
            borderSubtle = Color(0xFF3F131F),
            textPrimary = Color(0xFFFFF1F2),
            textSecondary = Color(0xFFFDA4AF),
            textTertiary = Color(0xFFFB7185),
            accentPrimary = Color(0xFFFF3366),
            accentSecondary = Color(0xFFFF7597),
            onAccent = Color.White,
            signalHigh = Color(0xFF34D399),
            signalLow = Color(0xFF3F4652),
            signalUnknown = Color(0xFFFBBF24),
            signalError = Color(0xFFFF1E44),
            gridDotColor = Color(0xFF3E131F)
        )
        AppTheme.SOLAR_BRASS -> LogicLabColors(
            darkBg = Color(0xFF140F0A),
            canvasBg = Color(0xFF1B140E),
            panelBg = Color(0xFF231B12),
            cardBg = Color(0xFF32271A),
            border = Color(0xFF5A442E),
            borderSubtle = Color(0xFF3F3020),
            textPrimary = Color(0xFFFEF3C7),
            textSecondary = Color(0xFFFCD34D),
            textTertiary = Color(0xFFB45309),
            accentPrimary = Color(0xFFE6A23C),
            accentSecondary = Color(0xFFF59E0B),
            onAccent = Color(0xFF140F0A),
            signalHigh = Color(0xFF10B981),
            signalLow = Color(0xFF451A03),
            signalUnknown = Color(0xFFFDE68A),
            signalError = Color(0xFFEF4444),
            gridDotColor = Color(0xFF3D2E1F)
        )
        AppTheme.MINIMAL_OLED -> LogicLabColors(
            darkBg = Color(0xFF000000),
            canvasBg = Color(0xFF000000),
            panelBg = Color(0xFF0A0A0A),
            cardBg = Color(0xFF141414),
            border = Color(0xFF333333),
            borderSubtle = Color(0xFF222222),
            textPrimary = Color(0xFFFFFFFF),
            textSecondary = Color(0xFFAAAAAA),
            textTertiary = Color(0xFF777777),
            accentPrimary = Color(0xFFFFFFFF),
            accentSecondary = Color(0xFF00E5FF),
            onAccent = Color(0xFF000000), // High-contrast black text on OLED white accent elements
            signalHigh = Color(0xFF00FF7F),
            signalLow = Color(0xFF333333),
            signalUnknown = Color(0xFFFFCC00),
            signalError = Color(0xFFFF3333),
            gridDotColor = Color(0xFF222222)
        )
        AppTheme.SOLARIZED_LIGHT -> LogicLabColors(
            darkBg = Color(0xFFF1F5F9),
            canvasBg = Color(0xFFF8FAFC),
            panelBg = Color(0xFFFFFFFF),
            cardBg = Color(0xFFF1F5F9),
            border = Color(0xFFCBD5E1),
            borderSubtle = Color(0xFFE2E8F0),
            textPrimary = Color(0xFF0F172A),
            textSecondary = Color(0xFF475569),
            textTertiary = Color(0xFF64748B),
            accentPrimary = Color(0xFF0284C7),
            accentSecondary = Color(0xFF0D9488),
            onAccent = Color.White,
            signalHigh = Color(0xFF16A34A),
            signalLow = Color(0xFF94A3B8),
            signalUnknown = Color(0xFFD97706),
            signalError = Color(0xFFDC2626),
            gridDotColor = Color(0xFFCBD5E1)
        )
    }
}

val LocalLogicLabColors = staticCompositionLocalOf {
    getLogicLabColors(AppTheme.CYBER_OBSIDIAN)
}

fun getMaterialColorScheme(theme: AppTheme): ColorScheme {
    val colors = getLogicLabColors(theme)
    return if (theme.isDark) {
        darkColorScheme(
            primary = colors.accentPrimary,
            onPrimary = colors.onAccent,
            primaryContainer = colors.cardBg,
            onPrimaryContainer = colors.textPrimary,
            secondary = colors.accentSecondary,
            onSecondary = colors.onAccent,
            secondaryContainer = colors.cardBg,
            onSecondaryContainer = colors.textPrimary,
            tertiary = colors.signalHigh,
            onTertiary = colors.darkBg,
            background = colors.darkBg,
            onBackground = colors.textPrimary,
            surface = colors.panelBg,
            onSurface = colors.textPrimary,
            surfaceVariant = colors.cardBg,
            onSurfaceVariant = colors.textSecondary,
            outline = colors.border,
            outlineVariant = colors.borderSubtle,
            error = colors.signalError,
            onError = colors.textPrimary
        )
    } else {
        lightColorScheme(
            primary = colors.accentPrimary,
            onPrimary = Color.White,
            primaryContainer = Color(0xFFE0F2FE),
            onPrimaryContainer = Color(0xFF0369A1),
            secondary = colors.accentSecondary,
            onSecondary = Color.White,
            secondaryContainer = Color(0xFFCCFBF1),
            onSecondaryContainer = Color(0xFF0F766E),
            tertiary = colors.signalHigh,
            onTertiary = Color.White,
            background = colors.darkBg,
            onBackground = colors.textPrimary,
            surface = colors.panelBg,
            onSurface = colors.textPrimary,
            surfaceVariant = colors.cardBg,
            onSurfaceVariant = colors.textSecondary,
            outline = colors.border,
            outlineVariant = colors.borderSubtle,
            error = colors.signalError,
            onError = Color.White
        )
    }
}

@Composable
fun LogicLabTheme(
    theme: AppTheme = AppTheme.CYBER_OBSIDIAN,
    content: @Composable () -> Unit
) {
    val colors = getLogicLabColors(theme)
    val colorScheme = getMaterialColorScheme(theme)

    CompositionLocalProvider(
        LocalLogicLabColors provides colors
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}

object LogicLabDesign {
    val colors: LogicLabColors
        @Composable
        @ReadOnlyComposable
        get() = LocalLogicLabColors.current
}

// Retain alias for any existing reference
@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    LogicLabTheme(content = content)
}
