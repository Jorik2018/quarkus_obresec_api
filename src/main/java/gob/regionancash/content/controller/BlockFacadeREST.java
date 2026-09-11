package gob.regionancash.content.controller;

import java.util.HashMap;
import java.util.Map;
import java.util.List;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.transaction.Transactional;
import io.quarkus.panache.common.Parameters;
import gob.regionancash.content.model.MenuRouter;
import gob.regionancash.content.model.Node;
import gob.regionancash.content.model.NodeRevision;
import gob.regionancash.content.model.UrlAlias;
import gob.regionancash.content.dto.NodeDTO;
//import gob.regionancash.content.model.Upload;

@Path("block")
public class BlockFacadeREST {

	private final Client client = ClientBuilder.newClient();

	@POST
	@Transactional
	public Response post(Node entity) {

		NodeRevision incomingRevision = entity.getRevision();

		if (incomingRevision == null || incomingRevision.getId() == null) {
			return Response
					.status(Response.Status.BAD_REQUEST)
					.entity("Revision inválida")
					.build();
		}

		NodeRevision revision = NodeRevision.findById(incomingRevision.getId());

		if (revision == null) {
			return Response
					.status(Response.Status.NOT_FOUND)
					.entity("Revision no encontrada")
					.build();
		}

		revision.setBody(
				sanitize(incomingRevision.getBody())[0]);

		return Response.ok(revision).build();
	}

	@GET
	@Path("{id}")
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
		/*
		 * List<Upload> uploads = Upload.list(
		 * "uploadPK.vid = ?1 AND list > 0 ORDER BY weight",
		 * node.getVid());
		 * 
		 * List<Upload> galleryList = new ArrayList<>();
		 * List<Upload> uploadList = new ArrayList<>();
		 * 
		 * for (Upload upload : uploads) {
		 * if (upload.getList() > 1) {
		 * galleryList.add(upload);
		 * } else {
		 * uploadList.add(upload);
		 * }
		 * }
		 */

		/*
		 * El código antiguo recorría desde atrás y luego hacía reverse().
		 * Separándolo así conservamos directamente el ORDER BY weight.
		 */
		// ext.put("galleryList", galleryList);
		// ext.put("uploadList", uploadList);

		/*
		 * Comentarios
		 */
		/*
		 * if (node.getComment() > 0) {
		 * 
		 * List<Comment> comments = Comment.list(
		 * "nid = ?1 ORDER BY timestamp",
		 * node.getId());
		 * 
		 * ext.put("comments", comments);
		 * 
		 * ext.put(
		 * "links",
		 * new Object[][] {
		 * { "Comentar", "/node/comment" }
		 * });
		 * }
		 */

		node.setExt(ext);

		return Response.ok(new NodeDTO(node)).build();
	}

	@Inject
	EntityManager entityManager;

	@GET
	@Path("/{from}/{to}")
	public Map<String, Object> page(
			@PathParam("from") int from,
			@PathParam("to") int size,
			@QueryParam("theme") String theme,
			@QueryParam("sortField") String sortField) {

		Map<String, Object> result = new HashMap<>();

		String jpql = "SELECT b FROM Block b WHERE b.theme = :theme";

		if (sortField != null && !sortField.isBlank()) {
			jpql += " ORDER BY b." + sortField;
		}

		List<Block> data = entityManager
				.createQuery(jpql, Block.class)
				.setParameter("theme", theme)
				.setFirstResult(from)
				.setMaxResults(size)
				.getResultList();

		Long total = entityManager
				.createQuery(
						"SELECT COUNT(b) FROM Block b WHERE b.theme = :theme",
						Long.class)
				.setParameter("theme", theme)
				.getSingleResult();

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

	private String[] sanitize(String body) {
		String url = "http://localhost/html/api";

		try {
			return client
					.target(url)
					.request()
					.post(
							Entity.text(body),
							String[].class);

		} catch (Exception e) {
			throw new RuntimeException(
					"No se pudo enviar al servicio '" + url + "'",
					e);
		}
	}

}