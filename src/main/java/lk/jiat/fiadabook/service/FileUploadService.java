package lk.jiat.fiadabook.service;

import jakarta.servlet.ServletContext;
import lk.jiat.fiadabook.util.Env;
import org.apache.commons.io.FilenameUtils;
import org.glassfish.jersey.media.multipart.ContentDisposition;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FileUploadService {

    private static final String UPLOAD_DIRECTORY_NAME = "/uploads";
    private final ServletContext context;

    public FileUploadService(ServletContext context){
        this.context= context;
    }

    public FileItem uploadFile(String directoryName , InputStream inputStream , ContentDisposition fileMeataData){
        return writeFile(UPLOAD_DIRECTORY_NAME+"/"+directoryName,inputStream,fileMeataData);
    }

    private FileItem writeFile(String pathName , InputStream inputStream , ContentDisposition contentDisposition){
        Path uploadPath = Paths.get(context.getRealPath(pathName));
        String extension =  FilenameUtils.getExtension(contentDisposition.getFileName());
        String fileName = System.currentTimeMillis()+"."+extension;

        if(!Files.exists(uploadPath)){
            try {
                System.out.println("Upload path not Found. Creating directory :\"" +uploadPath+"\"");
                Files.createDirectories(uploadPath);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        try {
            int read;
            byte[] bytes = new byte[1024];

            OutputStream outputStream = new FileOutputStream(new java.io.File(uploadPath.toFile(), fileName));

            while((read = inputStream.read(bytes)) != -1){
                outputStream.write(bytes , 0 ,read);
            }
            outputStream.flush();
            outputStream.close();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        String relativePath = (pathName + "/" + fileName)
                .replace("\\", "/")
                .replaceAll("^/+", "");

        String appUrl = Env.getProperty("app.url");
        String url = context.getContextPath()+"/"+relativePath;
        String fullUrl = appUrl+"/"+relativePath;
        String path = relativePath;

        return new FileItem(fileName,contentDisposition.getFileName(),path,url,fullUrl);
    }

    public static class FileItem{
        private String fileName;
        private String originalFileName;
        private String filePath;
        private String url;
        private String fullUrl;

        public FileItem(String fileName, String originalFileName, String filePath, String url, String fullUrl) {
            this.fileName = fileName;
            this.originalFileName = originalFileName;
            this.filePath = filePath;
            this.url = url;
            this.fullUrl = fullUrl;
        }

        public String getFileName() {
            return fileName;
        }

        public void setFileName(String fileName) {
            this.fileName = fileName;
        }

        public String getOriginalFileName() {
            return originalFileName;
        }

        public void setOriginalFileName(String originalFileName) {
            this.originalFileName = originalFileName;
        }

        public String getFilePath() {
            return filePath;
        }

        public void setFilePath(String filePath) {
            this.filePath = filePath;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public String getFullUrl() {
            return fullUrl;
        }

        public void setFullUrl(String fullUrl) {
            this.fullUrl = fullUrl;
        }
    }

}