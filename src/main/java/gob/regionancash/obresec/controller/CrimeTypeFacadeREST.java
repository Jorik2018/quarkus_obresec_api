package gob.regionancash.obresec.controller;

import gob.regionancash.obresec.model.CrimeType;
import gob.regionancash.obresec.service.CrimeTypeFacadeLocal;

import java.util.HashMap;

import jakarta.inject.Inject;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.QueryParam;

@Path("crime-type")
public class CrimeTypeFacadeREST{

    @Inject
    private CrimeTypeFacadeLocal ejbFacade;

    @POST
    public void create(CrimeType entity) {
        ejbFacade.edit(entity);
    }

    @PUT
    @Path("{id}")
    public void edit(@PathParam("id") Integer id, CrimeType entity) {
        ejbFacade.edit(entity);
    }

    @DELETE
    @Path("{id}")
    public void remove(@PathParam("id") Integer id) {
        //ejbFacade.delete(ejbFacade.find(id));
    }

    @GET
    @Path("prepare")
    public Object prepare() {
        return new CrimeType();
    }

    @GET
    @Path("{id}")
    public CrimeType find(@PathParam("id") Integer id) {
        return ejbFacade.find(id);
    }

    @GET
    @Path("{from}/{to}")
    public Object load(@PathParam("from") Integer from, @PathParam("to") Integer to,
            @QueryParam("category") String category) {
        HashMap m = new HashMap();
        if (category != null) {
            m.put("category", category);
        }
        try {
            m.put("data", ejbFacade.load(from, to, null, m));
        } catch (Exception e) {
            m.put("error", e.toString());
        }
        return m;
    }


}
