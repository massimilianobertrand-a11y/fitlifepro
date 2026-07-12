
package it.fitlifepro.app.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val Indigo500  = Color(0xFF5B6AF5)
val Indigo700  = Color(0xFF3949AB)
val Orange500  = Color(0xFFF5622E)
val Green500   = Color(0xFF22B55E)
val Purple500  = Color(0xFF9C27B0)
val Blue500    = Color(0xFF0288D1)
val Amber500   = Color(0xFFFF8F00)
val Surface    = Color(0xFFF4F6FA)
val OnSurface  = Color(0xFF1A1D2E)
val Card       = Color(0xFFFFFFFF)
val Subtle     = Color(0xFF9AA0B4)

private val LightColors = lightColorScheme(
    primary          = Indigo500,
    onPrimary        = Color.White,
    primaryContainer = Color(0xFFE8EAFF),
    secondary        = Orange500,
    onSecondary      = Color.White,
    tertiary         = Green500,
    background       = Surface,
    surface          = Card,
    onBackground     = OnSurface,
    onSurface        = OnSurface,
    surfaceVariant   = Color(0xFFEEF0F8),
    outline          = Color(0xFFDDE0EE)
)

@Composable
fun FitLifeTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColors,
        typography  = Typography(),
        content     = content
    )
}
