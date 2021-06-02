package ru.sastsy.dental;

import java.util.ArrayList;

public class Jaw {

    private final String hostName;
    private ArrayList<Tooth> toothList;

    public Jaw(String hostName) {
        this.hostName = hostName;
        this.toothList = new ArrayList<>();
    }

    public void addTooth(Tooth tooth) {
        this.toothList.add(tooth);
    }

    public ArrayList<Tooth> getToothList() {
        return this.toothList;
    }

    public Tooth getTooth(int i) {
        return this.toothList.get(i);
    }
}
