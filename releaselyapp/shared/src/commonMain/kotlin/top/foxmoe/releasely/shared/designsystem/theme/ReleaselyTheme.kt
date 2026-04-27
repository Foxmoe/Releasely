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

private val CoralRed = Color(0xFFFF6F61)
private val VitalOrange = Color(0xFFFF8C42)
private val DeepBlue = Color(0xFF1D3557)
private val SlateGray = Color(0xFF5C6773)
private val LightBackground = Color(0xFFF8FAFC)
private val DarkBackground = Color(0xFF101622)

private val LightColors: ColorScheme = lightColorScheme(
    primary = CoralRed,
    secondary = VitalOrange,
    tertiary = DeepBlue,
    background = LightBackground,
    surface = Color.White,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = Color(0xFF111827),
    onSurface = Color(0xFF1F2937)
)

private val DarkColors: ColorScheme = darkColorScheme(
    primary = VitalOrange,
    secondary = CoralRed,
    tertiary = Color(0xFF7FB3FF),
    background = DarkBackground,
    surface = Color(0xFF182234),
    onPrimary = Color.Black,
    onSecondary = Color.Black,
    onBackground = Color(0xFFE5E7EB),
    onSurface = Color(0xFFD1D5DB)
)

private val ReleaselyTypography = Typography(
    displayLarge = TextStyle(
        fontSize = 32.sp,
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
    small = RoundedCornerShape(16.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(24.dp)
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
