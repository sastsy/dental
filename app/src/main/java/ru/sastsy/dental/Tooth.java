package ru.sastsy.dental;

import java.util.ArrayList;

public class Tooth {

    public String name;
    public ArrayList<Long> state;
    public int number;
    public ArrayList<Event> event;

    String[] name_list = {"Третий моляр (зуб мудрости)", "Второй моляр", "Первый моляр", "Второй премоляр",
    "Первый премоляр", "Клык", "Боковой резец", "Центральный резец"};

    public Tooth() {}

    public Tooth(int number) {
        if (number <= 8 || number >= 17 && number <= 24) {
            if (number / 8 == (float) number / 8) this.name = name_list[7];
            else this.name = name_list[8 - (8 * (number / 8 + 1) - number) - 1];
        }
        else {
            if (number / 8 == (float) number / 8) this.name = name_list[0];
            else this.name = name_list[8 * (number / 8 + 1) - number];
        }
        this.number = number;
        this.state = new ArrayList<>();
        this.event = new ArrayList<>();
    }

    public void addEvent(Event event) {
        this.event.add(event);
    }

    public int getNumber() {
        return number;
    }

    public String getName() {
        return name;
    }

    public ArrayList<Event> getEvent() {
        return event;
    }

    public ArrayList<Long> getState() {
        return state;
    }
}
