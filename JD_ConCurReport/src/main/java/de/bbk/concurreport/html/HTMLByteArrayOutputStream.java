/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.bbk.concurreport.html;

import ec.tss.html.AbstractHtmlElement;
import ec.tss.html.HtmlStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 *
 * @author Christiane Hofer
 */
public class HTMLByteArrayOutputStream extends AbstractHtmlElement {

    private final ByteArrayOutputStream os;

    public HTMLByteArrayOutputStream(ByteArrayOutputStream os) {
        this.os = os;
    }

    @Override
    public void write(HtmlStream stream) throws IOException {
        stream.write(os.toString());
    }

}
