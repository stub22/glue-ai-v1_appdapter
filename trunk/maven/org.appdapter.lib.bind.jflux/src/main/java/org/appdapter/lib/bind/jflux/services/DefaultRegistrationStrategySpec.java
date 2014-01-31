package org.appdapter.lib.bind.jflux.services;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.appdapter.bind.rdf.jena.assembly.KnownComponentImpl;

/**
 *
 * @author Jason G. Pallack <jgpallack@gmail.com>
 */


public class DefaultRegistrationStrategySpec extends KnownComponentImpl {
    private List<String> myClassNames;
    private Map<String, String> myRegistrationProperties;
    
    public DefaultRegistrationStrategySpec() {
        myClassNames = new ArrayList<String>();
        myRegistrationProperties = new HashMap<String, String>();
    }
    
    public String[] getClassNames() {
        return myClassNames.toArray(new String[myClassNames.size()]);
    }
    
    public Map<String, String> getRegistrationProperties() {
        return myRegistrationProperties;
    }
    
    public void addClassName(String className) {
        myClassNames.add(className);
    }
    
    public void removeClassName(String className) {
        myClassNames.remove(className);
    }
    
    public void addProperty(String key, String value) {
        myRegistrationProperties.put(key, value);
    }
    
    public void removeProperty(String key) {
        myRegistrationProperties.remove(key);
    }
}
