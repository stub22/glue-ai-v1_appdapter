package org.appdapter.lib.bind.jflux.services;

import com.hp.hpl.jena.assembler.Assembler;
import com.hp.hpl.jena.assembler.Mode;
import java.util.List;
import org.appdapter.bind.rdf.jena.assembly.CachingComponentAssembler;
import org.appdapter.bind.rdf.jena.assembly.ItemAssemblyReader;
import org.appdapter.core.item.Item;
import org.appdapter.core.name.Ident;

import com.hp.hpl.jena.rdf.model.Resource;
/**
 *
 * @author Jason G. Pallack <jgpallack@gmail.com>
 * @author Jason R. Eads <jeads362@gmail.com>
 */


public class DefaultRegistrationStrategySpecBuilder
    extends CachingComponentAssembler<DefaultRegistrationStrategySpec> {
    private final static String theHasProperty = 
            "http://www.cogchar.org/schema/scene#hasProperty";
    private final static String theServiceJavaFQCN = 
            "http://www.jflux.org/service#serviceJavaFQCN";
    
    public DefaultRegistrationStrategySpecBuilder( Resource builderConfRes ) {
        super(builderConfRes);
    }

    @Override
    protected Class<DefaultRegistrationStrategySpec> decideComponentClass(
            Ident ident, Item item) {
        return DefaultRegistrationStrategySpec.class;
    }

    @Override
    protected void initExtendedFieldsAndLinks(
            DefaultRegistrationStrategySpec mkc, Item item, Assembler asmblr,
            Mode mode) {
        
        ItemAssemblyReader reader = getReader();
        mkc.addClassName(reader.readConfigValString(
                item.getIdent(), theServiceJavaFQCN, item, ""));
        
        List linkedProperties =
                reader.findOrMakeLinkedObjects(
                item, theHasProperty, asmblr, mode, null);
        
        for(Object prop: linkedProperties) {
            if(prop instanceof PropertySpec) {
                PropertySpec propertySpec = (PropertySpec)prop;
                mkc.addProperty(
                        propertySpec.getName(), propertySpec.getValue());
            }
        }
    }
}
