/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.bbk.outputpdf.util;

/**
 *
 * @author s4504ch
 */
public class Frozen {

    private static final String FROZEN = " [frozen]";

    public static String removeFrozen(String stringWithFrozen) {
        if (stringWithFrozen.contains(FROZEN)) {
            stringWithFrozen = stringWithFrozen.replace(FROZEN, "");
        }
        return stringWithFrozen;
    }
}
