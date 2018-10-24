package com.richard.weger.wqc.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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

import com.richard.weger.wqc.R;
import com.richard.weger.wqc.activity.ItemReportEditActivity;
import com.richard.weger.wqc.domain.Item;
import com.richard.weger.wqc.util.App;
import com.richard.weger.wqc.util.FileHandler;

import java.io.File;
import java.util.List;

import static com.richard.weger.wqc.util.AppConstants.*;

public class ItemAdapter extends ArrayAdapter<Item> {

    private final List<Item> itemList;
    private final Context context;

    private ChangeListener listener;

    public void setChangeListener(ChangeListener listener) {
        this.listener = listener;
    }

    public interface ChangeListener {
        void onChangeHappened(Item item, int position, View view);
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


        final ImageView imageView = rowView.findViewById(R.id.ivItem);
        TextView textView = rowView.findViewById(R.id.tvItemDesc);
        RadioButton rdNotChecked = rowView.findViewById(R.id.rdNotChecked);
        RadioButton rdNotAplicable = rowView.findViewById(R.id.rdNotAplicable);
        RadioButton rdNotAproved = rowView.findViewById(R.id.rdNotAproved);
        final RadioButton rdAproved = rowView.findViewById(R.id.rdAproved);
        final EditText editText = rowView.findViewById(R.id.editItemComments);

        final Item item = itemList.get(position);
        if(FileHandler.isValidFile(item.getPicture().getFilePath())){
            imageView.setImageDrawable(
                    App.getContext().getResources().getDrawable(
                            android.R.drawable.ic_menu_camera
                    )
            );
//            imageView.setImageBitmap(
//                    App.getContext().getResources().getDrawable(Android)
//                    BitmapFactory.decodeFile(
//                            new File(
//                                    item.getPicture().getFilePath()
//                            ).getAbsolutePath()
//                    )
//            );
        }
//        ProxyBitmap proxyBitmap = new ProxyBitmap(item.getPicture().getProxyBitmap().getBitmap());
//        if(proxyBitmap.getBitmap() != null){
//            imageView.setImageBitmap(proxyBitmap.getBitmap());
//        }

        textView.setText(String.valueOf(item.getNumber()).concat(" - ").concat(item.getDescription()));

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
                listener.onChangeHappened(item, position, imageView);
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
                listener.onChangeHappened(item, position, editText);
            }
        });

        rdAproved.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    itemList.get(position).setStatus(ITEM_APROVED_KEY);
                    ((ItemReportEditActivity)context).updatePendingItemsCount();
                    listener.onChangeHappened(item, position, rdAproved);
                }
            }
        });
        rdNotAproved.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    itemList.get(position).setStatus(ITEM_NOT_APROVED_KEY);
                    ((ItemReportEditActivity)context).updatePendingItemsCount();
                    listener.onChangeHappened(item, position, rdAproved);
                }
            }
        });
        rdNotAplicable.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    itemList.get(position).setStatus(ITEM_NOT_APLICABLE_KEY);
                    ((ItemReportEditActivity)context).updatePendingItemsCount();
                    listener.onChangeHappened(item, position, rdAproved);
                }
            }
        });
        rdNotChecked.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    itemList.get(position).setStatus(ITEM_NOT_CHECKED_KEY);
                    ((ItemReportEditActivity)context).updatePendingItemsCount();
                    listener.onChangeHappened(item, position, rdAproved);
                }
            }
        });

        return rowView;
    }
}
