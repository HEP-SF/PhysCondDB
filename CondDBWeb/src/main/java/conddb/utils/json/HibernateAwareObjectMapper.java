package conddb.utils.json;

import java.text.SimpleDateFormat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.hibernate4.Hibernate4Module;

/**
 * @author formica
 *
 */
public class HibernateAwareObjectMapper extends ObjectMapper {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3127251877944335700L;

	private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss z");
	
	public HibernateAwareObjectMapper() {
		System.out.println("INITIALIZE OBJECT MAPPER FOR HIBERNATE");
		Hibernate4Module hm = new Hibernate4Module();
		this.setDateFormat(dateFormat);
		registerModule(hm);
	}
}
