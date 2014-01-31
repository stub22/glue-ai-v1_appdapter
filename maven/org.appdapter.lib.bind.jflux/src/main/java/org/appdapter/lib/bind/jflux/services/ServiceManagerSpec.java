package org.appdapter.lib.bind.jflux.services;

import java.util.HashMap;
import java.util.Map;
import org.appdapter.bind.rdf.jena.assembly.KnownComponentImpl;

/**
 *
 * @author Jason G. Pallack <jgpallack@gmail.com>
 */

public class ServiceManagerSpec extends KnownComponentImpl {
    private String myLifecycleClassName;
    private Map<String, ServiceBindingSpec> myServiceBindings;
    private DefaultRegistrationStrategySpec myServiceRegistration;
    private DefaultRegistrationStrategySpec myManagerRegistration;
    
    public ServiceManagerSpec() {
        myServiceBindings = new HashMap<String, ServiceBindingSpec>();
        myManagerRegistration = null;
    }
    
    public String getLifecycleClassName() {
        return myLifecycleClassName;
    }
    
    public Map<String, ServiceBindingSpec> getServiceBindings() {
        return myServiceBindings;
    }
    
    public DefaultRegistrationStrategySpec getServiceRegistration() {
        return myServiceRegistration;
    }
    
    public DefaultRegistrationStrategySpec getManagerRegistration() {
        return myManagerRegistration;
    }
    
    public void setLifecycleClassName(String lifecycleClassName) {
        myLifecycleClassName = lifecycleClassName;
    }
    
    public void addServiceBinding(String name, ServiceBindingSpec binding) {
        myServiceBindings.put(name, binding);
    }
    
    public void removeServiceBinding(String name) {
        myServiceBindings.remove(name);
    }
    
    public void setServiceRegistration(
            DefaultRegistrationStrategySpec registration) {
        myServiceRegistration = registration;
    }
    
    public void setManagerRegistration(
            DefaultRegistrationStrategySpec registration) {
        myManagerRegistration = registration;
    }
}
