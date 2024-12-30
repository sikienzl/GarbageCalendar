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
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class ReasonSelectionDialog extends JDialog {

    private List<JCheckBox> checkBoxes;
    private List<String> selectedReasons;
    private JCheckBox jCheckBoxHoliday;
    private JTextField jTextFieldHoliday;
    private ReasonDialogState state;

    public ReasonSelectionDialog(Frame owner, List<String> reasons, boolean isSunday) {
        super(owner, "Select Reasons", true);

        checkBoxes = new ArrayList<>();
        selectedReasons = new ArrayList<>();

        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(reasons.size() + 3, 1));

        // Initialisiere Komponenten mit Factory
        jCheckBoxHoliday = ComponentFactory.createCheckBox("Feiertag:", e -> updateCheckBoxComponents());
        jTextFieldHoliday = ComponentFactory.createTextField(false);

        for (String reason : reasons) {
            JCheckBox checkBox = ComponentFactory.createCheckBox(reason, e -> updateHolidayComponents());
            panel.add(checkBox);
            checkBoxes.add(checkBox);
        }

        panel.add(jCheckBoxHoliday);
        panel.add(jTextFieldHoliday);

        // Zustand festlegen (State Pattern)
        state = isSunday ? new SundayState() : new NormalState();
        state.configureDialog(this);

        // Buttons hinzufügen
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(createOkButton());
        buttonPanel.add(createCancelButton());

        setLayout(new BorderLayout());
        add(panel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
        setSize(300, 300);
        setLocationRelativeTo(owner);
    }

    private JButton createOkButton() {
        return ComponentFactory.createButton("OK", e -> {
            selectedReasons.clear();
            if (jCheckBoxHoliday.isSelected() && !jTextFieldHoliday.getText().trim().isEmpty()) {
                selectedReasons.add("#" + jTextFieldHoliday.getText().trim() + "#");
            } else {
                for (JCheckBox checkBox : checkBoxes) {
                    if (checkBox.isSelected()) {
                        selectedReasons.add(checkBox.getText());
                    }
                }
            }
            dispose();
        });
    }

    private JButton createCancelButton() {
        return ComponentFactory.createButton("Cancel", e -> {
            selectedReasons = null;
            dispose();
        });
    }

    public void updateHolidayComponents() {
        boolean anySelected = checkBoxes.stream().anyMatch(JCheckBox::isSelected);
        jCheckBoxHoliday.setEnabled(!anySelected);
        jTextFieldHoliday.setEnabled(jCheckBoxHoliday.isSelected());
    }

    public void updateCheckBoxComponents() {
        boolean holidaySelected = jCheckBoxHoliday.isSelected();
        for (JCheckBox checkBox : checkBoxes) {
            checkBox.setEnabled(!holidaySelected);
        }
        updateHolidayComponents();
    }

    // Getter für Zustands-Implementierungen
    public List<JCheckBox> getCheckBoxes() {
        return checkBoxes;
    }

    public JCheckBox getHolidayCheckBox() {
        return jCheckBoxHoliday;
    }

    public JTextField getHolidayTextField() {
        return jTextFieldHoliday;
    }

    public List<String> getSelectedReasons() {
        return selectedReasons;
    }
}
