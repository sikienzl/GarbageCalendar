package kienzle.calendar;

import javax.swing.*;

public class SundayState implements ReasonDialogState {
    @Override
    public void configureDialog(ReasonSelectionDialog dialog) {
        dialog.getHolidayCheckBox().setEnabled(true);
        dialog.getHolidayTextField().setEnabled(false);
        for (JCheckBox checkBox : dialog.getCheckBoxes()) {
            checkBox.setVisible(false);
        }
    }
}

