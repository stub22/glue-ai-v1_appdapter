/*
 * Copyright 2014 by The Appdapter Project (www.appdapter.org).
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

import org.appdapter.bind.rdf.jena.assembly.CachingComponentAssembler;
import org.appdapter.core.item.Item;
import org.appdapter.core.name.Ident;

import com.hp.hpl.jena.assembler.Assembler;
import com.hp.hpl.jena.assembler.Mode;

import com.hp.hpl.jena.rdf.model.Resource;
import java.util.List;
import org.appdapter.bind.rdf.jena.assembly.ItemAssemblyReader;

public class RegistrySpecBuilder extends CachingComponentAssembler<RegistrationSpec> {

    private final String spec = "http://www.friedularity.org/Connection#Object";
    private final String property = "http://www.friedularity.org/Connection#hasProperty";
//    private final String registrySpecQN = "http://www.friedularity.org/Connection#registrationQN";

    public RegistrySpecBuilder(Resource builderConfRes) {
        super(builderConfRes);
    }

    @Override
    protected Class<RegistrationSpec> decideComponentClass(Ident ident, Item item) {
        return RegistrationSpec.class;
    }

    @Override
    protected void initExtendedFieldsAndLinks(RegistrationSpec registrationSpec, Item item, Assembler asmblr, Mode mode) {
        ItemAssemblyReader reader = getReader();
        List linkedSpecs =
                reader.findOrMakeLinkedObjects(
                item, spec, asmblr, mode, null);
        
        if(linkedSpecs.isEmpty()) {
            throw new IllegalArgumentException("No spec objects found.");
        }
        
        registrationSpec.setSpec((Object)linkedSpecs.get(0));
//        registrationSpec.setQN(reader.readConfigValString(item.getIdent(), registrySpecQN, item, ""));
        
        List linkedProperties =
                reader.findOrMakeLinkedObjects(
                item, property, asmblr, mode, null);
        
        for (Object prop : linkedProperties) {
            if (prop instanceof PropertySpec) {
                PropertySpec propertySpec = (PropertySpec) prop;
                registrationSpec.addProperty( propertySpec.getName(), propertySpec.getValue());
            }
        }

   }
}