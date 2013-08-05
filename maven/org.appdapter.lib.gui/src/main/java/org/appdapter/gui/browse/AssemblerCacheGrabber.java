package org.appdapter.gui.browse;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GraphicsEnvironment;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
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

import org.appdapter.api.trigger.AnyOper;
import org.appdapter.api.trigger.AnyOper.UISalient;
import org.appdapter.bind.rdf.jena.assembly.AssemblerUtils;
import org.appdapter.core.component.ComponentCache;
import org.appdapter.core.convert.ReflectUtils;
import org.appdapter.core.log.BasicDebugger;
import org.appdapter.core.log.Debuggable;
import org.appdapter.core.name.Ident;
import org.appdapter.gui.api.BT;
import org.appdapter.gui.api.NamedObjectCollection;

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

	public Map<Class, ComponentCache> getCacheMap() {
		return AssemblerUtils.getComponentCacheMap(AssemblerUtils.getDefaultSession());
	}

	public static String anyToString(Object any) {
		return "" + any;
	}

	@Override public String toString() {
		// TODO Auto-generated method stub
		return super.toString();
	}

	public boolean longThreadQuit = false;

	@UISalient public void loadAssemblerClasses() {
		final NamedObjectCollection bp = Utility.getTreeBoxCollection();
		Map<Class, ComponentCache> map = getCacheMap();
		final Object[] clzes;
		synchronized (map) {
			clzes = map.keySet().toArray();
		}
		setLongRunner(new Runnable() {
			@Override public void run() {
				for (Object c : clzes) {
					bp.findOrCreateBox(c);
					if (longThreadQuit) {
						longThreadQuit = false;
						return;
					}
				}
			}
		});
	}

	@UISalient() public void loadAssemblerInstances() {
		final NamedObjectCollection bp = Utility.getTreeBoxCollection();
		Map<Class, ComponentCache> cmap = getCacheMap();
		final Object[] clzes;
		synchronized (cmap) {
			clzes = cmap.values().toArray();
		}

		setLongRunner(new Runnable() {

			@Override public void run() {
				for (Object c : clzes) {
					Map<Ident, Object> map = (Map<Ident, Object>) ((ComponentCache) c).getCompCache();
					synchronized (map) {
						for (Map.Entry<Ident, Object> me : map.entrySet()) {
							if (longThreadQuit) {
								longThreadQuit = false;
								return;
							}
							Utility.recordCreated(bp, me.getKey(), me.getValue());
						}
					}
				}
			}
		});
	}
	

	protected String[] _fontNames;
	protected List<String> _fontList;

	public Component getDemoPanel() {
		_fontNames = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
		_fontList = Arrays.asList(_fontNames);

		JPanel panel1 = createPanel1();
		JPanel panel2 = createAutoCompleteForTree();

		JPanel panel = new JPanel(new BorderLayout(6, 6));
		panel.add(panel1, BorderLayout.BEFORE_FIRST_LINE);
		panel.add(panel2);
		return panel;
	}

	private JPanel createPanel1() {
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

	public JPanel createAutoCompleteForTree() {
		JPanel panel = new JPanel();
		panel.setLayout(new JideBoxLayout(panel, JideBoxLayout.Y_AXIS));
		panel.setBorder(BorderFactory.createCompoundBorder(new JideTitledBorder(new PartialEtchedBorder(PartialEtchedBorder.LOWERED, PartialSide.NORTH), "AutoCompletion with list and tree",
				JideTitledBorder.LEADING, JideTitledBorder.ABOVE_TOP), BorderFactory.createEmptyBorder(0, 0, 0, 0)));

		// create tree combobox
		final JTextField treeTextField = new JTextField();
		treeTextField.setName("AutoCompletion JTextField with JTree");
		SelectAllUtils.install(treeTextField);
		final JTree tree = Utility.browserPanel.getTree();
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

	@UISalient() public void loadAddedBoxes() {
		final NamedObjectCollection bp = Utility.getTreeBoxCollection();
		Map<Class, ComponentCache> cmap = getCacheMap();
		final Object[] clzes;
		synchronized (cmap) {
			clzes = cmap.values().toArray();
		}

		setLongRunner(new Runnable() {

			@Override public void run() {
				for (Object c : clzes) {
					Map<Object, BT> map = Utility.allBoxes;
					List<Entry<Object, BT>> es;
					synchronized (map) {
						es = ReflectUtils.copyOf(map.entrySet());
					}
					for (Map.Entry<Object, BT> me : es) {
						if (longThreadQuit) {
							longThreadQuit = false;
							return;
						}
						Object obj = me.getKey();
						Utility.asWrapped(obj);
					}
				}

			}
		});
	}

	private Object longThreadSync = this;
	public Thread longThread;

	synchronized void setLongRunner(final Runnable longRunner) {

		synchronized (longThreadSync) {
			if (this.longThread != null) {
				longThreadQuit = true;
				try {
					longThread.join();
				} catch (InterruptedException e) {
				}
			}
			longThread = new Thread() {
				public void destroy() {
					longThreadQuit = true;
				}

				public void run() {
					longRunner.run();
					//synchronized (longThreadSync) 
					{
						if (longThread == Thread.currentThread()) {
							longThread = null;
						}
					}
				};
			};
			longThreadQuit = false;
			longThread.start();
		}

	}

	@UISalient public void loadBasicDebuggerInstances() {
		Collection all = Debuggable.allObjectsForDebug;
		final NamedObjectCollection bp = Utility.getTreeBoxCollection();
		final Collection allCopy;
		synchronized (all) {
			allCopy = new LinkedList(all);
		}
		setLongRunner(new Runnable() {

			@Override public void run() {
				for (Object o : allCopy) {
					if (o.getClass() == BasicDebugger.class)
						continue;
					if (longThreadQuit) {
						longThreadQuit = false;
						return;
					}
					bp.findOrCreateBox(o);
				}
			}
		});
	}
}
