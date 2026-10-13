package com.example.graduationprocessbe.service.report;

import org.springframework.web.util.HtmlUtils;

import java.util.regex.Pattern;

/** Turns the rich-text HTML stored for a proposal into plain text lines for a Word placeholder. */
public final class HtmlText {

    private static final Pattern LIST_ITEM = Pattern.compile("(?i)<li\\b[^>]*>");
    private static final Pattern LINE_END = Pattern.compile("(?i)<br\\s*/?>|</(p|div|li|tr|h[1-6]|ul|ol|table)>");
    private static final Pattern CELL_END = Pattern.compile("(?i)</t[dh]>");
    private static final Pattern TAG = Pattern.compile("<[^>]+>");
    private static final Pattern BLANK_RUNS = Pattern.compile("\\n{3,}");

    private HtmlText() {
    }

    /** Plain text with one line per paragraph, "• " before list items and tabs between table cells. Plain input is returned as is. */
    public static String toPlainText(String html) {
        if (html == null || html.isBlank()) return "";
        if (html.indexOf('<') < 0) return html.strip();

        String text = LIST_ITEM.matcher(html).replaceAll("• ");
        text = CELL_END.matcher(text).replaceAll("\t");
        text = LINE_END.matcher(text).replaceAll("\n");
        text = TAG.matcher(text).replaceAll("");
        text = HtmlUtils.htmlUnescape(text);
        text = text.replace(' ', ' ').replaceAll("[ \\t]+\\n", "\n");
        return BLANK_RUNS.matcher(text).replaceAll("\n\n").strip();
    }
}
