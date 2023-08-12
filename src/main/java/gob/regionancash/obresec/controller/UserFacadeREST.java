package gob.regionancash.obresec.controller;

import gob.regionancash.obresec.model.Risk;
import gob.regionancash.obresec.service.ObresecUserFacade;

import java.util.HashMap;
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

@Path("user")
public class UserFacadeREST{

    @Inject
    private ObresecUserFacade ejbFacade;

    /*@POST
    public void create(Risk entity) {
        ejbFacade.edit(entity);
    }*/

    @PUT
    @Path("{id}")
    public void edit(@PathParam("id") Integer id, Risk entity) {
//        ejbFacade.edit(entity);
    }

    @DELETE
    @Path("{id}")
    public void remove(@PathParam("id") Integer id) {
//        Risk risk = ejbFacade.find(id);
//        risk.setCanceled(true);
//        ejbFacade.edit(risk);
    }

    @GET
    @Path("prepare")
    public Object prepare() {
        return new Risk();
    }

//    @GET
//    @Path("{id}")
//    @Produces({APPLICATION_JSON})
//    public Risk find(@PathParam("id") Integer id) {
//        return ejbFacade.load(id);
//    }

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
