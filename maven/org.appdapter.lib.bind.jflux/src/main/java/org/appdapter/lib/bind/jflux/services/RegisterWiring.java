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

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.appdapter.core.matdat.BoundModelProvider;
import org.appdapter.core.matdat.EnhancedRepoClient;
import org.appdapter.core.matdat.ModelProviderFactory;
import org.appdapter.core.matdat.PipelineQuerySpec;
import org.appdapter.core.name.Ident;
import org.osgi.framework.BundleContext;
import org.jflux.impl.services.rk.lifecycle.ManagedService;
import org.jflux.impl.services.rk.lifecycle.utils.SimpleLifecycle;
import org.jflux.impl.services.rk.osgi.lifecycle.OSGiComponent;

/**
 *
 * @author Major Jacquote II <mjacquote@gmail.com>
 */
public class RegisterWiring {
    public final static String PIPELINE_GRAPH_QN = "csi:pipeline_sheet_0";
    public final static String PIPE_QUERY_QN = "ccrt:find_pipes_77"; //"ccrt:find_sheets_77";
    public final static String PIPE_SOURCE_QUERY_QN = "ccrt:find_pipe_sources_99";
    public final static PipelineQuerySpec myDefaultPipelineQuerySpec = new PipelineQuerySpec(
            PIPE_QUERY_QN, PIPE_SOURCE_QUERY_QN, PIPELINE_GRAPH_QN);

    public static List<ManagedService> loadAndRegisterSpec(BundleContext context, EnhancedRepoClient defaultDemoRepoClient, String mergedSheetQN) {
        Ident derivedBehavGraphID = defaultDemoRepoClient.makeIdentForQName(mergedSheetQN);
        List<ManagedService> managedServices = new ArrayList();
        BoundModelProvider derivedBMP =
                ModelProviderFactory.makeOneDerivedModelProvider(
                defaultDemoRepoClient, myDefaultPipelineQuerySpec, derivedBehavGraphID);

        Set<Object> allSpecs = derivedBMP.assembleModelRoots();
        List<RegistrationSpec> registrationSpecs = new ArrayList<RegistrationSpec>();
        for (Object root : allSpecs) {
            if (root == null || !RegistrationSpec.class.isAssignableFrom(root.getClass())) {
                continue;
            }
            registrationSpecs.add((RegistrationSpec) root);
        }
        for (RegistrationSpec root : registrationSpecs) {
            ManagedService ms = new OSGiComponent(context, 
                    new SimpleLifecycle(root.getSpec(), root.getSpec().getClass()), 
                    root.getProperties());
            ms.start();
            managedServices.add(ms);
        }
        return managedServices;

    }
}
