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
            = "<style>body{"
            +"font: normal 12px Arial,sans-serif;\n"
            + "text-align: left;\n"
            + "width:1000px; \n"
            + "max-width:1000px;}\n"
            + "hr { background: red; height: 2px; border: none } \n"
            + "h1 {	font-size:100%;\n"
            + "	margin-top: 0px;\n"
            + "        margin-bottom: -5px;\n"
            + "        line-height: 1.1;}\n"
            + "h2 {font-size:100%;\n"
            + "	line-height: 1.0;}\n"
            + "\n"
            + "h3 {	font-size:100%;\n"
            + "	line-height: 1.0;\n"
            + "	text-decoration:underline;}\n"
            + "h4 {font-size:100%;\n"
            + "	line-height: 1.0;\n"
            + "	page-break-after:always;\n"
            + "        color:white}\n"
            + "\n"
            + "table,\n"
            + " tr,\n"
            + " td{\n"
            + "    	text-align:center;\n"
            + "	border-collapse: collapse;\n"
            + "    	padding:4px;\n"
            + "	font-size: 100%;\n"
            + "}\n"
            + "\n"
            + "#div1left\n"
            + "{ 	width:450px;\n"
            + "   	max-width:450px;\n"
            + "	vertical-align:top;\n"
            + "   	display: inline-block;}\n"
            + "\n"
            + "#div2right\n"
            + "{ 	width:450px; \n"
            + "   	max-width:450px;\n"
            + "	vertical-align:top;   	\n"
            + "	display: inline-block;}\n"
            + "\n"
            +"</style>";
}
