/**
 * 
 */
package conddb.security;

import java.util.ArrayList;
import java.util.List;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

/**
 * @author formica
 *
 */
@Component
public class CustomAuthenticationProvider implements AuthenticationProvider {
 
    @Override
    public Authentication authenticate(Authentication authentication) 
      throws AuthenticationException {
        String name = authentication.getName();
        String password = authentication.getCredentials().toString();
 
        // use the credentials to try to authenticate against the third party system
        if (authenticatedAgainstThirdPartySystem()) {
            List<GrantedAuthority> grantedAuths = new ArrayList<>();
            GrantedAuthority adminauth = new SimpleGrantedAuthority("ROLE_USER");
            grantedAuths.add(adminauth);
            return new UsernamePasswordAuthenticationToken(name, password, grantedAuths);
        } else {
            throw new AuthenticationServiceException("error in authentication to third party");
        }
    }
 
    private boolean authenticatedAgainstThirdPartySystem() {
    	System.out.println("Implement authentication");
		return true;
	}

	@Override
    public boolean supports(Class<?> authentication) {
        return authentication.equals(UsernamePasswordAuthenticationToken.class);
    }
}