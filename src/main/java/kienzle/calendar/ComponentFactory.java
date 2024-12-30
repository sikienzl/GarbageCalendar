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

