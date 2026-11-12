package com.smartbuilding.analytics.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Pure Java PDF 1.4 document builder.
 * Generates standards-compliant PDF documents with formatted headers, KPI metric cards,
 * structured tables, section headings, and narrative blocks without external dependencies.
 */
public class PdfReportGenerator {

    private static final float PAGE_WIDTH = 612f;
    private static final float PAGE_HEIGHT = 792f;
    private static final float MARGIN_LEFT = 45f;
    private static final float MARGIN_RIGHT = 45f;
    private static final float MARGIN_TOP = 45f;
    private static final float MARGIN_BOTTOM = 45f;
    private static final float USABLE_WIDTH = PAGE_WIDTH - MARGIN_LEFT - MARGIN_RIGHT;

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
            .withZone(ZoneId.systemDefault());

    private final List<StringBuilder> pages = new ArrayList<>();
    private StringBuilder currentStream;
    private float currentY;
    private int pageIndex = 0;

    private final String documentTitle;
    private final String documentCategory;

    public PdfReportGenerator(String documentTitle, String documentCategory) {
        this.documentTitle = documentTitle;
        this.documentCategory = documentCategory;
        startNewPage();
    }

    private void startNewPage() {
        if (currentStream != null) {
            // Draw footer on current page before moving to next
            drawFooter();
        }
        currentStream = new StringBuilder();
        pages.add(currentStream);
        pageIndex++;
        currentY = PAGE_HEIGHT - MARGIN_TOP;

        // Draw top banner header
        drawPageBanner();
    }

    private void ensureSpace(float neededHeight) {
        if (currentY - neededHeight < MARGIN_BOTTOM + 25) {
            startNewPage();
        }
    }

    private void drawPageBanner() {
        // Dark Navy Banner header background: #0f172a (15, 23, 42) -> r=0.06, g=0.09, b=0.16
        currentStream.append(String.format("0.06 0.09 0.16 rg\n"));
        currentStream.append(String.format("%.2f %.2f %.2f %.2f re f\n",
                MARGIN_LEFT, currentY - 50, USABLE_WIDTH, 50f));

        // Cyan accent line at bottom of banner: #06b6d4 -> r=0.02, g=0.71, b=0.83
        currentStream.append(String.format("0.02 0.71 0.83 rg\n"));
        currentStream.append(String.format("%.2f %.2f %.2f %.2f re f\n",
                MARGIN_LEFT, currentY - 53, USABLE_WIDTH, 3f));

        // Banner text: Title & NEXORA Tagline
        currentStream.append("BT\n");
        currentStream.append("/F2 16 Tf\n"); // Helvetica-Bold 16pt
        currentStream.append("1 1 1 rg\n"); // White
        currentStream.append(String.format("%.2f %.2f Td\n", MARGIN_LEFT + 14, currentY - 24));
        currentStream.append(String.format("(%s) Tj\n", escapePdfString(documentTitle)));
        currentStream.append("ET\n");

        currentStream.append("BT\n");
        currentStream.append("/F1 9 Tf\n"); // Helvetica 9pt
        currentStream.append("0.75 0.85 0.95 rg\n"); // Light cyan
        currentStream.append(String.format("%.2f %.2f Td\n", MARGIN_LEFT + 14, currentY - 40));
        currentStream.append(String.format("(NEXORA Smart Building Intelligence  |  %s  |  Generated: %s) Tj\n",
                escapePdfString(documentCategory), DATE_FMT.format(Instant.now())));
        currentStream.append("ET\n");

        currentY -= 70; // Move down below banner
    }

    private void drawFooter() {
        // Footer line
        currentStream.append("0.8 0.85 0.9 rg\n");
        currentStream.append(String.format("%.2f %.2f %.2f %.2f re f\n",
                MARGIN_LEFT, MARGIN_BOTTOM + 15, USABLE_WIDTH, 1f));

        // Footer text
        currentStream.append("BT\n");
        currentStream.append("/F1 8 Tf\n");
        currentStream.append("0.4 0.45 0.5 rg\n");
        currentStream.append(String.format("%.2f %.2f Td\n", MARGIN_LEFT, MARGIN_BOTTOM));
        currentStream.append(String.format("(NEXORA Digital Twin System - Confidential Operations Report  |  Page %d) Tj\n",
                pageIndex));
        currentStream.append("ET\n");
    }

    public void addSection(String sectionTitle) {
        ensureSpace(35);
        currentY -= 10;

        // Section header background pill
        currentStream.append("0.93 0.95 0.98 rg\n");
        currentStream.append(String.format("%.2f %.2f %.2f %.2f re f\n",
                MARGIN_LEFT, currentY - 18, USABLE_WIDTH, 20f));

        // Left accent bar: Cyan
        currentStream.append("0.02 0.71 0.83 rg\n");
        currentStream.append(String.format("%.2f %.2f %.2f %.2f re f\n",
                MARGIN_LEFT, currentY - 18, 4f, 20f));

        // Title text
        currentStream.append("BT\n");
        currentStream.append("/F2 11 Tf\n");
        currentStream.append("0.08 0.12 0.2 rg\n");
        currentStream.append(String.format("%.2f %.2f Td\n", MARGIN_LEFT + 10, currentY - 13));
        currentStream.append(String.format("(%s) Tj\n", escapePdfString(sectionTitle.toUpperCase())));
        currentStream.append("ET\n");

        currentY -= 26;
    }

    public static class KpiItem {
        public final String label;
        public final String value;
        public final String sublabel;

        public KpiItem(String label, String value, String sublabel) {
            this.label = label;
            this.value = value;
            this.sublabel = sublabel;
        }
    }

    public void addKpiRow(List<KpiItem> items) {
        if (items == null || items.isEmpty()) return;
        ensureSpace(60);

        int count = items.size();
        float gap = 8f;
        float totalGaps = (count - 1) * gap;
        float cardWidth = (USABLE_WIDTH - totalGaps) / count;
        float cardHeight = 48f;

        for (int i = 0; i < count; i++) {
            KpiItem item = items.get(i);
            float x = MARGIN_LEFT + i * (cardWidth + gap);
            float y = currentY - cardHeight;

            // Card background: light gray-blue
            currentStream.append("0.96 0.97 0.99 rg\n");
            currentStream.append(String.format("%.2f %.2f %.2f %.2f re f\n", x, y, cardWidth, cardHeight));

            // Card border: soft slate
            currentStream.append("0.85 0.88 0.92 RG\n");
            currentStream.append(String.format("0.75 w\n"));
            currentStream.append(String.format("%.2f %.2f %.2f %.2f re S\n", x, y, cardWidth, cardHeight));

            // Label text
            currentStream.append("BT\n");
            currentStream.append("/F1 7.5 Tf\n");
            currentStream.append("0.4 0.45 0.5 rg\n");
            currentStream.append(String.format("%.2f %.2f Td\n", x + 8, y + cardHeight - 14));
            currentStream.append(String.format("(%s) Tj\n", escapePdfString(item.label.toUpperCase())));
            currentStream.append("ET\n");

            // Value text
            currentStream.append("BT\n");
            currentStream.append("/F2 14 Tf\n");
            currentStream.append("0.08 0.12 0.2 rg\n");
            currentStream.append(String.format("%.2f %.2f Td\n", x + 8, y + cardHeight - 31));
            currentStream.append(String.format("(%s) Tj\n", escapePdfString(item.value)));
            currentStream.append("ET\n");

            // Sublabel text
            if (item.sublabel != null && !item.sublabel.isBlank()) {
                currentStream.append("BT\n");
                currentStream.append("/F1 7 Tf\n");
                currentStream.append("0.3 0.6 0.5 rg\n");
                currentStream.append(String.format("%.2f %.2f Td\n", x + 8, y + 6));
                currentStream.append(String.format("(%s) Tj\n", escapePdfString(item.sublabel)));
                currentStream.append("ET\n");
            }
        }

        currentY -= (cardHeight + 12);
    }

    public void addKeyValue(String key, String value) {
        ensureSpace(16);

        currentStream.append("BT\n");
        currentStream.append("/F2 9 Tf\n");
        currentStream.append("0.2 0.25 0.35 rg\n");
        currentStream.append(String.format("%.2f %.2f Td\n", MARGIN_LEFT, currentY - 10));
        currentStream.append(String.format("(%s:) Tj\n", escapePdfString(key)));
        currentStream.append("ET\n");

        currentStream.append("BT\n");
        currentStream.append("/F1 9 Tf\n");
        currentStream.append("0.1 0.15 0.2 rg\n");
        currentStream.append(String.format("%.2f %.2f Td\n", MARGIN_LEFT + 160, currentY - 10));
        currentStream.append(String.format("(%s) Tj\n", escapePdfString(value != null ? value : "-")));
        currentStream.append("ET\n");

        currentY -= 16;
    }

    public void addParagraph(String text) {
        if (text == null || text.isBlank()) return;
        List<String> lines = wrapText(text, 95);
        for (String line : lines) {
            ensureSpace(14);
            currentStream.append("BT\n");
            currentStream.append("/F1 9 Tf\n");
            currentStream.append("0.15 0.2 0.25 rg\n");
            currentStream.append(String.format("%.2f %.2f Td\n", MARGIN_LEFT, currentY - 10));
            currentStream.append(String.format("(%s) Tj\n", escapePdfString(line)));
            currentStream.append("ET\n");
            currentY -= 14;
        }
        currentY -= 6;
    }

    public void addTable(String[] headers, List<String[]> rows, float[] relativeWidths) {
        if (headers == null || headers.length == 0 || rows == null) return;

        float totalWeight = 0;
        for (float w : relativeWidths) totalWeight += w;
        float[] colWidths = new float[headers.length];
        for (int i = 0; i < headers.length; i++) {
            colWidths[i] = (relativeWidths[i] / totalWeight) * USABLE_WIDTH;
        }

        // Draw Table Header
        ensureSpace(24);
        float rowHeight = 18f;

        currentStream.append("0.15 0.2 0.3 rg\n"); // Slate Navy
        currentStream.append(String.format("%.2f %.2f %.2f %.2f re f\n",
                MARGIN_LEFT, currentY - rowHeight, USABLE_WIDTH, rowHeight));

        float curX = MARGIN_LEFT;
        for (int i = 0; i < headers.length; i++) {
            currentStream.append("BT\n");
            currentStream.append("/F2 8 Tf\n");
            currentStream.append("1 1 1 rg\n");
            currentStream.append(String.format("%.2f %.2f Td\n", curX + 5, currentY - 12));
            currentStream.append(String.format("(%s) Tj\n", escapePdfString(headers[i])));
            currentStream.append("ET\n");
            curX += colWidths[i];
        }
        currentY -= rowHeight;

        // Draw Table Rows
        boolean alternate = false;
        for (String[] row : rows) {
            ensureSpace(rowHeight);
            if (alternate) {
                currentStream.append("0.96 0.97 0.99 rg\n");
                currentStream.append(String.format("%.2f %.2f %.2f %.2f re f\n",
                        MARGIN_LEFT, currentY - rowHeight, USABLE_WIDTH, rowHeight));
            }
            alternate = !alternate;

            // Row bottom line
            currentStream.append("0.9 0.92 0.95 rg\n");
            currentStream.append(String.format("%.2f %.2f %.2f %.2f re f\n",
                    MARGIN_LEFT, currentY - rowHeight, USABLE_WIDTH, 0.5f));

            curX = MARGIN_LEFT;
            for (int i = 0; i < headers.length && i < row.length; i++) {
                String val = row[i] != null ? row[i] : "-";
                // truncate text if too long for column
                int maxChars = (int) (colWidths[i] / 5.2f);
                if (val.length() > maxChars && maxChars > 3) {
                    val = val.substring(0, maxChars - 2) + "..";
                }

                currentStream.append("BT\n");
                currentStream.append("/F1 8 Tf\n");
                currentStream.append("0.15 0.2 0.25 rg\n");
                currentStream.append(String.format("%.2f %.2f Td\n", curX + 5, currentY - 12));
                currentStream.append(String.format("(%s) Tj\n", escapePdfString(val)));
                currentStream.append("ET\n");
                curX += colWidths[i];
            }
            currentY -= rowHeight;
        }

        currentY -= 10;
    }

    public byte[] build() {
        if (currentStream != null) {
            drawFooter();
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            List<Integer> objectOffsets = new ArrayList<>();
            // Object 0 (not used in PDF xref, dummy)
            objectOffsets.add(0);

            // PDF Header
            baos.write(new byte[] { '%', 'P', 'D', 'F', '-', '1', '.', '4', '\n', '%', (byte) 0xE2, (byte) 0xE3, (byte) 0xCF, (byte) 0xD3, '\n' });

            // Object 1: Catalog
            objectOffsets.add(baos.size());
            baos.write("1 0 obj\n<< /Type /Catalog /Pages 2 0 R >>\nendobj\n".getBytes(StandardCharsets.ISO_8859_1));

            // Object 2: Pages
            objectOffsets.add(baos.size());
            StringBuilder kids = new StringBuilder();
            int totalPages = pages.size();
            for (int p = 0; p < totalPages; p++) {
                kids.append(5 + p * 2).append(" 0 R ");
            }
            baos.write(String.format("2 0 obj\n<< /Type /Pages /Kids [%s] /Count %d >>\nendobj\n",
                    kids.toString().trim(), totalPages).getBytes(StandardCharsets.ISO_8859_1));

            // Object 3: Font Helvetica
            objectOffsets.add(baos.size());
            baos.write("3 0 obj\n<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica >>\nendobj\n".getBytes(StandardCharsets.ISO_8859_1));

            // Object 4: Font Helvetica-Bold
            objectOffsets.add(baos.size());
            baos.write("4 0 obj\n<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica-Bold >>\nendobj\n".getBytes(StandardCharsets.ISO_8859_1));

            // For each page:
            // Page object: 5 + p * 2
            // Stream object: 6 + p * 2
            for (int p = 0; p < totalPages; p++) {
                int pageObjNum = 5 + p * 2;
                int streamObjNum = 6 + p * 2;

                // Page Object
                objectOffsets.add(baos.size());
                baos.write(String.format("%d 0 obj\n<< /Type /Page /Parent 2 0 R /MediaBox [0 0 %.2f %.2f] " +
                                "/Resources << /Font << /F1 3 0 R /F2 4 0 R >> >> /Contents %d 0 R >>\nendobj\n",
                        pageObjNum, PAGE_WIDTH, PAGE_HEIGHT, streamObjNum).getBytes(StandardCharsets.ISO_8859_1));

                // Stream Object
                byte[] streamBytes = pages.get(p).toString().getBytes(StandardCharsets.ISO_8859_1);
                objectOffsets.add(baos.size());
                baos.write(String.format("%d 0 obj\n<< /Length %d >>\nstream\n", streamObjNum, streamBytes.length)
                        .getBytes(StandardCharsets.ISO_8859_1));
                baos.write(streamBytes);
                baos.write("\nendstream\nendobj\n".getBytes(StandardCharsets.ISO_8859_1));
            }

            // Cross-Reference Table
            int xrefOffset = baos.size();
            int totalObjects = objectOffsets.size();
            baos.write(String.format("xref\n0 %d\n", totalObjects).getBytes(StandardCharsets.ISO_8859_1));
            baos.write("0000000000 65535 f \n".getBytes(StandardCharsets.ISO_8859_1));
            for (int i = 1; i < totalObjects; i++) {
                baos.write(String.format("%010d 00000 n \n", objectOffsets.get(i)).getBytes(StandardCharsets.ISO_8859_1));
            }

            // Trailer
            baos.write(String.format("trailer\n<< /Size %d /Root 1 0 R >>\nstartxref\n%d\n%%%%EOF\n",
                    totalObjects, xrefOffset).getBytes(StandardCharsets.ISO_8859_1));

        } catch (IOException e) {
            throw new RuntimeException("Failed to construct PDF binary", e);
        }

        return baos.toByteArray();
    }

    private static String escapePdfString(String str) {
        if (str == null) return "";
        return str.replace("\\", "\\\\")
                .replace("(", "\\(")
                .replace(")", "\\)")
                .replace("\r", " ")
                .replace("\n", " ");
    }

    private static List<String> wrapText(String text, int maxLineLength) {
        List<String> result = new ArrayList<>();
        if (text == null) return result;

        String[] paragraphs = text.split("\n");
        for (String para : paragraphs) {
            String[] words = para.split("\\s+");
            StringBuilder currentLine = new StringBuilder();
            for (String word : words) {
                if (currentLine.length() + word.length() + 1 > maxLineLength) {
                    if (currentLine.length() > 0) {
                        result.add(currentLine.toString());
                        currentLine = new StringBuilder();
                    }
                }
                if (currentLine.length() > 0) currentLine.append(" ");
                currentLine.append(word);
            }
            if (currentLine.length() > 0) {
                result.add(currentLine.toString());
            }
        }
        return result;
    }
}
