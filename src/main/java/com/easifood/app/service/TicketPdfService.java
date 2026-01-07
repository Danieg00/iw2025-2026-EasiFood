package com.easifood.app.service;

import com.easifood.app.model.Pedido;
import com.easifood.app.model.PedidoProducto;
import com.easifood.app.model.Producto;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.springframework.stereotype.Service;
import com.easifood.app.model.Cliente;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Service
public class TicketPdfService {

    private static final NumberFormat EUR = NumberFormat.getCurrencyInstance(new Locale("es", "ES"));
    private static final DateTimeFormatter FECHA_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public byte[] generarTicket(Pedido p) {
        if (p == null) throw new IllegalArgumentException("Pedido nulo");

        try (PDDocument doc = new PDDocument();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            PDPage page = new PDPage(PDRectangle.A4);
            doc.addPage(page);

            float margin = 50f;
            float y = page.getMediaBox().getHeight() - margin;
            float x = margin;
            float leading = 16f;

            try (PDPageContentStream cs = new PDPageContentStream(doc, page)) {

                // ===== Header
                cs.beginText();
                cs.setFont(PDType1Font.HELVETICA_BOLD, 18);
                cs.newLineAtOffset(x, y);
                cs.showText("EasiFood · Ticket de pedido");
                cs.endText();

                y -= 28;

                String rest = (p.getRestaurante() != null && p.getRestaurante().getNombre() != null)
                        ? p.getRestaurante().getNombre() : "-";
                String fecha = (p.getFechaCreacion() != null)
                        ? p.getFechaCreacion().format(FECHA_FORMAT) : "-";
                String estado = (p.getEstado() != null && !p.getEstado().isBlank())
                        ? p.getEstado() : "-";
                String direccion = (p.getDireccionEntrega() != null) ? p.getDireccionEntrega() : "-";
                String nombreCliente = "-";
                String correoCliente = "-";

                if (p.getCliente() != null) {
                    Cliente c = p.getCliente();

                    String nombre = (c.getNombre() != null) ? c.getNombre() : "";
                    String apellidos = (c.getApellidos() != null) ? c.getApellidos() : "";
                    String fullName = (nombre + " " + apellidos).trim();

                    if (!fullName.isBlank()) {
                        nombreCliente = fullName;
                    }

                    if (c.getCorreo() != null && !c.getCorreo().isBlank()) {
                        correoCliente = c.getCorreo();
                    }
                }

                y = writeLine(cs, x, y, "Pedido #" + p.getId(), PDType1Font.HELVETICA_BOLD, 12, leading);
                y = writeLine(cs, x, y, "Restaurante: " + rest, PDType1Font.HELVETICA, 11, leading);
                y = writeLine(cs, x, y, "Fecha: " + fecha, PDType1Font.HELVETICA, 11, leading);
                y = writeLine(cs, x, y, "Estado: " + estado, PDType1Font.HELVETICA, 11, leading);

                y -= 8;
                y = writeLine(cs, x, y, "Cliente:", PDType1Font.HELVETICA_BOLD, 11, leading);
                y = writeLine(cs, x, y, "Nombre: " + nombreCliente, PDType1Font.HELVETICA, 11, leading);
                y = writeLine(cs, x, y, "Correo: " + correoCliente, PDType1Font.HELVETICA, 11, leading);

                y -= 8;
                y = writeLine(cs, x, y, "Dirección de entrega:", PDType1Font.HELVETICA_BOLD, 11, leading);

                // Dirección (troceada)
                y = writeWrapped(cs, x, y, direccion, PDType1Font.HELVETICA, 11, 480, leading);

                y -= 14;

                // ===== Tabla cabecera
                cs.setFont(PDType1Font.HELVETICA_BOLD, 11);
                y = drawRow(cs, x, y, "Producto", "Cant.", "P.Unit", "Subtotal");
                y -= 6;

                // ===== Líneas
                BigDecimal total = BigDecimal.ZERO;

                if (p.getLineas() != null) {
                    for (PedidoProducto l : p.getLineas()) {
                        Producto prod = l.getProducto();
                        String nombreProd = (prod != null && prod.getNombre() != null) ? prod.getNombre() : "Producto";
                        int qty = l.getCantidad();
                        BigDecimal pu = (l.getPrecioUnitario() != null) ? l.getPrecioUnitario() : BigDecimal.ZERO;

                        BigDecimal sub = pu.multiply(BigDecimal.valueOf(Math.max(0, qty)));
                        total = total.add(sub);

                        cs.setFont(PDType1Font.HELVETICA, 11);

                        // nombre (cortito para que no rompa)
                        String nombreCorto = nombreProd.length() > 38 ? nombreProd.substring(0, 37) + "…" : nombreProd;

                        y = drawRow(
                                cs, x, y,
                                nombreCorto,
                                String.valueOf(qty),
                                EUR.format(pu),
                                EUR.format(sub)
                        );

                        y -= 2;

                        // Si te quedas sin página, aquí podrías añadir paginado (si lo necesitas luego lo hacemos)
                        if (y < 90) break;
                    }
                }

                y -= 10;

                // ===== Total
                cs.beginText();
                cs.setFont(PDType1Font.HELVETICA_BOLD, 13);
                cs.newLineAtOffset(x, y);
                cs.showText("TOTAL: " + EUR.format(total));
                cs.endText();

                y -= 24;

                cs.beginText();
                cs.setFont(PDType1Font.HELVETICA, 10);
                cs.newLineAtOffset(x, y);
                cs.showText("Gracias por tu pedido.");
                cs.endText();
            }

            doc.save(out);
            return out.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("Error generando PDF: " + e.getMessage(), e);
        }
    }

    private float writeLine(PDPageContentStream cs, float x, float y, String text,
                            org.apache.pdfbox.pdmodel.font.PDFont font, int size, float leading) throws Exception {
        cs.beginText();
        cs.setFont(font, size);
        cs.newLineAtOffset(x, y);
        cs.showText(text);
        cs.endText();
        return y - leading;
    }

    private float writeWrapped(PDPageContentStream cs, float x, float y, String text,
                               org.apache.pdfbox.pdmodel.font.PDFont font, int size,
                               float maxWidth, float leading) throws Exception {

        String[] words = text.replace("\n", " ").split("\\s+");
        StringBuilder line = new StringBuilder();

        for (String w : words) {
            String candidate = line.isEmpty() ? w : (line + " " + w);
            float width = font.getStringWidth(candidate) / 1000f * size;

            if (width > maxWidth && !line.isEmpty()) {
                y = writeLine(cs, x, y, line.toString(), font, size, leading);
                line = new StringBuilder(w);
            } else {
                line = new StringBuilder(candidate);
            }
        }

        if (!line.isEmpty()) {
            y = writeLine(cs, x, y, line.toString(), font, size, leading);
        }

        return y;
    }

    private float drawRow(PDPageContentStream cs, float x, float y,
                          String c1, String c2, String c3, String c4) throws Exception {

        // Columnas fijas
        float col1 = x;
        float col2 = x + 290;
        float col3 = x + 350;
        float col4 = x + 430;

        cs.beginText();
        cs.newLineAtOffset(col1, y);
        cs.showText(c1);
        cs.endText();

        cs.beginText();
        cs.newLineAtOffset(col2, y);
        cs.showText(c2);
        cs.endText();

        cs.beginText();
        cs.newLineAtOffset(col3, y);
        cs.showText(c3);
        cs.endText();

        cs.beginText();
        cs.newLineAtOffset(col4, y);
        cs.showText(c4);
        cs.endText();

        return y - 16f;
    }
}
