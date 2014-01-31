package org.appdapter.lib.bind.jflux.services;

import java.util.HashMap;
import java.util.Map;
import org.appdapter.bind.rdf.jena.assembly.KnownComponentImpl;
import org.jflux.api.service.ServiceDependency.Cardinality;
import org.jflux.api.service.ServiceDependency.UpdateStrategy;

/**
 *
 * @author Jason G. Pallack <jgpallack@gmail.com>
 */


public class ServiceDependencySpec extends KnownComponentImpl {
    private Cardinality myCardinality;
    private UpdateStrategy myUpdateStrategy;
    private String myClassName;
    private String myName;
    private Map<String, String> myProperties;
    
    public ServiceDependencySpec() {
        myProperties = new HashMap<String, String>();
    }
    
    public Cardinality getCardinality() {
        return myCardinality;
    }
    
    public UpdateStrategy getUpdateStrategy() {
        return myUpdateStrategy;
    }
    
    public String getClassName() {
        return myClassName;
    }

    public String getName() {
        return myName;
    }
    
    public Map<String, String> getProperties() {
        return myProperties;
    }
    
    public void setCardinality(Cardinality cardinality) {
        myCardinality = cardinality;
    }
    
    public void setUpdateStrategy(UpdateStrategy updateStrategy) {
        myUpdateStrategy = updateStrategy;
    }
    
    public void setClassName(String className) {
        myClassName = className;
    }
    
    public void setName(String name) {
        myName = name;
    }
    
    public void addProperty(String key, String value) {
        myProperties.put(key, value);
    }
    
    public void removeProperty(String key) {
        myProperties.remove(key);
    }
}
