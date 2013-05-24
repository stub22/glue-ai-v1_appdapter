package org.appdapter.gui;

import org.appdapter.api.trigger.TriggerImpl;
import org.appdapter.core.store.Repo;
import org.appdapter.core.store.RepoOper;
import org.appdapter.demo.DemoResources;
import org.appdapter.gui.demo.RepoNavigator;
import org.appdapter.gui.repo.RepoBox;

// static class ConcBootstrapTF extends
// BootstrapTriggerFactory<TriggerImpl<BoxImpl<TriggerImpl>>> {
// } // TT extends TriggerImpl<BT>
public class ReloadAllModelsTrigger<RB extends RepoBox<TriggerImpl<RB>>> extends TriggerImpl<RB> {

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
            RepoOper.theLogger.error("Repo not reloadable! " + targetBox);
        } else {
            RepoOper.Reloadable reloadme = (RepoOper.Reloadable) targetBox;
            reloadme.reloadAllModels();
        }
        String resultXML = targetBox.processQueryAtUrlAndProduceXml(resolvedQueryURL, optCL);
        logInfo("ResultXML\n-----------------------------------" + resultXML + "\n---------------------------------");
    }
}