package ps.backend.service;

import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.HorizontalAlignment;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;

import org.springframework.stereotype.Service;
import ps.backend.dto.TicketDTO;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;

@Service
public class PDFService {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");

    public byte[] generateTicketPDF(TicketDTO ticketDTO) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        PdfWriter writer = new PdfWriter(outputStream);
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf, PageSize.A4);
        document.setMargins(50, 50, 50, 50);

        Paragraph title = new Paragraph("BOARDING PASS")
                .setFontSize(24)
                .setBold()
                .setTextAlignment(TextAlignment.CENTER);
        document.add(title);

        document.add(new Paragraph("\n"));

        Table ticketTable = new Table(UnitValue.createPercentArray(new float[]{1, 1}));
        ticketTable.setWidth(UnitValue.createPercentValue(100));

        ticketTable.addCell(createCell("Flight", true));
        ticketTable.addCell(createCell(ticketDTO.getFlightNumber(), false));

        ticketTable.addCell(createCell("Route", true));
        ticketTable.addCell(createCell(ticketDTO.getOrigin() + " → " + ticketDTO.getDestination(), false));

        ticketTable.addCell(createCell("Passenger", true));
        ticketTable.addCell(createCell(ticketDTO.getPassengerName(), false));

        ticketTable.addCell(createCell("Seat", true));
        ticketTable.addCell(createCell(ticketDTO.getSeatNumber(), false));

        ticketTable.addCell(createCell("Departure", true));
        ticketTable.addCell(createCell(ticketDTO.getDepartureTime().format(DATE_TIME_FORMATTER), false));

        ticketTable.addCell(createCell("Arrival", true));
        ticketTable.addCell(createCell(ticketDTO.getArrivalTime().format(DATE_TIME_FORMATTER), false));

        ticketTable.addCell(createCell("Ticket #", true));
        ticketTable.addCell(createCell(ticketDTO.getTicketNumber(), false));

        ticketTable.addCell(createCell("Price", true));
        ticketTable.addCell(createCell("$" + ticketDTO.getPrice(), false));

        ticketTable.addCell(createCell("Status", true));
        ticketTable.addCell(createCell(ticketDTO.getStatus().toString(), false));

        document.add(ticketTable);

        document.add(new Paragraph("\n"));
        Paragraph barcode = new Paragraph("*" + ticketDTO.getTicketNumber() + "*")
                .setFontSize(14)
                .setTextAlignment(TextAlignment.CENTER);
        document.add(barcode);

        document.add(new Paragraph("\n"));
        Paragraph terms = new Paragraph("Please arrive at the airport at least 2 hours before departure. " +
                "This ticket is non-refundable and cannot be transferred to another person. " +
                "Flight schedules are subject to change without prior notice.")
                .setFontSize(8)
                .setTextAlignment(TextAlignment.CENTER)
                .setItalic();
        document.add(terms);

        document.close();

        return outputStream.toByteArray();
    }

    private Cell createCell(String text, boolean isHeader) {
        Cell cell = new Cell();
        cell.add(new Paragraph(text));
        cell.setPadding(5);

        if (isHeader) {
            cell.setBackgroundColor(ColorConstants.LIGHT_GRAY);
            cell.setTextAlignment(TextAlignment.RIGHT);
            cell.setBold();
        } else {
            cell.setTextAlignment(TextAlignment.LEFT);
        }

        cell.setBorder(new SolidBorder(ColorConstants.GRAY, 0.5f));
        return cell;
    }
}