package conddb.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class TestCondDB1 {
	final static Logger logger = LoggerFactory.getLogger(TestCondDB1.class);
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		logger.info("Initializing Spring context.");
        
        ApplicationContext applicationContext = new ClassPathXmlApplicationContext("/webapp/WEB-INF/spring/services-context.xml");
        
        logger.info("Spring context initialized.");
        
        String[] beannames = applicationContext.getBeanDefinitionNames();
        for (String nm : beannames) {
			logger.info("Loaded bean...:"+nm);
		}
        ((ConfigurableApplicationContext)applicationContext).close();
	}

}
