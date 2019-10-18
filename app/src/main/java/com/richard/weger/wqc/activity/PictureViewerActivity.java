package com.richard.weger.wqc.activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.fragment.app.FragmentActivity;
import androidx.viewpager.widget.ViewPager;

import com.richard.weger.wqc.R;
import com.richard.weger.wqc.adapter.PictureViewerAdapter;
import com.richard.weger.wqc.helper.ProjectHelper;
import com.richard.weger.wqc.helper.StringHelper;
import com.richard.weger.wqc.util.App;
import com.richard.weger.wqc.util.LoggerManager;
import com.richard.weger.wqc.util.MyViewPager;

import java.util.List;
import java.util.logging.Logger;

import static com.richard.weger.wqc.appconstants.AppConstants.PICTURES_LIST_KEY;
import static com.richard.weger.wqc.appconstants.AppConstants.PICTURE_START_INDEX_KEY;

public class PictureViewerActivity extends FragmentActivity {

    Logger logger;

    MyViewPager mPager;
    PictureViewerAdapter adapter;

    @Override
    public void onBackPressed(){}

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_picture_viewer);

        Intent intent = getIntent();
        if(intent != null) {
            List<String> picsList = intent.getStringArrayListExtra(PICTURES_LIST_KEY);
            int position = intent.getIntExtra(PICTURE_START_INDEX_KEY, 0);

            adapter = new PictureViewerAdapter(getSupportFragmentManager(), picsList, StringHelper.getPicturesFolderPath(ProjectHelper.getProject()));
            mPager = findViewById(R.id.pager);
            mPager.setAdapter(adapter);
            mPager.setCurrentItem(position);
            mPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                @Override
                public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}

                @Override
                public void onPageScrollStateChanged(int state) {}

                @Override
                public void onPageSelected(int position) {
                    updateCurrentPageLabel();
                }
            });

            logger = LoggerManager.getLogger(PictureViewerActivity.class);

            findViewById(R.id.backButton).setOnClickListener(v -> finishAndRemoveTask());
            updateCurrentPageLabel();

        } else {
            finishAndRemoveTask();
        }

    }

    private void updateCurrentPageLabel(){
        TextView tvCurrPage = findViewById(R.id.tvCurrentPage);
        tvCurrPage.setText(String.format(App.getLocale(), "%d/%d", mPager.getCurrentItem() + 1, adapter.getCount()));
    }

}
