package com.richard.weger.wqc.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.richard.weger.wqc.R;

import java.util.List;

public class GeneralPictureAdapter extends ArrayAdapter<String> {

    private List<String> fileNames;
    private final Context context;
    private boolean enabled = true;

    private ChangeListener listener;

    public void setChangeListener(ChangeListener listener) {
        this.listener = listener;
    }

    public interface ChangeListener {
        void onTouch(int position, View view);
    }

    public void setEnabled(boolean enabled){
        this.enabled = enabled;
    }

    public GeneralPictureAdapter(@NonNull Context context, @NonNull List<String> fileNames) {
        super(context, R.layout.generalpicture_row_layout, fileNames);
        this.fileNames = fileNames;
        this.context = context;
    }

    @NonNull
    @Override
    public View getView(final int position, View convertView, @NonNull ViewGroup parent){
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(
                Context.LAYOUT_INFLATER_SERVICE);

        assert inflater != null;
        @SuppressLint("ViewHolder")
        View rowView = inflater.inflate(R.layout.generalpicture_row_layout, parent, false);

        final TextView tvPictureName = rowView.findViewById(R.id.tv_generalpicture_name);
        String picName = fileNames.get(position);

        tvPictureName.setEnabled(enabled);
        tvPictureName.setText(picName);

        tvPictureName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onTouch(position, tvPictureName);
            }
        });

        return rowView;
    }
}
