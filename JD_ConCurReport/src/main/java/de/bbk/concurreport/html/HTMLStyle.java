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

/**
 *
 * @author Christiane Hofer
 */
public class HTMLStyle {

    public static final String STYLE
            = "<style>\n"
            + "	body{\n"
            + "         font: normal 12px Arial,sans-serif;\n"
            + "         text-align: left;\n"
            + "         width:1000px;\n"
            + "         max-width:1000px;}\n"
            + "	h1{\n"
            + "         font-size:100%;\n"
            + "         margin-top: 0px;\n"
            + "         margin-bottom: -5px;\n"
            + "         line-height: 1.1;}\n"
            + "	h2{\n"
            + "		font-size:100%;\n"
            + "		line-height: 1.0;}\n"
            + "	h3{\n"
            + "		font-size:100%;\n"
            + "		line-height: 1.0;\n"
            + "		text-decoration:underline;}\n"
            + "	h4{\n"
            + "		font-size:10%;\n"
            + "		line-height: 1.0;\n"
            + "		page-break-after:always;\n"
            + "		color:white;}\n"
            + "	table{\n"
            + "		width:100%;\n"
            + "		font-size:100%;\n"
            + "		table-layout:auto;\n"
            + "		border-collapse:collapse;}\n"
            + "	th, td{\n"
            + "		padding:3px 5px;\n"
            + "		border:.1px solid #000;\n"
            + "		vertical-align:top;}\n"
            + "	td{\n"
            + "		text-align:right;}\n"
            + "	#div1left{\n"
            + "		width:450px;\n"
            + "		max-width:450px;\n"
            + "		vertical-align:top;\n"
            + "		display: inline-block;}\n"
            + "	#div2right{\n"
            + "		width:450px;\n"
            + "		max-width:450px;\n"
            + "		vertical-align:top;\n"
            + "		display: inline-block;}\n"
            + "	tr:hover{\n"
            + "		background-color: #f5f5f5;}\n"
            + "	@media print {\n"
            + "		body {-webkit-print-color-adjust: exact;}}\n"
            + "</style>";
}
