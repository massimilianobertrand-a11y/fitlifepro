
package it.fitlifepro.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

fun Typography() = Typography(
    headlineLarge  = TextStyle(fontWeight = FontWeight.Bold,   fontSize = 24.sp, lineHeight = 30.sp),
    headlineMedium = TextStyle(fontWeight = FontWeight.Bold,   fontSize = 20.sp, lineHeight = 26.sp),
    titleLarge     = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 18.sp),
    titleMedium    = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 15.sp),
    bodyLarge      = TextStyle(fontWeight = FontWeight.Normal, fontSize = 15.sp),
    bodyMedium     = TextStyle(fontWeight = FontWeight.Normal, fontSize = 13.sp),
    labelLarge     = TextStyle(fontWeight = FontWeight.Medium, fontSize = 12.sp),
    labelMedium    = TextStyle(fontWeight = FontWeight.Medium, fontSize = 11.sp),
)
