/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.bbk.concur.html;

import de.bbk.autoconcur.AutoConCur;
import de.bbk.autoconcur.Decision;
import de.bbk.autoconcur.DecisionBean;
import ec.tss.html.AbstractHtmlElement;
import ec.tss.html.CssProperty;
import ec.tss.html.CssStyle;
import ec.tss.html.HtmlStream;
import ec.tss.html.HtmlTag;
import ec.tss.sa.documents.SaDocument;
import ec.tstoolkit.MetaData;
import java.io.IOException;
import java.io.StringWriter;
import org.openide.util.Exceptions;

/**
 *
 * @author Jan Gerhardt
 */
public class HtmlAutomised extends AbstractHtmlElement {

    private final DecisionBean bean;
    private final MetaData meta;
    private final static CssStyle STYLEERRORCHANGE = new CssStyle();
    private final static CssStyle STYLEUPDATE = new CssStyle();
    private final static CssStyle STYLEKEEP = new CssStyle();
    private final static CssStyle STYLEMANUAL = new CssStyle();

    static {
        STYLEERRORCHANGE.add(CssProperty.BACKGROUND_COLOR, "RED");
        STYLEERRORCHANGE.add(CssProperty.COLOR, "WHITE");
    }

    static {
        STYLEUPDATE.add(CssProperty.BACKGROUND_COLOR, "YELLOW");
        STYLEUPDATE.add(CssProperty.COLOR, "BLACK");
    }

    static {
        STYLEKEEP.add(CssProperty.BACKGROUND_COLOR, "GREEN");
        STYLEKEEP.add(CssProperty.COLOR, "WHITE");
    }

    static {
        STYLEMANUAL.add(CssProperty.BACKGROUND_COLOR, "BLACK");
        STYLEMANUAL.add(CssProperty.COLOR, "WHITE");
    }

    public HtmlAutomised(String title, SaDocument doc) {
        this.meta = doc.getMetaData();
        this.bean = AutoConCur.decision(title, doc);
    }

    public String writeText() {
        StringWriter sbuilder = new StringWriter();
        HtmlStream stream = new HtmlStream(sbuilder);

        try {
            stream.open();
            write(stream);
            stream.close();
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }

        return sbuilder.toString();
    }

    @Override
    public void write(HtmlStream stream) throws IOException {
        if (bean != null) {
            if (bean.isManual()) {
                stream.write(HtmlTag.IMPORTANT_TEXT, STYLEMANUAL, "Recommendation: " + bean.getDecision().toString() + " ");
            } else {
                switch (bean.getDecision()) {
                    case UNKNOWN:
                        stream.write(HtmlTag.IMPORTANT_TEXT, STYLEERRORCHANGE, bean.getErrortext()).newLine();
                        return;
                    case CHECK:
                        stream.write(HtmlTag.IMPORTANT_TEXT, STYLEERRORCHANGE, "Recommendation: " + Decision.CHECK.toString() + " ");
                        break;
                    case UPDATE:
                        stream.write(HtmlTag.IMPORTANT_TEXT, STYLEUPDATE, "Recommendation: " + Decision.UPDATE.toString() + " ");
                        break;
                    case KEEP:
                        stream.write(HtmlTag.IMPORTANT_TEXT, STYLEKEEP, "Recommendation: " + Decision.KEEP.toString() + " ");
                        break;
                }
            }
            stream.write("  |   ");
            writeMeta(stream);
//            if (bean.isDevelopment()) {
//                stream.write(HtmlTag.EMPHASIZED_TEXT, "Large movement detected. ");
//            }
            if (bean.isExtremevalue()) {
                stream.write(HtmlTag.EMPHASIZED_TEXT, "Extreme value detected.");
            }
        }
    }

    private void writeMeta(HtmlStream stream) throws IOException {
        if (!MetaData.isNullOrEmpty(meta)) {
            stream.write(writeMeta()).newLine();
        }
    }

    private String writeMeta() {
        StringBuilder sbuilder = new StringBuilder()
                .append("check sign=")
                .append(bean.isCheckSign())
                .append("  |  ")
                .append("manual Check=")
                .append(bean.isManual())
                .append("  |  ")
                .append("nSD=")
                .append(bean.getNSD())
                .append("  |  ")
                .append("nD8=")
                .append(bean.getND8())//.append((meta.get(AutoConCur.N) != null) ? meta.get(AutoConCur.N) : AutoConCur.NDEFAULT)
                .append("  |  ")
                .append("nGrowth=")
                .append(bean.getNGrowth())//.append((meta.get(AutoConCur.M) != null) ? meta.get(AutoConCur.M) : AutoConCur.MDEFAULT)
                .append("  |  ")
                .append("tolD8=")
                .append(bean.getTolD8())
                .append("  |  ")
                .append("tolGrowth=")
                .append(bean.getToleranceGrowth())
                .append("  |  ")
                .append("Trim=")
                .append(bean.getTrim());
        return sbuilder.toString();
    }
}
