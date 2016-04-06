/**
 * 
 */
package conddb.cool.migutils.svc;

import java.io.StringReader;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.xml.sax.InputSource;

import conddb.cool.dao.JdbcCondDBRepository;
import conddb.cool.data.CoolIovType;
import conddb.cool.data.GtagTagType;
import conddb.cool.data.NodeType;
import conddb.cool.migutils.CoolIov;
import conddb.data.GlobalTag;
import conddb.data.GlobalTagMap;
import conddb.data.Iov;
import conddb.data.Payload;
import conddb.data.PayloadData;
import conddb.data.SystemDescription;
import conddb.data.Tag;
import conddb.data.utils.PayloadGenerator;
import conddb.svc.dao.baserepository.PayloadDataBaseCustom;
import conddb.svc.dao.repositories.GlobalTagMapRepository;
import conddb.svc.dao.repositories.GlobalTagRepository;
import conddb.svc.dao.repositories.IovRepository;
import conddb.svc.dao.repositories.PayloadRepository;
import conddb.svc.dao.repositories.SystemNodeRepository;
import conddb.svc.dao.repositories.TagRepository;

/**
 * @author formica
 *
 */
public class CoolMigrationService {

	@Autowired
	private JdbcCondDBRepository coolRepository;
	@Autowired
	private GlobalTagRepository gtagRepository;
	@Autowired
	private GlobalTagMapRepository mapRepository;
	@Autowired
	private TagRepository tagRepository;
	@Autowired
	private IovRepository iovRepository;
	@Autowired
	private PayloadRepository pyldRepository;
	@Autowired
	@Qualifier("payloaddatadbrepo")
	private PayloadDataBaseCustom payloadDataBaseCustom;

	@Autowired
	private SystemNodeRepository sdRepository;

	private Logger log = LoggerFactory.getLogger(getClass());

	private SimpleDateFormat df = new SimpleDateFormat("yyyy-dd-MM_hh:mm:ss");

	public CoolMigrationService(JdbcCondDBRepository simpleRepository) {
		this.coolRepository = simpleRepository;
	}

	public List<GtagTagType> getGlobalTagVsTag(String schema, String instance,
			String gtagpattern) throws Exception {
		return coolRepository.getTagsAssociationsFromCool(schema, instance,
				gtagpattern);
	}

	public void migrateGlobalTagAndTags(List<GtagTagType> coolgtags)
			throws ParseException {
		List<GlobalTag> conddbgtags = new ArrayList<GlobalTag>();
		List<Tag> conddbtags = new ArrayList<Tag>();
		for (GtagTagType cooltag : coolgtags) {
			String description = (cooltag.getGtagDescription() != null) ? cooltag
					.getGtagDescription() : "none";
			Date instimestr = df.parse(cooltag.getSysInstime());
			String iovtype = "unknown";
			String xml = ("<mynode>" + cooltag.getNodeDescription() + "</mynode>");
			iovtype = getiovTypeFromNodeDescription(xml);

			// create global tag
			GlobalTag conddbgtag = new GlobalTag(cooltag.getGtagName(),
					new BigDecimal(0), description, "1.0", 
					new java.sql.Timestamp(new Date().getTime()));
			
			// set tagdescription
			String tagdescription = (cooltag.getTagDescription() != null) ? cooltag
					.getTagDescription() : "none";

			// check existence of tag description object via the nodefullpath
			SystemDescription chksd = sdRepository.findByNodeFullpath(cooltag
					.getNodeFullpath());
			if (chksd == null) {
				// create tag description object
				SystemDescription sd = new SystemDescription();
				sd.setNodeDescription(cooltag.getNodeDescription());
				sd.setSchemaName(cooltag.getSchemaName());
				sd.setNodeFullpath(cooltag.getNodeFullpath());
				String[] tagnamearr = cooltag.getTagName().split("-");
				String tagNameRoot = (tagnamearr != null && tagnamearr.length > 0) ? tagnamearr[0]
						: cooltag.getTagName();
				sd.setTagNameRoot(tagNameRoot);
				try {
					sdRepository.save(sd);
				} catch (Exception e) {
					log.error("Error in saving system description "
							+ e.getMessage());
				}
			}
			
			// Create tag object for storage
			Tag conddbtag = new Tag(cooltag.getTagName(), iovtype, "MY_OBJECT",
					"SYNCHRO -" + cooltag.getNodeFullpath(), tagdescription,
					new BigDecimal(0), new BigDecimal(0));
			// Check if tag name already exists
			Tag dbtag = tagRepository.findByName(cooltag.getTagName());
			if (dbtag != null) {
				conddbtag = dbtag;
			}
			
			// Check if global tag already exists
			GlobalTag gtsaved = gtagRepository.findByName(conddbgtag.getName());
			if (gtsaved != null) {
				continue;
			}
			
			// Register entries in PhysCondDb
			gtagRepository.save(conddbgtag);
			tagRepository.save(conddbtag);

			// create map entry
			GlobalTagMap mappedtag = new GlobalTagMap(conddbgtag, conddbtag);
			// Register mapping entry
			mapRepository.save(mappedtag);

			conddbgtags.add(conddbgtag);
			conddbtags.add(conddbtag);
		}
		log.debug("Saved list of Gtags of size "+conddbgtags.size());
		log.debug("Saved list of Tags of size "+conddbtags.size());
	}

	public void migrateCoolIovs(String tagpattern) throws Exception {
		Iterable<Tag> taglist = tagRepository.findByNameLike(tagpattern);
		Map<String,Object> defaultmap = PayloadGenerator.createDefaultPayload();
		Payload defaultpyld = (Payload) defaultmap.get("payload");
		PayloadData defaultpylddata = (PayloadData) defaultmap.get("payloaddata");
		
		for (Tag atag : taglist) {
			log.info("Search for node description related information using "+atag
					.getSynchronization());
			SystemDescription sd = sdRepository.findByNodeFullpath(atag
					.getSynchronization().split("-")[1]);

			log.info("Copy iovs from " + sd.getSchemaName() + " "
					+ sd.getNodeFullpath() + " " + atag.getName());
			List<CoolIovType> cooliovs = coolRepository.getIovRangeFromCool(sd
					.getSchemaName(), "CONDBR2", sd.getNodeFullpath(), atag
					.getName(), "%", new BigDecimal(0), new BigDecimal(
					CoolIov.COOL_MAX_DATE));
			// 
			Timestamp now = new Timestamp(new Date().getTime());
			for (CoolIovType ciov : cooliovs) {
				// Use default empty payload for the moment...
				// TODO modify this code to use real data
				String sincestring = ciov.getSinceCoolStr();
				Payload pyld = pyldRepository.findOne(defaultpyld.getHash());
				if (pyld == null) {
					pyldRepository.save(defaultpyld);
					payloadDataBaseCustom.save(defaultpylddata);
					pyld = pyldRepository.findOne(defaultpyld.getHash());
				}
				Iov conddbiov = new Iov(ciov.getIovSince(), sincestring,
						pyld, atag);
				Iov storediov = iovRepository
						.fetchBySinceAndInsertionTimeAndTagName(atag.getName(),
								ciov.getIovSince(), now);
				if (storediov != null) {
					log.info("IOV entry already exists...is probably another channel from COOL");
				} else {
					iovRepository.save(conddbiov);
				}
			}
		}
		return;
	}

	public List<CoolIovType> getCoolIovs(String schema, String db, String node,
			String tag, String channel, BigDecimal since, BigDecimal until)
			throws Exception {
		List<CoolIovType> cooliovs = null;
		// Check node (retrieve the COOL node element to extract the iov-type)
		List<NodeType> nodelist = coolRepository.getNodesFromCool(schema, db,
				node);
		if (nodelist == null)
			throw new Exception("Cannot search for iovs in node " + node);
		if (nodelist.size() > 1) {
			throw new Exception("Cannot search for iovs in many nodes "
					+ nodelist.size());
		}
		NodeType nt = nodelist.get(0);
		String iovbase = nt.getNodeIovBase();
		log.info("Analyse node " + nt.getNodeFullpath() + " of type " + iovbase);
		cooliovs = coolRepository.getIovRangeFromCool(schema, db, node, tag,
				channel, since, until);
		return cooliovs;
	}

	public List<NodeType> getCoolNodes(String schema, String db, String node)
			throws Exception {
		List<NodeType> coolnodes = null;
		coolnodes = coolRepository.getNodesFromCool(schema, db, node);
		return coolnodes;
	}

	protected String getiovTypeFromNodeDescription(String xml) {
		XPathFactory xpathFactory = XPathFactory.newInstance();
		XPath xpath = xpathFactory.newXPath();

		InputSource source = new InputSource(new StringReader(xml));

		String iovtype = "";
		try {
			iovtype = (String) xpath.evaluate("/mynode/timeStamp", source,
					XPathConstants.STRING);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return iovtype;
	}
}
