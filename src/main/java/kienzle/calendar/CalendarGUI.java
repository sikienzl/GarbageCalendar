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

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import kienzle.garbage.GarbageCan;
import kienzle.type.*;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.YearMonth;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Locale;

public class CalendarGUI extends JFrame {

    private CardLayout cardLayout;
    private JPanel cardPanel;
    private List<GarbageCan> garbageCans;
    private Map<String, Color> reasonColorMap;

    public CalendarGUI(List<GarbageCan> garbageCans) {
        super("Kalender 2024");
        this.garbageCans = garbageCans;
        reasonColorMap = new HashMap<>();
        for (GarbageCan gc : garbageCans) {
            reasonColorMap.put(gc.getType().toString(), gc.getColorObject());
        }

        setTitle("Kalender 2024");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);

        JPanel mainPanel = new JPanel(new BorderLayout());

        JPanel headerPanel = new JPanel(new BorderLayout());
        JButton prevButton = new JButton("<");
        JButton nextButton = new JButton(">");
        JLabel titleLabel = new JLabel("Kalender 2024", SwingConstants.CENTER);

        headerPanel.add(prevButton, BorderLayout.WEST);
        headerPanel.add(titleLabel, BorderLayout.CENTER);
        headerPanel.add(nextButton, BorderLayout.EAST);

        mainPanel.add(headerPanel, BorderLayout.NORTH);

        cardLayout = new CardLayout();
        cardPanel = new JPanel(cardLayout);

        JPanel firstHalfPanel = createCalendarPanel(1, 6);
        JPanel secondHalfPanel = createCalendarPanel(7, 12);

        cardPanel.add(firstHalfPanel, "FirstHalf");
        cardPanel.add(secondHalfPanel, "SecondHalf");

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

    private JPanel createCalendarPanel(int startMonth, int endMonth) {
        JPanel panel = new JPanel(new GridLayout(1, 6)); // 1 Zeile, 6 Spalten

        String[] columnNames = {"Tag", "Wochentag", "Reason"};

        for (int month = startMonth; month <= endMonth; month++) {
            YearMonth yearMonth = YearMonth.of(2024, month);
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
                    return column == 2; // Nur die "Reason"-Spalte ist editierbar
                }
            };

            JTable table = new JTable(model);
            table.getColumnModel().getColumn(2).setCellEditor(new ReasonCellEditor(this, getReasonTypes()));
            table.setDefaultRenderer(Object.class, new CustomTableCellRenderer());

            JScrollPane scrollPane = new JScrollPane(table);
            JPanel tablePanel = new JPanel(new BorderLayout());
            tablePanel.add(scrollPane, BorderLayout.CENTER);

            JLabel monthLabel = new JLabel(yearMonth.getMonth().getDisplayName(TextStyle.FULL, Locale.GERMAN), SwingConstants.CENTER);
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

    private class CustomTableCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

            String reason = (String) table.getValueAt(row, 2); // Grund in der "Reason"-Spalte
            Color color = reasonColorMap.get(reason);
            if (color != null) {
                c.setBackground(color);
                if (color.equals(Color.BLACK)) {
                    c.setForeground(Color.WHITE); 
                } else {
                    c.setForeground(Color.BLACK); 
                }
            } else {
                c.setBackground(Color.WHITE);
                c.setForeground(Color.BLACK);
            }

            return c;
        }
    }

    public static void main(String[] args) {
        List<GarbageCan> garbageCans = List.of(
            new GarbageCan("yellow", GarbageType.Plastikmuell),
            new GarbageCan("blue", GarbageType.Restmuell),
            new GarbageCan("brown", GarbageType.Biomuell),
            new GarbageCan("black", GarbageType.Restmuell)
        );

        CalendarGUI calendarGUI = new CalendarGUI(garbageCans);
        calendarGUI.setVisible(true);
    }
}
