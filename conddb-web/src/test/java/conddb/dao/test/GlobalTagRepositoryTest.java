/**
 *
 */
package conddb.dao.test;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import conddb.data.GlobalTag;
import conddb.svc.dao.controllers.GlobalTagAdminService;
import conddb.svc.dao.controllers.GlobalTagExpertService;
import conddb.svc.dao.exceptions.ConddbServiceException;
import conddb.svc.dao.repositories.GlobalTagRepository;

/**
 * @author formica
 *
 */
@ActiveProfiles({ "dev", "h2" })
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:/spring/services-context.xml" })
public class GlobalTagRepositoryTest {

	@Autowired
	private GlobalTagRepository repo;
	@Autowired
	private GlobalTagAdminService gtagadmin;
	@Autowired
	private GlobalTagExpertService gtagexpert;

	private String gtagtestname = "TEST-01-01-00";
	
	@Test
	public void searchAllGlobalTags_ShouldReturnEmptyList() {
		List<GlobalTag> gtags = (List<GlobalTag>) this.repo.findAll();
		assertThat(gtags.size(), is(0));
	}

	protected void initGlobalTag() {
		GlobalTag gtag = new GlobalTag(gtagtestname, new BigDecimal(0),
				"test global tag", "test", 
				new Timestamp(new Date().getTime()));
		this.repo.save(gtag);
	}
	
	protected void cleanGlobalTag() {
		this.repo.deleteAll();
	}

	@Test
	public void insertGlobalTag() {
		initGlobalTag();
		List<GlobalTag> gtags = (List<GlobalTag>) this.repo.findByNameLike(gtagtestname);
		assertThat(gtags.size(), is(1));
	}
	
	@Test
	public void updateGlobalTagLocking() {
		try {
			gtagexpert.updateGlobalTagLocking(gtagtestname, "locked");
			GlobalTag gtag = repo.findByName(gtagtestname);
			assertThat(gtag.getLockstatus(), is("locked"));
		} catch (ConddbServiceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
