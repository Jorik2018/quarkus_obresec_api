package gob.regionancash.obresec.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import java.util.HashMap;
import jakarta.persistence.NoResultException;
import org.isobit.app.X;
import org.isobit.app.model.User;
import org.isobit.app.service.UserService;
import org.isobit.util.AbstractFacade;
import org.isobit.util.XUtil;
import gob.regionancash.obresec.model.Risk;

@ApplicationScoped
public class RiskFacade extends AbstractFacade<Risk> implements RiskFacadeLocal {

    @Inject
    private UserService userService;

    @Override
    public List<Risk> load(int first, int pageSize, String sortField, Map<String, Object> filters) {

        Object filter = XUtil.isEmpty(filters.get("filter"), null);
        System.out.println(filters);
        Object from = XUtil.isEmpty(filters.get("from"), null);
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
        Object riskType = XUtil.isEmpty(filters.get("riskType"), null);
        if (riskType != null && riskType.getClass().isArray()) {
            List l = new ArrayList();
            for (Object o : (Object[]) riskType) {
                l.add(XUtil.intValue(o));
            }
            riskType = l;
        }
        List<Query> ql = new ArrayList();
        String sql;
        EntityManager em = this.getEntityManager();

//        List<Object[]> l3 = em.createNativeQuery("SELECT p.codigo_prov,p.nombre_prov,d.codigo_dist,d.nombre_dist "
//                + "FROM drt_distrito d "
//                + "INNER JOIN drt_provincia p ON d.id_prov=p.id_prov AND d.id_dpto=p.id_dpto "
//                + "WHERE p.codigo_prov LIKE '02%' ORDER BY p.nombre_prov,d.nombre_dist").getResultList();
//        Map m3 = new HashMap();
//        List p = new ArrayList();
//        List p2;
//        for (Object[] r : l3) {
//            p2 = (List) m3.get(r[0]);
//            if (p2 == null) {
//                p.add(new Object[]{r[0], r[1]});
//                m3.put(r[0], p2=new ArrayList());
//            }
//            p2.add(new Object[]{r[0],r[2], r[3]});
//        }
//        System.out.println(X.gson.toJson(new XMap("province",p,"district",m3)));
        User u = (User) userService.getCurrentUser();
        boolean ADMIN_CORESEC_OWN_CRIME = userService.access("ADMIN_CORESEC_OWN_CRIME");
//        ql.add(em.createQuery("SELECT o FROM Risk o WHERE o.riskType.id");
        ql.add(em.createQuery("SELECT o, " + (ADMIN_CORESEC_OWN_CRIME ? "(case when o.userId=" + u.getDirectoryId()+ " then 1 else 0 end)" : "1") + (sql = " FROM Risk o WHERE o.canceled=0 "
                + (from != null ? " AND o.fechaIni>=:from" : "")
                + (to != null ? " AND o.fechaIni<=:to" : "")
                + (riskType != null ? " AND o.riskType.id IN :riskType" : "")
                + (province instanceof List ? " AND substring(o.districtId,1,4) IN :province"
                        : (province != null || district != null ? " AND o.districtId LIKE :ubigeo" : "")) //+ (filter != null ? " AND UPPER(w.name) like :filter" : "")
                ) + "  ORDER BY "+(ADMIN_CORESEC_OWN_CRIME ? "2 DESC," : "") +"1 DESC"));
        if (pageSize > 0) {
            ql.get(0).setFirstResult(first).setMaxResults(pageSize);
            ql.add(em.createQuery("SELECT COUNT(o) " + sql));
        }
        for (Query q : ql) {
            if (riskType instanceof List) {
                q.setParameter("riskType", riskType);
            }
            if (province instanceof List) {
                q.setParameter("province", province);
            } else if (district != null) {
                q.setParameter("ubigeo", district);
            } else if (province != null) {
                q.setParameter("ubigeo", province + "%");
            }
            if (to != null) {
                q.setParameter("to", to);
            }
            if (from != null) {
                q.setParameter("from", from);
            }
            if (filter != null) {
                q.setParameter("filter", "%" + filter.toString().toUpperCase().replace(" ", "%") + "%");
            }
        }
        if (pageSize > 0) {
            filters.put("size", ql.get(1).getSingleResult());
        }
        List<Risk> l = AbstractFacade.getColumn(ql.get(0).getResultList());
        List ids = new ArrayList();
        for (Risk c : l) {
            ids.add(c.getDistrictId());
        }
        if (!ids.isEmpty()) {
            Map m = new HashMap();
            for (Object[] r : (List<Object[]>) getEntityManager().createQuery("SELECT d.code,d.name,p.code,p.name FROM District d JOIN d.province p WHERE d.code IN (:ids)")
                    .setParameter("ids", ids)
                    .getResultList()) {
                m.put(r[0], r);
            }
            for (Risk c : l) {
                c.setExt(m.get(c.getDistrictId()));
            }
        }
        return l;
    }

    @Override
    public void edit(Risk entity) {
        User u = userService.getCurrentUser();
        System.out.println("u="+u);
        if (XUtil.intValue(entity.getUserId()) == 0) {
            entity.setUserId(u.getUid());
        }
        if (XUtil.intValue(entity.getDirectoryId()) == 0) {
            entity.setDirectoryId(u.getDirectoryId());
        }
        if (XUtil.intValue(entity.getDependencyId()) == 0) {
            entity.setDependencyId(u.getDependencyId());
        }
        if (entity.getFechaReg() == null) {
            entity.setFechaReg(X.getServerDate());
        }
        if (entity.getId() == null) {
            super.create(entity);
        } else {
            super.edit(entity);
        }
    }

    @Override
    public Risk load(Object id) {
        EntityManager em = getEntityManager();
        Risk c = find(id instanceof Risk ? ((Risk) id).getId() : XUtil.intValue(id));
        Map ext = (Map) c.getExt();
        if (ext == null) {
            c.setExt(ext = new HashMap());
        }
        if (c.getUserId() > 0) {
            User user = em.find(User.class, c.getUserId());
            if(user!=null){
                em.detach(user);
                //people.setDocument(null);
                ext.put("user", user);
            }
        }
        ext.put("province", (int) (XUtil.intValue(c.getDistrictId()) / 100));
        String districtId = String.format("%06d", XUtil.intValue(c.getDistrictId()));
        try {
            Object r[] = (Object[]) getEntityManager().createQuery("SELECT r.code,r.name,p.code,p.name,d.code,d.name FROM District d JOIN d.province p JOIN p.region r WHERE d.code=:district")
                    .setMaxResults(1)
                    .setParameter("district", districtId).getSingleResult();
            ext.put("provinceName", r[3]);
            ext.put("districtName", r[5]);
        } catch (NoResultException e) {

        }
        return c;
    }

}
