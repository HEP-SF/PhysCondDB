package conddb.dao.svc;

import java.util.List;

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

	@Autowired
	private GlobalTagRepository gtagRepository;
	@Autowired
	private TagRepository tagRepository;
	@Autowired
	private IovRepository iovRepository;
	@Autowired
	private PayloadRepository payloadRepository;
	
	@ProfileExecution
	public GlobalTag getGlobalTagTrace(String gtagname) {
		return gtagRepository.findByNameAndFetchTagsEagerly(gtagname);
	}
	@ProfileExecution
	public Tag getTagIovs(String tagname) {
		return tagRepository.findByNameAndFetchIovsEagerly(tagname);
	}
	@ProfileExecution
	public List<Tag> getTagLike(String tagname) {
		return tagRepository.findByNameLike(tagname);
	}
	public List<Iov> getIovsForTag(String tagname) {
		return iovRepository.findByTagName(tagname);
	}
	public List<Iov> getIovsForTagFetchPayload(String tagname) {
		return iovRepository.findByTagNameAndFetchPayloadEagerly(tagname);
	}
	public GlobalTag getGlobalTag(String gtagname) {
		return gtagRepository.findOne(gtagname);
	}
	public List<GlobalTag> getGlobalTagLike(String gtagname) {
		return gtagRepository.findByNameLike(gtagname);
	}
	public Tag getTag(String tagname) {
		return tagRepository.findByName(tagname);
	}
	public Payload getPayload(String hash) {
		return payloadRepository.findByHash(hash);
	}
	public List<Payload> getPayloadSizeGt(Integer size) {
		return payloadRepository.findByDatasizeGreaterThan(size);
	}
}
