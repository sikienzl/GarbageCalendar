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

import kienzle.holiday.Holiday;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.util.Map;

public class CustomTableCellRenderer extends DefaultTableCellRenderer {

    private final Map<Integer, Holiday> holidaysMap;
    private final Map<String, Color> reasonColorMap;

    public CustomTableCellRenderer(Map<Integer, Holiday> holidaysMap, Map<String, Color> reasonColorMap) {
        this.holidaysMap = holidaysMap;
        this.reasonColorMap = reasonColorMap;
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

        // Prüfen, ob der aktuelle Tag ein Sonntag ist
        String dayOfWeek = (String) table.getValueAt(row, 1);
        boolean isSunday = "So.".equals(dayOfWeek); // "So." ist die Kurzform für Sonntag im Deutschen

        if (holidaysMap.containsKey(row) || isSunday) {
            c.setBackground(Color.LIGHT_GRAY);
            c.setForeground(Color.BLACK);
        } else {
            String reason = (String) table.getValueAt(row, 2);
            Color color = reasonColorMap.get(reason);
            if (color != null) {
                c.setBackground(color);
                c.setForeground(color.equals(Color.BLACK) ? Color.WHITE : Color.BLACK);
            } else {
                c.setBackground(Color.WHITE);
                c.setForeground(Color.BLACK);
            }
        }

        return c;
    }
}
