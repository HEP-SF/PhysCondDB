/**
 * 
 */
package conddb.svc.calibration.tools;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.commons.compress.archivers.ArchiveOutputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.compress.utils.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import conddb.data.GlobalTag;
import conddb.data.GlobalTagMap;
import conddb.data.Iov;
import conddb.data.Payload;
import conddb.data.PayloadData;
import conddb.data.SystemDescription;
import conddb.data.Tag;
import conddb.svc.dao.controllers.GlobalTagService;
import conddb.svc.dao.controllers.IovService;
import conddb.svc.dao.controllers.SystemNodeService;
import conddb.svc.dao.exceptions.ConddbServiceException;
import conddb.utils.bytes.PayloadBytesHandler;

/**
 * @author aformic
 *
 */
@Service
public class DirectoryMapperService {

	private Logger log = LoggerFactory.getLogger(this.getClass());

	private String localrootdir = "/tmp";

	@Autowired
	private GlobalTagService globalTagService;
	@Autowired
	private SystemNodeService systemNodeService;
	@Autowired
	private IovService iovService;
	@Autowired
	private PayloadBytesHandler payloadBytesHandler;

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
			log.info("create directory for global tag " + globaltag.getName());
			if (!resource.exists()) {
				log.info("Creating directory " + resource.getFile().getPath());
				File gfile = resource.getFile();
				gfile.mkdirs();
				// resource = new FileSystemResource(localrootdir + "/" +
				// globaltag.getName());
			}
			Timestamp snapshot = entity.getSnapshotTime();
			Set<GlobalTagMap> tagmaplist = entity.getGlobalTagMaps();
			for (GlobalTagMap globalTagMap : tagmaplist) {
				Tag tag = globalTagMap.getSystemTag();
				log.info("create directory for tag " + tag.getName());
				String tagNameRoot = tag.getName().split("-HEAD")[0];
				String filename = tag.getObjectType();
				SystemDescription system = systemNodeService.getSystemNodesByTagname(tagNameRoot);
				String nodefullpath = system.getNodeFullpath();
				Resource tagresource = new FileSystemResource(resource.getFile().getPath() + nodefullpath);
				if (!tagresource.exists()) {
					log.info("Creating directory " + tagresource.getFile().getPath());
					File tfile = tagresource.getFile();
					tfile.mkdirs();
				}
				List<Iov> iovlist = iovService.getIovsByTag(tag, null, snapshot);
				for (Iov iov : iovlist) {
					PayloadData data = iovService.getPayloadData(iov.getPayload().getHash());
					Payload info = iov.getPayload();
					String generatedFileName = iov.getSinceString() + "." + info.getStreamerInfo();
					String outfilename = tagresource.getFile().getPath() + "/" + generatedFileName;
					log.debug("Dump blob for tag " + tag.getName() + " into output file " + outfilename);
					log.debug("Blob stored in " + data.getUri());
					java.nio.file.Path path = Paths.get(data.getUri());
					OutputStream out = new FileOutputStream(new File(outfilename));
					Files.copy(path, out);
					log.debug("File has been copied from " + path.toString() + " into " + outfilename);
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

	public File createTar(GlobalTag globaltag) {
		try {
			Resource resource = new FileSystemResource(localrootdir + "/" + globaltag.getName());
			log.info("create a tar file from global tag " + globaltag.getName());
			if (!resource.exists()) {
				log.error("Cannot create a tar, directory does not yet exists");
			}
			List<File> filelist = new ArrayList<File>();
			filelist.add(resource.getFile());
			String tarname = localrootdir+"/"+globaltag.getName()+".tar";
			this.createTar(tarname, filelist);
			File tarfile = new File(tarname);
			return tarfile;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;

	}
	
	protected List<File> recurseDirectory(final File directory) {
	    List<File> files = new ArrayList<File>();
	    if (directory != null && directory.isDirectory()) {
	        for (File file : directory.listFiles()) {
	            if (file.isDirectory()) {
	                files.addAll(recurseDirectory(file));
	            } else {
	                files.add(file);
	            }
	        }
	    }
	    return files;
	}
	
	public void createTar(final String tarName, final List<File> pathEntries) throws IOException {
	    OutputStream tarOutput = new FileOutputStream(new File(tarName));

	    ArchiveOutputStream tarArchive = new TarArchiveOutputStream(tarOutput);
	    List<File> files = new ArrayList<File>();
	    for (File file : pathEntries) {
	        files.addAll(this.recurseDirectory(file));
	    }
	    for (File file : files) {
	        TarArchiveEntry tarArchiveEntry = new TarArchiveEntry(file, file.getPath());
	        tarArchiveEntry.setSize(file.length());
	        tarArchive.putArchiveEntry(tarArchiveEntry);
	        FileInputStream fileInputStream = new FileInputStream(file);
	        IOUtils.copy(fileInputStream, tarArchive);
	        fileInputStream.close();
	        tarArchive.closeArchiveEntry();
	    }
	    tarArchive.finish();
	    tarOutput.close();
	}
}
