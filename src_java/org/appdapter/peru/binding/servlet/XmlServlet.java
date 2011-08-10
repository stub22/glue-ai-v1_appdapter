/*
 *  Copyright 2011 by The Appdapter Project (www.appdapter.com).
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


/*
*  Based, for the moment, on HP-Joseki DumpServlet
*/

package org.appdapter.peru.binding.servlet;

import java.util.* ;
import java.io.* ;
import javax.servlet.http.*;
import javax.servlet.* ;

import java.util.Set;
import java.util.HashSet;
import java.util.List;
import java.util.ArrayList;


import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.transform.stream.StreamResult;

import org.dom4j.Document;
import org.dom4j.io.DocumentSource;

import java.io.OutputStream;
import java.io.FileInputStream;


import org.appdapter.peru.core.name.CoreAddress;

import org.appdapter.peru.core.machine.*;
import org.appdapter.peru.core.document.Doc;
import org.appdapter.peru.binding.dom4j.Dom4jDoc;

import org.appdapter.peru.core.environment.Environment;

import org.appdapter.peru.binding.jena.JenaConfiguredCommandMachine;

import com.hp.hpl.jena.rdf.model.Model;
import org.appdapter.peru.binding.jena.ModelUtils;
import org.appdapter.peru.binding.jena.JenaPulljector;

import org.appdapter.peru.module.projector.ProjectedNode;
import org.appdapter.peru.module.projector.ProjectorUtils;
import org.appdapter.peru.module.projector.SimpleAxisQuery;

/**
 * @author      Stu B. <www.texpedient.com>
 * @version     @PERUSER_VERSION@
 */
public class XmlServlet extends HttpServlet
{

    public XmlServlet()
    { }

    public void init()
    {
        return ;
    }
	
	public static String TEST_NS="http://www.logicu.com/web.owl#";
	public static String RDFS_NS="http://www.w3.org/2000/01/rdf-schema#";
	
	private void writeSubclassTreeWithComments(String modelPath, String rootNS, String rootFrag, int maxDepth, OutputStream os) throws Throwable {
		ProjectedNode node = projectBackpointerTree(modelPath, rootNS, rootFrag, RDFS_NS, "subClassOf");
		List fieldPropUris = new ArrayList();
		fieldPropUris.add(RDFS_NS + "comment");
		writeStyledTree(node, fieldPropUris, maxDepth, "/substrate/xslt/projector_tree.xslt", os);
	}
	private void writeInstanceTreeWithField(String modelPath, String rootNS, String rootFrag, int maxDepth, String axisPropNS, String axisPropFrag,
				String fieldPropNS, String fieldPropFrag, OutputStream os) throws Throwable {
		ProjectedNode node = projectBackpointerTree(modelPath, rootNS, rootFrag, axisPropNS, axisPropFrag);
		List fieldPropUris = new ArrayList();
		fieldPropUris.add(fieldPropNS + fieldPropFrag);
		writeStyledTree(node, fieldPropUris, maxDepth, "/substrate/xslt/projector_tree.xslt", os);
	}	
	private ProjectedNode projectBackpointerTree(String modelPath, String rootNS, String rootFrag, String propertyNS, String propertyFrag) throws Throwable  {
		HashSet	axisQuerySet = new HashSet();
		SimpleAxisQuery axisQuery = new SimpleAxisQuery(propertyNS + propertyFrag, SimpleAxisQuery.CHILD_POINTS_TO_PARENT);
		axisQuerySet.add(axisQuery);
		return projectModel(modelPath, rootNS, rootFrag, axisQuerySet);
	}
	// relativeStylePath used in testing: "/substrate/xslt/projector_tree.xslt"
	private void writeStyledTree (ProjectedNode rootNode, List fieldQueryUriList, int maxDepth, String relativeStylePath, OutputStream os) throws Throwable {			
		Document thingyDoc = 	ProjectorUtils.createDom4jDocument(rootNode, fieldQueryUriList, 3);
		styleDocumentAndOutput(thingyDoc, relativeStylePath, os);
	}
	private ProjectedNode projectModel(String modelPath, String rootNS, String rootFrag, Set axisQuerySet) throws Throwable {
		String path = getServletContext().getRealPath(modelPath);
		System.out.println ("Model path is " + path);
		FileInputStream	fis = new FileInputStream(path);
		//JenaPulljector jp = JenaPulljector.makePulljectorFromModelStream(fis, null);
		// 2nd arg is a default namespace hint
		Model baseModel = ModelUtils.loadJenaModelFromXmlSerialStream(fis, null);
		// 2nd arg is an ontology spec
		JenaPulljector jp = JenaPulljector.makePulljectorFromBaseModelAndOntSpec(baseModel, null);
		
		ProjectedNode resultNode = jp.projectNode(rootNS + rootFrag, axisQuerySet);		
		return resultNode;
	}
	private void writeIndividualsAsXML(String modelPath, String clsNS, String clsFrag, OutputStream os) throws Throwable {
		String flavorURI = clsNS + clsFrag;
		String mpath = getServletContext().getRealPath(modelPath);	
		String qpath = getServletContext().getRealPath("substrate/rdql/luweb_selector_test.rdql");	
		org.appdapter.peru.binding.jena.RDQL_Utils.queryOntModelWithRDQLtoWriteXML(mpath, qpath, flavorURI, os);
	}
	private void styleDocumentAndOutput (Document doc, String xsltPath, OutputStream os) throws Throwable {
		String stylePath = getServletContext().getRealPath(xsltPath);
		TransformerFactory tFactory = TransformerFactory.newInstance();
	
		DocumentSource xmlSource = new DocumentSource(doc);
		
		Source xslSource = new StreamSource(stylePath);
		xslSource.setSystemId(stylePath);
		
		// Parse the XSLT to produce a JAXP transformer
		Transformer transformer = tFactory.newTransformer(xslSource);
		// Not yet using transfomer properties, but they are set with:		transformer.setParameter(paramName, paramValue);
		
		// Perform the transformation, sending the output to the response.
		transformer.transform(xmlSource, new StreamResult(os));		
	}
    public void doGet(HttpServletRequest request, HttpServletResponse response)
    {
		System.out.println("McLuhan XmlServlet - bulb is warming up!");	
        try {
			response.setContentType("text/xml; charset=UTF-8");
			// A path known to work:  
			String pathInfo = request.getPathInfo();
			String opName = request.getParameter("op");
			String fragMaj = request.getParameter("fragMaj");
			String fragMin = request.getParameter("fragMin");
			
			if (pathInfo == null) {
				pathInfo = "/substrate/owl/lu_web_p31_121.owl";
			}
			if (opName == null) {
				opName = "subclass_tree";
			}
			if (fragMaj == null) {
				fragMaj = "Thingy";
			}
			
			System.out.println("pathInfo=" + pathInfo);
			System.out.println("op=" + opName);
			System.out.println("fragMaj=" + fragMaj);
			System.out.println("fragMin=" + fragMin);
			OutputStream out = response.getOutputStream();
			if (opName.equals("subclass_tree")) {
				writeSubclassTreeWithComments(pathInfo, TEST_NS, fragMaj, 3, out);
			} else if (opName.equals("backptr_tree")) {
				writeInstanceTreeWithField(pathInfo, TEST_NS, fragMaj, 3, TEST_NS, fragMin, TEST_NS, "name", out);
			} else if (opName.equals("getrows")) {
				writeIndividualsAsXML(pathInfo, TEST_NS, fragMaj, out);
			} else if (opName.equals("cop")) {
				Doc paramDoc = new Dom4jDoc(request.getParameterMap());
				paramDoc.writePretty(out);
			} else if (opName.equals("mop")) {
				Doc paramDoc = new Dom4jDoc(request.getParameterMap());
				Machine machine = new JenaConfiguredCommandMachine();
				String relModelPath = "substrate/protege_test_projects/subthing_owl_31b191/subthing_31b191.owl";
				ServletContext sc = getServletContext();
				Environment env = new ServletEnvironment(sc);
				machine.setup(relModelPath, env);
				
				
				String instructUriString = "ERROR:INSTRUCT_URI";
				CoreAddress instructAddr = new CoreAddress(instructUriString);
				
				
				Doc resultDoc = (Doc) machine.process(instructAddr, paramDoc);
				resultDoc.writePretty(out);
			}
			out.flush();
		/*
            out.print(dumpRequest(req)) ;
            out.print(dumpServletContext());
            out.print(dumpEnvironment());
			*/
        } catch (Throwable t)   {
			t.printStackTrace();
		}
		System.out.println("McLuhan XmlServlet - bulb is cooling down!");	
    }

    // This resets the input stream

    static public String dumpRequest(HttpServletRequest req)
    {
        try {
            StringWriter sw = new StringWriter() ;
            PrintWriter pw = new PrintWriter(sw) ;

            // Standard environment
            pw.println("Method:                 "+req.getMethod());
            pw.println("getContentLength:       "+Integer.toString(req.getContentLength()));
            pw.println("getContentType:         "+req.getContentType());
            pw.println("getRequestURI:          "+req.getRequestURI());
            pw.println("getRequestURL:          "+req.getRequestURL());
            pw.println("getContextPath:         "+req.getContextPath());
            pw.println("getServletPath:         "+req.getServletPath());
            pw.println("getPathInfo:            "+req.getPathInfo());
            pw.println("getPathTranslated:      "+req.getPathTranslated());
            pw.println("getQueryString:         "+req.getQueryString());
            pw.println("getProtocol:            "+req.getProtocol());
            pw.println("getScheme:              "+req.getScheme());
            pw.println("getServerName:          "+req.getServerName());
            pw.println("getServerPort:          "+req.getServerPort());
            pw.println("getRemoteUser:          "+req.getRemoteUser());
            pw.println("getRemoteAddr:          "+req.getRemoteAddr());
            pw.println("getRemoteHost:          "+req.getRemoteHost());
            pw.println("getRequestedSessionId:  "+req.getRequestedSessionId());
            {
                String tmp = "" ;
                Cookie c[] = req.getCookies() ;
                if ( c == null )
                    pw.println("getCookies:            <none>");
                else
                {
                    for ( int i = 0 ; i < c.length ; i++ )            
                    {
                        pw.println("Cookie:        "+c[i].getName());
                        pw.println("    value:     "+c[i].getValue());
                        pw.println("    version:   "+c[i].getVersion());
                        pw.println("    comment:   "+c[i].getComment());
                        pw.println("    domain:    "+c[i].getDomain());
                        pw.println("    maxAge:    "+c[i].getMaxAge());
                        pw.println("    path:      "+c[i].getPath());
                        pw.println("    secure:    "+c[i].getSecure());
                        pw.println();
                    }
                }
            }
            // To do: create a string for the output so can send to console and return it.
            Enumeration iter = req.getHeaderNames() ;

            for ( ; iter.hasMoreElements() ; )
            {
                String name = (String)iter.nextElement() ;
                String value = req.getHeader(name) ;
                pw.println("Head: "+name + " = " + value) ;
            }

            iter = req.getAttributeNames() ;
            if ( iter.hasMoreElements() )
                pw.println();
            for ( ; iter.hasMoreElements() ; )
            {
                String name = (String)iter.nextElement() ;
                String value = req.getAttribute(name).toString() ;
                pw.println("Attr: "+name + " = " + value) ;
            }

            // Note that doing this on a form causes the forms content (body) to be read
            // and parsed as form variables.
//            iter = req.getParameterNames() ;
//            if ( iter.hasMoreElements() )
//                pw.println();
//            for ( ; iter.hasMoreElements() ; )
//            {
//                String name = (String)iter.nextElement() ;
//                String value = req.getParameter(name) ;
//                pw.println("Param: "+name + " = " + value) ;
//            }

            // Don't use ServletRequest.getParameter or getParamterNames
            // as that reads form data.  This code parses just the query string.
            if ( req.getQueryString() != null )
            {
                pw.println();
                String[] params = req.getQueryString().split("&") ;
                for ( int i = 0 ; i < params.length ; i++ )
                {
                    String p = params[i] ;
                    String[] x = p.split("=",2) ;
                    String name = null ;
                    String value = null ;
                    
                    if ( x.length == 0 )
                    {
                        name = p ;
                        value = "" ;
                    }
                    else if ( x.length == 1 )
                    {
                        name = x[0] ;
                        value = "" ;
                    }
                    else
                    {
                        name = x[0] ;
                        value = x[1] ;
                    }
                    pw.println("Param: "+name + " = " + value) ;
                }
            }
            
            iter = req.getLocales();
            if ( iter.hasMoreElements() )
                pw.println();
            for ( ; iter.hasMoreElements() ; )
            {
                String name = ((Locale)iter.nextElement()).toString() ;
                pw.println("Locale: "+name) ;
            }

            pw.println() ;

            BufferedReader in = req.getReader() ;
            if ( req.getContentLength() > 0 )
                // Need +2 because last line may not have a CR/LF on it.
                in.mark(req.getContentLength()+2) ;
            else
                // This is a dump - try to do something that works, even if inefficient.
                in.mark(100*1024) ;


            while(in.ready())
            {
                pw.println(in.readLine());
            }

            try { in.reset() ;} catch (IOException e) { System.out.println("DumpServlet: Reset of content failed: "+e) ; }

            pw.close() ;
            sw.close() ;
            return sw.toString() ;
        } catch (IOException e)
        { }
        return null ;
    }

    /**
     * <code>dumpEnvironment</code>
     * @return	String that is the HTML of the System properties as name/value pairs.
     *			The values are with single quotes independent of whether or not the 
	 *			value has single quotes in it.
     */
    static public String dumpEnvironment()
    {
        Properties properties = System.getProperties();
        StringWriter sw = new StringWriter() ;
        PrintWriter pw = new PrintWriter(sw) ;
        Enumeration iter = properties.keys();
        while(iter.hasMoreElements())
        {
            String key = (String)iter.nextElement();
            pw.println(key+": '"+properties.getProperty(key)+"'");
        }
        pw.println() ;
        pw.close() ;
        try {
            sw.close() ;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return sw.toString() ;      
    }

    public String dumpServletContext()
    {
        StringWriter sw = new StringWriter() ;
        PrintWriter pw = new PrintWriter(sw) ;

        ServletContext sc =  getServletContext();
        pw.println("majorVersion: '"+sc.getMajorVersion()+"'");
        pw.println("minorVersion: '"+sc.getMinorVersion()+"'");
        pw.println("contextName:  '"+sc.getServletContextName()+"'");
        pw.println("servletInfo:  '"+getServletInfo()+"'");
        pw.println("serverInfo:  '"+sc.getServerInfo()+"'");

        Enumeration iter = null ;
        // Deprecated and will be removed - from Servlet API 2.0
//        Enumeration iter = sc.getServlets();
//        if (iter != null) {
//            pw.println("servlets: ");
//            while(iter.hasMoreElements())
//            {
//                String key = (String)iter.nextElement();
//                try {
//                    pw.println(key+": '"+sc.getServlet(key)+"'");
//                } catch (ServletException e1) {
//                    pw.println(key+": '"+e1.toString()+"'");
//                }
//            }
//        }
        iter = sc.getInitParameterNames();
        if (iter != null) {
            pw.println("initParameters: ");
            while(iter.hasMoreElements())
            {
                String key = (String)iter.nextElement();
                pw.println(key+": '"+sc.getInitParameter(key)+"'");
            }
        }
        iter = sc.getAttributeNames();
        if (iter != null) {
            pw.println("attributes: ");
            while(iter.hasMoreElements())
            {
                String key = (String)iter.nextElement();
                pw.println(key+": '"+sc.getAttribute(key)+"'");
            }
        }
        pw.println() ;
        pw.close() ;
        try {
            sw.close() ;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return sw.toString() ;      
    }

    
    public void doPost(HttpServletRequest req, HttpServletResponse resp)
    {
        doGet(req, resp) ;
    }


    public String getServletInfo()
    {
        return "Peruser McLuhan XML Servlet";
    }
}
