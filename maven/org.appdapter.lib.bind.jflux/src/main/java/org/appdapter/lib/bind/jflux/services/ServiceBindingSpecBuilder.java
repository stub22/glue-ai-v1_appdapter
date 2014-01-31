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
import org.jflux.api.service.binding.ServiceBinding.BindingStrategy;

import com.hp.hpl.jena.rdf.model.Resource;
import org.appdapter.core.item.ItemFuncs;
/**
 * The builder that is called to make a Spec object from the raw RDF data
 * representing a ServiceBinding.
 * 
 * @author Jason R. Eads <jeads362@gmail.com>
 * @author Jason G. Pallack <jgpallack@gmail.com>
 */

public class ServiceBindingSpecBuilder
    extends CachingComponentAssembler<ServiceBindingSpec> {
    // Defines the relationship "#Property Name" key (aka the Predicate),
    // that is followed from an individual to collect the data
    
    public ServiceBindingSpecBuilder( Resource builderConfRes ) {
        super(builderConfRes);
    }
    
    private final static String theServiceJavaFQCN =
            "http://www.jflux.org/service#serviceJavaFQCN";
    private final static String theHasProperty =
            "http://www.cogchar.org/schema/scene#hasProperty";
    private final static String theBinding =
            "http://www.cogchar.org/schema/scene#Binding";
    private final static String theDependencyURI = 
            "http://www.cogchar.org/schema/scene#dependencyURI";
    
    private final static String BINDING_STRATEGY_INVALID_WARN =
            "Invalid binding strategy for serviceBinding";
    private final static Logger theLogger =
            Logger.getLogger(ServiceBindingSpecBuilder.class.getName());

    @Override
    protected Class<ServiceBindingSpec> decideComponentClass(
            Ident ident, Item item) {
        return ServiceBindingSpec.class;
    }

    @Override
    protected void initExtendedFieldsAndLinks(
            ServiceBindingSpec mkc, Item item, Assembler asmblr, Mode mode) {
        ItemAssemblyReader reader = getReader();
        
        // read in the data fields and store them in the Spec
        mkc.setClassName(reader.readConfigValString(
                item.getIdent(), theServiceJavaFQCN, item, ""));
        
        // read in the binding strategy
        Ident bindingIdent = ItemFuncs.getNeighborIdent(item, theBinding);
        Item bindingStrategy =
                item.getSingleLinkedItem(
                bindingIdent, Item.LinkDirection.FORWARD);
        if(bindingStrategy.getIdent().getLocalName().equals("lazy")) {
            mkc.setBindingStrategy(BindingStrategy.LAZY);
        }
        else if(bindingStrategy.getIdent().getLocalName().equals("eager")) {
            mkc.setBindingStrategy(BindingStrategy.EAGER);
        }
        else {
            getLogger().warn(BINDING_STRATEGY_INVALID_WARN);
            mkc.setBindingStrategy(BindingStrategy.LAZY);
        }
        
        // read in and build the linked properties, and storing each in the Spec
        List linkedProperties =
                reader.findOrMakeLinkedObjects(
                item, theHasProperty, asmblr, mode, null);
        List linkedDependencies =
                reader.findOrMakeLinkedObjects(
                item, theDependencyURI, asmblr, mode, null);
        
        for(Object lp: linkedProperties) {
            if(!(lp instanceof PropertySpec)) {
                theLogger.log(
                        Level.WARNING, "Unexpected object found at {0} = {1}",
                        new Object[]{theHasProperty, lp.toString()});
                continue;
            }

            PropertySpec propertySpec = (PropertySpec)lp;
            mkc.addProperty(propertySpec.getName(), propertySpec.getValue());
        }

        for(Object ld: linkedDependencies) {
            if(!(ld instanceof ServiceDependencySpec)) {
                theLogger.log(
                        Level.WARNING, "Unexpected object found at {0} = {1}",
                        new Object[]{theDependencyURI, ld.toString()});
                continue;
            }

            ServiceDependencySpec dep = (ServiceDependencySpec)ld;
            mkc.setServiceDependency(dep);

            break;
        }
    }
}