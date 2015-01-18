/**
 * 
 */
package conddb.dao.test;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.assertThat;
import static org.hamcrest.Matchers.is;
import conddb.dao.repositories.GlobalTagRepository;
import conddb.data.GlobalTag;

/**
 * @author formica
 *
 */
@ActiveProfiles({ "dev","hsql" })
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:/spring/services-context.xml" })
public class GlobalTagRepositoryTest {

	@Autowired
	private GlobalTagRepository repo;

	@Test
	public void searchAllGlobalTags_ShouldReturnEmptyList() {
		List<GlobalTag> gtags = (List<GlobalTag>) repo.findAll();
		assertThat(gtags.size(), is(0));
	}
	
	@Test
	public void insertGlobalTag() {
		GlobalTag gtag = new GlobalTag("TEST_01",new BigDecimal(0),"test global tag","test",new Date(),new Date());
		repo.save(gtag);
		List<GlobalTag> gtags = (List<GlobalTag>) repo.findAll();
		assertThat(gtags.size(), is(1));
		
		repo.deleteAll();
	}

}
