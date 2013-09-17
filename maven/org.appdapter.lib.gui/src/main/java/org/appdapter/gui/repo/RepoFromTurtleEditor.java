package org.appdapter.gui.repo;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

import org.appdapter.core.convert.NoSuchConversionException;
import org.appdapter.core.log.Debuggable;
import org.appdapter.core.matdat.OmniLoaderRepo;
import org.appdapter.core.store.Repo;
import org.appdapter.core.store.Repo.WithDirectory;
import org.appdapter.core.store.RepoOper;
import org.appdapter.gui.browse.Utility;
import org.appdapter.gui.demo.DemoBrowser;
import org.appdapter.gui.editors.ObjectPanel;
import org.appdapter.impl.store.DirectRepo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.rdf.listeners.StatementListener;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelChangedListener;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;

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
		//Utility.registerPanel(RepoFromTurtleEditor.class, Repo.class);
		//Utility.addClassMethods(RepoFromTurtleEditor.class);
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

	private Model totalModel;

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

	private JFileChooser saveFileChooser;

	@Override protected void initSubclassGUI() throws Throwable {
		//String startDir = System.getProperty("user.dir");
		saveFileChooser = new JFileChooser(".");

		super.initSubclassGUI();

		makeButton("Save Repo", new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					saveTTL(getFile("Save as..", true));
				} catch (Exception e1) {
					Debuggable.printStackTrace(e1);
				}
			}
		});

		makeButton("Load Repo", new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					loadTTL(getFile("Open Repo", false));
				} catch (Exception e1) {
					Debuggable.printStackTrace(e1);
				}
			}
		});

	}

	File saveFile;

	public File getFile(String called, boolean confirmOverwrite) {
		saveFileChooser.setDialogTitle(called);
		int returnVal = saveFileChooser.showSaveDialog(Utility.getAppFrame());
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			saveFile = saveFileChooser.getSelectedFile();
			if (confirmOverwrite && saveFile.exists()) {
				int returnValue = JOptionPane.showConfirmDialog(Utility.getAppFrame(), "Overwrite Existing File: " + saveFile.getName() + "?", "Question", JOptionPane.YES_NO_OPTION);
				if (returnValue != JOptionPane.YES_OPTION)
					return null;
			}
			return saveFile;
		}
		return null;
	}

	void setRepoObject(final Repo boundRepo) {
		super.setObject(boundRepo);
		String repoName = "" + boundRepo;
		this.totalModel = ModelFactory.createDefaultModel();
		add(totalModel, repoName);
		Model dirModel = null;
		if (boundRepo instanceof Repo.WithDirectory) {
			dirModel = ((Repo.WithDirectory) boundRepo).getDirectoryModel();
			totalModel.add(dirModel);
		}
		if (dirModel == null) {
			dirModel = ModelFactory.createDefaultModel();
		}
		String modelName = addNamedModel("", dirModel);
		Dataset ds = boundRepo.getMainQueryDataset();
		Iterator dni = ds.listNames();
		while (dni.hasNext()) {
			String name = (String) dni.next();
			Model m = ds.getNamedModel(name);
			totalModel.add(m);
			addNamedModel(name, m);
		}
		Model defaultModel = ds.getDefaultModel();
		if (defaultModel != null) {
			add(defaultModel, getBaseURI(defaultModel, "defaultModel"));
			totalModel.add(defaultModel);
		}
	}

	@Override public void saveTTL(File file) throws IOException, NoSuchConversionException {
		Repo boundRepo = getRepo(getValue());
		FileOutputStream os = new FileOutputStream(file);
		OutputStreamWriter ow = new OutputStreamWriter(os);
		writeToTTL(boundRepo, ow);
		ow.close();
	}

	private void writeToTTL(Repo boundRepo, Writer ow) throws IOException {
		String repoName = "" + boundRepo;
		String thiz = "<_:self>";
		String bar = "########################################\n";
		ow.write("# reponame=" + repoName + "\n");
		ow.write("# time=" + new Date().toString() + "\n");
		Model dirModel = null;
		if (boundRepo instanceof Repo.WithDirectory) {
			dirModel = ((Repo.WithDirectory) boundRepo).getDirectoryModel();
			ow.write("# dirModel = " + dirModel.size() + "\n");
			ow.flush();
			dirModel.write(ow, "TTL");
			ow.write("\n\n");
			ow.write(thiz + " a ccrt:DirectoryModel. \n");
			ow.write("\n\n");
			ow.flush();
		}
		/*if (dirModel == null) {
			dirModel = ModelFactory.createDefaultModel();
		}
		String modelName = addNamedModel("", dirModel);
		*/
		Dataset ds = boundRepo.getMainQueryDataset();

		Iterator dni = ds.listNames();
		Model defaultModel = ds.getDefaultModel();
		while (dni.hasNext()) {
			String name = (String) dni.next();
			ow.write("\n\n");
			ow.write(bar);
			ow.write("# modelName=" + name + "\n");
			ow.write(thiz + " ccrt:sheetName \"" + name + "\".\n");
			Model m = ds.getNamedModel(name);
			ow.write(getModelSource(m) + "\n");
			ow.write("# modelSize=" + m.size() + "\n");
			ow.write(thiz + " a ccrt:RepoSheetModel. \n");
			if (m == defaultModel) {
				ow.write(thiz + " a ccrt:DatasetDefaultModel. \n");
				defaultModel = null;
			}
			ow.write("\n\n");
			ow.flush();
		}
		if (defaultModel != null) {
			ow.write(bar);
			ow.write("# defaultModel..." + "\n");
			String name = getBaseURI(defaultModel, null);
			ow.write("# modelName=" + name + "\n");
			ow.write(thiz + " ccrt:sheetName \"" + name + "\".\n");
			ow.write(getModelSource(defaultModel) + "\n");
			ow.write("\n\n");
			ow.write("# modelSize=" + defaultModel.size() + "\n");
			ow.write(thiz + " a ccrt:DatasetDefaultModel. \n");
			ow.write("\n\n");
		}
		ow.flush();
	}

	@Override public void loadTTL(File file) throws IOException, NoSuchConversionException {
		final Model loaderModel = ModelFactory.createDefaultModel();
		final Model[] currentModel = new Model[] { ModelFactory.createDefaultModel(), null, null };
		final String[] modelName = new String[] { "" };
		final Map<String, Model> constits = new HashMap();
		final Repo.WithDirectory repo = (WithDirectory) this.getRepo(getValue());
		loaderModel.register(new StatementListener() {

			@Override public void addedStatement(Statement arg0) {
				System.out.println("Adding statement: " + arg0);
				String subjStr = "" + arg0.getSubject();
				if (subjStr.equals("self")) {
					// processing directive
					RDFNode r = arg0.getObject();
					if (r.isLiteral()) {
						// is a model start declaration;
						String baseURI = modelName[0] = r.asLiteral().getString();
						Model newModel = currentModel[0] = ModelFactory.createDefaultModel();
						currentModel[0].setNsPrefix("", baseURI);
					} else if (r.isResource()) {
						// is a model ending declaration (we dont clear)
						Resource rs = r.asResource();
						String type = rs.getLocalName();
						Model newModel = currentModel[0];
						newModel.setNsPrefixes(loaderModel.getNsPrefixMap());
						if (type.equals("DirectoryModel")) {
							currentModel[1] = currentModel[0];
						} else if (type.equals("RepoSheetModel")) {
							constits.put(modelName[0], currentModel[0]);
						} else if (type.equals("DatasetDefaultModel")) {
							currentModel[2] = currentModel[0];
						}
					}
				} else {
					currentModel[0].add(arg0);
				}
			}
		});
		FileInputStream fis = new FileInputStream(file);
		InputStreamReader isr = new InputStreamReader(fis, Charset.defaultCharset().name());
		try {
			loaderModel.read(isr, null, "TTL");
		} catch (Throwable t) {
			Debuggable.printStackTrace(t);
		}
		// create a direct repo now
		Repo.WithDirectory rwd = null;

		if (constits.size() == 0) {
			// we probably need to load
			rwd = new OmniLoaderRepo(currentModel[1]);
		} else {
			// we already have loaded most likely
			rwd = new DirectRepo(currentModel[1]);
		}
		Dataset ds = rwd.getMainQueryDataset();
		if (currentModel[2] != null)
			ds.setDefaultModel(currentModel[2]);

		for (Map.Entry<String, Model> entry : constits.entrySet()) {
			ds.addNamedModel(entry.getKey(), entry.getValue());
		}
		if (repo.getClass() == rwd.getClass()) {
			RepoOper.replaceModelElements(repo.getDirectoryModel(), rwd.getDirectoryModel());
			RepoOper.replaceDatasetElements(repo.getMainQueryDataset(), ds);
		} else {
			setRepoObject(rwd);
		}

	}

	private String addNamedModel(String name, Model m) {
		String baseURI = getBaseURI(m, name);

		m.setNsPrefix("", baseURI);
		if (!baseURIToGraph.containsKey(baseURI)) {
			if (baseURI.indexOf(":") == -1 && baseURI.length() > 0) {
				baseURI = "file:" + baseURI;
			}
			baseURIToGraph.put(baseURI, m);
		}
		String fileURI = baseURI;
		if (!filenamesToGraph.containsKey(fileURI)) {
			if (fileURI.indexOf(":") == -1 && fileURI.length() > 0) {
				fileURI = "file:" + fileURI;
			}
			filenamesToGraph.put(fileURI, m);
		}
		add(m, name);
		return baseURI;
	}

	@Override public Class<Repo> getClassOfBox() {
		return EDITTYPE[0];
	}

	@Override protected boolean reloadObjectGUI(Object obj) throws Throwable {
		setRepoObject(getRepo(obj));
		return true;
	}

	private Repo getRepo(Object obj) throws NoSuchConversionException {
		return (Repo) Utility.recast(obj, getClassOfBox());
	}
}