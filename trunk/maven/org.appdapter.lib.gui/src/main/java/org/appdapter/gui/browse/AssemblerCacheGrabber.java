package org.appdapter.gui.browse;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GraphicsEnvironment;
import java.util.*;
import java.util.Map.Entry;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTree;

import org.appdapter.api.trigger.ABoxImpl;
import org.appdapter.api.trigger.AnyOper;
import org.appdapter.api.trigger.AnyOper.UIHidden;
import org.appdapter.api.trigger.AnyOper.UISalient;
import org.appdapter.bind.rdf.jena.assembly.AssemblerUtils;
import org.appdapter.core.component.ComponentCache;
import org.appdapter.core.convert.ReflectUtils;
import org.appdapter.core.log.BasicDebugger;
import org.appdapter.core.log.Debuggable;
import org.appdapter.core.name.Ident;
import org.appdapter.gui.api.BT;
import org.appdapter.gui.api.NamedObjectCollection;
import org.appdapter.gui.swing.CantankerousJob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jidesoft.swing.AutoCompletion;
import com.jidesoft.swing.AutoCompletionComboBox;
import com.jidesoft.swing.JideBoxLayout;
import com.jidesoft.swing.JideTitledBorder;
import com.jidesoft.swing.ListSearchable;
import com.jidesoft.swing.PartialEtchedBorder;
import com.jidesoft.swing.PartialSide;
import com.jidesoft.swing.SelectAllUtils;
import com.jidesoft.swing.TreeSearchable;

public class AssemblerCacheGrabber extends BasicDebugger implements AnyOper.Singleton, AnyOper.Autoload {

	static Logger theLogger = LoggerFactory.getLogger(AssemblerCacheGrabber.class);
	static int instances = 0;

	public AssemblerCacheGrabber() {
		instances++;
		theLogger.warn("Made this " + instances);
	}

	@UISalient(ResultIsSingleton = true)//
	public Map<Class, ComponentCache> getCacheMap() {
		return AssemblerUtils.getComponentCacheMap(AssemblerUtils.getDefaultSession());
	}

	public static String anyToString(Object any) {
		return "" + any;
	}

	@UISalient public void loadAssemblerClasses() {
		setLongRunner("loadAssemblerClasses", new Runnable() {
			@Override public void run() {
				final NamedObjectCollection bp = Utility.getTreeBoxCollection();
				Map<Class, ComponentCache> map = getCacheMap();
				final Object[] clzes;
				synchronized (map) {
					clzes = map.keySet().toArray();
				}
				for (Object c : clzes) {
					bp.findOrCreateBox(c);
				}
			}
		});
	}

	@UISalient() public void loadAssemblerInstances() {
		setLongRunner("loadAssemblerInstances", new Runnable() {

			@Override public void run() {
				final NamedObjectCollection bp = Utility.getTreeBoxCollection();
				Map<Class, ComponentCache> cmap = getCacheMap();
				final Object[] clzes;
				synchronized (cmap) {
					clzes = cmap.values().toArray();
				}
				for (Object c : clzes) {
					Map<Ident, Object> map = (Map<Ident, Object>) ((ComponentCache) c).getCompCache();
					synchronized (map) {
						for (Map.Entry<Ident, Object> me : map.entrySet()) {
							Utility.recordCreated(bp, me.getKey(), me.getValue());
						}
					}
				}
			}
		});
	}

	@UISalient() public void loadAddedBoxes() {
		setLongRunner("loadAddedBoxes", new Runnable() {

			@Override public void run() {
				final NamedObjectCollection bp = Utility.getTreeBoxCollection();
				Map<Class, ComponentCache> cmap = getCacheMap();
				final Object[] clzes;
				synchronized (cmap) {
					clzes = cmap.values().toArray();
				}
				for (Object c : clzes) {
					Map<Object, BT> map = Utility.allBoxes;
					List<Entry<Object, BT>> es;
					synchronized (map) {
						es = ReflectUtils.copyOf(map.entrySet());
					}
					for (Map.Entry<Object, BT> me : es) {
						Object obj = me.getKey();
						Utility.asWrapped(obj);
					}
				}

			}
		});
	}

	public Map<String, CantankerousJob> longThreads = new HashMap();
	private Object longThreadSync = longThreads;

	synchronized void setLongRunner(String named, final Runnable longRunner) {

		synchronized (longThreadSync) {
			CantankerousJob cj = longThreads.get(named);
			if (cj == null) {
				cj = new CantankerousJob(named, this) {

					@Override public void run() {
						longRunner.run();

					}
				};
				longThreads.put(named, cj);
			}
			cj.attempt();
		}

	}

	@UISalient public void loadBasicDebuggerInstances() {
		setLongRunner("loadBasicDebuggerInstances", new Runnable() {
			@Override public void run() {
				Collection all = Debuggable.allObjectsForDebug;
				final NamedObjectCollection bp = Utility.getTreeBoxCollection();
				final Collection allCopy;
				synchronized (all) {
					allCopy = new LinkedList(all);
				}
				for (Object o : allCopy) {
					if (o.getClass() == BasicDebugger.class)
						continue;
					bp.findOrCreateBox(o);
				}
			}
		});
	}

	static protected String[] _fontNames;
	static protected List<String> _fontList;

	@UISalient public Component getDemoPanel() {
		_fontNames = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
		_fontList = Arrays.asList(_fontNames);

		JPanel panel1 = createPanel1();
		JPanel panel2 = createAutoCompleteForTree();

		JPanel panel = new JPanel(new BorderLayout(6, 6));
		panel.add(panel1, BorderLayout.BEFORE_FIRST_LINE);
		panel.add(panel2);
		return panel;
	}

	@UIHidden private JPanel createPanel1() {
		JPanel panel = new JPanel();
		panel.setLayout(new JideBoxLayout(panel, JideBoxLayout.Y_AXIS));
		panel.setBorder(BorderFactory.createCompoundBorder(new JideTitledBorder(new PartialEtchedBorder(PartialEtchedBorder.LOWERED, PartialSide.NORTH), "AutoCompletion combo box and text field",
				JideTitledBorder.LEADING, JideTitledBorder.ABOVE_TOP), BorderFactory.createEmptyBorder(0, 0, 0, 0)));

		JComboBox autoCompletionComboBox = new AutoCompletionComboBox(_fontNames);
		autoCompletionComboBox.setName("AutoCompletion JComboBox (Strict)");
		autoCompletionComboBox.setToolTipText("AutoCompletion JComboBox (Strict)");
		panel.add(new JLabel("AutoCompletion JComboBox (Strict)"));
		panel.add(Box.createVerticalStrut(3), JideBoxLayout.FIX);
		panel.add(autoCompletionComboBox);
		panel.add(Box.createVerticalStrut(12), JideBoxLayout.FIX);

		AutoCompletionComboBox autoCompletionComboBoxNotStrict = new AutoCompletionComboBox(_fontNames);
		autoCompletionComboBoxNotStrict.setStrict(false);
		autoCompletionComboBoxNotStrict.setName("AutoCompletion JComboBox (Not strict)");
		autoCompletionComboBoxNotStrict.setToolTipText("AutoCompletion JComboBox (Not strict)");
		panel.add(new JLabel("AutoCompletion JComboBox (Not strict)"));
		panel.add(Box.createVerticalStrut(3), JideBoxLayout.FIX);
		panel.add(autoCompletionComboBoxNotStrict);
		panel.add(Box.createVerticalStrut(12), JideBoxLayout.FIX);

		// create tree combobox
		final JTextField textField = new JTextField();
		textField.setName("AutoCompletion JTextField with a hidden data");
		SelectAllUtils.install(textField);
		new AutoCompletion(textField, _fontList);
		panel.add(new JLabel("AutoCompletion JTextField with a hidden data"));
		panel.add(Box.createVerticalStrut(3), JideBoxLayout.FIX);
		panel.add(textField);
		panel.add(Box.createVerticalStrut(12), JideBoxLayout.FIX);

		//        panel.add(Box.createVerticalStrut(24), JideBoxLayout.FIX);
		//        panel.add(new JLabel("As comparisons:"));
		//        panel.add(Box.createVerticalStrut(6), JideBoxLayout.FIX);
		//
		//        JComboBox searchableComboBox = new JComboBox(_fontNames);
		//        searchableComboBox.setEditable(false);
		//        SearchableUtils.installSearchable(searchableComboBox);
		//        searchableComboBox.setToolTipText("Searchable JComboBox");
		//        panel.add(new JLabel("Searchable JComboBox"));
		//        panel.add(Box.createVerticalStrut(3), JideBoxLayout.FIX);
		//        panel.add(searchableComboBox);
		//        panel.add(Box.createVerticalStrut(12), JideBoxLayout.FIX);
		//
		//        JTextField completionTextField = new JTextField();
		//        new ListCompletion(completionTextField, _fontNames);
		//        completionTextField.setToolTipText("Completion JTextField (not auto-complete)");
		//        panel.add(new JLabel("Completion JTextField (not auto-complete)"));
		//        panel.add(Box.createVerticalStrut(3), JideBoxLayout.FIX);
		//        panel.add(completionTextField);
		//        panel.add(Box.createVerticalStrut(12), JideBoxLayout.FIX);

		return panel;
	}

	static public JPanel createAutoCompleteForTree() {
		JPanel panel = new JPanel();
		JTree tree = new JTree();
		panel.setLayout(new JideBoxLayout(panel, JideBoxLayout.Y_AXIS));
		/*panel.setBorder(BorderFactory.createCompoundBorder(new JideTitledBorder(new PartialEtchedBorder(PartialEtchedBorder.LOWERED, PartialSide.NORTH), "AutoCompletion with list and tree",
				JideTitledBorder.LEADING, JideTitledBorder.ABOVE_TOP), BorderFactory.createEmptyBorder(0, 0, 0, 0)));
		*/
		// create tree combobox
		final JTextField treeTextField = new JTextField();
		treeTextField.setName("AutoCompletion JTextField with JTree");
		SelectAllUtils.install(treeTextField);
		tree.setVisibleRowCount(10);
		final TreeSearchable searchable = new TreeSearchable(tree);
		searchable.setRecursive(true);
		new AutoCompletion(treeTextField, searchable);
		panel.add(new JLabel("AutoCompletion JTextField with JTree"));
		panel.add(Box.createVerticalStrut(3), JideBoxLayout.FIX);
		panel.add(treeTextField);
		panel.add(Box.createVerticalStrut(2), JideBoxLayout.FIX);
		panel.add(new JScrollPane(tree));
		panel.add(Box.createVerticalStrut(12), JideBoxLayout.FIX);

		if (true)
			return panel;
		_fontNames = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
		_fontList = Arrays.asList(_fontNames);

		// create font name combobox
		final JTextField fontNameTextField = new JTextField();
		fontNameTextField.setName("AutoCompletion JTextField with JList");
		SelectAllUtils.install(fontNameTextField);
		final JList fontNameList = new JList(_fontNames);
		fontNameList.setVisibleRowCount(10);
		new AutoCompletion(fontNameTextField, new ListSearchable(fontNameList));
		panel.add(new JLabel("AutoCompletion JTextField with JList"));
		panel.add(Box.createVerticalStrut(3), JideBoxLayout.FIX);
		panel.add(fontNameTextField);
		panel.add(Box.createVerticalStrut(2), JideBoxLayout.FIX);
		panel.add(new JScrollPane(fontNameList));
		panel.add(Box.createVerticalStrut(12), JideBoxLayout.FIX);

		return panel;
	}

}
