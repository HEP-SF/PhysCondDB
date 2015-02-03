/**
 * 
 */
package cool.security;

import static org.hamcrest.Matchers.greaterThan;
import static org.junit.Assert.assertThat;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.codec.Base64;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * @author formica
 *
 */
@ActiveProfiles({ "dev","h2"})
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:/spring/services-context.xml" })
public class Base64Test {

	final static Logger logger = LoggerFactory.getLogger(Base64Test.class);
	
	@BeforeClass
	public static void setupBeforeClass() {
		System.setProperty("socksProxyHost", "localhost");
		System.setProperty("socksProxyPort", "3129");
	}

	@Test
	public void encodeBasic() {
//		String encoded = BasePasswordEncoder.encodePassword("user:userPass", null);
		//encode("user:userPass".getBytes());
		byte[] encoded = Base64.encode("user:userPass".getBytes());
		String encstr=new String(encoded);
		System.out.println("Encoded password is : "+encstr);
		byte[] decoded = Base64.decode(encstr.getBytes());
		System.out.println("Decoded password is : "+new String(decoded));
		assertThat(encstr.length(), greaterThan(0));
	}
	
}
