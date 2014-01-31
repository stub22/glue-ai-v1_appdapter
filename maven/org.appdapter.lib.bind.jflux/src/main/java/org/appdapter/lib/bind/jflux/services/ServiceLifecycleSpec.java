package org.appdapter.lib.bind.jflux.services;

import java.util.LinkedList;
import java.util.List;
import org.appdapter.bind.rdf.jena.assembly.KnownComponentImpl;

/**
 *
 * @author Jason G. Pallack <jgpallack@gmail.com>
 */

public class ServiceLifecycleSpec extends KnownComponentImpl {
    private String myLifecycleClassName;
    private List<ServiceDependencySpec> myDependencies;
    
    public ServiceLifecycleSpec() {
        myDependencies = new LinkedList<ServiceDependencySpec>();
    }
    
    public String getLifecycleClassName() {
        return myLifecycleClassName;
    }
    
    public List<ServiceDependencySpec> getDependencies() {
        return myDependencies;
    }
    
    public void setLifecycleClassName(String className) {
        myLifecycleClassName = className;
    }
    
    public void addDependency(ServiceDependencySpec dependency) {
        myDependencies.add(dependency);
    }
    
    public void removeDependency(ServiceDependencySpec dependency) {
        myDependencies.remove(dependency);
    }
}
