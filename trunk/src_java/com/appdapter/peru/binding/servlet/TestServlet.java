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
 * (c) Copyright 2003, 2004 Hewlett-Packard Development Company, LP
 * [See end of file]
 * 
 * dumpEnvironment and dumpServletContext from patch by Fred Hartman // webMethods.
 */

// Could be neater - much, much neater!
package com.appdapter.peru.binding.servlet;

import java.util.* ;
import java.io.* ;
import javax.servlet.http.*;
import javax.servlet.* ;

/**
 * A servlet that dumps its request
 *
 * @author      Stu B. <www.texpedient.com>
 * @version     @PERUSER_VERSION@
 */
public class TestServlet extends HttpServlet
{

    public TestServlet()
	{ }

    public void init()
    {
        return ;
    }

    public void doGet(HttpServletRequest req, HttpServletResponse resp)
    {
        try {
            PrintWriter out = resp.getWriter() ;
            resp.setContentType("text/html");

            String now = new Date().toString() ;

            // HEAD
            out.println("<html>") ;
            out.println("<head>") ;
            out.println("<Title>Dump @ "+now+"</Title>") ;
            // Reduce the desire to cache it.
            out.println("<meta CONTENT=now HTTP-EQUIV=expires>") ;
            out.println("</head>") ;

            // BODY
            out.println("<body>") ;
            out.println("<pre>") ;

            out.println("SMOOCHES!");
            out.println("Dump : "+now);
            out.println() ;
            out.println("==== Request");
            out.println() ;
            out.print(dumpRequest(req)) ;
            out.println() ;
                        
            out.println("==== ServletContext");
            out.println() ;
            out.print(dumpServletContext());
            out.println() ;

            out.println("==== Environment");
            out.println() ;
            out.print(dumpEnvironment());
            out.println() ;

            out.println("</pre>") ;

            out.println("</body>") ;
            out.println("</html>") ;
            out.flush() ;
        } catch (IOException e)
        { }
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
     * 			The values are with single quotes independent of whether or not the
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
        return "Peruser WebTest Servlet";
    }
}
