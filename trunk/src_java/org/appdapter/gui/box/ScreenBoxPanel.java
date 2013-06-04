/*
 *  Copyright 2011 by The Appdapter Project (www.appdapter.org).
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.appdapter.gui.box;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.plaf.basic.BasicButtonUI;

import org.appdapter.api.trigger.Box;
import org.appdapter.gui.browse.DisplayContext;
import org.appdapter.gui.pojo.Utility;

import com.jidesoft.swing.JideButton;
import com.jidesoft.swing.JideLabel;

/**
 * Component to be used as tabComponent;
 * Contains a JLabel to show the text and 
 * a JButton to close the tab it belongs to 
 */
public abstract class ScreenBoxPanel<BoxType extends Box> extends JPanel implements UIProvider {
	//private final JTabbedPane pane;
	public enum Kind {
		MATRIX, DB_MANAGER, REPO_MANAGER, OBJECT_PROPERTIES, OTHER,
	}

	private JideLabel label;
	private TabCloseOrRenameButton button;

	/** Make the display of this panel foocus on a particular box.
	 * 
	 * @param b - a box to focus on
	 */
	public Object lastFocusOnBox;

	public void focusOnBox(BoxType b) {
		lastFocusOnBox = b;
	}

	/** Return the live object in which we think we are updating 
	 * 
	 *  This can be 'this' object
	 * 
	 */
	public Object getValue() {
		return lastFocusOnBox;
	}

	public ScreenBoxPanel() {
		//unset default FlowLayout' gaps
		super(new FlowLayout(FlowLayout.LEFT, 0, 0));

		//this.pane = pane;
		setOpaque(false);
		//tab button
		this.button = new TabCloseOrRenameButton();
		add(button);

		//make JLabel read titles from JTabbedPane
		this.label = new JideLabel() {
			public String getText() {
				String lt = getLabelText();
				return lt;
			}

			public String getLabelText() {
				//BoxPanelSwitchableView pane = getBoxPanelTabPane();
				return getBoxPanelTabPane().getTitleOf(ScreenBoxPanel.this);
			}
		};

		add(label);
		//add more space between the label and the button
		label.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 5));
		//tab button
		add(button);
		//add more space to the top of the component

		setBorder(BorderFactory.createEmptyBorder(2, 0, 0, 0));
	}

	private class TabCloseOrRenameButton extends JideButton implements ActionListener {
		public TabCloseOrRenameButton() {
			int size = 17;
			setPreferredSize(new Dimension(size, size));
			setToolTipText("close this tab");
			//Make the button looks the same for all Laf's
			setUI(new BasicButtonUI());
			//Make it transparent
			setContentAreaFilled(false);
			//No need to be focusable
			setFocusable(false);
			setBorder(BorderFactory.createEtchedBorder());
			setBorderPainted(false);
			//Making nice rollover effect
			//we use the same listener for all buttons
			addMouseListener(buttonMouseListener);
			setRolloverEnabled(true);
			//Close the proper tab by clicking the button
			addActionListener(this);
		}

		public void actionPerformed(ActionEvent e) {
			BoxPanelSwitchableView pane = ScreenBoxPanel.this.getBoxPanelTabPane();
			if (pane.containsComponent(ScreenBoxPanel.this)) {
				// TODO notify someone
				pane.removeComponent(ScreenBoxPanel.this);
			}
		}

		//we don't want to update UI for this button
		public void updateUI() {
		}

		//paint the cross
		protected void paintComponent(Graphics g) {
			super.paintComponent(g);
			Graphics2D g2 = (Graphics2D) g.create();
			//shift the image for pressed buttons
			if (getModel().isPressed()) {
				g2.translate(1, 1);
			}
			g2.setStroke(new BasicStroke(2));
			g2.setColor(Color.BLACK);
			if (getModel().isRollover()) {
				g2.setColor(Color.MAGENTA);
			}
			int delta = 6;
			g2.drawLine(delta, delta, getWidth() - delta - 1, getHeight() - delta - 1);
			g2.drawLine(getWidth() - delta - 1, delta, delta, getHeight() - delta - 1);
			g2.dispose();
		}
	}

	private final static MouseListener buttonMouseListener = new MouseAdapter() {
		public void mouseEntered(MouseEvent e) {
			Component component = e.getComponent();
			if (component instanceof AbstractButton) {
				AbstractButton button = (AbstractButton) component;
				button.setBorderPainted(true);
			}
		}

		public void mouseExited(MouseEvent e) {
			Component component = e.getComponent();
			if (component instanceof AbstractButton) {
				AbstractButton button = (AbstractButton) component;
				button.setBorderPainted(false);
			}
		}
	};

	public BoxPanelSwitchableView getBoxPanelTabPane() {
		return getDisplayContext().getBoxPanelTabPane();
	}

	public DisplayContext getDisplayContext() {
		return Utility.getCurrentContext();
	}
}