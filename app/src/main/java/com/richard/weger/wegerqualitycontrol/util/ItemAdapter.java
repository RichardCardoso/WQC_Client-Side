package com.richard.weger.wegerqualitycontrol.util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;

import com.richard.weger.wegerqualitycontrol.R;
import com.richard.weger.wegerqualitycontrol.activity.PictureViewerActivity;
import com.richard.weger.wegerqualitycontrol.activity.ReportEditActivity;
import com.richard.weger.wegerqualitycontrol.domain.Item;

import java.util.List;

import static com.richard.weger.wegerqualitycontrol.util.AppConstants.*;

public class ItemAdapter extends ArrayAdapter<Item> {

    private final List<Item> itemList;
    private final Context context;

    private ChangeListener listener;

    public void setChangeListener(ChangeListener listener) {
        this.listener = listener;
    }

    public interface ChangeListener {
        void onChangeHappened(Item item, int position);
    }

    public ItemAdapter(@NonNull Context context, @NonNull List<Item> itemList) {
        super(context, R.layout.item_row_layout, itemList);
        this.itemList = itemList;
        this.context = context;
    }

    @Override
    public View getView(@NonNull final int position,@NonNull View convertView,@NonNull ViewGroup parent){
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(
                Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.item_row_layout, parent, false);


        ImageView imageView = rowView.findViewById(R.id.ivItem);
        TextView textView = rowView.findViewById(R.id.tvItemDesc);
        RadioButton rdNotChecked = rowView.findViewById(R.id.rdNotChecked);
        RadioButton rdNotAplicable = rowView.findViewById(R.id.rdNotAplicable);
        RadioButton rdNotAproved = rowView.findViewById(R.id.rdNotAproved);
        RadioButton rdAproved = rowView.findViewById(R.id.rdAproved);
        final EditText editText = rowView.findViewById(R.id.editItemComments);

        Item item = itemList.get(position);
        ProxyBitmap proxyBitmap = new ProxyBitmap(item.getPicture().getProxyBitmap().getBitmap());
        if(proxyBitmap.getBitmap() != null){
            imageView.setImageBitmap(proxyBitmap.getBitmap());
        }

        textView.setText(item.toString());

        editText.setText(item.getComments());

        switch(item.getStatus()){
            case ITEM_NOT_CHECKED_KEY:
                rdNotChecked.setChecked(true);
                break;
            case ITEM_APROVED_KEY:
                rdAproved.setChecked(true);
                break;
            case ITEM_NOT_APROVED_KEY:
                rdNotAproved.setChecked(true);
                break;
            case ITEM_NOT_APLICABLE_KEY:
                rdNotAplicable.setChecked(true);
                break;
        }

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onChangeHappened(itemList.get(position), position);
            }
        });

        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                itemList.get(position).setComments(s.toString());
            }
        });

        rdAproved.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    itemList.get(position).setStatus(ITEM_APROVED_KEY);
                    ((ReportEditActivity)context).updatePendingItemsCount();
                }
            }
        });
        rdNotAproved.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    itemList.get(position).setStatus(ITEM_NOT_APROVED_KEY);
                    ((ReportEditActivity)context).updatePendingItemsCount();
                }
            }
        });
        rdNotAplicable.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    itemList.get(position).setStatus(ITEM_NOT_APLICABLE_KEY);
                    ((ReportEditActivity)context).updatePendingItemsCount();
                }
            }
        });
        rdNotChecked.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    itemList.get(position).setStatus(ITEM_NOT_CHECKED_KEY);
                    ((ReportEditActivity)context).updatePendingItemsCount();
                }
            }
        });

        return rowView;
    }
}
