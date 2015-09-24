package conddb.dao.svc;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import conddb.annotations.ProfileExecution;
import conddb.dao.baserepository.PayloadDataBaseCustom;
import conddb.dao.exceptions.ConddbServiceException;
import conddb.dao.repositories.GlobalTagRepository;
import conddb.dao.repositories.IovRepository;
import conddb.dao.repositories.PayloadRepository;
import conddb.dao.repositories.TagRepository;
import conddb.data.GlobalTag;
import conddb.data.Iov;
import conddb.data.Payload;
import conddb.data.PayloadData;
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
	@Autowired
	@Qualifier("payloaddatajcrrepo")
	private PayloadDataBaseCustom payloaddataRepository;

	public List<GlobalTag> getGlobalTagLike(String gtagname) throws Exception {
		return gtagRepository.findByNameLike(gtagname);
	}
	
	// Global tag related methods
	@ProfileExecution
	public List<GlobalTag> getGlobalTagOne(String gtagname) throws Exception {
		List<GlobalTag> list = new ArrayList<GlobalTag>();
		GlobalTag gtag = gtagRepository.findOne(gtagname);
		list.add(gtag);
		return list;
	}

	public List<GlobalTag> getGlobalTagTrace(String gtagname) throws Exception {
		List<GlobalTag> list = new ArrayList<GlobalTag>();
		GlobalTag gtag = gtagRepository.findByNameAndFetchTagsEagerly(gtagname);
		list.add(gtag);
		return list;
	}

	// Iovs related methods
	public List<Iov> getIovsForTag(String tagname) throws Exception {
		Page<Iov> iovlist = iovRepository.findByTagName(tagname, new PageRequest(0,10000));
		return iovlist.getContent();
	}

	public List<Iov> getIovsForTagFetchPayload(String tagname) throws Exception {
		return iovRepository.findByTagNameAndFetchPayloadEagerly(tagname);
	}

	// Payload related methods
	public Payload getPayload(String hash) throws Exception {
		try {
			Payload pyld = payloadRepository.findByHash(hash);
			if (pyld == null) {
				throw new ConddbServiceException("Cannot find payload for hash "+hash);
			}
			return pyld;
		} catch (Exception e) {
			throw e;
		}
	}

	public PayloadData getPayloadData(String hash) throws Exception {
		try {
			PayloadData pyld = payloaddataRepository.find(hash);
			if (pyld == null) {
				throw new ConddbServiceException("Cannot find payload data for hash "+hash);
			}
			return pyld;
		} catch (Exception e) {
			throw e;
		}
	}

	public List<Payload> getPayloadSizeGt(Integer size) throws Exception {
		return payloadRepository.findByDatasizeGreaterThan(size);
	}

	// Tag related methods
	public List<Tag> getTagOne(String tagname) throws Exception {
		List<Tag> list = new ArrayList<Tag>();
		Tag atag = tagRepository.findByName(tagname);
		list.add(atag);
		return list;
	}
	@ProfileExecution
	public List<Tag> getTagIovs(String tagname) throws Exception {
		List<Tag> list = new ArrayList<Tag>();
		Tag atag = tagRepository.findByNameAndFetchIovsEagerly(tagname);
		list.add(atag);
		return list;
	}

	@ProfileExecution
	public List<Tag> getTagLike(String tagname) throws Exception {
		return tagRepository.findByNameLike(tagname);
	}
	
	@ProfileExecution
	public List<Tag> getTagBackTrace(String tagname) throws Exception {
		List<Tag> list = new ArrayList<Tag>();
		Tag atag = tagRepository.findByNameAndFetchGlobalTagsEagerly(tagname);
		list.add(atag);
		return list;
	}

}
