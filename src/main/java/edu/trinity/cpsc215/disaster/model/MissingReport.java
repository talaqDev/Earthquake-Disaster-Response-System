package edu.trinity.cpsc215.disaster.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class MissingReport {
    private final String id;
    private final String name;
    private final Species species;
    private final String relationship;
    private final String submittedBy;
    private final DnaProfile dnaProfile;

    @JsonCreator
    public MissingReport(
            @JsonProperty("id") String id,
            @JsonProperty("name") String name,
            @JsonProperty("species") Species species,
            @JsonProperty("relationship") String relationship,
            @JsonProperty("submittedBy") String submittedBy,
            @JsonProperty("dnaProfile") DnaProfile dnaProfile) {
        this.id = id;
        this.name = name;
        this.species = species;
        this.relationship = relationship;
        this.submittedBy = submittedBy;
        this.dnaProfile = dnaProfile;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public Species getSpecies() { return species; }
    public String getRelationship() { return relationship; }
    public String getSubmittedBy() { return submittedBy; }
    public DnaProfile getDnaProfile() { return dnaProfile; }

    @Override
    public String toString() {
        return String.format("MissingReport[%s, %s, %s, by=%s]",
                id, name, species, submittedBy);
    }
}
