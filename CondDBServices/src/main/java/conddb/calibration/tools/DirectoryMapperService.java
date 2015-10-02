/**
 * 
 */
package conddb.calibration.tools;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import conddb.dao.controllers.GlobalTagService;
import conddb.dao.controllers.IovService;
import conddb.dao.exceptions.ConddbServiceException;
import conddb.data.GlobalTag;
import conddb.data.GlobalTagMap;
import conddb.data.Tag;

/**
 * @author aformic
 *
 */
@Service
public class DirectoryMapperService {

	private Logger log = LoggerFactory.getLogger(this.getClass());

	private String localrootdir="/tmp";

	@Autowired
	private GlobalTagService globalTagService;
	@Autowired
	private IovService iovService;
	
	
	public DirectoryMapperService() {
		super();
	}

	public DirectoryMapperService(String localrootdir) {
		super();
		this.localrootdir = localrootdir;
	}
	
	public void dumpBlobOnDisk(byte[] data, String path, String outputfile) {
		return;
	}
	
	public void dumpGlobalTagOnDisk(GlobalTag globaltag) {
		try {
			GlobalTag entity = globalTagService.getGlobalTagFetchTags(globaltag.getName());
			Resource resource = new FileSystemResource(localrootdir + "/" + globaltag.getName());
			log.info("create directory for global tag "+globaltag.getName());
			if (!resource.exists()) {
				log.info("Creating directory "+resource.getFile().getPath());
				File gfile = resource.getFile();
				gfile.mkdirs();
				//resource = new FileSystemResource(localrootdir + "/" + globaltag.getName());
			}
			Set<GlobalTagMap> tagmaplist = entity.getGlobalTagMaps();
			for (GlobalTagMap globalTagMap : tagmaplist) {
				Tag tag = globalTagMap.getSystemTag();
				log.info("create directory for tag "+tag.getName());
				Resource tagresource = new FileSystemResource(resource.getFile().getPath() + "/" + tag.getName());
				if (!tagresource.exists()) {
					log.info("Creating directory "+tagresource.getFile().getPath());
					File tfile = tagresource.getFile();
					tfile.mkdirs();	
				}
				
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ConddbServiceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}

}
