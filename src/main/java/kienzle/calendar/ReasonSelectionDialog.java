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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

public class ReasonSelectionDialog extends JDialog {

    private List<JCheckBox> checkBoxes;
    private List<String> selectedReasons;
    private JCheckBox jCheckBoxHoliday;
    private JTextField jTextFieldHoliday;
    private boolean isSunday;

    public ReasonSelectionDialog(Frame owner, List<String> reasons, boolean isSunday) {
        super(owner, "Select Reasons", true);
        this.isSunday = isSunday; // Setze den Wochentagstatus

        checkBoxes = new ArrayList<>();
        selectedReasons = new ArrayList<>();

        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(reasons.size() + 3, 1));

        jCheckBoxHoliday = new JCheckBox("Feiertag:");
        jTextFieldHoliday = new JTextField();
        jTextFieldHoliday.setEnabled(false);

        for (String reason : reasons) {
            JCheckBox checkBox = new JCheckBox(reason);
            checkBox.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    updateHolidayComponents();
                }
            });
            panel.add(checkBox);
            checkBoxes.add(checkBox);
        }

        jCheckBoxHoliday.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateCheckBoxComponents();
            }
        });

        panel.add(jCheckBoxHoliday);
        panel.add(jTextFieldHoliday);

        JButton okButton = new JButton("OK");
        okButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                selectedReasons.clear();
                if (jCheckBoxHoliday.isSelected()) {
                    if (!jTextFieldHoliday.getText().trim().isEmpty()) {
                        
                        selectedReasons.add(jTextFieldHoliday.getText().trim());
                    }
                } else {
                    for (JCheckBox checkBox : checkBoxes) {
                        if (checkBox.isSelected()) {
                            selectedReasons.add(checkBox.getText());
                        }
                    }
                }
                dispose();
            }
        });

        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                selectedReasons = null;
                dispose();
            }
        });

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(okButton);
        buttonPanel.add(cancelButton);

        setLayout(new BorderLayout());
        add(panel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
        setSize(300, 300);
        setLocationRelativeTo(owner);

        // Initialisiere die Komponentenzustände
        updateHolidayComponents();
        if (isSunday) {
            disableCheckBoxes();
        }
    }

    private void updateHolidayComponents() {
        boolean anySelected = checkBoxes.stream().anyMatch(JCheckBox::isSelected);
        jCheckBoxHoliday.setEnabled(!anySelected);
        jTextFieldHoliday.setEnabled(jCheckBoxHoliday.isSelected());
    }

    private void updateCheckBoxComponents() {
        boolean holidaySelected = jCheckBoxHoliday.isSelected();
        for (JCheckBox checkBox : checkBoxes) {
            checkBox.setEnabled(!holidaySelected);
        }
        updateHolidayComponents();
    }

    private void disableCheckBoxes() {
        for (JCheckBox checkBox : checkBoxes) {
            checkBox.setVisible(false);
        }
        /*jCheckBoxHoliday.setVisible(false);
        jTextFieldHoliday.setEnabled(false);*/
    }

    public List<String> getSelectedReasons() {
        return selectedReasons;
    }
}
