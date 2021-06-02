package ru.sastsy.dental;

public class Event {

    private final String date;
    private final String place;
    private final String doctor;
    private final String comment;

    public Event(String date, String place, String doctor, String comment) {
        this.date = date;
        this.place = place;
        this.doctor = doctor;
        this.comment = comment;
    }

    public String getDate() {
        return this.date;
    }

    public String getPlace() {
        return this.place;
    }

    public String getDoctor() {
        return this.doctor;
    }

    public String getComments() {
        return this.comment;
    }
}
