/*
GNU Lesser General Public License

EkitApplet - Java Swing HTML Editor & Viewer Applet
Copyright (C) 2000 Howard Kistler

This library is free software; you can redistribute it and/or
modify it under the terms of the GNU Lesser General Public
License as published by the Free Software Foundation; either
version 2.1 of the License, or (at your option) any later version.

This library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public
License along with this library; if not, write to the Free Software
Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
*/

package com.hexidec.ekit;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Vector;

import javax.swing.JApplet;

/** EkitApplet
  * Applet for editing and saving HTML in a Java browser component
  *
  * @author Howard Kistler
  * @version 0.9e
  *
  * REQUIREMENTS
  * Java 2 (JDK 1.3 or 1.4)
  * Swing Library
  */

public class EkitApplet extends JApplet {
	/* Components */
	EkitCore ekitCore;

	/** Constructor
	  */
	public EkitApplet() {
		getRootPane().putClientProperty("defeatSystemEventQueueCheck", Boolean.TRUE);
	}

	/** Applet Init
	  */
	public void init() {
		String sRawDocument = this.getParameter("DOCUMENT");
		String sStyleSheetRef = this.getParameter("STYLESHEET");
		boolean base64 = ((this.getParameter("BASE64") != null) && this.getParameter("BASE64").equalsIgnoreCase("true"));
		URL urlCSS = (URL) null;
		try {
			if (sStyleSheetRef != null && sStyleSheetRef.length() > 0) {
				urlCSS = new URL(this.getCodeBase(), sStyleSheetRef);
			}
		} catch (MalformedURLException murle) {
			murle.printStackTrace(System.err);
		}
		boolean showToolBar = true;
		boolean showToolBarMulti = false;
		if (this.getParameter("TOOLBAR") != null) {
			showToolBar = this.getParameter("TOOLBAR").equalsIgnoreCase("true");
		}
		if (this.getParameter("TOOLBARMULTI") != null) {
			showToolBar = this.getParameter("TOOLBARMULTI").equalsIgnoreCase("true");
			showToolBarMulti = this.getParameter("TOOLBARMULTI").equalsIgnoreCase("true");
		}
		boolean showViewSource = ((this.getParameter("SOURCEVIEW") != null && this.getParameter("SOURCEVIEW").equalsIgnoreCase("true")));
		String sLanguage = this.getParameter("LANGCODE");
		String sCountry = this.getParameter("LANGCOUNTRY");
		boolean editModeExclusive = true;
		if (this.getParameter("EXCLUSIVE") != null) {
			editModeExclusive = this.getParameter("EXCLUSIVE").equalsIgnoreCase("true");
		}
		boolean showMenuIcons = true;
		if (this.getParameter("MENUICONS") != null) {
			showMenuIcons = this.getParameter("MENUICONS").equalsIgnoreCase("true");
		}
		boolean spellChecker = false;
		if (this.getParameter("SPELLCHECK") != null) {
			spellChecker = this.getParameter("SPELLCHECK").equalsIgnoreCase("true");
		}
		//
		//		if(spellChecker)
		//		{
		//			ekitCore = new EkitCoreSpell(sRawDocument, urlCSS, showToolBar, showViewSource, showMenuIcons, editModeExclusive, sLanguage, sCountry, base64, showToolBarMulti);
		//		}
		//		else
		//		{
		ekitCore = new EkitCore(sRawDocument, urlCSS, showToolBar, showViewSource, showMenuIcons, editModeExclusive, sLanguage, sCountry, base64, false, showToolBarMulti);
		//}

		/* Obtain image servlet information */
		ekitCore.setServletURL(this.getParameter("SERVLETURL"));
		ekitCore.setImageDir(this.getParameter("IMAGEDIR"));
		ekitCore.setTreePilotSystemID(this.getParameter("SYSTEMID"));

		/* Add menus, based on whether or not they are requested (all are shown by default) */
		Vector vcMenus = new Vector();
		if (this.getParameter("MENU_EDIT") != null) {
			if (this.getParameter("MENU_EDIT").equalsIgnoreCase("true")) {
				vcMenus.add(EkitCore.KEY_MENU_EDIT);
			}
		} else {
			vcMenus.add(EkitCore.KEY_MENU_EDIT);
		}
		if (this.getParameter("MENU_VIEW") != null) {
			if (this.getParameter("MENU_VIEW").equalsIgnoreCase("true")) {
				vcMenus.add(EkitCore.KEY_MENU_VIEW);
			}
		} else {
			vcMenus.add(EkitCore.KEY_MENU_VIEW);
		}
		if (this.getParameter("MENU_FONT") != null) {
			if (this.getParameter("MENU_FONT").equalsIgnoreCase("true")) {
				vcMenus.add(EkitCore.KEY_MENU_FONT);
			}
		} else {
			vcMenus.add(EkitCore.KEY_MENU_FONT);
		}
		if (this.getParameter("MENU_FORMAT") != null) {
			if (this.getParameter("MENU_FORMAT").equalsIgnoreCase("true")) {
				vcMenus.add(EkitCore.KEY_MENU_FORMAT);
			}
		} else {
			vcMenus.add(EkitCore.KEY_MENU_FORMAT);
		}
		if (this.getParameter("MENU_INSERT") != null) {
			if (this.getParameter("MENU_INSERT").equalsIgnoreCase("true")) {
				vcMenus.add(EkitCore.KEY_MENU_INSERT);
			}
		} else {
			vcMenus.add(EkitCore.KEY_MENU_INSERT);
		}
		if (this.getParameter("MENU_TABLE") != null) {
			if (this.getParameter("MENU_TABLE").equalsIgnoreCase("true")) {
				vcMenus.add(EkitCore.KEY_MENU_TABLE);
			}
		} else {
			vcMenus.add(EkitCore.KEY_MENU_TABLE);
		}
		if (this.getParameter("MENU_FORMS") != null) {
			if (this.getParameter("MENU_FORMS").equalsIgnoreCase("true")) {
				vcMenus.add(EkitCore.KEY_MENU_FORMS);
			}
		} else {
			vcMenus.add(EkitCore.KEY_MENU_FORMS);
		}
		if (this.getParameter("MENU_SEARCH") != null) {
			if (this.getParameter("MENU_SEARCH").equalsIgnoreCase("true")) {
				vcMenus.add(EkitCore.KEY_MENU_SEARCH);
			}
		} else {
			vcMenus.add(EkitCore.KEY_MENU_SEARCH);
		}
		if (this.getParameter("MENU_TOOLS") != null) {
			if (this.getParameter("MENU_TOOLS").equalsIgnoreCase("true")) {
				vcMenus.add(EkitCore.KEY_MENU_TOOLS);
			}
		} else {
			vcMenus.add(EkitCore.KEY_MENU_TOOLS);
		}
		if (this.getParameter("MENU_HELP") != null) {
			if (this.getParameter("MENU_HELP").equalsIgnoreCase("true")) {
				vcMenus.add(EkitCore.KEY_MENU_HELP);
			}
		} else {
			vcMenus.add(EkitCore.KEY_MENU_HELP);
		}
		this.setJMenuBar(ekitCore.getCustomMenuBar(vcMenus));

		/* Add the components to the app */
		if (showToolBar) {
			if (showToolBarMulti) {
				this.getContentPane().setLayout(new GridBagLayout());
				GridBagConstraints gbc = new GridBagConstraints();
				gbc.fill = GridBagConstraints.HORIZONTAL;
				gbc.anchor = GridBagConstraints.NORTH;
				gbc.gridheight = 1;
				gbc.gridwidth = 1;
				gbc.weightx = 1.0;
				gbc.weighty = 0.0;
				gbc.gridx = 1;

				gbc.gridy = 1;
				Vector vcTools = new Vector();
				vcTools.add(EkitCore.KEY_TOOL_NEW);
				vcTools.add(EkitCore.KEY_TOOL_SEP);
				vcTools.add(EkitCore.KEY_TOOL_CUT);
				vcTools.add(EkitCore.KEY_TOOL_COPY);
				vcTools.add(EkitCore.KEY_TOOL_PASTE);
				vcTools.add(EkitCore.KEY_TOOL_SEP);
				vcTools.add(EkitCore.KEY_TOOL_CLEAR);
				vcTools.add(EkitCore.KEY_TOOL_SEP);
				vcTools.add(EkitCore.KEY_TOOL_ANCHOR);
				vcTools.add(EkitCore.KEY_TOOL_SEP);
				vcTools.add(EkitCore.KEY_TOOL_SOURCE);
				this.getContentPane().add(ekitCore.customizeToolBar(EkitCore.TOOLBAR_MAIN, vcTools, showToolBar), gbc);

				gbc.gridy = 2;
				this.getContentPane().add(ekitCore.getToolBarFormat(showToolBar), gbc);

				gbc.gridy = 3;
				this.getContentPane().add(ekitCore.getToolBarStyles(showToolBar), gbc);

				gbc.anchor = GridBagConstraints.CENTER;
				gbc.fill = GridBagConstraints.BOTH;
				gbc.weighty = 1.0;
				gbc.gridy = 4;
				this.getContentPane().add(ekitCore, gbc);
			} else {
				Vector vcTools = new Vector();
				vcTools.add(EkitCore.KEY_TOOL_NEW);
				vcTools.add(EkitCore.KEY_TOOL_SEP);
				vcTools.add(EkitCore.KEY_TOOL_CUT);
				vcTools.add(EkitCore.KEY_TOOL_COPY);
				vcTools.add(EkitCore.KEY_TOOL_PASTE);
				vcTools.add(EkitCore.KEY_TOOL_SEP);
				vcTools.add(EkitCore.KEY_TOOL_BOLD);
				vcTools.add(EkitCore.KEY_TOOL_ITALIC);
				vcTools.add(EkitCore.KEY_TOOL_UNDERLINE);
				vcTools.add(EkitCore.KEY_TOOL_STRIKE);
				vcTools.add(EkitCore.KEY_TOOL_SUPER);
				vcTools.add(EkitCore.KEY_TOOL_SUB);
				vcTools.add(EkitCore.KEY_TOOL_ULIST);
				vcTools.add(EkitCore.KEY_TOOL_OLIST);
				vcTools.add(EkitCore.KEY_TOOL_SEP);
				vcTools.add(EkitCore.KEY_TOOL_CLEAR);
				vcTools.add(EkitCore.KEY_TOOL_SEP);
				vcTools.add(EkitCore.KEY_TOOL_ANCHOR);
				vcTools.add(EkitCore.KEY_TOOL_SEP);
				vcTools.add(EkitCore.KEY_TOOL_SOURCE);
				vcTools.add(EkitCore.KEY_TOOL_SEP);
				vcTools.add(EkitCore.KEY_TOOL_STYLES);

				this.getContentPane().setLayout(new BorderLayout());
				this.getContentPane().add(ekitCore, BorderLayout.CENTER);
				this.getContentPane().add(ekitCore.customizeToolBar(EkitCore.TOOLBAR_MAIN, vcTools, showToolBar), BorderLayout.NORTH);
			}
		}
	}

	/* Applet methods */
	public void start() {
		;
	}

	public void stop() {
		;
	}

	public void destroy() {
		;
	}

	/** Method for passing back the document text to the applet's container.
	  * This is the entire document, including the top-level HTML tags.
	  */
	public String getDocumentText() {
		return ekitCore.getDocumentText();
	}

	/** Method for passing back the document body to the applet's container.
	  * This is only the text contained within the BODY tags.
	  */
	public String getDocumentBody() {
		return ekitCore.getDocumentSubText("body");
	}

	/** Method for setting the document manually.
	  * Will need code in the web page to call this.
	  */
	public void setDocumentText(String text) {
		ekitCore.setDocumentText(text);
	}

}
