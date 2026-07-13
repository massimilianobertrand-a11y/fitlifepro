
package it.fitlifepro.app.ui.components

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import it.fitlifepro.app.data.model.Exercise
import it.fitlifepro.app.ui.theme.*

/** Estrae il video ID da URL YouTube di vari formati */
fun extractYouTubeId(url: String): String? {
    val patterns = listOf(
        Regex("(?:youtube\\.com/watch\\?v=|youtu\\.be/|youtube\\.com/shorts/)([a-zA-Z0-9_-]{11})"),
        Regex("youtube\\.com/embed/([a-zA-Z0-9_-]{11})")
    )
    for (p in patterns) {
        val m = p.find(url)
        if (m != null) return m.groupValues[1]
    }
    return null
}

fun isImageUrl(url: String): Boolean {
    val lower = url.lowercase()
    return lower.endsWith(".jpg") || lower.endsWith(".jpeg") ||
           lower.endsWith(".png") || lower.endsWith(".gif") ||
           lower.endsWith(".webp")
}

/**
 * Anteprima compatta del media assegnato all'esercizio.
 * Mostrata inline nella scheda esercizio durante l'allenamento attivo.
 * Non visibile se videoUrl è vuoto.
 */
@Composable
fun ExerciseMediaPreview(
    videoUrl: String,
    modifier: Modifier = Modifier
) {
    if (videoUrl.isBlank()) return

    val context = LocalContext.current
    val ytId = remember(videoUrl) { extractYouTubeId(videoUrl) }
    val isImage = remember(videoUrl) { isImageUrl(videoUrl) }

    fun openUrl() {
        context.startActivity(
            Intent(Intent.ACTION_VIEW, Uri.parse(videoUrl)).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        )
    }

    when {
        // ── YouTube thumbnail con overlay play ──────────────────────────
        ytId != null -> {
            val thumbUrl = "https://img.youtube.com/vi/$ytId/mqdefault.jpg"
            Box(
                modifier = modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .border(1.dp, Orange500.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                    .clickable { openUrl() }
            ) {
                AsyncImage(
                    model = thumbUrl,
                    contentDescription = "Video tutorial",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                // Dark overlay
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.25f))
                )
                // Play button overlay
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    FilledIconButton(
                        onClick = ::openUrl,
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = Orange500.copy(alpha = 0.9f)
                        ),
                        modifier = Modifier.size(56.dp)
                    ) {
                        Icon(
                            Icons.Default.PlayArrow,
                            contentDescription = "Apri video",
                            modifier = Modifier.size(32.dp),
                            tint = Color.White
                        )
                    }
                }
                // Label bottom-left
                Text(
                    "▶ YouTube · tocca per aprire",
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(10.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color.Black.copy(alpha = 0.55f))
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White
                )
            }
        }
        // ── Immagine diretta ────────────────────────────────────────────
        isImage -> {
            Box(
                modifier = modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .border(1.dp, Orange500.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
            ) {
                AsyncImage(
                    model = videoUrl,
                    contentDescription = "Immagine esercizio",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )
            }
        }
        // ── Link generico ───────────────────────────────────────────────
        else -> {
            OutlinedButton(
                onClick = ::openUrl,
                modifier = modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Orange500)
            ) {
                Icon(Icons.Default.OpenInBrowser, null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Apri riferimento esercizio", maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExerciseMediaSheet(
    exercise: Exercise,
    onDismiss: () -> Unit,
    onSave: (Exercise, String) -> Unit
) {
    val context = LocalContext.current
    val keyboard = LocalSoftwareKeyboardController.current

    var urlInput by remember { mutableStateOf(exercise.videoUrl) }
    var previewUrl by remember { mutableStateOf(exercise.videoUrl) }

    val ytId = remember(previewUrl) { extractYouTubeId(previewUrl) }
    val isImage = remember(previewUrl) { isImageUrl(previewUrl) }
    val hasMedia = previewUrl.isNotBlank()

    val youtubeSearchUrl = "https://www.youtube.com/results?search_query=${Uri.encode(exercise.name + " tutorial")}"
    val googleImgUrl    = "https://www.google.com/search?tbm=isch&q=${Uri.encode(exercise.name + " exercise")}"

    fun openUrl(url: String) {
        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        })
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.VideoLibrary, null, tint = Orange500,
                    modifier = Modifier.size(28.dp))
                Spacer(Modifier.width(10.dp))
                Column(Modifier.weight(1f)) {
                    Text("Media Esercizio",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold)
                    Text(exercise.name,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.outline,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis)
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, null)
                }
            }

            HorizontalDivider()

            // Search buttons
            Text("Cerca online",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.outline)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = { openUrl(youtubeSearchUrl) },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFFF0000))
                ) {
                    Icon(Icons.Default.PlayCircle, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("YouTube", maxLines = 1)
                }
                OutlinedButton(
                    onClick = { openUrl(googleImgUrl) },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF4285F4))
                ) {
                    Icon(Icons.Default.Image, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Immagini", maxLines = 1)
                }
            }

            Text("Incolla il link trovato",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.outline)

            // URL input
            OutlinedTextField(
                value = urlInput,
                onValueChange = { urlInput = it },
                label = { Text("URL video o immagine") },
                placeholder = { Text("https://youtu.be/... oppure https://...") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                trailingIcon = {
                    if (urlInput.isNotBlank()) {
                        IconButton(onClick = { urlInput = ""; previewUrl = "" }) {
                            Icon(Icons.Default.Clear, null)
                        }
                    }
                },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = {
                    keyboard?.hide()
                    previewUrl = urlInput
                })
            )

            // Preview button
            if (urlInput.isNotBlank() && urlInput != previewUrl) {
                TextButton(
                    onClick = { previewUrl = urlInput; keyboard?.hide() },
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Icon(Icons.Default.Visibility, null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Anteprima")
                }
            }

            // Media preview
            if (hasMedia) {
                val thumbnailUrl = when {
                    ytId != null -> "https://img.youtube.com/vi/$ytId/mqdefault.jpg"
                    isImage -> previewUrl
                    else -> null
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .border(1.dp, Orange500.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (ytId != null) Icons.Default.SmartDisplay
                                          else if (isImage) Icons.Default.Image
                                          else Icons.Default.Link,
                            contentDescription = null,
                            tint = Orange500,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = when {
                                ytId != null -> "Video YouTube"
                                isImage -> "Immagine"
                                else -> "Link salvato"
                            },
                            style = MaterialTheme.typography.labelLarge,
                            color = Orange500
                        )
                        if (ytId != null || isImage) {
                            Spacer(Modifier.weight(1f))
                            IconButton(
                                onClick = { openUrl(previewUrl) },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(Icons.Default.OpenInNew, null,
                                    modifier = Modifier.size(16.dp),
                                    tint = MaterialTheme.colorScheme.outline)
                            }
                        }
                    }

                    if (thumbnailUrl != null) {
                        AsyncImage(
                            model = thumbnailUrl,
                            contentDescription = "Anteprima",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                                .clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        // Generic link — show URL
                        Text(
                            text = previewUrl,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        OutlinedButton(
                            onClick = { openUrl(previewUrl) },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.OpenInBrowser, null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Apri nel browser")
                        }
                    }
                }
            }

            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (exercise.videoUrl.isNotBlank()) {
                    OutlinedButton(
                        onClick = {
                            urlInput = ""
                            previewUrl = ""
                            onSave(exercise, "")
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Icon(Icons.Default.Delete, null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Rimuovi")
                    }
                }
                Button(
                    onClick = {
                        onSave(exercise, urlInput.trim())
                        onDismiss()
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Orange500),
                    enabled = urlInput.trim() != exercise.videoUrl
                ) {
                    Icon(Icons.Default.Save, null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Salva")
                }
            }
        }
    }
}
