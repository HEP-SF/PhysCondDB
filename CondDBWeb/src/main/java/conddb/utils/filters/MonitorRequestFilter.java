package conddb.utils.filters;

import java.io.IOException;
import java.security.Principal;
import java.util.Enumeration;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//@WebFilter(filterName = "ConddbMonitorFilter", urlPatterns = { "/conddbweb/rest/*" })
public class MonitorRequestFilter implements Filter {
	 
	private Logger log = LoggerFactory.getLogger(this.getClass());

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
        
    	Enumeration<String> headers = ((HttpServletRequest) request).getHeaderNames();
		while (headers.hasMoreElements()) {
			String headername = headers.nextElement();
			String acrh = ((HttpServletRequest) request).getHeader(headername);
			log.debug("Request headers " + headername + " = " + acrh);
		}
		log.debug("Request information context path: "+((HttpServletRequest) request).getContextPath());
		log.debug("Request information path info: "+((HttpServletRequest) request).getPathInfo());
		log.debug("Request information method: "+((HttpServletRequest) request).getMethod());

		String remotehost = ((HttpServletRequest) request).getRemoteHost();
		int    remoteport = ((HttpServletRequest) request).getRemotePort();
		String remoteuser = ((HttpServletRequest) request).getRemoteUser();
		Principal userprincipal = ((HttpServletRequest) request).getUserPrincipal();
		log.debug("Other info from request: "+remotehost+", "+remoteport+", "+remoteuser+", "+((userprincipal!= null) ? userprincipal.getName(): "principal does not exists"));
		
        chain.doFilter(request, response);
    }

	@Override
	public void destroy() {
		// TODO Auto-generated method stub
	}

	@Override
	public void init(FilterConfig arg0) throws ServletException {
		// TODO Auto-generated method stub
	}
    
}
