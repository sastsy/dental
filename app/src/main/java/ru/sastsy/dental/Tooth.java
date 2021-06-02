package ru.sastsy.dental;

import java.util.ArrayList;

public class Tooth {

    private final String name;
    private ArrayList<Long> state;
    private final int number;
    private ArrayList<String> event;

    public Tooth(int number) {
        String[] name_list = {"Третий моляр (зуб мудрости)", "Второй моляр", "Первый моляр", "Второй премоляр",
                "Первый премоляр", "Клык", "Боковой резец", "Центральный резец"};

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

    public void addEvent(String event) {
        this.event.add(event);
    }

    public int getNumber() {
        return this.number;
    }

    public String getName() {
        return this.name;
    }

    public ArrayList<String> getEvent() {
        return this.event;
    }

    public ArrayList<Long> getState() {
        return this.state;
    }

    public void setState(ArrayList<Long> list) {
        this.state = list;
    }

    public void setEvent(ArrayList<String> list) {
        this.event = list;
    }
}
