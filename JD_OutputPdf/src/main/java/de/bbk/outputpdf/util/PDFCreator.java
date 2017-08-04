///*
// * To change this license header, choose License Headers in Project Properties.
// * To change this template file, choose Tools | Templates
// * and open the template in the editor.
// */
//package de.bbk.outputpdf.util;
//import java.io.File;
//import java.io.FileNotFoundException;
//import java.io.FileOutputStream;
//import org.apache.batik.transcoder.Transcoder;
//import org.apache.batik.transcoder.TranscoderException;
//import org.apache.batik.transcoder.TranscoderInput;
//import org.apache.batik.transcoder.TranscoderOutput;
//
//import org.apache.fop.svg.PDFTranscoder;
///**
// *
// * @author Christiane Hofer
// */
//public class PDFCreator {
//
//    public PDFCreator() {
//    }
//    
//    public static void creatPDF(String htmlText, File file) throws FileNotFoundException, TranscoderException{
//         Transcoder transcoder = new PDFTranscoder();
//      //  TranscoderInput transcoderInput = new TranscoderInput(new FileInputStream(new File("/tmp/test.svg")));
//        TranscoderInput transcoderInput = new TranscoderInput(htmlText);
// 
//        TranscoderOutput transcoderOutput = new TranscoderOutput(new FileOutputStream(file));
//        transcoder.transcode(transcoderInput, transcoderOutput);
//    }
//    
//}
