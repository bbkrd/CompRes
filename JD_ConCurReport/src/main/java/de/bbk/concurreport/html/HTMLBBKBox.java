/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.bbk.concurreport.html;

import ec.tss.html.AbstractHtmlElement;
import ec.tss.html.HtmlStream;
import ec.tss.html.IHtmlElement;
import java.io.IOException;

/**
 *
 * @author Christiane Hofer
 */
public class HTMLBBKBox extends AbstractHtmlElement implements IHtmlElement {

    private final AbstractHtmlElement[] htmlElements;

    public HTMLBBKBox(AbstractHtmlElement[] htmlElements) {
        this.htmlElements=htmlElements;
    }

    @Override
    public void write(HtmlStream stream) throws IOException {
        for (AbstractHtmlElement htmlElement : htmlElements) {
            htmlElement.write(stream);
        }
    }

}
