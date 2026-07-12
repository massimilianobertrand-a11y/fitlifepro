
package it.fitlifepro.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SectionCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(Modifier.padding(16.dp), content = content)
    }
}

@Composable
fun StatChip(
    icon: ImageVector,
    value: String,
    label: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(color.copy(alpha = 0.12f))
            .padding(horizontal = 14.dp, vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(icon, null, tint = color, modifier = Modifier.size(22.dp))
        Spacer(Modifier.height(4.dp))
        Text(value, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = color)
        Text(label, fontSize = 11.sp, color = color.copy(alpha = 0.7f))
    }
}

@Composable
fun LinearProgressRow(
    label: String,
    current: Int,
    goal: Int,
    color: Color,
    unit: String = ""
) {
    val progress = if (goal > 0) (current.toFloat() / goal).coerceIn(0f, 1f) else 0f
    Column {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
            Text("$current / $goal $unit".trim(), style = MaterialTheme.typography.bodyMedium, color = color)
        }
        Spacer(Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
            color = color,
            trackColor = color.copy(alpha = 0.15f)
        )
    }
}

@Composable
fun SectionHeader(title: String, color: Color, icon: ImageVector) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = color, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(8.dp))
        Text(title, style = MaterialTheme.typography.titleMedium, color = color, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun EmptyState(message: String, icon: ImageVector) {
    Column(
        Modifier.fillMaxWidth().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(icon, null, tint = MaterialTheme.colorScheme.outline, modifier = Modifier.size(48.dp))
        Spacer(Modifier.height(12.dp))
        Text(message, color = MaterialTheme.colorScheme.outline, style = MaterialTheme.typography.bodyMedium)
    }
}
