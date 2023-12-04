package gob.regionancash.obresec.controller;

import gob.regionancash.obresec.model.RiskType;
import gob.regionancash.obresec.service.RiskTypeFacadeLocal;

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
import jakarta.ws.rs.core.MediaType;

@Path("risk/type")
public class RiskTypeFacadeREST {

    @Inject
    private RiskTypeFacadeLocal ejbFacade;

    @POST
    public void create(RiskType entity) {
        ejbFacade.edit(entity);
    }

    @PUT
    @Path("{id}")
    public void edit(@PathParam("id") Integer id, RiskType entity) {
        ejbFacade.edit(entity);
    }

    @DELETE
    @Path("{id}")
    public void remove(@PathParam("id") Integer id) {
        RiskType riskType=ejbFacade.find(id);
        riskType.setCanceled(true);
        ejbFacade.edit(riskType);
    }
    
    @GET
    @Path("prepare")
    public Object prepare() {
        return new RiskType();
    }

    @GET
    @Path("{id}")
    public RiskType find(@PathParam("id") Integer id) {
        return ejbFacade.find(id);
    }

    @GET
    @Path("{from}/{to}")
    public Object load(@PathParam("from") Integer from, @PathParam("to") Integer to) {
        HashMap m = new HashMap();
        try {
            m.put("data", ejbFacade.load(from, to, null, m));
        } catch (Exception e) {
            m.put("error", e.toString());
        }
        return m;
    }

}