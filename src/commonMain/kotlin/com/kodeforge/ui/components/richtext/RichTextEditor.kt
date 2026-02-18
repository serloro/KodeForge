package com.kodeforge.ui.components.richtext

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Simple one-piece WYSIWYG editor:
 * - A single editable surface (BasicTextField)
 * - Styles rendered via VisualTransformation (cursor/selection offsets remain stable)
 * - Inline formatting via marks stored separately
 */
@Composable
fun RichTextEditor(
    value: TextFieldValue,
    marks: List<Mark>,
    onValueChange: (TextFieldValue) -> Unit,
    onMarksChange: (List<Mark>) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "Escribe...",
) {
    val scrollState = rememberScrollState()
    val focusRequester = remember { FocusRequester() }

    // Keep the last known selection even if a toolbar click briefly steals focus.
    var lastSelection by remember { mutableStateOf(value.selection) }
    LaunchedEffect(value.selection) {
        lastSelection = value.selection
    }

    Column(modifier = modifier) {
        RichTextToolbar(
            enabled = true,
            onBold = {
                focusRequester.requestFocus()
                onMarksChange(RichTextOps.toggleMark(marks, lastSelection, MarkType.BOLD))
            },
            onItalic = {
                focusRequester.requestFocus()
                onMarksChange(RichTextOps.toggleMark(marks, lastSelection, MarkType.ITALIC))
            },
            onUnderline = {
                focusRequester.requestFocus()
                onMarksChange(RichTextOps.toggleMark(marks, lastSelection, MarkType.UNDERLINE))
            },
            onStrike = {
                focusRequester.requestFocus()
                onMarksChange(RichTextOps.toggleMark(marks, lastSelection, MarkType.STRIKE))
            },
            onSetColor = { hex ->
                focusRequester.requestFocus()
                onMarksChange(RichTextOps.setFontColor(marks, lastSelection, hex))
            },
            onSetFontFamily = { family ->
                focusRequester.requestFocus()
                onMarksChange(RichTextOps.setFontFamily(marks, lastSelection, family))
            },
            onBullets = {
                focusRequester.requestFocus()
                val (newText, newSel) = RichTextOps.applyBullets(value.text, lastSelection)
                onValueChange(TextFieldValue(newText, selection = newSel))
            },
            onFontDown = {
                focusRequester.requestFocus()
                onMarksChange(RichTextOps.bumpFontSize(marks, lastSelection, deltaSp = -1))
            },
            onFontUp = {
                focusRequester.requestFocus()
                onMarksChange(RichTextOps.bumpFontSize(marks, lastSelection, deltaSp = 1))
            },
        )

        Spacer(Modifier.height(8.dp))

        val borderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)

        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxSize()
                .focusRequester(focusRequester)
                .border(1.dp, borderColor, MaterialTheme.shapes.medium)
                .background(MaterialTheme.colorScheme.surface, MaterialTheme.shapes.medium)
                .padding(12.dp)
                .verticalScroll(scrollState),
            textStyle = LocalTextStyle.current.merge(
                TextStyle(
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            ),
            visualTransformation = remember(marks) { RichVisualTransformation(marks) },
            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
            decorationBox = { inner ->
                Box {
                    if (value.text.isEmpty()) {
                        Text(
                            text = placeholder,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                            fontSize = 14.sp
                        )
                    }
                    inner()
                }
            }
        )
    }
}

@Composable
private fun RichTextToolbar(
    enabled: Boolean,
    onBold: () -> Unit,
    onItalic: () -> Unit,
    onUnderline: () -> Unit,
    onStrike: () -> Unit,
    onSetColor: (String) -> Unit,
    onSetFontFamily: (String) -> Unit,
    onBullets: () -> Unit,
    onFontDown: () -> Unit,
    onFontUp: () -> Unit,
) {
    var showColorMenu by remember { mutableStateOf(false) }
    var showFontMenu by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        // Avoid material-icons dependency (not always included in KMP templates).
        // Also avoid stealing focus from the editor (otherwise selection collapses).
        fun Modifier.noFocusOnClick(): Modifier = this.focusProperties { canFocus = false }

        @Composable
        fun ToolButton(
            iconText: String,
            label: String,
            onClick: () -> Unit,
            iconStyle: TextStyle = LocalTextStyle.current,
        ) {
            FilledTonalButton(
                onClick = onClick,
                enabled = enabled,
                modifier = Modifier
                    .noFocusOnClick()
                    .heightIn(min = 44.dp),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(iconText, style = iconStyle)
                    Spacer(Modifier.height(2.dp))
                    Text(label, style = MaterialTheme.typography.labelSmall)
                }
            }
        }

        ToolButton(
            iconText = "B",
            label = "Negrita",
            onClick = onBold,
            iconStyle = LocalTextStyle.current.merge(TextStyle(fontWeight = FontWeight.Bold))
        )

        ToolButton(
            iconText = "I",
            label = "Cursiva",
            onClick = onItalic,
            iconStyle = LocalTextStyle.current.merge(TextStyle(fontStyle = FontStyle.Italic))
        )

        ToolButton(
            iconText = "U",
            label = "Subrayar",
            onClick = onUnderline,
            iconStyle = LocalTextStyle.current.merge(TextStyle(textDecoration = TextDecoration.Underline))
        )

        ToolButton(
            iconText = "S̶",
            label = "Tachado",
            onClick = onStrike,
            iconStyle = LocalTextStyle.current.merge(TextStyle(textDecoration = TextDecoration.LineThrough))
        )

        Box {
            ToolButton(
                iconText = "A",
                label = "Color",
                onClick = { showColorMenu = true },
            )
            DropdownMenu(
                expanded = showColorMenu,
                onDismissRequest = { showColorMenu = false },
            ) {
                @Composable
                fun item(name: String, hex: String) {
                    DropdownMenuItem(
                        text = { Text(name) },
                        onClick = {
                            showColorMenu = false
                            onSetColor(hex)
                        }
                    )
                }
                item("Negro", "#000000")
                item("Rojo", "#d32f2f")
                item("Azul", "#1976d2")
                item("Verde", "#388e3c")
                item("Morado", "#7b1fa2")
            }
        }

        Box {
            ToolButton(
                iconText = "Aa",
                label = "Fuente",
                onClick = { showFontMenu = true },
            )
            DropdownMenu(
                expanded = showFontMenu,
                onDismissRequest = { showFontMenu = false },
            ) {
                DropdownMenuItem(
                    text = { Text("Sans") },
                    onClick = {
                        showFontMenu = false
                        onSetFontFamily("sans")
                    }
                )
                DropdownMenuItem(
                    text = { Text("Serif") },
                    onClick = {
                        showFontMenu = false
                        onSetFontFamily("serif")
                    }
                )
                DropdownMenuItem(
                    text = { Text("Mono") },
                    onClick = {
                        showFontMenu = false
                        onSetFontFamily("mono")
                    }
                )
            }
        }

        Spacer(Modifier.width(6.dp))

        ToolButton(
            iconText = "≡",
            label = "Lista",
            onClick = onBullets,
        )

        ToolButton(
            iconText = "A−",
            label = "Pequeña",
            onClick = onFontDown,
        )

        ToolButton(
            iconText = "A+",
            label = "Grande",
            onClick = onFontUp,
        )
    }
}

private class RichVisualTransformation(
    private val marks: List<Mark>
) : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val styled = buildAnnotatedString {
            append(text.text)

            val len = text.length
            for (m in marks) {
                val s = m.start.coerceIn(0, len)
                val e = m.end.coerceIn(0, len)
                if (e <= s) continue

                when (m.type) {
                    MarkType.BOLD -> addStyle(SpanStyle(fontWeight = FontWeight.Bold), s, e)
                    MarkType.ITALIC -> addStyle(SpanStyle(fontStyle = FontStyle.Italic), s, e)
                    MarkType.UNDERLINE -> addStyle(SpanStyle(textDecoration = TextDecoration.Underline), s, e)
                    MarkType.STRIKE -> addStyle(SpanStyle(textDecoration = TextDecoration.LineThrough), s, e)
                    MarkType.CODE -> addStyle(
                        SpanStyle(
                            fontFamily = FontFamily.Monospace,
                            background = Color(0x11000000)
                        ),
                        s,
                        e
                    )
                    MarkType.LINK -> {
                        addStyle(
                            SpanStyle(
                                textDecoration = TextDecoration.Underline,
                                color = Color(0xFF1565C0)
                            ),
                            s,
                            e
                        )
                        addStringAnnotation(tag = "link", annotation = m.data ?: "", start = s, end = e)
                    }
                    MarkType.FONT_SIZE -> {
                        val sizeSp = m.data?.toIntOrNull()
                        if (sizeSp != null) {
                            addStyle(SpanStyle(fontSize = sizeSp.sp), s, e)
                        }
                    }
                    MarkType.FONT_COLOR -> {
                        val c = parseHexColorOrNull(m.data)
                        if (c != null) addStyle(SpanStyle(color = c), s, e)
                    }
                    MarkType.FONT_FAMILY -> {
                        val fam = when (m.data) {
                            "serif" -> FontFamily.Serif
                            "mono" -> FontFamily.Monospace
                            else -> FontFamily.SansSerif
                        }
                        addStyle(SpanStyle(fontFamily = fam), s, e)
                    }
                }
            }
        }

        return TransformedText(styled, OffsetMapping.Identity)
    }
}

private fun parseHexColorOrNull(hex: String?): Color? {
    if (hex == null) return null
    val h = hex.trim().lowercase()
    if (!Regex("^#[0-9a-f]{6}$").matches(h)) return null
    return try {
        val rgb = h.removePrefix("#").toLong(16)
        Color((0xFF000000 or rgb).toInt())
    } catch (_: Exception) {
        null
    }
}
