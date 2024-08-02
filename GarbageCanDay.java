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

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class GarbageCanDay extends Day {
    private GarbageCan garbageCan;
    
    public GarbageCanDay(int day, Month month, int year, GarbageCan garbageCan) {
        super(day, month, year);
        this.garbageCan = garbageCan;
    }

    @Override
    public String toString() {
        LocalDate date = LocalDate.of(super.getYear(), super.geMonth().ordinal() + 1, super.getDay());
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
        return garbageCan.getType() + " (" + garbageCan.getColour() + ") am " + date.format(formatter)
                + " (" + getWeekday(super.getYear(), super.geMonth().ordinal() + 1, super.getDay()).getShoString() + ")";
    }
}
