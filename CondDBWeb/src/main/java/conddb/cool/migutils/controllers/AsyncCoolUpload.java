/**
 * 
 */
package conddb.cool.migutils.controllers;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import conddb.cool.migutils.GtagTagType;
import conddb.cool.migutils.svc.CoolMigrationService;

/**
 * @author formica
 *
 */
@RestController
public class AsyncCoolUpload {


	private Logger log = LoggerFactory.getLogger(getClass());

	@Autowired
	private CoolMigrationService coolsvc;
	
	@Autowired
	private TaskExecutor taskExecutor;


	@RequestMapping(value = "/loadtags", method = RequestMethod.POST)
	@ResponseBody
	@Async
	public String loadCoolGtagTags(
			@RequestParam(value = "name", defaultValue = "ATLAS_COOL%") String schemaName,
			@RequestParam(value = "dbname", defaultValue = "CONDBR2") String instance,
			@RequestParam(value = "gtag", defaultValue = "CONDBR%") String gtagpattern)
			throws Exception {
		log.info(
				"Asynchronous processing request for loadCoolGtagTags: schemaName={} ...",
				schemaName);
		
		CoolLoaderTask mytask = new CoolLoaderTask("uploadGtagsTags");
		mytask.setCoolSvc(coolsvc);
		mytask.setDb(instance);
		mytask.setSchema(schemaName);
		mytask.setTag(gtagpattern);
		taskExecutor.execute(mytask);
		
		return "Task for uploading COOL global tags and tags has been launched";
	}
	
	@RequestMapping(value = "/loadiovs", method = RequestMethod.POST)
	@ResponseBody
	public String loadCoolIovs(@RequestParam(value="tag", defaultValue="M%") String tagpattern) throws Exception {

		log.info("Asynchronous processing request for loadCoolIovs");
		
		CoolLoaderTask mytask = new CoolLoaderTask("uploadIovs");
		mytask.setCoolSvc(coolsvc);
		mytask.setTag(tagpattern);
		taskExecutor.execute(mytask);
		
		return "Task for uploading COOL iovs has been launched using "+tagpattern;
	}


	private class CoolLoaderTask implements Runnable {

		private CoolMigrationService coolsvc;

		private String message;
		private String schema;
		private String db;
		private String node;
		private String tag;
		
		/**
		 * @param message
		 */
		public CoolLoaderTask(String message) { 
			this.message = message;
		}
		
		/**
		 * @param coolsvc
		 */
		public void setCoolSvc(CoolMigrationService coolsvc) {
			this.coolsvc = coolsvc;
		}
		

		/**
		 * @param schema
		 */
		public void setSchema(String schema) {
			this.schema = schema;
		}


		/**
		 * @param db
		 */
		public void setDb(String db) {
			this.db = db;
		}

		/**
		 * @param node
		 */
		public void setNode(String node) {
			this.node = node;
		}

		/**
		 * @param tag
		 */
		public void setTag(String tag) {
			this.tag = tag;
		}


		public void run() { 
			System.out.println("Running task:"+message);
			if (message.equals("uploadGtagsTags")) {
				List<GtagTagType> coolgtags;
				try {
					coolgtags = coolsvc.getGlobalTagVsTag(schema,
							db, tag);
					System.out.println("Found list of "+coolgtags.size()+" gtags and tags !");
					coolsvc.migrateGlobalTagAndTags(coolgtags);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			} else if (message.equals("uploadIovs")) {
				try {
					coolsvc.migrateCoolIovs(tag);
				} catch (Exception e) {
					e.printStackTrace();
				}				
			}
		} 
	}

}
