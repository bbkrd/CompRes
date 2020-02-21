/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.bbk.concurreport;

public enum ReportStyle {
    SHORT("Short report"),
    LONG("Long report"),
    D8B("Just D8B"),
    INDIVIDUAL("Userdefined");

    private final String displayName;

    private ReportStyle(String displayName) {
        this.displayName = displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }

}
