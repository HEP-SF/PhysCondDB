/**
 * 
 */
package conddb.svc.calibration.tools;

import java.io.File;
import java.io.FileInputStream;
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

	
	protected String getFilenameExtension(Tag tag) throws ConddbServiceException {
		// TODO: make this code more general ? what constraints are needed on names ?
		// Object type should be a string identifying the BLOB. 
		// In the case of calibration files, this is a file name. Not sure this works outside the
		// calibration files use case !!!
		String tagNameRoot = tag.getName().split(Tag.DEFAULT_TAG_EXTENSION)[0];
//		String filename = tag.getObjectType();
//		filename = filename.substring(0, filename.lastIndexOf("."));
//		log.debug("Extracted file name and extension..." + filename);
		SystemDescription system = systemNodeService.getSystemNodesByTagname(tagNameRoot);
		log.debug("Extracted system information..." + system.getNodeFullpath());
		String nodefullpath = system.getNodeFullpath().substring(1);
		return nodefullpath;
	}
	
	protected void dumpIovsInTag(Tag tag, Timestamp snapshot, Path tagrealpath,Path rootdir) {
		OutputStream out = null;
		try {
			List<Iov> iovlist = iovService.getIovsByTag(tag, null, snapshot);
			log.debug("Retrieved list of iovs for tag " + tag.getName()+" of size "+iovlist.size());
			log.debug("Use tagrealpath directory : "+tagrealpath);

			String fileext = tag.getObjectType();
			fileext = fileext.substring(fileext.lastIndexOf(".") + 1, fileext.length());
			log.debug("Extracted filename extension...used with the iov string : "+fileext);
			String node = getFilenameExtension(tag); // this should not have a slash at the beginning
			Path nodepath = Paths.get(node);
			log.debug("Use nodepath directory : "+nodepath);

			log.debug("Create tag resource path as directory : "+tagrealpath.resolve(nodepath));
			Path filepath = tagrealpath.resolve(nodepath);
			Path tagresource = Files.createDirectories(rootdir.resolve(filepath));
			for (Iov iov : iovlist) {
				PayloadData data = iovService.getPayloadData(iov.getPayload().getHash());
				Payload info = iov.getPayload();
				String generatedFileName = iov.getSinceString() + "." + fileext;
				
				// It was using this one, but now I am extracting the info
				// on the filename : info.getStreamerInfo();
				
				String outfilename = tagresource.toRealPath().toString() + PATH_SEPARATOR + generatedFileName;
				log.debug("Dump blob for tag " + tag.getName() + " into output file " + outfilename);
				
				// Check if the blob is on disk
				java.nio.file.Path path = Paths.get(data.getUri());
				out = new FileOutputStream(new File(outfilename));
				if (Files.notExists(path)) {
					log.debug("Blob is stored in memory as string....dump it directly on output");
					payloadBytesHandler.saveToOutStream(data.getData().getBinaryStream(), out); 
				} else {
					log.debug("Blob stored in " + data.getUri());
					Files.copy(path, out);
				}
				log.debug("File has been copied from " + path.toString() + " into " + outfilename);
			}
		} catch (ConddbServiceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
				try {
					if (out != null)
						out.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}
	
	}
	
	public void dumpAsgGlobalTagOnDisk(GlobalTag globaltag, String schema) {
		try {
			String schemadir = "";
			if (schema != null) {
				schemadir = schema;
			}
			GlobalTag entity = globalTagService.getGlobalTagFetchTags(globaltag.getName());

			// Define a resource pointing to root dir only, plus schema dir if it exists
			Resource resource = new FileSystemResource(localrootdir);
			Path rootdir = Paths.get(resource.getFile().getPath());

			// Now you should define a path for the global tag and the package name
			String mainpkg = schemadir+PATH_SEPARATOR+globaltag.getName();
			Path pmainpkg = Paths.get(mainpkg);
			log.debug("Created path for main package: "+pmainpkg);

			Timestamp snapshot = entity.getSnapshotTime();
			Set<GlobalTagMap> tagmaplist = entity.getGlobalTagMaps();
			for (GlobalTagMap globalTagMap : tagmaplist) {
				log.debug("Analyse directory structure for a given TAG");
				Tag tag = globalTagMap.getSystemTag();
				log.info("Found tag " + tag.getName());
				// Tag has been obtained, now try to get the package name from the record.
				// In the method tagFile we have been using schema name for record, and tagnameroot for label.
				// Instead in the method collect we have the original global tag name as a label.
				String schemaname = globalTagMap.getRecord();
				String labelisgtag = globalTagMap.getLabel();
				log.debug("Check record and label in mapping..."+schemaname+" , "+labelisgtag);
				//
				// If labelisgtag is equal to tagnameroot it means that we are dumping a normal tag
				// associated to the input global tag
				String tagNameRoot = tag.getName().split(Tag.DEFAULT_TAG_EXTENSION)[0];
				if (labelisgtag.equals(tagNameRoot) || labelisgtag.equals("none")) {
					// dump this tag under the rootdir/schemadir/globaltag/xxx 
					log.debug("This seems to be a normal global tag, try to dump "+tag.getName());
					String subpkg = tag.getName();
					Path psubpkg = pmainpkg.resolve(subpkg); //schemadir/globaltag/tagname
					log.debug("created path "+psubpkg.toString());
					boolean fileexists = Files.exists(rootdir.resolve(psubpkg));
					log.debug("Check existence of "+rootdir.resolve(psubpkg).toString()+" result in "+fileexists);
					if (!fileexists) {
						// dump the tag content in Path psubpkg
						log.debug("file does not exists, dump the iovs using snapshot: "+snapshot);
						Path tagresource = Files.createDirectories(rootdir.resolve(psubpkg));
						log.debug("created tag resource: "+tagresource);
						this.dumpIovsInTag(tag, snapshot, psubpkg, rootdir);
					}
					log.debug("end of actions for tag "+tag.getName());
				} else {
				// If labelisgtag is the global tag original name, it means we are dumping a tag associated
				// to an ASG global tag in input. In this case either the directory 
				// rootdir/schemadir/globaltag is already there (then we can link it) or we have to dump it and link it
					log.debug("This seems to be an ASG global tag, try to dump "+tag.getName()+" using link if needed");
					if (schemaname.equals("none")) {
						schemaname = labelisgtag.split("-")[0]; // the labelisgtag should have format: package-xx-yy
					}
					boolean gtagexists = Files.exists(rootdir.resolve(mainpkg));
					if (!gtagexists) {
						Path globaltagresource = Files.createDirectories(rootdir.resolve(mainpkg));
						log.debug("Created path for main package: "+globaltagresource);
					}
					String subpkg = schemaname+PATH_SEPARATOR+labelisgtag;
					Path psubpkg = Paths.get(subpkg);
					Path linkedgtag = pmainpkg.resolve(Paths.get(labelisgtag));

					log.debug("Created path for sub package: "+psubpkg);
					log.debug("Link to this should be: "+linkedgtag);

					Path  pmainpkg_relativize_subpkg = pmainpkg.relativize(psubpkg);
					log.debug("Created relative path for sub package: "+pmainpkg_relativize_subpkg);
					boolean fileexists = Files.exists(rootdir.resolve(psubpkg));
					log.debug("Check existence of "+rootdir.resolve(psubpkg)+" result in "+fileexists);
					if (!fileexists) {
						log.debug("file does not exists, dump the global tag and link it");
						GlobalTag subgtag = globalTagService.getGlobalTag(labelisgtag);
						log.debug(" >>>>> global tag to dump: "+subgtag.getName());
						this.dumpAsgGlobalTagOnDisk(subgtag, schemaname);
					}
					boolean linkexists = Files.exists(rootdir.resolve(linkedgtag));
					if (!linkexists) {
						Path asg_subpkg_link = Files.createSymbolicLink(rootdir.resolve(linkedgtag), pmainpkg_relativize_subpkg);
						log.debug("asg_subpkg_link is " + asg_subpkg_link.toString());
					}
					log.debug("ASG: end of actions for tag "+tag.getName());
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public File createTar(GlobalTag globaltag, String schema) {
		try {
			String schemadir = "";
			if (schema != null) {
				schemadir = schema;
			}
			GlobalTag entity = globalTagService.getGlobalTag(globaltag.getName());

			// Define a resource pointing to root dir only, plus schema dir if it exists
			Resource resource = new FileSystemResource(localrootdir);
			Path rootdir = Paths.get(resource.getFile().getPath());

			// Now you should define a path for the global tag and the package name
			String mainpkg = schemadir+PATH_SEPARATOR+globaltag.getName();
			Path pmainpkg = Paths.get(mainpkg);
			log.debug("Created path for main package: "+pmainpkg);

			boolean gtagexists = Files.exists(rootdir.resolve(pmainpkg));
			if (!gtagexists) {
				this.dumpAsgGlobalTagOnDisk(entity, schemadir);
			}
			// Now you can create the tar file
			Resource tarresource = new FileSystemResource(rootdir.resolve(mainpkg).toRealPath().toString());
			log.info("create a tar file from global tag " + globaltag.getName());
			if (!tarresource.exists()) {
				log.error("Cannot create a tar, directory does not yet exists");
			}
			List<File> filelist = new ArrayList<File>();
			filelist.add(tarresource.getFile());
			String tardir = "tar-files";
			Path ptar = Paths.get(tardir);
			boolean tardirexists = Files.exists(rootdir.resolve(ptar));
			if (!tardirexists) {
				Path tardirresource = Files.createDirectories(rootdir.resolve(ptar));
				log.debug("Created path for tar directory: "+tardirresource);				
			}
			String tarname = globaltag.getName() + ".tar";
			this.createTar(tarname, rootdir, ptar, filelist);
			File tarfile = new File(tarname);
			return tarfile;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ConddbServiceException e) {
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

	public void createTar(final String tarName, Path rootdir, Path tardir, final List<File> pathEntries) throws IOException {

		String tarfilepath = tardir.toString() + PATH_SEPARATOR + tarName;
		Path ptar = rootdir.resolve(tarfilepath);
		log.debug("Create tar file in "+ptar);
		OutputStream tarOutput = new FileOutputStream(new File(ptar.toString()));

		ArchiveOutputStream tarArchive = new TarArchiveOutputStream(tarOutput);
		((TarArchiveOutputStream) tarArchive).setLongFileMode( TarArchiveOutputStream.LONGFILE_GNU );
		List<File> files = new ArrayList<File>();
		for (File file : pathEntries) {
			files.addAll(this.recurseDirectory(file));
		}
		log.debug("Now loop into file list of size "+files.size());

		for (File file : files) {
			java.nio.file.Path path = Paths.get(file.getPath());
			java.nio.file.Path relativepath = rootdir.relativize(path);
			log.info("Created relative file path " + relativepath.toString());
			TarArchiveEntry tarArchiveEntry = new TarArchiveEntry(file, relativepath.toString());
			tarArchive.putArchiveEntry(tarArchiveEntry);
			FileInputStream fileInputStream = new FileInputStream(file);
			IOUtils.copy(fileInputStream, tarArchive);
			fileInputStream.close();
			tarArchive.closeArchiveEntry();
			//			tarArchiveEntry.setSize(file.length());

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
