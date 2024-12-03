/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package de.bbk.autoconcur;

/**
 *
 * @author Jan Gerhardt
 */
@lombok.Data
public class MetadataBean {

    private boolean partial = false;
    private boolean manual = false;
    private boolean checkPrevious = false;
    private boolean checkSign = false;
    private boolean nSD = false;
    private boolean nD8 = false;
    private boolean nGrowth = false;
    private boolean tolD8 = false;
    private boolean toleranceGrowth = false;
    private boolean trim = false;

    public MetadataBean() {

    }
}
