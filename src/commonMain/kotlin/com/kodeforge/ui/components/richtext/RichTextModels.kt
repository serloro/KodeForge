package com.kodeforge.ui.components.richtext

/**
 * Minimal inline rich-text model for a simple WYSIWYG editor.
 *
 * - Text is stored as plain String.
 * - Formatting is stored as inline marks (spans) using absolute offsets.
 * - Offsets are in [0, text.length].
 */
enum class MarkType {
    BOLD,
    ITALIC,
    UNDERLINE,
    STRIKE,
    CODE,
    LINK,
    /** Inline font size in sp. Stored in [Mark.data] as a number (e.g. "16"). */
    FONT_SIZE,
    /** Font color. Stored in [Mark.data] as hex string, e.g. "#ff0000". */
    FONT_COLOR,
    /** Font family. Stored in [Mark.data] as "sans", "serif" or "mono". */
    FONT_FAMILY,
}

data class Mark(
    val start: Int,
    val end: Int,
    val type: MarkType,
    /** Extra data.
     *  - LINK: href
     *  - FONT_SIZE: size in sp, numeric string (e.g. "16")
     *  - FONT_COLOR: hex string (e.g. "#ff0000")
     *  - FONT_FAMILY: "sans" | "serif" | "mono"
     */
    val data: String? = null,
) {
    init {
        require(start >= 0) { "start must be >= 0" }
        require(end >= start) { "end must be >= start" }
    }
}

data class RichDoc(
    val text: String,
    val marks: List<Mark>,
)
