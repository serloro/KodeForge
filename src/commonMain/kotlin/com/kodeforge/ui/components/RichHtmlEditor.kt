package com.kodeforge.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.kodeforge.ui.components.richtext.*

/**
 * One-piece WYSIWYG HTML editor (custom, lightweight).
 *
 * - Shows formatted editable text in a single component.
 * - Stores markup as HTML on save.
 *
 * Supported tags are limited (see [RichTextHtml]).
 */
@Composable
fun RichHtmlEditor(
    html: String,
    onSave: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var isEditMode by remember { mutableStateOf(false) }

    // Parse html only when entering edit mode or when html changes while not editing.
    var doc by remember(html, isEditMode) {
        mutableStateOf(
            if (isEditMode) RichTextHtml.fromHtml(html) else RichDoc(text = "", marks = emptyList())
        )
    }
    var value by remember(html, isEditMode) {
        mutableStateOf(TextFieldValue(if (isEditMode) doc.text else ""))
    }
    var marks by remember(html, isEditMode) {
        mutableStateOf(if (isEditMode) doc.marks else emptyList())
    }

    // Keep marks in sync with text edits
    fun onValueChanged(newValue: TextFieldValue) {
        val newMarks = RichTextOps.adjustMarksForValueChange(value, newValue, marks)
        value = newValue
        marks = newMarks
    }

    Column(modifier = modifier.fillMaxSize()) {
        // Action buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isEditMode) {
                TextButton(onClick = {
                    // Reset
                    val parsed = RichTextHtml.fromHtml(html)
                    doc = parsed
                    value = TextFieldValue(parsed.text)
                    marks = parsed.marks
                    isEditMode = false
                }) {
                    Text("Cancelar")
                }

                Spacer(Modifier.width(8.dp))

                Button(
                    onClick = {
                        val outHtml = RichTextHtml.toHtml(RichDoc(value.text, marks))
                        onSave(outHtml)
                        isEditMode = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                ) {
                    Text("Guardar")
                }
            } else {
                Button(
                    onClick = {
                        val parsed = RichTextHtml.fromHtml(html)
                        doc = parsed
                        value = TextFieldValue(parsed.text)
                        marks = parsed.marks
                        isEditMode = true
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2196F3))
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Editar")
                }
            }
        }

        if (isEditMode) {
            RichTextEditor(
                value = value,
                marks = marks,
                onValueChange = ::onValueChanged,
                onMarksChange = { marks = it },
                modifier = Modifier.fillMaxSize(),
                placeholder = "Escribe tu nota..."
            )
        } else {
            // Read mode: reuse existing HTML viewer
            HtmlViewer(html = html, modifier = Modifier.fillMaxSize())
        }
    }
}
