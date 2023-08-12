package gob.regionancash.obresec.service;

import java.util.List;
import java.util.Map;
import gob.regionancash.obresec.model.Crime;
import org.isobit.util.AbstractFacadeLocal;

public interface CrimeFacadeLocal extends AbstractFacadeLocal{

    public List load(int first, int pageSize, String sortField, Map<String, Object> filters);

    void create(Crime crime);

    void edit(Crime crime);

    void remove(Crime crime);

    Crime find(Object id);

    List<Crime> findAll();

    List<Crime> findRange(int[] range);

    long count();

    public void remove(List<Crime> selectedList);

    public Crime load(Object id);

}