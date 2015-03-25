package conddb.dao.svc;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import conddb.annotations.ProfileExecution;
import conddb.dao.repositories.GlobalTagRepository;
import conddb.dao.repositories.IovRepository;
import conddb.dao.repositories.PayloadRepository;
import conddb.dao.repositories.TagRepository;
import conddb.data.GlobalTag;
import conddb.data.Iov;
import conddb.data.Payload;
import conddb.data.Tag;

public class ConddbClientService {

	private Logger log = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private GlobalTagRepository gtagRepository;
	@Autowired
	private TagRepository tagRepository;
	@Autowired
	private IovRepository iovRepository;
	@Autowired
	private PayloadRepository payloadRepository;

	@ProfileExecution
	public GlobalTag getGlobalTagTrace(String gtagname) throws Exception {
		return gtagRepository.findByNameAndFetchTagsEagerly(gtagname);
	}

	@ProfileExecution
	public Tag getTagIovs(String tagname) throws Exception {
		return tagRepository.findByNameAndFetchIovsEagerly(tagname);
	}

	@ProfileExecution
	public List<Tag> getTagLike(String tagname) throws Exception {
		return tagRepository.findByNameLike(tagname);
	}

	public List<Iov> getIovsForTag(String tagname) throws Exception {
		List<Iov> iovlist = iovRepository.findByTagName(tagname);
		for (Iov iov : iovlist) {
			log.info("Loaded iov " + iov.getSince() + " with payload "
					+ iov.getPayload().toString());
		}
		return iovlist;
	}

	public List<Iov> getIovsForTagFetchPayload(String tagname) throws Exception {
		return iovRepository.findByTagNameAndFetchPayloadEagerly(tagname);
	}

	public GlobalTag getGlobalTag(String gtagname) throws Exception {
		return gtagRepository.findOne(gtagname);
	}

	public List<GlobalTag> getGlobalTagLike(String gtagname) throws Exception {
		return gtagRepository.findByNameLike(gtagname);
	}

	public Tag getTag(String tagname) throws Exception {
		return tagRepository.findByName(tagname);
	}

	public Payload getPayload(String hash) throws Exception {
		return payloadRepository.findByHash(hash);
	}

	public List<Payload> getPayloadSizeGt(Integer size) throws Exception {
		return payloadRepository.findByDatasizeGreaterThan(size);
	}
}
