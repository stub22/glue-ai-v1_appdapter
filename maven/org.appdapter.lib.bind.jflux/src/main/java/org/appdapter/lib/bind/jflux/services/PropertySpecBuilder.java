package org.appdapter.lib.bind.jflux.services;

import com.hp.hpl.jena.assembler.Assembler;
import com.hp.hpl.jena.assembler.Mode;
import org.appdapter.bind.rdf.jena.assembly.CachingComponentAssembler;
import org.appdapter.bind.rdf.jena.assembly.ItemAssemblyReader;
import org.appdapter.core.item.Item;
import org.appdapter.core.name.Ident;

import com.hp.hpl.jena.rdf.model.Resource;
/**
 * The builder that is called to make a Spec object from the raw RDF data
 * representing a ServiceBinding.
 * @author Jason Randolph Eads <eadsjr@hansonrobokind.com>
 * @author Jason G. Pallack <jgpallack@gmail.com>
 */

public class PropertySpecBuilder extends CachingComponentAssembler<PropertySpec> {
    // Defines the relationship "#Property Name" key (aka the Predicate),
    // that is followed from an individual to collect the data
    
    private final static String thePropertyKeyId = "http://www.cogchar.org/schema/scene#propKey";
    private final static String thePropertyValueId = "http://www.cogchar.org/schema/scene#propValue";
    
    public PropertySpecBuilder( Resource builderConfRes ) {
        super(builderConfRes);
    }
    
    @Override
    protected Class<PropertySpec> decideComponentClass(Ident ident, Item item) {
        return PropertySpec.class;
    }
    
    @Override
    protected void initExtendedFieldsAndLinks(
            PropertySpec mkc, Item item, Assembler asmblr, Mode mode) {
        ItemAssemblyReader reader = getReader();
        
        // reads in the data fields and stores them in the Spec
        
        mkc.setName(reader.readConfigValString(
                item.getIdent(), thePropertyKeyId, item, ""));
        mkc.setValue(reader.readConfigValString(
                item.getIdent(), thePropertyValueId, item, ""));
    }
    
}
