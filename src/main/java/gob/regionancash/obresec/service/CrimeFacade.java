package gob.regionancash.obresec.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.Query;

import org.isobit.app.X;
import org.isobit.app.model.User;
import org.isobit.app.service.SessionFacade;
import org.isobit.app.service.UserService;
import org.isobit.directory.model.Country;
import org.isobit.directory.model.People;
import org.isobit.util.AbstractFacade;
import org.isobit.util.XDate;
import org.isobit.util.XUtil;

import gob.regionancash.obresec.model.Crime;
import java.util.Date;
import java.util.HashMap;

@ApplicationScoped
public class CrimeFacade extends AbstractFacade<Crime> implements CrimeFacadeLocal {

    @Inject
    private SessionFacade sessionFacade;

    @Inject
    private UserService userService;

    protected EntityManager getEntityManager() {
        return Crime.getEntityManager();
    }

    @Override
    public List load(int first, int pageSize, String sortField, Map<String, Object> filters) {
        Object last = XUtil.isEmpty(filters.get("last"), null);
        Object from = XUtil.isEmpty(filters.get("from"), null);
        Object columns = XUtil.isEmpty(filters.get("columns"), null);
        Object sorter = XUtil.isEmpty(filters.get("sorter"), null);
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
        Object criminalSex = XUtil.isEmpty(filters.get("criminalSex"), null);
        Object victimSex = XUtil.isEmpty(filters.get("victimSex"), null);
        if (crimeType != null) {
            List l = new ArrayList();
            if (crimeType instanceof List) {
                for (Object o : (List) crimeType) {
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
        EntityManager em = this.getEntityManager();
        User u = (User) userService.getCurrentUser();
        boolean OBRESEC_ADMIN_CRIME = userService.access(ObresecFacade.Perm.OBRESEC_ADMIN_CRIME);

        
        
        Date[] period = XDate.getPeriod((String) filters.get("date"));
        if (period != null) {
            System.out.println("from="+period[0]+" to="+period[0]);
            from = period[0];
            to = period[1];
        }
        if (from != null) {
            from = XDate.getDate((Date) from);
        }
        if (to != null) {
            to = XDate.getDate((Date) to);
        }
        ql.add(em.createQuery("SELECT " + (columns == null ? "o, "
                + (OBRESEC_ADMIN_CRIME ? "1" : "(case when o.uid=" + u.getUid()
                        + (u.getDependencyId() != null ? " OR o.dependencyId=" + u.getDependencyId() : "")
                        + (u.getDirectoryId() != null ? " OR o.directoryId=" + u.getDirectoryId() : "")
                        + " then 1 else 0 end)") : columns)
                + (sql = " FROM Crime o "
                +(columns!=null?" LEFT OUTER JOIN District d ON d.code=o.districtId LEFT JOIN d.province p ":"")
                + "WHERE o.canceled=FALSE "
                + (from != null ? " AND DATE(o.fecha)>=DATE(:from)" : "")
                + (last != null ? " AND DAYS_DIFF(o.fecha,:today)<:last" : "")
                + (description != null ? " AND UPPER(o.description) LIKE :description" : "")
                + (crimeType instanceof List ? " AND o.crimeTypeId IN :crimeType"
                        : (crimeType != null ? " AND o.crimeTypeId IN :crimeType" : ""))
                        
                        
                        
                   + (criminalSex != null ? " AND o.criminalSex IN :criminalSex" : "")   
                        + (victimSex != null ? " AND o.victimSex IN :victimSex" : "") 
                        
                + (to != null ? " AND DATE(o.fecha)<=DATE(:to)" : "")
                + (province instanceof List ? " AND substring(o.districtId,1,4) IN :province"
                        : (province != null || district != null ? " AND o.districtId LIKE :ubigeo" : "")))
                + "  ORDER BY " + (sorter != null ? sorter : "2 DESC,o.fecha DESC")));
        if (pageSize > 0) {
            ql.get(0).setFirstResult(first).setMaxResults(pageSize);
            ql.add(em.createQuery("SELECT COUNT(o) " + sql));
        }
        //+ (columns != null ? " LEFT JOIN District d ON d.code=o.district LEFT JOIN d.province p " : "")
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
            if (criminalSex != null) {
                q.setParameter("criminalSex", criminalSex);
            }
            if (victimSex != null) {
                q.setParameter("victimSex", victimSex);
            }
            if (description != null) {
                q.setParameter("description", "%" + description.toString().toUpperCase().replace(" ", "%") + "%");
            }
        }
        if (pageSize > 0) {
            filters.put("size", ql.get(1).getSingleResult());
        }
        List ids = new ArrayList();
        if (columns != null) {
            return ql.get(0).getResultList();
        }

        List<Crime> l = AbstractFacade.getColumn(ql.get(0).getResultList(), (Object[] row) -> {
            Crime c = (Crime) row[0];
            if (c.getDistrictId() != null) {
                ids.add(c.getDistrictId());
            }
            HashMap ext = new HashMap();
            System.out.println(")) " + row[1]);
            ext.put("own", row[1]);
            c.setExt(ext);
            return c;
        });
        if (!ids.isEmpty()) {
            Map m = new HashMap();
            ((List<Object[]>) getEntityManager().createQuery("SELECT d.code,d.name,p.code,p.name FROM District d JOIN d.province p WHERE d.code IN (:ids)")
                    .setParameter("ids", ids)
                    .getResultList()).forEach((r) -> {
                        m.put(r[0], r);
                    });
            l.forEach((c) -> {
                Map map = (Map) c.getExt();
                map.put("district", m.get(c.getDistrictId()));
            });
        }
        return l;
    }

    @Override
    public void edit(Crime entity) {
        User u = (User) sessionFacade.get(X.USER);
        if (entity.getUid() == 0) {
            entity.setUid(u.getUid());
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

    @Inject
    private RiskFacadeLocal riskFacade;

    @Override
    public Crime load(Object id) {
        EntityManager em = this.getEntityManager();
        Crime c = find(id instanceof Crime ? ((Crime) id).getId() : XUtil.intValue(id));
        if (c != null) {
            Map ext = (Map) c.getExt();
            if (ext == null) {
                c.setExt(ext = new HashMap());
            }
            if (c.getUid() > 0) {
                
                People people = em.find(People.class, c.getUid());
                if(people!=null){
                em.detach(people);
                //people.setDocument(null);
                ext.put("people", people);
                }
            }
            ext.put("province", (int) (XUtil.intValue(c.getDistrictId()) / 100));
            
            
            String districtId = String.format("%06d", XUtil.intValue(c.getDistrictId()));
            try {
                Object r[] = (Object[]) getEntityManager().createQuery("SELECT r.code,r.name,p.code,p.name,d.code,d.name FROM District d JOIN d.province p JOIN p.region r WHERE d.code=:district")
                        .setMaxResults(1)
                        .setParameter("district", districtId).getSingleResult();
                ext.put("provinceName",r[3]);
                ext.put("districtName",r[5]);
            } catch (NoResultException e) {
                
            }
            
            ext.put("category", c.getCrimeType().getCrimeCategoryId());
            if (c.getVictimCountry() != null) {
                ext.put("victimCountry", em.find(Country.class, c.getVictimCountry()));
            }
            if (c.getCriminalCountry() != null) {
                ext.put("criminalCountry", em.find(Country.class, c.getCriminalCountry()));
            }
        }
        return c;
    }

}
