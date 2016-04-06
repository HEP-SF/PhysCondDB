/**
 * 
 */
package conddb.cool.migutils.controllers;

import java.math.BigDecimal;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import conddb.cool.data.CoolIovType;
import conddb.cool.data.GtagTagType;
import conddb.cool.migutils.svc.CoolMigrationService;

/**
 * @author formica
 *
 */
@RestController
@RequestMapping(value="/cool")
public class CoolController {

	private Logger log = LoggerFactory.getLogger(getClass());

	@Autowired
	private CoolMigrationService coolsvc;

	@RequestMapping(value = "/tracetags", method = RequestMethod.GET)
	@ResponseBody
	public List<GtagTagType> getCoolGtagTags(
			@RequestParam(value = "name", defaultValue = "ATLAS_COOL%") String schemaName,
			@RequestParam(value = "dbname", defaultValue = "CONDBR2") String instance,
			@RequestParam(value = "gtag", defaultValue = "CONDBR%") String gtagpattern)
			throws Exception {
		log.info(
				"CoolController processing request for getCoolGtagTags: schemaName={} ...",
				schemaName);
		List<GtagTagType> coolgtags = coolsvc.getGlobalTagVsTag(schemaName,
				instance, gtagpattern);
		return coolgtags;
	}

	@RequestMapping(value = "/iovs", method = RequestMethod.GET, params = {
			"name", "dbname", "node", "tag", "channel", "since", "until" })
	@ResponseBody
	public List<CoolIovType> getCoolIovs(
			@RequestParam(value = "name", defaultValue = "ATLAS_COOL") String schemaName,
			@RequestParam(value = "dbname", defaultValue = "CONDBR2") String instance,
			@RequestParam(value = "node", defaultValue = "none") String node,
			@RequestParam(value = "tag", defaultValue = "none") String tag,
			@RequestParam(value = "channel", defaultValue = "%") String channelName,
			@RequestParam(value = "since", defaultValue = "0") String since,
			@RequestParam(value = "until", defaultValue = "9223372036854775807") String until)
			throws Exception {
		log.info("CoolController processing request for getCoolIovs: schemaName="
				+ schemaName
				+ " db="
				+ instance
				+ " node="
				+ node
				+ " tag="
				+ tag
				+ " channel="
				+ channelName
				+ " since="
				+ since
				+ " until=" + until);
		BigDecimal dsince = new BigDecimal(since);
		BigDecimal duntil = new BigDecimal(until);
		log.info("Launch query...");
		List<CoolIovType> cooliovs = coolsvc.getCoolIovs(schemaName, instance,
				node, tag, channelName, dsince, duntil);
		return cooliovs;
	}


	@ExceptionHandler
	ResponseEntity handleExceptions(Exception ex) {
		ResponseEntity responseEntity = null;
		if (ex instanceof Exception) {
			responseEntity = new ResponseEntity(
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
		return responseEntity;
	}
}
