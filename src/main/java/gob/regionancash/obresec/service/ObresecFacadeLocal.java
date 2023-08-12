package gob.regionancash.obresec.service;

import java.util.List;
import java.util.Map;
import jakarta.ejb.Local;
import org.isobit.util.AbstractFacadeLocal;

@Local
public interface ObresecFacadeLocal extends AbstractFacadeLocal{

    public List getMemberList();
    
    public Map getSummary(Map m);
    
    public void load();
    
}