package gob.regionancash.obresec.controller;

import gob.regionancash.obresec.model.Crime;
import gob.regionancash.obresec.service.CrimeFacadeLocal;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.StreamingOutput;
import org.isobit.app.X;
import org.isobit.util.XFile;
import org.isobit.util.XMap;
import org.isobit.util.XUtil;
import static org.isobit.util.XUtil.toList;

@Path("crime")
public class CrimeFacadeREST{

    @Inject
    private CrimeFacadeLocal ejbFacade;

    @POST
    public Object create2(Crime entity) {
        ejbFacade.edit(entity);
        return entity.getId();
    }

    @PUT
    @Path("{id}")
    public void edit(@PathParam("id") Integer id, Crime entity) {
        ejbFacade.edit(entity);
    }

    @DELETE
    @Path("{id}")
    public void remove(@PathParam("id") Integer id) {
        Crime crime = ejbFacade.find(id);
        crime.setCanceled(true);
        ejbFacade.edit(crime);
    }

    @GET
    @Path("prepare")
    public Object prepare() {
        return new Crime();
    }

    @GET
    @Path("{id}")
    public Crime find(@PathParam("id") Integer id) {
        return ejbFacade.load(id);
    }

    @GET
    @Path("{from}/{to}")
    public Object load(
            @PathParam("from") Integer from, @PathParam("to") Integer to,
            @QueryParam("fecha") String date,
            @QueryParam("from") String from2, @QueryParam("to") String to2,
            @QueryParam("year") Integer year, @QueryParam("month") Integer month,
            @QueryParam("crimeType") String crimeType,
            @QueryParam("criminalSex") String criminalSex,
            @QueryParam("victimSex") String victimSex,
            @QueryParam("province") String province,
            @QueryParam("district") String district,
            @QueryParam("description") String description
    ) {
        HashMap m = new HashMap();
        if (!XUtil.isEmpty(date)) {
            m.put("date", date);
        }
        if (!XUtil.isEmpty(from2)) {
            m.put("from", from2);
        }
        if (!XUtil.isEmpty(to2)) {
            m.put("to", to2);
        }
        if (year != null) {
            m.put("year", year);
        }
        if (month != null) {
            m.put("month", month);
        }
        if (crimeType != null) {
            m.put("crimeType", toList(crimeType, (String o) -> Integer.parseInt(o.toString().trim())));
        }
        if (criminalSex != null) {
            m.put("criminalSex", toList(criminalSex, (String o) -> o.toString().charAt(0)));
        }
        if (victimSex != null) {
            m.put("victimSex", toList(victimSex, (String o) -> o.toString().charAt(0)));
        }
        if (province != null) {
            m.put("province", province);
        }
        if (district != null) {
            m.put("district", district);
        }
        if (description != null) {
            m.put("crime:description", description);
        }
        try {
            System.out.println("=========="+ejbFacade);
            m.put("data", ejbFacade.load(from, to, null, m));
        } catch (Exception e) {
            e.printStackTrace();
            m.put("error", e.toString());
        }
        return m;
    }

    static class DownloadForm implements Serializable {

        public DownloadForm() {
        }

        public String FORMAT;

        public String province;

        public String district;

        public Date from;

        public Date to;

    }

    @POST
    @Path("download")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response exportFile(Map params) throws IOException {
        String fileName = "data.jao";
        List data = new ArrayList();
        data.add(new Object[]{
            new Object[]{"ID", 40, Number.class.getName()},
            new Object[]{"CATEGORY", 80},
            new Object[]{"TYPE", 80},
            new Object[]{"DISTRICT_ID", 80},
            new Object[]{"DISTRICT", 80},
            new Object[]{"PROVINCE", 80},
            new Object[]{"DESCRIPTION", 200},
            new Object[]{"FECHA", 80, Date.class.getName()},
            new Object[]{"CRIMINAL_SEX", 70, Character.class.getName()},
            new Object[]{"CRIMINAL_AGE", 70, Number.class.getName()},
            new Object[]{"VICTIM_SEX", 70, Character.class.getName()},
            new Object[]{"VICTIM_AGE", 70, Number.class.getName()}
        });
        params.put("columns", "o.id,o.crimeType.crimeCategory.name,o.crimeType.name,o.districtId,d.name,p.name,o.description,o.fecha,"
                + "o.criminalSex,o.criminalAge,o.victimSex,o.victimAge");
        params.put("sorter", "o.id DESC");
        data.addAll(ejbFacade.load(0, 0, null, params));
        InputStream is2;
        XFile.saveObject(new File(File.createTempFile("temp-file-name", ".tmp").getParentFile(), fileName).getAbsolutePath(), data);
        if ("pdf".equals(params.get("FORMAT"))) {
            is2 = ClientBuilder.newClient().target("http://localhost/admin/jasper/api/export/" + fileName)
                    .request()
                    .post(Entity.text(""), InputStream.class);
            fileName = "repor.pdf";
        } else {
            is2 = ClientBuilder.newClient().target("http://localhost/xls/api/export/" + fileName)
                    .request()
                    .post(Entity.text(""), InputStream.class);
            fileName = "repor.xlsx";
        }
        return Response.ok((StreamingOutput) (java.io.OutputStream output) -> {
            try {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                int len;
                byte[] buffer = new byte[4096];
                while ((len = is2.read(buffer, 0, buffer.length)) != -1) {
                    output.write(buffer, 0, len);
                }
                output.flush();
                is2.close();
            } catch (IOException e) {
                throw new WebApplicationException("File Not Found !!", e);
            }
        }, MediaType.APPLICATION_OCTET_STREAM)
                .header("content-disposition", "attachment; filename = " + fileName
                ).build();
    }

}
