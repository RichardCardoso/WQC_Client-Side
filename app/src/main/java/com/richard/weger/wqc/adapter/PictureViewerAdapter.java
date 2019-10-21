package com.richard.weger.wqc.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import com.richard.weger.wqc.fragment.PictureViewerFragment;

import java.util.List;

public class PictureViewerAdapter extends FragmentStatePagerAdapter {

    private List<String> fileNames;
    private String picturesFolderPath;

    // mode 0 = zoom / pan
    // mode 1 = add mark
    private int mode;

    public PictureViewerAdapter(@NonNull FragmentManager fm, List<String> fileNames, String picturesFolderPath) {
        super(fm);
        if(fileNames == null || fileNames.size() == 0){
            throw new IllegalArgumentException("Either a null or an empty fileNames list was received at PictureViewerAdapter constructor!");
        }
        this.fileNames = fileNames;
        if(picturesFolderPath == null){
            throw new IllegalArgumentException("Null picturesFolderPath was received at PictureViewerAdapter constructor!");
        }
        this.picturesFolderPath = picturesFolderPath;
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        return PictureViewerFragment.init(fileNames.get(position), picturesFolderPath);
    }

    @Override
    public int getCount() {
        return fileNames.size();
    }

    public int getMode() {
        return mode;
    }

    public void setMode(int mode) {
        this.mode = mode;
    }
}
