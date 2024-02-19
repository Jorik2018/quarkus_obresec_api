package gob.regionancash.node.controller;

import gob.regionancash.node.model.MenuRouter;
import gob.regionancash.node.model.Node;
import gob.regionancash.node.model.NodeRevision;
import gob.regionancash.node.model.UrlAlias;
import gob.regionancash.obresec.model.CrimeCategory;
import gob.regionancash.obresec.service.CrimeCategoryFacadeLocal;
import io.quarkus.panache.common.Parameters;

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


@Path("node")
public class NodeFacadeREST {

    @GET
    public Object get(@QueryParam("dst") String dst) {
        UrlAlias urlAlias = UrlAlias.find("SELECT o FROM UrlAlias o WHERE o.dst=:dst", Parameters.with("dst", dst)).firstResult();
        if(urlAlias==null){
            MenuRouter menuRouter = MenuRouter.findById(dst);
            menuRouter.getFile();
            Node node = new Node();
            NodeRevision nodeRevision = new NodeRevision();
            nodeRevision.setBody(dst);
            nodeRevision.setTitle(menuRouter.getTitle());
            nodeRevision.setBody(menuRouter.getFile());
            node.setRevision(nodeRevision);
            return node;
        }
        String src = urlAlias.getSrc();
        String nid = src.split("/")[1];
        Node node = Node.findById(Integer.parseInt(nid));
        node.setRevision(NodeRevision.findById(node.getVid()));
        return node;
    }

}