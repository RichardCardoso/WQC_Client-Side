package com.richard.weger.wqc.helper;

import android.content.Context;
import android.os.AsyncTask;

import com.richard.weger.wqc.domain.CheckReport;
import com.richard.weger.wqc.domain.DrawingRef;
import com.richard.weger.wqc.domain.Item;
import com.richard.weger.wqc.domain.ItemReport;
import com.richard.weger.wqc.domain.Mark;
import com.richard.weger.wqc.domain.Page;
import com.richard.weger.wqc.domain.Project;
import com.richard.weger.wqc.domain.Report;
import com.richard.weger.wqc.rest.RestTemplateHelper;
import com.richard.weger.wqc.rest.UriBuilder;
import com.richard.weger.wqc.util.ItemsMissingPictures;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.richard.weger.wqc.constants.AppConstants.*;
import static com.richard.weger.wqc.helper.LogHelper.writeData;
import static java.io.File.separatorChar;

public class ProjectHelper {
    // superFolder = "Originals/"
    public static void byteArrayToFile(byte[] bytes, Project project, UriBuilder uriBuilder, String superFolder) throws IOException {
        String folder = StringHelper.getProjectFolderPath(project).concat(superFolder);
        String fileName = uriBuilder.getParameters().get(0);
        String filePath = folder + "/" + fileName;
        if(bytes != null) {
            FileHelper.byteArray2File(filePath, bytes);
        }
    }

    public static List<Item> itemsWithMissingPictures(Project project){
        List<Item> picList = new ArrayList<>();
        for(Report report : project.getDrawingRefs().get(0).getReports()) {
            if (report instanceof ItemReport) {
                ItemReport ir = (ItemReport) report;
                for (Item item : ir.getItems()) {
                    String picsDirPath = StringHelper.getPicturesFolderPath(project);
                    String picFileName = item.getPicture().getFileName();
                    String filePath = picsDirPath.concat(picFileName);
                    if (!FileHelper.isValidFile(filePath)) {
                        picList.add(item);
                    }
                }
            }
        }
        return picList;
    }

    public static Item getItemReferences(Project project, Item currItem){
        ProjectHelper.linkReferences(project);
        for (Report report: project.getDrawingRefs().get(0).getReports()){
            if(report instanceof ItemReport){
                ItemReport ir = (ItemReport) report;
                for(Item item : ir.getItems()){
                    if(item.getId() == currItem.getId()){
                        return item;
                    }
                }
            }
        }
        return null;
    }

    public static boolean hasAllReportFiles(Project project){
        String folder = StringHelper.getProjectFolderPath(project).concat("Originals/");
        if(project != null){
            for(Report r : project.getDrawingRefs().get(0).getReports()){
                if(r instanceof CheckReport){
                    CheckReport checkReport = (CheckReport) r;
                    File file = new File(folder.concat(String.valueOf(separatorChar)).concat(checkReport.getFileName()));
                    if(!file.exists()){
                        return false;
                    } else {
                        try{
                            if(PdfHelper.getPageCount(file.getPath()) <= 0){
                                return false;
                            }
                        } catch (Exception ex){
                            ex.printStackTrace();
                            return false;
                        }
                    }
                }
            }
        }
        return true;
    }

    public static Project fromJson(String json){
        Project project = JsonHelper.toObject(json, Project.class);
        linkReferences(project);
        return project;
    }

    public static void linkReferences(Project project){
        if(project != null){
            for(DrawingRef d : project.getDrawingRefs()){
                if(d != null){
                    d.setProject(project);
                    for(Report r : d.getReports()){
                        if(r != null){
                            r.setDrawingref(d);
                            if(r instanceof ItemReport){
                                for(Item i : ((ItemReport)r).getItems()){
                                    if(i != null){
                                        i.setItemReport((ItemReport) r);
                                        if(i.getPicture() != null){
                                            i.getPicture().setItem(i);
                                        }
                                    }
                                }
                            } else if(r instanceof CheckReport){
                                for(Page pg : ((CheckReport) r).getPages()){
                                    if(pg != null){
                                        pg.setReport((CheckReport) r);
                                        for(Mark m : pg.getMarks()){
                                            m.setPage(pg);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    public static boolean isEverythingFinished(Project project){
        DrawingRef d = project.getDrawingRefs().get(0);
        for(Report r : d.getReports()){
            if(r instanceof CheckReport){
                if(((CheckReport) r).getMarksCount() == 0){
                    return false;
                }
            } else if (r instanceof ItemReport){
                if((((ItemReport) r).getPendingItemsCount() > 0)){
                    return false;
                }
            }
        }
        return true;
    }

    public static void reportFilesErase(Project project){
        DrawingRef d = project.getDrawingRefs().get(0);
        String superFolder = StringHelper.getProjectFolderPath(project);
        for(Report r : d.getReports()){
            if(r instanceof CheckReport){
                String fileName = ((CheckReport) r).getFileName() ;
                if(!fileName.equals("")){
                    FileHelper.fileDelete(superFolder.concat("/").concat(fileName));
                }
            }
        }
    }

    public static void projectUpdate(Project project, RestTemplateHelper.RestHelperResponse delegate){
        RestTemplateHelper restTemplateHelper = new RestTemplateHelper(delegate);
        UriBuilder uriBuilder = new UriBuilder();
        uriBuilder.setRequestCode(REST_PROJECTSAVE_KEY);
        uriBuilder.setProject(project);
        restTemplateHelper.execute(uriBuilder);
    }

    public void checkForGenPictures(RestTemplateHelper.RestHelperResponse delegate, Project project, boolean deleteOldFiles){
        writeData("Started routine to check if general pictures download is necessary");
        if(deleteOldFiles) {
            String picFolderPath = StringHelper.getPicturesFolderPath(project);
            if (FileHelper.isValidFile(picFolderPath)) {
                File picFolder = new File(picFolderPath);
                for (File f : picFolder.listFiles()) {
                    if (f.getName().contains("QP")) {
                        FileHelper.fileDelete(f.getAbsolutePath());
                    }
                }
            }
        }
        requestGenPictures(delegate, project);
    }

    private void requestGenPictures(RestTemplateHelper.RestHelperResponse delegate, Project project){
        UriBuilder uriBuilder = new UriBuilder();
        uriBuilder.setRequestCode(REST_GENPICTURESREQUEST_KEY);
        uriBuilder.setProject(project);

        RestTemplateHelper restTemplateHelper = new RestTemplateHelper(delegate);
        restTemplateHelper.execute(uriBuilder);
    }

    public void getGenPictures(RestTemplateHelper.RestHelperResponse delegate, List<String> pictures, List<RestTemplateHelper> queue, Project project){
        for(String fileName : pictures) {
            boolean isNeeded = true;
//            boolean exists = FileHelper.isValidFile(StringHelper.getQrText(fileName.getItemReport().getDrawingref().getProject()).concat("/").concat(fileName));
            if(fileName.length() > 0) {
                File picturesFolder = new File(StringHelper.getPicturesFolderPath(project));
                if(picturesFolder.exists()) {
                    for (File f : picturesFolder.listFiles()) {
                        if (f.getName().contains(fileName)) {
                            isNeeded = false;
                            break;
                        }
                    }
                }
                if(isNeeded){
                    RestTemplateHelper restTemplateHelper = addTask(delegate, queue);
                    if (fileName.contains("/")) {
                        fileName = fileName.substring(fileName.lastIndexOf("/") + 1);
                    }
                    UriBuilder uriBuilder = new UriBuilder();
                    uriBuilder.setRequestCode(REST_GENPICTUREDOWNLOAD_KEY);
                    uriBuilder.setProject(project);
                    uriBuilder.getParameters().add(fileName);

                    restTemplateHelper.execute(uriBuilder);
                }
            }
        }
    }

    public static boolean hasPendingTasks(List<RestTemplateHelper> restTemplateHelperQueue, boolean ignoreLast){
        int limit = 0;

        if(ignoreLast)
            limit = 1;

        if(restTemplateHelperQueue != null){
            removeFinishedTasks(restTemplateHelperQueue);
            return restTemplateHelperQueue.size() > limit;
        } else {
            return false;
        }
    }

    private static void removeFinishedTasks(List<RestTemplateHelper> restTemplateHelperQueue){
        for (int i = 0; i < restTemplateHelperQueue.size(); i ++) {
            RestTemplateHelper r = restTemplateHelperQueue.get(i);
            if (r.getStatus() == AsyncTask.Status.FINISHED || r.isCancelled() || r.getStatus() == AsyncTask.Status.PENDING) {
                restTemplateHelperQueue.remove(r);
            }
        }
    }

    public static int getCurrentPicNumber(Project project){
        File folder = new File(StringHelper.getPicturesFolderPath(project));
        int currentPicNumber = 1;
        if(folder.exists() && folder.listFiles().length > 0) {
            currentPicNumber += Arrays.asList(folder.listFiles()).stream().filter(f -> f.getName().contains("QP")).count();
//            for (File f : folder.listFiles()) {
//                String fName = f.getName();
//                if (fName.contains("QP"))
//                    currentPicNumber++;
//            }
        }
        return currentPicNumber;
    }

    public static void itemPictureUpload(RestTemplateHelper.RestHelperResponse delegate, Item item, Report report){
        writeData("Started picture upload request");

        String picName = item.getPicture().getFileName();
        picName = picName.substring(picName.lastIndexOf("/") + 1);

        RestTemplateHelper restTemplateHelper = new RestTemplateHelper(delegate);
        UriBuilder uriBuilder = new UriBuilder();
        uriBuilder.setRequestCode(REST_PICTUREUPLOAD_KEY);
        uriBuilder.setReport(report);
        uriBuilder.setItem(item);
        uriBuilder.setProject(report.getDrawingref().getProject());
        uriBuilder.getParameters().add(picName);
        restTemplateHelper.execute(uriBuilder);
    }

    public static void generalPictureUpload(RestTemplateHelper.RestHelperResponse delegate, Project project, String picName, List<RestTemplateHelper> queue){
        RestTemplateHelper restTemplateHelper = addTask(delegate, queue);
        UriBuilder uriBuilder = new UriBuilder();
        uriBuilder.setRequestCode(REST_GENPICTUREUPLOAD_KEY);
        uriBuilder.setProject(project);
        uriBuilder.getParameters().add(picName);
        restTemplateHelper.execute(uriBuilder);
    }

    private static RestTemplateHelper addTask(RestTemplateHelper.RestHelperResponse delegate, List<RestTemplateHelper> queue){
        RestTemplateHelper helper = new RestTemplateHelper(delegate);
        queue.add(helper);
        return helper;
    }
}
