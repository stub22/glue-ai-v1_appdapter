package org.appdapter.lib.bind.jflux.services;

import org.appdapter.bind.rdf.jena.assembly.KnownComponentImpl;
import java.util.Properties;

public class RegistrationSpec extends KnownComponentImpl {

    private Object spec;
    private Properties property;
//    private String registrationQN;

    public RegistrationSpec() {
        property=new Properties();
    }

    public void setSpec(Object obj) {
        spec = obj;
    }

    public void addProperty(String key, String value) {
        property.put(key, value);
    }

//    public void setQN(String name) {
//        registrationQN = name;
//    }

    public Object getSpec() {
        return spec;
    }

//    public Map<String, String> getProperties() {
//        return property;
//    }
    public Properties getProperties()
    {
        return property;
    }

//    public String getQN() {
//        return registrationQN;
//    }
}