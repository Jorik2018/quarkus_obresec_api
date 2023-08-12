package gob.regionancash.obresec.service;

import java.util.List;
import java.util.Map;
import jakarta.ejb.Local;
import gob.regionancash.obresec.model.ObrDirectory;
import org.isobit.util.AbstractFacadeLocal;

@Local
public interface ObrDirectoryFacadeLocal extends AbstractFacadeLocal{

    public List<ObrDirectory> load(int first, int pageSize, String sortField, Map<String, Object> filters);

    ObrDirectory load(Object id);
    
    void create(ObrDirectory obrdirectory);

    void edit(ObrDirectory obrdirectory);

    void remove(ObrDirectory obrdirectory);

    ObrDirectory find(Object id);

    List<ObrDirectory> findAll();

    List<ObrDirectory> findRange(int[] range);

    long count();

    public void remove(List<ObrDirectory> selectedList);

    public Object getPeople(int intValue);

  

    public void exec(String id);

    public List positionList();

}