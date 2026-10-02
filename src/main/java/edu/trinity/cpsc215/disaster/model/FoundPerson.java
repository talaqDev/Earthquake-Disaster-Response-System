package edu.trinity.cpsc215.disaster.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public class FoundPerson {
    private final String id;
    private final Species species;
    private final Status status;
    private final int age;
    private final GpsCoordinate location;
    private final DnaProfile dnaProfile;

    @JsonCreator
    public FoundPerson(
            @JsonProperty("id") String id,
            @JsonProperty("species") Species species,
            @JsonProperty("status") Status status,
            @JsonProperty("age") int age,
            @JsonProperty("location") GpsCoordinate location,
            @JsonProperty("dnaProfile") DnaProfile dnaProfile) {
        this.id = id;
        this.species = species;
        this.status = status;
        this.age = age;
        this.location = location;
        this.dnaProfile = dnaProfile;
    }

    public String getId() { return id; }
    public Species getSpecies() { return species; }
    public Status getStatus() { return status; }
    public int getAge() { return age; }
    public GpsCoordinate getLocation() { return location; }
    public DnaProfile getDnaProfile() { return dnaProfile; }

    @JsonIgnore
    public int getTriagePriority() {
        int priority = 0;
        if (status == Status.ALIVE) priority += 100;
        if (age < 18) priority += 50;
        else if (age > 65) priority += 30;
        return priority;
    }

    @Override
    public String toString() {
        return String.format("FoundPerson[%s, %s, %s, age=%d, %s]",
                id, species, status, age, location);
    }
}
