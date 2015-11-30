package conddb.utils.filters;

import java.io.IOException;
import java.security.Principal;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Collection;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MultivaluedMap;

import org.glassfish.jersey.filter.LoggingFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Value;

import conddb.security.data.LogCondRequests;
import conddb.security.svc.dao.repositories.LogCondRequestsRepository;
import conddb.utils.PropertyConfigurator;
 
public class CustomLoggingFilter extends LoggingFilter implements ContainerRequestFilter, ContainerResponseFilter {

	private Logger log = LoggerFactory.getLogger(this.getClass());
	private Instant start;
	private Instant end;
	
	public String monitorLogging = PropertyConfigurator.getInstance().getMonitorLogging();
	
	@Autowired
	LogCondRequestsRepository logCondRequestRepository;
	
	@Context 
	private HttpServletRequest httpRequest;
	
    @Override
    public void filter(ContainerRequestContext requestContext)  throws IOException
    {
    	if (monitorLogging.equals("skip")) {
    		return;
    	}
    	start = Instant.now();
        StringBuilder sb = new StringBuilder();
        Principal user = requestContext.getSecurityContext().getUserPrincipal();
        sb.append("User: ").append(user == null ? "unknown"
                        : user);
        sb.append(" - Path: ").append(requestContext.getUriInfo().getPath());
        sb.append(" - Header: ").append(requestContext.getHeaders());
        sb.append(" - Start time : "+start.toString());
 //       sb.append(" - Entity: ").append(getEntityBody(requestContext));
        requestContext.setProperty("timespent", start);
   
        Collection<String> properties = requestContext.getPropertyNames();
        sb.append(" - Properties names : ");
        for (String name : properties) {
            sb.append(" "+name);
		}
        
        MultivaluedMap<String, String> quaeryparam = requestContext.getUriInfo().getQueryParameters();
        sb.append(" - QueryParams : ");
        StringBuffer qryparamsurl = new StringBuffer();
        for (String param : quaeryparam.keySet()) {
            sb.append(" "+param+"="+quaeryparam.getFirst(param));
            qryparamsurl.append(param+"="+quaeryparam.getFirst(param)+"&");
		}
 
        MultivaluedMap<String, String> pathparam = requestContext.getUriInfo().getPathParameters();
        sb.append(" - PathParams : ");
        for (String param : pathparam.keySet()) {
            sb.append(" "+param+"="+pathparam.getFirst(param));
		}
        String serverhost = ((HttpServletRequest) httpRequest).getHeader("Host");
		String remotehost = ((HttpServletRequest) httpRequest).getRemoteHost();
		String remoteuser = ((HttpServletRequest) httpRequest).getRemoteUser();

        LogCondRequests logreq = new LogCondRequests();
        logreq.setHttpMethod(requestContext.getMethod());
        logreq.setStart(new Timestamp(start.toEpochMilli()));
        logreq.setServerHost(serverhost);
        logreq.setRemoteHost(remotehost);
        logreq.setRequestHeader(requestContext.getHeaders().toString());
        logreq.setUserRole(user.getName());
        logreq.setUserName((remoteuser==null) ? "unknown" : remoteuser);
        String path = requestContext.getUriInfo().getPath();
        if (qryparamsurl.length()>0) {
        	path += ("?"+qryparamsurl);
        }
        logreq.setRequestUrl(path);
        requestContext.setProperty("logreq", logreq);

        log.debug("HTTP REQUEST : " + sb.toString());
    }
 
    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) throws IOException
    {
    	if (monitorLogging.equals("skip")) {
    		return;
    	}
    	end = Instant.now();
        start = (Instant) requestContext.getProperty("timespent");
        LogCondRequests logreq = (LogCondRequests)requestContext.getProperty("logreq");
        logreq.setEnd(new Timestamp(end.toEpochMilli()));
        
        if (monitorLogging.equals("save")) {
        	logCondRequestRepository.save(logreq);
        }
        StringBuilder sb = new StringBuilder();
        sb.append("Header: ").append(responseContext.getHeaders());
        sb.append(" - End time : "+end.toString());
        sb.append(" - Time spent (ms) : "+(end.toEpochMilli()-start.toEpochMilli()));
        
        //sb.append(" - Entity: ").append(responseContext.getEntity());
        log.debug("HTTP RESPONSE : " + sb.toString());
    }
}