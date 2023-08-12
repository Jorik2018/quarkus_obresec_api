package gob.regionancash.obresec.service;

import java.util.List;
import java.util.Map;
import org.isobit.util.AbstractFacadeLocal;

import gob.regionancash.obresec.model.Risk;

public interface RiskFacadeLocal extends AbstractFacadeLocal{

    public List<Risk> load(int first, int pageSize, String sortField, Map<String, Object> filters);

    void create(Risk risk);

    void edit(Risk risk);

    void remove(Risk risk);

    Risk find(Object id);

    List<Risk> findAll();

    List<Risk> findRange(int[] range);

    long count();

    public void remove(List<Risk> selectedList);

    public Risk load(Object id);

}