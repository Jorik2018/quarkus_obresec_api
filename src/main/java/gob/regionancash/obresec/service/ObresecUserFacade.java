package gob.regionancash.obresec.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import jakarta.ejb.Stateless;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import org.isobit.app.X;
import org.isobit.app.model.User;
import org.isobit.util.AbstractFacade;
import org.isobit.util.XUtil;

@Stateless
@ApplicationScoped
public class ObresecUserFacade {

    //@PersistenceContext(unitName = SYSTEM_UNIT_NAME)
    @PersistenceContext
    private EntityManager em;

    public List load(int first, int pageSize, String sortField, Map<String, Object> filters) {
        Object last = XUtil.isEmpty(filters.get("last"), null);
        Object from = XUtil.isEmpty(filters.get("from"), null);
        Object description = XUtil.isEmpty(filters.get("crime:description"), null);
        Object to = XUtil.isEmpty(filters.get("to"), null);
        Object district = XUtil.isEmpty(filters.get("district"), null);
        Object province = XUtil.isEmpty(filters.get("province"), null);
        if (province != null && province.getClass().isArray()) {
            List l = new ArrayList();
            for (Object o : (Object[]) province) {
                l.add(o.toString());
            }
            province = l;
        }
        Object crimeType = XUtil.isEmpty(filters.get("crimeType"), null);
        if (crimeType != null) {
            List l = new ArrayList();
            if (crimeType.getClass().isArray()) {
                for (Object o : (Object[]) crimeType) {
                    l.add(XUtil.intValue(o));
                }
                crimeType = l;
            } else {
                l.add(XUtil.intValue(crimeType));
            }
            crimeType = l;
        }
        List<Query> ql = new ArrayList();
        String sql;

        ql.add(em.createQuery("SELECT d,t,u "+(sql="FROM Dependency d JOIN d.type t JOIN User u ON u.dependencyId=d.id WHERE d.companyId=830")));
        if (pageSize > 0) {
            ql.get(0).setFirstResult(first).setMaxResults(pageSize);
            ql.add(em.createQuery("SELECT COUNT(d) " + sql));
        }
        for (Query q : ql) {
            if (crimeType != null) {
                q.setParameter("crimeType", crimeType);
            }
            if (province instanceof List) {
                q.setParameter("province", province);
            } else if (district != null) {
                System.out.println("districtId='" + district + "'");
                q.setParameter("ubigeo", district);
            } else if (province != null) {
                System.out.println("provinceId='" + province + "'");
                q.setParameter("ubigeo", province + "%");
            }
            if (to != null) {
                q.setParameter("to", to);
            }
            if (last != null) {
                q.setParameter("last", last).setParameter("today", X.getServerDate());
            }
            if (from != null) {
                q.setParameter("from", from);
            }
            if (description != null) {
                q.setParameter("description", "%" + description.toString().toUpperCase().replace(" ", "%") + "%");
            }
        }
        if (pageSize > 0) {
            filters.put("size", ql.get(1).getSingleResult());
        }
        List l = AbstractFacade.getColumn(ql.get(0).getResultList(), (Object[] row) -> {
            User user=(User) row[2];
            //user.setExt(row[0]);
            return user;
        });
        System.out.println("size=" + l.size());
        
        return l;
    }

}
