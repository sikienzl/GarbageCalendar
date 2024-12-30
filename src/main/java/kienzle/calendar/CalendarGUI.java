package kienzle.calendar;

/*
 * Copyright 2024 Siegfried Kienzle
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import kienzle.garbage.GarbageCan;
import kienzle.holiday.Holiday;
import kienzle.month.Month;
import kienzle.type.GarbageType;
import kienzle.calendar.CustomTableCellRenderer;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.Year;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.*;
import java.util.List;

public class CalendarGUI extends JFrame {

    private CardLayout cardLayout;
    private JPanel cardPanel;
    private List<GarbageCan> garbageCans;
    private Map<String, Color> reasonColorMap;
    private JComboBox<Integer> yearComboBox;
    private JLabel titleLabel;
    private JTable table;
    private JLabel monthLabel;
    private Map<Integer, Holiday> holidaysMap = new HashMap<>();
    //private Map<YearMonth, List<String>> calendarData = new HashMap<>();
    private Map<YearMonth, Map<Integer, String>> calendarData = new HashMap<>();


    public CalendarGUI(List<GarbageCan> garbageCans) {
        super("Kalender");
        this.garbageCans = garbageCans;
        reasonColorMap = new HashMap<>();
        for (GarbageCan gc : garbageCans) {
            reasonColorMap.put(gc.getType().toString(), gc.getColorObject());
        }

        setTitle("Kalender");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);

        JPanel mainPanel = new JPanel(new BorderLayout());

        JPanel headerPanel = new JPanel(new BorderLayout());
        JButton prevButton = new JButton("<");
        JButton nextButton = new JButton(">");
        titleLabel = new JLabel("Kalender", SwingConstants.CENTER);

        yearComboBox = new JComboBox<>();
        fillYearComboBox(); // Methode zum Füllen der Jahre
        yearComboBox.addActionListener(new YearSelectionListener());

        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        titlePanel.add(titleLabel);
        titlePanel.add(yearComboBox);

        JButton exportButton = new JButton("PDF Exportieren");
        exportButton.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Speichern als PDF");

            // Setzt einen Filter für PDF-Dateien
            FileNameExtensionFilter filter = new FileNameExtensionFilter("PDF-Dateien (*.pdf)", "pdf");
            fileChooser.setFileFilter(filter);

            if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                File selectedFile = fileChooser.getSelectedFile();
                // Fügt ".pdf" hinzu, wenn der Benutzer keine Endung eingegeben hat
                if (!selectedFile.getName().endsWith(".pdf")) {
                    selectedFile = new File(selectedFile.getAbsolutePath() + ".pdf");
                }

                try {
                    CalendarPDFGenerator pdfGenerator = new CalendarPDFGenerator();
                    pdfGenerator.generatePDF(selectedFile, (int) yearComboBox.getSelectedItem(), calendarData, holidaysMap);
                    JOptionPane.showMessageDialog(this, "PDF erfolgreich gespeichert!", "Erfolg", JOptionPane.INFORMATION_MESSAGE);
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(this, "Fehler beim Speichern der PDF: " + ex.getMessage(), "Fehler", JOptionPane.ERROR_MESSAGE);
                }
            }
        });




        headerPanel.add(prevButton, BorderLayout.WEST);
        headerPanel.add(titlePanel, BorderLayout.CENTER);
        headerPanel.add(exportButton, BorderLayout.SOUTH);
        headerPanel.add(nextButton, BorderLayout.EAST);

        mainPanel.add(headerPanel, BorderLayout.NORTH);

        cardLayout = new CardLayout();
        cardPanel = new JPanel(cardLayout);

        updateCalendarPanel(); // Kalenderpanel zum ersten Mal erstellen

        mainPanel.add(cardPanel, BorderLayout.CENTER);

        add(mainPanel);

        prevButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                cardLayout.show(cardPanel, "FirstHalf");
            }
        });

        nextButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                cardLayout.show(cardPanel, "SecondHalf");
            }
        });
    }

    private void fillYearComboBox() {
        int currentYear = Year.now().getValue();
        for (int i = currentYear; i <= currentYear + 10; i++) {
            yearComboBox.addItem(i);
        }
        yearComboBox.setSelectedItem(currentYear); // Das aktuelle Jahr als Standardwert setzen
    }

    private void updateCalendarPanel() {
        cardPanel.removeAll();
        int selectedYear = (int) yearComboBox.getSelectedItem();

        titleLabel.setText("Kalender"); // Aktualisiere den Titel

        JPanel firstHalfPanel = createCalendarPanel(selectedYear, 1, 6);
        JPanel secondHalfPanel = createCalendarPanel(selectedYear, 7, 12);

        cardPanel.add(firstHalfPanel, "FirstHalf");
        cardPanel.add(secondHalfPanel, "SecondHalf");

        cardPanel.revalidate();
        cardPanel.repaint();
    }

    private JPanel createCalendarPanel(int year, int startMonth, int endMonth) {
        JPanel panel = new JPanel(new GridLayout(1, endMonth - startMonth + 1)); // Eine Zeile, eine Spalte pro Monat

        String[] columnNames = {"Tag", "Wochentag", "Grund"};

        for (int month = startMonth; month <= endMonth; month++) {
            YearMonth yearMonth = YearMonth.of(year, month);
            int daysInMonth = yearMonth.lengthOfMonth();
            Object[][] data = new Object[daysInMonth][3];

            for (int day = 1; day <= daysInMonth; day++) {
                LocalDate date = yearMonth.atDay(day);
                data[day - 1][0] = date.getDayOfMonth(); // Tag (Nummer)
                data[day - 1][1] = date.getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.GERMAN); // Wochentag (Text)

                // Hole oder erstelle die Map von Tagen zu Gründen für diesen Monat
                Map<Integer, String> dailyReasons = calendarData.computeIfAbsent(yearMonth, k -> new HashMap<>());

                // Wenn der Tag einen Grund hat, weise diesen zu
                String reason = dailyReasons.getOrDefault(day, "");
                if (!reason.isEmpty()) {
                    data[day - 1][2] = reason;
                } else {
                    Holiday holiday = holidaysMap.get(day);
                    if (holiday != null) {
                        data[day - 1][2] = holiday.getName();
                    } else {
                        data[day - 1][2] = ""; // Platzhalter für Grund
                    }
                }
            }

            int finalMonth1 = month;
            DefaultTableModel model = new DefaultTableModel(data, columnNames) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    // Nur die "Grund"-Spalte ist editierbar und nur, wenn es kein Feiertag ist
                    return column == 2 && !holidaysMap.containsKey(row + 1);
                }

                @Override
                public void setValueAt(Object value, int row, int column) {
                    super.setValueAt(value, row, column);

                    if (column == 2) { // Nur die "Grund"-Spalte ist editierbar
                        YearMonth currentMonth = YearMonth.of(year, finalMonth1);

                        // Stelle sicher, dass die Map für den Monat existiert
                        Map<Integer, String> dailyReasons = calendarData.computeIfAbsent(currentMonth, k -> new HashMap<>());

                        // Den geänderten Wert speichern
                        int day = row + 1; // Der Tag ist die Zeilen-Nummer + 1 (da Zeilen bei 0 beginnen)
                        dailyReasons.put(day, value != null ? value.toString() : "");  // Verhindert Null-Werte
                    }
                }
            };

            table = new JTable(model);
            table.getColumnModel().getColumn(2).setCellEditor(new ReasonCellEditor(this, getReasonTypes()));
            table.setDefaultRenderer(Object.class, new CustomTableCellRenderer(holidaysMap, reasonColorMap));

            JPopupMenu popupMenu = new JPopupMenu();
            JMenuItem holidayItem = new JMenuItem("Als Feiertag markieren");
            popupMenu.add(holidayItem);

            table.addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    if (SwingUtilities.isRightMouseButton(e) && table.isShowing()) {
                        int row = table.rowAtPoint(e.getPoint());
                        int column = table.columnAtPoint(e.getPoint());
                        table.setRowSelectionInterval(row, row);
                        table.setColumnSelectionInterval(column, column);
                        popupMenu.show(table, e.getX(), e.getY());
                    }
                }
            });

            int finalMonth = month;
            model.addTableModelListener(e -> {
                int row = e.getFirstRow();
                int column = e.getColumn();
                if (column == 2) { // Nur bei der "Grund"-Spalte
                    Object newValue = model.getValueAt(row, column);
                    YearMonth currentMonth = YearMonth.of(year, finalMonth);
                    Map<Integer, String> dailyReasons = calendarData.computeIfAbsent(currentMonth, k -> new HashMap<>());

                    // Den neuen Wert in die Map setzen
                    int day = row + 1; // Der Tag ist die Zeilen-Nummer + 1
                    dailyReasons.put(day, newValue != null ? newValue.toString() : "");
                    System.out.println("DEBUG: Gründe-Map nach Änderung: " + dailyReasons + " Year: " + year + " month: " + finalMonth + " row: " + row + " column " + column);
                }
            });

            holidayItem.addActionListener(e -> markAsHoliday(table.getSelectedRow(), year));

            JScrollPane scrollPane = new JScrollPane(table);
            JPanel tablePanel = new JPanel(new BorderLayout());
            tablePanel.add(scrollPane, BorderLayout.CENTER);

            monthLabel = new JLabel(yearMonth.getMonth().getDisplayName(TextStyle.FULL, Locale.GERMAN), SwingConstants.CENTER);
            monthLabel.setFont(new Font("Arial", Font.BOLD, 16));
            tablePanel.add(monthLabel, BorderLayout.NORTH);

            panel.add(tablePanel);
        }

        return panel;
    }

    private List<String> getReasonTypes() {
        return garbageCans.stream()
            .map(gc -> gc.getType().toString())
            .toList();
    }

    private void markAsHoliday(int row, int year) {
        String holidayName = JOptionPane.showInputDialog(this, "Name des Feiertags:");
        if (holidayName != null && !holidayName.trim().isEmpty()) {
            int day = (int) table.getValueAt(row, 0);
            String monthName = monthLabel.getText();
            Month month = Month.valueOf(monthName.toUpperCase(Locale.GERMAN));

            // Create a new holiday
            Holiday holiday = new Holiday(day, month, year, holidayName);
            holidaysMap.put(row, holiday);

            // Set the holiday name in the table
            table.setValueAt(holiday.toString(), row, 2);
            ((DefaultTableModel) table.getModel()).fireTableRowsUpdated(row, row);

            // Call the method to make the row read-only
            setHolidayRowReadOnly(row);
        }
    }

    private void setHolidayRowReadOnly(int row) {
        // Loop through all columns in the selected row and set their background color to light gray.
        for (int column = 0; column < table.getColumnCount(); column++) {
            Component c = table.getCellRenderer(row, column).getTableCellRendererComponent(
                    table, table.getValueAt(row, column), false, false, row, column);
            c.setBackground(Color.LIGHT_GRAY); // Set background color for the row
        }

        // Disable editing for this row by setting the cell editor to null
        for (int column = 0; column < table.getColumnCount(); column++) {
            table.getColumnModel().getColumn(column).setCellEditor(null);
        }

        // Refresh the table to reflect changes
        table.repaint();
    }

    private class YearSelectionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            updateCalendarPanel();
        }
    }

}
