package kienzle.calendar;

import kienzle.holiday.Holiday;
import kienzle.type.GarbageType;
import org.apache.pdfbox.pdmodel.*;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.common.PDRectangle;

import java.io.File;
import java.io.IOException;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.*;

public class CalendarPDFGenerator {
    public void generatePDF(File file, int year, Map<YearMonth, Map<Integer, String>> calendarData, Map<Integer, Holiday> holidaysMap) throws IOException {
        try (PDDocument document = new PDDocument()) {
            // Erste Seite: Januar - Juni
            PDRectangle landscape = new PDRectangle(PDRectangle.A4.getHeight(), PDRectangle.A4.getWidth());
            PDPage page1 = new PDPage(landscape);
            document.addPage(page1);
            // Die Methode wird jetzt mit der Map<YearMonth, Map<Integer, String>> aufgerufen
            addCalendarToPage(document, page1, year, 1, 6, calendarData, holidaysMap);

            // Zweite Seite: Juli - Dezember
            PDPage page2 = new PDPage(landscape);
            document.addPage(page2);
            // Auch hier die angepasste Map verwenden
            addCalendarToPage(document, page2, year, 7, 12, calendarData, holidaysMap);

            document.save(file);
        }
    }


    private List<String> splitReasons(List<String> reasons) {
        List<String> newReasons = new LinkedList<>();

        for (String reason : reasons) {
            if (reason.contains("\n")) {
                // Split the string by \n and add each part to the list
                String[] splitParts = reason.split("\n");
                Collections.addAll(newReasons, splitParts);
            } else {
                // Add the reason directly if it does not contain \n
                newReasons.add(reason);
            }
        }

        return newReasons;
    }


    private void addCalendarToPage(PDDocument document, PDPage page, int year, int startMonth, int endMonth,
                                   Map<YearMonth, Map<Integer, String>> calendarData, Map<Integer, Holiday> holidaysMap) throws IOException {
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

                // Holen Sie sich die Gründe für den Monat aus der Map
                Map<Integer, String> reasonsMap = calendarData.get(yearMonth);

                // Monatsnamen oben schreiben
                float monthStartX = startX + (month - startMonth) * cellWidth;
                contentStream.setFont(PDType1Font.HELVETICA_BOLD, 14);
                contentStream.beginText();
                contentStream.newLineAtOffset(monthStartX + 5, startY + 20);
                contentStream.showText(yearMonth.getMonth().getDisplayName(TextStyle.FULL, Locale.GERMAN).toString());
                contentStream.endText();

                // Zeichnen der Spalten (Tage)
                contentStream.setFont(PDType1Font.HELVETICA, 8);

                contentStream.moveTo(monthStartX, startY - 15); // Startpunkt der Linie
                contentStream.lineTo(monthStartX + cellWidth, startY - 15); // Endpunkt der Linie
                contentStream.stroke();

                for (int day = 1; day <= yearMonth.lengthOfMonth(); day++) {
                    float cellX = monthStartX;
                    float cellY = startY - (day * cellHeight);

                    // Überprüfen, ob es ein Feiertag oder eine Müllabfuhr gibt
                    String reason = "";

                    // Feiertag prüfen
                    Holiday holiday = holidaysMap.get(day);
                    if (holiday != null) {
                        reason = holiday.getName();
                    } else if (reasonsMap != null && reasonsMap.containsKey(day)) {
                        // Grund aus calendarData holen, wenn vorhanden
                        reason = reasonsMap.get(day);
                    }

                    List<String> garbageTypes = Arrays.asList(
                            GarbageType.Papiermuell.toString(),
                            GarbageType.Biomuell.toString(),
                            GarbageType.Plastikmuell.toString(),
                            GarbageType.Restmuell.toString()
                    );

                    // Prüfen, ob der Grund Müllabfuhr-Informationen enthält
                    String finalReason = reason;
                    boolean isGarbageDay = garbageTypes.stream().anyMatch(type -> finalReason.contains(type));
                    String weekday = getWeekdayName(YearMonth.of(year, month).atDay(day).getDayOfWeek().getValue());
                    boolean isSunday = "Sonntag".equalsIgnoreCase(weekday) || "So".equalsIgnoreCase(weekday);

                    boolean isHolidayCheck = (reason.startsWith("#") && reason.endsWith("#"));

                    // Zelle zeichnen
                    if (isHolidayCheck || isSunday) {
                        contentStream.setNonStrokingColor(192, 192, 192); // Grau für Feiertage
                        if (reason.startsWith("#") && reason.endsWith("#")) {
                            reason = reason.substring(1, reason.length() - 1); // Entferne die # Zeichen
                        }
                    } else if (isGarbageDay) {
                        contentStream.setNonStrokingColor(128, 128, 128);
                    } else {
                        contentStream.setNonStrokingColor(255, 255, 255); // Weiß (Standard)
                    }

                    contentStream.addRect(cellX, cellY, cellWidth, -cellHeight);
                    contentStream.fill();
                    contentStream.stroke();

                    // Text in der Zelle
                    String text = String.format("%02d", day); // Nur der Tag
                    String weekdayText = weekday; // Wochentag

                    // Zeilenumbruch für reasonText: Splitte den Text an '\n' und schreibe jede Zeile
                    String[] reasonLines = reason.split("\n");

                    contentStream.setNonStrokingColor(0, 0, 0); // Schwarz für Text
                    contentStream.beginText();
                    contentStream.newLineAtOffset(cellX + 1, cellY - 12); // Tagnummer
                    contentStream.showText(text);
                    contentStream.endText();

                    contentStream.beginText();
                    contentStream.newLineAtOffset(cellX + 13, cellY - 12); // Wochentag
                    contentStream.showText(weekdayText);
                    contentStream.endText();

                    // Grund (Reason) mit Zeilenumbruch verarbeiten
                    float reasonStartY = cellY - 7;
                    int elementsPerRow = 2;  // 2 Elemente pro Zeile

                    // Berechne, wie viele Zeilen benötigt werden
                    int numberOfRows = (int) Math.ceil(reasonLines.length / (double) elementsPerRow);

                    if (reasonLines.length < 3) {
                        contentStream.setFont(PDType1Font.HELVETICA, 9);
                    }

                    float xOffsetCellx = cellX + 24;
                    for (int i = 0; i < numberOfRows; i++) {
                        // Berechne den Y-Offset für diese Zeile (konstant für alle Elemente der Zeile)
                        float yOffset = 0;
                        if (reasonLines.length < 3) {
                            yOffset = reasonStartY - (i * 6) - 3;
                        } else {
                            yOffset = reasonStartY - (i * 6);
                        }
                        for (int j = 0; j < elementsPerRow; j++) {
                            int index = i * elementsPerRow + j;  // Bestimmt den Index des aktuellen Elements
                            if (index < reasonLines.length) {
                                contentStream.beginText();

                                // Berechne den X-Offset basierend auf der Spalte (j)
                                float xOffset = xOffsetCellx + (j * 50); // 100 ist der Abstand zwischen den Spalten
                                contentStream.newLineAtOffset(xOffset, yOffset);
                                contentStream.showText(reasonLines[index]);

                                contentStream.endText();
                            }
                        }
                    }

                    contentStream.setFont(PDType1Font.HELVETICA, 8);
                    // Horizontale Linie unter dem Tag
                    contentStream.moveTo(cellX, cellY - cellHeight);
                    contentStream.lineTo(cellX + cellWidth, cellY - cellHeight);
                    contentStream.stroke();

                    // Vertikale Trennlinien
                    contentStream.moveTo(cellX, cellY);
                    contentStream.lineTo(cellX, cellY - cellHeight);
                    contentStream.stroke();

                    contentStream.moveTo(cellX + 11, cellY);
                    contentStream.lineTo(cellX + 11, cellY - cellHeight);
                    contentStream.stroke();

                    contentStream.moveTo(cellX + 24, cellY);
                    contentStream.lineTo(cellX + 24, cellY - cellHeight);
                    contentStream.stroke();

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

