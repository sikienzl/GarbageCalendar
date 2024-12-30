package kienzle.calendar;

import javax.swing.*;
import java.awt.event.ActionListener;

public class ComponentFactory {

    public static JCheckBox createCheckBox(String text, ActionListener listener) {
        JCheckBox checkBox = new JCheckBox(text);
        checkBox.addActionListener(listener);
        return checkBox;
    }

    public static JButton createButton(String text, ActionListener listener) {
        JButton button = new JButton(text);
        button.addActionListener(listener);
        return button;
    }

    public static JTextField createTextField(boolean enabled) {
        JTextField textField = new JTextField();
        textField.setEnabled(enabled);
        return textField;
    }
}

