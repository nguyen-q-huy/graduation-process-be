package com.example.graduationprocessbe.service.report;

import com.example.graduationprocessbe.exception.DocumentGenerationException;
import org.docx4j.Docx4J;
import org.docx4j.fonts.IdentityPlusMapper;
import org.docx4j.fonts.Mapper;
import org.docx4j.fonts.PhysicalFont;
import org.docx4j.fonts.PhysicalFonts;
import org.docx4j.TraversalUtil;
import org.docx4j.XmlUtils;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.CTBorder;
import org.docx4j.wml.STBorder;
import org.docx4j.wml.Tbl;
import org.docx4j.wml.TblPr;
import org.docx4j.wml.Tc;
import org.docx4j.wml.TcPrInner;
import org.docx4j.wml.Tr;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Converts a {@code .docx} to PDF in pure Java (docx4j + Apache FOP), so no Word or LibreOffice is needed on the server.
 *
 * <p>Fonts: the document's fonts are used when they are installed on the machine; otherwise they fall back to the
 * metric-compatible open fonts shipped with docx4j (Tinos for Times New Roman, Arimo for Arial, ...), which also
 * cover Vietnamese.
 */
public final class DocxPdfConverter {

    private static final Logger log = LoggerFactory.getLogger(DocxPdfConverter.class);

    /** Word font -> open, metric-compatible substitute. */
    private static final Map<String, String> FALLBACKS = Map.of(
            "Times New Roman", "Tinos",
            "Arial", "Arimo",
            "Calibri", "Carlito",
            "Cambria", "Caladea",
            "Courier New", "Cousine");
    private static final String DEFAULT_FALLBACK = "Tinos";

    private static volatile Mapper fontMapper;

    /** Converts the bytes of a Word document into the bytes of a PDF. */
    public byte[] convert(byte[] docx) {
        try {
            WordprocessingMLPackage word = Docx4J.load(new ByteArrayInputStream(docx));
            removeBordersOverriddenByEveryCell(word);
            word.setFontMapper(fontMapper());
            ByteArrayOutputStream pdf = new ByteArrayOutputStream(Math.max(32_768, docx.length * 2));
            Docx4J.toPDF(word, pdf);
            return pdf.toByteArray();
        } catch (Exception ex) {
            throw new DocumentGenerationException("Không chuyển được tài liệu Word sang PDF", ex);
        }
    }

    /** Discovering the installed fonts is slow, so the mapper is built once and shared (it is read-only afterwards). */
    private static Mapper fontMapper() throws Exception {
        Mapper mapper = fontMapper;
        if (mapper == null) {
            synchronized (DocxPdfConverter.class) {
                mapper = fontMapper;
                if (mapper == null) {
                    PhysicalFonts.discoverPhysicalFonts();
                    IdentityPlusMapper identity = new IdentityPlusMapper();
                    FALLBACKS.forEach((wordFont, substitute) -> map(identity, wordFont, substitute));
                    map(identity, "Default", DEFAULT_FALLBACK);
                    fontMapper = mapper = identity;
                }
            }
        }
        return mapper;
    }

    private static void map(IdentityPlusMapper mapper, String wordFont, String substitute) {
        if (PhysicalFonts.get(wordFont) != null) return; // the real font is installed: IdentityPlusMapper uses it as is
        PhysicalFont font = PhysicalFonts.get(substitute);
        if (font == null) {
            log.warn("Neither '{}' nor its substitute '{}' is available; PDF text may use the FOP default font", wordFont, substitute);
            return;
        }
        mapper.put(wordFont, font);
    }

    /**
     * Word lets a cell switch its borders off even when the table has borders, and the header and signature blocks of
     * the templates rely on that. The FO export keeps the table border instead, which draws an unwanted frame. So when
     * every cell of a table turns all four of its borders off, the table-level borders are dropped as well.
     */
    private static void removeBordersOverriddenByEveryCell(WordprocessingMLPackage word) {
        List<Tbl> tables = new ArrayList<>();
        new TraversalUtil(word.getMainDocumentPart().getContent(), new TraversalUtil.CallbackImpl() {
            @Override
            public List<Object> apply(Object node) {
                if (XmlUtils.unwrap(node) instanceof Tbl table) tables.add(table);
                return null;
            }
        });
        for (Tbl table : tables) {
            TblPr properties = table.getTblPr();
            if (properties != null && properties.getTblBorders() != null && allCellsHaveNoBorders(table)) {
                properties.setTblBorders(null);
            }
        }
    }

    private static boolean allCellsHaveNoBorders(Tbl table) {
        boolean anyCell = false;
        for (Object rowNode : table.getContent()) {
            if (!(XmlUtils.unwrap(rowNode) instanceof Tr row)) continue;
            for (Object cellNode : row.getContent()) {
                if (!(XmlUtils.unwrap(cellNode) instanceof Tc cell)) continue;
                anyCell = true;
                TcPrInner.TcBorders borders = cell.getTcPr() == null ? null : cell.getTcPr().getTcBorders();
                if (borders == null || !isOff(borders.getTop()) || !isOff(borders.getLeft())
                        || !isOff(borders.getBottom()) || !isOff(borders.getRight())) {
                    return false;
                }
            }
        }
        return anyCell;
    }

    private static boolean isOff(CTBorder border) {
        return border != null && (border.getVal() == STBorder.NONE || border.getVal() == STBorder.NIL);
    }
}
