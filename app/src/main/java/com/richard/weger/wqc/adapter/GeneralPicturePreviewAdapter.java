package com.richard.weger.wqc.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.signature.StringSignature;
import com.richard.weger.wqc.R;
import com.richard.weger.wqc.helper.FileHelper;
import com.richard.weger.wqc.helper.ImageHelper;
import com.richard.weger.wqc.util.App;
import com.richard.weger.wqc.util.GeneralPictureDTO;

import java.io.File;
import java.util.List;

public class GeneralPicturePreviewAdapter extends RecyclerView.Adapter<GeneralPicturePreviewAdapter.GeneralPicturesViewHolder> {

    private String rootPath;
    private List<GeneralPictureDTO> files;
    private PictureTapHandler pictureTapHandler;
    private boolean canRemove;

    public boolean isCanRemove() {
        return canRemove;
    }

    public void setCanRemove(boolean canRemove) {
        this.canRemove = canRemove;
    }

    public interface PictureTapHandler {
        void onPictureTap(int position);
        void onRemoveRequest(int position);
    }

    public GeneralPicturePreviewAdapter(String picturesPath, List<GeneralPictureDTO> files, PictureTapHandler handler, boolean canRemove) {
        rootPath = picturesPath;

        this.files = files;
        this.pictureTapHandler = handler;
        this.canRemove = canRemove;
    }

    @NonNull
    @Override
    public GeneralPicturesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.general_picture_item_row, parent, false);
        return new GeneralPicturesViewHolder(view);
    }

    @Override
    public void onViewRecycled(@NonNull GeneralPicturesViewHolder holder) {
//        Glide.clear(holder.ivPicture);
    }

    @Override
    public void onBindViewHolder(@NonNull GeneralPicturesViewHolder holder, int position) {
        String fileName = files.get(position).getFileName();
        boolean processed = files.get(position).isProcessed();
        boolean error = files.get(position).isError();

        holder.ivPicture.setOnClickListener(v -> {
            if(pictureTapHandler != null) {
                pictureTapHandler.onPictureTap(position);
            }
        });

        if(error) {
            holder.ivProcessed.setImageResource(R.drawable.ic_error);
        } else {
            holder.ivProcessed.setImageResource(R.drawable.ic_success);
        }

        if(processed){
            holder.ivProcessed.setVisibility(View.VISIBLE);
            holder.removeButton.setVisibility(View.INVISIBLE);
        } else {
            holder.ivProcessed.setVisibility(View.INVISIBLE);
            if(files.get(position).isProcessing()) {
                holder.removeButton.setVisibility(View.INVISIBLE);
            }
        }

        holder.removeButton.setOnClickListener(v -> pictureTapHandler.onRemoveRequest(position));

        holder.setFileName(fileName);

    }

    @Override
    public int getItemCount() {
        return files.size();
    }

    public class GeneralPicturesViewHolder extends RecyclerView.ViewHolder {
        ImageView ivPicture, ivProcessed;
        TextView tvFileName;
        ProgressBar pbLoading;
        ImageButton removeButton;

        GeneralPicturesViewHolder(@NonNull View itemView) {
            super(itemView);
            ivPicture = itemView.findViewById(R.id.ivPicture);
            ivProcessed = itemView.findViewById(R.id.ivProcessed);
            tvFileName = itemView.findViewById(R.id.tvFileName);
            pbLoading = itemView.findViewById(R.id.pbLoading);
            removeButton = itemView.findViewById(R.id.removeButton);

        }

        public void setFileName(String fileName) {
            String filePath;
            filePath = rootPath.concat(File.separator).concat(fileName);
            pbLoading.bringToFront();
            pbLoading.setVisibility(View.VISIBLE);
            removeButton.setVisibility(View.GONE);
            if(FileHelper.isValidFile(filePath)) {
                File f = new File(filePath);
                Glide.with(App.getContext()).load(f)
                        .signature(new StringSignature(String.valueOf(f.lastModified())))
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .transform(new ImageHelper.RotateTransformation(itemView.getContext(), ImageHelper.getImageRotation(filePath)))
                        .listener(new RequestListener<File, GlideDrawable>() {
                            @Override
                            public boolean onException(Exception e, File model, Target<GlideDrawable> target, boolean isFirstResource) {
                                pbLoading.setVisibility(View.INVISIBLE);
                                ivPicture.setImageDrawable(ContextCompat.getDrawable(App.getContext(), R.drawable.ic_error));
                                return false;
                            }

                            @Override
                            public boolean onResourceReady(GlideDrawable resource, File model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                                pbLoading.setVisibility(View.INVISIBLE);
                                removeButton.bringToFront();
                                if(canRemove) {
                                    removeButton.setVisibility(View.VISIBLE);
                                } else {
                                    removeButton.setVisibility(View.GONE);
                                }
                                ivPicture.setLayoutParams(new ConstraintLayout.LayoutParams(ivPicture.getWidth(), ivPicture.getWidth()));
//                                processFileName(fileName, tvFileName);
                                return false;
                            }
                        })
                        .into(ivPicture);
            }
        }

    }

    private void processFileName(String fileName, TextView tvFileName) {
        tvFileName.bringToFront();
        tvFileName.setBackgroundColor(ContextCompat.getColor(App.getContext(), R.color.white));
        tvFileName.getBackground().setAlpha(128);
        if(fileName.contains(".")) {
            fileName = fileName.substring(0, fileName.indexOf("."));
        }
        String project = fileName.substring(0, fileName.indexOf("Z"));
        String drawingPart = fileName.substring(project.length(), fileName.indexOf("Q"));
        String picInfo = fileName.substring(fileName.indexOf("Q"));
        fileName = project.concat("\n").concat(drawingPart).concat("\n").concat(picInfo);
        tvFileName.setText(fileName);
    }
}

