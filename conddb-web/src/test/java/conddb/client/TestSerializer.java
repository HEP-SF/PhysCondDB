package conddb.client;

import java.math.BigDecimal;
import java.net.URI;
import java.time.Instant;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.databind.ObjectMapper;

import conddb.data.GlobalTag;
import conddb.data.GlobalTagMap;
import conddb.data.Tag;
import conddb.web.resources.Link;
import conddb.web.resources.generic.GenericPojoResource;

import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

public class TestSerializer {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		TestSerializer ts = new TestSerializer();
		ts.traceGlobalTagJackson();
		ts.traceGlobalTagGeneric();
		ts.traceGlobalTagJackson();
	}

	
	public void traceGlobalTagJackson() {
		System.out.println("Start trace using jackson");
		Instant now = Instant.now();
		GlobalTag gt = new GlobalTag("TESTGTAG-01-01",new BigDecimal(99L),"test gtag","a release");
		Set<GlobalTagMap> maps = new HashSet<>();
		for (int i=0; i<10000;i++) {
			String name= "atag-"+i;
			Tag atag = new Tag(name,"time","obj","none","a tag auto generated",new BigDecimal(0L),new BigDecimal(0L));
			GlobalTagMap map = new GlobalTagMap(gt,atag,"a record "+i,"a label ");
			maps.add(map);
		}
		gt.setGlobalTagMaps(maps);
		ObjectMapper om = new ObjectMapper();
		
//		// Add filter
//		FilterProvider filters = new SimpleFilterProvider().addFilter("myFilter",
//			    SimpleBeanPropertyFilter.filterOutAllExcept("name"));
		
		Map<String, Object> objectAsMap = om.convertValue(gt, Map.class);
//		for (String akey : objectAsMap.keySet()) {
//			System.out.println("Created key "+akey+ " = "+objectAsMap.get(akey));
//		}
		Instant end = Instant.now();
		Long millis = end.toEpochMilli()-now.toEpochMilli();
		System.out.println("End trace using jackson: "+millis);
	}

	public void traceGlobalTagGeneric() {
		System.out.println("Start trace using generic");
		Instant now = Instant.now();
		GlobalTag gt = new GlobalTag("TESTGTAG-01-01",new BigDecimal(99L),"test gtag","a release");
		Set<GlobalTagMap> maps = new HashSet<>();
		for (int i=0; i<1000;i++) {
			String name= "atag-"+i;
			Tag atag = new Tag(name,"time","obj","none","a tag auto generated",new BigDecimal(0L),new BigDecimal(0L));
			GlobalTagMap map = new GlobalTagMap(gt,atag,"a record "+i,"a label ");
			maps.add(map);
		}
		gt.setGlobalTagMaps(maps);
//		URI uri = UriBuilder.fromUri("http://localhost/").path("{a}/{b}").build("physconddb/api/rest",Link.GLOBALTAGS);
//		UriInfo info = UriInfo.re
				GenericPojoResource<GlobalTag> gtag = new GenericPojoResource<>(null, gt);
//		for (Object akey : gtag.keySet()) {
//			System.out.println("Created key "+akey+ " = "+gtag.get(akey));
//		}
		Instant end = Instant.now();
		Long millis = end.toEpochMilli()-now.toEpochMilli();
		System.out.println("End trace using generic "+millis);
	}

}
