package com.richard.weger.wegerqualitycontrol.domain;

import java.util.ArrayList;
import java.util.List;

public class ControlCardReport extends Report {
    public ControlCardReport(){
        fillItemsList();
    }

    @Override
    public String toString() {
        return "Control Card Report";
    }

    @Override
    protected void fillItemsList() {
         itemList = new ArrayList<Item>(){
            {
                int x = 1;
                add(new Item(x++, "Geräteaufbau laut Zeichnung"));
                add(new Item(x++, "Abmessungen der Geräte lt. Zeichnung" ));
                add(new Item(x++, "Liefersektionen laut Zeichnung "));
                add(new Item(x++, "Ansaug- und Ausblasöffnungen lt. Zchng. "));
                add(new Item(x++, "Bedienseite laut Zeichnung "));
                add(new Item(x++, "Position Registeranschlüsse lt. Zchng. "));
                add(new Item(x++, "Position Kondensatanschlüsse lt. Zchng. "));
                add(new Item(x++, "Reihenfolge der Register laut Zeichnung "));
                add(new Item(x++, "Registeranschlussdurchmesser lt. Datenblatt "));
                add(new Item(x++, "Laufrichtung Flachriemen"));
                add(new Item(x++, "Zugänglichkeit Motorklemmkasten "));
                add(new Item(x++, "Gerät innen späne- und schmutzfrei gereinigt "));
                add(new Item(x++, "Gerät aussen gereinigt "));
                add(new Item(x++, "Wannen späne- und schmutzfrei gereinigt "));
                add(new Item(x++, "Grundrahmen lt. Zeichnung "));
                add(new Item(x++, "Gerätefüsse lt. Zeichnung "));
                add(new Item(x++, "Kranlaschen montiert "));
                add(new Item(x++, "Klappen leichtgängig "));
                add(new Item(x++, "Funktionstest Klappen: vollständig öffen- u. schliessbar "));
                add(new Item(x++, "alle Hinweisschilder aufgeklebt"));
                add(new Item(x++, "Regelgerät für Rotortauscher vorhanden"));
                add(new Item(x, "Innenverbindungslaschen bei Ansaugwänden und Klappen vorhanden "));
            }
        };
    }
}
