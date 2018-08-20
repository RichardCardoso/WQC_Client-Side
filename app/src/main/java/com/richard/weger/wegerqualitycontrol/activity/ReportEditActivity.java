package com.richard.weger.wegerqualitycontrol.activity;

import android.app.ListActivity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.richard.weger.wegerqualitycontrol.R;
import com.richard.weger.wegerqualitycontrol.domain.Item;
import com.richard.weger.wegerqualitycontrol.domain.Project;
import com.richard.weger.wegerqualitycontrol.domain.Report;
import com.richard.weger.wegerqualitycontrol.util.ItemAdapter;
import com.richard.weger.wegerqualitycontrol.util.ProxyBitmap;

import static com.richard.weger.wegerqualitycontrol.util.AppConstants.ITEM_ID_KEY;
import static com.richard.weger.wegerqualitycontrol.util.AppConstants.ITEM_KEY;
import static com.richard.weger.wegerqualitycontrol.util.AppConstants.PICTURES_AUTHORITY;
import static com.richard.weger.wegerqualitycontrol.util.AppConstants.PICTURE_VIEWER_SCREEN_ID;
import static com.richard.weger.wegerqualitycontrol.util.AppConstants.PROJECT_KEY;
import static com.richard.weger.wegerqualitycontrol.util.AppConstants.REPORT_KEY;

public class ReportEditActivity extends ListActivity implements ItemAdapter.ChangeListener{

    Report report;
    Project project;
    ItemAdapter itemAdapter;

    @Override
    public void onBackPressed() {
        /*if (!shouldAllowBack()) {
            doSomething();
        } else {
            super.onBackPressed();
        }
        */
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report_edit);
        Intent intent = getIntent();
        if(intent != null) {
            report = (Report) intent.getSerializableExtra(REPORT_KEY);
            project = (Project) intent.getSerializableExtra(PROJECT_KEY);
        }
        if(report == null || project == null){
            Toast.makeText(this, R.string.dataRecoverError,Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        itemAdapter = new ItemAdapter(this, report.getItemList());
        setListAdapter(itemAdapter);
        itemAdapter.setChangeListener(this);
        setListeners();
        setTextViews();
        updatePictures();
    }

    private void updatePictures(){
        for(Item i : report.getItemList()){
            if(i.getPicture() != null){
                Bitmap bitmap;
                Drawable drawable;
                if(i.getPicture().getFilePath().length() > PICTURES_AUTHORITY.length()){
                    drawable = getResources().getDrawable(android.R.drawable.ic_menu_gallery);
                }
                else{
                    drawable = getResources().getDrawable(android.R.drawable.ic_menu_camera);
                }
                bitmap = ((BitmapDrawable)drawable).getBitmap();
                i.getPicture().setProxyBitmap(new ProxyBitmap(bitmap));
            }
        }
    }

    private void setTextViews(){
        ((TextView)findViewById(R.id.tvProjectInfo)).setText(
                String.format(getResources().getConfiguration().locale,
                        "%s - Z%d - T%d",
                        project.getNumber(),
                        project.getDrawingList().get(0).getNumber(),
                        project.getDrawingList().get(0).getPart().get(0).getNumber()
                        )
        );
        ((TextView)findViewById(R.id.tvReportType)).setText(report.toString());

        ((EditText)findViewById(R.id.editReportComments)).addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                report.setComments(s.toString());
            }
        });

        EditText editText = findViewById(R.id.editReportComments);
        String reportComments = report.getComments();
        editText.setText(reportComments);

        updatePendingItemsCount();
    }

    private void setListeners(){
        Button btn = findViewById(R.id.btnCancel);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setResult(RESULT_CANCELED);
                finish();
            }
        });
        btn = findViewById(R.id.btnSave);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.putExtra(REPORT_KEY, report);
                setResult(RESULT_OK, intent);
                finish();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode == RESULT_OK){
            if(requestCode == PICTURE_VIEWER_SCREEN_ID){
                Item i = (Item) data.getSerializableExtra(ITEM_KEY);
                int itemId = data.getIntExtra(ITEM_ID_KEY, -1);
                if(itemId > -1) {
                    report.getItemList().set(itemId, i);
                }
                updatePictures();
                itemAdapter.notifyDataSetChanged();
                /*
                int position = data.getIntExtra(ITEM_ID_KEY, -1);
                Bitmap bitmap;
                if(i != null) {
                    bitmap = i.getPicture().getProxyBitmap().getBitmap();
                }
                else{
                    Toast.makeText(this,R.string.dataRecoverError, Toast.LENGTH_LONG).show();
                    return;
                }
                if(bitmap != null && position > -1){
                    // report.getItemList().get(position).getPicture().setProxyBitmap(new ProxyBitmap(bitmap));
                }
                */
            }
        }
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        updatePendingItemsCount();
    }

    public void updatePendingItemsCount(){
        ((TextView)findViewById(R.id.tvPendingItems)).setText(
                String.format(getResources().getConfiguration().locale, "%s: %d",
                        getResources().getString(R.string.pendingItems),
                        report.getPendingItemsCount()));
    }

    @Override
    public void onChangeHappened(Item item, int position) {
        Intent intent = new Intent(ReportEditActivity.this, PictureViewerActivity.class);
        intent.putExtra(ITEM_KEY, item);
        intent.putExtra(ITEM_ID_KEY, position);
        intent.putExtra(PROJECT_KEY, project);
        startActivityForResult(intent, PICTURE_VIEWER_SCREEN_ID);
    }

}
