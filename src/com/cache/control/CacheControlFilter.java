package com.cache.control;

import java.io.IOException;
import java.util.Iterator;
import java.util.Set;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class CacheControlFilter implements Filter {

	public void init(FilterConfig config) throws ServletException {
	    ServletContext servletContext = config.getServletContext();

		CacheControlUtils.setDebugCode("true".equalsIgnoreCase(config.getInitParameter("debugCode")));

		String pathCss = config.getInitParameter("path_css");
		if (pathCss != null) {
		    String[] paths = pathCss.split(",");
		    for (int i=0; i < paths.length; i++) {
		        populateEncodedFiles(servletContext, paths[i]);
		    }
		}

		String pathJs = config.getInitParameter("path_js");
        if (pathJs != null) {
            String[] paths = pathJs.split(",");
            for (int i=0; i < paths.length; i++) {
                populateEncodedFiles(servletContext, paths[i]);
            }
        }
	}

	public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) 
			throws ServletException, IOException {
		
		HttpServletRequest request = (HttpServletRequest) req;
		HttpServletResponse response = (HttpServletResponse) res;
		
		String uri = request.getRequestURI();

		CacheControlUtils.debug("doFilter: uri [" + uri + "]");
		
		if (uri.indexOf(CacheControlUtils.FILTER_KEY) > -1) {
			String uriAux = uri.substring(uri.indexOf(CacheControlUtils.FILTER_KEY) + CacheControlUtils.FILTER_KEY_SIZE);
			CacheControlUtils.debug("uri_real [" + uriAux + "]");

			request.getRequestDispatcher(uriAux).forward(request, response);
			return;
		}

		chain.doFilter(request, response);
	}
	
	public void destroy() {
	}

	private void populateEncodedFiles(ServletContext servletContext, String pathResources) {
        
        Set resources = servletContext.getResourcePaths(pathResources);
        CacheControlUtils.debug("path [" + pathResources + "] resources [" + resources + "]");
        
        if (resources == null) {
            CacheControlUtils.warn("CacheControlFilter.populateEncodedFiles: path [" + pathResources + "] have not files.");
            return;
        }
        
        Iterator iter = resources.iterator();
        while (iter.hasNext()) {
            String path = (String) iter.next();
            String encodeFile = CacheControlUtils.getEncodeFile(servletContext, path);

            // put in cache
            if (encodeFile != null) {
                CacheControlUtils.getEncodedFiles().put(path, encodeFile);    
            }
        }
    }
}