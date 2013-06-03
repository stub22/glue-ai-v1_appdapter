/*
 *  Copyright 2011 by The Appdapter Project (www.appdapter.org).
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.appdapter.core.store;

import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;

import org.appdapter.api.trigger.AnyOper;
import org.appdapter.api.trigger.Box;
import org.appdapter.api.trigger.TriggerImpl;
import org.appdapter.demo.DemoResources;
import org.appdapter.gui.demo.RepoNavigator;
import org.appdapter.gui.repo.RepoBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.query.DataSource;
import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.rdf.model.Model;

/**
 * @author Dmiles
 */
// / Dmiles needed something in java to cover Dmiles's Scala blindspots
public class RepoOper implements AnyOper {

    @UISalient
    static public interface ISeeToString {
        @Override @ToStringResult
        public String toString();
    }

    @UISalient
    static public interface Reloadable {

        @SalientVoidCall(Named="Reload Repo")
        void reloadAllModels();

        @SalientVoidCall()
        void reloadSingleModel(String modelName);

        @ToStringResult
        Dataset getMainQueryDataset();
    }

    // static class ConcBootstrapTF extends
    // BootstrapTriggerFactory<TriggerImpl<BoxImpl<TriggerImpl>>> {
    // } // TT extends TriggerImpl<BT>
    public static class ReloadAllModelsTrigger<RB extends RepoBox<TriggerImpl<RB>>> extends TriggerImpl<RB> {

        Repo.WithDirectory m_repo;

        // @TODO obviouly we should be using specs and not repos! but
        // With.Directory may as well be the spec for now.
        // Also consider we are using the actual Repo (not the Spec) due to the
        // fact we must have something to clear and update right?
        public ReloadAllModelsTrigger(Repo.WithDirectory repo) {
            m_repo = repo;
        }

        @Override
        public void fire(RB targetBox) {
            String resolvedQueryURL = DemoResources.QUERY_PATH;
            ClassLoader optCL = RepoNavigator.class.getClassLoader();
            if (!(m_repo instanceof RepoOper.Reloadable)) {
                theLogger.error("Repo not reloadable! " + targetBox);
            } else {
                RepoOper.Reloadable reloadme = (RepoOper.Reloadable) targetBox;
                reloadme.reloadAllModels();
            }
            String resultXML = targetBox.processQueryAtUrlAndProduceXml(resolvedQueryURL, optCL);
            logInfo("ResultXML\n-----------------------------------" + resultXML + "\n---------------------------------");
        }
    }

    static public class ReloadSingleModelTrigger extends TriggerImpl {

        final String graphURI;
        final Reloadable m_repo;

        public ReloadSingleModelTrigger(String graphURI, Reloadable repo) {
            this.graphURI = graphURI;
            m_repo = repo;
        }

        @Override
        public void fire(Box bt) {
            m_repo.reloadSingleModel(graphURI);
        }
    }
    static Logger theLogger = LoggerFactory.getLogger(RepoOper.class);

    public static void replaceModelElements(Model dest, Model src) {
        if (src == dest) {
            return;
        }
        dest.removeAll();
        dest.add(src);
        dest.setNsPrefixes(src.getNsPrefixMap());
        // dest.getGraph().getPrefixMapping().equals(obj)
        //if (src.getGraph() )dest.setNsPrefix("", src.getNsPrefixURI(""));
        ///dest.setNsPrefix("#", src.getNsPrefixURI("#"));
    }

    public static void replaceDatasetElements(Dataset dest, Dataset src, String onlyModel) {
        if (!(dest instanceof DataSource)) {
            theLogger.error("Destination is not a datasource! " + dest.getClass() + " " + dest);
            return;
        }
        DataSource sdest = (DataSource) dest;
        boolean onSrc = true, onDest = true;
        if (!dest.containsNamedModel(onlyModel)) {
            onSrc = false;
            theLogger.warn("Orginal did not contain model" + onlyModel);

        }
        if (!src.containsNamedModel(onlyModel)) {
            onDest = false;
            theLogger.warn("New did not contain model " + onlyModel);
        }
        if (onSrc && onDest) {
            Model destModel = src.getNamedModel(onlyModel);
            Model srcModel = dest.getNamedModel(onlyModel);
            replaceModelElements(destModel, srcModel);
            theLogger.info("Replaced model " + onlyModel);
            return;
        }
        if (onSrc) {
            sdest.addNamedModel(onlyModel, src.getNamedModel(onlyModel));
            theLogger.info("Added model " + onlyModel);
            return;
        }
        if (onDest) {
            dest.getNamedModel(onlyModel).removeAll();
            theLogger.info("clearing model " + onlyModel);
            return;
        }
    }

    public static void replaceDatasetElements(Dataset dest, Dataset src) {
        if (!(dest instanceof DataSource)) {
            theLogger.error("Destination is not a datasource! " + dest.getClass() + " " + dest);
            return;
        }
        DataSource sdest = (DataSource) dest;
        Model defDestModel = dest.getDefaultModel();
        Model defSrcModel = src.getDefaultModel();
        replaceModelElements(defDestModel, defSrcModel);
        HashSet<String> dnames = setOF(sdest.listNames());
        HashSet<String> snames = setOF(src.listNames());
        HashSet<String> replacedModels = new HashSet<String>();

        for (String nym : snames) {
            Model getsrc = src.getNamedModel(nym);
            if (dest.containsNamedModel(nym)) {
                Model getdest = dest.getNamedModel(nym);
                replacedModels.add(nym);
                replaceModelElements(getdest, getsrc);
                dnames.remove(nym);
                continue;
            }
        }
        for (String nym : replacedModels) {
            snames.remove(nym);
        }

        if (dnames.size() == 0) {
            if (snames.size() == 0) {// perfect!
                return;
            } else {
                // add the new models to the datasource
                for (String nym : snames) {
                    sdest.addNamedModel(nym, src.getNamedModel(nym));
                }
                // still good
                return;
            }
        } else {
            // dnames > 0
            if (snames.size() == 0) {
                // some graphs might need cleared?
                for (String nym : dnames) {
                    sdest.getNamedModel(nym).removeAll();
                    sdest.removeNamedModel(nym);
                }
                return;
            } else {
                // New names to add AND graphs might need cleared
                for (String nym : dnames) {
                    sdest.getNamedModel(nym).removeAll();
                    sdest.removeNamedModel(nym);
                }
                for (String nym : snames) {
                    sdest.addNamedModel(nym, src.getNamedModel(nym));
                }
            }
        }
    }

    public static <E> HashSet<E> setOF(Enumeration<E> en) {
        HashSet<E> hs = new HashSet<E>();
        while (en.hasMoreElements()) {
            E e = (E) en.nextElement();
            hs.add(e);
        }
        return hs;
    }

    public static <E> HashSet<E> setOF(Iterator<E> en) {
        HashSet<E> hs = new HashSet<E>();
        while (en.hasNext()) {
            E e = (E) en.next();
            hs.add(e);
        }
        return hs;
    }
}
