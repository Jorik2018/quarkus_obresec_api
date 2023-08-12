package gob.regionancash.obresec.service;

import java.util.List;
import java.util.Map;
import jakarta.ejb.Local;
import gob.regionancash.obresec.model.Infrastructure;
import org.isobit.util.AbstractFacadeLocal;

@Local
public interface InfrastructureFacadeLocal extends AbstractFacadeLocal{

    public List<Infrastructure> load(int first, int pageSize, String sortField, Map<String, Object> filters);

    void create(Infrastructure infrastructure);

    void edit(Infrastructure infrastructure);

    void remove(Infrastructure infrastructure);

    Infrastructure find(Object id);

    List<Infrastructure> findAll();

    List<Infrastructure> findRange(int[] range);

    long count();

    public void remove(List<Infrastructure> selectedList);

    public Infrastructure load(Integer id);

}