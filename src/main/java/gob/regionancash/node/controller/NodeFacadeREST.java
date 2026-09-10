package gob.regionancash.node.controller;

import java.util.HashMap;
import java.util.Map;
import java.util.List;
import gob.regionancash.node.model.MenuRouter;
import gob.regionancash.node.model.Node;
import gob.regionancash.node.model.NodeRevision;
import gob.regionancash.node.model.UrlAlias;
import io.quarkus.panache.common.Parameters;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.PathParam;

@Path("node")
public class NodeFacadeREST {

    @GET
    @Path("{from}/{to}")
    public Object page(
            @PathParam("from") int from,
            @PathParam("to") int size,
            @QueryParam("title") String title,
            @QueryParam("dst") String dst,
            @QueryParam("details") Boolean details) {

        StringBuilder query = new StringBuilder("1 = 1");

        Map<String, Object> params = new HashMap<>();

        /*
         * Filtrar por título de NodeRevision.
         */
        if (title != null && !title.isBlank()) {

            query.append("""
                         AND vid IN (
                             SELECT nr.id
                             FROM NodeRevision nr
                             WHERE UPPER(nr.title) LIKE :title
                         )
                    """);

            params.put(
                    "title",
                    "%" + title.toUpperCase() + "%");
        }

        /*
         * Filtrar por URL alias.
         */
        if (dst != null && !dst.isBlank()) {

            List<UrlAlias> aliases = UrlAlias.list(
                    "LOWER(dst) LIKE ?1",
                    dst.toLowerCase() + "%");

            List<Integer> nids = aliases.stream()
                    .map(UrlAlias::getSrc)
                    .filter(src -> src != null &&
                            src.startsWith("node/"))
                    .map(src -> src.substring("node/".length()))
                    .map(Integer::valueOf)
                    .toList();

            if (nids.isEmpty()) {
                return List.of();
            }

            query.append(" AND id IN :nids");

            params.put(
                    "nids",
                    nids);
        }

        /*
         * Consulta de Node.
         */
        List<Node> nodes = Node
                .find(
                        query + " ORDER BY id DESC",
                        params)
                .range(
                        from,
                        from + size - 1)
                .list();

        /*
         * Cargar revisión si details=true.
         */
        if (details!=null&&details && !nodes.isEmpty()) {

            List<Integer> vids = nodes.stream()
                    .map(Node::getVid)
                    .filter(v -> v != null)
                    .toList();

            if (!vids.isEmpty()) {

                List<NodeRevision> revisions = NodeRevision.list(
                        "id in ?1",
                        vids);

                Map<Integer, NodeRevision> revisionsById = new HashMap<>();

                for (NodeRevision revision : revisions) {
                    revisionsById.put(
                            revision.getId(),
                            revision);
                }

                for (Node node : nodes) {
                    node.setRevision(
                            revisionsById.get(
                                    node.getVid()));
                }
            }
        }

        return nodes;
    }

    @GET
    public Object get(@QueryParam("dst") String dst) {
        UrlAlias urlAlias = UrlAlias.find("SELECT o FROM UrlAlias o WHERE o.dst=:dst", Parameters.with("dst", dst))
                .firstResult();
        if (urlAlias == null) {
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