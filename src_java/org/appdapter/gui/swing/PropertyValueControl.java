package org.appdapter.gui.swing;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.beans.BeanInfo;
import java.beans.EventSetDescriptor;
import java.beans.IntrospectionException;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyDescriptor;
import java.beans.PropertyEditor;
import java.beans.PropertyEditorSupport;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.appdapter.gui.browse.DisplayContext;
import org.appdapter.gui.pojo.GetSetObject;
import org.appdapter.gui.pojo.ScreenBoxedPOJORefPanel;
import org.appdapter.gui.pojo.Utility;
import org.appdapter.gui.swing.impl.JVPanel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PropertyValueControl extends JVPanel implements PropertyChangeListener, GetSetObject, ValueEditor {
	static Logger theLogger = LoggerFactory.getLogger(PropertyValueControl.class);

	DisplayContext context = Utility.getCurrentContext();

	Class type = null;
	Object value = null;
	boolean editable = true;
	PropertyDescriptor property = null;
	Object source = null;
	PropertyEditor currentEditor = null;
	boolean showLabel = false;

	public PropertyValueControl() {
		this((Object) null, true);
	}

	public PropertyValueControl(boolean editable) {
		this((Object) null, editable);
	}

	public PropertyValueControl(DisplayContext context, Object source, PropertyDescriptor property) {
		if (context != null)
			this.context = context;
		bind(source, property);
	}

	public PropertyValueControl(DisplayContext context, Object source, String propertyName) throws IntrospectionException {
		this(context, source, getPropertyDescriptor(source, propertyName));
	}

	public PropertyValueControl(Object source, PropertyDescriptor property) {
		this(null, source, property);
	}

	public PropertyValueControl(Object source, String propertyName) throws IntrospectionException {
		this(source, getPropertyDescriptor(source, propertyName));
	}

	public PropertyValueControl(Class type, boolean editable) {
		this(Utility.getCurrentContext(), type, editable);
	}

	/**
	 * Creates an unbound PropertyValueControl of the given type. It will be
	 * initialized to a default value, for non-primitive this is null and for
	 * primitives it is 0, false, or whatever.
	 */
	public PropertyValueControl(DisplayContext context, Class type, boolean editable) {
		this.context = context;
		this.type = type;
		this.editable = editable;
		this.value = getDefaultValue(type);
		recreateGUI();
	}

	public PropertyValueControl(Object value, boolean editable) {
		if (value != null) {
			this.type = value.getClass();
		}
		this.value = value;
		this.editable = editable;
		recreateGUI();
	}

	public void showLabel(boolean b) {
		this.showLabel = b;
		recreateGUI();
	}

	public boolean isBound() {
		return source != null;
	}

	public void bind(Object source, PropertyDescriptor property) {
		this.property = property;
		this.source = source;
		this.type = property.getPropertyType();
		this.editable = (property.getWriteMethod() != null);
		// readBoundValue();
		recreateGUI();
	}

	public void unbind() {
		this.property = null;
		this.source = null;
	}

	public boolean isEditable() {
		return editable;
	}

	public void setEditable(boolean editable) {
		if (editable != this.editable) {
			if (isBound() && editable == true) {
				if (property.getWriteMethod() != null) {
					this.editable = true;
					recreateGUI();
				}
			} else {
				this.editable = editable;
				recreateGUI();
			}
		}
	}

	private static PropertyDescriptor getPropertyDescriptor(Object object, String propName) throws IntrospectionException {
		BeanInfo info = Utility.getBeanInfo(object.getClass());
		PropertyDescriptor[] array = info.getPropertyDescriptors();
		int len = array.length;
		for (int i = 0; i < len; ++i) {
			PropertyDescriptor pd = array[i];
			if (pd.getName().equals(propName)) {
				return pd;
			}
		}
		throw new IntrospectionException("No such property: " + propName);
	}

	private void readBoundValue() {
		if (isBound()) {
			Throwable realCause;
			Object obj = source;
			try {
				Method readMethod = property.getReadMethod();
				if (readMethod == null) {
					String pn = property.getDisplayName();
					return;
				}
				if (readMethod == null) {
					throw new Exception("readMethod = null for object " + obj + " and property '" + property.getName() + "'!!!");
				}
				Object boundValue = readMethod.invoke(obj, new Object[0]);
				setValue(boundValue);
				return;

			} catch (InvocationTargetException err) {
				realCause = err.getCause();
			} catch (Exception err) {
				realCause = err;
				Throwable rc = realCause.getCause();
				while (rc != null && rc != realCause) {
					realCause = rc;
					rc = realCause.getCause();
				}
			}
			try {
				setValue(null);
			} catch (Exception err2) {
				theLogger.error("An error occurred", err2);
			}
			if (realCause instanceof java.awt.IllegalComponentStateException)
				return;
			theLogger.error("An error occurred", realCause);

		} else {
			theLogger.warn("PropertyValueControl warning: ValueView.readBoundValue should only be called if value is bound!");
		}

	}

	private void readEditorValue() {
		if (currentEditor != null) {
			if (!(Utility.isEqual(value, currentEditor.getValue()))) {
				try {
					setValue(currentEditor.getValue());
				} catch (Exception err) {
					theLogger.error("An error occurred", err);
				}
			}
		}
	}

	private void writeBoundValue() throws Exception {
		if (editable) {
			if (isBound()) {
				Object obj = source;
				property.getWriteMethod().invoke(obj, new Object[] { value });
			} else {
				theLogger.warn("PropertyValueControl warning: ValueView.writeBoundValue should only be called if value is bound!");
			}
		}
	}

	private void writeEditorValue() {
		if (currentEditor != null) {
			if (!(Utility.isEqual(value, currentEditor.getValue()))) {
				currentEditor.setValue(value);
			}
		}
	}

	public Object getValue() {
		return value;
	}

	/**
	 * Returns the type of this PropertyValueControl, if there is a fixed type.
	 * For example if this is String then this PropertyValueControl can only be
	 * used to create and view Strings.
	 */
	public Class getFixedType() {
		return type;
	}

	/**
	 * Returns the current type of the value in this PropertyValueControl. If
	 * there is a fixed type that will be returned instead. If the there is no
	 * value set and no fixed type, null will be returned.
	 */
	public Class getCurrentType() {
		if (type == null) {
			if (value == null) {
				return null;
			} else {
				return value.getClass();
			}
		} else {
			return type;
		}
	}

	public void setFixedType(Class newType) {
		Class oldType = type;
		if (oldType != newType) {
			type = newType;
			if (newType != null) {
				if (value != null && value.getClass() != newType) {
					try {
						setValue(null);
					} catch (Exception err) {
						theLogger.error("An error occurred", err);
					}
				}
			}
		}
	}

	/**
	 * Sets the value in this PropertyValueControl to the default for the
	 * variable type. For example if the type is String the value will be null,
	 * if the type is int the value will be 0, etc.
	 */
	public void resetValue() throws Exception {
		Class currentType = getCurrentType();
		Object defaultValue = getDefaultValue(currentType);
		setValue(defaultValue);
	}

	@Override public void setObject(Object newValue) throws InvocationTargetException {
		try {
			setValue(newValue);
		} catch (RuntimeException e) {
			throw e;
		} catch (Error e) {
			throw e;
		} catch (InvocationTargetException e) {
			throw e;
		} catch (Exception e) {
			throw new RuntimeException(e);
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	public void setValue(Object newValue) throws Exception {
		Object oldValue = value;
		if (!Utility.isEqual(newValue, oldValue)) {
			Class oldType = getCurrentType();

			// @warning !!!! the code below was commented out
			// because I was having trouble with int.class vs Integer.class
			// (and stuff like that). This may cause trouble...

			// make sure type is correct, if restriction is set.
			/*
			 * if (newValue != null || type != null) {// &&
			 * !(type.isAssignableFrom(newValue.getClass()))) { throw new
			 * IllegalArgumentException("object must be of type " + type + " - "
			 * + newValue + " is invalid."); }
			 */

			this.value = newValue;
			Class newType = getCurrentType();
			if (newType == oldType) {
				// if this is bound, update the object's property value.
				if (isBound()) {
					try {
						writeBoundValue();
					} catch (Exception err) {
						readBoundValue();
					}
				}
				writeEditorValue();
			} else {
				recreateGUI();
			}
		}
	}

	private synchronized void recreateGUI() {
		removeAll();
		setLayout(new BorderLayout());

		if (currentEditor != null)
			currentEditor.removePropertyChangeListener(this);
		stopListeningToSource();

		currentEditor = null;
		Component comp = null;

		if (type == null) {
			// untyped, so I can assume it is unbound
			if (value == null) {
				// I have no idea what the type should be
				currentEditor = getEditor(String.class, editable);
				currentEditor.setValue(null);
				comp = getEditorComponent(currentEditor, editable);
			} else {
				// AHA, I have a value, that means I can check the current type!
				Class currentType = value.getClass();
				currentEditor = getEditor(currentType, editable);
				currentEditor.setValue(value);
				comp = getEditorComponent(currentEditor, editable);
			}
		} else {
			// The type is fixed
			currentEditor = getEditor(type, editable);
			// currentEditor.setValue(value);
			comp = getEditorComponent(currentEditor, editable);
			if (isBound()) {
				// It is bound, so I have to listen for changes
				readBoundValue();
				listenToSource();
			}
			writeEditorValue();
		}

		if (comp != null) {
			if (showLabel && property != null) {
				add("West", new JLabel(property.getDisplayName() + ": "));
			}
			add("Center", comp);
		}
		if (currentEditor != null)
			currentEditor.addPropertyChangeListener(this);
	}

	@Override public void propertyChange(PropertyChangeEvent evt) {
		if (isBound() && evt.getSource() == source) {
			// the source object's property changed...
			readBoundValue();
		} else if (evt.getSource() == currentEditor) {
			// the editor's value changed
			readEditorValue();
		}
	}

	/**
	 * Start listening to the source object
	 */
	private void listenToSource() {
		try {
			if (source != null) {
				BeanInfo info = Utility.getBeanInfo(source.getClass());
				EventSetDescriptor[] events = info.getEventSetDescriptors();
				for (int i = 0; i < events.length; ++i) {
					EventSetDescriptor event = events[i];
					if (event.getListenerType() == PropertyChangeListener.class) {
						Method method = event.getAddListenerMethod();
						method.invoke(source, new Object[] { this });
					}
				}
			}
		} catch (Exception err) {
			theLogger.error("An error occurred", err);
		}
	}

	/**
	 * Stop listening to the source object
	 */
	private void stopListeningToSource() {
		try {
			if (source != null) {
				BeanInfo info = Utility.getBeanInfo(source.getClass());
				EventSetDescriptor[] events = info.getEventSetDescriptors();
				for (int i = 0; i < events.length; ++i) {
					EventSetDescriptor event = events[i];
					if (event.getListenerType() == PropertyChangeListener.class) {
						Method method = event.getRemoveListenerMethod();
						method.invoke(source, new Object[] { this });
					}
				}
			}
		} catch (Exception err) {
			theLogger.error("An error occurred", err);
		}
	}

	/**
	 * Locate a value editor for a given target type.
	 *
	 * @param type  The Class object for the type to be edited
	 * @return An editor object for the given target class. 
	 * The result is null if no suitable editor can be found.
	 */

	private PropertyEditor getEditor(Class type, boolean editable) {
		PropertyEditor editor = Utility.findEditor(type);

		if (editor == null) {
			return new BeanReferenceEditor(type, editable);
		} else {
			return editor;
		}
	}

	private Component getEditorComponent(PropertyEditor editor, boolean editable) {
		Component comp = null;
		if (editor.supportsCustomEditor()) {
			if (editable || isTypeMutable()) {
				comp = editor.getCustomEditor();
			} else {
				// if this is, for example, an uneditable Integer,
				// we only want to show a simple label.
				comp = new TextBasedViewComponent(editor);
			}
		} else {
			if (editable) {
				if (editor.getTags() != null) {
					comp = new ComboBoxInputComponent(editor);
				} else {
					comp = new TextBasedInputComponent(editor);
				}
			} else {
				comp = new TextBasedViewComponent(editor);
			}
		}
		return comp;
	}

	/**
	 * "Mutable" classes are basically anything except String and Number
	 * subclasses, i.e stuff you can modify after creation.
	 */
	private boolean isTypeMutable() {
		Class type = getCurrentType();
		return !(String.class.isAssignableFrom(type) || Number.class.isAssignableFrom(type) || Boolean.class.isAssignableFrom(type) || Color.class.isAssignableFrom(type) || type.isPrimitive());
	}

	private static Object getDefaultValue(Class type) {
		if (type == null || !type.isPrimitive()) {
			return null;
		} else {
			if (type == Boolean.TYPE) {
				return new Boolean(false);
			} else if (type == Integer.TYPE) {
				return new Integer(0);
			} else if (type == Short.TYPE) {
				return new Short((short) 0);
			} else if (type == Long.TYPE) {
				return new Long(0);
			} else if (type == Short.TYPE) {
				return new Short((short) 0);
			} else if (type == Double.TYPE) {
				return new Double(0);
			} else if (type == Character.TYPE) {
				return new Character((char) 0);
			} else if (type == Byte.TYPE) {
				return new Byte((byte) 0);
			} else {
				theLogger.error("Strange, I don't recognize the primitive type " + type + ", so I can't determine the default value. I will use null.");
				return null;
			}
		}
	}

	public class BeanReferenceEditor extends PropertyEditorSupport implements PropertyChangeListener {
		boolean editable;
		Class type = null;
		ObjectValuesChoicePanel choice = null;
		JPanel objectPanel = null;

		ScreenBoxedPOJORefPanel view = null;
		JLabel label = null;

		public BeanReferenceEditor(Class type, boolean editable) {
			this.type = type;
			this.editable = editable;
		}

		public Object getObject() {
			return this.getValue();
		}

		@Override public void propertyChange(PropertyChangeEvent evt) {
			Object object = evt.getNewValue();
			// BeanWrapper object = (BeanWrapper) val;
			try {
				if (object == null)
					PropertyValueControl.this.setValue(null);
				else
					PropertyValueControl.this.setValue(object);
			} catch (Exception err) {
				theLogger.error("An error occurred", err);
			}
		}

		@Override public void setValue(Object newValue) {
			Object oldValue = super.getValue();
			if (!Utility.isEqual(oldValue, newValue)) {
				super.setValue(newValue);
				if (editable) {
					if (choice != null)
						choice.setSelection(getObject());
				} else {
					if (objectPanel != null) {
						updateScreenBoxPanel();
					}
				}
			}
		}

		private void updateScreenBoxPanel() {
			if (objectPanel != null) {
				Object object = this.getValue();
				if (object == null) {
					if (label == null) {
						objectPanel.removeAll();
						label = new JLabel("<null>");
						view = null;
						objectPanel.add(label);
						validate();
					}
				} else {
					objectPanel.removeAll();
					label = null;
					view = new ScreenBoxedPOJORefPanel(object, true, true, true);
					objectPanel.add(view);
					validate();
				}
			}
		}

		@Override public Component getCustomEditor() {
			if (editable) {
				if (choice == null) {
					choice = new ObjectValuesChoicePanel(context, type, getObject());
					choice.addPropertyChangeListener(this);
				}
				return choice;
			} else {
				if (objectPanel == null) {
					objectPanel = new JPanel();
					objectPanel.setLayout(new BorderLayout());
				}
				updateScreenBoxPanel();
				return objectPanel;
			}
		}

		@Override public boolean supportsCustomEditor() {
			return true;
		}
	}

	class TextBasedViewComponent extends JLabel implements PropertyChangeListener {
		PropertyEditor editor;

		public TextBasedViewComponent(PropertyEditor editor) {
			this.editor = editor;
			editor.addPropertyChangeListener(this);
			readValue();
		}

		@Override public void propertyChange(PropertyChangeEvent evt) {
			readValue();
		}

		@Override public String getText() {
			try {
				return super.getText();
			} catch (Exception e) {
				return e.getMessage();
			}
		}

		String getEditorAsText() {
			try {
				Object isNull = editor.getValue();
				if (isNull == null)
					return null;
				return editor.getAsText();
			} catch (Exception e) {
				//e.printStackTrace();
				//return e.getMessage();
				return null;
			}
		}

		private void readValue() {
			String et = getEditorAsText();
			if (!Utility.isEqual(getText(), et)) {
				setText(et);
			}
		}
	}

	class TextBasedInputComponent extends JTextField implements PropertyChangeListener, ActionListener, FocusListener, InputComponent {
		PropertyEditor editor;

		public TextBasedInputComponent(PropertyEditor editor) {
			this.editor = editor;
			editor.addPropertyChangeListener(this);
			addActionListener(this);
			addFocusListener(this);
		}

		@Override public void propertyChange(PropertyChangeEvent evt) {
			readValue();
		}

		@Override public void actionPerformed(ActionEvent evt) {
			writeValue();
		}

		@Override public void focusGained(FocusEvent e) {
		}

		@Override public void focusLost(FocusEvent e) {
			writeValue();
		}

		private void writeValue() {
			if (!Utility.isEqual(getText(), editor.getAsText())) {
				try {
					editor.setAsText(getText());
				} catch (Exception err) {
					theLogger.error("An error occurred", err);
				}
				readValue();
			}
		}

		private void readValue() {
			if (!Utility.isEqual(getText(), editor.getAsText())) {
				setText(editor.getAsText());
			}
		}

	}

	/**
	 * Displays a list of fixed values to choose from.
	 */
	class ComboBoxInputComponent extends JComboBox implements PropertyChangeListener, ActionListener, InputComponent {
		PropertyEditor editor;

		public ComboBoxInputComponent(PropertyEditor editor) {
			this.editor = editor;
			populate();
			editor.addPropertyChangeListener(this);
			addActionListener(this);
			setMaximumRowCount(10);
		}

		@Override public void propertyChange(PropertyChangeEvent evt) {
			readValue();
		}

		@Override public void actionPerformed(ActionEvent evt) {
			writeValue();
		}

		private void writeValue() {
			Object selected = getSelectedItem();
			if (!Utility.isEqual(selected, editor.getAsText())) {
				try {
					editor.setAsText(selected.toString());
				} catch (Exception err) {
					theLogger.error("An error occurred while setting value of property '" + property + "'", err);
				}
				readValue();
			}
		}

		private void readValue() {
			Object selected = getSelectedItem();
			if (!Utility.isEqual(selected, editor.getAsText())) {
				setSelectedItem(editor.getAsText());
			}
		}

		private void populate() {
			String[] tags = editor.getTags();
			int len = tags.length;
			for (int i = 0; i < len; ++i) {
				addItem(tags[i]);
			}
		}
	}
}
