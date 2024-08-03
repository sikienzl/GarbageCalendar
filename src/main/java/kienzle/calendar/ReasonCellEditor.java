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
import javax.swing.table.TableCellEditor;
import java.awt.*;
import java.util.List;

public class ReasonCellEditor extends AbstractCellEditor implements TableCellEditor {

    private JFrame parentFrame;
    private List<String> reasons;
    private String selectedReasons;

    public ReasonCellEditor(JFrame parentFrame, List<String> reasons) {
        this.parentFrame = parentFrame;
        this.reasons = reasons;
    }

    @Override
    public Object getCellEditorValue() {
        return selectedReasons; // Gibt die zuletzt ausgewählten Gründe zurück
    }

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        // Setzt die ausgewählten Gründe als den aktuellen Wert der Zelle
        if (value != null) {
            selectedReasons = (String) value;
        } else {
            selectedReasons = "";
        }

        JButton button = new JButton("Select Reasons");
        button.addActionListener(e -> {
            // Erzeugt den Dialog und wartet auf die Benutzeraktion
            ReasonSelectionDialog dialog = new ReasonSelectionDialog(parentFrame, reasons);
            dialog.setVisible(true);

            // Überprüft, ob Benutzer eine Auswahl getroffen hat
            List<String> dialogSelectedReasons = dialog.getSelectedReasons();
            if (dialogSelectedReasons != null) {
                selectedReasons = String.join("\n", dialogSelectedReasons);
                fireEditingStopped(); // Benachrichtigt JTable über die Bearbeitung
            }
        });
        return button;
    }
}
