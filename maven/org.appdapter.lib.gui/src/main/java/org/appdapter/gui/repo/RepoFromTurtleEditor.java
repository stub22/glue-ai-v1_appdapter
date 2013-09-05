package org.appdapter.gui.repo;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;

import javax.swing.JFrame;

import org.appdapter.core.store.Repo;
import org.appdapter.gui.browse.Utility;
import org.appdapter.gui.demo.DemoBrowser;
import org.appdapter.gui.editors.ObjectPanel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.rdf.listeners.StatementListener;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Statement;

//import com.hp.hpl.jena.n3.N3Exception;

/**
 * <p>A Swing-based GUI window that provides a simple Turtle-based
 * editor and inspector for Jena models. Useful for debugging GUI
 * and web applications. To open an editor window, pass the model
 * instance to the static {@link RepoFromTurtleEditor#open(Repo)} method.</p>
 * 
 * <p>The editor has basic reporting of Turtle syntax errors.
 * It also updates the namespace prefixes of the model.
 * Several windows for different models may be open at the same
 * time. Concurrent changes to the model are reported.</p>
 * 
 * <p>The class has a {@link #main} method for demonstration purposes.
 * It loads one or more RDF files into Jena models and displays an editor
 * for each.</p>
 * 
 * @version $Id$
 * @author Richard Cyganiak (richard@cyganiak.de)
 * @author LogicMoo
 */
public class RepoFromTurtleEditor extends ModelAsTurtleEditor implements ObjectPanel {
	static Logger theLogger = LoggerFactory.getLogger(RepoFromTurtleEditor.class);

	public static Class[] EDITTYPE = new Class[] { Repo.class, Dataset.class };

	@UISalient(IsPanel = true) static public RepoFromTurtleEditor showTurtleTextEditor(Repo obj) {
		return new RepoFromTurtleEditor(obj);
	}

	static {
		Utility.registerPanel(RepoFromTurtleEditor.class, Repo.class);
		Utility.addClassMethods(RepoFromTurtleEditor.class);
	}

	/**
	 * Opens a new editor window and binds it to the given model.
	 * @param sourceRepo A Jena model
	 * @return A reference to the new editor window
	 */
	@UISalient public static RepoFromTurtleEditor open(Repo sourceRepo) {
		return new RepoFromTurtleEditor(sourceRepo, "Jena Repo Editor for " + sourceRepo);
	}

	/**
	 * Opens a new editor window and binds it to the given model.
	 * A custom title is useful to distinguish multiple editor
	 * windows for different models.
	 * @param sourceRepo A Jena model
	 * @param title A custom title for the editor window
	 * @return A reference to the new editor window
	 */
	public static ModelAsTurtleEditor open(Model sourceRepo, String title) {
		return new ModelAsTurtleEditor(sourceRepo, title + " - Jena Repo Editor");
	}

	/**
	 * Main method for demonstration purposes. Takes a number of
	 * filename or URL arguments. Reads them as RDF/XML or Turtle
	 * (if ends with ".n3" or ".ttl"). Displays an editor for
	 * each. If the same filename appears twice, then both editors
	 * will use the same model.
	 * @param args
	 */
	public static void main(String[] args) {
		if (args.length == 0) {
			System.out.println("Please specify one or more " + "RDF filenames or URLs.");
			return;
		}
		Repo.WithDirectory r = DemoBrowser.getDemoRepo();
		RepoFromTurtleEditor rfte = new RepoFromTurtleEditor();
		rfte.setRepoObject(r);

		JFrame f = new JFrame(rfte.getName());
		f.setContentPane(rfte);
		f.setVisible(true);

	}

	/**
	 * Creates and displays new RepoEditor.
	 * @param sourceRepo The model bound to the editor
	 * @param title The full title of the editor window
	 */
	public RepoFromTurtleEditor(Repo sourceRepo, String title) {
		this.window = Utility.getAppFrame();
		this.titleShouldBe = title;

		this.setObject(sourceRepo);
	}

	public RepoFromTurtleEditor() {
		this.window = Utility.getAppFrame();
	}

	public RepoFromTurtleEditor(Repo sourceRepo) {
		this.window = Utility.getAppFrame();
		if (sourceRepo != null) {
			this.titleShouldBe = "" + sourceRepo;
		}
		setRepoObject(sourceRepo);
	}

	void setRepoObject(final Repo boundRepo) {
		super.setObject(boundRepo);
		if (boundRepo instanceof Repo.WithDirectory) {
			add(((Repo.WithDirectory) boundRepo).getDirectoryModel(), "" + boundRepo);
		}
		Dataset ds = boundRepo.getMainQueryDataset();
		Iterator dni = ds.listNames();
		while (dni.hasNext()) {
			String name = (String) dni.next();
			Model m = ds.getNamedModel(name);
			String baseURI = getBaseURI(m, name);

			m.setNsPrefix("", baseURI);
			if (!baseURIToGraph.containsKey(baseURI)) {
				if (baseURI.indexOf(":") == -1) {
					baseURI = "file:" + baseURI;
				}
				baseURIToGraph.put(baseURI, m);
			}
			String fileURI = baseURI;
			if (!filenamesToGraph.containsKey(fileURI)) {
				if (fileURI.indexOf(":") == -1) {
					fileURI = "file:" + fileURI;
				}
				filenamesToGraph.put(fileURI, m);
			}
			add(m, name);
		}
		Model defaultModel = ds.getDefaultModel();
		add(defaultModel, getBaseURI(defaultModel, null));

	}

	@Override public Class<Repo> getClassOfBox() {
		return EDITTYPE[0];
	}

	@Override protected boolean reloadObjectGUI(Object obj) throws Throwable {
		setRepoObject((Repo) Utility.recast(obj, getClassOfBox()));
		return true;
	}
}