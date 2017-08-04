/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.bbk.outputpdf;

import de.bbk.outputcustomized.html.HtmlCCA;
import de.bbk.outputcustomized.view.TablesPercentageChangeView;
import de.bbk.outputpdf.files.HTMLFiles;
import de.bbk.outputpdf.html.HTML2Div;
import de.bbk.outputpdf.html.HTMLBBKChartMain;
import de.bbk.outputpdf.html.HTMLBBKFooter;
import de.bbk.outputpdf.html.HTMLBBKPeriodogram;
import de.bbk.outputpdf.html.HTMLBBKTableD8A;
import de.bbk.outputpdf.html.HTMLBBKText1;
import de.bbk.outputpdf.html.HTMLBBkHeader;
import de.bbk.outputpdf.html.HTMLStyle;
import de.bbk.outputpdf.util.Pagebreak;
import ec.satoolkit.ISaSpecification;
import ec.satoolkit.x11.Mstatistics;
import ec.satoolkit.x11.X11Results;
import ec.tss.Ts;
import ec.tss.documents.DocumentManager;
import ec.tss.documents.TsDocument;
import ec.tss.html.HtmlStream;
import ec.tss.html.implementation.HtmlMstatistics;
import ec.tss.html.implementation.HtmlSingleTsData;
import ec.tss.sa.SaItem;
import ec.tss.sa.documents.SaDocument;
import ec.tss.sa.documents.X13Document;
import ec.tss.tsproviders.utils.MultiLineNameUtil;
import ec.tstoolkit.modelling.ComponentInformation;
import ec.tstoolkit.modelling.ComponentType;
import ec.tstoolkit.modelling.ModellingDictionary;
import ec.tstoolkit.timeseries.simplets.TsData;
import ec.tstoolkit.timeseries.simplets.TsDomain;
import java.io.IOException;
import java.io.StringWriter;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.JOptionPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Christieane Hofer
 */
public class Processing {

    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());
    private HTMLFiles htmlf;

    public void start(SaItem[] selection, String name) {
        htmlf = HTMLFiles.getInstance();
        htmlf.selectFolder();
        if (htmlf.isOutputfileSelected()) {
            startWithOutFileSelection(selection, name);
        } else {
            JOptionPane.showMessageDialog(null, "The html('s) are not generated, you haven't selected a folder. ");
        }

    }

    public void start(Map<String, List<SaItem>> map) {

        htmlf = HTMLFiles.getInstance();
        htmlf.selectFolder();
        if (htmlf.isOutputfileSelected()) {
            Set<String> keySet = map.keySet();
            keySet.stream().forEach((singleKey) -> {
                SaItem[] selection = (SaItem[]) map.get(singleKey).toArray();
                startWithOutFileSelection(selection, singleKey);
            });
        } else {
            JOptionPane.showMessageDialog(null, "The html('s) are not generated, you haven't selected a folder. ");
        }

    }

    private void startWithOutFileSelection(SaItem[] selection, String name) {
        Thread testThread = new Thread(new Processing.MyRun(selection, name), "Html" + name);
        testThread.start();
    }

    class MyRun implements Runnable {

        private final SaItem[] items;
        private final String saProcessingName;

        public MyRun(SaItem[] items, String saProcessingName) {
            this.items = items;
            this.saProcessingName = saProcessingName;
        }

        @Override
        public void run() {
            StringBuilder sbError = new StringBuilder();
            StringBuilder sbSuccussfull = new StringBuilder();

            for (SaItem item : items) {

                item.getTs().getName();// Name SAItem
                //   int index = cur.getCurrentProcessing().indexOf(selection[i]);
                SaDocument<ISaSpecification> doc = item.toDocument();
                TsDocument t = item.toDocument();
                String str = item.getName()
                        + "in Multi-doc " + this.saProcessingName;
                str = str.replace("\n", "-");
                if (t.getClass() == X13Document.class) {
                    X13Document x13doc = (X13Document) t;
                    X11Results x11Results = x13doc.getDecompositionPart();
                    //   CompositeResults results = doc.getResults();

                    TsDomain domCharMax5years;
                    Ts tsY;
                    tsY = DocumentManager.instance.getTs(x13doc, ModellingDictionary.Y);
                    TsDomain domTsY = tsY.getTsData().getDomain();
                    int int5Jahre = domTsY.getFrequency().intValue() * 5;
                    domCharMax5years = new TsDomain(domTsY.getEnd().minus(int5Jahre), int5Jahre);
                    domCharMax5years = domCharMax5years.intersection(domTsY);

                    HtmlStream stream;
                    StringWriter writer = new StringWriter();
                    try {
                        //Open the stream
                        stream = new HtmlStream(writer);
                        stream.open();
                        // String cssPath = "C:\\Daten\\style.css";
                        // stream.write("<link rel=\"stylesheet\" href=\"" + cssPath + "\">");
                        stream.write(HTMLStyle.getStyle());
                        //Einleitung Kopf
                        final HTMLBBkHeader headerbbk = new HTMLBBkHeader(saProcessingName, item.getRawName(), item.getTs().getRawName());
                        headerbbk.write(stream);
                        stream.newLine();

                        //Uerbersichts Chart mit y Seasonally and calendar adjusted von gespeichert und aktuelle
                        HTMLBBKChartMain chartMain = new HTMLBBKChartMain(x13doc, domCharMax5years);
                        final HTMLBBKText1 bBKText1 = new HTMLBBKText1(x13doc);

                        final HTML2Div hTML2Div = new HTML2Div(bBKText1, chartMain);
                        hTML2Div.write(stream);
                        stream.write("Irregular ??").newLine();
                        final HTMLBBKPeriodogram htmlBBKPeriodogram = new HTMLBBKPeriodogram(x13doc.getDecompositionPart().getSeriesDecomposition().getSeries(ComponentType.Irregular, ComponentInformation.Value));

                        htmlBBKPeriodogram.write(stream);

                        final HTMLBBKFooter bBKFooter = new HTMLBBKFooter(item.getTs());
                        bBKFooter.write(stream);
                        stream.newLine();
                        final Pagebreak p = new Pagebreak();
                        p.write(stream);

                        headerbbk.write(stream);

                        //soll durch das schöne Table aus dem Output ersetzt werden
                        final HTMLBBKTableD8A hTMLBBKTableD8B = new HTMLBBKTableD8A(x13doc, domCharMax5years);
                        hTMLBBKTableD8B.write(stream);
                        stream.newLine();

                        HtmlCCA htmlCCA = new HtmlCCA(MultiLineNameUtil.join(doc.getInput().getName()), x13doc);
                        htmlCCA.writeTextWithoutTitle(stream);

                        Mstatistics mstats = Mstatistics.computeFromX11(x11Results.getSeriesDecomposition().getMode(), x11Results.getInformation());
                        final HtmlMstatistics htmlMstatistics = new HtmlMstatistics(mstats);
                        htmlMstatistics.writeSummary(stream);

                        p.write(stream);

                        TablesPercentageChangeView tpcv = new TablesPercentageChangeView();
                        tpcv.set(x13doc);
                        Ts SeasonallyadjustedPercentageChange = tpcv.GetSeasonallyadjustedPercentageChange();

                        HtmlSingleTsData htmlSingleTsData = new HtmlSingleTsData(
                                onlyLast2years(SeasonallyadjustedPercentageChange.getTsData()), SeasonallyadjustedPercentageChange.getName());
                        htmlSingleTsData.write(stream);
                        stream.newLine();
                        htmlSingleTsData = new HtmlSingleTsData(onlyLast2years(tpcv.GetSavedSeasonallyAdjustedPercentageChange().getTsData()), tpcv.GetSavedSeasonallyAdjustedPercentageChange().getName());
                        htmlSingleTsData.write(stream);
                        stream.newLine();
//footer
                        bBKFooter.write(stream);

                        stream.close();

                        String output = writer.getBuffer().toString();

                        //    htmlf.createHTMLTempFiles(output);
                        htmlf.creatHTMLFile(output, this.saProcessingName, item.getName());

                        // hTMLBBKTableD8B.dispose();
                    } catch (IOException ex) {
                        LOGGER.error(ex.getMessage());
                    }

                    sbSuccussfull.append(str);
                    sbSuccussfull.append("\n");
                } else {

                    sbError.append(str);
                    sbError.append("\n");

                }
            }
            if (!"".equals(sbError.toString())) {
                String str = "These documents are not X13: \n";
                sbError.insert(0, str);
                JOptionPane.showMessageDialog(null, sbError.toString(), "This output is not available. ", JOptionPane.ERROR_MESSAGE);
            }

            if (!"".equals(sbSuccussfull.toString())) {

                JOptionPane.showMessageDialog(null, sbSuccussfull.toString(), "This output is available for:. ", JOptionPane.INFORMATION_MESSAGE);
            }
        }

    }

    /**
     *
     * @param tsData
     * @return a clone from tsData with has a max lenght from 2 years
     */
    private TsData onlyLast2years(TsData tsData) {
        TsData tsData2 = tsData.clone();
        int freq = tsData2.getFrequency().intValue();
        int drop = tsData2.getLength() - 2 * freq;
        if (drop > 0) {
            return tsData2.drop(drop, 0);
        } else {
            return tsData2;
        }

    }

}
