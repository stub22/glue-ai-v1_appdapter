package org.appdapter.lib.bind.jflux.services;

import java.util.HashMap;
import java.util.Map;
import org.appdapter.bind.rdf.jena.assembly.KnownComponentImpl;
import org.jflux.api.registry.Descriptor;
import org.jflux.api.registry.basic.BasicDescriptor;
import org.jflux.api.service.binding.ServiceBinding.BindingStrategy;

/**
 * The data object for the dependency binding properties of a service.
 *
 * @author Jason Randolph Eads <eadsjr@hansonrobokind.com>
 * @author Jason G. Pallack <jgpallack@gmail.com>
 */

public class ServiceBindingSpec extends KnownComponentImpl {
    // The stored data definitions
    
    private String myClassName;
    private Map<String, String> myProperties;
    private ServiceDependencySpec myServiceDependency;
    private BindingStrategy myBindingStrategy;
    
    public ServiceBindingSpec() {
        myProperties = new HashMap<String, String>();
    }
    
    // Getters for data
    
    public ServiceDependencySpec getServiceDependency() {
        return myServiceDependency;
    }
    
    public Descriptor getDescriptor() {
        return new BasicDescriptor(myClassName, myProperties);
    }
    
    public BindingStrategy getBindingStrategy() {
        return myBindingStrategy;
    }
    
    // Setters for data
    
    public void setClassName(String className) {
        myClassName = className;
    }
    
    public void setBindingStrategy(BindingStrategy bindingStrategy) {
        myBindingStrategy = bindingStrategy;
    }
    
    public void setServiceDependency(ServiceDependencySpec serviceDependency) {
        myServiceDependency = serviceDependency;
    }
    
    // Accumulators for data
    
    public void addProperty(String key, String value) {
        myProperties.put(key, value);
     }
     
    public void removeProperty(String key) {
        myProperties.remove(key);
    }
}
