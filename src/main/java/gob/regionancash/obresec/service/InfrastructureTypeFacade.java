package gob.regionancash.obresec.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import jakarta.ejb.Stateless;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import gob.regionancash.obresec.model.InfrastructureType;
import org.isobit.util.AbstractFacade;
import org.isobit.util.XUtil;

@ApplicationScoped
@Stateless
public class InfrastructureTypeFacade extends AbstractFacade<InfrastructureType> implements InfrastructureTypeFacadeLocal{

    @Override
    public List<InfrastructureType> load(int first, int pageSize, String sortField, Map<String, Object> filters) {
        Object filter = XUtil.isEmpty(filters.get("filter"), null);
        List<Query> ql = new ArrayList();
        String sql;
        EntityManager em = this.getEntityManager();
        ql.add(em.createQuery("SELECT o " + (sql="FROM InfrastructureType o WHERE 1=1 "
                //+ (filter != null ? " AND UPPER(w.name) like :filter" : "")
        )+"  ORDER BY o.name ASC"));
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
    public void edit(InfrastructureType entity) {
        if (entity.getId() == null) {
            super.create(entity);
        } else {
            super.edit(entity);
        }
    }

}
