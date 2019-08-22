package com.richard.weger.wqc.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.content.res.ResourcesCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;

import com.richard.weger.wqc.R;
import com.richard.weger.wqc.activity.ItemReportEditActivity;
import com.richard.weger.wqc.domain.DomainEntity;
import com.richard.weger.wqc.domain.Item;
import com.richard.weger.wqc.domain.Project;
import com.richard.weger.wqc.helper.FileHelper;
import com.richard.weger.wqc.helper.MessageboxHelper;
import com.richard.weger.wqc.helper.StringHelper;
import com.richard.weger.wqc.util.App;

import java.util.Comparator;
import java.util.List;

import static com.richard.weger.wqc.appconstants.AppConstants.ITEM_APROVED_KEY;
import static com.richard.weger.wqc.appconstants.AppConstants.ITEM_NOT_APLICABLE_KEY;
import static com.richard.weger.wqc.appconstants.AppConstants.ITEM_NOT_APROVED_KEY;
import static com.richard.weger.wqc.appconstants.AppConstants.ITEM_NOT_CHECKED_KEY;

public class ItemAdapter extends ArrayAdapter<Item> {

    private List<Item> itemList;
    private final Context context;
    private Project project;
    private boolean enabled;
    private boolean cameraEnabled;

    private ChangeListener listener;

    public void setChangeListener(ChangeListener listener) {
        this.listener = listener;
    }

    public interface ChangeListener {
        void onChangeHappened(int position, View view);
    }

    public void setEnabled(boolean enabled){
        this.cameraEnabled = enabled;
        this.enabled = enabled;
    }

    public void setCameraEnabled(boolean enabled){
        this.cameraEnabled = enabled;
    }

    public ItemAdapter(@NonNull Context context, @NonNull List<Item> itemList, Project project) {
        super(context, R.layout.item_row_layout, itemList);
        this.itemList = itemList;
        this.context = context;
        this.project = project;
    }

    public void setItemList(List<Item> itemList){
        this.itemList = itemList;
    }

    @NonNull
    @Override
    public View getView(final int position, View convertView, @NonNull ViewGroup parent){
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(
                Context.LAYOUT_INFLATER_SERVICE);

        itemList.sort(Comparator.comparing(DomainEntity::getId));

        assert inflater != null;
        @SuppressLint("ViewHolder")
        View rowView = inflater.inflate(R.layout.item_row_layout, parent, false);

        final ImageView imageView = rowView.findViewById(R.id.ivItem);
        TextView tvItemDesc = rowView.findViewById(R.id.tvItemDesc);
        RadioButton rdNotChecked = rowView.findViewById(R.id.rdNotChecked);
        RadioButton rdNotAplicable = rowView.findViewById(R.id.rdNotAplicable);
        RadioButton rdNotAproved = rowView.findViewById(R.id.rdNotAproved);
        final RadioButton rdAproved = rowView.findViewById(R.id.rdAproved);
        final TextView tvComments = rowView.findViewById(R.id.tvItemComments);

        tvItemDesc.setEnabled(enabled);
        imageView.setEnabled(cameraEnabled);
        tvComments.setEnabled(enabled);
        rdNotAplicable.setEnabled(enabled);
        rdNotAproved.setEnabled(enabled);
        rdNotChecked.setEnabled(enabled);
        rdAproved.setEnabled(enabled);
        tvComments.setEnabled(enabled);

        Item item = itemList.get(position);
        String picPath = "";
        if(item != null && item.getParent() != null && item.getParent().getParent() != null && item.getParent().getParent().getParent() != null){
            String fileName = item.getPicture().getFileName();
            picPath = StringHelper.getPicturesFolderPath(project).concat(fileName);
        }
        if(FileHelper.isValidFile(picPath)){
            imageView.setImageDrawable(
                    ResourcesCompat.getDrawable(App.getContext().getResources(), android.R.drawable.ic_menu_camera, null)
//                    App.getContext().getResources().getDrawable(
//                            android.R.drawable.ic_menu_camera
//                    )
            );
//            imageView.setImageBitmap(
//                    App.getContext().getResources().getDrawable(Android)
//                    BitmapFactory.decodeFile(
//                            new File(
//                                    item.getPicture().getFileName()
//                            ).getAbsolutePath()
//                    )
//            );
        }
//        ProxyBitmap proxyBitmap = new ProxyBitmap(item.getPicture().getProxyBitmap().getBitmap());
//        if(proxyBitmap.getBitmap() != null){
//            imageView.setImageBitmap(proxyBitmap.getBitmap());
//        }

        tvItemDesc.setText(String.valueOf(item.getNumber()).concat(" - ").concat(item.getDescription()));

        if(item.getComments() != null && item.getComments().length() > 0) {
            tvComments.setText(item.getComments());
        } else {
            tvComments.setText(App.getContext().getResources().getString(R.string.commentsLabel));
        }

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

        imageView.setOnClickListener(v -> listener.onChangeHappened(position, imageView));

        tvComments.setOnClickListener(v -> {
            String message = itemList.get(position).getDescription();
            MessageboxHelper.getString(context, message, (content) -> {
                if (!content.equals(App.getContext().getResources().getString(R.string.editCommentsHint))) {
                    itemList.get(position).setComments(content);
                    tvComments.setText(content);
                    listener.onChangeHappened(position, tvComments);
                }
            });
        });

        rdAproved.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if(isChecked){
                itemList.get(position).setStatus(ITEM_APROVED_KEY);
                ((ItemReportEditActivity)context).updatePendingItemsCount();
                listener.onChangeHappened(position, rdAproved);
            }
        });
        rdNotAproved.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if(isChecked){
                itemList.get(position).setStatus(ITEM_NOT_APROVED_KEY);
                ((ItemReportEditActivity)context).updatePendingItemsCount();
                listener.onChangeHappened(position, rdAproved);
            }
        });
        rdNotAplicable.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if(isChecked){
                itemList.get(position).setStatus(ITEM_NOT_APLICABLE_KEY);
                ((ItemReportEditActivity)context).updatePendingItemsCount();
                listener.onChangeHappened(position, rdAproved);
            }
        });
        rdNotChecked.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if(isChecked){
                itemList.get(position).setStatus(ITEM_NOT_CHECKED_KEY);
                ((ItemReportEditActivity)context).updatePendingItemsCount();
                listener.onChangeHappened(position, rdAproved);
            }
        });

        return rowView;
    }
}
