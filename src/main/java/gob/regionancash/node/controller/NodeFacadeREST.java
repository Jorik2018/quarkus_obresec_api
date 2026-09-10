package gob.regionancash.node.controller;

import java.util.HashMap;
import java.util.Map;
import java.util.List;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.core.Response;
import io.quarkus.panache.common.Parameters;
import gob.regionancash.node.model.MenuRouter;
import gob.regionancash.node.model.Node;
import gob.regionancash.node.model.NodeRevision;
import gob.regionancash.node.model.UrlAlias;
import gob.regionancash.node.dto.NodeDTO;
//import gob.regionancash.node.model.Upload;

@Path("node")
public class NodeFacadeREST {

public Response load(
            @PathParam("id") Integer id,
            @QueryParam("ref") String ref) {

        /*
         * Buscar node
         */
        Node node = Node.findById(id);

        if (node == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        /*
         * Cargar revisión actual
         */
        if (node.getVid() != null) {
            NodeRevision revision = NodeRevision.findById(node.getVid());
            node.setRevision(revision);
        }

        /*
         * Datos adicionales
         */
        Map<String, Object> ext = new HashMap<>();

        if (ref != null && !ref.isBlank()) {
            ext.put("ref", ref);
        }

        /*
         * Alias
         */
        List<UrlAlias> aliases = UrlAlias.list(
                "src = ?1",
                "node/" + node.getId());

        ext.put("urlAliasList", aliases);

        /*
         * URL principal del nodo
         */
        String url = aliases.stream()
                .map(UrlAlias::getDst)
                .filter(dst -> dst != null && !dst.isBlank())
                .findFirst()
                .orElse("node/" + node.getId());

        node.setUrl("/" + url);

        /*
         * Uploads
         */
        /*List<Upload> uploads = Upload.list(
                "uploadPK.vid = ?1 AND list > 0 ORDER BY weight",
                node.getVid());

        List<Upload> galleryList = new ArrayList<>();
        List<Upload> uploadList = new ArrayList<>();

        for (Upload upload : uploads) {
            if (upload.getList() > 1) {
                galleryList.add(upload);
            } else {
                uploadList.add(upload);
            }
        }*/

        /*
         * El código antiguo recorría desde atrás y luego hacía reverse().
         * Separándolo así conservamos directamente el ORDER BY weight.
         */
        //ext.put("galleryList", galleryList);
        //ext.put("uploadList", uploadList);

        /*
         * Comentarios
         */
        /*if (node.getComment() > 0) {

            List<Comment> comments = Comment.list(
                    "nid = ?1 ORDER BY timestamp",
                    node.getId());

            ext.put("comments", comments);

            ext.put(
                    "links",
                    new Object[][] {
                            { "Comentar", "/node/comment" }
                    });
        }*/

        node.setExt(ext);

        return Response.ok(new NodeDTO(node)).build();
    }

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
				return Map.of(
						"data", List.of(),
						"size", 0);
			}

			query.append(" AND id IN :nids");
			params.put("nids", nids);
		}

		/*
		 * Total SIN paginación
		 */
		long total = Node.count(
				query.toString(),
				params);

		/*
		 * Datos paginados
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
		 * Obtener aliases
		 */
		if (!nodes.isEmpty()) {

			List<String> srcList = nodes.stream()
					.map(node -> "node/" + node.getId())
					.toList();

			List<UrlAlias> aliases = UrlAlias.list(
					"src in ?1",
					srcList);

			Map<String, String> aliasMap = new HashMap<>();

			for (UrlAlias alias : aliases) {
				aliasMap.put(
						alias.getSrc(),
						alias.getDst());
			}

			for (Node node : nodes) {

				String src = "node/" + node.getId();

				String url = aliasMap.get(src);

				if (url == null || url.isBlank()) {
					url = src;
				}

				node.setUrl(
						"/" + url);
			}
		}

		/*
		 * Cargar revisión
		 */
		if (Boolean.TRUE.equals(details)
				&& !nodes.isEmpty()) {

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

		Map<String, Object> result = new HashMap<>();
		List<NodeDTO> data = nodes.stream().map(NodeDTO::new).toList();
		
		result.put("data", data);
		result.put("size", total);

		return result;
	}

	@GET
	public Object get(@QueryParam("dst") String dst) {
		UrlAlias urlAlias = UrlAlias
				.find("SELECT o FROM UrlAlias o WHERE o.dst=:dst", Parameters.with("dst", dst))
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