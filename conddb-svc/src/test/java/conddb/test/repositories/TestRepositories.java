package conddb.test.repositories;


import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;

import java.math.BigDecimal;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.domain.Specifications;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import conddb.data.Tag;
import conddb.svc.dao.controllers.GlobalTagService;
import conddb.svc.dao.exceptions.ConddbServiceException;


@ActiveProfiles({ "dev", "h2" })
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "/spring/services-context.xml" })
public class TestRepositories {

	@Autowired
	GlobalTagService globalTagService;

    @Test
    public void insertTag() {
    	System.out.println("Start test for storing Tag....");
    	Tag entity = new Tag("test-01","time","none","online","test tag",new BigDecimal(0L),new BigDecimal(-1L));
    	Tag saved=null;
		try {
			saved = globalTagService.insertTag(entity);
	    	System.out.println("Saved pojo "+saved);
		} catch (ConddbServiceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
	
}
