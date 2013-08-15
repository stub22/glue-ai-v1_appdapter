package org.appdapter.gui.swing;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.text.JTextComponent;
import javax.swing.text.Utilities;

import org.appdapter.gui.api.BT;
import org.appdapter.gui.api.DisplayContext;
import org.appdapter.gui.api.POJOCollectionListener;
import org.appdapter.gui.browse.Utility;
import org.appdapter.gui.util.ClassFinder;
import org.appdapter.gui.util.PromiscuousClassUtilsA;

import com.jidesoft.swing.AutoCompletion;
import com.jidesoft.swing.ComboBoxSearchable;
import com.jidesoft.swing.SearchableUtils;

public class ClassChooserPanel extends JPanel implements ActionListener, DocumentListener, POJOCollectionListener {
	Class selectedClass = String.class;

	//JLayeredPane desk;
	//JSplitPane split;
	JButton classBrowserButton;
	JComboBox classField;
	DisplayContext context;
	HashSet<String> classesSaved = new HashSet<String>();
	HashSet<String> classesShown = new HashSet<String>();
	Thread classGroveler;

	private ComboBoxSearchable searchable;

	public ClassChooserPanel(DisplayContext context0) {
		super(true);
		this.context = context0;
		Utility.registerEditors();
		Utility.setBeanInfoSearchPath();
		initGUI();
		if (autoCompletion == null) {
			autoCompletion = new AutoCompletion(classField);
			autoCompletion.setStrict(false);
			autoCompletion.setStrictCompletion(false);
			searchable = SearchableUtils.installSearchable(classField);
			this.searchable.setCaseSensitive(false);
			searchable.setWildcardEnabled(true);
			searchable.setCountMatch(true);
		}
		synchronized (classesSaved) {
			classesSaved.add(String.class.getName());
			classesSaved.add(Boolean.class.getName());
			classesSaved.add(Integer.class.getName());
			classesSaved.add(Float.class.getName());
		}
		startClassGroveler();
	}

	private void startClassGroveler() {
		if (classGroveler == null) {
			classGroveler = new Thread("Class groveler") {
				public void run() {
					try {
						Set set = ClassFinder.getClassNames("java.");
						synchronized (classesSaved) {
							for (Object s : set) {
								classesSaved.add("" + s);
							}
						}
					} catch (Throwable t) {

					}
					Utility.uiObjects.addListener(ClassChooserPanel.this, true);
					while (true) {
						resetAutoComplete();
						try {
							Thread.sleep(30000);
						} catch (InterruptedException e) {
						}
					}
				}
			};
		}
		classGroveler.start();
	}

	AutoCompletion autoCompletion;

	private void resetAutoComplete() {
		/*  JIDESOFT
		 		 */

		synchronized (classesSaved) {
			for (Class c : PromiscuousClassUtilsA.getInstalledClasses()) {
				classAdd0(c);
			}
		}

		final List<String> copy;
		synchronized (classesSaved) {
			if (classesSaved.size() == classesShown.size())
				return;
			copy = new ArrayList<String>(classesSaved);
		}

		Utility.invokeAndWait(new Runnable() {

			@Override public void run() {

				try {
					classField.setEnabled(false);
					for (String c : copy) {
						if (!classesShown.add(c)) {
							classField.addItem(c);
						}
					}
				} finally {
					classField.setEnabled(true);
				}
			}
		});
	}

	@Override public void actionPerformed(ActionEvent evt) {
		Object evtsrc = evt.getSource();
		classBrowserButton.setEnabled(true);
		if (evtsrc == classField) {
			String className = "" + classField.getSelectedItem();
			try {
				selectedClass = PromiscuousClassUtilsA.forName(className);
			} catch (Throwable e) {
			}
			classBrowserButton.setEnabled(selectedClass != null);
		} else {
			if (evtsrc == classBrowserButton) {
				openClassBrowser();
			}
		}
	}

	private synchronized void openClassBrowser() {
		if (selectedClass != null) {
			try {
				Utility.browserPanel.showScreenBox(selectedClass);
			} catch (Throwable err) {
				Utility.showError(context, null, err);
			}
		}
	}

	void initGUI() {
		removeAll();

		adjustSize();

		classBrowserButton = new JButton("Examine...");
		classBrowserButton.setToolTipText("Opens a new window that lets you examine classes and create new object instances");
		classBrowserButton.addActionListener(this);
		classBrowserButton.setEnabled(false);
		//classBrowserButton.setActionCommand(COMMAND_CREATE_BEAN);

		DefaultComboBoxModel dcm = new DefaultComboBoxModel() {
			// implements javax.swing.MutableComboBoxModel
			protected void fireIntervalAdded(Object source, int index0, int index1) {
				//if (true) return;
				Object[] listeners = listenerList.getListenerList();
				ListDataEvent e = null;

				for (int i = listeners.length - 2; i >= 0; i -= 2) {
					if (listeners[i] == ListDataListener.class) {
						if (e == null) {
							e = new ListDataEvent(source, ListDataEvent.INTERVAL_ADDED, index0, index1);
						}
						((ListDataListener) listeners[i + 1]).intervalAdded(e);
					}
				}
			}
		};
		classField = new JComboBox(dcm) {

			@Override public void addItem(Object obj) {
				int count = getItemCount();
				String toAdd = (String) obj;

				List<String> items = new ArrayList<String>();
				for (int i = 0; i < count; i++) {
					items.add((String) getItemAt(i));
				}

				if (items.size() == 0) {
					super.addItem(toAdd);
					return;
				} else {
					if (toAdd.compareTo(items.get(0)) <= 0) {
						insertItemAt(toAdd, 0);
					} else {
						int lastIndexOfHigherNum = 0;
						for (int i = 0; i < count; i++) {
							if (toAdd.compareTo(items.get(i)) > 0) {
								lastIndexOfHigherNum = i;
							}
						}
						insertItemAt(toAdd, lastIndexOfHigherNum + 1);
					}
				}
			}
		};
		classField.setSize(400, (int) classField.getSize().getHeight());
		classField.addActionListener(this);
		classField.setEnabled(true);

		final JTextComponent tc = (JTextComponent) classField.getEditor().getEditorComponent();
		tc.getDocument().addDocumentListener(this);

		setBorder(new TitledBorder("Class browser"));
		setLayout(new BorderLayout());
		add("North", new JLabel("Full class name:"));
		add("Center", classField);
		add("East", classBrowserButton);
	}

	private void adjustSize() {
		Container p = getParent();
		if (p != null) {
			setSize(p.getSize());
		}
	}

	private void classFieldChanged() {
		try {
			final JTextComponent tc = (JTextComponent) classField.getEditor().getEditorComponent();
			selectedClass = PromiscuousClassUtilsA.forName(tc.getText());
		} catch (Exception err) {
			selectedClass = null;
		}
		classBrowserButton.setEnabled(selectedClass != null || true);
	}

	@Override public void insertUpdate(DocumentEvent e) {
		classFieldChanged();
	}

	@Override public void removeUpdate(DocumentEvent e) {
		classFieldChanged();
	}

	@Override public void changedUpdate(DocumentEvent e) {
		classFieldChanged();
	}

	public Object getValue() {
		return selectedClass;
	}

	@Override public void pojoAdded(Object obj, BT box, Object senderCollection) {
		synchronized (classesSaved) {
			if (obj instanceof Class) {
				classAdded((Class) obj);
			} else {
				if (obj != null)
					classAdded(obj.getClass());
			}
		}
		//invalidate();
	}

	public void classAdded(Class clz) {
		if (clz == null)
			return;
		if (!classAdd0(clz))
			return;
		for (Class c : clz.getInterfaces()) {
			classAdd0(c);
		}
		classAdded(clz.getSuperclass());

	}

	private boolean classAdd0(Class clz) {
		String clzname = clz.getCanonicalName();
		if (clzname == null) {
			clzname = clz.getName();
		}
		synchronized (classesSaved) {
			if (!classesSaved.add(clzname))
				return false;
		}
		return true;
	}

	@Override public void pojoRemoved(Object obj, BT box, Object senderCollection) {
		// TODO Auto-generated method stub

	}

}
