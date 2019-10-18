package com.richard.weger.wqc.helper;

import android.content.res.Resources;
import android.os.AsyncTask;

import com.richard.weger.wqc.R;
import com.richard.weger.wqc.domain.CheckReport;
import com.richard.weger.wqc.domain.DrawingRef;
import com.richard.weger.wqc.domain.Item;
import com.richard.weger.wqc.domain.ItemReport;
import com.richard.weger.wqc.domain.Mark;
import com.richard.weger.wqc.domain.Page;
import com.richard.weger.wqc.domain.ParamConfigurations;
import com.richard.weger.wqc.domain.Part;
import com.richard.weger.wqc.domain.Project;
import com.richard.weger.wqc.domain.Report;
import com.richard.weger.wqc.domain.dto.FileDTO;
import com.richard.weger.wqc.rest.RequestParameter;
import com.richard.weger.wqc.rest.RestTemplateHelper;
import com.richard.weger.wqc.rest.entity.EntityRestTemplateHelper;
import com.richard.weger.wqc.rest.file.FileRestTemplateHelper;
import com.richard.weger.wqc.result.AbstractResult;
import com.richard.weger.wqc.result.EmptyResult;
import com.richard.weger.wqc.result.ErrorResult;
import com.richard.weger.wqc.service.FileRequestParametersResolver;
import com.richard.weger.wqc.service.ProjectRequestParametersResolver;
import com.richard.weger.wqc.util.App;
import com.richard.weger.wqc.util.LoggerManager;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static com.richard.weger.wqc.appconstants.AppConstants.DRAWING_NUMBER_KEY;
import static com.richard.weger.wqc.appconstants.AppConstants.PART_NUMBER_KEY;
import static com.richard.weger.wqc.appconstants.AppConstants.PROJECT_NUMBER_KEY;
import static com.richard.weger.wqc.appconstants.AppConstants.REST_GENPICTUREDOWNLOAD_KEY;
import static com.richard.weger.wqc.appconstants.AppConstants.REST_GENPICTURESREQUEST_KEY;
import static com.richard.weger.wqc.appconstants.AppConstants.REST_GENPICTUREUPLOAD_KEY;
import static com.richard.weger.wqc.appconstants.AppConstants.REST_ITEMPICTURESREQUEST_KEY;
import static com.richard.weger.wqc.appconstants.AppConstants.REST_PDFDOCUMENTSREQUEST_KEY;
import static com.richard.weger.wqc.appconstants.AppConstants.REST_PDFREPORTDOWNLOAD_KEY;
import static com.richard.weger.wqc.appconstants.AppConstants.REST_PICTUREUPLOAD_KEY;
import static com.richard.weger.wqc.appconstants.AppConstants.REST_QRPROJECTLOAD_KEY;
import static java.io.File.separatorChar;

public class ProjectHelper {

    private static String qrCode;

    private static ParamConfigurations conf;

    private static Logger logger = LoggerManager.getLogger(ProjectHelper.class);

    public static AbstractResult setQrCode(String qrCode) {
        Map<String, String> values = new QrTextHelper(conf).execute(qrCode);
        if(values == null){
            return new ErrorResult(ErrorResult.ErrorCode.QR_TRANSLATION_FAILED,
                    App.getContext().getResources().getString(R.string.invalidQrCodeString),
                    ErrorResult.ErrorLevel.SEVERE);
        }
        ProjectHelper.qrCode = qrCode;
        return new EmptyResult();
    }

    public static ParamConfigurations getConf() {
        return conf;
    }

    public static void setConf(ParamConfigurations conf) {
        ProjectHelper.conf = conf;
    }

    public static Project getProject(String qrCode, ParamConfigurations conf){
        Map<String, String> values = new QrTextHelper(conf).execute(qrCode);

        Project project = new Project();
        project.setReference(values.get(PROJECT_NUMBER_KEY));

        DrawingRef dRef = new DrawingRef();
        dRef.setDnumber(Integer.valueOf(Objects.requireNonNull(values.get(DRAWING_NUMBER_KEY))));
        dRef.setParent(project);

        Part part = new Part();
        part.setNumber(Integer.valueOf(Objects.requireNonNull(values.get(PART_NUMBER_KEY))));
        part.setParent(dRef);

        project.getDrawingRefs().add(dRef);
        dRef.getParts().add(part);

        return project;
    }

    public static Project getProject(){
        Map<String, String> values = new QrTextHelper(conf).execute(getQrCode());

        Project project = new Project();
        project.setReference(values.get(PROJECT_NUMBER_KEY));

        DrawingRef dRef = new DrawingRef();
        dRef.setDnumber(Integer.valueOf(Objects.requireNonNull(values.get(DRAWING_NUMBER_KEY))));
        dRef.setParent(project);

        Part part = new Part();
        part.setNumber(Integer.valueOf(Objects.requireNonNull(values.get(PART_NUMBER_KEY))));
        part.setParent(dRef);

        project.getDrawingRefs().add(dRef);
        dRef.getParts().add(part);

        return project;
    }

    public static void projectLoad(RestTemplateHelper.RestResponseHandler handler){
        projectLoad(handler,true);
    }

    public static void projectLoad(RestTemplateHelper.RestResponseHandler handler, boolean toggleWaitScreen){
        StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
        StackTraceElement caller = stackTraceElements[5];
        logger.info("Started project load request routine (Caller: " + caller.getClassName() + "." + caller.getMethodName() + ":" + caller.getLineNumber() + ").");
        Resources r = App.getContext().getResources();
        String message = String.format(r.getConfiguration().getLocales().get(0), "%s, %s",
                r.getString(R.string.projectLoadingMessage),
                r.getString(R.string.pleaseWaitMessage).toLowerCase());
        if(toggleWaitScreen) {
            ActivityHelper.setHandlerWaitingLayout(handler, message);
        } else {
            ActivityHelper.disableHandlerControls(handler, true);
        }
        ProjectRequestParametersResolver resolver = new ProjectRequestParametersResolver(REST_QRPROJECTLOAD_KEY, getConf(), true);
        resolver.getEntity(getProject(), handler);
    }

    // superFolder = "Originals/"
    public static void byteArrayToFile(byte[] bytes, List<RequestParameter> params, String superFolder) {
        String qrcode = params.stream().filter(p -> p.getName().equals("qrcode")).map(RequestParameter::getValue).findFirst().orElse(null);
        String fileName = params.stream().filter(p -> p.getName().equals("filename")).map(RequestParameter::getValue).findFirst().orElse(null);
        if(qrcode != null && fileName != null) {
            String folder = StringHelper.getProjectFolderPath(qrcode);
            if(folder != null){
                folder = folder.concat(superFolder);
            }
            String filePath = folder + "/" + fileName;
            if (bytes != null) {
                FileHelper.byteArray2File(filePath, bytes);
            } else {
                logger.warning("Empty byte array received instead of a valid file content. This can be the result of a trial to access a file that doesn't exists at the server.");
            }
        } else {
            logger.warning("Invalid qrcode / filename parameter sent to server through a get request. qrcode=" + qrcode + ", filename=" + fileName);
        }
    }

    public static List<Item> getReportItems(Project project) {
        List<Item> ret = project.getDrawingRefs().stream()
                .flatMap(d -> d.getReports().stream()
                        .filter(r -> r instanceof ItemReport)
                        .map(r -> (ItemReport) r)
                        .flatMap(r -> r.getItems().stream()))
                .collect(Collectors.toList());
        return ret;
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
                        File f = new File(filePath);

                    } else {
                        picList.add(item);
                    }
                }
            }
        }
        return picList;
    }

    public List<String> neededReportFiles(Project project) {
        List<String> ret;

        ret = project.getDrawingRefs().stream()
                .flatMap(d -> d.getReports().stream()
                        .filter(r -> r instanceof CheckReport)
                        .map(r -> (CheckReport) r)
                        .map(CheckReport::getFileName))
                .collect(Collectors.toList());

        return ret;
    }

    public static int validReportFilesCount(Project project){
        int count = 0;
        String folder = StringHelper.getProjectFolderPath(project);
        if(folder != null) {
            folder = folder.concat("Originals/");
            if(project != null){
                for(Report r : project.getDrawingRefs().get(0).getReports()){
                    if(r instanceof CheckReport){
                        CheckReport checkReport = (CheckReport) r;
                        File file = new File(folder.concat(String.valueOf(separatorChar)).concat(checkReport.getFileName()));
                        if(file.exists()) {
                            try{
                                if(PdfHelper.getPageCount(file.getPath()) > 0){
                                    count++;
                                }
                            } catch (Exception ex){
                                logger.warning(ex.getMessage());
                            }
                        }
                    }
                }
            }
        }
        return count;
    }

    public static void linkReferences(Project project){
        if(project != null){
            for(DrawingRef d : project.getDrawingRefs()){
                if(d != null){
                    d.setParent(project);
                    for(Report r : d.getReports()){
                        if(r != null){
                            r.setParent(d);
                            if(r instanceof ItemReport){
                                for(Item i : ((ItemReport)r).getItems()){
                                    if(i != null){
                                        i.setParent(r);
                                        if(i.getPicture() != null){
                                            i.getPicture().setParent(i);
                                        }
                                    }
                                }
                            } else if(r instanceof CheckReport){
                                for(Page pg : ((CheckReport) r).getPages()){
                                    if(pg != null){
                                        pg.setParent(r);
                                        for(Mark m : pg.getMarks()){
                                            m.setParent(pg);
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

    public static void getPdfsList(RestTemplateHelper.RestResponseHandler delegate) {
        FileRequestParametersResolver resolver = new FileRequestParametersResolver(REST_PDFDOCUMENTSREQUEST_KEY, delegate);
        resolver.getPdfsList(getQrCode());
    }

    public static void getItemPicturesList(RestTemplateHelper.RestResponseHandler delegate) {
        FileRequestParametersResolver resolver = new FileRequestParametersResolver(REST_ITEMPICTURESREQUEST_KEY, delegate);
        resolver.getItemPicturesList(getQrCode());
    }

    public static void getGenPicturesList(RestTemplateHelper.RestResponseHandler delegate){
        FileRequestParametersResolver resolver = new FileRequestParametersResolver(REST_GENPICTURESREQUEST_KEY, delegate);
        resolver.getGeneralPicturesList(getQrCode());
    }

    private static boolean shouldDeletePicture(List<FileDTO> pictures, File f, String suffixToTest) {
        return pictures.stream().noneMatch(p -> p.getFileName().equals(f.getName())) && (suffixToTest == null || f.getName().contains(suffixToTest));
    }

    public static List<String> getObsoletePdfDocuments(List<FileDTO> resourcesToCheck, Project project) {
        return findObsoleteResources(resourcesToCheck, StringHelper.getPdfsFolderPath(project), null);
    }

    public static List<String> getObsoleteItemPictures(List<FileDTO> resourcesToCheck, Project project) {
        return findObsoleteResources(resourcesToCheck, StringHelper.getPicturesFolderPath(project), "Q");
    }

    public static List<String> getObsoleteGenPictures(List<FileDTO> resourcesToCheck, Project project){
        return findObsoleteResources(resourcesToCheck, StringHelper.getPicturesFolderPath(project), "QP");
    }

    private static List<String> findObsoleteResources(List<FileDTO> resourcesToCheck, String rootFolder, String suffixToTest){
        List<String> toDownload = new ArrayList<>();
        if(resourcesToCheck != null) {
            for (FileDTO dto : resourcesToCheck) {
                String fileName = dto.getFileName();
                boolean isNeeded = true;
                if (fileName.length() > 0) {
                    File picturesFolder = new File(rootFolder);
                    if (picturesFolder.exists()) {
                        for (File f : picturesFolder.listFiles()) {
                            if (f.getName().contains(fileName)) {
                                isNeeded = f.length() != dto.getFileSize();
                                break;
                            } else if (shouldDeletePicture(resourcesToCheck, f, suffixToTest)) {
                                FileHelper.fileDelete(f.getAbsolutePath());
                            }
                        }
                    }
                    if (isNeeded) {
                        if (fileName.contains("/")) {
                            fileName = fileName.substring(fileName.lastIndexOf("/") + 1);
                        }
                        toDownload.add(fileName);
                    }
                }
            }
        }
        return toDownload;
    }

    public static void getItemPictures(List<String> fileNames, List<FileRestTemplateHelper> queue, RestTemplateHelper.RestResponseHandler delegate) {
        for(String fileName : fileNames) {
            FileRequestParametersResolver resolver = new FileRequestParametersResolver(REST_GENPICTUREDOWNLOAD_KEY, delegate);
            FileRestTemplateHelper helper = resolver.getPicture(fileName, getQrCode());
            queue.add(helper);
        }
    }

    public static void getGenPictures(List<String> fileNames, List<FileRestTemplateHelper> queue, RestTemplateHelper.RestResponseHandler delegate) {
        for(String fileName : fileNames) {
            FileRequestParametersResolver resolver = new FileRequestParametersResolver(REST_GENPICTUREDOWNLOAD_KEY, delegate);
            FileRestTemplateHelper helper = resolver.getPicture(fileName, getQrCode());
            queue.add(helper);
        }
    }

    public static void getPdfDocuments(List<String> fileNames, List<FileRestTemplateHelper> queue, RestTemplateHelper.RestResponseHandler delegate) {
        for (String fileName : fileNames) {
            FileRequestParametersResolver resolver = new FileRequestParametersResolver(REST_PDFREPORTDOWNLOAD_KEY, delegate);
            FileRestTemplateHelper helper = resolver.getPdf(fileName, getQrCode());
            queue.add(helper);
        }
    }

    public static boolean hasPendingTasks(List<EntityRestTemplateHelper> entityHelperQueue, List<FileRestTemplateHelper> fileHelperQueue, boolean ignoreLast){
        boolean hasPending;
        int limit = 0;

        if(ignoreLast) {
            if(entityHelperQueue != null && fileHelperQueue != null){
                limit = 1;
            } else {
                limit = 1;
            }
        }
        removeFinishedTasks(entityHelperQueue, fileHelperQueue);
        hasPending = (entityHelperQueue != null && entityHelperQueue.size() > limit) || (fileHelperQueue != null && fileHelperQueue.size() > limit);

        return hasPending;
    }

    private static void removeFinishedTasks(List<EntityRestTemplateHelper> entityHelperQueue, List<FileRestTemplateHelper> fileHelperQueue){
        if(entityHelperQueue != null) {
            for (int i = 0; i < entityHelperQueue.size(); i++) {
                EntityRestTemplateHelper r = entityHelperQueue.get(i);
                if (r.getStatus() == AsyncTask.Status.FINISHED || r.isCancelled() || r.getStatus() == AsyncTask.Status.PENDING) {
                    entityHelperQueue.remove(r);
                }
            }
        }
        if(fileHelperQueue != null) {
            for (int i = 0; i < fileHelperQueue.size(); i++) {
                FileRestTemplateHelper r = fileHelperQueue.get(i);
                if (r.getStatus() == AsyncTask.Status.FINISHED || r.isCancelled() || r.getStatus() == AsyncTask.Status.PENDING) {
                    fileHelperQueue.remove(r);
                }
            }
        }
    }

    public static int getCurrentPicNumber(Project project){
        File folder = new File(StringHelper.getPicturesFolderPath(project));
        int currentPicNumber = Integer.MAX_VALUE;
        if(folder.exists() && folder.listFiles().length > 0) {
            List<Integer> qpIdList = new ArrayList<>();
            Arrays.stream(folder.listFiles())
                    .filter(f -> f.getName().contains("QP"))
                    .map(File::getName)
                    .mapToInt(n -> Integer.valueOf(n.substring(n.lastIndexOf("QP") + 2).replace(".jpg","").replace("_new","")))
                    .forEach(qpIdList::add);
            currentPicNumber = qpIdList.stream().mapToInt(i -> i).max().orElse(currentPicNumber);
            if(currentPicNumber < Integer.MAX_VALUE){
                currentPicNumber++;
            }
//            currentPicNumber += Arrays.stream(folder.listFiles()).filter(f -> f.getName().contains("QP")).count();
        }
        return currentPicNumber;
    }

    public static void itemPictureUpload(RestTemplateHelper.RestResponseHandler delegate, Item item, Project project){
        logger.info("Started picture upload request");

        String picName = item.getPicture().getFileName();
        picName = picName.substring(picName.lastIndexOf("/") + 1);

        FileRequestParametersResolver resolver = new FileRequestParametersResolver(REST_PICTUREUPLOAD_KEY, delegate);
        resolver.uploadItemPicture(picName, StringHelper.getQrText(project), item.getId());

    }

    public static void generalPictureUpload(RestTemplateHelper.RestResponseHandler delegate, Project project, String picName, List<FileRestTemplateHelper> queue){

        FileRequestParametersResolver resolver = new FileRequestParametersResolver(REST_GENPICTUREUPLOAD_KEY, delegate);
        FileRestTemplateHelper helper = resolver.uploadGeneralPicture(picName, StringHelper.getQrText(project));
        queue.add(helper);

    }

    public static String getQrCode() {
        return qrCode;
    }

    public static boolean shouldRefresh(Report report, Long itemId, Long parentId){
        boolean ret;

        if(report == null || itemId == null || parentId == null){
            return false;
        }

        ret = report.getId().equals(itemId);
        ret |= report.getId().equals(parentId);
        ret |= report.getChildren().stream().anyMatch(c -> c.getId().equals(itemId) || c.getId().equals(parentId));

        return ret;
    }
}
