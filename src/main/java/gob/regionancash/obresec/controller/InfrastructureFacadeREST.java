package gob.regionancash.obresec.controller;

import gob.regionancash.obresec.model.Infrastructure;
import gob.regionancash.obresec.service.InfrastructureFacadeLocal;

import java.util.HashMap;
import jakarta.inject.Inject;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.QueryParam;

@Path("infrastructure")
public class InfrastructureFacadeREST{

    @Inject
    private InfrastructureFacadeLocal ejbFacade;

    @POST
    public void create(Infrastructure entity) {
        ejbFacade.edit(entity);
    }

    @PUT
    @Path("{id}")
    public void edit(@PathParam("id") Integer id, Infrastructure entity) {
        ejbFacade.edit(entity);
    }

    @DELETE
    @Path("{id}")
    public void remove(@PathParam("id") Integer id) {
        Infrastructure risk = ejbFacade.find(id);
        //risk.setCanceled(true);
        ejbFacade.edit(risk);
    }

    @GET
    @Path("prepare")
    public Object prepare() {
        return new Infrastructure();
    }

    @GET
    @Path("{id}")
    public Infrastructure find(@PathParam("id") Integer id) {
        return ejbFacade.load(id);
    }

    @GET
    @Path("{from}/{to}")
    public Object load(@PathParam("from") Integer from, @PathParam("to") Integer to,
             @QueryParam("province") String province,
             @QueryParam("district") String district, @QueryParam("description") String description) {
        HashMap m = new HashMap();
        if (province != null) {
            m.put("province", province);
        }
        if (district != null) {
            m.put("district", district);
        }
        if (description != null) {
            m.put("description", description);
        }
        try {
            m.put("data", ejbFacade.load(from, to, null, m));
        } catch (Exception e) {
            m.put("error", e.toString());
        }
        return m;
    }

}
