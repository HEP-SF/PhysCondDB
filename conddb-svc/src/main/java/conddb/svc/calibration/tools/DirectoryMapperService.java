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
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
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
import conddb.data.utils.bytes.PayloadBytesHandler;
import conddb.svc.dao.controllers.GlobalTagService;
import conddb.svc.dao.controllers.IovService;
import conddb.svc.dao.controllers.SystemNodeService;
import conddb.svc.dao.exceptions.ConddbServiceException;

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


	public static final String PATH_SEPARATOR = "/";

	public DirectoryMapperService() {
		super();
	}

	public DirectoryMapperService(String localrootdir) {
		super();
		this.localrootdir = localrootdir;
	}

	public File createFileFromBlob(Payload payload) {
		if (payload.getData() == null) {
			return null;
		}
		PayloadData data = payload.getData();
		String generatedFileName = payload.getObjectType();
		try {
			Resource resource = new FileSystemResource(localrootdir + "/");
			log.info("create a file from payload " + payload.getHash());
			if (!resource.exists()) {
				log.error("Cannot create a file, file resource does not yet exists");
			}
			String outfilename = resource.getFile().getPath() + PATH_SEPARATOR + generatedFileName;
			log.debug("Blob stored in " + data.getUri());
			java.nio.file.Path path = Paths.get(data.getUri());
			OutputStream out = new FileOutputStream(new File(outfilename));
			Files.copy(path, out);
			log.debug("File has been copied from " + path.toString() + " into " + outfilename);
			File outfile = new File(outfilename);
			return outfile;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	public void dumpBlobOnDisk(byte[] data, String path, String outputfile) {
		return;
	}

	/**
	 * This method is dumping a generic global tag into disk. This assumes that the global tag maps do contain information related to the system,
	 * at the level of the record and label fields: label indicates in general the tagnameroot, and record the schemaName (or package name in the
	 * case of calibration files). One can assume that if record is "none", the first part of the tag name should be used. This obviously depends
	 * on the tag name format. So it should be carefully checked via regexp ?
	 * @param globaltag
	 * @param schema
	 */
	public void dumpGlobalTagOnDisk(GlobalTag globaltag, String schema) {
		try {
			String schemadir = "";
			if (schema != null) {
				schemadir = schema + PATH_SEPARATOR;
			}
			GlobalTag entity = globalTagService.getGlobalTagFetchTags(globaltag.getName());
			Resource resource = new FileSystemResource(localrootdir + PATH_SEPARATOR + schemadir + globaltag.getName());
			log.info("create directory for global tag " + globaltag.getName());
			if (!resource.exists()) {
				log.info("Creating directory " + resource.getFile().getPath());
				File gfile = resource.getFile();
				gfile.mkdirs();
			}
			Timestamp snapshot = entity.getSnapshotTime();
			Set<GlobalTagMap> tagmaplist = entity.getGlobalTagMaps();
			for (GlobalTagMap globalTagMap : tagmaplist) {
				log.debug("Analyse directory structure for a given TAG");
				Tag tag = globalTagMap.getSystemTag();
				log.info("Found tag " + tag.getName());
				String tagNameRoot = globalTagMap.getLabel();
				if (tagNameRoot.equals("none")) {
					// FIXME: this works only for calibration files....we should check for a regexp ? How to guarantee the format of the tag name ?
					tagNameRoot = tag.getName().split(Tag.DEFAULT_TAG_EXTENSION)[0];
				}
				// Object type should be a string identifying the BLOB. In the case of calibration files, this is a file name.
				String filename = tag.getObjectType();
				filename = filename.substring(0, filename.lastIndexOf("."));
				String fileext = tag.getObjectType();
				fileext = fileext.substring(fileext.lastIndexOf(".") + 1, fileext.length());
				log.debug("Extracted file name and extension..." + filename+" "+fileext);
				SystemDescription system = systemNodeService.getSystemNodesByTagname(tagNameRoot);
				log.debug("Found system " + system.getSchemaName());
				// Check if this is a link to a global tag
				String labelisgtag = globalTagMap.getLabel();
				log.debug("Check label in mapping..."+labelisgtag);
				Resource subresource = new FileSystemResource(
						localrootdir + PATH_SEPARATOR + system.getSchemaName() + PATH_SEPARATOR + labelisgtag);
				log.debug("Verify subresource in path "+subresource.toString());
				if (subresource.exists()) {
					log.info("Creating link to " + subresource.getFile().getPath());
					Path newLink = Paths.get(resource.getFile().getPath() + PATH_SEPARATOR + labelisgtag);
					Path target = Paths.get(subresource.getFile().getPath());
					try {
						log.debug("Create symbolic link to "+target.toString());
						Files.createSymbolicLink(newLink, target);
					} catch (IOException x) {
						log.error(x.getMessage());
					} catch (UnsupportedOperationException x) {
						// Some file systems do not support symbolic links.
						log.error(x.getMessage());
					}
					continue;
				}
				String nodefullpath = system.getNodeFullpath();
				Resource tagresource = new FileSystemResource(
						resource.getFile().getPath() + nodefullpath + PATH_SEPARATOR + filename);
				// If the tag resource exists we should create a link...???
				if (!tagresource.exists()) {
					log.info("Creating directory " + tagresource.getFile().getPath());
					File tfile = tagresource.getFile();
					tfile.mkdirs();
				}
				List<Iov> iovlist = iovService.getIovsByTag(tag, null, snapshot);
				for (Iov iov : iovlist) {
					PayloadData data = iovService.getPayloadData(iov.getPayload().getHash());
					Payload info = iov.getPayload();
					String generatedFileName = iov.getSinceString() + "." + fileext;
					
					// It was using this one, but now I am extracting the info
					// on the filename : info.getStreamerInfo();
					String outfilename = tagresource.getFile().getPath() + "/" + generatedFileName;
					log.debug("Dump blob for tag " + tag.getName() + " into output file " + outfilename);
					
					// Check if the blob is on disk
					java.nio.file.Path path = Paths.get(data.getUri());
					OutputStream out = new FileOutputStream(new File(outfilename));
					if (Files.notExists(path)) {
						log.debug("Blob is stored in memory as string....dump it directly on output");
						payloadBytesHandler.saveToOutStream(data.getData().getBinaryStream(), out); 
					} else {
						log.debug("Blob stored in " + data.getUri());
						Files.copy(path, out);
					}
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
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void dumpAsgGlobalTagOnDisk(GlobalTag globaltag, String schema) {
		try {
			String schemadir = "";
			if (schema != null) {
				schemadir = schema + PATH_SEPARATOR;
			}
			GlobalTag entity = globalTagService.getGlobalTagFetchTags(globaltag.getName());
			Resource resource = new FileSystemResource(localrootdir + PATH_SEPARATOR + schemadir + globaltag.getName());
			log.info("create directory for global tag " + globaltag.getName());
			if (!resource.exists()) {
				log.info("Creating directory " + resource.getFile().getPath());
				File gfile = resource.getFile();
				gfile.mkdirs();
			}
			Timestamp snapshot = entity.getSnapshotTime();
			Set<GlobalTagMap> tagmaplist = entity.getGlobalTagMaps();
			for (GlobalTagMap globalTagMap : tagmaplist) {
				log.debug("Analyse directory structure for a given TAG");
				Tag tag = globalTagMap.getSystemTag();
				log.info("Found tag " + tag.getName());
				String tagNameRoot = tag.getName().split(Tag.DEFAULT_TAG_EXTENSION)[0];
				String filename = tag.getObjectType();
				filename = filename.substring(0, filename.lastIndexOf("."));
				String fileext = tag.getObjectType();
				fileext = fileext.substring(fileext.lastIndexOf(".") + 1, fileext.length());
				log.debug("Extracted file name extension..." + fileext);
				SystemDescription system = systemNodeService.getSystemNodesByTagname(tagNameRoot);
				log.debug("Found system " + system.getSchemaName());
				// Check if this is a link to a global tag
				String labelisgtag = globalTagMap.getLabel();
				log.debug("Check label in mapping..."+labelisgtag);
				Resource subresource = new FileSystemResource(
						localrootdir + PATH_SEPARATOR + system.getSchemaName() + PATH_SEPARATOR + labelisgtag);
				log.debug("Verify subresource in path "+subresource.toString());
				if (subresource.exists()) {
					log.info("Creating link to " + subresource.getFile().getPath());
					Path newLink = Paths.get(resource.getFile().getPath() + PATH_SEPARATOR + labelisgtag);
					Path target = Paths.get(subresource.getFile().getPath());
					try {
						log.debug("Create symbolic link to "+target.toString());
						Files.createSymbolicLink(newLink, target);
					} catch (IOException x) {
						log.error(x.getMessage());
					} catch (UnsupportedOperationException x) {
						// Some file systems do not support symbolic links.
						log.error(x.getMessage());
					}
					continue;
				}
				String nodefullpath = system.getNodeFullpath();
				Resource tagresource = new FileSystemResource(
						resource.getFile().getPath() + nodefullpath + PATH_SEPARATOR + filename);
				// If the tag resource exists we should create a link...???
				if (!tagresource.exists()) {
					log.info("Creating directory " + tagresource.getFile().getPath());
					File tfile = tagresource.getFile();
					tfile.mkdirs();
				}
				List<Iov> iovlist = iovService.getIovsByTag(tag, null, snapshot);
				for (Iov iov : iovlist) {
					PayloadData data = iovService.getPayloadData(iov.getPayload().getHash());
					Payload info = iov.getPayload();
					String generatedFileName = iov.getSinceString() + "." + fileext;
					
					// It was using this one, but now I am extracting the info
					// on the filename : info.getStreamerInfo();
					String outfilename = tagresource.getFile().getPath() + "/" + generatedFileName;
					log.debug("Dump blob for tag " + tag.getName() + " into output file " + outfilename);
					
					// Check if the blob is on disk
					java.nio.file.Path path = Paths.get(data.getUri());
					OutputStream out = new FileOutputStream(new File(outfilename));
					if (Files.notExists(path)) {
						log.debug("Blob is stored in memory as string....dump it directly on output");
						payloadBytesHandler.saveToOutStream(data.getData().getBinaryStream(), out); 
					} else {
						log.debug("Blob stored in " + data.getUri());
						Files.copy(path, out);
					}
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
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public File createTar(GlobalTag globaltag, String schema) {
		try {
			String schemadir = "";
			if (schema != null && !schema.equals("")) {
				schemadir = schema + PATH_SEPARATOR;
			}
			Resource resource = new FileSystemResource(localrootdir + PATH_SEPARATOR + schemadir + globaltag.getName());
			log.info("create a tar file from global tag " + globaltag.getName());
			if (!resource.exists()) {
				log.error("Cannot create a tar, directory does not yet exists");
			}
			List<File> filelist = new ArrayList<File>();
			filelist.add(resource.getFile());
			String tarname = localrootdir + "/" + globaltag.getName() + ".tar";
			this.createTar(tarname, schemadir, filelist);
			File tarfile = new File(tarname);
			return tarfile;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	public boolean isOnDisk(GlobalTag globaltag, String schema) {
		Resource resource = new FileSystemResource(localrootdir + "/" + globaltag.getName());
		if (resource.exists()) {
			return true;
		}
		return false;
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

	public void createTar(final String tarName, final String schema, final List<File> pathEntries) throws IOException {
		OutputStream tarOutput = new FileOutputStream(new File(tarName));

		ArchiveOutputStream tarArchive = new TarArchiveOutputStream(tarOutput);
		((TarArchiveOutputStream) tarArchive).setLongFileMode( TarArchiveOutputStream.LONGFILE_GNU );
		List<File> files = new ArrayList<File>();
		for (File file : pathEntries) {
			files.addAll(this.recurseDirectory(file));
		}
		String schemadir = "";
		if (schema != null && !schema.equals("")) {
			schemadir = schema + PATH_SEPARATOR;
		}
		java.nio.file.Path rootpath = Paths.get(localrootdir + PATH_SEPARATOR + schemadir);
		for (File file : files) {
			java.nio.file.Path path = Paths.get(file.getPath());
			java.nio.file.Path relativepath = rootpath.relativize(path);
			log.info("Created relative file path " + relativepath.toString());
			TarArchiveEntry tarArchiveEntry = new TarArchiveEntry(file, relativepath.toString());
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

	public String getLocalrootdir() {
		return localrootdir;
	}

	public void setLocalrootdir(String localrootdir) {
		this.localrootdir = localrootdir;
	}
	
	
}
