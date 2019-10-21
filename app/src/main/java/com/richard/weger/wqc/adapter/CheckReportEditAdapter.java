package com.richard.weger.wqc.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import com.richard.weger.wqc.domain.Page;
import com.richard.weger.wqc.fragment.CheckReportEditFragment;
import com.richard.weger.wqc.views.TouchImageView;

import java.util.List;

public class CheckReportEditAdapter extends FragmentStatePagerAdapter {


    private List<Page> pages;
    private String filePath;
    private TouchImageView.ImageTouchListener listener;
    private CheckReportEditFragment.MarkTouchListener mListener;
    private int currentPosition;
    Fragment fragment;

    public void setPages(List<Page> pages){
        this.pages.clear();
        this.pages.addAll(pages);
    }

    public CheckReportEditAdapter(@NonNull FragmentManager fm, String fileName, String pdfsFolder, List<Page> pages,
                                  TouchImageView.ImageTouchListener listener, CheckReportEditFragment.MarkTouchListener mListener) {
        super(fm);
        this.pages = pages;
        String filePath;
        filePath = pdfsFolder.concat(fileName);
        this.filePath = filePath;
        this.listener = listener;
        this.mListener = mListener;
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        fragment = CheckReportEditFragment.init(filePath, position, pages.get(position).getMarks(), listener, mListener);
        return fragment;
    }

    @Override
    public int getItemPosition(@NonNull Object object) {
        // this method will be called for every fragment in the ViewPager
        return POSITION_NONE;
    }

    @Override
    public int getCount() {
        return pages.size();
    }

    public int getCurrentPosition() {
        return currentPosition;
    }

    public void setCurrentPosition(int currentPosition) {
        this.currentPosition = currentPosition;
    }
}
