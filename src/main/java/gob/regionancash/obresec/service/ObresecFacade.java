package gob.regionancash.obresec.service;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import jakarta.annotation.PostConstruct;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.isobit.app.X;
import org.isobit.app.service.UserService;
import org.isobit.util.AbstractFacade;
import org.isobit.util.XDate;
import org.isobit.util.XUtil;

import gob.regionancash.rh.model.Contract;


public class ObresecFacade extends AbstractFacade<Object> implements ObresecFacadeLocal
//, BlockFacadeLocal.BlockModule 
        {

    @Inject
    private UserService userService;

    @Override
    public void load() {
        //X.getRequest().setAttribute(X.TEMPLATE, "/template.xhtml");
    }

    @Override
    public Map getSummary(Map m) {
        String ubigeo = (String) m.get("ubigeo");
        Object last = m.get("last");
        Object year = m.get("year");
        Object from = m.get("from");
        Object to = m.get("to");
        if (from instanceof Number) {
            from = new Date(((Number) from).longValue());
        } else if (from instanceof String) {
            from = XDate.parseDate((String) from);
        }
        Object month = m.get("month");
        if (from != null) {
            from = XDate.getDate((Date) from);
        } else if (month == null) {
            Calendar c = Calendar.getInstance();
            c.setTime(X.getServerDate());
            c.set(Calendar.DAY_OF_MONTH, 1);
            from = c.getTime();
        }
        if (to instanceof Number) {
            to = new Date(((Number) to).longValue());
        } else if (to instanceof String) {
            to = XDate.parseDate((String) to);
        }
        if (to != null) {
            to = XDate.getDate((Date) to);
        }
        EntityManager em = getEntityManager();
        if (m.containsKey("crime")) {
            Query q1 = em.createQuery("SELECT t.id,t.name,count(c.id),t.color,t.shape FROM Crime c JOIN c.crimeType t WHERE c.canceled=0 " + (from != null ? " AND fecha>=:from" : "") + (to != null ? " AND fecha<=:to" : "") + (month != null ? " AND MONTH(fecha)=:month" : "") + (year != null ? " AND YEAR(fecha)=:year " : "") + (last != null ? " AND DAYS_DIFF(c.fecha,:today)<:last " : "") + (ubigeo != null ? " AND c.districtId LIKE :ubigeo" : "") + " GROUP BY t.id,t.name,t.color,t.shape ORDER BY 3 DESC");

            if (ubigeo != null) {
                q1.setParameter("ubigeo", ubigeo + "%");
            }
            if (last != null) {
                q1.setParameter("last", last).setParameter("today", X.getServerDate());
            }
            if (from != null) {
                q1.setParameter("from", from);
            }
            if (to != null) {
                q1.setParameter("to", to);
            }
            if (year != null) {
                q1.setParameter("year", XUtil.intValue(year));
            }
            if (month != null) {
                q1.setParameter("month", XUtil.intValue(month));
            }
            int t = 0;
            List<Object[]> rl = q1.getResultList();
            for (Object[] r : rl) {
                t += XUtil.intValue(r[2]);
            }
            m.put("total", t);
            m.put("crimeSummary", rl);
            if (m.get("summary") == null) {
                ubigeo = null;//aqui se procura q la cantidad de sea la misma sin considerar el ubigeo
                q1 = em.createQuery("SELECT c,t FROM Crime c JOIN c.crimeType t WHERE canceled=0 "
                        + (from != null ? " AND fecha>=:from" : "")
                        + (to != null ? " AND fecha<=:to" : "")
                        + (month != null ? " AND MONTH(fecha)=:month" : "")
                        + (year != null ? " AND YEAR(fecha)=:year " : "")
                        + (last != null ? " AND DAYS_DIFF(c.fecha,:today)<:last " : "")
                        + (ubigeo != null ? " AND c.districtId LIKE :ubigeo" : ""));
                if (ubigeo != null) {
                    q1.setParameter("ubigeo", ubigeo + "%");
                }
                if (last != null) {
                    q1.setParameter("last", last).setParameter("today", X.getServerDate());
                }
                if (from != null) {
                    q1.setParameter("from", from);
                }
                if (to != null) {
                    q1.setParameter("to", to);
                }
                if (year != null) {
                    q1.setParameter("year", XUtil.intValue(year));
                }
                if (month != null) {
                    q1.setParameter("month", XUtil.intValue(month));
                }
                m.put("crimeList", AbstractFacade.getColumn(q1.getResultList()));
            }
        }
        if (m.containsKey("risk")) {
            ubigeo = (String) m.get("ubigeo");
            Query q2 = getEntityManager().createQuery("SELECT t.id,t.name,count(c.id),t.color,t.shape FROM Risk c JOIN c.type t WHERE 1=1 "
                    + (from != null ? " AND (c.fechaFin>=:from OR c.fechaFin IS NULL) " : "")
                    + (to != null ? " AND c.fechaIni<=:to" : "")
                    + (ubigeo != null ? " AND c.districtId LIKE :ubigeo" : "")
                    + " GROUP BY t.id,t.name,t.color,t.shape ORDER BY 3 DESC");
            if (from != null) {
                q2.setParameter("from", from);
            }
            if (to != null) {
                q2.setParameter("to", to);
            }
            if (ubigeo != null) {
                q2.setParameter("ubigeo", ubigeo + "%");
            }
            int t = 0;
            List<Object[]> rl = q2.getResultList();
            for (Object[] r : rl) {
                t += XUtil.intValue(r[2]);
            }
            m.put("total", t);
            m.put("riskSummary", rl);
            ubigeo = null;
            if (m.get("summary") == null) {
                q2 = getEntityManager().createQuery("SELECT c FROM Risk c JOIN c.type t WHERE 1=1 "
                    + (from != null ? " AND (c.fechaFin>=:from OR c.fechaFin IS NULL) " : "")
                    + (to != null ? " AND c.fechaIni<=:to" : "")
                        + (ubigeo != null ? " AND c.districtId LIKE :ubigeo" : "")
                        + " ORDER BY t.name");
                if (from != null) {
                    q2.setParameter("from", from);
                }
                if (to != null) {
                    q2.setParameter("to", to);
                }
                if (ubigeo != null) {
                    q2.setParameter("ubigeo", ubigeo + "%");
                }
                m.put("riskList", q2.getResultList());
            }
        }
        return m;
    }

    public enum Perm {
        ACCESS_CORESEC,
        ADMIN_CORESEC,
        ACCESS_CORESEC_CRIME,
        OBRESEC_ADMIN_CRIME,
        ADMIN_CORESEC_OWN_CRIME,
        ACCESS_CORESEC_CRIMETYPE,
        ADMIN_CORESEC_CRIMETYPE,
        ACCESS_OBRESEC_CRIMECATEGORY,
        ADMIN_OBRESEC_CRIMECATEGORY,
        ACCESS_CORESEC_RISK,
        ADMIN_CORESEC_RISK,
        ADMIN_CORESEC_OWN_RISK,
        ACCESS_CORESEC_RISKTYPE,
        ADMIN_CORESEC_RISKTYPE,
        ACCESS_CORESEC_INFRASTRUCTURE,
        ADMIN_CORESEC_INFRASTRUCTURE,
        ACCESS_CORESEC_INFRASTRUCTURETYPE,
        ADMIN_CORESEC_INFRASTRUCTURETYPE,
        ACCESS_CORESEC_MEMBER,
        ACCESS_CORESEC_OBRDIRECTORY,
        ADMIN_CORESEC_OBRDIRECTORY
    };

    /*@Override
    public Object getBlock(HttpServletRequest request, String op, Object delta) {
        if ("list".equals(op)) {
            Map blocks = (Map) request.getAttribute("#blocks");
            blocks.put("crimes", new XMap("info", "Coresec"));
            return blocks;
        } else if ("view".equals(op)) {
            if (userService.access(Perm.ACCESS_CORESEC)) {
                return new XMap("title", "Coresecs", "src", "/obresec/block/Block.xhtml");
            }
        }
        return null;

    }*/

    public List getMemberList() {
        EntityManager em = getEntityManager();
        List<Contract> l = em.createQuery("SELECT c FROM Contract c WHERE c.dependency.id=:idDep")
                .setParameter("idDep", 24)
                .getResultList();
        List<Contract> l2 = em.createQuery("SELECT c FROM Contract c WHERE NOT c.dependency.idDep=:idDep OR c.dependency IS NULL")
                .setParameter("idDep", 24)
                .getResultList();
        Map m = new HashMap();
        for (Contract c : l2) {
            m.put(c.getPeopleId(), c);
        }
        for (Contract c : l) {
            //c.setExt(m.get(c.getPeopleId()));
        }
        return l;
    }

    @PostConstruct
    public void init() {
        add(this);
        /*add(new RoleFacade.RoleModule() {
            @Override
            public Object[] getPerms() {
                return Perm.values();
            }
        });*/
        /*add(new UserFacadeLocal.UserModule() {
            @Override
            public User login(String name, String pass, Map m) {
                return null;
            }

            @Override
            public User authenticateFinalize(User user) {
//                ((Map) user.getExt()).put("dependency", getEntityManager().createQuery("SELECT DISTINCT d FROM Contract c JOIN c.dependency d WHERE c.people.idDir=:people")
//                        .setParameter("people", user.getIdDir()).getResultList());
                return user;
            }

            @Override
            public User password(Map m) {
                return null;
            }

            @Override
            public void loadPerm(User user, List perms) {
                System.out.println("user=" + user + "; perms=" + perms);
                EntityManager em = getEntityManager();
                List roles = new ArrayList();
                for (Object[] o : (List<Object[]>) em.createQuery("SELECT UPPER(p.name),UPPER(d.acronym) FROM Contract c LEFT JOIN Dependency d ON d.id=c.dependencyId LEFT JOIN Position p ON p.id=c.positionId WHERE c.peopleId=:peopleId AND c.active=1")
                        .setParameter("peopleId", user.getIdDir()).getResultList()) {
                    if ("COPROSEC".equals(o[1])) {
                        o[1] = "COPROSEC";
                        //En este caso es importante cargar las juridicciones 
                    }
                    roles.add(o[0] + " " + o[1]);
                    System.out.println(o[0] + " " + o[1]);
                }
                System.out.println("user.ext=" + user.getExt());
                for (Object perm
                        : em.createQuery("SELECT DISTINCT p.perm FROM Role r JOIN r.druPermissionCollection p WHERE UPPER(r.name) IN (:roles)")
                                .setParameter("roles", roles)
                                .getResultList()) {
                    if (!perms.contains(perm)) {
                        System.out.println("Add perm > " + perm);
                        perms.add(perm);
                    }
                }
            }

            @Override
            public User logout(User user) {
                return user;
            }
        });*/

    }

}
