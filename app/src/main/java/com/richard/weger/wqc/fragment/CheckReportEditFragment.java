package com.richard.weger.wqc.fragment;


import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.richard.weger.wqc.R;
import com.richard.weger.wqc.domain.Mark;
import com.richard.weger.wqc.helper.WQCDocumentHelper;
import com.richard.weger.wqc.views.TouchImageView;

import java.lang.reflect.Type;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class CheckReportEditFragment extends Fragment implements TouchImageView.ImageTouchListener {

    public interface MarkTouchListener{
        void onMarkTouch(Mark m);
    }

    private String filePath;
    private int pageNumber;
    private List<Mark> marksList;
    private TouchImageView.ImageTouchListener iListener;
    private MarkTouchListener mListener;
    private TouchImageView ivPicture;

    public CheckReportEditFragment() {}

    @Override
    public void onCreate(Bundle data) {
        super.onCreate(data);
        filePath = getArguments() != null ? getArguments().getString("filePath") : null;
        pageNumber = getArguments() != null ? getArguments().getInt("pageNumber") : 0;
        String marksListJson = getArguments() != null ? getArguments().getString("marksList") : null;
        if (marksListJson != null) {
            Type t = new TypeToken<List<Mark>>(){}.getType();
            marksList = (new Gson()).fromJson(marksListJson, t);
        }
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_report_edit, container, false);
        ivPicture = root.findViewById(R.id.ivDocument);

        Bitmap original = WQCDocumentHelper.pageLoad(pageNumber, filePath, getResources());
        if(original != null){
            Bitmap marked = WQCDocumentHelper.updatePointsDrawing(marksList, original, getResources());
            ivPicture.setImageBitmap(marked);
            ivPicture.setMaxZoom(12f);
            ivPicture.setChangeListener(iListener);
        }

        return root;
    }

    public static CheckReportEditFragment init(String filePath, int pageNumber, List<Mark> marksList,
                                               TouchImageView.ImageTouchListener listener, MarkTouchListener mListener){
        CheckReportEditFragment f = new CheckReportEditFragment();
        Bundle b = new Bundle();

        b.putString("filePath", filePath);
        b.putInt("pageNumber", pageNumber);
        b.putString("marksList", new Gson().toJson(marksList));
        f.setArguments(b);
        f.setListeners(listener, mListener);
        return f;
    }

    private void setListeners(TouchImageView.ImageTouchListener iListener, MarkTouchListener mListener) {
        this.iListener = iListener;
        this.mListener = mListener;
    }

    @Override
    public void onTouch(float[] touchPoint) {
        Mark m = getTouchedMark(touchPoint);
        if(m != null) {
            mListener.onMarkTouch(m);
        } else {
            iListener.onTouch(touchPoint);
        }
    }

    private Mark getTouchedMark(float[] touchPoint){
        Bitmap markedBitmap = ((BitmapDrawable) ivPicture.getDrawable()).getBitmap();
        return marksList.stream().filter(m -> {
            int radius = WQCDocumentHelper.radius;
            float mX, mY, pX, pY;
            mX = m.getX() * markedBitmap.getWidth();
            mY = m.getY() * markedBitmap.getHeight();
            pX = touchPoint[0] * markedBitmap.getWidth();
            pY = touchPoint[1] * markedBitmap.getHeight();
            return (pX >= (mX - radius) && pX <= (mX + radius) &&
                    pY >= (mY - radius) && pY <= (mY + radius));
        }).findFirst().orElse(null);
    }

}
