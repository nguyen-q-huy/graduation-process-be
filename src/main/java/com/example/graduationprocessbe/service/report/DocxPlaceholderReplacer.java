package com.example.graduationprocessbe.service.report;

import org.apache.poi.xwpf.usermodel.IBodyElement;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFFooter;
import org.apache.poi.xwpf.usermodel.XWPFHeader;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Replaces {@code ${name}} placeholders in a Word document with the given values.
 *
 * <p>Word often splits one placeholder over several runs (spell checking, edits, formatting), so the text of
 * a whole paragraph is merged before searching. The merged text is written back into the first run, which
 * keeps that run's font, size and style. Placeholders in the body, nested tables, headers and footers are handled.
 */
public final class DocxPlaceholderReplacer {

    private static final Logger log = LoggerFactory.getLogger(DocxPlaceholderReplacer.class);
    private static final Pattern PLACEHOLDER = Pattern.compile("\\$\\{\\s*([A-Za-z0-9_.-]+)\\s*}");

    private final Map<String, String> values;
    private final Set<String> unknown = new LinkedHashSet<>();

    public DocxPlaceholderReplacer(Map<String, String> values) {
        this.values = values;
    }

    /** Replaces all placeholders in the document in place. */
    public void replaceIn(XWPFDocument document) {
        replaceInElements(document.getBodyElements());
        for (XWPFHeader header : document.getHeaderList()) replaceInElements(header.getBodyElements());
        for (XWPFFooter footer : document.getFooterList()) replaceInElements(footer.getBodyElements());
        if (!unknown.isEmpty()) log.warn("Template has placeholders without a value (replaced by empty text): {}", unknown);
    }

    private void replaceInElements(List<IBodyElement> elements) {
        for (IBodyElement element : elements) {
            if (element instanceof XWPFParagraph paragraph) replaceInParagraph(paragraph);
            else if (element instanceof XWPFTable table) replaceInTable(table);
        }
    }

    private void replaceInTable(XWPFTable table) {
        for (XWPFTableRow row : table.getRows()) {
            for (XWPFTableCell cell : row.getTableCells()) replaceInElements(cell.getBodyElements());
        }
    }

    private void replaceInParagraph(XWPFParagraph paragraph) {
        String text = paragraph.getText();
        if (text == null || !text.contains("${")) return;

        Matcher matcher = PLACEHOLDER.matcher(text);
        StringBuilder merged = new StringBuilder();
        boolean found = false;
        while (matcher.find()) {
            found = true;
            String value = values.get(matcher.group(1));
            if (value == null) {
                unknown.add(matcher.group(1));
                value = "";
            }
            matcher.appendReplacement(merged, Matcher.quoteReplacement(value));
        }
        if (!found) return;
        matcher.appendTail(merged);

        List<XWPFRun> runs = paragraph.getRuns();
        if (runs.isEmpty()) return;
        XWPFRun first = runs.getFirst();
        // keep runs that carry a picture (e.g. a logo sharing the paragraph); only text runs are merged into the first run
        for (int i = runs.size() - 1; i > 0; i--) {
            if (runs.get(i).getEmbeddedPictures().isEmpty()) paragraph.removeRun(i);
        }
        writeText(first, merged.toString());
    }

    /** Writes text into a run, turning line feeds into Word line breaks. */
    private static void writeText(XWPFRun run, String text) {
        // clear the existing text nodes of the run (setText(x, 0) replaces the first one, others are removed below)
        int existing = run.getCTR().sizeOfTArray();
        for (int i = existing - 1; i >= 0; i--) run.getCTR().removeT(i);

        String[] lines = text.replace("\r\n", "\n").replace('\r', '\n').split("\n", -1);
        for (int i = 0; i < lines.length; i++) {
            if (i > 0) run.addBreak();
            run.setText(lines[i], run.getCTR().sizeOfTArray());
        }
    }
}
