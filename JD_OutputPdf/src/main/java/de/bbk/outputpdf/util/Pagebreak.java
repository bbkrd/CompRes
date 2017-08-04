/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.bbk.outputpdf.util;

import ec.tss.html.AbstractHtmlElement;
import ec.tss.html.HtmlStream;
import ec.tss.html.HtmlTag;
import ec.tss.html.IHtmlElement;
import java.io.IOException;

/**
 *
 * @author Christiane Hofer
 */
public class Pagebreak extends AbstractHtmlElement implements IHtmlElement{

    @Override
    public void write(HtmlStream stream) throws IOException {
       stream.write(HtmlTag.HEADER4, h4,"pagebreak");
    }
   
    
}
