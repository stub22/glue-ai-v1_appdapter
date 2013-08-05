package org.appdapter.gui.swing;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
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
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.appdapter.core.convert.ReflectUtils;
import org.appdapter.core.log.Debuggable;
import org.appdapter.gui.api.BT;
import org.appdapter.gui.api.DisplayContext;
import org.appdapter.gui.api.GetSetObject;
import org.appdapter.gui.api.IValidate;
import org.appdapter.gui.api.NamedObjectCollection;
import org.appdapter.gui.browse.Utility;
import org.appdapter.gui.editors.ValueEditor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings({ "serial" })
public class PropertyValueControl extends JVPanel implements PropertyChangeListener, IValidate, GetSetObject, ValueEditor {

	/**
	 * Displays a list of fixed values to choose from.
	 */
	class ComboBoxInputComponent extends JComboBox implements PropertyChangeListener, ActionListener, UserInputComponent {
		PropertyEditor editor;
		final PropertyDescriptor property;

		public ComboBoxInputComponent(PropertyEditor editor, PropertyDescriptor pvc) {
			property = pvc;
			this.editor = editor;
			populate();
			editor.addPropertyChangeListener(this);
			addActionListener(this);
			setMaximumRowCount(10);
		}

		@Override public void actionPerformed(ActionEvent evt) {
			writeValue();
		}

		private void populate() {
			String[] tags = editor.getTags();
			if (tags == null)
				tags = getPVTags();
			int len = tags.length;
			for (int i = 0; i < len; ++i) {
				addItem(tags[i]);
			}
		}

		@Override public void propertyChange(PropertyChangeEvent evt) {
			readValue();
		}

		private void readValue() {
			Object selected = getSelectedItem();
			if (!Utility.isEqual(selected, editor.getAsText())) {
				setSelectedItem(editor.getAsText());
			}
		}

		private void writeValue() {
			Object selected = getSelectedItem();
			if (!Utility.isEqual(selected, editor.getAsText())) {
				try {
					editor.setAsText(selected.toString());
				} catch (Exception err) {
					PropertyValueControl.theLogger.error("An error occurred while setting value of property '" + property + "'", err);
				}
				readValue();
			}
		}
	}

	static public class ObjectReferenceEditor extends PropertyEditorSupport implements PropertyChangeListener {

		ObjectChoiceComboPanel choice = null;
		boolean editable;
		JLabel label = null;
		JPanel objectPanel = null;
		//NamedObjectCollection fromCollection = null;
		final PropertyValueControl provalctrl;
		Class type = null;
		final Container validator;
		SmallObjectView view = null;

		public ObjectReferenceEditor(Class type, boolean editable, PropertyValueControl pvc, Container validater) {
			if (editable) {
				if (type == String.class) {
					Debuggable.warn("This is not good for strings!");
				}
			}
			provalctrl = pvc;
			this.type = type;
			this.editable = editable;
			validator = validater;
			String[] tags = getTags();
			if (tags.length < 2) {
			//	Debuggable.warn("TAGS=", tags);
			}
		}

		public String[] getTags() {
			String[] tags = super.getTags();
			/*if (editor != null) {
				tags = editor.getTags();
			}*/
			if (tags == null) {
				return provalctrl.getPVTags();
			}
			return tags;
		}

		@Override public Component getCustomEditor() {
			if (editable) {
				if (choice == null) {
					NamedObjectCollection fromCollection = provalctrl.getNamedObjectCollection();
					Object initialValue = getBoundValue();
					choice = new ObjectChoiceComboPanel(fromCollection, type, initialValue);
					choice.addPropertyChangeListener(this);
				}
				return choice;
			} else {
				if (objectPanel == null) {
					objectPanel = new JPanel();
					objectPanel.setLayout(new BorderLayout());
				}
				Object initialValue = getBoundValue();
				updateReadonlyPanel(initialValue);
				return objectPanel;
			}
		}

		public Object getObject() {
			return getBoundValue();
		}

		@Override public void propertyChange(PropertyChangeEvent evt) {
			Object object = evt.getNewValue();
			// BeanWrapper object = (BeanWrapper) val;
			try {
				if (object == null) {
					provalctrl.setObject(null);
				} else {
					provalctrl.setObject(object);
				}
			} catch (Exception err) {
				PropertyValueControl.theLogger.error("An error occurred", err);
			}
		}

		@Override public void setValue(Object newValue) {
			Object oldValue = getShownValue();
			if (!Utility.isEqual(oldValue, newValue)) {
				if (editable) {
					if (choice != null) {
						choice.setSelection(newValue);
						updateShownValue(newValue);
					} else {
						Debuggable.warn(" choice == null ");
					}
				} else {
					if (objectPanel == null) {
						objectPanel = new JPanel();
						objectPanel.setLayout(new BorderLayout());
					}
					updateReadonlyPanel(newValue);
					updateShownValue(newValue);
				}
			}
		}

		@Override public boolean supportsCustomEditor() {
			return true;
		}

		@Override public Object getValue() {
			return getShownValue();
		}

		private void updateReadonlyPanel(Object newValue) {
			if (objectPanel != null) {
				Object object = newValue;
				if (editable) {
					Debuggable.warn(" aboutToLabelAnEditable(); ");
				}
				objectPanel.removeAll();
				if (object == null) {
					Class type2 = getPropertyValueControl().getReturnType();
					if (label == null)
						label = new JLabel("<null> " + type2);
					view = null;
					objectPanel.add(label);
				} else {
					// obj is null
					label = null;
					view = new SmallObjectView(null, provalctrl.getNamedObjectCollection(), object, null);
					objectPanel.add(view);
				}
				validator.validate();
			}
		}

		public Object getBoundValue() {
			Object val = getValueSomeType();
			if (val == null)
				return null;
			if (ReflectUtils.nonPrimitiveTypeFor(type).isInstance(val))
				return val;
			NamedObjectCollection fromCollection = provalctrl.getNamedObjectCollection();
			BT bval = fromCollection.findBoxByObject(val);
			val = bval.convertTo(type);
			return val;
		}

		public Object getValueSomeType() {
			Object val1 = null;
			try {
				val1 = this.getPropertyValueControl().getBoundValue();
			} catch (Throwable e) {
				Debuggable.printStackTrace(e);
			}
			if (val1 != null)
				return val1;
			Object val2 = getShownValue();
			if (val1 == null && val2 == null)
				return null;
			if (val2 == null) {
				return val1;
			}
			return val2;
		}

		public Object getShownValue() {
			return super.getValue();
		}

		public void updateShownValue(Object value) {
			super.setValue(value);
		}

		PropertyValueControl getPropertyValueControl() {
			return provalctrl;
		}

		@Override public String toString() {
			return "Editor: " + provalctrl.toString();
		}
	}

	static class TextBasedInputComponent extends JTextField implements PropertyChangeListener, ActionListener, FocusListener, UserInputComponent {
		PropertyEditor editor;

		public TextBasedInputComponent(PropertyEditor editor) {
			this.editor = editor;
			editor.addPropertyChangeListener(this);
			addActionListener(this);
			addFocusListener(this);
		}

		@Override public void actionPerformed(ActionEvent evt) {
			writeValue();
		}

		@Override public void focusGained(FocusEvent e) {
		}

		@Override public void focusLost(FocusEvent e) {
			writeValue();
		}

		@Override public void propertyChange(PropertyChangeEvent evt) {
			readValue();
		}

		private void readValue() {
			if (!Utility.isEqual(getText(), editor.getAsText())) {
				setText(editor.getAsText());
			}
		}

		private void writeValue() {
			if (!Utility.isEqual(getText(), editor.getAsText())) {
				try {
					editor.setAsText(getText());
				} catch (Exception err) {
					PropertyValueControl.theLogger.error("An error occurred", err);
				}
				readValue();
			}
		}

	}

	static class TextBasedViewComponent extends JLabel implements PropertyChangeListener {
		PropertyEditor editor;

		public TextBasedViewComponent(PropertyEditor editor) {
			this.editor = editor;
			editor.addPropertyChangeListener(this);
			readValue();
		}

		String getEditorAsText() {
			try {
				Object isNull = editor.getValue();
				if (isNull == null) {
					return null;
				}
				return editor.getAsText();
			} catch (Exception e) {
				//e.printStackTrace();
				//return e.getMessage();
				return null;
			}
		}

		@Override public String getText() {
			try {
				return super.getText();
			} catch (Exception e) {
				return e.getMessage();
			}
		}

		@Override public void propertyChange(PropertyChangeEvent evt) {
			readValue();
		}

		private void readValue() {
			String et = getEditorAsText();
			if (!Utility.isEqual(getText(), et)) {
				setText(et);
			}
		}
	}

	static Logger theLogger = LoggerFactory.getLogger(PropertyValueControl.class);

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
				PropertyValueControl.theLogger.error("Strange, I don't recognize the primitive type " + type + ", so I can't determine the default value. I will use null.");
				return null;
			}
		}
	}

	public Class getReturnType() {
		Class pt = type;
		if (property != null) {
			pt = property.getPropertyType();
			if (pt != null)
				return pt;
			Method rm = property.getReadMethod();
			if (rm != null) {
				pt = rm.getReturnType();
				if (pt != void.class)
					return pt;
			}
		} else {

		}
		return pt;
	}

	private static PropertyDescriptor getPropertyDescriptor(Object object, String propName) throws IntrospectionException {
		BeanInfo info = Utility.getBeanInfo(object.getClass(), object);
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

	DisplayContext context = Utility.getCurrentContext();
	public PropertyEditor currentEditor = null;

	boolean editable = true;

	PropertyDescriptor property = null;

	boolean showLabel = false;

	Object source = null;

	Class type = null;

	Object value = null;

	private Object title;

	public PropertyValueControl() {
		this((String) null, (Object) null, true);
	}

	public PropertyValueControl(String title, boolean editable) {
		this(title, (Object) null, editable);
	}

	public PropertyValueControl(String title, Class type, boolean editable) {
		this(Utility.getCurrentContext(), title, type, editable);
	}

	/**
	 * Creates an unbound PropertyValueControl of the given type. It will be
	 * initialized to a default value, for non-primitive this is null and for
	 * primitives it is 0, false, or whatever.
	 */
	public PropertyValueControl(DisplayContext context, String title, Class type, boolean editable) {
		this.title = title;
		if (context != null) {
			this.context = context;
		}
		this.type = type;
		this.editable = editable;
		value = getDefaultValue(type);
		recreateGUI();
	}

	public PropertyValueControl(DisplayContext context, String title, Object source, PropertyDescriptor property) {
		this.title = title;
		this.property = property;
		this.source = source;
		if (context != null) {
			this.context = context;
		}
		bind(source, property);
	}

	public PropertyValueControl(DisplayContext context, String title, Object source, String propertyName) throws IntrospectionException {
		this(context, title, source, getPropertyDescriptor(source, propertyName));
	}

	public PropertyValueControl(String title, Object value, boolean editable) {
		this.title = title;
		if (value != null) {
			type = value.getClass();
		}
		this.value = value;
		this.editable = editable;
		recreateGUI();
	}

	public PropertyValueControl(String title, Object source, PropertyDescriptor property) {
		this((DisplayContext) null, title, source, property);
	}

	public PropertyValueControl(String title, Object source, String propertyName) throws IntrospectionException {
		this(title, source, getPropertyDescriptor(source, propertyName));
	}

	public void bind(Object source, PropertyDescriptor property) {
		this.property = property;
		this.source = source;
		type = property.getPropertyType();
		editable = property.getWriteMethod() != null;
		// readBoundValue();
		recreateGUI();
	}

	/**
	 * Returns the current type of the value in this PropertyValueControl. If
	 * there is a fixed type that will be returned instead. If the there is no
	 * value set and no fixed type, null will be returned.
	 */
	public Class getCurrentType() {
		if (type == null) {
			if (value == null) {
				if (property == null) {
					return null;
				} else {
					return property.getPropertyType();
				}
			} else {
				return value.getClass();
			}
		} else {
			return type;
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
			return new ObjectReferenceEditor(type, editable, this, this);
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
					comp = new ComboBoxInputComponent(editor, property);
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
	 * Returns the type of this PropertyValueControl, if there is a fixed type.
	 * For example if this is String then this PropertyValueControl can only be
	 * used to create and view Strings.
	 */
	public Class getFixedType() {
		return type;
	}

	@Override public Object getValue() {
		return value;
	}

	public boolean isBound() {
		return source != null;
	}

	public boolean isEditable() {
		return editable;
	}

	/**
	 * "Mutable" classes are basically anything except String and Number
	 * subclasses, i.e stuff you can modify after creation.
	 */
	private boolean isTypeMutable() {
		Class type = getCurrentType();
		if (type == null)
			return true;
		return !(String.class.isAssignableFrom(type) || Number.class.isAssignableFrom(type) || Boolean.class.isAssignableFrom(type) || Color.class.isAssignableFrom(type) || type.isPrimitive());
	}

	/**
	 * Start listening to the source object
	 */
	private void listenToSource() {
		try {
			if (source != null) {
				BeanInfo info = Utility.getBeanInfo(source.getClass(), source);
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
			PropertyValueControl.theLogger.error("An error occurred", err);
		}
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

	@Override public String toString() {
		if (property != null)
			return getClass().getSimpleName() + " " + Debuggable.toInfoStringF(property);
		if (this.title != null)
			return "" + title;
		return super.toString();
	}

	@Override public String getName() {
		if (title != null)
			return title.toString();
		return super.getName();
	}

	public Object getBoundValue() throws Throwable {
		if (property == null)
			return value;
		boolean was = Debuggable.QuitelyDoNotShowExceptions;
		Debuggable.QuitelyDoNotShowExceptions = true;
		try {
			Method readMethod = property.getReadMethod();
			return getBoundValue(source, readMethod);
		} finally {
			Debuggable.QuitelyDoNotShowExceptions = was;
		}
	}

	public Object getBoundValue(Object obj, Method readMethod) throws Throwable {
		Object obj0 = obj;
		Throwable realCause;
		try {
			if (readMethod == null) {
				if (property != null) {
					String pn = property.getName();
					if (pn == null)
						pn = property.getDisplayName();
					if (pn != null) {
						try {
							Collection<Field> fields = ReflectUtils.findFields(obj, null, obj.getClass(), false, null, false, pn);
							if (fields != null && fields.size() > 0)
								return ReflectUtils.getFieldValue(obj, fields.iterator().next());
							fields = ReflectUtils.findFields(obj, null, obj.getClass(), true, null, true, pn);
							if (fields != null && fields.size() > 0)
								return ReflectUtils.getFieldValue(obj, fields.iterator().next());
						} catch (NoSuchFieldException nsfe) {
							//							
						}
					}
				}
				return value;
			}
			if (readMethod == null) {
				throw new Exception("readMethod = null for object " + obj + " and property '" + property.getName() + "'!!!");
			}
			return ReflectUtils.invoke(source, readMethod);
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
		throw realCause;
	}

	private void readBoundValue() {
		if (isBound()) {
			Throwable realCause;
			Object obj = source;
			try {
				Object boundValue = getBoundValue();
				setObject(boundValue);
				return;
			} catch (Throwable err) {
				realCause = err;
			}
			try {
				setObject(null);
			} catch (Exception err2) {
				PropertyValueControl.theLogger.error("An error occurred", err2);
			}
			if (realCause instanceof java.awt.IllegalComponentStateException) {
				return;
			}
			PropertyValueControl.theLogger.error("An error occurred", realCause);

		} else {
			PropertyValueControl.theLogger.warn("PropertyValueControl warning: ValueView.readBoundValue should only be called if value is bound!");
		}

	}

	private void readEditorValue() {
		if (currentEditor != null) {
			if (!Utility.isEqual(value, currentEditor.getValue())) {
				try {
					setObject(currentEditor.getValue());
				} catch (Exception err) {
					PropertyValueControl.theLogger.error("An error occurred", err);
				}
			}
		}
	}

	private synchronized void recreateGUI() {
		removeAll();
		setLayout(new BorderLayout());

		if (currentEditor != null) {
			currentEditor.removePropertyChangeListener(this);
		}
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
		if (comp instanceof ObjectChoiceComboPanel) {

		} else {

		}
		if (comp != null) {
			if (showLabel && property != null) {
				add("West", new JLabel(property.getDisplayName() + ": "));
			}
			add("Center", comp);
		}
		if (currentEditor != null) {
			currentEditor.addPropertyChangeListener(this);
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
		setObject(defaultValue);
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

	public void setFixedType(Class newType) {
		Class oldType = type;
		if (oldType != newType) {
			type = newType;
			if (newType != null) {
				if (value != null && value.getClass() != newType) {
					try {
						setObject(null);
					} catch (Exception err) {
						PropertyValueControl.theLogger.error("An error occurred", err);
					}
				}
			}
		}
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

			value = newValue;
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

	public void showLabel(boolean b) {
		showLabel = b;
		recreateGUI();
	}

	/**
	 * Stop listening to the source object
	 */
	private void stopListeningToSource() {
		try {
			if (source != null) {
				BeanInfo info = Utility.getBeanInfo(source.getClass(), source);
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
			PropertyValueControl.theLogger.error("An error occurred", err);
		}
	}

	public void unbind() {
		property = null;
		source = null;
	}

	private void writeBoundValue() throws Exception {
		if (editable) {
			if (isBound()) {
				Object obj = source;
				property.getWriteMethod().invoke(obj, new Object[] { value });
			} else {
				PropertyValueControl.theLogger.warn("PropertyValueControl warning: ValueView.writeBoundValue should only be called if value is bound!");
			}
		}
	}

	private void writeEditorValue() {
		if (currentEditor != null) {
			if (!Utility.isEqual(value, currentEditor.getValue())) {
				currentEditor.setValue(value);
			}
		}
	}

	NamedObjectCollection fromCollection;

	public PropertyValueControl getPropertyValueControl() {
		return this;
	}

	public String[] getPVTags() {
		java.util.HashSet<String> choices = new java.util.HashSet<String>();
		fromCollection = getNamedObjectCollection();
		if (currentEditor != null && !(currentEditor instanceof ObjectReferenceEditor)) {
			String[] tags = currentEditor.getTags();
			if (tags != null) {
				for (String t : tags) {
					choices.add(t);
				}
			}
		}
		Iterable objectList = fromCollection.findObjectsByType(type);
		for (Object o : objectList) {
			choices.add(fromCollection.getTitleOf(o));
		}
		return choices.toArray(new String[choices.size()]);
	}

	public NamedObjectCollection getNamedObjectCollection() {
		if (fromCollection != null)
			return fromCollection;
		return Utility.getTreeBoxCollection();
	}
}
