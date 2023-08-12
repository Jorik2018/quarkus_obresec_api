package gob.regionancash.obresec.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import gob.regionancash.obresec.model.Crime;
import gob.regionancash.obresec.model.CrimeType;

import org.isobit.app.service.UserService;
import org.isobit.util.AbstractFacade;
import org.isobit.util.XUtil;

@ApplicationScoped
public class CrimeTypeFacade extends AbstractFacade<CrimeType> implements CrimeTypeFacadeLocal {

    @Inject
    private UserService userService;

    protected EntityManager getEntityManager() {
        return Crime.getEntityManager();
    }

    @Override
    public List<CrimeType> load(int first, int pageSize, String sortField, Map<String, Object> filters) {
        Object filter = XUtil.isEmpty(filters.get("filter"), null);
        Object category = XUtil.isEmpty(filters.get("category"), null);
        List<Query> ql = new ArrayList();

        String sql;
        EntityManager em = this.getEntityManager();
//                ql.add(em.createQuery("SELECT o FROM CrimeType o LEFT JOIN o.crimeCategory c WHERE 1=1 AND o.crimeCategoryId=:category  ORDER BY c.name,o.name ASC"));
        ql.add(em.createQuery("SELECT o " + (sql = "FROM CrimeType o LEFT JOIN o.crimeCategory c WHERE 1=1 "
                + (category != null ? " AND o.crimeCategoryId=:category" : "")) + "  ORDER BY c.name,o.name ASC"));
        if (pageSize > 0) {
            ql.get(0).setFirstResult(first).setMaxResults(pageSize);
            ql.add(em.createQuery("SELECT COUNT(o) " + sql));
        }
        for (Query q : ql) {
            if (category != null) {
                q.setParameter("category", XUtil.intValue(category));
            }
        }
        if (pageSize > 0) {
            filters.put("size", ql.get(1).getSingleResult());
        }
        return ql.get(0).getResultList();
    }

    @Override
    public void edit(CrimeType entity) {
        if (entity.getId() == null) {
            super.create(entity);
        } else {
            super.edit(entity);
        }
    }

}
