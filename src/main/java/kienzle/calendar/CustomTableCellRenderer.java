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

        // Prüfen, ob der aktuelle Tag ein Sonntag ist
        String dayOfWeek = (String) table.getValueAt(row, 1);
        boolean isSunday = "So.".equals(dayOfWeek);

        // Prüfen, ob es einen Feiertag in der Zeile gibt
        boolean isHoliday = holidaysMap.containsKey(row);

        // Abrufen des Grundes aus der entsprechenden Zelle
        String reason = (String) table.getValueAt(row, 2);

        // Standard-Hintergrund- und Textfarben setzen
        c.setBackground(Color.WHITE);
        c.setForeground(Color.BLACK);

        // Feiertag oder Sonntag - Setze die gesamte Zeile auf Grau
        if (isHoliday || isSunday) {
            c.setBackground(Color.LIGHT_GRAY);
        } else {
            // Prüfen, ob der Grund eine spezielle Farbe hat
            Color reasonColor = reasonColorMap.get(reason);
            if (reasonColor != null) {
                c.setBackground(reasonColor);
                c.setForeground(reasonColor.equals(Color.BLACK) ? Color.WHITE : Color.BLACK);
            }

            // Prüfen, ob der Grund ein gültiger Mülltyp ist
            if (reason != null && !reason.trim().isEmpty() && 
                !isGarbageType(reason)) {
                System.out.print(reason);
                c.setBackground(Color.LIGHT_GRAY); // Setzt die Hintergrundfarbe auf Grau
            }
        }

        return c;
    }

    // Prüfen, ob der Grund ein gültiger Mülltyp ist
    private boolean isGarbageType(String reason) {
       for (GarbageType type : GarbageType.values()) {
            if (type.toString().equals(reason)) {
                return true;
            }
        }
        return false;
    }
}
