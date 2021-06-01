/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.bbk.concurreport.html;

import ec.tss.html.*;
import ec.tstoolkit.algorithm.ProcessingContext;
import ec.tstoolkit.dstats.T;
import ec.tstoolkit.eco.ConcentratedLikelihood;
import ec.tstoolkit.modelling.PreadjustmentVariable;
import ec.tstoolkit.modelling.arima.JointRegressionTest;
import ec.tstoolkit.modelling.arima.PreprocessingModel;
import ec.tstoolkit.timeseries.regression.*;
import ec.tstoolkit.timeseries.simplets.TsData;
import ec.tstoolkit.timeseries.simplets.TsFrequency;
import ec.tstoolkit.utilities.NameManager;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

/**
 *
 * @author Deutsche Bundesbank
 */
public class HTMLRegressionModel extends AbstractHtmlElement {

    private final PreprocessingModel preprocessingModel;
    private final TsVariableList regressionVariables;
    private final ConcentratedLikelihood concentratedLikelihood;
    private final int freeParametersCount;

    public HTMLRegressionModel(final PreprocessingModel model) {
        preprocessingModel = model;
        regressionVariables = preprocessingModel.description.buildRegressionVariables();
        concentratedLikelihood = preprocessingModel.estimation.getLikelihood();
        freeParametersCount = preprocessingModel.description.getArimaComponent().getFreeParametersCount();
    }

    @Override
    public void write(HtmlStream stream) throws IOException {
        stream.write(HtmlTag.HEADER2, "Regression model").newLine();
        writeMean(stream);
        TsFrequency context = preprocessingModel.description.getEstimationDomain().getFrequency();
        writeRegressionItems(stream, context, true, var -> var instanceof ITradingDaysVariable);
        writeRegressionItems(stream, context, false, var -> var instanceof ILengthOfPeriodVariable);
        writeFixedRegressionItems(stream, "Fixed calendar effects", context, var -> var.isCalendar());
        writeRegressionItems(stream, context, false, var -> var instanceof IMovingHolidayVariable);
        writeFixedRegressionItems(stream, "Fixed moving holidays effects", context, var -> var.isMovingHoliday());
        writeOutliers(stream, true, context);
        writeOutliers(stream, false, context);
        writeFixedRegressionItems(stream, "Fixed outliers", context, var -> var.isOutlier());
        writeRegressionItems(stream, "Ramps", true, context, var -> var instanceof Ramp);
        writeRegressionItems(stream, "Intervention variables", true, context, var -> var instanceof InterventionVariable);
        writeRegressionItems(stream, "User variables", true, context, var -> var instanceof IUserTsVariable
                && !(var instanceof InterventionVariable) && !(var instanceof Ramp));
        writeFixedRegressionItems(stream, "Fixed other regression effects", context, var -> var.isUser());
        writeMissing(stream);
    }

    private void writeMean(HtmlStream stream) throws IOException {
        if (!preprocessingModel.description.isMean()) {
            return;
        }
        if (preprocessingModel.description.isEstimatedMean()) {
            writeEstimatedMean(stream);
        } else {
            writeFixedMean(stream);
        }
    }

    private void writeEstimatedMean(HtmlStream stream) throws IOException {
        double[] b = concentratedLikelihood.getB();
        stream.write(HtmlTag.HEADER3, "Mean");
        stream.open(new HtmlTable());
        stream.open(HtmlTag.TABLEROW);
        stream.write(new HtmlTableHeader(""));
        stream.write(new HtmlTableHeader("Coefficient"));
        stream.write(new HtmlTableHeader("T-Stat"));
        stream.write(new HtmlTableHeader("P[|T| &gt t]"));
        stream.close(HtmlTag.TABLEROW);
        stream.open(HtmlTag.TABLEROW);
        stream.write(new HtmlTableHeader("mu"));
        stream.write(new HtmlTableCell(df4.format(b[0])));
        T t = new T();
        t.setDegreesofFreedom(concentratedLikelihood.getDegreesOfFreedom(true, freeParametersCount));
        double tval = concentratedLikelihood.getTStat(0, true, freeParametersCount);
        stream.write(new HtmlTableCell(formatT(tval)));
        double prob = 1 - t.getProbabilityForInterval(-tval, tval);
        stream.write(new HtmlTableCell(df4.format(prob)));
        stream.close(HtmlTag.TABLEROW);
        stream.close(HtmlTag.TABLE);
        stream.newLine();
    }

    private void writeFixedMean(HtmlStream stream) throws IOException {
        stream.write(HtmlTag.HEADER3, "Fixed mean");
        stream.open(new HtmlTable());
        stream.open(HtmlTag.TABLEROW);
        stream.write(new HtmlTableHeader(""));
        stream.write(new HtmlTableHeader("Coefficient"));
        stream.close(HtmlTag.TABLEROW);
        stream.open(HtmlTag.TABLEROW);
        stream.write(new HtmlTableHeader("mu"));
        stream.write(new HtmlTableCell(df4.format(preprocessingModel.description.getArimaComponent().getMeanCorrection())));
        stream.close(HtmlTag.TABLEROW);
        stream.close(HtmlTag.TABLE);
        stream.newLine();
    }

    private void writeOutliers(HtmlStream stream, boolean prespecified, TsFrequency context) throws IOException {

        String header = prespecified ? "Prespecified outliers" : "Outliers";
        TsVariableSelection<ITsVariable> regs = regressionVariables.select(var -> var instanceof IOutlierVariable
                && preprocessingModel.description.isPrespecified((IOutlierVariable) var) == prespecified);

        if (regs.isEmpty()) {
            return;
        }

        List<TsVariableSelection.Item<ITsVariable>> items = Arrays.asList(regs.elements());
        Collections.sort(items, (o1, o2) -> {
            if (o1.variable instanceof IOutlierVariable && o2.variable instanceof IOutlierVariable) {
                int i = ((IOutlierVariable) o1.variable).getPosition().compareTo(((IOutlierVariable) o2.variable).getPosition());
                if (i == 0) {
                    return ((IOutlierVariable) o1.variable).getCode().compareTo(((IOutlierVariable) o2.variable).getCode());
                }
                return i;
            }
            return 0;
        });

        T t = new T();
        t.setDegreesofFreedom(concentratedLikelihood.getDegreesOfFreedom(true, freeParametersCount));
        double[] b = concentratedLikelihood.getB();
        stream.write(HtmlTag.HEADER3, header);
        stream.open(new HtmlTable());
        stream.open(HtmlTag.TABLEROW);
        stream.write(new HtmlTableHeader(""));
        stream.write(new HtmlTableHeader("Coefficients"));
        stream.write(new HtmlTableHeader("T-Stat"));
        stream.write(new HtmlTableHeader("P[|T| &gt t]"));
        stream.close(HtmlTag.TABLEROW);
        int start = preprocessingModel.description.getRegressionVariablesStartingPosition();
        for (TsVariableSelection.Item<ITsVariable> reg : items) {
            stream.open(HtmlTag.TABLEROW);
            stream.write(new HtmlTableHeader(reg.variable.getDescription(context)));
            stream.write(new HtmlTableCell(df4.format(b[start + reg.position])));
            double tval = concentratedLikelihood.getTStat(start + reg.position, true, freeParametersCount);
            stream.write(new HtmlTableCell(formatT(tval)));
            double prob = 1 - t.getProbabilityForInterval(-tval, tval);
            stream.write(new HtmlTableCell(df4.format(prob)));
            stream.close(HtmlTag.TABLEROW);
        }
        stream.close(HtmlTag.TABLE);
        stream.newLine();
    }

    private <V extends ITsVariable> void writeRegressionItems(HtmlStream stream, TsFrequency context, boolean jointest, Predicate<ITsVariable> predicate) throws IOException {
        TsVariableSelection<ITsVariable> regs = regressionVariables.select(predicate);
        if (regs.isEmpty()) {
            return;
        }
        T t = new T();
        t.setDegreesofFreedom(concentratedLikelihood.getDegreesOfFreedom(true, freeParametersCount));
        double[] b = concentratedLikelihood.getB();
        int start = preprocessingModel.description.getRegressionVariablesStartingPosition();
        for (TsVariableSelection.Item<ITsVariable> reg : regs.elements()) {
            stream.write(HtmlTag.HEADER3, reg.variable.getDescription(context));
            stream.open(new HtmlTable());
            stream.open(HtmlTag.TABLEROW);
            stream.write(new HtmlTableHeader(""));
            stream.write(new HtmlTableHeader("Coefficients"));
            stream.write(new HtmlTableHeader("T-Stat"));
            stream.write(new HtmlTableHeader("P[|T| &gt t]"));
            stream.close(HtmlTag.TABLEROW);
            int ndim = reg.variable.getDim();
            for (int j = 0; j < reg.variable.getDim(); ++j) {
                stream.open(HtmlTag.TABLEROW);
                stream.write(new HtmlTableHeader(reg.variable.getItemDescription(j, context)));
                stream.write(new HtmlTableCell(df4.format(b[start + j + reg.position])));
                double tval = concentratedLikelihood.getTStat(start + j + reg.position, true, freeParametersCount);
                stream.write(new HtmlTableCell(formatT(tval)));
                double prob = 1 - t.getProbabilityForInterval(-tval, tval);
                stream.write(new HtmlTableCell(df4.format(prob)));
                stream.close(HtmlTag.TABLEROW);
            }
            if (ndim > 1 && reg.variable instanceof GregorianCalendarVariables) {
                // we compute the derived sunday variable
                stream.open(HtmlTag.TABLEROW);
                stream.write(new HtmlTableHeader("Sunday (derived)"));
                double bd = 0;
                int k0 = start + reg.position, k1 = k0 + ndim;
                for (int k = k0; k < k1; ++k) {
                    bd -= b[k];
                }
                stream.write(new HtmlTableCell(df4.format(bd)));
                double var = concentratedLikelihood.getBVar(true, freeParametersCount).subMatrix(k0, k1, k0, k1).sum();
                double tval = bd / Math.sqrt(var);
                stream.write(new HtmlTableCell(formatT(tval)));
                double prob = 1 - t.getProbabilityForInterval(-tval, tval);
                stream.write(new HtmlTableCell(df4.format(prob)));
                stream.close(HtmlTag.TABLEROW);
            }
            stream.close(HtmlTag.TABLE);
            stream.newLine();
        }
        int nvars = regs.getVariablesCount();
        if (jointest && regs.getItemsCount() == 1 && nvars > 1) {
            try {
                JointRegressionTest jtest = new JointRegressionTest(.05);
                boolean ok = jtest.accept(concentratedLikelihood, freeParametersCount, start + regs.get(0).position, nvars, null);
                StringBuilder builder = new StringBuilder();
                builder.append("Joint F-Test = ").append(df2.format(jtest.getTest().getValue()))
                        .append(" (").append(df4.format(jtest.getTest().getPValue())).append(')');

                if (!ok) {
                    stream.write(HtmlTag.IMPORTANT_TEXT, builder.toString(), Bootstrap4.TEXT_DANGER);
                } else {
                    stream.write(HtmlTag.EMPHASIZED_TEXT, builder.toString());
                }
                stream.newLines(2);
            } catch (RuntimeException ex) {
            }
        }
    }

    private <V extends ITsVariable> void writeRegressionItems(HtmlStream stream, String header, boolean desc, TsFrequency context, Predicate<ITsVariable> predicate) throws IOException {
        TsVariableSelection<ITsVariable> regs = regressionVariables.select(predicate);
        if (regs.isEmpty()) {
            return;
        }
        if (header != null) {
            stream.write(HtmlTag.HEADER3, header);
        }
        T t = new T();
        stream.open(new HtmlTable());
        stream.open(HtmlTag.TABLEROW);
        stream.write(new HtmlTableHeader(""));
        stream.write(new HtmlTableHeader("Coefficients"));
        stream.write(new HtmlTableHeader("T-Stat"));
        stream.write(new HtmlTableHeader("P[|T| &gt t]"));
        stream.close(HtmlTag.TABLEROW);
        t.setDegreesofFreedom(concentratedLikelihood.getDegreesOfFreedom(true, freeParametersCount));
        double[] b = concentratedLikelihood.getB();
        int start = preprocessingModel.description.getRegressionVariablesStartingPosition();
        for (TsVariableSelection.Item<ITsVariable> reg : regs.elements()) {
            int ndim = reg.variable.getDim();
            for (int j = 0; j < reg.variable.getDim(); ++j) {
                stream.open(HtmlTag.TABLEROW);
                if (ndim > 1 || desc) {
                    stream.write(new HtmlTableHeader(reg.variable.getItemDescription(j, context)));
                } else {
                    stream.write(new HtmlTableHeader(""));
                }
                stream.write(new HtmlTableCell(df4.format(b[start + j + reg.position])));
                double tval = concentratedLikelihood.getTStat(start + j + reg.position, true, freeParametersCount);
                stream.write(new HtmlTableCell(formatT(tval)));
                double prob = 1 - t.getProbabilityForInterval(-tval, tval);
                stream.write(new HtmlTableCell(df4.format(prob)));
                stream.close(HtmlTag.TABLEROW);
            }
        }
        stream.close(HtmlTag.TABLE);
        stream.newLine();
    }

    private <V extends ITsVariable> void writeFixedRegressionItems(HtmlStream stream, String header, TsFrequency context, Predicate<PreadjustmentVariable> predicate) throws IOException {
        List<PreadjustmentVariable> regs = preprocessingModel.description.selectPreadjustmentVariables(predicate);
        if (regs.isEmpty()) {
            return;
        }
        if (header != null) {
            stream.write(HtmlTag.HEADER3, header);
        }
        stream.open(new HtmlTable());
        stream.open(HtmlTag.TABLEROW);
        stream.write(new HtmlTableHeader(""));
        stream.write(new HtmlTableHeader("Coefficients"));
        stream.close(HtmlTag.TABLEROW);

        for (PreadjustmentVariable reg : regs) {
            ITsVariable cur = reg.getVariable();
            double[] c = reg.getCoefficients();
            for (int j = 0; j < cur.getDim(); ++j) {
                stream.open(HtmlTag.TABLEROW);

                String name = cur.getItemDescription(j, context);
                if (name.startsWith("td|")) {
                    name = name.replace("td|", "");
                    ProcessingContext pc = ProcessingContext.getActiveContext();
                    NameManager<TsVariables> tsVariableManagers = pc.getTsVariableManagers();
                    String prefix = name.substring(0, name.indexOf('.'));
                    if (tsVariableManagers.contains(prefix)) {
                        TsVariables tss = tsVariableManagers.get(prefix);
                        String tsname = name.substring(name.indexOf('.') + 1, name.length());
                        if (tss.contains(tsname)) {
                            {
                                stream.write(new HtmlTableHeader(tss.get(tsname).getDescription(context)));
                            }
                        }
                    }
                } else {
                    stream.write(new HtmlTableHeader(cur.getItemDescription(j, context)));
                }
                stream.write(new HtmlTableCell(df4.format(c[j])));
                stream.close(HtmlTag.TABLEROW);
            }
        }

        stream.close(HtmlTag.TABLE);
        stream.newLine();
    }

    private void writeMissing(HtmlStream stream) throws IOException {
        MissingValueEstimation[] missings = preprocessingModel.missings(true);
        if (missings == null) {
            return;
        }
        stream.write(HtmlTag.HEADER3, "Missing values");
        stream.open(new HtmlTable());
        stream.open(HtmlTag.TABLEROW);
        stream.write(new HtmlTableHeader("Periods"));
        stream.write(new HtmlTableHeader("Value"));
        stream.write(new HtmlTableHeader("Standard error"));
        stream.write(new HtmlTableHeader("Untransformed value"));
        stream.close(HtmlTag.TABLEROW);
        for (int i = 0; i < missings.length; ++i) {
            stream.open(HtmlTag.TABLEROW);
            stream.write(new HtmlTableHeader(missings[i].getPosition().toString()));
            stream.write(new HtmlTableCell(df4.format(missings[i].getValue())));
            stream.write(new HtmlTableCell(df4.format(missings[i].getStdev())));
            TsData tmp = new TsData(missings[i].getPosition(), new double[]{missings[i].getValue()}, false);
            preprocessingModel.backTransform(tmp, true, true);
            stream.write(new HtmlTableCell(df4.format(tmp.get(0))));
            stream.close(HtmlTag.TABLEROW);
        }
        stream.close(HtmlTag.TABLE);
        stream.newLines(2);

    }

}
