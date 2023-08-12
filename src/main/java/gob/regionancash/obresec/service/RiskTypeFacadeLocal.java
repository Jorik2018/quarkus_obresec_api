package gob.regionancash.obresec.service;

import java.util.List;
import java.util.Map;
import gob.regionancash.obresec.model.RiskType;
import org.isobit.util.AbstractFacadeLocal;

public interface RiskTypeFacadeLocal extends AbstractFacadeLocal{

    public List<RiskType> load(int first, int pageSize, String sortField, Map<String, Object> filters);

    void create(RiskType risktype);

    void edit(RiskType risktype);

    void remove(RiskType risktype);

    RiskType find(Object id);

    List<RiskType> findAll();

    List<RiskType> findRange(int[] range);

    long count();

    public void remove(List<RiskType> selectedList);

}