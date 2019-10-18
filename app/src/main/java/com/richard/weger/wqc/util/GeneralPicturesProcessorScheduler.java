package com.richard.weger.wqc.util;

import android.os.AsyncTask;

import com.richard.weger.wqc.domain.Project;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class GeneralPicturesProcessorScheduler {

    Set<String> pictures;
    Project project;
    GeneralPicturesProcessor currentTask;
    boolean stopped = false;
    GeneralPicturesProcessorListener listener;

    public interface GeneralPicturesProcessorListener {
        void onPicturesListProcessFinish();
        void onPictureProcessed(String picPath);
    }

    public GeneralPicturesProcessorScheduler(List<String> pictures, Project project, GeneralPicturesProcessorListener listener){
        this.pictures = new HashSet<>(pictures);
        this.project = project;
        this.listener = listener;
    }

    public void appendAndProccess(List<String> pictures) {
        this.pictures.addAll(new HashSet<>(pictures));
        if(stopped) {
            stopped = false;
            process();
        }
    }

    public List<String> stopAndGetNotProcessedPics() {
        List<String> ret;
        stopped = true;
        if(currentTask != null && currentTask.getStatus() != AsyncTask.Status.FINISHED) {
            currentTask.cancel(true);
        }
        ret = new ArrayList<>(pictures);
        pictures.clear();
        return new ArrayList<>(ret);
    }

    public void process() {
        if(!stopped && pictures != null && pictures.iterator().hasNext()) {
            GeneralPicturesProcessor ex = new GeneralPicturesProcessor(this, project);
            String next = pictures.iterator().next();
            currentTask = ex;
            ex.execute(next);
        } else {
            if(listener != null) {
                stopAndGetNotProcessedPics();
                listener.onPicturesListProcessFinish();
            }
        }
    }

    void doNext(String finishedFilename) {
        pictures.remove(finishedFilename);
        if(listener != null) {
            listener.onPictureProcessed(finishedFilename);
        }
        process();
    }
}
