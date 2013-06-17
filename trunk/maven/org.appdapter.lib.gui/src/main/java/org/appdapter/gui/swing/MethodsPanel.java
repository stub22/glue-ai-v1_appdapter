package org.appdapter.gui.swing;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.appdapter.api.trigger.DisplayContext;
import org.appdapter.gui.api.Utility;
import org.appdapter.gui.impl.JJPanel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A GUI component showing all the methods of a given object and controls
 * for providing parameters, executing the methods, and manipulating the result
 *
 * 
 */
public class MethodsPanel extends JJPanel implements ActionListener, ListSelectionListener {
	private static Logger theLogger = LoggerFactory.getLogger(MethodsPanel.class);

	DisplayContext context;
	Object object;
	Class objectClass;

	MethodList methodList;
	MethodParametersPanel paramPanel;
	JButton executeButton;
	JSplitPane splitter;
	MethodResultPanel resultPanel;

	public MethodsPanel(Object object) throws Exception {
		this(Utility.getCurrentContext(), object, object.getClass());
	}

	public MethodsPanel(DisplayContext context, Object object, Class objClass) throws Exception {
		this.context = context;
		this.object = object;
		this.objectClass = objClass;
		if (object instanceof Class) {
			objectClass = (Class) object;
		}
		initGUI();
	}

	@Override public void valueChanged(ListSelectionEvent e) {
		Method current = methodList.getSelectedMethod();

		paramPanel.setMethod(current);
		resultPanel.setVisible(current != null);
		if (current != null)
			resultPanel.setResultType(current.getReturnType());
	}

	/**
	 * Executes the given method now
	 */
	private void executeMethod(Method method) throws Exception {
		if (method != null) {
			Object[] params = paramPanel.getValues();
			theLogger.debug("Invoking " + method + " on " + object + " with args " + params);

			Object returnValue = Utility.invoke(object, method, params);
			resultPanel.setResultValue(returnValue);
		}
	}

	private void initGUI() throws Exception {
		paramPanel = new MethodParametersPanel();
		methodList = new MethodList(object, object instanceof Class);
		resultPanel = new MethodResultPanel(context);

		executeButton = new JButton("Execute method");
		executeButton.addActionListener(this);

		JPanel bottomPanel = new JPanel();
		bottomPanel.setLayout(new BorderLayout(10, 10));
		bottomPanel.add("West", executeButton);
		bottomPanel.add("Center", resultPanel);

		JScrollPane scroller = new JScrollPane(methodList);
		scroller.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

		splitter = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, true, scroller, paramPanel);

		setLayout(new BorderLayout());
		add("Center", splitter);
		add("South", bottomPanel);
		methodList.addListSelectionListener(this);
	}

	@Override public void actionPerformed(ActionEvent evt) {
		if (evt.getSource() == executeButton) {
			Method method = methodList.getSelectedMethod();
			if (method != null) {
				try {
					executeMethod(method);
				} catch (InvocationTargetException err) {
					Utility.showError(context, null, err.getTargetException());
				} catch (Throwable err) {
					Utility.showError(context, null, err);
				}
			}
		}
	}
}
