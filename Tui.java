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

import java.time.YearMonth;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.Locale;

public class Tui {
    

    public void print_grid() {

        String[][] calendar = new String[32][6];
        
        // Monatsnamen in die erste Zeile einfügen
        String[] monthNames = {"Januar", "Februar", "März", "April", "Mai", "Juni"};
        for (int monthCounter = 0; monthCounter < 6; monthCounter++) {
            calendar[0][monthCounter] = monthNames[monthCounter];
        }

        // Tage einfügen
        for (int monthCounter = 0; monthCounter < 6; monthCounter++) {
            YearMonth yearMonth = YearMonth.of(2024, monthCounter + 1);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd");
            for (int day = 1; day <= yearMonth.lengthOfMonth(); day++) {
                LocalDate date = yearMonth.atDay(day);
                String formattedDate = date.format(formatter);
                String dayOfWeek = date.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.GERMAN);
                String firstTwoLettersOfWeekday = dayOfWeek.substring(0, 2);
                calendar[day][monthCounter] = formattedDate + " | " + firstTwoLettersOfWeekday + " | reason";
            }
        }

        // Kalender ausgeben
        for (int row = 0; row < 32; row++) {
            for (int col = 0; col < 6; col++) {
                if (calendar[row][col] != null) {
                    System.out.print(calendar[row][col] + "\t");
                } else {
                    System.out.print("\t\t\t");
                }
            }
            System.out.println();
        }
    }
}
