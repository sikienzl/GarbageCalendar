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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    /*private List<String> splitReasons(List<String> reasons) {
        List<String> newReasons = new LinkedList<>();
        String[] arrayReasons = new String[0];
        for(String reason: reasons) {
            if(reason.contains("\n")) {
                arrayReasons = reason.split("\n");
            } else {
                newReasons.add(reason);
            }

        }

        for(int reasonNumber = 0; reasonNumber < arrayReasons.length; reasonNumber++) {
            newReasons.add(arrayReasons[reasonNumber]);
        }

        return newReasons;
    }*/

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

                for (int day = 1; day <= yearMonth.lengthOfMonth(); day++) {
                    float cellX = monthStartX;
                    float cellY = startY - (day * cellHeight);

                    // Überprüfen, ob es ein Feiertag oder eine Müllabfuhr gibt
                    String reason = reasons != null && reasons.size() >= day ? reasons.get(day - 1) : "";


                    Holiday holiday = holidaysMap.get(day);


                    boolean isHoliday = holiday != null;
                    String reasonText = (isHoliday ? holiday.getName() : reason);
                    //boolean isGarbageDay = reason != null && !reason.isEmpty();
                    //boolean isGarbageDay = reasonText.contains("*muell*");
                    List<String> garbageTypes = Arrays.asList(
                            GarbageType.Papiermuell.toString(),
                            GarbageType.Biomuell.toString(),
                            GarbageType.Plastikmuell.toString(),
                            GarbageType.Restmuell.toString()
                    );

                    // Prüfen, ob "muell" oder ein GarbageType enthalten ist
                    boolean isGarbageDay = garbageTypes.stream().anyMatch(type -> reasonText.contains(type));
                    String weekday = getWeekdayName(YearMonth.of(year, month).atDay(day).getDayOfWeek().getValue());
                    boolean isSunday = "Sonntag".equalsIgnoreCase(weekday) || "So".equalsIgnoreCase(weekday);

                    boolean isHolidayCheck = (reasonText.startsWith("#") && reasonText.endsWith("#"));



                    // Zelle zeichnen
                    if (isHolidayCheck || isSunday) {
                        contentStream.setNonStrokingColor(192, 192, 192); // Grau für Feiertage
                    } else if (isGarbageDay) {
                        contentStream.setFont(PDType1Font.HELVETICA_BOLD, 10);
                        //contentStream.setNonStrokingColor(0, 0, 0); // Schwarz für Müllabfuhr
                        contentStream.setNonStrokingColor(192, 210, 192);
                    } else {
                        contentStream.setNonStrokingColor(255, 255, 255); // Weiß (Standard)
                    }

                    contentStream.addRect(cellX, cellY, cellWidth, -cellHeight);
                    contentStream.fill();
                    //contentStream.setStrokingColor(0, 0, 0);
                    contentStream.stroke();

                    // Text in der Zelle
                    String text = String.format("%02d", day); // Nur der Tag
                    String weekdayText = weekday; // Wochentag

                    // Zeilenumbruch für reasonText: Splitte den Text an '\n' und schreibe jede Zeile
                    String[] reasonLines = reasonText.split("\n");

                    contentStream.setNonStrokingColor(0, 0, 0); // Schwarz für Text
                    contentStream.beginText();
                    contentStream.newLineAtOffset(cellX + 5, cellY - 12); // Tagnummer
                    contentStream.showText(text);
                    contentStream.endText();

                    contentStream.beginText();
                    contentStream.newLineAtOffset(cellX + 35, cellY - 12); // Wochentag
                    contentStream.showText(weekdayText);
                    contentStream.endText();

                    // Grund (Reason) mit Zeilenumbruch verarbeiten
                    float reasonStartY = cellY - 12;
                    for (int i = 0; i < reasonLines.length; i++) {
                        contentStream.beginText();
                        contentStream.newLineAtOffset(cellX + 80, reasonStartY - (i * 10)); // Verschiebung der Zeilen nach unten
                        contentStream.showText(reasonLines[i]);
                        contentStream.endText();
                    }

                    // Horizontale Linie unter dem Tag
                    contentStream.moveTo(cellX, cellY - cellHeight);
                    contentStream.lineTo(cellX + cellWidth, cellY - cellHeight);
                    contentStream.stroke();

                    // Vertikale Trennlinien
                    contentStream.moveTo(cellX, cellY);
                    contentStream.lineTo(cellX, cellY - cellHeight);
                    contentStream.stroke();

                    contentStream.moveTo(cellX + 30, cellY);
                    contentStream.lineTo(cellX + 30, cellY - cellHeight);
                    contentStream.stroke();

                    contentStream.moveTo(cellX + 75, cellY);
                    contentStream.lineTo(cellX + 75, cellY - cellHeight);
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

