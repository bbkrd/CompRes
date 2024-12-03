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

    private String file;

    private String title;

    private boolean partial = "1".equals(AutoConCur.PARTIALDEFAULT);
    private boolean manual = "1".equals(AutoConCur.MANUALDEFAULT);
    private boolean checkPrevious ="1".equals(AutoConCur.CHECKPREVIOUSDEFAULT);
    private boolean checkSign = "1".equals(AutoConCur.CHECKSIGNDEFAULT);
    private int nSD = Integer.valueOf(AutoConCur.NSDDEFAULT);
    private int nD8 = Integer.valueOf(AutoConCur.ND8DEFAULT);
    private int nGrowth = Integer.valueOf(AutoConCur.NGROWTHDEFAULT);
    private double tolD8 = Double.valueOf(AutoConCur.TOLD8DEFAULT);
    private double toleranceGrowth = Double.valueOf(AutoConCur.TOLGROWTHDEFAULT);
    private double trim = Double.valueOf(AutoConCur.TRIMDEFAULT);

    private Decision decision = Decision.UNKNOWN;

    private boolean seasonalFactor;
    private boolean growthRate;
    private boolean signChange;
    private boolean extremevalue;
    private boolean fixOutlier;

    private String errortext;
    private DecisionBean preperiodbean;

    //private double[] quantsGrowth;
    private double lastGrowth;

    private double[] intervalSF;
    private double lastSF;
    private double lastD10;

    private double growthOld;
    private double growthNew;

    public DecisionBean() {
        decision = Decision.UNKNOWN;
    }

    public DecisionBean(String title, boolean manual, boolean checkSign, int nSD, int nD8, int nGrowth, double toleranceD8, double toleranceGrowth, double trim) {
        this.title = title;
        this.manual = manual;
        this.checkSign = checkSign;
        this.nSD = nSD;
        this.nD8 = nD8;
        this.nGrowth = nGrowth;
        this.tolD8 = toleranceD8;
        this.toleranceGrowth = toleranceGrowth;
        this.trim = trim;
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
