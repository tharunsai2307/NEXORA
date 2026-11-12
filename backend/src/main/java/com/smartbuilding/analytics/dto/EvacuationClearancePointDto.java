package com.smartbuilding.analytics.dto;

public class EvacuationClearancePointDto {
    private int elapsedMinutes;
    private int clearedRooms;
    private int remainingRooms;
    private int personsEvacuated;

    public EvacuationClearancePointDto() {}

    public EvacuationClearancePointDto(int elapsedMinutes, int clearedRooms, int remainingRooms, int personsEvacuated) {
        this.elapsedMinutes = elapsedMinutes;
        this.clearedRooms = clearedRooms;
        this.remainingRooms = remainingRooms;
        this.personsEvacuated = personsEvacuated;
    }

    public int getElapsedMinutes() { return elapsedMinutes; }
    public void setElapsedMinutes(int elapsedMinutes) { this.elapsedMinutes = elapsedMinutes; }

    public int getClearedRooms() { return clearedRooms; }
    public void setClearedRooms(int clearedRooms) { this.clearedRooms = clearedRooms; }

    public int getRemainingRooms() { return remainingRooms; }
    public void setRemainingRooms(int remainingRooms) { this.remainingRooms = remainingRooms; }

    public int getPersonsEvacuated() { return personsEvacuated; }
    public void setPersonsEvacuated(int personsEvacuated) { this.personsEvacuated = personsEvacuated; }
}

