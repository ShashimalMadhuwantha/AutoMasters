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
    private static final byte[] INIT = { 0x1B, 0x40 }; // Initialize printer
    private static final byte[] CUT_PAPER = { 0x1D, 0x56, 0x00 }; // Full cut
    private static final byte[] FEED_LINE = { 0x0A }; // Line feed
    private static final byte[] ALIGN_CENTER = { 0x1B, 0x61, 0x01 };
    private static final byte[] ALIGN_LEFT = { 0x1B, 0x61, 0x00 };
    private static final byte[] ALIGN_RIGHT = { 0x1B, 0x61, 0x02 };
    private static final byte[] BOLD_ON = { 0x1B, 0x45, 0x01 };
    private static final byte[] BOLD_OFF = { 0x1B, 0x45, 0x00 };
    private static final byte[] DOUBLE_HEIGHT_ON = { 0x1B, 0x21, 0x10 };
    private static final byte[] DOUBLE_WIDTH_ON = { 0x1B, 0x21, 0x20 };
    private static final byte[] DOUBLE_SIZE_ON = { 0x1B, 0x21, 0x30 };
    private static final byte[] NORMAL_SIZE = { 0x1B, 0x21, 0x00 };

    private static final int LINE_WIDTH = 32; // Adjusted for 2.5 inch (approx 32 chars)

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
        // Header on single line - remove DOUBLE_WIDTH
        baos.write(DOUBLE_HEIGHT_ON);
        baos.write(BOLD_ON);
        baos.write("GALLEAUTO SERVICE\n".getBytes());
        baos.write(NORMAL_SIZE); // Reset
        baos.write("Vehicle Service Center\n".getBytes());
        baos.write(createLine('-'));
        baos.write(FEED_LINE);

        // Invoice info
        baos.write(ALIGN_LEFT);
        baos.write(BOLD_ON);
        baos.write(String.format("Invoice: %s\n", invoice.getInvoiceNumber()).getBytes());
        baos.write(BOLD_OFF);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");
        baos.write(String.format("Date: %s\n", invoice.getInvoiceDate().format(formatter)).getBytes());
        baos.write(FEED_LINE);

        // Customer details
        baos.write(createLine('-'));
        // Adjusted for 32 chars
        baos.write(String.format("Cust : %-22s\n", invoice.getCustomerName()).getBytes());
        baos.write(String.format("Tel  : %-22s\n", invoice.getContactNumber()).getBytes());
        baos.write(BOLD_ON);
        baos.write(String.format("Veh  : %-22s\n", invoice.getVehicleNumber()).getBytes());
        if (invoice.getCurrentMileage() != null && invoice.getCurrentMileage() > 0) {
            baos.write(String.format("Mil  : %,d km\n", invoice.getCurrentMileage()).getBytes());
        }
        baos.write(BOLD_OFF);
        baos.write(createLine('-'));
        baos.write(FEED_LINE);

        // Services header
        baos.write(BOLD_ON);
        // Layout: Description (21) | Amount (10) | Space (1) = 32
        baos.write(String.format("%-21s %10s\n", "Description", "Amount").getBytes());
        baos.write(BOLD_OFF);
        baos.write(createLine('-'));

        // Service items
        for (InvoiceItem item : invoice.getItems()) {
            String desc = item.getDescription();
            String price = String.format("%.2f", item.getPrice());

            if (desc.length() > 21) {
                // Truncate to fit column
                desc = desc.substring(0, 19) + "..";
            }
            baos.write(String.format("%-21s %10s\n", desc, price).getBytes());
        }

        baos.write(createLine('-'));
        baos.write(FEED_LINE);

        // Total
        baos.write(ALIGN_RIGHT);
        baos.write(BOLD_ON);
        baos.write(DOUBLE_HEIGHT_ON);
        // Adjusted width for simple alignment
        baos.write(String.format("TOTAL: %10.2f\n", invoice.getTotalAmount()).getBytes());
        baos.write(NORMAL_SIZE);
        baos.write(BOLD_OFF);
        baos.write(FEED_LINE);

        // Footer
        baos.write(ALIGN_CENTER);
        baos.write(createLine('-'));
        baos.write("Thank you!\n".getBytes());
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
        baos.write("GALLEAUTO SERVICE\n".getBytes());
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
