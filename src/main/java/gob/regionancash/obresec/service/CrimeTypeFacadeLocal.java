package gob.regionancash.obresec.service;

import java.util.List;
import java.util.Map;
import jakarta.ejb.Local;
import gob.regionancash.obresec.model.CrimeType;
import org.isobit.util.AbstractFacadeLocal;

@Local
public interface CrimeTypeFacadeLocal extends AbstractFacadeLocal{

    public List<CrimeType> load(int first, int pageSize, String sortField, Map<String, Object> filters);

    void create(CrimeType crimetype);

    void edit(CrimeType crimetype);

    void remove(CrimeType crimetype);

    CrimeType find(Object id);

    List<CrimeType> findAll();

    List<CrimeType> findRange(int[] range);

    long count();

    public void remove(List<CrimeType> selectedList);

}