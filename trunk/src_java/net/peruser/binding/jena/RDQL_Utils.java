package net.peruser.binding.jena;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringWriter;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;

import org.apache.xerces.util.URI;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

// import com.hp.hpl.jena.rdql.*; 

// We use the Xerces URI parser to find fragments in URIs.

/** 
 * Encapsulates our use of jena RDQL features.
 * 
 * @author      Stu Baurmann
 * @version     @PERUSER_VERSION@
 */
public class RDQL_Utils
{
		private static Log 		theLog = LogFactory.getLog(RDQL_Utils.class);
	public static final String LF = System.getProperty("line.separator" );
		
	public static void queryOntModelWithRDQLtoWriteXML(String mpath, String qpath, 
			String flavorURI, OutputStream out) throws Throwable {
				
		OntModelSpec oms = OntModelSpec.RDFS_MEM_RDFS_INF;
		queryOntModelWithRDQLtoWriteXML(mpath, qpath, oms, flavorURI, out);
	}
			
	public static void queryOntModelWithRDQLtoWriteXML(String mpath, String qpath, 
			OntModelSpec oms, String flavorURI, OutputStream out) throws Throwable {

		theLog.debug("=====================================================================================");
		
		FileInputStream	modelInputStream = new FileInputStream(mpath);
		Model baseModel = ModelFactory.createDefaultModel();
		baseModel.read(modelInputStream, null);
		OntModel ontModel = ModelFactory.createOntologyModel(oms, baseModel);
		
		FileReader	qfr = new FileReader(qpath);
		
		BufferedReader qbr = new BufferedReader(qfr);
		StringWriter qsw = new StringWriter();
		String line;
		while ((line = qbr.readLine()) != null) {
			qsw.write(line + LF);
		}
		qbr.close();
		String queryText = qsw.toString();
		
		theLog.warn("=====================================================================================");
		theLog.warn("[DEPRECATED RDQL QUERY IGNORED]: " +  queryText);
		theLog.warn("=====================================================================================");
	}
	/*
		ResultBindingImpl argBinding = new ResultBindingImpl();
		Resource fres = ontModel.getResource(flavorURI);
		argBinding.add("flavor", fres);
		
		Document doc = queryModelWithRDQLtoProduceDom4jDoc (ontModel, queryText, argBinding);
		OutputFormat format = OutputFormat.createPrettyPrint();
		XMLWriter writer = new XMLWriter(out, format);
		writer.write(doc);
	
	}
	public static Document queryModelWithRDQLtoProduceDom4jDoc(Model ontModel, 
			String queryText, ResultBindingImpl argBinding) throws Throwable {

			System.out.println("=====================================================================================");
			System.out.println("[DEPRECATED RDQL QUERY IGNORED]: " +  queryText);
		System.out.println("=====================================================================================");
		return null;

		Query q = new Query (queryText);
		q.setSource(ontModel);
		QueryEngine qe = new QueryEngine(q);

		QueryResults qr = qe.exec(argBinding);
		Element bagElement = DocumentHelper.createElement("bag");
		
		HashMap		elementsByThingFrag = new HashMap();
		
		while (qr.hasNext()) {
			ResultBinding rb = (ResultBinding) qr.next();
			String thingFrag =  getFragment(rb.get("thing"));
			String fieldFrag = getFragment(rb.get("field"));
			String valString = rb.get("val").toString();
			System.out.println("thing=" + thingFrag + ", field=" + fieldFrag + ", val=" + valString);
			Element te = (Element) elementsByThingFrag.get(thingFrag);
			if (te == null) {
				te = DocumentHelper.createElement("thing");
				bagElement.add(te);
				elementsByThingFrag.put(thingFrag, te);
				te.addAttribute("frag", thingFrag);
			}
			Element fe = DocumentHelper.createElement("field");
			fe.addAttribute("name", fieldFrag);
			fe.setText(valString);
			te.add(fe);
			// e.addAttribute
		}

		Document doc = DocumentHelper.createDocument(bagElement);
		return doc;
	
	}
	*/
	public static Resource getResultResource (Object o) throws Throwable {
		RDFNode rn = (RDFNode) o;
		if (!rn.isResource() || !rn.canAs(Resource.class)) {
			throw new Exception ("Can't extract URI from non-resource result node: " + o.toString());
		}
		Resource res = (Resource) rn.as(Resource.class);
		return res;
	}
	public static String getFragment (Object o) throws Throwable {
		Resource res = getResultResource(o);
		URI	xercesURI = new URI(res.getURI());
		String frag = xercesURI.getFragment();
		return frag;
		
	}
}

