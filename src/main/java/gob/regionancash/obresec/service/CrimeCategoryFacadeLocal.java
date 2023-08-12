package gob.regionancash.obresec.service;

import java.util.List;
import java.util.Map;
import gob.regionancash.obresec.model.CrimeCategory;
import org.isobit.util.AbstractFacadeLocal;

public interface CrimeCategoryFacadeLocal extends AbstractFacadeLocal{

    public List<CrimeCategory> load(int first, int pageSize, String sortField, Map<String, Object> filters);

    void create(CrimeCategory crimecategory);

    void edit(CrimeCategory crimecategory);

    void remove(CrimeCategory crimecategory);

    CrimeCategory find(Object id);

    List<CrimeCategory> findAll();

    List<CrimeCategory> findRange(int[] range);

    long count();

    public void remove(List<CrimeCategory> selectedList);

}