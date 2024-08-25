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

import kienzle.type.GarbageType;
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

        String dayOfWeek = (String) table.getValueAt(row, 1);
        boolean isSunday = "So.".equals(dayOfWeek);

        boolean isHoliday = holidaysMap.containsKey(row);

        String reason = (String) table.getValueAt(row, 2);

        c.setBackground(Color.WHITE);
        c.setForeground(Color.BLACK);

        if (isHoliday || isSunday) {
            c.setBackground(Color.LIGHT_GRAY);
        } else {
            Color reasonColor = reasonColorMap.get(reason);
            if (reasonColor != null) {
                c.setBackground(reasonColor);
                c.setForeground(reasonColor.equals(Color.BLACK) ? Color.WHITE : Color.BLACK);
            }

            if (reason != null && !reason.trim().isEmpty() && !isGarbageType(reason)) {
                c.setBackground(Color.LIGHT_GRAY); 
            }

            if (column == 2 && value instanceof String) {
                ((JLabel) c).setText("<html>" + reason.replaceAll("\n", "<br/>") + "</html>");
                
                int width = table.getColumnModel().getColumn(column).getWidth();
                int prefHeight = (int) c.getPreferredSize().getHeight();
                table.setRowHeight(row, prefHeight);
            }
        }

        return c;
    }

    private boolean isGarbageType(String reason) {
        for (GarbageType type : GarbageType.values()) {
            if (type.toString().equals(reason)) {
                return true;
            }
        }
        return false;
    }
}
