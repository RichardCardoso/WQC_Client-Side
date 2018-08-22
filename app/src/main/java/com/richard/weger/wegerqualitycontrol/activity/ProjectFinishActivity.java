package com.richard.weger.wegerqualitycontrol.activity;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.richard.weger.wegerqualitycontrol.R;
import com.richard.weger.wegerqualitycontrol.domain.Item;
import com.richard.weger.wegerqualitycontrol.domain.Project;
import com.richard.weger.wegerqualitycontrol.domain.Report;
import com.richard.weger.wegerqualitycontrol.util.ConfigurationsManager;
import com.richard.weger.wegerqualitycontrol.util.QrTextHandler;
import com.richard.weger.wegerqualitycontrol.util.StringHandler;
import com.richard.weger.wegerqualitycontrol.util.WQCDocumentHandler;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

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

import static com.richard.weger.wegerqualitycontrol.util.AppConstants.*;

public class ProjectFinishActivity extends Activity {

    Project project = null;
    Map<String, String> mapValues = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_project_finish);

        Button btn = findViewById(R.id.btnReportSubmit);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                reportSubmit();
            }
        });
        mapValues = (HashMap) getIntent().getSerializableExtra(MAP_VALUES_KEY);
    }

    private void reportSubmit(){
        Report report;
        Bundle b;
        EditText editClient, editResponsible, editComments; //, editCommission;
        Workbook workbook;
        WritableWorkbook finalWorkbook;
        WritableSheet writableSheet;
        WorkbookSettings ws;
        InputStream inputStream;
        OutputStream outputStream;
        String finalFileName;
        File file;

        editClient = findViewById(R.id.editClient);
        // editCommission = findViewById(R.id.tvComission);
        editResponsible = findViewById(R.id.editResponsible);
        editComments = findViewById(R.id.editReportComments);
        if(editClient.getText().toString().equals("") || editResponsible.getText().toString().equals("")){
            Toast.makeText(this, R.string.emptyFieldsError, Toast.LENGTH_LONG).show();
            return;
        }

        b = getIntent().getExtras();
        finalFileName = b.getString(CONTROL_CARD_REPORT_FILE_KEY);
        project = (Project) b.get(PROJECT_KEY);
        report = project.getReportList().get(CONTROL_CARD_REPORT_ID);
        report.setClient(editClient.getText().toString());
        // report.setCommission(editCommission.getText().toString());
        report.setResponsible(editResponsible.getText().toString());
        report.setComments(editComments.getText().toString());
        report.setDate(Calendar.getInstance().getTime());

        ws = new WorkbookSettings();
        ws.setEncoding("CP1250");

        inputStream = getResources().openRawResource(R.raw.kontrollkarte);
        file = new File(getExternalFilesDir(null), CONTROLCARDREPORT_FILENAME);
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

        file = new File(getExternalFilesDir(null), CONTROLCARDREPORT_FILENAME);
        try {
            workbook = Workbook.getWorkbook(file, ws);

            File fWork = new File(StringHandler.generateProjectFolderName(getExternalFilesDir(null),project),finalFileName);
            finalWorkbook = Workbook.createWorkbook(fWork, workbook);
            writableSheet = finalWorkbook.getSheet(0);
            writeData(writableSheet, report);
            try {
                finalWorkbook.write();
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
            }
            finalWorkbook.close();
            workbook.close();
        } catch (IOException | BiffException | WriteException e) {
            e.printStackTrace();
        }
        file.delete();

        String inputPath = project.getDrawingList().get(0).getOriginalFileLocalPath();
//                WQCDocumentHandler.getFilePath(CONSTRUCTION_PATH_KEY,
//                ConfigurationsManager.loadConfig(this),
//                mapValues);
        WQCDocumentHandler.bitmap2Pdf(inputPath,
                StringHandler.generateProjectFolderName(getExternalFilesDir(null), project)
                        + ConfigurationsManager.loadConfig(this).getDrawingCode() + "-" +
                        StringHandler.generateFileName(project, "pdf"), project,
                ConfigurationsManager.loadConfig(this).getDrawingCode(),
                project.getDrawingList().get(0).getHashPoints(), getResources());

        inputPath = project.getDrawingList().get(0).getDatasheet().getOriginalFileLocalPath();
//                WQCDocumentHandler.getFilePath(TECHNICAL_PATH_KEY,
//                ConfigurationsManager.loadConfig(this),
//                mapValues);
        WQCDocumentHandler.bitmap2Pdf(inputPath,
                StringHandler.generateProjectFolderName(getExternalFilesDir(null), project)
                        + ConfigurationsManager.loadConfig(this).getDatasheetCode() + "-" +
                        StringHandler.generateFileName(project, "pdf"), project,
                ConfigurationsManager.loadConfig(this).getDatasheetCode(),
                project.getDrawingList().get(0).getDatasheet().getHashPoints(), getResources());
    }

    private void writeData(WritableSheet writableSheet, Report report){
        int actualLine = CCRF_FIRSTLINE;
        WritableCellFormat cellFormat;
        WritableCellFormat cellFormat2;
        Label label;

        try {
            for(Item item : report.getItemList()) {
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
            cellFormat = new WritableCellFormat();
            cellFormat.setBorder(Border.BOTTOM, BorderLineStyle.DASHED);
            label = new Label(CCRF_CLIENTCOLUMN,
                    CCRF_CLIENTROW, report.getClient(), cellFormat);
            writableSheet.addCell(label);

            // Project number
            cellFormat = new WritableCellFormat();
            cellFormat.setBorder(Border.BOTTOM, BorderLineStyle.DASHED);
            label = new Label(CCRF_COMMISSIONCOLUMN,
                    CCRF_COMMISSIONROW, project.getNumber(), cellFormat);
            writableSheet.addCell(label);

            // Drawing number
            label = new Label(CCRF_DRAWINGCOLUMN,
                    CCRF_DRAWINGROW,
                    String.valueOf(report.getDrawing().getNumber()), cellFormat);
            writableSheet.addCell(label);

            // Part number
            label = new Label(CCRF_PARTCOLUMN,
                    CCRF_PARTROW,
                    String.valueOf(report.getPart().getNumber()), cellFormat);
            writableSheet.addCell(label);

            // Report comments
            cellFormat = new WritableCellFormat();
            label = new Label(CCRF_REPORTCOMMENTSCOLUMN,
                    CCRF_REPORTCOMMENTSROW, report.getComments(), cellFormat);
            writableSheet.addCell(label);

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
            cellFormat = new WritableCellFormat();
            cellFormat.setBorder(Border.BOTTOM, BorderLineStyle.DASHED);
            label = new Label(CCRF_REPORTRESPONSIBLECOLUMN,
                    CCRF_REPORTRESPONSIBLEROW, report.getResponsible(), cellFormat);
            writableSheet.addCell(label);

            label = new Label(CCRF_PARTCOLUMN,
                    CCRF_PARTROW,
                    String.valueOf(report.getPart().getNumber()), cellFormat);
            writableSheet.addCell(label);
        } catch (WriteException e){
            e.printStackTrace();
        }
    }
}
