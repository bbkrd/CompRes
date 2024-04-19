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
@lombok.Data
public class DecisionBean {

    private String title;
    private int nD8 = Integer.valueOf(AutoConCur.ND8DEFAULT);
    private int nGrowth = Integer.valueOf(AutoConCur.NGROWTHDEFAULT);
    private double trim = Double.valueOf(AutoConCur.TRIMDEFAULT);
    private double toleranceGrowth = Double.valueOf(AutoConCur.TOLGROWTHDEFAULT);

    private Decision decision = Decision.UNKNOWN;
    private boolean development;
    private boolean extremevalue;
    private String errortext;
    private DecisionBean preperiodbean;

    private double[] quantsGrowth;
    private double lastGrowth;

    private double[] intervalSF;
    private double lastSF;
    private double lastD10;

    private double growthOld;
    private double growthNew;

    public DecisionBean() {
        decision = Decision.UNKNOWN;
    }

    public DecisionBean(String title, int nD8, int nGrowth, double trim, double tolerance) {
        this.title = title;
        this.nD8 = nD8;
        this.nGrowth = nGrowth;
        this.trim = trim;
        this.toleranceGrowth = tolerance;
        decision = Decision.UNKNOWN;
    }

    public static DecisionBean ErrorBean(String title, String error) {
        DecisionBean bean = new DecisionBean();
        bean.setTitle(title);
        bean.setDecision(Decision.UNKNOWN);
        bean.setErrortext("No decision could be made. " + System.lineSeparator() + error);
        return bean;
    }
}
