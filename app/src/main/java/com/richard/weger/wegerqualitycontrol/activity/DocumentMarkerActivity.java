package com.richard.weger.wegerqualitycontrol.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import com.richard.weger.wegerqualitycontrol.R;
import com.richard.weger.wegerqualitycontrol.domain.WQCDocumentMark;
import com.richard.weger.wegerqualitycontrol.util.PdfHandler;
import com.richard.weger.wegerqualitycontrol.util.TouchImageView;
import com.richard.weger.wegerqualitycontrol.util.WQCDocumentHandler;
import com.richard.weger.wegerqualitycontrol.util.WQCPointF;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.richard.weger.wegerqualitycontrol.util.AppConstants.*;

public class DocumentMarkerActivity extends Activity implements TouchImageView.ChangeListener{

    Bitmap originalBitmap = null,
            currentBitmap = null;
    Map<Integer, List<WQCDocumentMark>> documentMarks = null;
    int currentPage = 0,
        pageCount = 0;
    TouchImageView imageView = null;
    String filePath, documentKey;
    int mode = 0,
    markType = 0;
    // mode 0 = zoom / pan
    // mode 1 = add mark

    @Override
    public void onBackPressed(){

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_document_marker);

        Intent intent = getIntent();
        documentKey = intent.getStringExtra(DOCUMENT_TYPE_KEY);
        filePath = intent.getStringExtra(FILE_PATH_KEY);
        if(documentKey == null || filePath == null){
            setResult(RESULT_CANCELED);
            finish();
        }
        documentMarks = (HashMap) intent.getSerializableExtra(DOCUMENT_HASH_POINTS_KEY);

        imageView = findViewById(R.id.ivDocument);
        imageView.setMaxZoom(12f);
        imageView.setChangeListener(this);
        imageView.setTag(mode);

        pageCount = PdfHandler.getPageCount(filePath);
        if(pageCount == 0){
            // Toast.makeText(this, R.string.dataRecoverError, Toast.LENGTH_LONG).show();
            setResult(RESULT_CANCELED);
            finish();
        }
        originalBitmap = WQCDocumentHandler.pageLoad(currentPage, filePath, getResources());
        if(originalBitmap != null)
            currentBitmap = WQCDocumentHandler.bitmapCopy(originalBitmap);
        else {
            Toast.makeText(this, R.string.unknownErrorMessage, Toast.LENGTH_LONG).show();
            cancel();
        }

        init();
        setListeners();
        updatePointsDrawing();
    }

    private void init(){
        if(documentMarks == null || documentMarks.size() == 0 || documentMarks.get(0) == null) {
            fillPointsMap();
        }
        fillSpinnerValues();
        updateButtonState();
    }

    private void setListeners(){
        Button btn = findViewById(R.id.btnNext);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                nextPage();
            }
        });

        btn = findViewById(R.id.btnPrevious);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                previousPage();
            }
        });

        btn = findViewById(R.id.btnAddMark);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggleMarkAdd();
            }
        });

        btn = findViewById(R.id.btnUndo);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                undo();
            }
        });

        btn = findViewById(R.id.btnSave);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                save();
            }
        });

        btn = findViewById(R.id.btnCancel);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cancel();
            }
        });

        Spinner spinner = findViewById(R.id.SpinnerMarkType);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String item = (String) parent.getItemAtPosition(position);
                markTypeChanged(item);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                markTypeChanged(String.valueOf(0));
            }
        });
    }

    private void markTypeChanged(String item){
        if(item.equals(getResources().getString(R.string.okTag))){
            markType = 0;
        } else if (item.equals(getResources().getString(R.string.elTag))){
            markType = 1;
        } else if (item.equals(getResources().getString(R.string.magTag))){
            markType = 2;
        }
    }

    private void undo(){
        List<WQCDocumentMark> markList = documentMarks.get(currentPage);
        if(markList != null && markList.size() > 0){
            markList.remove(markList.size() - 1);
            updatePointsDrawing();
            updateButtonState();
            toggleMarkAdd(false);
        }
    }

    private void fillPointsMap(){
        for(int i = 0; i < pageCount; i++){
            documentMarks.put(currentPage, new ArrayList<WQCDocumentMark>());
        }
    }

    private void fillSpinnerValues(){
        Spinner spinner;
        ArrayAdapter adapter;

        String[] array_spinner = new String[3];
        array_spinner[0] = getResources().getString(R.string.okTag);
        array_spinner[1] = getResources().getString(R.string.elTag);
        array_spinner[2] = getResources().getString(R.string.magTag);

        spinner = findViewById(R.id.SpinnerMarkType);
        adapter = new ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item,
                array_spinner);
        spinner.setAdapter(adapter);
    }

    private void toggleMarkAdd(){
        Button btn = findViewById(R.id.btnAddMark);
        if(btn.getText().equals(getResources().getString(R.string.btnAddMark))) {
            btn.setText(R.string.btnAddAltText);
            mode = 1;
        }
        else{
            btn.setText(R.string.btnAddMark);
            mode = 0;
        }
        imageView.setTag(mode);
    }

    private void toggleMarkAdd(boolean status){
        Button btn = findViewById(R.id.btnAddMark);
        if(status) {
            btn.setText(R.string.btnAddAltText);
            mode = 1;
        }
        else{
            btn.setText(R.string.btnAddMark);
            mode = 0;
        }
        imageView.setTag(mode);
    }

    private void save(){
        Intent intent = new Intent();
        Bundle b = new Bundle();
        b.putSerializable(DOCUMENT_HASH_POINTS_KEY, (HashMap) documentMarks);
        b.putString(DOCUMENT_TYPE_KEY, documentKey);
        intent.putExtras(b);
        setResult(RESULT_OK, intent);
        finish();
    }

    private void cancel(){
        setResult(RESULT_CANCELED);
        finish();
    }

    private void nextPage(){
        if(currentPage < pageCount - 1) {
            originalBitmap = WQCDocumentHandler.pageLoad(++currentPage, filePath, getResources());
            currentBitmap = WQCDocumentHandler.bitmapCopy(originalBitmap);
            updatePointsDrawing();
        }
        updateButtonState();
    }

    private void previousPage(){
        if(currentPage > 0) {
            originalBitmap = WQCDocumentHandler.pageLoad(--currentPage, filePath, getResources());
            if(originalBitmap != null){
                updateImageView(currentBitmap);
                currentBitmap = WQCDocumentHandler.bitmapCopy(originalBitmap);
            }
            updatePointsDrawing();
        }
        updateButtonState();
    }

    private void updateButtonState(){
        findViewById(R.id.btnNext).setEnabled(!(currentPage == pageCount - 1));
        findViewById(R.id.btnPrevious).setEnabled(!(currentPage == 0));
        findViewById(R.id.btnUndo).setEnabled(documentMarks != null &&
                documentMarks.get(currentPage) != null &&
                documentMarks.get(currentPage).size() > 0);
    }

    private void updateImageView(Bitmap bitmap){
        imageView.setImageBitmap(bitmap);
    }

    private void addMark(float[] touchPoint){
        List<WQCDocumentMark> markList = documentMarks.get(currentPage);
        if(markList == null){
            documentMarks.put(currentPage, new ArrayList<WQCDocumentMark>());
            markList = documentMarks.get(currentPage);
        }
        markList.add(new WQCDocumentMark(new WQCPointF(touchPoint[0], touchPoint[1]), markType));
        updatePointsDrawing();
        toggleMarkAdd(false);
        updateButtonState();
    }

    private void updatePointsDrawing(){
        List<WQCDocumentMark> markList = documentMarks.get(currentPage);
        if(markList != null) {
            currentBitmap = WQCDocumentHandler.updatePointsDrawing(markList, originalBitmap, getResources());
            updateImageView(currentBitmap);
        }
        else{
            updateImageView(originalBitmap);
        }
    }

    @Override
    public void onChangeHappened(float[] touchPoint) {
        addMark(touchPoint);
    }
}
