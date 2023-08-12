package gob.regionancash.obresec.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import gob.regionancash.obresec.model.Infrastructure;
import java.util.HashMap;
import org.isobit.app.X;
import org.isobit.app.service.UserService;
import org.isobit.util.AbstractFacade;
import org.isobit.util.XUtil;

@ApplicationScoped
public class InfrastructureFacade extends AbstractFacade<Infrastructure> implements InfrastructureFacadeLocal{

    @Inject
    private UserService userService;

    @Override
    public List<Infrastructure> load(int first, int pageSize, String sortField, Map<String, Object> filters) {
        Object filter = XUtil.isEmpty(filters.get("filter"), null);
        List<Query> ql = new ArrayList();
        String sql;
        EntityManager em = this.getEntityManager();
        ql.add(em.createQuery("SELECT o " + (sql="FROM Infrastructure o WHERE 1=1 "
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
        List<Infrastructure> l = ql.get(0).getResultList();
        List ids = new ArrayList();
        for (Infrastructure c : l) {
            ids.add(c.getDistrictId());
        }
        if (!ids.isEmpty()) {
            Map m = new HashMap();
            for (Object[] r : (List<Object[]>) getEntityManager().createQuery("SELECT d.code,d.name FROM District d WHERE d.code IN (:ids)")
                    .setParameter("ids", ids)
                    .getResultList()) {
                m.put(r[0], r[1]);
            }
            for (Infrastructure c : l) {
                //c.setExt(m.get(c.getDistrictId()));
            }
        }
        return l;
    }
    
    @Override
    public void edit(Infrastructure entity) {
        if (entity.getId() == null) {
            entity.setUserId(userService.getCurrentUser().getDirectoryId());
            entity.setFechaReg(X.getServerDate());
            super.create(entity);
        } else {
            super.edit(entity);
        }
    }

    @Override
    public Infrastructure load(Integer id) {
        return getEntityManager().find(Infrastructure.class, id);
    }

}
