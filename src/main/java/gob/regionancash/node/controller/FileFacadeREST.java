package gob.regionancash.node.controller;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.jboss.resteasy.reactive.PartType;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.InternalServerErrorException;
import jakarta.annotation.security.PermitAll;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Path("file")
public class FileFacadeREST {

    public static class FDR {

        public String folder;

    }



@DELETE
public Object delete(Map<String, Object> body) {
    return deleteFile(null, body);
}

@DELETE
@Path("{path:.+}")
public Object delete(
        @PathParam("path") String path,
        Map<String, Object> body) {

    return deleteFile(path, body);
}

private Object deleteFile(
        String pathParam,
        Map<String, Object> body) {

    String path = null;

    // 1. Preferir body
    if (body != null && body.get("path") != null) {
        path = body.get("path").toString();
    }

    // 2. Compatibilidad con el endpoint anterior
    if ((path == null || path.isBlank())
            && pathParam != null
            && !pathParam.isBlank()) {
        path = pathParam;
    }

    if (path == null || path.isBlank()) {
        throw new BadRequestException("Path is required");
    }

    File file = new File(path);

    if (!file.exists()) {
        throw new NotFoundException(
            "File not found: " + path
        );
    }

    if (!file.delete()) {
        throw new InternalServerErrorException(
            "Could not delete: " + path
        );
    }

    return Map.of(
        "path", path,
        "deleted", true
    );
}

    @POST
public Object get(Map<String, Object> m) {
    String f = (String) m.get("current");

    ArrayList<Map<String, Object>> list = new ArrayList<>();
    ArrayList<Map<String, Object>> parents = new ArrayList<>();

    if (f == null) {
        File[] drives = File.listRoots();

        if (drives != null) {
            for (File drive : drives) {
                list.add(Map.of(
                    "path", drive.getAbsolutePath(),
                    "type", 'D'
                ));
            }
        }

        return Map.of(
            "parents", parents,
            "data", list
        );
    }

    File directory = new File(f);

    File[] files = directory.listFiles();

    if (files != null) {
        for (File file : files) {
            list.add(Map.of(
                "path", file.getAbsolutePath(),
                "name", file.getName(),
                "type", file.isFile() ? 'F' : 'D',
                "length", file.length()
            ));
        }
    }

File current = directory;

while (current != null) {
    String path = current.getAbsolutePath();

    // D:/ o D:\ -> D:
    if (current.getParentFile() == null
            && path.matches("^[A-Za-z]:[/\\\\]$")) {
        path = path.substring(0, 2);
    }

    parents.add(
        0,
        Map.of(
            "name",
            current.getName().isEmpty()
                ? path
                : current.getName(),
            "path",
            path
        )
    );

    current = current.getParentFile();
}

    return Map.of(
        "parents", parents,
        "data", list
    );
}

    @POST
    @Path("download")
    @PermitAll
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response downloadFilePost(Map<String, Object> m) {
        return this.downloadFile((String) m.get("folder"));
    }

    @GET
    @Path("download")
    @PermitAll
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response downloadFile(@QueryParam("filename") String filename) {
        try {
            File file = new File(filename);
            if (file.isDirectory()) {
                file = compressTemporaryCompress(file);
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
        return Map.of("file", body.dst, "path", Paths.get(filePath).toFile().getAbsolutePath());
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