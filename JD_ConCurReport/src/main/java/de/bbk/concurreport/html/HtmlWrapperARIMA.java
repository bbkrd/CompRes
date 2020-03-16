/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.bbk.concurreport.html;

import ec.tss.html.AbstractHtmlElement;
import ec.tss.html.HtmlStream;
import ec.tss.html.HtmlTag;
import ec.tss.html.implementation.HtmlRegArima;
import ec.tstoolkit.modelling.arima.PreprocessingModel;
import java.io.IOException;

/**
 *
 * @author s4504tw
 */
public class HtmlWrapperARIMA extends AbstractHtmlElement {

    private final PreprocessingModel preprocessingModel;

    public HtmlWrapperARIMA(PreprocessingModel preprocessingModel) {
        this.preprocessingModel = preprocessingModel;
    }

    @Override
    public void write(HtmlStream stream) throws IOException {
        HtmlRegArima htmlRegArima = new HtmlRegArima(preprocessingModel, true);

        stream.write(HtmlTag.HEADER2, "Arima model").newLine();
        htmlRegArima.writeArima(stream);
    }

}
