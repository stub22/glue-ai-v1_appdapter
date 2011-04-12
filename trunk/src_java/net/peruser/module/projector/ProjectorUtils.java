package net.peruser.module.projector;

import java.io.OutputStream;
import java.io.PrintStream;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;

// We use the Jena URI parser for formatting
// But...ooooo, they are now "rewriting" this feature, and have marked this class deprecated.
// import com.hp.hpl.jena.rdf.arp.URI;




/**
 * @author      Stu Baurmann
 * @version     @PERUSER_VERSION@
 */
public class ProjectorUtils {

	public static String getURI_Fragment(String fullURI) {
	/*
			Here is the deprecated jena functionality we are replacing:
			URI		jenaURI = new URI(fpuri);
			result = jenaURI.getFragment();
	*/
		String result = null;
		if (fullURI != null) {
			int uriLen = fullURI.length();
			int markPos = fullURI.indexOf('#');
			
			if (markPos >= 0) {
				// Will return empty string if URI ends with "#".  Is that what we want?
				result = fullURI.substring(markPos + 1); 
			}
		}
		return result;
	}
	
	public static void printFieldValues(PrintStream ps, ProjectedNode pn, List fieldPropertyUriList, String header) throws Throwable {
		Iterator fpui = fieldPropertyUriList.iterator();
		while (fpui.hasNext()) {
			String fpuri = (String) fpui.next();
			String fragment = getURI_Fragment (fpuri);
			String fieldHeader = header + "\t[" + fragment + "] = ";
			printFieldValues(ps, pn, fpuri, fieldHeader);
		}
	}
	public static void printFieldValues(PrintStream ps, ProjectedNode pn, String fieldPropertyURI, String header) throws Throwable {
		Iterator fvsi = pn.getFieldValueStringIterator(fieldPropertyURI);
		while (fvsi.hasNext()) {
			String fvs = (String) fvsi.next();
			ps.println(header + fvs);
		}
	}
	public static void printDescendantsWithFields(PrintStream ps, ProjectedNode pn, List fieldPropertyUriList, String header) throws Throwable {
		Iterator kidi = pn.getChildNodeIterator();
		while (kidi.hasNext()) {
			ProjectedNode kpn = (ProjectedNode) kidi.next();
			String uri = kpn.getUriString();
			String fragment = getURI_Fragment (uri);
			
			String stemPropURI = kpn.getStemPropertyURI();
			String spf = getURI_Fragment(stemPropURI);
			String formattedLine = header + "{" + spf + "}=[" +  fragment + "]";
			ps.println(formattedLine);
			printFieldValues(ps, kpn, fieldPropertyUriList, header);
			printDescendantsWithFields(ps, kpn, fieldPropertyUriList, header + "\t");
		}
	}
	
	public static void printProjectedNodeTree (PrintStream ps, ProjectedNode pn, List fieldPropertyUriList) throws Throwable {
		ps.println("Dumping contents of projected node: " + pn.getUriString());
		printFieldValues(ps, pn, fieldPropertyUriList, "\t");
		printDescendantsWithFields(ps, pn, fieldPropertyUriList, "\t");
	}
	public static Element createDom4jElement (ProjectedNode pn, List fieldPropertyUriList, int childLevelMax) throws Throwable {
		//   file:///d:/_japp/dom4j_152/docs/apidocs/index.html
		String uriString = pn.getUriString();
		String stemPropURI = pn.getStemPropertyURI();
		String uriFragment = getURI_Fragment (uriString);
		String spf = getURI_Fragment(stemPropURI);
		Element itemElement = DocumentHelper.createElement("item");
		if (spf != null) {
			itemElement.addAttribute("stemPropertyFragment", spf);
		}
		itemElement.addAttribute("uriFragment", uriFragment);

		Iterator fpui = fieldPropertyUriList.iterator();
		while (fpui.hasNext()) {
			String fpuri = (String) fpui.next();
			String fpUriFragment = getURI_Fragment(fpuri);
			String fieldName = fpUriFragment;
			Iterator fvsi = pn.getFieldValueStringIterator(fpuri);
			while (fvsi.hasNext()) {
				String fieldValue = (String) fvsi.next();
				Element fieldElement = DocumentHelper.createElement("field");
				fieldElement.addAttribute("name", fieldName);
				fieldElement.setText(fieldValue);
				itemElement.add(fieldElement);
			}
		}
		if (childLevelMax > 1) {
			Iterator kidi = pn.getChildNodeIterator();
			while (kidi.hasNext()) {
				ProjectedNode kpn = (ProjectedNode) kidi.next();
				Element kide = createDom4jElement(kpn, fieldPropertyUriList, childLevelMax - 1);
				itemElement.add(kide);
			}
		}
		return itemElement;
	}
	public static Document createDom4jDocument (ProjectedNode pn, List fieldPropertyUriList, 
				int childLevelMax) throws Throwable {
		Element pnElement = createDom4jElement(pn, fieldPropertyUriList, childLevelMax);
		Document doc = DocumentHelper.createDocument(pnElement);
		return doc;
	}
	public static void writeProjectedNodeXmlDump(OutputStream outStream, ProjectedNode pn, List fieldPropertyUriList,
				int childLevelMax) throws Throwable {
		Document pnDocument = createDom4jDocument(pn, fieldPropertyUriList, childLevelMax);
		OutputFormat format = OutputFormat.createPrettyPrint();
		XMLWriter writer = new XMLWriter(outStream, format);
		writer.write(pnDocument);
	}
	public static void printProjectedTreeAsBothTextAndXML (PrintStream outPS, ProjectedNode pn, 
				List fieldPropertyUriList, int childLevelMax) throws Throwable {
		outPS.println("=====================================================================================");
		// Dump results using internal links
		printProjectedNodeTree(outPS, pn, fieldPropertyUriList);
		outPS.println("=====================================================================================");
		// Dump results as XML
		ProjectorUtils.writeProjectedNodeXmlDump(outPS, pn, fieldPropertyUriList, childLevelMax);
		outPS.println("\n=====================================================================================");	
	}
	public static ProjectedNode doSimpleAxisQuery (Projector p, String startURI, 
				String propertyURI, int direction) throws Throwable {
		ProjectedNode result = null;
		HashSet	queryProps = new HashSet();
		SimpleAxisQuery saq = new SimpleAxisQuery(propertyURI, direction);
		queryProps.add(saq);
		result = p.projectNode(startURI, queryProps);
		return result;
	}
}
