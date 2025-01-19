package gob.regionancash.node.controller;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.jboss.resteasy.reactive.PartType;

import jakarta.annotation.security.PermitAll;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("file")
public class FileFacadeREST {

    @POST
    public Object get(Map<String, Object> m) {
        File directory = new File((String)m.get("folder"));
        ArrayList<Map<String, Object>> list = new ArrayList<>();
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                list.add(Map.of("file", file.getAbsolutePath(),"type",file.isFile()?'F':'D'));
            }
        }
        return Map.of("data", list);
    }

    @POST
    @Path("download")
    @PermitAll
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response downloadFilePost(Map<String, Object> m) {
        return this.downloadFile((String)m.get("folder"));
    }

    @GET
    @Path("download")
    @PermitAll
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response downloadFile(@QueryParam("filename") String filename) {
        try {
            File file = new  File(filename);
            if(file.isDirectory()){
                file=compressTemporaryCompress(file);
            }
            FileInputStream fileInputStream = new FileInputStream(file);
            Response.ResponseBuilder responseBuilder = Response.ok(fileInputStream);
            responseBuilder.header("Content-Disposition", "attachment; filename=\"" + file.getName() + "\"");
            return responseBuilder.build();
        } catch (IOException e) {
            return Response.serverError().entity("Error downloading file: " + e.getMessage()).build();
        }
    }
    
    private File compressTemporaryCompress(File directory) throws IOException {
        // Create a temporary zip file
        File zipFile = File.createTempFile(directory.getName(), ".zip");

        try (FileOutputStream fos = new FileOutputStream(zipFile);
             ZipOutputStream zos = new ZipOutputStream(fos)) {
            zipDirectory(directory, directory.getName(), zos);
        }

        return zipFile;
    }

    private void zipDirectory(File folder, String parentFolder, ZipOutputStream zos) throws IOException {
        for (File file : folder.listFiles()) {
            if (file.isDirectory()) {
                zipDirectory(file, parentFolder + "/" + file.getName(), zos);
                continue;
            }

            zos.putNextEntry(new ZipEntry(parentFolder + "/" + file.getName()));

            try (FileInputStream fis = new FileInputStream(file)) {
                byte[] buffer = new byte[1024];
                int length;
                while ((length = fis.read(buffer)) > 0) {
                    zos.write(buffer, 0, length);
                }
            }

            zos.closeEntry();
        }
    }

    @POST
    @Path("upload")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Object upload(MultipartBody body) throws IOException {
        byte[] fileBytes = body.file.readAllBytes();
        String filePath = body.dst;
        Files.write(Paths.get(filePath), fileBytes);
        return Map.of("file",body.dst, "path", Paths.get(filePath).toFile().getAbsolutePath());
    }

    public static class MultipartBody {

        @FormParam("file")
        @PartType(MediaType.APPLICATION_OCTET_STREAM)
        public InputStream file;

        @FormParam("dst")
        @PartType(MediaType.TEXT_PLAIN)
        public String dst;

    }
    
}