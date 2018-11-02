package com.richard.weger.wqc.helper;

import android.content.Context;

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
import java.util.List;

import static com.richard.weger.wqc.constants.AppConstants.*;
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
}
