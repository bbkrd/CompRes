package de.bbk.concurreport;

@lombok.Value
public class ReportMessages {

    public static ReportMessages EMPTY = new ReportMessages("", "");

    String successMessages;
    String errorMessages;
}
