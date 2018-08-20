package com.richard.weger.wegerqualitycontrol.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.PointF;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.richard.weger.wegerqualitycontrol.R;
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
    Map<Integer, List<WQCPointF>> hashPoints = null;
    int currentPage = 0,
        pageCount = 0;
    TouchImageView imageView = null;
    String filePath, documentKey;
    int mode = 0;
    // mode 0 = default
    // mode 1 = add mark

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
        hashPoints = (HashMap) intent.getSerializableExtra(DOCUMENT_HASH_POINTS_KEY);

        imageView = findViewById(R.id.ivDocument);
        imageView.setMaxZoom(12f);
        imageView.setChangeListener(this);
        imageView.setTag(mode);

        pageCount = PdfHandler.getPageCount(filePath);
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
        if(hashPoints == null || hashPoints.size() == 0 || hashPoints.get(0) == null) {
            fillPointsMap();
        }
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
    }

    private void undo(){
        List<WQCPointF> pointFList = hashPoints.get(currentPage);
        if(pointFList != null && pointFList.size() > 0){
            pointFList.remove(pointFList.size() - 1);
            updatePointsDrawing();
            updateButtonState();
        }
    }

    private void fillPointsMap(){
        for(int i = 0; i < pageCount; i++){
            hashPoints.put(currentPage, new ArrayList<WQCPointF>());
        }
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
        b.putSerializable(DOCUMENT_HASH_POINTS_KEY, (HashMap) hashPoints);
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
            originalBitmap = WQCDocumentHandler.pageLoad(currentPage++, filePath, getResources());
            currentBitmap = WQCDocumentHandler.bitmapCopy(originalBitmap);
            updatePointsDrawing();
        }
        updateButtonState();
    }

    private void previousPage(){
        if(currentPage > 0) {
            originalBitmap = WQCDocumentHandler.pageLoad(currentPage--, filePath, getResources());
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
        findViewById(R.id.btnUndo).setEnabled(hashPoints != null &&
                hashPoints.get(currentPage) != null &&
                hashPoints.get(currentPage).size() > 0);
    }

    private void updateImageView(Bitmap bitmap){
        imageView.setImageBitmap(bitmap);
    }

    /*
    private void bitmapCopy(){
        currentBitmap = originalBitmap.copy(originalBitmap.getConfig(), true);
    }

    private void drawMark(float[] touchPoint, Canvas canvas, Paint paint){
        int radius = 18;
        Paint circlePaint = new Paint();
        circlePaint.setColor(Color.RED);
        circlePaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_OVER)); // Text over picture
        canvas.drawCircle(touchPoint[0] + radius / 2, touchPoint[1] - radius / 2 + 4, radius, circlePaint);
        canvas.drawText(getResources().getString(R.string.okTag), touchPoint[0], touchPoint[1], paint);
    }
    */

    private void addPoint(float[] touchPoint){
        List<WQCPointF> lstPoints = hashPoints.get(currentPage);
        lstPoints.add(new WQCPointF(touchPoint[0], touchPoint[1]));
        updatePointsDrawing();
        toggleMarkAdd(false);
    }

    private void updatePointsDrawing(){
        List<WQCPointF> lstPoints = hashPoints.get(currentPage);
        if(lstPoints != null) {
            currentBitmap = WQCDocumentHandler.updatePointsDrawing(lstPoints, originalBitmap, getResources());
            updateImageView(currentBitmap);
            /*
            Canvas canvas;

            bitmapCopy();
            canvas = new Canvas(currentBitmap);

            Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
            paint.setColor(Color.YELLOW);
            paint.setTextSize(16);
            paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD)); // Bold text
            paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_OVER)); // Text over picture

            canvas.drawBitmap(originalBitmap, 0, 0, paint);
            for (PointF p : lstPoints) {
                Float x = p.x,
                        y = p.y;
                drawMark(new float[]{x, y}, canvas, paint);
            }
            updateImageView(currentBitmap);
            toggleMarkAdd(false);
            updateButtonState();
            */
        }
        else{
            updateImageView(originalBitmap);
        }
    }

    @Override
    public void onChangeHappened(float[] touchPoint) {
        addPoint(touchPoint);
    }
}
