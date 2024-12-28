package kienzle.calendar;

import kienzle.holiday.Holiday;
import org.apache.pdfbox.pdmodel.*;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.common.PDRectangle;

import java.io.File;
import java.io.IOException;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class CalendarPDFGenerator {

    public void generatePDF(File file, int year, Map<YearMonth, List<String>> calendarData, Map<Integer, Holiday> holidaysMap) throws IOException {
        try (PDDocument document = new PDDocument()) {
            // Erste Seite: Januar - Juni
            PDRectangle landscape = new PDRectangle(PDRectangle.A4.getHeight(), PDRectangle.A4.getWidth());
            PDPage page1 = new PDPage(landscape);
            document.addPage(page1);
            addCalendarToPage(document, page1, year, 1, 6, calendarData, holidaysMap);

            // Zweite Seite: Juli - Dezember
            PDPage page2 = new PDPage(landscape);
            document.addPage(page2);
            addCalendarToPage(document, page2, year, 7, 12, calendarData, holidaysMap);

            document.save(file);
        }
    }

    private void addCalendarToPage(PDDocument document, PDPage page, int year, int startMonth, int endMonth,
                                   Map<YearMonth, List<String>> calendarData, Map<Integer, Holiday> holidaysMap) throws IOException {
        PDPageContentStream contentStream = new PDPageContentStream(document, page);

        try {
            // Header mit Jahr
            contentStream.setFont(PDType1Font.HELVETICA_BOLD, 18);
            contentStream.beginText();
            contentStream.newLineAtOffset(300, 550);
            contentStream.showText("Kalender " + year);
            contentStream.endText();

            // Tabellenparameter
            int columns = endMonth - startMonth + 1;
            float tableWidth = page.getMediaBox().getWidth() - 100; // Seitenbreite minus Rand
            float tableHeight = 400; // Höhe des Kalenders
            float cellWidth = tableWidth / columns; // Breite einer Zelle (pro Monat)
            float cellHeight = 15; // Höhe einer Tageszelle
            float startX = 50; // Startpunkt X
            float startY = 500; // Startpunkt Y

            // Zeichnen der Tabelle und Befüllen mit Text
            for (int month = startMonth; month <= endMonth; month++) {
                YearMonth yearMonth = YearMonth.of(year, month);
                List<String> reasons = calendarData.get(yearMonth);

                // Monatsnamen oben schreiben
                float monthStartX = startX + (month - startMonth) * cellWidth;
                contentStream.setFont(PDType1Font.HELVETICA_BOLD, 14);
                contentStream.beginText();
                contentStream.newLineAtOffset(monthStartX + 5, startY + 20);
                contentStream.showText(yearMonth.getMonth().getDisplayName(TextStyle.FULL, Locale.GERMAN).toString());
                contentStream.endText();

                // Zeichnen der Spalten (Tage)
                contentStream.setFont(PDType1Font.HELVETICA, 10);

                contentStream.moveTo(monthStartX, startY - 15); // Startpunkt der Linie
                contentStream.lineTo(monthStartX + cellWidth, startY - 15); // Endpunkt der Linie
                contentStream.stroke();
                // Zeichnen der Spalten (Tage)
                //contentStream.setFont(PDType1Font.HELVETICA, 10);
                for (int day = 1; day <= yearMonth.lengthOfMonth(); day++) {
                    float cellX = monthStartX;
                    float cellY = startY - (day * cellHeight);

                    // Überprüfen, ob es ein Feiertag oder eine Müllabfuhr gibt
                    String reason = reasons != null && reasons.size() >= day ? reasons.get(day - 1) : "";
                    Holiday holiday = holidaysMap.get(day);

                    boolean isHoliday = holiday != null;
                    boolean isGarbageDay = reason != null && !reason.isEmpty();
                    String weekday = getWeekdayName(YearMonth.of(year, month).atDay(day).getDayOfWeek().getValue());
                    boolean isSunday = "Sonntag".equalsIgnoreCase(weekday) || "So".equalsIgnoreCase(weekday);

                    // Zelle zeichnen
                    if (isHoliday || isSunday) {
                        contentStream.setNonStrokingColor(192, 192, 192); // Grau für Feiertage
                    } else if (isGarbageDay) {
                        contentStream.setNonStrokingColor(0, 0, 0); // Schwarz für Müllabfuhr
                    } else {
                        contentStream.setNonStrokingColor(255, 255, 255); // Weiß (Standard)
                    }

                    contentStream.addRect(cellX, cellY, cellWidth, -cellHeight);
                    contentStream.fill();
                    contentStream.setStrokingColor(0, 0, 0);
                    contentStream.stroke();

                    // Text in der Zelle
                    String text = String.format("%02d", day); // Nur der Tag
                    String weekdayText = weekday; // Wochentag
                    String reasonText = (isHoliday ? holiday.getName() : reason); // Grund

                    contentStream.setNonStrokingColor(0, 0, 0); // Schwarz für Text
                    contentStream.beginText();
                    contentStream.newLineAtOffset(cellX + 5, cellY - 12); // Tagnummer
                    contentStream.showText(text);
                    contentStream.endText();

                    contentStream.beginText();
                    contentStream.newLineAtOffset(cellX + 35, cellY - 12); // Wochentag
                    contentStream.showText(weekdayText);
                    contentStream.endText();

                    contentStream.beginText();
                    contentStream.newLineAtOffset(cellX + 80, cellY - 12); // Reason
                    contentStream.showText(reasonText);
                    contentStream.endText();

                    // Horizontale Linie unter dem Tag
                    contentStream.moveTo(cellX, cellY - cellHeight);
                    contentStream.lineTo(cellX + cellWidth, cellY - cellHeight);
                    contentStream.stroke();

                    // Vertikale Trennlinien

                    // Erste vertikale Linie (vor Tagesnummer)
                    contentStream.moveTo(cellX, cellY);
                    contentStream.lineTo(cellX, cellY - cellHeight);
                    contentStream.stroke();

                    // Nach der Tagesnummer
                    contentStream.moveTo(cellX + 30, cellY);
                    contentStream.lineTo(cellX + 30, cellY - cellHeight);
                    contentStream.stroke();

                    // Nach dem Wochentag
                    contentStream.moveTo(cellX + 75, cellY);
                    contentStream.lineTo(cellX + 75, cellY - cellHeight);
                    contentStream.stroke();

                    // Nach dem Reason (am Ende der Zelle)
                    contentStream.moveTo(cellX + cellWidth, cellY);
                    contentStream.lineTo(cellX + cellWidth, cellY - cellHeight);
                    contentStream.stroke();
                }
            }
        } finally {
            contentStream.close();
        }
    }

    // Hilfsmethode, um den Wochentag des Tags als String zurückzugeben
    private String getWeekdayName(int weekdayIndex) {
        switch (weekdayIndex) {
            case 1:
                return "Mo"; // Montag
            case 2:
                return "Di"; // Dienstag
            case 3:
                return "Mi"; // Mittwoch
            case 4:
                return "Do"; // Donnerstag
            case 5:
                return "Fr"; // Freitag
            case 6:
                return "Sa"; // Samstag
            case 7:
                return "So"; // Sonntag
            default:
                return "";
        }
    }
}

