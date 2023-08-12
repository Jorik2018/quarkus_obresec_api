package gob.regionancash.obresec.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import gob.regionancash.obresec.model.RiskType;

import org.isobit.app.service.UserService;
import org.isobit.util.AbstractFacade;
import org.isobit.util.XUtil;

@ApplicationScoped
public class RiskTypeFacade extends AbstractFacade<RiskType> implements RiskTypeFacadeLocal{

    @Inject
    private UserService userService;

    @Override
    public List<RiskType> load(int first, int pageSize, String sortField, Map<String, Object> filters) {
        Object filter = XUtil.isEmpty(filters.get("filter"), null);
        List<Query> ql = new ArrayList();
        String sql;
        EntityManager em = this.getEntityManager();
        ql.add(em.createQuery("SELECT o " + (sql="FROM RiskType o WHERE 1=1 AND o.canceled=0 "
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
    public void edit(RiskType entity) {
        if (entity.getId() == null) {
            super.create(entity);
        } else {
            super.edit(entity);
        }
    }

}
