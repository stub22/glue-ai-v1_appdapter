/*
 * Copyright 2013 The Friendularity Project (www.friendularity.org).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.appdapter.lib.bind.jflux.services;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jflux.api.registry.Registry;
import org.jflux.api.service.DefaultRegistrationStrategy;
import org.jflux.api.service.RegistrationStrategy;
import org.jflux.api.service.ServiceDependency;
import org.jflux.api.service.ServiceLifecycle;
import org.jflux.api.service.ServiceManager;
import org.jflux.api.service.binding.ServiceBinding;
import org.osgi.framework.BundleContext;
import org.jflux.impl.services.rk.osgi.ServiceClassListener;

/**
 * @author Jason G. Pallack <jgpallack@gmail.com>
 */
public class ServiceManagerSpecExtender
        extends ServiceClassListener<ServiceManagerSpec> {

    /**
     * Stores the managed connections for later removal
     */
    private Map<ServiceManagerSpec, ServiceManager> myManagedServicesMap;
    /**
     * Context reference for interacting with JFlux
     */
    private Registry myRegistry;
    /**
     * The logger, used for reporting errors.
     */
    private final static Logger theLogger =
            Logger.getLogger(ServiceManagerSpecExtender.class.getName());

    public ServiceManagerSpecExtender(
            BundleContext context, Registry registry, String serviceFilter) {
        super(ServiceManagerSpec.class, context, serviceFilter);
        myRegistry = registry;
        myManagedServicesMap =
                new HashMap<ServiceManagerSpec, ServiceManager>();
    }

    /**
     * Creates and starts a service manager.
     *
     * @param serviceManagerSpec data object used to generate connection
     */
    @Override
    protected void addService(ServiceManagerSpec serviceManagerSpec) {
        ServiceLifecycle lifecycle;
        Map<String, ServiceBinding> bindings =
                new HashMap<String, ServiceBinding>();

        if (serviceManagerSpec == null
                || myManagedServicesMap.containsKey(serviceManagerSpec)) {
            return;
        }

        try {
            Class lifecycleClass =
                    Class.forName(serviceManagerSpec.getLifecycleClassName());
            lifecycle = (ServiceLifecycle) lifecycleClass.newInstance();
        } catch (Exception e) {
            theLogger.log(
                    Level.SEVERE, "Unable to instantiate class: {0}",
                    serviceManagerSpec.getLifecycleClassName());
            return;
        }

        for (Entry<String, ServiceBindingSpec> specItem
                : serviceManagerSpec.getServiceBindings().entrySet()) {
            ServiceBindingSpec spec = specItem.getValue();
            ServiceDependencySpec depSpec = spec.getServiceDependency();
            ServiceDependency dep = getDep(depSpec.getName(), lifecycle);
//                    new ServiceDependency(
//                    depSpec.getName(), depSpec.getClassName(),
//                    depSpec.getCardinality(), depSpec.getUpdateStrategy(),
//                    depSpec.getProperties());
            if (dep == null) {
                theLogger.log(Level.SEVERE, "Dependency with name: {0} was not found.", depSpec.getName());
                continue;
            } 
            ServiceBinding binding =
                    new ServiceBinding(
                    dep, spec.getDescriptor(), spec.getBindingStrategy());
            bindings.put(specItem.getKey(), binding);
        }

        DefaultRegistrationStrategySpec stratSpec =
                serviceManagerSpec.getServiceRegistration();
        RegistrationStrategy strat =
                new DefaultRegistrationStrategy(
                stratSpec.getClassNames(),
                stratSpec.getRegistrationProperties());

        ServiceManager serviceManager =
                new ServiceManager(lifecycle, bindings, strat, null);
        // Start the service manager
        serviceManager.start(myRegistry);
        // Store the service manager so it may be removed later.
        myManagedServicesMap.put(serviceManagerSpec, serviceManager);
    }

    private ServiceDependency getDep(String name, ServiceLifecycle<?> l) {
        for (ServiceDependency d : l.getDependencySpecs()) {
            if (d.getDependencyName().equals(name)) {
                return d;
            }
        }
        return null;
    }

    /**
     * Stops a service manager.
     *
     * @param serviceManagerSpec data object used to generate connection
     */
    @Override
    protected void removeService(ServiceManagerSpec serviceManagerSpec) {
        if (serviceManagerSpec == null
                || !myManagedServicesMap.containsKey(serviceManagerSpec)) {
            return;
        }
        ServiceManager manager =
                myManagedServicesMap.remove(serviceManagerSpec);
        if (manager != null) {
            manager.stop();
        }
    }
}
