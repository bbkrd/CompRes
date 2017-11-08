/* 
 * Copyright 2017 Deutsche Bundesbank
 * 
 * Licensed under the EUPL, Version 1.1 or – as soon they
 * will be approved by the European Commission - subsequent 
 * versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the
 * Licence.
 * You may obtain a copy of the Licence at:
 * 
 * http://ec.europa.eu/idabc/eupl.html
 * 
 * Unless required by applicable law or agreed to in
 * writing, software distributed under the Licence is
 * distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied.
 * See the Licence for the specific language governing
 * permissions and limitations under the Licence.
 */
package de.bbk.concur.util;

import static de.bbk.concur.util.SeasonallyAdjusted_Saved.calcSeasonallyAdjusted;
import ec.satoolkit.DecompositionMode;
import ec.satoolkit.DefaultSeriesDecomposition;
import ec.satoolkit.algorithm.implementation.X13ProcessingFactory;
import ec.satoolkit.x11.X11Specification;
import ec.satoolkit.x13.X13Specification;
import ec.tstoolkit.MetaData;
import ec.tstoolkit.algorithm.CompositeResults;
import ec.tstoolkit.algorithm.SequentialProcessing;
import ec.tstoolkit.modelling.DefaultTransformationType;
import ec.tstoolkit.modelling.RegressionTestSpec;
import ec.tstoolkit.modelling.arima.x13.MovingHolidaySpec;
import ec.tstoolkit.modelling.arima.x13.OutlierSpec;
import ec.tstoolkit.modelling.arima.x13.SingleOutlierSpec;
import ec.tstoolkit.modelling.arima.x13.TradingDaysSpec;
import ec.tstoolkit.timeseries.calendars.TradingDaysType;
import ec.tstoolkit.timeseries.regression.OutlierType;
import ec.tstoolkit.timeseries.simplets.TsData;
import ec.tstoolkit.timeseries.simplets.TsFrequency;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author Christiane Hofer
 */
public class SeasonallyAdjusted_SavedTest {
    
    @Test
    public void SeasonallyAdjustedTestX11() {
        calcSeasonallyAdjustedX11Test(DecompositionMode.Multiplicative, "Mult X11");
        calcSeasonallyAdjustedX11Test(DecompositionMode.Additive, "Add X11");
        calcSeasonallyAdjustedX11Test(DecompositionMode.LogAdditive, "LogAdd X11");
        calcSeasonallyAdjustedX11Test(DecompositionMode.Undefined, "Undefined X11");
    }
    
    private void calcSeasonallyAdjustedX11Test(DecompositionMode mode, String message) {
        X13Specification x13Specification = X13Specification.RSAX11;
        X11Specification xs = getX11Spec(mode);
        x13Specification.setX11Specification(xs);
        SequentialProcessing<TsData> processing = X13ProcessingFactory.instance.generateProcessing(x13Specification);
        CompositeResults comprest = processing.process(TSDATA_UNEMPLOYMENT_SEASONAL);
        MetaData meta = new MetaData();
        TsData tsd10_all = comprest.getData("d-tables.d10", TsData.class);
        if (mode.isMultiplicative()) {
            tsd10_all = tsd10_all.times(100);
        }
        
        TsData_MetaDataConverter.convertTsToMetaData(tsd10_all, meta, SavedTables.SEASONALFACTOR);

// There is no forecast for x11
        TsData tsdA1_all = comprest.getData("a-tables.a1", TsData.class);
//        TsData_MetaDataConverter.convertTsToMetaData(tsdA1_all, meta, SavedTables.FORECAST);
        
       TsData tsdSavedSAS = calcSeasonallyAdjusted(meta, mode, tsdA1_all).getTsData();
        TsData tsd11 = comprest.getData("d-tables.d11", TsData.class);
        Assert.assertTrue(message + " D11", CompareTsData.compareTS(tsdSavedSAS.fittoDomain(tsd11.getDomain()), tsd11, 0.000000001));
    }
    
    private X11Specification getX11Spec(DecompositionMode mode) {
        X11Specification x11Specification = new X11Specification();
        x11Specification.setMode(mode);
        return x11Specification;
    }
    
    @Test
    public void SeasonallyAdjustedTestLogAdd() {
        String strMessage = "LogAdd";
        DecompositionMode mode = DecompositionMode.LogAdditive;
        X13Specification x13spec = getX13Spec(mode, true);
        calcSeasonallyAdjustedTest(x13spec, strMessage);
    }
    
    @Test
    public void SeasonallyAdjustedRSA1() {
        String strMessage = "RSA1";
        calcSeasonallyAdjustedTest(X13Specification.RSA1, strMessage);
    }
    
    @Test
    public void SeasonallyAdjustedTestMultTD() {
        String strMessage = "mult,TD";
        DecompositionMode mode = DecompositionMode.Multiplicative;
        X13Specification x13spec = getX13Spec(mode, true);
        TradingDaysSpec daysSpec = new TradingDaysSpec();
        daysSpec.setTradingDaysType(TradingDaysType.TradingDays);
        daysSpec.setTest(RegressionTestSpec.None);
        x13spec.getRegArimaSpecification().getRegression().setTradingDays(daysSpec);
        calcSeasonallyAdjustedTest(x13spec, strMessage);
    }
    
    @Test
    public void SeasonallyAdjustedLogUndefinedTest() {
        String strMessage;
        X13Specification x13spec;
        DecompositionMode mode;
        strMessage = "Log Undefined";
        mode = DecompositionMode.Undefined;
        x13spec = getX13Spec(mode, true);
        calcSeasonallyAdjustedTest(x13spec, strMessage);
    }
    
    @Test
    public void SeasonallyAdjustedTestAllX13() {
        String strMessage;
        X13Specification x13spec;
        DecompositionMode mode;
        
        strMessage = "Log mult";
        mode = DecompositionMode.Multiplicative;
        x13spec = getX13Spec(mode, true);
        calcSeasonallyAdjustedTest(x13spec, strMessage);
        strMessage = "Log LogAdd";
        mode = DecompositionMode.LogAdditive;
        x13spec = getX13Spec(mode, true);
        calcSeasonallyAdjustedTest(x13spec, strMessage);

// sollte aber funktionieren
//        strMessage = "Log Add";
//        mode = DecompositionMode.Additive;
//        x13spec = getX13Spec(mode, true);
//        calcSeasonallyAdjustedTest(x13spec, strMessage);
//        strMessage = "Level mult";
//        mode = DecompositionMode.Multiplicative;
//        x13spec = getX13Spec(mode, false);
//        calcSeasonallyAdjustedTest(x13spec, strMessage);
//         strMessage = "Level LogAdd";
//        mode = DecompositionMode.LogAdditive;
//        x13spec = getX13Spec(mode, false);
//        calcSeasonallyAdjustedTest(x13spec, strMessage);
        strMessage = "Level Undefined";
        mode = DecompositionMode.Undefined;
        x13spec = getX13Spec(mode, false);
        calcSeasonallyAdjustedTest(x13spec, strMessage);
        
        strMessage = "Level Add";
        mode = DecompositionMode.Additive;
        x13spec = getX13Spec(mode, false);
        calcSeasonallyAdjustedTest(x13spec, strMessage);
        
        strMessage = "mult";
        mode = DecompositionMode.Multiplicative;
        x13spec = getX13Spec(mode, true);
        calcSeasonallyAdjustedTest(x13spec, strMessage);
        
        strMessage = "mult,Outlier";
        SingleOutlierSpec singleOutlierSpec = new SingleOutlierSpec(OutlierType.AO, 2.0);
        OutlierSpec outlierSpec = new OutlierSpec();
        outlierSpec.add(singleOutlierSpec);
        x13spec.getRegArimaSpecification().setOutliers(outlierSpec);
        calcSeasonallyAdjustedTest(x13spec, strMessage);
        
        strMessage = "mult,TD";
        TradingDaysSpec daysSpec = new TradingDaysSpec();
        daysSpec.setTradingDaysType(TradingDaysType.TradingDays);
        daysSpec.setTest(RegressionTestSpec.None);
        x13spec.getRegArimaSpecification().getRegression().setTradingDays(daysSpec);
        calcSeasonallyAdjustedTest(x13spec, strMessage);
        
        strMessage = "mult,TD,easter";
        x13spec = X13Specification.RSA5;
        x13spec.getRegArimaSpecification().getTransform().setFunction(DefaultTransformationType.Log);
        x13spec.getX11Specification().setMode(DecompositionMode.Multiplicative);
        
        daysSpec.setTradingDaysType(TradingDaysType.TradingDays);
        daysSpec.setTest(RegressionTestSpec.None);
        x13spec.getRegArimaSpecification().getRegression().setTradingDays(daysSpec);
        
        MovingHolidaySpec[] holidaySpec = {new MovingHolidaySpec()};
        holidaySpec[0].setType(MovingHolidaySpec.Type.Easter);
        holidaySpec[0].setTest(RegressionTestSpec.None);
        holidaySpec[0].setW(8);
        
        x13spec.getRegArimaSpecification().getRegression().setMovingHolidays(holidaySpec);
        calcSeasonallyAdjustedTest(x13spec, strMessage);
        
        strMessage = "add,TD,easter";
        x13spec = X13Specification.RSA5;
        x13spec.getRegArimaSpecification().getTransform().setFunction(DefaultTransformationType.None);
        x13spec.getX11Specification().setMode(DecompositionMode.Additive);
        x13spec.getRegArimaSpecification().getRegression().setTradingDays(daysSpec);

        holidaySpec[0].setType(MovingHolidaySpec.Type.Easter);
        holidaySpec[0].setTest(RegressionTestSpec.None);
        holidaySpec[0].setW(8);
        
        x13spec.getRegArimaSpecification().getRegression().setMovingHolidays(holidaySpec);
        calcSeasonallyAdjustedTest(x13spec, strMessage);
        
        strMessage = "add";
        mode = DecompositionMode.Additive;
        x13spec = getX13Spec(mode, false);
        calcSeasonallyAdjustedTest(x13spec, strMessage);

        //Test for LogAdd decomposition Mode with Log Transformation
        strMessage = "LogAdd";
        mode = DecompositionMode.LogAdditive;
        x13spec = getX13Spec(mode, true);
        
        calcSeasonallyAdjustedTest(x13spec, strMessage);
        
        for (X13Specification xs : X13Specification.allSpecifications()) {
            if (!xs.equals(X13Specification.RSAX11)) {
                
                calcSeasonallyAdjustedTest(xs, xs.toString());
            }
        }
    }
    
    private void calcSeasonallyAdjustedTest(X13Specification x13spec, String strMessage) {
        strMessage = strMessage + "With new A1 ";
        SequentialProcessing<TsData> processing = X13ProcessingFactory.instance.generateProcessing(x13spec);
        CompositeResults comprest = processing.process(TSDATA_UNEMPLOYMENT_SEASONAL);
        
        MetaData meta = new MetaData();
        TsData tsd10_all = comprest.getData("d-tables.d10", TsData.class).update(comprest.getData("d-tables.d10a", TsData.class));
        TsData tsdA6_all = comprest.getData("a-tables.a6", TsData.class);
        TsData tsdA7_all=comprest.getData("a-tables.a7", TsData.class);;
        DecompositionMode dsd = comprest.get("final", DefaultSeriesDecomposition.class).getMode();

        // if (x13spec.getRegArimaSpecification().getTransform().getFunction() == DefaultTransformationType.Log) {
        if (dsd == DecompositionMode.Multiplicative || dsd == DecompositionMode.LogAdditive) {
           tsdA6_all=tsdA6_all.times(tsdA7_all);
            tsd10_all = tsd10_all.times(100);
            tsdA6_all = tsdA6_all.times(100);
        }else{
            tsdA6_all=tsdA6_all.plus(tsdA7_all);
        }
        TsData_MetaDataConverter.convertTsToMetaData(tsd10_all, meta, SavedTables.SEASONALFACTOR);
        TsData_MetaDataConverter.convertTsToMetaData(tsdA6_all, meta, SavedTables.CALENDARFACTOR);
        TsData tsdA1_all = comprest.getData("a-tables.a1", TsData.class).update(comprest.getData("a-tables.a1a", TsData.class));
    //    TsData_MetaDataConverter.convertTsToMetaData(tsdA1_all, meta, SavedTables.FORECAST);
        DecompositionMode mode = comprest.getData("mode", DecompositionMode.class);
        TsData tsdSavedSAS = calcSeasonallyAdjusted(meta, mode, tsdA1_all).getTsData();
        TsData tsd11 = comprest.getData("d-tables.d11", TsData.class);
        Assert.assertTrue(strMessage + " :D11", CompareTsData.compareTS(tsdSavedSAS.fittoDomain(tsd11.getDomain()), tsd11, 0.000000001));
        TsData tsd11a = comprest.getData("d-tables.d11a", TsData.class);
        Assert.assertTrue(strMessage + " :D11a", CompareTsData.compareTS(tsdSavedSAS.fittoDomain(tsd11a.getDomain()), tsd11a, .000000001));
    }
    
    private X13Specification getX13Spec(DecompositionMode mode, boolean isLog) {
        X13Specification x13Spec = X13Specification.RSA0;
        x13Spec.getRegArimaSpecification().getOutliers().clearTypes();
        if (isLog) {
            x13Spec.getRegArimaSpecification().getTransform().setFunction(DefaultTransformationType.Log);
        } else {
            x13Spec.getRegArimaSpecification().getTransform().setFunction(DefaultTransformationType.None);
        }
        
        x13Spec.getX11Specification().setMode(mode);
        return x13Spec;
    }
    
    private static final double[] UMEMPLOYMENT_SEASONAL_VALUES = {0.9180562, 1.0272757, 1.0668658, 0.9787514, 0.9313836, 1.0250405, 1.0650804, 0.9747613, 0.9418502, 1.0156112, 1.0654049, 0.9738503, 0.9504815, 1.010104, 1.0645658, 0.9699722, 0.9592689, 1.0082833, 1.0642167, 0.9640116, 0.965041, 1.0076595, 1.0648469, 0.9615725, 0.9678711, 10.0034218, 1.066394, 0.9605819, 0.9743569, 0.996285, 1.0674977, 0.9616944, 0.9799257, 0.9869897, 1.0695324, 0.9633976, 0.9840746, 0.9803236, 1.0720522, 0.9668027, 0.9761973, 0.9852996, 1.0730376, 0.9667407, 0.9710266, 0.9903273, 1.0730635, 0.967825, 0.9660222, 0.992547, 1.0727041, 0.9699537, 0.964653, 0.9927414, 1.072418, 0.9711013, 0.9627251, 0.9940281, 1.0721618, 0.9717765, 0.9603546, 0.9962908, 1.0715377, 0.9748881, 0.9546215, 0.9984903, 1.0715511, 0.9792769, 0.9477065, 1.000862, 1.0715326, 0.9832773, 0.942155, 1.003048, 1.0724468, 0.983362, 0.9407448, 1.0021712, 1.0738647, 0.9849541, 0.9379978, 1.002259, 1.0755799, 0.9857489, 0.9344911, 1.0034349, 1.0764464, 0.986686, 0.9328302, 1.0034421, 1.076927, 0.9878056, 0.9318523, 1.0025105, 1.077559, 0.9893451, 0.9308975, 1.000813, 1.0781244, 0.9907408, 0.9303614, 1.0008349, 1.0780044, 0.989879, 0.9316045, 1.0010713, 1.077419, 0.9894252, 0.9319111, 1.001701, 1.0762305, 0.9898486, 0.9325315, 1.0023491, 1.0752361, 0.9884643, 0.9347162, 1.0023267, 1.0739461, 0.9876701, 0.9378643, 1.0009766, 1.0735102, 0.9861014, 0.9406324, 0.999855, 1.0730103, 0.9865699, 0.9411618, 0.9989676, 1.0728322, 0.9869915, 0.9418439, 0.9982591, 1.072555, 0.9861752, 0.9438692, 0.998087, 1.0721105, 0.9846667, 0.9457213, 0.9979742, 1.0716681, 0.9834916, 0.9478989, 0.9970811, 1.0713196, 0.983102, 0.9502612, 0.9942578, 1.0710793, 0.985063, 0.9511854, 0.9914698, 1.0707433, 0.9874407, 0.9515542, 0.9900275, 1.070696, 0.9845132, 0.9548301, 0.9932408, 1.0691118, 0.977684, 0.9601211, 0.9968435, 1.0677422, 0.9696101, 0.9651861, 1.0007962, 1.065838, 0.9700529, 0.9625211, 0.9996584, 1.0667989, 0.9738917, 0.9600908, 0.996862, 1.0680945, 0.977864, 0.9575264, 0.9946873, 1.070673, 0.9772387, 0.9537045, 1.0004651, 1.0713684, 0.974299, 0.9493839, 1.0071895, 1.0722726, 0.9697757, 0.946103, 1.0144437, 1.0720051, 0.9700858, 0.9426046, 1.0120981, 1.0744092, 0.9738236, 0.939198, 1.0102382, 1.0757456, 0.9792007, 0.9355581, 1.0059027, 1.0780573, 0.9789557, 0.9381395, 1.0058429, 1.0776412, 0.9771881, 0.9388107, 1.0076234, 1.0778313, 0.9752792, 0.9391388, 1.0061087, 1.0754564, 0.9793699, 0.9439685, 1.0006578, 1.0737004, 0.9810835, 0.9492386, 0.994066, 1.0730588, 0.9855865, 0.950731, 0.9875736, 1.0734068, 0.9896219, 0.9502576, 0.9861444, 1.0712624, 0.9920906, 0.9516965, 0.9867609};
    public static final TsData TSDATA_UNEMPLOYMENT_SEASONAL = new TsData(TsFrequency.Monthly, 1991, 0, UMEMPLOYMENT_SEASONAL_VALUES, false);
    
}
