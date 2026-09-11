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
import gob.regionancash.content.model.Box;
import gob.regionancash.content.model.Block;
import gob.regionancash.content.model.NodeRevision;
import gob.regionancash.content.model.UrlAlias;
import gob.regionancash.content.dto.NodeDTO;

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

	@Inject
	EntityManager entityManager;

	@GET
	@Path("{id}")
	public Response load(@PathParam("id") Integer id) {

		Block b = Block.findById(id);

		if (b == null) {
			return Response
					.status(Response.Status.NOT_FOUND)
					.build();
		}

		if ("Block".equals(b.getModule())
				&& b.getDelta() != null) {

			Box box = Box.findById(
					Long.valueOf(b.getDelta()));

			b.setExt(box);
		}

		return Response.ok(b).build();
	}

	@GET
	@Path("/{from}/{to}")
	public Map<String, Object> page(
			@PathParam("from") int from,
			@PathParam("to") int size,
			@QueryParam("theme") String theme,
			@QueryParam("sortField") String sortField) {

		sortField = "region";

		theme = "garland";

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

}