package top.foxmoe.releasely.shared.designsystem.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.Shapes
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Typography

private val MintPrimary = Color(0xFF0EA5A4)
private val SkySecondary = Color(0xFF3B82F6)
private val DeepBlue = Color(0xFF1E293B)
private val SlateGray = Color(0xFF5C6773)
private val LightBackground = Color(0xFFF4F8FB)
private val DarkBackground = Color(0xFF0F172A)

private val LightColors: ColorScheme = lightColorScheme(
    primary = MintPrimary,
    secondary = SkySecondary,
    tertiary = DeepBlue,
    background = LightBackground,
    surface = Color.White,
    outline = Color(0xFFB6C5D5),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = Color(0xFF111827),
    onSurface = Color(0xFF1F2937)
)

private val DarkColors: ColorScheme = darkColorScheme(
    primary = Color(0xFF2DD4BF),
    secondary = Color(0xFF60A5FA),
    tertiary = Color(0xFF93C5FD),
    background = DarkBackground,
    surface = Color(0xFF1E293B),
    outline = Color(0xFF475569),
    onPrimary = Color.Black,
    onSecondary = Color.Black,
    onBackground = Color(0xFFE5E7EB),
    onSurface = Color(0xFFD1D5DB)
)

private val ReleaselyTypography = Typography(
    displayLarge = TextStyle(
        fontSize = 34.sp,
        lineHeight = 38.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = (-0.2).sp
    ),
    bodyLarge = TextStyle(
        fontSize = 16.sp,
        lineHeight = 24.sp,
        fontWeight = FontWeight.Medium
    ),
    bodyMedium = TextStyle(
        fontSize = 14.sp,
        lineHeight = 20.sp,
        fontWeight = FontWeight.Normal,
        color = SlateGray
    ),
    labelSmall = TextStyle(
        fontSize = 12.sp,
        lineHeight = 16.sp,
        fontWeight = FontWeight.SemiBold,
        color = SlateGray
    )
)

private val ReleaselyShapes = Shapes(
    small = RoundedCornerShape(14.dp),
    medium = RoundedCornerShape(18.dp),
    large = RoundedCornerShape(26.dp)
)

@Composable
fun ReleaselyTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = ReleaselyTypography,
        shapes = ReleaselyShapes,
        content = content
    )
}
