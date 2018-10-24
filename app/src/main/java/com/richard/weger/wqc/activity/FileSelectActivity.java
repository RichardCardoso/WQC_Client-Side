package com.richard.weger.wqc.activity;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import com.richard.weger.wqc.R;
import com.richard.weger.wqc.util.FileFromList;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static com.richard.weger.wqc.util.AppConstants.*;

public class FileSelectActivity extends ListActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_select);

        String path = this.getFilesDir().getAbsolutePath();
        File directory = new File(path);
        File[] files = directory.listFiles();
        List<FileFromList> filesFromList = new ArrayList<>();
        int x = 0;
        for(File f:files){
            String fName = f.getName();
            String fExt = fName.substring(fName.length() - 4 ,fName.length());
            boolean fContainsZ = fName.contains("_Z_");
            boolean fContainsT = fName.contains("_T_");
            if(!fExt.equals(FILE_EXTENSION) || ! (fContainsZ && fContainsT && fName.lastIndexOf('.') > 0)){
                continue;
            }
            FileFromList fileFromList = new FileFromList(this, PreferenceManager.getDefaultSharedPreferences(this));
            fileFromList.setId(x++);
            fileFromList.setFilePath(f.getAbsolutePath());
            fileFromList.setFileName(f.getName());
            filesFromList.add(fileFromList);
        }
        final ArrayAdapter<FileFromList> arrayAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_list_item_1,
                filesFromList);

        setListAdapter(arrayAdapter);
        getListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                FileFromList ffl = (FileFromList) getListAdapter().getItem(position);
                Intent intent = new Intent();
                intent.putExtra(CONTINUE_CODE_KEY, ffl.getFileName());
                setResult(RESULT_OK, intent);
                finish();
            }
        });
    }
}
