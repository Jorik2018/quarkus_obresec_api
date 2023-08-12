package gob.regionancash.obresec.service;

import java.util.List;
import java.util.Map;
import jakarta.ejb.Local;
import gob.regionancash.obresec.model.InfrastructureType;
import org.isobit.util.AbstractFacadeLocal;

@Local
public interface InfrastructureTypeFacadeLocal extends AbstractFacadeLocal{

    public List<InfrastructureType> load(int first, int pageSize, String sortField, Map<String, Object> filters);

    void create(InfrastructureType infrastructuretype);

    void edit(InfrastructureType infrastructuretype);

    void remove(InfrastructureType infrastructuretype);

    InfrastructureType find(Object id);

    List<InfrastructureType> findAll();

    List<InfrastructureType> findRange(int[] range);

    long count();

    public void remove(List<InfrastructureType> selectedList);

}