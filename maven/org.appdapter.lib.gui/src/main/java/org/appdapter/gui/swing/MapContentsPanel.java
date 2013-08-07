package org.appdapter.gui.swing;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyVetoException;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.OverlayLayout;
import javax.swing.border.Border;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.appdapter.api.trigger.Box;
import org.appdapter.core.convert.ReflectUtils;
import org.appdapter.gui.api.BT;
import org.appdapter.gui.api.BoxPanelSwitchableView;
import org.appdapter.gui.api.Chooser;
import org.appdapter.gui.api.DisplayContext;
import org.appdapter.gui.api.NamedObjectCollection;
import org.appdapter.gui.api.ObjectCollectionRemoveListener;
import org.appdapter.gui.api.POJOCollectionListener;
import org.appdapter.gui.browse.Utility;

/**
 * A GUI component that shows what a Map contains,
 * and lets you add and remove elements.
 *
 * 
 */
public class MapContentsPanel<BoxType extends Box>

extends ScreenBoxPanel<BoxType> implements ObjectCollectionRemoveListener, DropTargetListener, Chooser<Object>

, ChangeListener, POJOCollectionListener {

	public Map getValue() {
		return getMap();
	}

	@Override public Dimension getPreferredSize() {
		return new Dimension(130, 200);
		// return Utility.getMaxDimension(new Dimension(250, 200),
		// super.getPreferredSize());
	}

	@Override public Dimension getMinimumSize() {
		return getPreferredSize();
	}

	@Override public void pojoAdded(Object obj, BT box, Object senderMap) {
		// @optimize
		if (filter != null) {
			if (!filter.isInstance(obj))
				return;
		}
		reloadContents();
	}

	@Override public void pojoRemoved(Object obj, BT box, Object from) {
		// @optimize
		if (filter != null) {
			if (!filter.isInstance(obj))
				return;
		}
		reloadContents();
	}

	CantankerousJob reloadConts = new CantankerousJob("reloadContents", this) {
		@Override public void run() {
			reloadContents00();
		}
	};
	private Object selectedObject;

	public void reloadContents() {
		reloadConts.attempt();
	}

	public void reloadContents00() {

		final Map localMap = getMap();
		panel.removeAll();
		Iterator it;

		it = localMap.entrySet().iterator();

		while (it.hasNext()) {
			Object value = it.next();
			if (filter != null) {
				if (!filter.isInstance(value))
					continue;
			}
			SmallObjectView view = new SmallObjectView(context, localNOC, value, localMap.entrySet()) {
				@Override public void valueChanged(Object oldValue, Object newVal) {
					Map.Entry newValue = (Map.Entry) newVal;
					replaceInContext(localNOC, oldValue, newValue);
					replace(localMap, oldValue, newValue);
					super.valueChanged(oldValue, newValue);
				}
			};
			view.setRemoveListener(new ObjectCollectionRemoveListener() {
				@Override public void objectRemoved(Object oldValue, Collection parent) {
					replaceInContext(localNOC, oldValue, null);
				}
			});
			view.setRemoveListener(this);
			panel.add(view);
		}
		invalidate();
		validate();
		repaint();
	}

	protected void replaceInContext(NamedObjectCollection context2, Object oldValue, Map.Entry newValue) {
		Object shouldBeenOldValue = context2.findOrCreateBox(oldValue).setValue(newValue);
	}

	public Object getSelectedObject() {
		if (!valueIsOneSelectedItem)
			return super.getValue();
		return selectedObject;
	}

	public void setSelectedObject(Object object) throws PropertyVetoException {

		this.selectedObject = (object);

	}

	@Override public void stateChanged(ChangeEvent evt) {
		boolean isSelected = parentTabs.getSelectedIndex() == parentTabs.indexOfComponent(this);
		if (wasSelected != isSelected) {
			if (isSelected) {
				this.reloadContents();
			}
		}
		wasSelected = isSelected;
	}

	boolean wasSelected = false;
	BoxPanelSwitchableView parentTabs;
	Map localMap;
	DisplayContext context;
	JPanel panel;
	JScrollPane scroll;
	Border defaultScrollBorder;
	JButton reloadButton;

	DropTarget dropTarget;

	//An invisible panel in front of the list of contents, which
	//captures drag/drop operations
	JPanel dropGlass;
	NamedObjectCollection localNOC;
	Class filter;
	boolean valueIsOneSelectedItem = false;
	private String titleString;

	@Override public String getName() {
		if (titleString != null)
			return titleString;
		String named = localMap.toString();
		if (filter != null) {
			named = Utility.getShortClassName(filter) + " of " + named;
		}
		return named;
	}

	public Map getMap() {
		Object v = super.getValue();
		if (v instanceof Map)
			return (Map) v;
		if (localMap == null)
			return null;
		return localNOC.getLiveMap();
	}

	public void setTitle(String title) {
		titleString = title;
		//defaultScrollBorder = new Defa
		//	scroll.setBorder(new TitledBorder(defaultScrollBorder, title));
	}

	public MapContentsPanel(DisplayContext context, String titleStr, Map localMap, Class filterc, NamedObjectCollection noc, BoxPanelSwitchableView tabs, boolean valueIsNotMap) {
		this.titleString = titleStr;
		this.valueIsOneSelectedItem = valueIsNotMap;
		this.context = context;
		this.filter = filterc;
		this.parentTabs = tabs;
		this.localNOC = noc;
		if (localMap == null && noc != null)
			localMap = noc.getLiveMap();
		this.objectValue = localMap;
		if (localNOC != null)
			localNOC.addListener(this, true);
	}

	public MapContentsPanel(DisplayContext context, String titleStr, Map localMap, BoxPanelSwitchableView tabs) {
		this(context, titleStr, localMap, null, null, tabs, false);
	}

	@Override protected void initSubclassGUI() throws Throwable {
		panel = new JJPanel();
		panel.setLayout(new VerticalLayout());

		dropGlass = new JJPanel();
		dropGlass.setOpaque(false);
		dropTarget = new DropTarget(dropGlass, this);
		scroll = new JScrollPane(panel);
		defaultScrollBorder = scroll.getBorder();
		JPanel buttonPanel = new JJPanel(new FlowLayout(FlowLayout.LEFT));
		reloadButton = new JButton("Update");
		buttonPanel.add(reloadButton);
		buttonPanel.add(new JLabel("To add objects just drag them into the panel below."));
		reloadButton.addActionListener(new ActionListener() {
			@Override public void actionPerformed(ActionEvent event) {
				reloadContents();
			}
		});

		JJPanel stack = new JJPanel();
		stack.setLayout(new OverlayLayout(stack));
		stack.add(dropGlass);
		stack.add(scroll);

		//setBorder(new TitledBorder(new , "XYZ"));
		setLayout(new BorderLayout());
		add("North", buttonPanel);
		add("Center", stack);
	}

	public void completeSubClassGUI() {
		setTitle(getName());
		reloadContents();
	}

	@Override public void objectRemoved(Object object, Collection parent) {
		if (parent != null) {
			parent.remove(object);
		}
		reloadContents();
	}

	static private void replace(Map localMap, Object oldValue, Map.Entry newValue) {
		Iterator it = localMap.entrySet().iterator();
		while (it.hasNext()) {
			final Object value = it.next();
			if (value == oldValue) {
				it.remove();
				localMap.put(newValue.getKey(), newValue.getValue());
				return;
			}
		}

	}

	//======= Drag/Drop methods ====================================0

	@Override public void dragEnter(DropTargetDragEvent event) {
		event.acceptDrag(DnDConstants.ACTION_MOVE);
	}

	@Override public void dragExit(DropTargetEvent dtde) {
	}

	@Override public void dragOver(DropTargetDragEvent dtde) {
	}

	@Override public void drop(DropTargetDropEvent event) {
		Transferable t = event.getTransferable();
		try {
			Object o = t.getTransferData(new DataFlavor(DataFlavor.javaJVMLocalObjectMimeType));
			Map.Entry entry = (Map.Entry) o;
			Map localMap = (Map) this.objectValue;
			localMap.put(entry.getKey(), entry.getValue());
			reloadContents();
		} catch (Exception err) {
			new ErrorDialog("An error occurred while handling a drop operation", err).show();
		}
	}

	@Override public void dropActionChanged(DropTargetDragEvent dtde) {
	}

	@Override protected boolean reloadObjectGUI(Object obj) throws Throwable {
		this.objectValue = ReflectUtils.recast(obj, Map.class);
		reloadContents();
		return true;
	}

	@Override public Class<Map> getClassOfBox() {
		return Map.class;
	}
}