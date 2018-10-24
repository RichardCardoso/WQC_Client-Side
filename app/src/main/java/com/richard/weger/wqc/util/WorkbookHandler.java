package com.richard.weger.wqc.util;

import android.content.res.Resources;

import com.richard.weger.wqc.R;
import com.richard.weger.wqc.domain.Item;
import com.richard.weger.wqc.domain.ItemReport;
import com.richard.weger.wqc.domain.Project;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;

import jxl.Workbook;
import jxl.WorkbookSettings;
import jxl.format.Alignment;
import jxl.format.Border;
import jxl.format.BorderLineStyle;
import jxl.format.VerticalAlignment;
import jxl.read.biff.BiffException;
import jxl.write.Label;
import jxl.write.WritableCellFormat;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;

import static com.richard.weger.wqc.util.AppConstants.CCRF_COMMENTS;
import static com.richard.weger.wqc.util.AppConstants.CCRF_COMMISSIONCOLUMN;
import static com.richard.weger.wqc.util.AppConstants.CCRF_COMMISSIONROW;
import static com.richard.weger.wqc.util.AppConstants.CCRF_DRAWINGCOLUMN;
import static com.richard.weger.wqc.util.AppConstants.CCRF_DRAWINGROW;
import static com.richard.weger.wqc.util.AppConstants.CCRF_FIRSTLINE;
import static com.richard.weger.wqc.util.AppConstants.CCRF_IO;
import static com.richard.weger.wqc.util.AppConstants.CCRF_JUMPONEMORE;
import static com.richard.weger.wqc.util.AppConstants.CCRF_NA;
import static com.richard.weger.wqc.util.AppConstants.CCRF_NIO;
import static com.richard.weger.wqc.util.AppConstants.CCRF_PARTCOLUMN;
import static com.richard.weger.wqc.util.AppConstants.CCRF_PARTROW;
import static com.richard.weger.wqc.util.AppConstants.CCRF_REPORTDATECOLUMN;
import static com.richard.weger.wqc.util.AppConstants.CCRF_REPORTDATEROW;
import static com.richard.weger.wqc.util.AppConstants.CONTROLCARDREPORT_FILENAME;
import static com.richard.weger.wqc.util.AppConstants.CONTROL_CARD_REPORT_ID;
import static com.richard.weger.wqc.util.AppConstants.ITEM_APROVED_KEY;
import static com.richard.weger.wqc.util.AppConstants.ITEM_NOT_APLICABLE_KEY;
import static com.richard.weger.wqc.util.AppConstants.ITEM_NOT_APROVED_KEY;

public abstract class WorkbookHandler {
    public static String handleWorkbook(Resources res, File externalFilesDir, Project project, String finalFileName){
        Workbook workbook;
        WritableWorkbook finalWorkbook;
        WritableSheet writableSheet;
        WorkbookSettings ws;
        InputStream inputStream;
        OutputStream outputStream;
        File file;
        ItemReport report;

        report = (ItemReport) project.getDrawingRefs().get(0).getReports().get(CONTROL_CARD_REPORT_ID);

        ws = new WorkbookSettings();
        ws.setEncoding("CP1250");

        inputStream = res.openRawResource(R.raw.kontrollkarte);
        file = new File(externalFilesDir, CONTROLCARDREPORT_FILENAME);
        outputStream = null;
        try {
            outputStream = new FileOutputStream(file);
            byte[] buffer = new byte[1024];
            int read;
            while((read = inputStream.read(buffer)) != -1){
                outputStream.write(buffer, 0, read);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        finally {
            if (inputStream != null){
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if(outputStream != null){
                try {
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        file = new File(externalFilesDir, CONTROLCARDREPORT_FILENAME);
        try {
            workbook = Workbook.getWorkbook(file, ws);

            File fWork = new File(StringHandler.generateProjectFolderName(externalFilesDir, project),finalFileName);
            finalWorkbook = Workbook.createWorkbook(fWork, workbook);
            writableSheet = finalWorkbook.getSheet(0);
            writeData(writableSheet, report, project);
            try {
                finalWorkbook.write();
            } catch (Exception e) {
                e.printStackTrace();
                return e.getMessage();
            }
            finalWorkbook.close();
            workbook.close();
        } catch (IOException | BiffException | WriteException e) {
            e.printStackTrace();
            return e.getMessage();
        }
        file.delete();
        return null;
    }

    private static void writeData(WritableSheet writableSheet, ItemReport report, Project project){
        int actualLine = CCRF_FIRSTLINE;
        WritableCellFormat cellFormat;
        WritableCellFormat cellFormat2;
        Label label;

        try {
            for(Item item : report.getItems()) {
                int col = 0;

                cellFormat = new WritableCellFormat();
                cellFormat2 = new WritableCellFormat();

                switch (item.getStatus()) {
                    case ITEM_APROVED_KEY:
                        col = CCRF_IO;
                        break;
                    case ITEM_NOT_APROVED_KEY:
                        col = CCRF_NIO;
                        break;
                    case ITEM_NOT_APLICABLE_KEY:
                        col = CCRF_NA;
                        break;
                }
                cellFormat.setBorder(Border.ALL, BorderLineStyle.THIN);
                cellFormat.setAlignment(Alignment.LEFT);
                cellFormat.setVerticalAlignment(VerticalAlignment.CENTRE);

                label = new Label(CCRF_COMMENTS, actualLine, item.getComments(), cellFormat);
                writableSheet.addCell(label);

                cellFormat2.setBorder(Border.ALL, BorderLineStyle.THIN);
                cellFormat2.setAlignment(Alignment.CENTRE);
                cellFormat2.setVerticalAlignment(VerticalAlignment.CENTRE);
                label = new Label(col, actualLine, "x", cellFormat2);
                writableSheet.addCell(label);

                for(Integer i : CCRF_JUMPONEMORE){
                    if(actualLine == i)
                        actualLine++;
                }
                actualLine++;
            }
            // Client name
//            cellFormat = new WritableCellFormat();
//            cellFormat.setBorder(Border.BOTTOM, BorderLineStyle.DASHED);
//            label = new Label(CCRF_CLIENTCOLUMN,
//                    CCRF_CLIENTROW, report.getClient(), cellFormat);
//            writableSheet.addCell(label);

            // Project number
            cellFormat = new WritableCellFormat();
            cellFormat.setBorder(Border.BOTTOM, BorderLineStyle.DASHED);
            label = new Label(CCRF_COMMISSIONCOLUMN,
                    CCRF_COMMISSIONROW, project.getReference(), cellFormat);
            writableSheet.addCell(label);

            // Drawing number
            label = new Label(CCRF_DRAWINGCOLUMN,
                    CCRF_DRAWINGROW,
                    String.valueOf(project.getDrawingRefs().get(0).getNumber()), cellFormat);
            writableSheet.addCell(label);

            // Part number
            label = new Label(CCRF_PARTCOLUMN,
                    CCRF_PARTROW,
                    String.valueOf(project.getDrawingRefs().get(0).getParts().get(0).getNumber()), cellFormat);
            writableSheet.addCell(label);

            // Report comments
//            cellFormat = new WritableCellFormat();
//            label = new Label(CCRF_REPORTCOMMENTSCOLUMN,
//                    CCRF_REPORTCOMMENTSROW, report.getComments(), cellFormat);
//            writableSheet.addCell(label);

            // Report date
            cellFormat = new WritableCellFormat();
            cellFormat.setBorder(Border.BOTTOM, BorderLineStyle.DASHED);
            //String sDate = DateFormat.getDateInstance().format(report.getDate());
            SimpleDateFormat sfd = new SimpleDateFormat("dd/MM/yyyy");
            String sDate = sfd.format(report.getDate());
            label = new Label(CCRF_REPORTDATECOLUMN,
                    CCRF_REPORTDATEROW, sDate, cellFormat);
            writableSheet.addCell(label);

            // Report responsible
//            cellFormat = new WritableCellFormat();
//            cellFormat.setBorder(Border.BOTTOM, BorderLineStyle.DASHED);
//            label = new Label(CCRF_REPORTRESPONSIBLECOLUMN,
//                    CCRF_REPORTRESPONSIBLEROW, report.getResponsible(), cellFormat);
//            writableSheet.addCell(label);

            label = new Label(CCRF_PARTCOLUMN,
                    CCRF_PARTROW,
                    String.valueOf(project.getDrawingRefs().get(0).getParts().get(0).getNumber()), cellFormat);
            writableSheet.addCell(label);
        } catch (WriteException e){
            e.printStackTrace();
        }
    }
}
