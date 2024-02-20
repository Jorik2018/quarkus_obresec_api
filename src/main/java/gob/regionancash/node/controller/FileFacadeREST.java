package gob.regionancash.node.controller;

import gob.regionancash.node.model.MenuRouter;
import gob.regionancash.node.model.Node;
import gob.regionancash.node.model.NodeRevision;
import gob.regionancash.node.model.UrlAlias;
import gob.regionancash.obresec.model.CrimeCategory;
import gob.regionancash.obresec.service.CrimeCategoryFacadeLocal;
import io.quarkus.panache.common.Parameters;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
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
import jakarta.ws.rs.core.MediaType;


@Path("file")
public class FileFacadeREST {

    @POST
    public Object get(Map m) {
        File directory = new File((String)m.get("folder"));
        ArrayList list = new ArrayList<>();
        // Get list of files and directories in the directory
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                list.add(Map.of("file", file.getAbsolutePath()));
            }
        }
        return Map.of("data", list);
    }

}