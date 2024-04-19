/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.bbk.autoconcur;

/**
 *
 * @author Jan Gerhardt
 */
public enum Decision {
    UNKNOWN("Unknown"),
    KEEP("Keep"),
    UPDATE("Update"),
    CHECK("Check");

    private final String name;

    private Decision(String s) {
        name = s;
    }

    public boolean equalsName(String otherName) {
        return name.equals(otherName);
    }

    public String toString() {
        return this.name;
    }
}
