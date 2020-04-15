package com.richard.weger.wqc.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.richard.weger.wqc.R;
import com.richard.weger.wqc.domain.Item;
import com.richard.weger.wqc.helper.AlertHelper;
import com.richard.weger.wqc.util.App;

import java.util.ArrayList;
import java.util.List;

import static com.richard.weger.wqc.util.App.getStringResource;

public class ItemReportAdapter extends RecyclerView.Adapter<ItemViewHolder> {

    private String rootPath;
    private List<Item> itemList;
    private ReportItemActionHandler reportItemTapHandler;
    private boolean enabled;

    private boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public ItemReportAdapter(String picturesPath, List<Item> itemList, ReportItemActionHandler handler) {
        rootPath = picturesPath;
        this.itemList = new ArrayList<>();
        this.itemList.addAll(itemList);
        this.reportItemTapHandler = handler;
        this.enabled = true;
    }

    public void setItemList(List<Item> itemList){
        this.itemList.clear();
        this.itemList.addAll(itemList);
    }

    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_row_layout, parent, false);
        return new ItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder holder, int position) {
        String picName, comments, description;
        int status;

        picName = itemList.get(position).getPicture().getFileName();
        comments = itemList.get(position).getComments();
        description = String.format(App.getLocale(), "%d - %s", position + 1, itemList.get(position).getDescription());
        status = itemList.get(position).getStatus();

        holder.setPicName(rootPath, picName);
        holder.setDescription(description);
        holder.setStatus(status, false);
        holder.setComments(comments);

        holder.ivItemPic.setOnClickListener(v -> {
            if(reportItemTapHandler != null) {
                reportItemTapHandler.onPictureTap(position);
            }
        });

        holder.statusSpinner.setEnabled(isEnabled());
        if(holder.rootLayout != null) {
            if (status == 0) {
                holder.tvItemDesc.setBackgroundColor(ContextCompat.getColor(holder.rootLayout.getContext(), R.color.red));
            } else {
                holder.tvItemDesc.setBackgroundColor(ContextCompat.getColor(holder.rootLayout.getContext(), android.R.color.transparent));
            }
        }


        holder.statusSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int selection, long id) {
                if(reportItemTapHandler != null) {
                    reportItemTapHandler.onStatusTap(selection, position);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        holder.tvComments.setEnabled(isEnabled());
        holder.tvComments.setOnClickListener(v -> {
            String message = String.format(App.getLocale(), "%d - %s", position + 1, itemList.get(position).getDescription());
            String defaultText = getStringResource(R.string.editCommentsHint);
            AlertHelper.getString(message, (content) -> {
                if (content != null && !content.equals(defaultText) && reportItemTapHandler != null) {
                    reportItemTapHandler.onCommentsChange(position, content);
                }
            }, defaultText);
        });

        holder.takeButton.setEnabled(isEnabled());
        holder.takeButton.setOnClickListener(v -> {
            if(reportItemTapHandler != null) {
                reportItemTapHandler.onRequestPictureCapture(position);
            }
        });

    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

}

