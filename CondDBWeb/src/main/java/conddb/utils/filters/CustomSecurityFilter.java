/**
 * 
 */
package conddb.utils.filters;

import java.io.IOException;
import java.security.Principal;
import java.util.Enumeration;
import java.util.List;

import javax.annotation.Priority;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.ext.Provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import conddb.security.cern.CernADFSParameters;
import conddb.security.data.UserPrincipal;

/**
 * @author aformic
 *
 */
@Provider
//default priority is 'USER' which is too late in the filter processing chain
@Priority(Priorities.AUTHORIZATION)
public class CustomSecurityFilter implements ContainerRequestFilter {

	@Context
	HttpServletRequest request;
	
	private Logger log = LoggerFactory.getLogger(this.getClass());
	
	private CernADFSParameters adfsParams = new CernADFSParameters();

	/* (non-Javadoc)
	 * @see javax.ws.rs.container.ContainerRequestFilter#filter(javax.ws.rs.container.ContainerRequestContext)
	 */
	@Override
	public void filter(ContainerRequestContext rctx) throws IOException {
		// TODO Auto-generated method stub
		log.debug("Calling CustomSecurityFilter Filter on request " + request.toString());
		Enumeration<String> headers = ((HttpServletRequest) request).getHeaderNames();
		while (headers.hasMoreElements()) {
			String headername = headers.nextElement();
			String acrh = ((HttpServletRequest) request).getHeader(headername);
			log.debug("Request headers " + headername + " = " + acrh);
			if (headername.startsWith("ADFS")) {
				//Verify that the user belongs to a group, store every ADFS info
				if (adfsParams.getParam(headername) == null) {
					adfsParams.setParam(headername, acrh);
				}
			}
		}
		log.debug("Loaded parameter from ADFS : \n"+adfsParams.toString());
		final String selectedrole = getRoleFromADFSGroup();
		final String selecteduser = (adfsParams.getParam("ADFS_USER") != null) ? adfsParams.getParam("ADFS_USER") : "guest";

		rctx.setSecurityContext(new SecurityContext() {
            @Override
            public Principal getUserPrincipal() {
                return new UserPrincipal(selecteduser, selectedrole, 1);
            }

            @Override
            public boolean isUserInRole(String role) {
            	log.debug("Calling isUserInRole for input "+role+" compared to stored role "+selectedrole);
                return selectedrole.equals(role);
            }

            @Override
            public boolean isSecure() {
                return false;
            }

            @Override
            public String getAuthenticationScheme() {
                return "custom";
            }
        });		
		
	}

	protected String getRoleFromADFSGroup() {
		List<String> adfsgrp = adfsParams.getGroups();
		String grouprole = "USER";
		if (adfsgrp == null) {
			return grouprole;
		}
		for (String group : adfsgrp) {
			if (group.equals("atlas-conditions-detector-contacts") && grouprole.equals("USER")) {
				grouprole = "EXPERT";
				log.debug("Setting role to EXPERT");
			} else if (group.equals("atlas-conditions-commit")) {
//				grouprole = "ADMIN";
				log.debug("Setting role to FAKE");
				grouprole = "FAKE";
			}
		}
		log.debug("Return role "+grouprole);

		return grouprole;
	}
}
