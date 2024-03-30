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
import org.isobit.app.service.UserService;
import org.isobit.directory.model.Company;
import org.isobit.directory.model.Dependency;
import org.isobit.directory.model.People;
import org.isobit.directory.service.CompanyFacade;
import org.isobit.directory.service.PeopleFacade;
import org.isobit.util.AbstractFacade;
import org.isobit.util.XMap;
import org.isobit.util.XUtil;

import gob.regionancash.obresec.model.ObrDirectory;
import gob.regionancash.rh.model.Contract;
import gob.regionancash.rh.model.Position;

import java.util.HashMap;

@ApplicationScoped
public class ObrDirectoryFacade extends AbstractFacade<ObrDirectory> implements ObrDirectoryFacadeLocal {

    @Inject
    private UserService userService;

    @Override
    public List<ObrDirectory> load(int first, int pageSize, String sortField, Map<String, Object> filters) {
        Object filter = XUtil.isEmpty(filters.get("filter"), null);
        Object company = XUtil.isEmpty(filters.get("company"), null);
        Object people = XUtil.isEmpty(filters.get("people"), null);
        List<Query> ql = new ArrayList();
        String sql;
        EntityManager em = this.getEntityManager();
        ql.add(em.createQuery("SELECT o,p,po,d " + (sql = "FROM ObrDirectory o JOIN o.people p LEFT JOIN p.documentType d LEFT JOIN Contract po ON po.id=o.contractId WHERE 1=1 "
                + (company != null ? " AND UPPER(o.companyName) like :company" : "")
                + (people != null ? " AND (UPPER(p.fullName) like :people OR UPPER(p.code) like :people)" : ""))
                + "  ORDER BY 1 ASC"));
        if (pageSize > 0) {
            ql.get(0).setFirstResult(first).setMaxResults(pageSize);
            ql.add(em.createQuery("SELECT COUNT(o) " + sql));
        }
        for (Query q : ql) {
            if (filter != null) {
                q.setParameter("filter", "%" + filter.toString().toUpperCase().replace(" ", "%") + "%");
            }
            if (company != null) {
                q.setParameter("company", "%" + company.toString().toUpperCase().replace(" ", "%") + "%");
            }
            if (people != null) {
                q.setParameter("people", "%" + people.toString().toUpperCase().replace(" ", "%") + "%");
            }
        }
        if (pageSize > 0) {
            filters.put("size", ql.get(1).getSingleResult());
        }
        Map m = new HashMap();
        List<ObrDirectory> l = AbstractFacade.getColumn(ql.get(0).getResultList(), new RowAdapter() {
            @Override
            public Object adapting(Object[] row) {
                ObrDirectory o = (ObrDirectory) row[0];
                People people = o.getPeople();
                em.detach(people);
                em.detach(o);
                people.setDocumentType(null);
                if (row[2] != null) {
                    Contract c = (Contract) row[2];
                    if (c != null) {
                        String p = null;
                        if (c.getPositionId() != null) {
                            Position position = em.find(Position.class, c.getPositionId());
                            if (position != null) {
                                p = position.getName();
                            }
                        }
                        if (c.getDependencyId() != null) {
                            Dependency dependency = em.find(Dependency.class, c.getDependencyId());
                            if (dependency != null) {
                                p = p == null ? dependency.getFullName() : (p + " " + dependency.getFullName());
                            }
                        }
                        o.setPosition(p);
                    }
                }
                m.put("" + o.getCompanyId(), null);
                return o;
            }
        });
        if (!m.isEmpty()) {
            for (Company pj : (List<Company>) em.createQuery("SELECT p FROM Company p WHERE p.ruc IN :ruc")
                    .setParameter("ruc", new ArrayList(m.keySet())).getResultList()) {
                m.put(pj.getRuc(), pj);
            }
            for (ObrDirectory o : l) {
                o.setCompany((Company) m.get("" + o.getCompanyId()));
            }
        }
        return l;
    }

    @Inject
    private PeopleFacade peopleFacade;

    @Inject
    private CompanyFacade companyFacade;

    @Override
    public void edit(ObrDirectory entity) {
        EntityManager em = this.getEntityManager();
        Map ext = null;//(Map) entity.getExt();
        People people = entity.getPeople();
        
        //No sera posible poner null en people
        if (people != null) {
            if (XUtil.booleanValue(ext.containsKey("editPeople"))) {
                //Se deberia usar una copia y solo cambiar los campos que se desee 
                if (people.getId() != null) {
                    People p = em.find(People.class, people.getId());

                }
                peopleFacade.edit(people);
            }
            entity.setPeople(people);
        }
        Company company = entity.getCompany();
        if (company != null) {
            if (XUtil.booleanValue(ext.containsKey("editCompany"))) {
                if (company.getId() != null) {
                    Company c = em.find(Company.class, company.getId());
                    c.setAddress(company.getAddress());
                    c.setRuc(company.getRuc());
                    c.setBusinessName(company.getBusinessName());
                    company = c;
                }
                //Se deberia usar una copia y solo cambiar los campos que se desee 
                companyFacade.edit(company);
            }
            entity.setCompanyName(company.getBusinessName());
            entity.setCompanyId(Long.parseLong(company.getRuc()));
        }
        Contract contract = entity.getContract();
        Contract originalContract;
        //Debe crearse solo si cumple cierta informacion
        if (entity.getContractId() == null) {
            originalContract = new Contract();
        } else {
            originalContract = em.find(Contract.class, entity.getContractId());
        }
        if (originalContract == null) {
            originalContract = new Contract();
        }
        originalContract.setCompanyId(company.getId());
        if (contract != null) {
            Dependency dependency = contract.getDependency();
            if (dependency != null) {
                if (dependency.getId() == null) {
                    dependency.setStatus("1");
                    //dependency.setFechaReg(X.getServerDate());
                    em.persist(dependency);
                }
                originalContract.setDependencyId(dependency.getId());
            } else {
                originalContract.setDependencyId(contract.getDependencyId());
            }
            originalContract.setPositionId(contract.getPositionId());
            Position position = contract.getPosition();
            if (position != null && position.getId() == null) {
                try {
                    position = em.createQuery("SELECT p FROM Position p WHERE UPPER(p.name)=:name", Position.class).setMaxResults(1).setParameter("name", position.getName().toUpperCase()).getSingleResult();
                } catch (NoResultException nre) {
                    em.persist(position);
                }
                originalContract.setPositionId(position.getId());
            }
        }
        originalContract.setPeopleId(people.getId());
        originalContract.setCompanyId(company.getId());
        //Si es nuevo se inserta los datos con los campos creados
        if (originalContract.getId() == null) {
            
            //originalContract.setFechaIni(X.getServerDate());
            //originalContract.setFechaReg(X.getServerDate());
            User user = userService.getCurrentUser();
            People peopleUser = user.getDirectoryId() != null ? em.find(People.class, user.getDirectoryId()) : null;
            if (peopleUser != null) {
                int peopleId = XUtil.intValue(peopleUser.getCode());
                originalContract.setUserId(peopleId > 0 ? peopleId : -user.getDirectoryId());
            } else {
                originalContract.setUserId(0);
            }
            em.persist(originalContract);
        } else {
            em.merge(originalContract);
        }
        entity.setContractId(originalContract.getId());
        if (entity.getId() == null) {
            super.create(entity);
        } else {
            super.edit(entity);
        }
        
        if (XUtil.booleanValue(ext.get("createAccount"))) {
            User u = userService.getUserByDir(people.getId());
            People p = entity.getPeople();
            Object pass = ext.get("pass");
            Object confirm = ext.get("pass");
            if (u == null) {
                u = new User();
                u.setStatus((short) 1);
                u.setName(entity.getPeople().getCode());
                if (!XUtil.isEmpty(p.getMail())) {
                    u.setMail(p.getMail());
                } else {
                    u.setMail(u.getName() + "@mail.org");
                }
            }
            if (pass == null) {
                pass = u.getName();
                confirm = u.getName();
            }
            /*u.setExt(new XMap(
                    "clave", pass,
                    "confirm", confirm,
                    "people", p
            ));*/
            u.setStatus((short) (XUtil.booleanValue(ext.get("status")) ? 1 : 0));
            userService.edit(u);
        } else {
            User user = userService.getUserByDir(people.getId());
            if (user != null) {
                /*user.setExt(new XMap(
                        "clave", ext.get("pass"),
                        "confirm", ext.get("confirm")
                ));*/
                user.setStatus((short) (XUtil.booleanValue(ext.get("status")) ? 1 : 0));
                userService.edit(user);
            }
            //Si mando clave es porq ya existe pero se actualizara
        }
    }

    @Override
    public ObrDirectory load(Object o) {
        EntityManager em = this.getEntityManager();
        ObrDirectory d = (ObrDirectory) ((Object[]) em.createQuery("SELECT c,p,d FROM ObrDirectory c JOIN c.people p LEFT JOIN p.document d WHERE c.id=:id")
                .setParameter("id", o instanceof ObrDirectory ? ((ObrDirectory) o).getId() : XUtil.intValue(o))
                .getSingleResult())[0];
        List<Company> l = companyFacade.load(0, 0, null, new XMap("ruc", "" + d.getCompanyId()));
        if (!l.isEmpty()) {
            d.setCompany(l.get(0));
        }
        if (d.getPeople().getDocumentType() != null) {
            d.getPeople().getDocumentType().getId();
        }
        Map ext = new HashMap();
        if (d.getContractId() != null) {
            Contract contract = em.find(Contract.class, d.getContractId());
            d.setContract(contract);
            if (contract != null) {
                String jurisdictionId = contract.getJurisdictionId();
                if (!XUtil.isEmpty(jurisdictionId)) {
                    int p = XUtil.intValue(jurisdictionId);
                    ext.put("idDpto", (int) p / 10000);
                    ext.put("idProv", ((int) p / 100) % 100);
                    ext.put("jurisdiction", jurisdictionId);
                }
//            ext.put("position", em.find(Position.class,contract.getPositionId()));
                ext.put("positionId", contract.getPositionId());
                ext.put("dependencyId", contract.getDependencyId());
            }
        }
        List<Object[]> l2 = this.getEntityManager().createQuery("SELECT COUNT(u),MAX(u.status) FROM User u WHERE u.idDir=:peopleId").setParameter("peopleId", d.getPeople().getId()).getResultList();
        if (!l2.isEmpty()) {
            ext.put("hasAccount", XUtil.intValue(l2.get(0)[0]) > 0);
            ext.put("status", XUtil.intValue(l2.get(0)[1]) > 0);
        }
        //d.setExt(ext);
        return d;
    }

    @Override
    public Object getPeople(int people) {
        List<Object[]> l = this.getEntityManager().createQuery("SELECT p,u FROM People p LEFT OUTER JOIN User u ON p.numeroPndid=u.name WHERE p.id=:people")
                .setParameter("people", people)
                .getResultList();
        if (!l.isEmpty()) {
            Object[] a = l.get(0);
            People p = (People) a[0];
           // p.setExt(new XMap("user", a[1]));
            return p;
        } else {
            return null;
        }
    }

    @Override
    public void exec(String id) {
        this.getEntityManager().createQuery(id);
    }

    @Override
    public List positionList() {
        return getEntityManager().createQuery("SELECT DISTINCT p FROM ObrDirectory d INNER JOIN Contract c ON c.id=d.contractId INNER JOIN Position p ON p.id=c.positionId").getResultList();
    }

}
