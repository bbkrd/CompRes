/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.bbk.outputpdf.html;

import ec.tss.html.AbstractHtmlElement;
import ec.tss.html.HtmlStream;
import ec.tss.html.IHtmlElement;
import java.io.IOException;

/**
 *
 * @author Christiane Hofer
 */
public class HTML2Div extends AbstractHtmlElement implements IHtmlElement {

    private final AbstractHtmlElement leftHtmlElement, rightHtmlElement;

    public HTML2Div(AbstractHtmlElement leftHtmlElement, AbstractHtmlElement rightHtmlElement) {
        this.leftHtmlElement = leftHtmlElement;
        this.rightHtmlElement = rightHtmlElement;
    }

    @Override
    public void write(HtmlStream stream) throws IOException {
        stream.write("<div id=\"div1left\">");
        this.leftHtmlElement.write(stream);
        stream.write("</div>");

        stream.write("<div id=\"div2right\">");
        this.rightHtmlElement.write(stream);
        stream.write("</div>");
    }

}
