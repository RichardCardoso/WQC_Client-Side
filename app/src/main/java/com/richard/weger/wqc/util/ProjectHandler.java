package com.richard.weger.wqc.util;

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

import java.io.File;
import java.io.IOException;

import static com.richard.weger.wqc.util.AppConstants.*;
import static java.io.File.separatorChar;

public class ProjectHandler {
    public void byteArrayToFile(byte[] bytes, Project project, UriBuilder uriBuilder) throws IOException {
        String folder = StringHandler
                .generateProjectFolderName(App.getContext().getExternalFilesDir(null), project)
                .concat("Originals/");
        String fileName = uriBuilder.getParameters().get(0);
        String filePath = folder + "/" + fileName;
        PdfHandler.byteArray2Pdf(filePath, bytes);
        for(Report r : project.getDrawingRefs().get(0).getReports()){
            if(r instanceof CheckReport){
                CheckReport cr = (CheckReport) r;
                if(cr.getServerPdfPath().equals(fileName)){
                    cr.setClientPdfPath(fileName);
                }
            }
        }
    }

    public static boolean hasAllReportFiles(Project project){
        String folder = StringHandler
                .generateProjectFolderName(App.getContext().getExternalFilesDir(null), project)
                .concat("Originals/");
        if(project != null){
            for(Report r : project.getDrawingRefs().get(0).getReports()){
                if(r instanceof CheckReport){
                    CheckReport checkReport = (CheckReport) r;
                    if(!checkReport.getClientPdfPath().equals("")){
                        File file = new File(folder.concat(String.valueOf(separatorChar)).concat(checkReport.getClientPdfPath()));
                        if(!file.exists()){
                            return false;
                        } else {
                            try{
                                if(PdfHandler.getPageCount(file.getPath()) <=0){
                                    return false;
                                }
                            } catch (Exception ex){
                                ex.printStackTrace();
                                return false;
                            }
                        }
                    } else {
                        return false;
                    }
                }
            }
        }
        return true;
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
//                                        if(i.getPicture() != null){
//                                            i.getPicture().setItems(i);
//                                        }
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
        for(Report r : d.getReports()){
            if(r instanceof CheckReport){
                if(!((CheckReport) r).getClientPdfPath().equals("")){
                    FileHandler.fileDelete(((CheckReport) r).getClientPdfPath());
                }
            }
        }
    }

    public static void projectUpdate(Project project, RestTemplateHelper.HttpHelperResponse delegate){
        RestTemplateHelper restTemplateHelper = new RestTemplateHelper(delegate);
        UriBuilder uriBuilder = new UriBuilder();
        uriBuilder.setRequestCode(REST_PROJECTSAVE_KEY);
        uriBuilder.setProject(project);
        restTemplateHelper.execute(uriBuilder);
    }

    public static boolean isLastReport(Project project, String fileName){
        int n = project.getDrawingRefs().get(0).getReports().size();
        if(((CheckReport)project.getDrawingRefs().get(0).getReports().get(n - 1)).getServerPdfPath().equals(fileName)){
            return true;
        }
        return false;
    }
}
