package com.kodeforge.ui.components.richtext

/**
 * Very small HTML <-> RichDoc converter for the MVP editor.
 *
 * Supported tags:
 * - <strong>, <em>, <u>, <code>, <s>/<del>
 * - <a href="...">...</a>
 * - <br>, <p>, </p>, <div>, </div>, <h1>, <h2> (treated as newlines)
 *
 * Unknown tags are stripped.
 *
 * IMPORTANT: This is not a full HTML parser. It's designed for the app's own generated HTML.
 */
object RichTextHtml {
    private data class Open(val type: MarkType, val start: Int, val data: String? = null)

    fun fromHtml(html: String): RichDoc {
        val text = StringBuilder()
        val marks = mutableListOf<Mark>()
        val stack = ArrayDeque<Open>()

        var i = 0
        fun flushText(s: String) {
            text.append(decodeEntities(s))
        }

        while (i < html.length) {
            val ch = html[i]
            if (ch != '<') {
                // copy until next tag
                val j = html.indexOf('<', startIndex = i).let { if (it == -1) html.length else it }
                flushText(html.substring(i, j))
                i = j
                continue
            }

            val close = html.indexOf('>', startIndex = i + 1)
            if (close == -1) {
                flushText(html.substring(i))
                break
            }
            val rawTag = html.substring(i + 1, close).trim()
            val tagLower = rawTag.lowercase()

            // Self-closing / block-ish tags => newline
            if (tagLower == "br" || tagLower == "br/" || tagLower.startsWith("br ") ||
                tagLower == "p" || tagLower == "/p" ||
                tagLower == "div" || tagLower == "/div" ||
                tagLower == "h1" || tagLower == "/h1" ||
                tagLower == "h2" || tagLower == "/h2"
            ) {
                // Avoid piling up multiple newlines
                if (text.isNotEmpty() && text.last() != '\n') text.append('\n')
                i = close + 1
                continue
            }

            when {
                tagLower == "strong" -> stack.addLast(Open(MarkType.BOLD, text.length))
                tagLower == "/strong" -> popTo(marks, stack, MarkType.BOLD, text.length)
                tagLower == "em" || tagLower == "i" -> stack.addLast(Open(MarkType.ITALIC, text.length))
                tagLower == "/em" || tagLower == "/i" -> popTo(marks, stack, MarkType.ITALIC, text.length)
                tagLower == "u" -> stack.addLast(Open(MarkType.UNDERLINE, text.length))
                tagLower == "/u" -> popTo(marks, stack, MarkType.UNDERLINE, text.length)
                tagLower == "s" || tagLower == "del" || tagLower == "strike" -> stack.addLast(Open(MarkType.STRIKE, text.length))
                tagLower == "/s" || tagLower == "/del" || tagLower == "/strike" -> popTo(marks, stack, MarkType.STRIKE, text.length)
                tagLower == "code" -> stack.addLast(Open(MarkType.CODE, text.length))
                tagLower == "/code" -> popTo(marks, stack, MarkType.CODE, text.length)
                tagLower.startsWith("a ") && tagLower.contains("href=") -> {
                    val href = extractHref(rawTag)
                    stack.addLast(Open(MarkType.LINK, text.length, href))
                }
                tagLower == "/a" -> popTo(marks, stack, MarkType.LINK, text.length)
                tagLower.startsWith("span") -> {
                    val size = extractFontSizeSp(rawTag)
                    val color = extractColorHex(rawTag)
                    val family = extractFontFamily(rawTag)

                    if (size != null) stack.addLast(Open(MarkType.FONT_SIZE, text.length, size.toString()))
                    if (color != null) stack.addLast(Open(MarkType.FONT_COLOR, text.length, color))
                    if (family != null) stack.addLast(Open(MarkType.FONT_FAMILY, text.length, family))
                }
                tagLower == "/span" -> {
                    // Close any inline style marks that could have been opened by <span ...>
                    popTo(marks, stack, MarkType.FONT_FAMILY, text.length)
                    popTo(marks, stack, MarkType.FONT_COLOR, text.length)
                    popTo(marks, stack, MarkType.FONT_SIZE, text.length)
                }
                else -> {
                    // ignore unknown tags
                }
            }

            i = close + 1
        }

        // Close any unclosed tags at end
        while (stack.isNotEmpty()) {
            val open = stack.removeLast()
            if (text.length > open.start) {
                marks += Mark(open.start, text.length, open.type, open.data)
            }
        }

        // Normalize newlines at edges
        val finalText = text.toString().trimEnd('\n')
        val trimmedDiff = text.length - finalText.length
        val finalMarks = if (trimmedDiff == 0) marks else marks.mapNotNull { m ->
            val newEnd = m.end.coerceAtMost(finalText.length)
            val newStart = m.start.coerceAtMost(newEnd)
            if (newEnd > newStart) m.copy(start = newStart, end = newEnd) else null
        }

        return RichDoc(finalText, finalMarks)
    }

    fun toHtml(doc: RichDoc): String {
        val text = doc.text
        if (text.isEmpty()) return ""

        // Kotlin doesn't allow local enums in common code on some targets; keep it simple.
        data class Event(val pos: Int, val isOpen: Boolean, val mark: Mark)

        val events = mutableListOf<Event>()
        for (m in doc.marks) {
            val s = m.start.coerceIn(0, text.length)
            val e = m.end.coerceIn(0, text.length)
            if (e <= s) continue
            events += Event(s, true, m.copy(start = s, end = e))
            events += Event(e, false, m.copy(start = s, end = e))
        }

        // Sort events so that closes happen before opens at the same position.
        // For closes at same position: close inner marks first.
        // For opens at same position: open outer marks first.
        events.sortWith { a, b ->
            when {
                a.pos != b.pos -> a.pos - b.pos
                a.isOpen != b.isOpen -> if (!a.isOpen) -1 else 1
                !a.isOpen -> b.mark.start - a.mark.start
                else -> a.mark.end - b.mark.end
            }
        }

        val sb = StringBuilder()
        var cursor = 0
        var idx = 0
        while (cursor <= text.length) {
            val nextPos = if (idx < events.size) events[idx].pos else text.length

            if (nextPos > cursor) {
                val chunk = text.substring(cursor, nextPos)
                sb.append(escapeHtml(chunk).replace("\n", "<br/>"))
                cursor = nextPos
            }

            while (idx < events.size && events[idx].pos == cursor) {
                val ev = events[idx]
                sb.append(
                    if (ev.isOpen) openTag(ev.mark) else closeTag(ev.mark)
                )
                idx++
            }

            if (cursor == text.length) break
        }

        return sb.toString()
    }

    private fun openTag(m: Mark): String = when (m.type) {
        MarkType.BOLD -> "<strong>"
        MarkType.ITALIC -> "<em>"
        MarkType.UNDERLINE -> "<u>"
        MarkType.STRIKE -> "<s>"
        MarkType.CODE -> "<code>"
        MarkType.LINK -> "<a href=\"${escapeAttr(m.data ?: "")}\">"
        MarkType.FONT_SIZE -> {
            val px = m.data?.toIntOrNull() ?: 14
            "<span style=\"font-size:${px}px\">"
        }
        MarkType.FONT_COLOR -> {
            val c = m.data ?: "#000000"
            "<span style=\"color:${escapeAttr(c)}\">"
        }
        MarkType.FONT_FAMILY -> {
            val fam = when (m.data) {
                "serif" -> "serif"
                "mono" -> "monospace"
                else -> "sans-serif"
            }
            "<span style=\"font-family:${escapeAttr(fam)}\">"
        }
    }

    private fun closeTag(m: Mark): String = when (m.type) {
        MarkType.BOLD -> "</strong>"
        MarkType.ITALIC -> "</em>"
        MarkType.UNDERLINE -> "</u>"
        MarkType.STRIKE -> "</s>"
        MarkType.CODE -> "</code>"
        MarkType.LINK -> "</a>"
        MarkType.FONT_SIZE -> "</span>"
        MarkType.FONT_COLOR -> "</span>"
        MarkType.FONT_FAMILY -> "</span>"
    }

    private fun extractFontSizeSp(rawTag: String): Int? {
        // Example: span style="font-size:16px; color:#..."
        val regex = Regex("font-size\\s*:\\s*([0-9]{1,3})(px|pt|em|rem)?", RegexOption.IGNORE_CASE)
        val m = regex.find(rawTag) ?: return null
        return m.groupValues[1].toIntOrNull()
    }

    private fun extractColorHex(rawTag: String): String? {
        // Example: span style="color:#1976d2; ..."
        val regex = Regex("color\\s*:\\s*(#[0-9a-fA-F]{6})")
        val m = regex.find(rawTag) ?: return null
        return m.groupValues[1].lowercase()
    }

    private fun extractFontFamily(rawTag: String): String? {
        // Example: span style="font-family:monospace; ..."
        val regex = Regex("font-family\\s*:\\s*([^;\"']+)", RegexOption.IGNORE_CASE)
        val m = regex.find(rawTag) ?: return null
        val fam = m.groupValues[1].trim().lowercase()
        return when {
            fam.contains("mono") -> "mono"
            fam.contains("serif") && !fam.contains("sans") -> "serif"
            fam.contains("sans") -> "sans"
            else -> null
        }
    }

    private fun popTo(marks: MutableList<Mark>, stack: ArrayDeque<Open>, type: MarkType, end: Int) {
        val idx = stack.indexOfLast { it.type == type }
        if (idx < 0) return
        // pop elements after idx, but keep them (they are unbalanced) to re-add later.
        val tail = mutableListOf<Open>()
        while (stack.size - 1 > idx) tail += stack.removeLast()
        val open = stack.removeLast()
        if (end > open.start) marks += Mark(open.start, end, open.type, open.data)
        // push tail back
        for (t in tail.asReversed()) stack.addLast(t)
    }

    private fun extractHref(rawTag: String): String {
        // rawTag example: a href="https://..." target="_blank"
        val regex = Regex("href\\s*=\\s*(\"([^\"]*)\"|'([^']*)'|([^\\s>]+))", RegexOption.IGNORE_CASE)
        val m = regex.find(rawTag) ?: return ""
        return m.groupValues[2].ifEmpty { m.groupValues[3].ifEmpty { m.groupValues[4] } }
    }

    private fun decodeEntities(s: String): String {
        return s
            .replace("&lt;", "<")
            .replace("&gt;", ">")
            .replace("&amp;", "&")
            .replace("&quot;", "\"")
            .replace("&#39;", "'")
            .replace("&nbsp;", " ")
    }

    private fun escapeHtml(s: String): String {
        return s
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&#39;")
    }

    private fun escapeAttr(s: String): String = escapeHtml(s)
}
