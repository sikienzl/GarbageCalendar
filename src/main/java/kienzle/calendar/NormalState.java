package kienzle.calendar;

public class NormalState implements ReasonDialogState {
    @Override
    public void configureDialog(ReasonSelectionDialog dialog) {
        dialog.updateHolidayComponents();
    }
}
