package org.appdapter.lib.bind.jflux.services;

import com.hp.hpl.jena.assembler.Assembler;
import com.hp.hpl.jena.assembler.Mode;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.appdapter.bind.rdf.jena.assembly.CachingComponentAssembler;
import org.appdapter.bind.rdf.jena.assembly.ItemAssemblyReader;
import org.appdapter.core.item.Item;
import org.appdapter.core.name.Ident;
import org.jflux.api.registry.Descriptor;
import com.hp.hpl.jena.rdf.model.Resource;

/**
 *
 * @author Jason G. Pallack <jgpallack@gmail.com>
 * @author Jason R. Eads <jeads362@gmail.com>
 */

public class ServiceManagerSpecBuilder
    extends CachingComponentAssembler<ServiceManagerSpec> {
    private final static String theLifecycleType = 
            "http://www.cogchar.org/schema/scene#lifecycleType";
    private final static String theServiceBinding = 
            "http://www.cogchar.org/schema/scene#serviceBinding";
    private final static String theRegistrationStrategy =
            "http://www.cogchar.org/schema/scene#registrationStrategy";
    private final static Logger theLogger =
            Logger.getLogger(ServiceManagerSpecBuilder.class.getName());
    
    
    public ServiceManagerSpecBuilder( Resource builderConfRes ) {
        super(builderConfRes);
    }
    
    @Override
    protected Class<ServiceManagerSpec> decideComponentClass(
            Ident ident, Item item) {
        return ServiceManagerSpec.class;
    }

    @Override
    protected void initExtendedFieldsAndLinks(
            ServiceManagerSpec mkc, Item item, Assembler asmblr, Mode mode) {
        ItemAssemblyReader reader = getReader();
        ServiceLifecycleSpec lifecycleSpec = null;
        
        List linkedLifecycles =
                reader.findOrMakeLinkedObjects(
                item, theLifecycleType, asmblr, mode, null);
        List linkedBindings =
                reader.findOrMakeLinkedObjects(
                item, theServiceBinding, asmblr, mode, null);
        List linkedStrategies =
                reader.findOrMakeLinkedObjects(
                item, theRegistrationStrategy, asmblr, mode, null);
        
        for(Object lc: linkedLifecycles) {
            if(!(lc instanceof ServiceLifecycleSpec)) {
                theLogger.log(
                        Level.WARNING, "Unexpected object found at {0} = {1}",
                        new Object[]{theLifecycleType, lc.toString()});
                continue;
            }
            
            lifecycleSpec = (ServiceLifecycleSpec)lc;
            String lifecycleClass = lifecycleSpec.getLifecycleClassName();
            mkc.setLifecycleClassName(lifecycleClass);
            break;
        }
        
        for(Object sb: linkedBindings) {
            if(!(sb instanceof ServiceBindingSpec)) {
                theLogger.log(
                        Level.WARNING, "Unexpected object found at {0} = {1}",
                        new Object[]{theServiceBinding, sb.toString()});
                continue;
            }
            
            ServiceBindingSpec bindingSpec = (ServiceBindingSpec)sb;
            Descriptor desc = bindingSpec.getDescriptor();
            String depName = bindingSpec.getServiceDependency().getName();
            mkc.addServiceBinding(depName, bindingSpec);
        }
        
        for(Object rs: linkedStrategies) {
            if(!(rs instanceof DefaultRegistrationStrategySpec)) {
                theLogger.log(
                        Level.WARNING, "Unexpected object found at {0} = {1}",
                        new Object[]{theRegistrationStrategy, rs.toString()});
                continue;
            }
            
            DefaultRegistrationStrategySpec stratSpec =
                    (DefaultRegistrationStrategySpec)rs;
            mkc.setServiceRegistration(stratSpec);
            break;
        }
    }
}
