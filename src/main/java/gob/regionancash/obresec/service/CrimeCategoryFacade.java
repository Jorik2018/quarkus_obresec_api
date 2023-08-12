package gob.regionancash.obresec.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import gob.regionancash.obresec.model.Crime;
import gob.regionancash.obresec.model.CrimeCategory;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;

import org.isobit.app.service.UserService;
import org.isobit.util.AbstractFacade;
import org.isobit.util.XUtil;

@ApplicationScoped
public class CrimeCategoryFacade extends AbstractFacade<CrimeCategory> implements CrimeCategoryFacadeLocal{

    @Inject
    private UserService userService;

    protected EntityManager getEntityManager() {
        return Crime.getEntityManager();
    }


    @Override
    public List<CrimeCategory> load(int first, int pageSize, String sortField, Map<String, Object> filters) {
        Object filter = XUtil.isEmpty(filters.get("filter"), null);
        List<Query> ql = new ArrayList();
        String sql;
        EntityManager em = getEntityManager();
        ql.add(em.createQuery("SELECT o " + (sql="FROM CrimeCategory o WHERE o.canceled=FALSE "
                //+ (filter != null ? " AND UPPER(w.name) like :filter" : "")
        )+"  ORDER BY 1 ASC"));
        if (pageSize > 0) {
            ql.get(0).setFirstResult(first).setMaxResults(pageSize);
            ql.add(em.createQuery("SELECT COUNT(o) " + sql));
        }
        for (Query q : ql) {
            if (filter != null) {
                q.setParameter("filter", "%" + filter.toString().toUpperCase().replace(" ", "%") + "%");
            }
        }
        if (pageSize > 0) {
            filters.put("size", ql.get(1).getSingleResult());
        }
        return ql.get(0).getResultList();
    }

    @Override
    public void edit(CrimeCategory entity) {
        if (entity.getId() == null) {
            super.create(entity);
        } else {
            super.edit(entity);
        }
    }

}
