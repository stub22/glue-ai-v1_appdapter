package org.appdapter.lib.bind.jflux.services;

import com.hp.hpl.jena.assembler.Assembler;
import com.hp.hpl.jena.assembler.Mode;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.appdapter.bind.rdf.jena.assembly.CachingComponentAssembler;
import org.appdapter.bind.rdf.jena.assembly.ItemAssemblyReader;
import org.appdapter.core.item.Item;
import org.appdapter.core.name.Ident;
import org.jflux.api.service.ServiceDependency.Cardinality;
import org.jflux.api.service.ServiceDependency.UpdateStrategy;

/**
 *
 * @author Jason G. Pallack <jgpallack@gmail.com>
 */


public class ServiceDependencySpecBuilder
    extends CachingComponentAssembler<ServiceDependencySpec> {
    private final static String theUpdateStrategy =
            "http://www.jflux.org/service/dependency#updateStrategy";
    private final static String theCountCardinality =
            "http://www.jflux.org/service/dependency#countCardinality";
    private final static String theRequired =
            "http://www.jflux.org/service/dependency#required";
    private final static String theServiceJavaFQCN =
            "http://www.appdapter.org/schema/box#serviceJavaFQCN";
    private final static Logger theLogger =
            Logger.getLogger(ServiceDependencySpecBuilder.class.getName());
    
    @Override
    protected Class<ServiceDependencySpec> decideComponentClass(
            Ident ident, Item item) {
        return ServiceDependencySpec.class;
    }

    @Override
    protected void initExtendedFieldsAndLinks(
            ServiceDependencySpec mkc, Item item, Assembler asmblr, Mode mode) {
        ItemAssemblyReader reader = getReader();
        String className = reader.readConfigValString(
                item.getIdent(), theServiceJavaFQCN, item, "");
        String update = reader.readConfigValString(
                item.getIdent(), theUpdateStrategy, item, "");
        String count = reader.readConfigValString(
                item.getIdent(), theCountCardinality, item, "");
        String required = reader.readConfigValString(
                item.getIdent(), theRequired, item, "");

        mkc.setClassName(className);
        mkc.setName(item.getIdent().getLocalName());
        
        if(update.equals("static")) {
            mkc.setUpdateStrategy(UpdateStrategy.STATIC);
        } else if (update.equals("dynamic")) {
            mkc.setUpdateStrategy(UpdateStrategy.DYNAMIC);
        } else {
            theLogger.log(
                    Level.SEVERE, "Unexpected update strategy: {0}", update);
            mkc.setUpdateStrategy(null);
        }
        
        if(count.equals("single") && required.equals("required")) {
            mkc.setCardinality(Cardinality.MANDATORY_UNARY);
        } else if(count.equals("multiple") && required.equals("required")) {
            mkc.setCardinality(Cardinality.MANDATORY_MULTIPLE);
        } else if(count.equals("single") && required.equals("optional")) {
            mkc.setCardinality(Cardinality.OPTIONAL_UNARY);
        } else if(count.equals("multiple") && required.equals("optional")) {
            mkc.setCardinality(Cardinality.OPTIONAL_MULTIPLE);
        } else {
            theLogger.log(
                    Level.SEVERE, "Unexpected cardinality: {0} {1}",
                    new Object[]{count, required});
            mkc.setCardinality(null);
        }
    }
}
