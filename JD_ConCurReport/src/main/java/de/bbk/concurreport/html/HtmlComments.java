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

import de.bbk.concurreport.util.Pagebreak;
import ec.tss.html.AbstractHtmlElement;
import ec.tss.html.HtmlStream;
import ec.tss.sa.documents.X13Document;
import ec.tstoolkit.MetaData;
import java.io.IOException;

/**
 *
 * @author Christiane Hofer
 */
public class HtmlComments extends AbstractHtmlElement {

    private final MetaData metaData;
    private final HTMLBBkHeader headerbbk;

    public HtmlComments(X13Document doc, HTMLBBkHeader headerbbk) {
        this.headerbbk = headerbbk;
        this.metaData = doc.getMetaData();

    }

    @Override
    public void write(HtmlStream stream) throws IOException {

        if (metaData == null || !metaData.containsKey("comment")) {

        } else {
            Pagebreak p = new Pagebreak();
            p.write(stream);
            headerbbk.write(stream);

            stream.write("Comment:").newLine();
            String comment = metaData.get("comment");

            stream.write(comment.replaceAll("\n", "<br/>")).newLine();
        }

    }

}
