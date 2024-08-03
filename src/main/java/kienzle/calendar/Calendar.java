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

import java.util.ArrayList;
import java.util.List;

import kienzle.garbage.GarbageCan;
import kienzle.garbage.GarbageCanDay;
import kienzle.holiday.Holiday;
import kienzle.month.Month;
import kienzle.tui.Tui;
import kienzle.type.*;

public class Calendar {
    public static void main(String[] args) {
        Holiday neujahr = new Holiday(1, Month.JANUAR, 2024, "Neujahr");
        Holiday weihnachten = new Holiday(24, Month.DEZEMBER, 2024, "Heilig Abend");
        Holiday silvester = new Holiday(31, Month.DEZEMBER, 2024, "Silvester");

        System.out.println(neujahr);
        System.out.println(weihnachten);
        System.out.println(silvester);

        GarbageCan yellowGarbageCan = new GarbageCan();
        String yellowColourString = "yellow";
        yellowGarbageCan.setColour(yellowColourString);
        yellowGarbageCan.setType(GarbageType.Plastikmuell);

        GarbageCanDay day_plastikmuell_1 = new GarbageCanDay(2, Month.APRIL, 2024, yellowGarbageCan);
        
        GarbageCan brownGarbageCan = new GarbageCan();
        String brownColourString = "brown";
        brownGarbageCan.setColour(brownColourString);
        brownGarbageCan.setType(GarbageType.Biomuell);

        GarbageCan blackGarbageCan = new GarbageCan();
        String blackColourString = "black";
        blackGarbageCan.setColour(blackColourString);
        blackGarbageCan.setType(GarbageType.Restmuell);

        GarbageCan blueGarbageCan = new GarbageCan();
        String blueColourString = "blue";
        blueGarbageCan.setColour(blueColourString);
        blueGarbageCan.setType(GarbageType.Papiermuell);
        
        GarbageCanDay day_biomuell_1 = new GarbageCanDay(4, Month.MAI, 2024, brownGarbageCan);

        System.out.println(day_plastikmuell_1);
        System.out.println(day_biomuell_1);

        Tui tui = new Tui();

        tui.print_grid();

        try {
            List<GarbageCan> garbageCans1 = new ArrayList<>();
            garbageCans1.add(yellowGarbageCan);
            garbageCans1.add(brownGarbageCan);
            garbageCans1.add(blackGarbageCan);
            garbageCans1.add(blueGarbageCan);
            GarbageCan.saveToXML(garbageCans1, "garbageCans.xml");
            
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            List<GarbageCan> garbageCans = GarbageCan.loadFromXML("garbageCans.xml");
            CalendarGUI gui = new CalendarGUI(garbageCans);
            gui.setVisible(true);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

   
}
