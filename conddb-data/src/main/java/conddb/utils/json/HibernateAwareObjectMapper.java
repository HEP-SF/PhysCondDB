package conddb.utils.json;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.cfg.HandlerInstantiator;
import com.fasterxml.jackson.datatype.hibernate4.Hibernate4Module;
import com.fasterxml.jackson.datatype.jsr310.JSR310Module;

/**
 * @author formica
 *
 */
@Component
public class HibernateAwareObjectMapper extends ObjectMapper {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3127251877944335700L;

	@Autowired
	private ApplicationContext applicationContext;

//	private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss z");
	
	public HibernateAwareObjectMapper() {
		System.out.println("INITIALIZE OBJECT MAPPER FOR HIBERNATE");
		Hibernate4Module hm = new Hibernate4Module();
//		hm.enable(Hibernate4Module.Feature.FORCE_LAZY_LOADING);
		enable(SerializationFeature.INDENT_OUTPUT);
		registerModule(hm);
		registerModule(new JSR310Module());
	}

	@Override
	@Autowired
	public Object setHandlerInstantiator(HandlerInstantiator hi) {
		System.out.println("Setting handler in objectmapper "+hi.getClass().getName());
		return super.setHandlerInstantiator(hi);
	}	
}
