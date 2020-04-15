package com.richard.weger.wqc.adapter;

import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.richard.weger.wqc.R;
import com.richard.weger.wqc.helper.FileHelper;
import com.richard.weger.wqc.helper.ImageHelper;
import com.richard.weger.wqc.util.App;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.richard.weger.wqc.util.App.getStringResource;

public class ItemViewHolder extends RecyclerView.ViewHolder {
    TextView tvItemDesc;
    ImageView ivItemPic;
    TextView tvComments;
    Spinner statusSpinner;
    ImageButton takeButton;
    ConstraintLayout rootLayout;

    ItemViewHolder(@NonNull View itemView) {
        super(itemView);
        ivItemPic = itemView.findViewById(R.id.ivItemPic);
        tvItemDesc = itemView.findViewById(R.id.tvItemDesc);
        statusSpinner = itemView.findViewById(R.id.statusSpinner);
        tvComments = itemView.findViewById(R.id.tvItemComments);
        takeButton = itemView.findViewById(R.id.backButton);
        rootLayout = itemView.findViewById(R.id.rootLayout);

         List<String> statusStringList = new ArrayList<>(Arrays.asList(
                 getStringResource(R.string.rdNotChecked),
                getStringResource(R.string.rdAproved),
                getStringResource(R.string.rdNotAproved),
                getStringResource(R.string.rdNotAplicable)));

//        List<Integer> statusList = new ArrayList<>(Arrays.asList(ITEM_NOT_CHECKED_KEY, ITEM_APROVED_KEY, ITEM_NOT_APROVED_KEY, ITEM_NOT_APLICABLE_KEY));
        ArrayAdapter<String> statusAdapter = new ArrayAdapter<>(App.getContext(), android.R.layout.simple_spinner_dropdown_item, statusStringList);
        statusSpinner.setAdapter(statusAdapter);

    }

    public void setDescription(String description){
        tvItemDesc.setText(description);
    }

    void setComments(String comments){
        if(comments != null && comments.length() > 0) {
            tvComments.setText(comments);
        } else {
            tvComments.setText(getStringResource(R.string.commentsLabel));
        }
    }

    void setStatus(int status, boolean triggerHandler) {
        statusSpinner.setSelection(status, triggerHandler);
    }

    void setPicName(String rootPath, String fileName) {
        String filePath;
        filePath = rootPath.concat(File.separator).concat(fileName);
        ivItemPic.setImageDrawable(ResourcesCompat.getDrawable(ivItemPic.getRootView().getResources(), android.R.drawable.ic_input_get, null));
        if(FileHelper.isValidFile(filePath)) {
            Glide.with(ivItemPic.getRootView().getContext()).load(filePath)
                    .thumbnail(1f)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .fitCenter()
                    .transform(new ImageHelper.RotateTransformation(itemView.getContext(), ImageHelper.getImageRotation(filePath)))
                    .into(ivItemPic);
        }
    }

}
