package kienzle.day;
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

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import kienzle.month.Month;
import kienzle.weekday.Weekday;

public class Day {
    private int day;
    private Month month;
    private int year; 

    public Day(int day, Month month, int year) {
        this.day = day;
        this.month = month;
        this.year = year;
    }

    public int getDay() {
        return day;
    }

    public void setDay(int day) {
        this.day = day;
    }

    public Month geMonth() {
        return month;
    }

    public void setMonth(Month month) {
        this.month = month;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public Weekday getWeekday(int year, int month, int day) {
        LocalDate date = LocalDate.of(year, month, day);
        DayOfWeek dayOfWeek = date.getDayOfWeek();

    // Umwandlung von DayOfWeek in Weekday
        switch (dayOfWeek) {
            case MONDAY:
                return Weekday.MO;
            case TUESDAY:
                return Weekday.DI;
            case WEDNESDAY:
                return Weekday.MI;
            case THURSDAY:
                return Weekday.DO;
            case FRIDAY:
                return Weekday.FR;
            case SATURDAY:
                return Weekday.SA;
            case SUNDAY:
                return Weekday.SO;
            default:
                throw new IllegalStateException("Unexpected value: " + dayOfWeek);
        }   
    }

    
}
