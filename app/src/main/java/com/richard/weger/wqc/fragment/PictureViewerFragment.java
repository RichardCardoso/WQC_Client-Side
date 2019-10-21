package com.richard.weger.wqc.fragment;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.signature.StringSignature;
import com.richard.weger.wqc.R;
import com.richard.weger.wqc.helper.FileHelper;
import com.richard.weger.wqc.helper.ImageHelper;

import java.io.File;

import uk.co.senab.photoview.PhotoViewAttacher;

public class PictureViewerFragment extends Fragment {

    private String fileName, picturesFolderPath;
    private PhotoViewAttacher photoView;

    public PictureViewerFragment() {}

    @Override
    public void onCreate(Bundle data){
        super.onCreate(data);
        fileName = getArguments() != null ? getArguments().getString("fileName") : null;
        picturesFolderPath = getArguments() != null ? getArguments().getString("picturesFolderPath") : null;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if(photoView != null){
            photoView.cleanup();
            View v = getView();
            if(v != null) {
                v.getViewTreeObserver().removeOnGlobalLayoutListener(photoView);
            }
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_picture_viewer, container, false);
        String absFileName;
        absFileName = picturesFolderPath.concat(fileName);

        ImageView ivPicture = root.findViewById(R.id.ivPicture);

        ProgressBar pbLoading = root.findViewById(R.id.pbLoading);
        pbLoading.setVisibility(View.VISIBLE);
        pbLoading.bringToFront();

        if(FileHelper.isValidFile(absFileName)) {
            File f = new File(absFileName);
            Glide.with(getContext()).load(f)
                    .signature(new StringSignature(String.valueOf(f.lastModified())))
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .transform(new ImageHelper.RotateTransformation(root.getContext(), ImageHelper.getImageRotation(absFileName)))
                    .listener(new RequestListener<File, GlideDrawable>() {
                        @Override
                        public boolean onException(Exception e, File model, Target<GlideDrawable> target, boolean isFirstResource) {
                            ivPicture.setImageResource(R.drawable.ic_error);
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(GlideDrawable resource, File model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                            pbLoading.setVisibility(View.INVISIBLE);
                            photoView = new PhotoViewAttacher(ivPicture);
                            photoView.update();
//                                processFileName(fileName, tvFileName);
                            return false;
                        }
                    })
                    .into(ivPicture);
        }
        return root;
    }

    public static PictureViewerFragment init(String fileName, String picturesFolderPath){
        PictureViewerFragment f = new PictureViewerFragment();
        Bundle args = new Bundle();
        args.putString("fileName", fileName);
        args.putString("picturesFolderPath", picturesFolderPath);
        f.setArguments(args);
        return f;
    }

}
