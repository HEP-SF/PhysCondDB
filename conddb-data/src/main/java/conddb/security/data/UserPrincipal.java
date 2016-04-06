/**
 * 
 */
package conddb.security.data;

import java.security.Principal;

/**
 * @author aformic
 *
 */
public class UserPrincipal implements Principal {

	private final String name;		
    private final String role;
    private final int accessLevel;

    public UserPrincipal(String name, String role, int accessLevel) {
        this.name = name;
        this.role = role;
        this.accessLevel = accessLevel;
    }
    
    public UserPrincipal(String name) {
		super();
		this.name = name;
		this.role = "USER";
		this.accessLevel=0;
	}


     public String getRole() {
        return role;
    }

    public int getAccessLevel() { return accessLevel; }


	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return name;
	}
	
}
