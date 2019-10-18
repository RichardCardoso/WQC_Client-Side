package com.richard.weger.wqc.fragment;


import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.richard.weger.wqc.R;
import com.richard.weger.wqc.helper.ImageHelper;

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
        Bitmap bitmap = ImageHelper.getScaledAndRotatedBitmap(absFileName, true);
        if(bitmap != null) {
            ivPicture.setImageBitmap(bitmap);
            photoView = new PhotoViewAttacher(ivPicture);
            photoView.update();
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
