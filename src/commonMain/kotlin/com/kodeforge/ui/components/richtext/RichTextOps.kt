package com.kodeforge.ui.components.richtext

import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.TextRange
import kotlin.math.max
import kotlin.math.min

object RichTextOps {
    private const val DEFAULT_FONT_SP = 14
    private const val DEFAULT_FONT_COLOR = "#000000"
    private const val DEFAULT_FONT_FAMILY = "sans"

    /**
     * Toggle a mark on the current selection.
     *
     * - If the selection is empty, we don't create a "pending" style. MVP keeps it simple.
     * - If the whole selection is already covered by the same type (and for LINK, same data), we remove it.
     * - Otherwise, we apply it (and merge/normalize).
     */
    fun toggleMark(
        marks: List<Mark>,
        selection: TextRange,
        type: MarkType,
        data: String? = null,
    ): List<Mark> {
        val a = min(selection.start, selection.end)
        val b = max(selection.start, selection.end)
        if (a == b) return marks

        val covered = isFullyCovered(marks, a, b, type, data)
        val newMarks = if (covered) {
            removeRange(marks, a, b, type, data)
        } else {
            addRange(marks, a, b, type, data)
        }
        return normalize(newMarks)
    }

    /**
     * Increase/decrease font size for the selection.
     *
     * Strategy (simple & predictable):
     * - Determine a "base" size from an existing FONT_SIZE mark covering the selection start.
     * - Remove any FONT_SIZE marks overlapping the selection.
     * - Add a single FONT_SIZE mark for the selection with the new size.
     */
    fun bumpFontSize(
        marks: List<Mark>,
        selection: TextRange,
        deltaSp: Int,
        minSp: Int = 10,
        maxSp: Int = 32,
    ): List<Mark> {
        val a = min(selection.start, selection.end)
        val b = max(selection.start, selection.end)
        if (a == b) return marks

        val base = currentFontSizeAt(marks, a) ?: DEFAULT_FONT_SP
        val next = (base + deltaSp).coerceIn(minSp, maxSp)

        val without = removeRange(marks, a, b, MarkType.FONT_SIZE, data = null)
        val out = without + Mark(a, b, MarkType.FONT_SIZE, data = next.toString())
        return normalize(out)
    }

    /**
     * Set font color for the selection.
     *
     * We remove overlapping FONT_COLOR marks in the selection and add a single mark.
     */
    fun setFontColor(
        marks: List<Mark>,
        selection: TextRange,
        hexColor: String,
    ): List<Mark> {
        val a = min(selection.start, selection.end)
        val b = max(selection.start, selection.end)
        if (a == b) return marks

        val normalized = if (hexColor.startsWith("#")) hexColor.lowercase() else "#${hexColor.lowercase()}"
        val safe = if (Regex("^#[0-9a-f]{6}$").matches(normalized)) normalized else DEFAULT_FONT_COLOR

        val without = removeRange(marks, a, b, MarkType.FONT_COLOR, data = null)
        val out = without + Mark(a, b, MarkType.FONT_COLOR, data = safe)
        return normalize(out)
    }

    /**
     * Set font family for the selection.
     *
     * Accepted families: "sans", "serif", "mono".
     */
    fun setFontFamily(
        marks: List<Mark>,
        selection: TextRange,
        family: String,
    ): List<Mark> {
        val a = min(selection.start, selection.end)
        val b = max(selection.start, selection.end)
        if (a == b) return marks

        val safe = when (family.lowercase()) {
            "serif" -> "serif"
            "mono" -> "mono"
            else -> DEFAULT_FONT_FAMILY
        }

        val without = removeRange(marks, a, b, MarkType.FONT_FAMILY, data = null)
        val out = without + Mark(a, b, MarkType.FONT_FAMILY, data = safe)
        return normalize(out)
    }

    private fun currentFontSizeAt(marks: List<Mark>, pos: Int): Int? {
        val m = marks
            .asSequence()
            .filter { it.type == MarkType.FONT_SIZE && it.start <= pos && pos < it.end }
            .maxByOrNull { it.end - it.start }
        return m?.data?.toIntOrNull()
    }

    /**
     * Prefix selected lines with a bullet ("• ").
     *
     * Returns the new text and a best-effort updated selection.
     */
    fun applyBullets(text: String, selection: TextRange): Pair<String, TextRange> {
        val a = min(selection.start, selection.end).coerceIn(0, text.length)
        val b = max(selection.start, selection.end).coerceIn(0, text.length)

        // Expand to whole lines.
        val lineStart = text.lastIndexOf('\n', startIndex = (a - 1).coerceAtLeast(0)).let { if (it == -1) 0 else it + 1 }
        val lineEnd = text.indexOf('\n', startIndex = b).let { if (it == -1) text.length else it }

        val block = text.substring(lineStart, lineEnd)
        val lines = block.split("\n")
        val withBullets = lines.joinToString("\n") { line ->
            if (line.startsWith("• ")) line else "• $line"
        }

        val newText = buildString {
            append(text.substring(0, lineStart))
            append(withBullets)
            append(text.substring(lineEnd, text.length))
        }

        // Selection: keep it roughly around the same content.
        val addedPerLine = lines.count { !it.startsWith("• ") } * 2
        val newA = (a + 2).coerceAtMost(newText.length)
        val newB = (b + addedPerLine).coerceAtMost(newText.length)
        return newText to TextRange(newA, newB)
    }

    /**
     * Update marks after a text edit.
     *
     * This is a pragmatic MVP implementation that handles the most common cases:
     * - insertion at cursor
     * - deletion (backspace/delete) or replacement over a selection
     * - paste (replacement)
     */
    fun adjustMarksForValueChange(
        oldValue: TextFieldValue,
        newValue: TextFieldValue,
        oldMarks: List<Mark>,
    ): List<Mark> {
        if (oldValue.text == newValue.text) return oldMarks

        // Determine replaced range in old text.
        val oldSel = oldValue.selection
        val newSel = newValue.selection

        // Heuristic:
        // - If old selection was non-empty, assume replacement.
        // - Otherwise, try to detect insertion/deletion around cursor using length delta.
        val oldText = oldValue.text
        val newText = newValue.text
        val delta = newText.length - oldText.length

        val (replaceStart, replaceEnd, insertedCount) = if (!oldSel.collapsed) {
            Triple(min(oldSel.start, oldSel.end), max(oldSel.start, oldSel.end), newText.length - (oldText.length - (max(oldSel.start, oldSel.end) - min(oldSel.start, oldSel.end))))
        } else {
            // No old selection: locate change near old cursor.
            val cursor = oldSel.start.coerceIn(0, oldText.length)
            if (delta > 0) {
                // insertion
                Triple(cursor, cursor, delta)
            } else {
                // deletion: could be backspace or delete.
                // Prefer using new cursor to infer direction.
                val newCursor = newSel.start.coerceIn(0, newText.length)
                if (newCursor < cursor) {
                    // backspace
                    Triple(newCursor, cursor, 0)
                } else {
                    // delete
                    Triple(cursor, cursor + (-delta), 0)
                }
            }
        }

        return applyReplacement(oldMarks, replaceStart, replaceEnd, insertedCount)
    }

    private fun applyReplacement(
        marks: List<Mark>,
        replaceStart: Int,
        replaceEnd: Int,
        insertedCount: Int,
    ): List<Mark> {
        val removedCount = max(0, replaceEnd - replaceStart)
        val shift = insertedCount - removedCount

        val out = mutableListOf<Mark>()
        for (m in marks) {
            // Entirely before replaced region.
            if (m.end <= replaceStart) {
                out += m
                continue
            }
            // Entirely after replaced region.
            if (m.start >= replaceEnd) {
                out += m.copy(start = m.start + shift, end = m.end + shift)
                continue
            }

            // Overlaps the replaced region.
            val leftStart = m.start
            val leftEnd = min(m.end, replaceStart)
            val rightStart = max(m.start, replaceEnd)
            val rightEnd = m.end

            // Keep left piece if any.
            if (leftEnd > leftStart) {
                out += m.copy(start = leftStart, end = leftEnd)
            }
            // Keep right piece if any, shifted.
            if (rightEnd > rightStart) {
                out += m.copy(start = rightStart + shift, end = rightEnd + shift)
            }
        }

        return normalize(out)
    }

    private fun isFullyCovered(
        marks: List<Mark>,
        start: Int,
        end: Int,
        type: MarkType,
        data: String?,
    ): Boolean {
        // Collect intervals of the same type that overlap selection.
        val intervals = marks
            .asSequence()
            .filter { it.type == type && (type != MarkType.LINK || it.data == data) }
            .map { max(it.start, start) to min(it.end, end) }
            .filter { (a, b) -> b > a }
            .sortedBy { it.first }
            .toList()

        if (intervals.isEmpty()) return false
        var cursor = start
        for ((a, b) in intervals) {
            if (a > cursor) return false
            cursor = max(cursor, b)
            if (cursor >= end) return true
        }
        return cursor >= end
    }

    private fun addRange(
        marks: List<Mark>,
        start: Int,
        end: Int,
        type: MarkType,
        data: String?,
    ): List<Mark> {
        val storedData = when (type) {
            MarkType.LINK, MarkType.FONT_SIZE -> data
            else -> null
        }
        return marks + Mark(start = start, end = end, type = type, data = storedData)
    }

    private fun removeRange(
        marks: List<Mark>,
        start: Int,
        end: Int,
        type: MarkType,
        data: String?,
    ): List<Mark> {
        val out = mutableListOf<Mark>()
        for (m in marks) {
            if (m.type != type) {
                out += m
                continue
            }
            if (type == MarkType.LINK && m.data != data) {
                out += m
                continue
            }
            // no overlap
            if (m.end <= start || m.start >= end) {
                out += m
                continue
            }
            // overlap: keep outside pieces
            if (m.start < start) out += m.copy(end = start)
            if (m.end > end) out += m.copy(start = end)
        }
        return out
    }

    private fun normalize(marks: List<Mark>): List<Mark> {
        if (marks.isEmpty()) return marks
        val sorted = marks
            .filter { it.end > it.start }
            .sortedWith(compareBy<Mark>({ it.type }, { it.data ?: "" }, { it.start }, { it.end }))

        val out = mutableListOf<Mark>()
        for (m in sorted) {
            val last = out.lastOrNull()
            if (last != null && last.type == m.type && last.data == m.data && m.start <= last.end) {
                // merge overlapping/adjacent
                out[out.lastIndex] = last.copy(end = max(last.end, m.end))
            } else {
                out += m
            }
        }
        return out
    }
}
