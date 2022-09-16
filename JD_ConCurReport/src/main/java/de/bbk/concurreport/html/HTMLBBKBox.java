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
package de.bbk.concurreport.html;

import ec.tss.html.HtmlStream;
import ec.tss.html.IHtmlElement;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Christiane Hofer
 */
public class HTMLBBKBox implements IHtmlElement {

    private final List<IHtmlElement> htmlElements;

    public HTMLBBKBox() {
        htmlElements = new ArrayList<>();
    }

    public boolean add(IHtmlElement htmlElement) {
        return htmlElements.add(htmlElement);
    }

    @Override
    public void write(HtmlStream stream) throws IOException {
        for (IHtmlElement htmlElement : htmlElements) {
            htmlElement.write(stream);
        }
    }

}
