package net.peruser.binding.dom4j;

import java.io.OutputStream;

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;

import javax.xml.transform.stream.StreamSource;

import com.hp.hpl.jena.rdf.model.Literal;

import net.peruser.binding.jena.JenaModelAwareConfig;

import net.peruser.core.name.Address;

import net.peruser.core.name.Abbreviator;
import net.peruser.core.name.CoreAbbreviator;

import net.peruser.core.config.Config;
import net.peruser.core.config.MutableConfig;

import net.peruser.core.document.Doc;

import net.peruser.core.name.ParsedPair;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.DOMWriter;
import org.dom4j.io.DOMReader;
import org.dom4j.io.SAXReader;

import org.dom4j.io.DocumentResult;
import org.dom4j.io.DocumentSource;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * Doc is currently hardwired to wrap around a dom4j doc.
 * This presents a certain handicap in representing small literals as Docs.
 * So, we plan to make the representation more flexible, later.
 *
 * @author      Stu Baurmann
 * @version     @PERUSER_VERSION@
 */
public class Dom4jDoc extends Doc {
	/**
	  * Our implementation delegate, using <a href="http://www.dom4j.org">DOM4J</a>.
	  */
	protected		Document	myDom4jDoc; 
	
	public void writePretty (OutputStream outStream) throws Throwable {
		OutputFormat format = OutputFormat.createPrettyPrint();
		XMLWriter writer = new XMLWriter(outStream, format);
		writer.write(myDom4jDoc);	
	}
	
	public String getTextDump() throws Throwable {
		return myDom4jDoc.asXML();
	}
	
	public Dom4jDoc(Document d) {
		myDom4jDoc = d;
	}
	public Document getDom4jDoc() {
		return myDom4jDoc;
	}
	
	public void setDom4jDoc(Document d) {
		myDom4jDoc = d;
	}
	
	/*
	 *  Currently, this always translates our dom4J document into an RDF-Literal, 
	 *  which is also an RDFNode.  Any other config will cause exceptions.
	 */
	
	public Object getCompatibleValue (Config conf) throws Throwable {
		net.peruser.binding.jena.JenaModelAwareConfig jmac = 
				(net.peruser.binding.jena.JenaModelAwareConfig) conf;
		return jmac.produceLiteral(this);
	}
	
	public Doc transform(String xformPath, String systemID) throws Throwable {
		TransformerFactory tFactory = TransformerFactory.newInstance();

		DocumentSource xmlSource = new DocumentSource(getDom4jDoc());
		Source xslSource = new StreamSource(xformPath);
		xslSource.setSystemId(systemID);

		Transformer transformer = tFactory.newTransformer(xslSource);
		// Not yet using transfomer properties, but they are set with:		transformer.setParameter(paramName, paramValue);

		DocumentResult docResult = new DocumentResult();
		// Perform the transformation, sending the output to the response.
		transformer.transform(xmlSource, docResult);	
		Document transformedDoc = docResult.getDocument();
		
		Dom4jDoc output  = new Dom4jDoc(transformedDoc); // (Doc) this.getClass().newInstance();
		return output;
	}
	
	public org.w3c.dom.Document getW3CDOM() throws Throwable {		
		DOMWriter domWriter = new DOMWriter();  
		org.w3c.dom.Document outDocW3C = domWriter.write(getDom4jDoc());
		return outDocW3C;
	}
	public static String	CO_overrides = "config_overrides";
	public static String	CO_param = "param";
	public static String	CO_thing = "thing";
	public static String	CO_field = "field";
	public static String	CO_strict = "strictFlag";
	public static String	CO_value = "value";	
	
	public static String	CO_thingFieldSeparator = "@";
	
	public static String	CO_valueIsAttributeClue = ":";

	public Dom4jDoc (Map paramMap) throws Throwable {
		Document configDoc = DocumentHelper.createDocument();
		Element configRoot = DocumentHelper.createElement(CO_overrides);
		Iterator pki = paramMap.keySet().iterator();
		while (pki.hasNext()) {
			String key = (String) pki.next();
			ParsedPair pp = ParsedPair.parsePair(key, CO_thingFieldSeparator);
			if (pp != null) {
				Element pelem = DocumentHelper.createElement(CO_param);
				if (pp.left != null && pp.left.length() > 0) {
					pelem.addAttribute(CO_field, pp.left);
				}
				if (pp.right != null && pp.right.length() > 0) {
					pelem.addAttribute(CO_thing, pp.right);
				}
				String[] values = (String []) paramMap.get(key);
				for (int i = 0; i < values.length ; i++) {
					String val = values[i];
					Element velem = DocumentHelper.createElement(CO_value);
					if (val.indexOf(CO_valueIsAttributeClue) >= 0) {
						velem.addAttribute(CO_thing, val);
					} else {
						velem.setText(val);
					}
					pelem.add(velem);
				}
				configRoot.add(pelem);
			}
		}
		configDoc.setRootElement(configRoot);
		myDom4jDoc = configDoc;
	}	


	/**
	* Override current values in this configuration with values from inDoc.  <br/>TODO:  Document the format of indoc.
	  */
	public void applyOverrides (MutableConfig conf, Abbreviator abbr) throws Throwable {
		Document configDoc = getDom4jDoc();
		Element root = configDoc.getRootElement();
		List paramElements = root.elements(CO_param);
		Iterator pei = paramElements.iterator();
		while (pei.hasNext()) {
			Element pe = (Element) pei.next();
			String tas = pe.attributeValue(CO_thing);
			String fas = pe.attributeValue(CO_field);
			String strictFlagString = pe.attributeValue(CO_strict);
			boolean strictFlag = false;
			if ((strictFlagString != null) && strictFlagString.equals("true")) {
				strictFlag = true;
			}
			
			Address thingAddress = abbr.makeAddressFromShortForm(tas);
			Address fieldAddress = abbr.makeAddressFromShortForm(fas);
			
			if (thingAddress == null | fieldAddress == null) {
				continue;
			}
			
			if (strictFlag) {
				conf.clearValues(thingAddress, fieldAddress);
			}

			List valueElements = pe.elements(CO_value);
			Iterator vei = valueElements.iterator();
			while (vei.hasNext()) {
				Element ve = (Element) vei.next();
				String valueThing = ve.attributeValue(CO_thing);
				if (valueThing != null) {
					Address valueThingAddress = abbr.makeAddressFromShortForm(valueThing);
					conf.addAddressValuedSentence (thingAddress, fieldAddress, valueThingAddress);
				} else {
					String valueString = ve.getText();
					conf.addStringValuedSentence (thingAddress, fieldAddress, valueString);
				}
			}
		}
	}
	public static Dom4jDoc readFromURL (String url) throws Throwable {
		SAXReader reader = new SAXReader();
		Document inDoc = reader.read(url);
		Dom4jDoc wrapperDoc = new Dom4jDoc (inDoc);
		return wrapperDoc;
	}
	public static Dom4jDoc buildFromW3CDOM(org.w3c.dom.Document inDocW3C) throws Throwable {
		DOMReader domReader = new DOMReader();
		Document inDoc = domReader.read(inDocW3C);
		Dom4jDoc wrapperDoc = new Dom4jDoc(inDoc);
		return wrapperDoc;
	}	
	
}
