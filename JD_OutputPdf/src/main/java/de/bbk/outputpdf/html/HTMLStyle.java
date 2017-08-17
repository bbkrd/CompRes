/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.bbk.outputpdf.html;

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
