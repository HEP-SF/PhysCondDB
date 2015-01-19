/**
 * 
 */
package cool.dao.test;

import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import conddb.cool.data.GtagTagType;
import conddb.cool.migutils.JdbcCondDBRepository;
import static org.junit.Assert.assertThat;
import static org.hamcrest.Matchers.greaterThan;

/**
 * @author formica
 *
 */
@ActiveProfiles({ "dev","hsql"})
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:/spring/services-context.xml" })
public class CoolRepositoryTest {

	final static Logger logger = LoggerFactory.getLogger(CoolRepositoryTest.class);

	@Autowired
	private JdbcCondDBRepository repo;
	
	@BeforeClass
	public static void setupBeforeClass() {
		System.setProperty("socksProxyHost", "localhost");
		System.setProperty("socksProxyPort", "3129");
	}

	@Test
	public void searchAllGlobalTags() {
		try {
			List<GtagTagType> gtags = (List<GtagTagType>) repo.getTagsAssociationsFromCool("ATLAS_COOLOFL%", "CONDBR2", "%BLKP%");
			for (GtagTagType gtagTagType : gtags) {
				logger.info("Found tag "+gtagTagType.toString());
			}
            assertThat(gtags.size(), greaterThan(0));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	

}
