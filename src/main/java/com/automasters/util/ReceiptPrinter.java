package com.automasters.util;

import com.automasters.entity.Invoice;
import com.automasters.entity.InvoiceItem;

import javax.print.*;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;

/**
 * Receipt printer utility for Epson TM-U220D (ESC/POS compatible)
 */
public class ReceiptPrinter {

    // ESC/POS Commands
    private static final byte[] INIT = {0x1B, 0x40}; // Initialize printer
    private static final byte[] CUT_PAPER = {0x1D, 0x56, 0x00}; // Full cut
    private static final byte[] FEED_LINE = {0x0A}; // Line feed
    private static final byte[] ALIGN_CENTER = {0x1B, 0x61, 0x01};
    private static final byte[] ALIGN_LEFT = {0x1B, 0x61, 0x00};
    private static final byte[] ALIGN_RIGHT = {0x1B, 0x61, 0x02};
    private static final byte[] BOLD_ON = {0x1B, 0x45, 0x01};
    private static final byte[] BOLD_OFF = {0x1B, 0x45, 0x00};
    private static final byte[] DOUBLE_HEIGHT_ON = {0x1B, 0x21, 0x10};
    private static final byte[] DOUBLE_WIDTH_ON = {0x1B, 0x21, 0x20};
    private static final byte[] DOUBLE_SIZE_ON = {0x1B, 0x21, 0x30};
    private static final byte[] NORMAL_SIZE = {0x1B, 0x21, 0x00};

    private static final int LINE_WIDTH = 42; // Characters per line for TM-U220D

    private String printerName;

    public ReceiptPrinter() {
        this.printerName = null; // Will use default printer
    }

    public ReceiptPrinter(String printerName) {
        this.printerName = printerName;
    }

    /**
     * Print invoice receipt
     */
    public void printInvoice(Invoice invoice) throws PrintException, IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        // Initialize printer
        baos.write(INIT);

        // Header - Company name
        baos.write(ALIGN_CENTER);
        baos.write(DOUBLE_SIZE_ON);
        baos.write("AUTOMASTERS\n".getBytes());
        baos.write(NORMAL_SIZE);
        baos.write("Vehicle Service Center\n".getBytes());
        baos.write(createLine('-'));
        baos.write(FEED_LINE);

        // Invoice info
        baos.write(ALIGN_LEFT);
        baos.write(BOLD_ON);
        baos.write(String.format("Invoice: %s\n", invoice.getInvoiceNumber()).getBytes());
        baos.write(BOLD_OFF);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
        baos.write(String.format("Date: %s\n", invoice.getInvoiceDate().format(formatter)).getBytes());
        baos.write(FEED_LINE);

        // Customer details
        baos.write(createLine('-'));
        baos.write(String.format("Customer: %s\n", invoice.getCustomerName()).getBytes());
        baos.write(String.format("Contact: %s\n", invoice.getContactNumber()).getBytes());
        baos.write(BOLD_ON);
        baos.write(String.format("Vehicle: %s\n", invoice.getVehicleNumber()).getBytes());
        baos.write(BOLD_OFF);
        baos.write(createLine('-'));
        baos.write(FEED_LINE);

        // Services header
        baos.write(BOLD_ON);
        baos.write(formatLine("No", "Description", "Amount").getBytes());
        baos.write(BOLD_OFF);
        baos.write(createLine('-'));

        // Service items
        for (InvoiceItem item : invoice.getItems()) {
            String line = formatServiceLine(
                    String.valueOf(item.getSerialNumber()),
                    item.getDescription(),
                    String.format("%.2f", item.getPrice())
            );
            baos.write(line.getBytes());
        }

        baos.write(createLine('-'));
        baos.write(FEED_LINE);

        // Total
        baos.write(ALIGN_RIGHT);
        baos.write(BOLD_ON);
        baos.write(DOUBLE_HEIGHT_ON);
        baos.write(String.format("TOTAL: Rs. %.2f\n", invoice.getTotalAmount()).getBytes());
        baos.write(NORMAL_SIZE);
        baos.write(BOLD_OFF);
        baos.write(FEED_LINE);

        // Footer
        baos.write(ALIGN_CENTER);
        baos.write(createLine('-'));
        baos.write("Thank you for your business!\n".getBytes());
        baos.write("Visit us again\n".getBytes());
        baos.write(FEED_LINE);
        baos.write(FEED_LINE);
        baos.write(FEED_LINE);

        // Cut paper
        baos.write(CUT_PAPER);

        // Send to printer
        printRaw(baos.toByteArray());
    }

    /**
     * Format a header line with 3 columns
     */
    private String formatLine(String col1, String col2, String col3) {
        return String.format("%-3s %-28s %8s\n", col1, col2, col3);
    }

    /**
     * Format service line, handling long descriptions
     */
    private String formatServiceLine(String no, String description, String amount) {
        int descMaxLen = 28;
        if (description.length() > descMaxLen) {
            description = description.substring(0, descMaxLen - 2) + "..";
        }
        return String.format("%-3s %-28s %8s\n", no, description, amount);
    }

    /**
     * Create a separator line
     */
    private byte[] createLine(char c) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < LINE_WIDTH; i++) {
            sb.append(c);
        }
        sb.append("\n");
        return sb.toString().getBytes();
    }

    /**
     * Send raw bytes to printer
     */
    private void printRaw(byte[] data) throws PrintException {
        PrintService printService = findPrintService();

        if (printService == null) {
            throw new PrintException("Printer not found. Please check printer connection.");
        }

        DocPrintJob job = printService.createPrintJob();
        DocFlavor flavor = DocFlavor.INPUT_STREAM.AUTOSENSE;
        Doc doc = new SimpleDoc(new ByteArrayInputStream(data), flavor, null);
        PrintRequestAttributeSet attrs = new HashPrintRequestAttributeSet();

        job.print(doc, attrs);
    }

    /**
     * Find the print service (printer)
     */
    private PrintService findPrintService() {
        PrintService[] services = PrintServiceLookup.lookupPrintServices(null, null);

        if (printerName != null && !printerName.isEmpty()) {
            // Find specific printer
            for (PrintService service : services) {
                if (service.getName().toLowerCase().contains(printerName.toLowerCase())) {
                    return service;
                }
            }
        }

        // Look for Epson printer
        for (PrintService service : services) {
            String name = service.getName().toLowerCase();
            if (name.contains("epson") || name.contains("tm-u220") || name.contains("tm-t")) {
                return service;
            }
        }

        // Return default printer
        return PrintServiceLookup.lookupDefaultPrintService();
    }

    /**
     * Get list of available printers
     */
    public static String[] getAvailablePrinters() {
        PrintService[] services = PrintServiceLookup.lookupPrintServices(null, null);
        String[] names = new String[services.length];
        for (int i = 0; i < services.length; i++) {
            names[i] = services[i].getName();
        }
        return names;
    }

    /**
     * Test print
     */
    public void testPrint() throws PrintException, IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        baos.write(INIT);
        baos.write(ALIGN_CENTER);
        baos.write(DOUBLE_SIZE_ON);
        baos.write("AUTOMASTERS\n".getBytes());
        baos.write(NORMAL_SIZE);
        baos.write("Printer Test\n".getBytes());
        baos.write(createLine('-'));
        baos.write("Printer is working!\n".getBytes());
        baos.write(FEED_LINE);
        baos.write(FEED_LINE);
        baos.write(FEED_LINE);
        baos.write(CUT_PAPER);

        printRaw(baos.toByteArray());
    }
}
