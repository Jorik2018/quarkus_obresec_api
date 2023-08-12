package gob.regionancash.obresec.controller;

import gob.regionancash.obresec.model.ObrDirectory;
import gob.regionancash.obresec.service.ObrDirectoryFacadeLocal;

import java.util.HashMap;
import java.util.List;
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
import jakarta.ws.rs.core.MediaType;

@Path("directory")
public class ObrDirectoryFacadeREST{

    @Inject
    private ObrDirectoryFacadeLocal obrDirectoryFacade;

    @POST
    public void create(ObrDirectory entity) {
        obrDirectoryFacade.edit(entity);
    }

    @PUT
    @Path("{id}")
    public void edit(@PathParam("id") Integer id, ObrDirectory entity) {
        obrDirectoryFacade.edit(entity);
    }

    @DELETE
    @Path("{id}")
    public void remove(@PathParam("id") Integer id) {
        obrDirectoryFacade.remove(obrDirectoryFacade.find(id));
    }

    @GET
    @Path("{id}")
    public ObrDirectory find(@PathParam("id") Integer id) {
        return obrDirectoryFacade.load(id);
    }

    @GET
    @Path("position")
    public List positionList() {
        return obrDirectoryFacade.positionList();
    }

    @GET
    public List<ObrDirectory> findAll() {
        return obrDirectoryFacade.findAll();
    }

    @GET
    @Path("{from}/{to}")
    public Object findRange(@PathParam("from") Integer from, @PathParam("to") Integer to,
            @QueryParam("company") String company,
            @QueryParam("code") String code,
            @QueryParam("mail") String mail,
            @QueryParam("people") String people,
            @QueryParam("query") String query,
            @QueryParam("sortBy") String sortBy) {
        HashMap m = new HashMap();
        if (company != null) {
            m.put("company", company);
        }
        if (people != null) {
            m.put("people", people);
        }
        if (code != null) {
            m.put("ruc", code);
        }
        if (query != null) {
            m.put("qs", query);
        }
        if (mail != null) {
            m.put("mail", mail);
        }
        List<ObrDirectory> l = obrDirectoryFacade.load(from, to, sortBy, m);
        m.put("data", l);
        return m;
    }

}
