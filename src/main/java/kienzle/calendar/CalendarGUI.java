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
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDate;
import java.time.Year;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

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

        headerPanel.add(prevButton, BorderLayout.WEST);
        headerPanel.add(titlePanel, BorderLayout.CENTER);
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
        JPanel panel = new JPanel(new GridLayout(1, 6)); // 1 Zeile, 6 Spalten

        String[] columnNames = {"Tag", "Wochentag", "Reason"};

        for (int month = startMonth; month <= endMonth; month++) {
            YearMonth yearMonth = YearMonth.of(year, month);
            int daysInMonth = yearMonth.lengthOfMonth();
            Object[][] data = new Object[daysInMonth][3];

            for (int day = 1; day <= daysInMonth; day++) {
                LocalDate date = yearMonth.atDay(day);
                data[day - 1][0] = date.getDayOfMonth();
                data[day - 1][1] = date.getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.GERMAN);
                data[day - 1][2] = ""; // Platzhalter für Grund
            }

            DefaultTableModel model = new DefaultTableModel(data, columnNames) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return column == 2 && !holidaysMap.containsKey(row); // Nur die "Reason"-Spalte ist editierbar und nur wenn es kein Feiertag ist
                }
            };

            table = new JTable(model);
            table.getColumnModel().getColumn(2).setCellEditor(new ReasonCellEditor(this, getReasonTypes()));
            table.setDefaultRenderer(Object.class, new CustomTableCellRenderer(holidaysMap, reasonColorMap));

            // Kontextmenü für Feiertage
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

            Holiday holiday = new Holiday(day, month, year, holidayName);
            holidaysMap.put(row, holiday);

            table.setValueAt(holiday.toString(), row, 2);
            ((DefaultTableModel) table.getModel()).fireTableRowsUpdated(row, row);

            setHolidayRowReadOnly(row);
        }
    }

    private void setHolidayRowReadOnly(int row) {
        for (int column = 0; column < table.getColumnCount(); column++) {
            table.getCellRenderer(row, column).getTableCellRendererComponent(table, null, false, false, row, column).setBackground(Color.LIGHT_GRAY);
        }
        table.setDefaultEditor(Object.class, null); // Macht die Zellen schreibgeschützt
    }

    private class YearSelectionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            updateCalendarPanel();
        }
    }

}
