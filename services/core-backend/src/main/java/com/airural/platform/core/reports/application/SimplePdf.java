/*
 * Purpose: Writes a small standards-compliant PDF without adding a heavyweight reporting dependency.
 * Why it exists: Sprint 1 needs executable PDF export in the backend while preserving a minimal dependency surface.
 * Architecture fit: Infrastructure-neutral utility used only by the Reports bounded context.
 */
package com.airural.platform.core.reports.application;

import java.nio.charset.StandardCharsets;
import java.util.*;

/** Minimal PDF writer for text reports. */
final class SimplePdf {
    private SimplePdf() {}

    static byte[] write(String title, List<String> lines) {
        List<String> objects = new ArrayList<>();
        StringBuilder content = new StringBuilder("BT /F1 12 Tf 50 790 Td 14 TL ");
        content.append("(").append(escape(title)).append(") Tj T* T* ");
        int count = 0;
        for (String line : lines) {
            if (count++ > 48) break;
            content.append("(").append(escape(line)).append(") Tj T* ");
        }
        content.append("ET");
        byte[] stream = content.toString().getBytes(StandardCharsets.ISO_8859_1);
        objects.add("<< /Type /Catalog /Pages 2 0 R >>");
        objects.add("<< /Type /Pages /Kids [3 0 R] /Count 1 >>");
        objects.add("<< /Type /Page /Parent 2 0 R /MediaBox [0 0 612 792] /Resources << /Font << /F1 4 0 R >> >> /Contents 5 0 R >>");
        objects.add("<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica >>");
        objects.add("<< /Length " + stream.length + " >>\nstream\n" + content + "\nendstream");

        StringBuilder pdf = new StringBuilder("%PDF-1.4\n");
        List<Integer> offsets = new ArrayList<>();
        for (int i = 0; i < objects.size(); i++) {
            offsets.add(pdf.length());
            pdf.append(i + 1).append(" 0 obj\n").append(objects.get(i)).append("\nendobj\n");
        }
        int xref = pdf.length();
        pdf.append("xref\n0 ").append(objects.size() + 1).append("\n");
        pdf.append("0000000000 65535 f \n");
        for (Integer offset : offsets) {
            pdf.append(String.format(Locale.ROOT, "%010d 00000 n \n", offset));
        }
        pdf.append("trailer << /Size ").append(objects.size() + 1).append(" /Root 1 0 R >>\n");
        pdf.append("startxref\n").append(xref).append("\n%%EOF\n");
        return pdf.toString().getBytes(StandardCharsets.ISO_8859_1);
    }

    private static String escape(String text) {
        return text == null ? "" : text.replace("\\", "\\\\").replace("(", "\\(").replace(")", "\\)");
    }
}
