/**
 * 
 */
package conddb.svc.calibration.tools;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.DirectoryIteratorException;
import java.nio.file.DirectoryStream;
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
import org.apache.commons.compress.archivers.tar.TarConstants;
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

	/**
	 * @param globaltagname
	 * @param schema
	 * @return
	 */
	public File createOldTar(String globaltagname, String schema) {
		try {
			String schemadir = "";
			if (schema != null) {
				schemadir = schema;
			}
			GlobalTag entity = globalTagService.getGlobalTag(globaltagname);

			// Define a resource pointing to root dir only, plus schema dir if it exists
			Resource resource = new FileSystemResource(localrootdir);
			Path rootdir = Paths.get(resource.getFile().getPath());

			// Now you should define a path for the global tag and the package name
			String mainpkg = schemadir+PATH_SEPARATOR+globaltagname;
			Path pmainpkg = Paths.get(mainpkg);
			log.debug("Created path for main package: "+pmainpkg);

			boolean gtagexists = Files.exists(rootdir.resolve(pmainpkg));
			if (!gtagexists) {
				this.dumpAsgGlobalTagOnDisk(entity, schemadir);
			}
			// Now you can create the tar file
			Resource tarresource = new FileSystemResource(rootdir.resolve(mainpkg).toRealPath().toString());
			log.info("create a tar file from global tag " + globaltagname);
			if (!tarresource.exists()) {
				log.error("Cannot create a tar, directory does not yet exists");
			}
			List<File> filelist = new ArrayList<File>();
			filelist.add(tarresource.getFile());
			
			List<Path> pathlist = new ArrayList<>();
			DirectoryStream<Path> stream = Files.newDirectoryStream(rootdir.resolve(pmainpkg));
			try {
			    for (Path file: stream) {
			        log.debug("Found file or directory: "+file.getFileName());
			        boolean islink = Files.isSymbolicLink(file);
			        if (islink) {
			        	Path link = Files.readSymbolicLink(file);
				        log.debug("this is a link: "+link);
			        	pathlist.add(link);
			        }
			        pathlist.add(file);
			    }
			} catch (DirectoryIteratorException x) {
			    // IOException can never be thrown by the iteration.
			    // In this snippet, it can only be thrown by newDirectoryStream.
			    log.error("DirectoryStream produced an error: "+x.getMessage());
			}			
			String tardir = "../tar-files";
			Path ptar = Paths.get(tardir);
			boolean tardirexists = Files.exists(rootdir.resolve(ptar));
			if (!tardirexists) {
				Path tardirresource = Files.createDirectories(rootdir.resolve(ptar));
				log.debug("Created path for tar directory: "+tardirresource);				
			}
			String tarname = globaltagname + ".tar";
			this.fillTar(tarname, rootdir, pmainpkg, ptar, pathlist);
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
	
	/**
	 * @param globaltagname
	 * @param schema
	 * @return
	 */
	public File createTar(String globaltagname, String schema) {
		try {
			String schemadir = "";
			if (schema != null) {
				schemadir = schema;
			}
			GlobalTag entity = globalTagService.getGlobalTag(globaltagname);

			// Define a resource pointing to root dir only, plus schema dir if it exists
			Resource resource = new FileSystemResource(localrootdir);
			Path rootdir = Paths.get(resource.getFile().getPath());

			// Now you should define a path for the global tag and the package name
			String mainpkg = schemadir+PATH_SEPARATOR+globaltagname;
			Path pmainpkg = Paths.get(mainpkg);
			Path schemapath = Paths.get(schemadir);

			log.debug("Created path for main package: "+pmainpkg);

			boolean gtagexists = Files.exists(rootdir.resolve(pmainpkg));
			if (!gtagexists) {
				this.dumpAsgGlobalTagOnDisk(entity, schemadir);
			}

			
			// Now you can create the tar file
			Resource tarresource = new FileSystemResource(rootdir.resolve(pmainpkg).toRealPath().toString());
			if (!tarresource.exists()) {
				System.out.println("Cannot create a tar, directory does not yet exists");
			}

			List<Path> pathlist = new ArrayList<>();
			DirectoryStream<Path> dstream = Files.newDirectoryStream(rootdir.resolve(pmainpkg));
			try {
				for (Path file : dstream) {
					System.out.println("Found file or directory: " + file.getFileName());
					boolean islink = Files.isSymbolicLink(file);
					if (islink) {
						Path link = Files.readSymbolicLink(file);
						System.out.println("this is a link: " + link);
						// pathlist.add(link);
					}
					pathlist.add(file);
				}
			} catch (DirectoryIteratorException x) {
				// IOException can never be thrown by the iteration.
				// In this snippet, it can only be thrown by newDirectoryStream.
				System.out.println("DirectoryStream produced an error: " + x.getMessage());
			}

			String tardir = "../tar-files";
			Path ptar = Paths.get(tardir);
			boolean tardirexists = Files.exists(rootdir.resolve(ptar));
			if (!tardirexists) {
				Path tardirresource = Files.createDirectories(rootdir.resolve(ptar));
				System.out.println("Created path for tar directory: " + tardirresource);
			}
			String tarname = globaltagname + ".tar";
			String createdtarpath = fillFinalTar(tarname, globaltagname, rootdir, schemapath, ptar, pathlist);
			Path createdtar = Paths.get(createdtarpath);
			File tarfile = rootdir.resolve(createdtar).toFile();
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

	public void fillTar(final String tarName, Path rootdir, Path pkgdir, Path tardir, final List<Path> pathEntries) throws IOException {

		String tarfilepath = tardir.toString() + PATH_SEPARATOR + tarName;
		Path ptar = rootdir.resolve(tarfilepath);
		log.debug("Create tar file in "+ptar);
		OutputStream tarOutput = new FileOutputStream(new File(ptar.toString()));

		ArchiveOutputStream tarArchive = new TarArchiveOutputStream(tarOutput);
		((TarArchiveOutputStream) tarArchive).setLongFileMode( TarArchiveOutputStream.LONGFILE_GNU );
		log.debug("Now loop into path list of size "+pathEntries.size());
		Path tarbasedir = rootdir.resolve(pkgdir);
		log.debug("Base directory for tar is "+tarbasedir);
		for (Path path : pathEntries) {
			log.debug("Add entry to tar "+path);
//			Path relativepath = rootdir.relativize(path);
//			log.info("Created relative file path (not used for the moment): " + relativepath.toString());
			File file = tarbasedir.resolve(path).toFile();
			if (file.isDirectory()) {
				log.debug("Entry "+file.getAbsolutePath()+" is directory...open it");
				List<File> files = new ArrayList<File>();
				files.addAll(this.recurseDirectory(file));
				for (File dirfile : files) {
					Path dirfpath = Paths.get(dirfile.getPath());
					Path relativepath = tarbasedir.relativize(dirfpath);
					log.debug("directory entry is "+relativepath+" from "+dirfpath);
					TarArchiveEntry tarArchiveEntry = new TarArchiveEntry(relativepath.toString());
					tarArchive.putArchiveEntry(tarArchiveEntry);
					tarArchive.closeArchiveEntry();
				}
				TarArchiveEntry tarArchiveEntry = new TarArchiveEntry(path.toString());
				tarArchive.putArchiveEntry(tarArchiveEntry);
				tarArchive.closeArchiveEntry();
				continue;
			}
			TarArchiveEntry tarArchiveEntry = new TarArchiveEntry(file, path.toString());
			tarArchive.putArchiveEntry(tarArchiveEntry);
			FileInputStream fileInputStream = new FileInputStream(file);
			IOUtils.copy(fileInputStream, tarArchive);
			fileInputStream.close();
			tarArchive.closeArchiveEntry();
		}
//		List<File> files = new ArrayList<File>();
//		for (File file : pathEntries) {
//			files.addAll(this.recurseDirectory(file));
//		}
//		log.debug("Now loop into file list of size "+files.size());

//		for (File file : files) {
//			Path path = Paths.get(file.getPath());
//			log.info("Using file path " + path.toString());
//			Path relativepath = rootdir.relativize(path);
//			log.info("Created relative file path " + relativepath.toString());
//			TarArchiveEntry tarArchiveEntry = new TarArchiveEntry(file, path.toString());
//			tarArchive.putArchiveEntry(tarArchiveEntry);
//			FileInputStream fileInputStream = new FileInputStream(file);
//			IOUtils.copy(fileInputStream, tarArchive);
//			fileInputStream.close();
//			tarArchive.closeArchiveEntry();
//			//			tarArchiveEntry.setSize(file.length());
//		}
		tarArchive.finish();
		tarOutput.close();
	}
	
	protected String fillFinalTar(final String tarName, final String gtag, Path rootdir, Path pkgdir, Path tardir,
			final List<Path> pathEntries) throws IOException {

		String tarfilepath = tardir.toString() + PATH_SEPARATOR + tarName;
		Path ptar = rootdir.resolve(tarfilepath);
		OutputStream tarOutput = new FileOutputStream(new File(ptar.toString()));

		ArchiveOutputStream tarArchive = new TarArchiveOutputStream(tarOutput);
		((TarArchiveOutputStream) tarArchive).setLongFileMode(TarArchiveOutputStream.LONGFILE_GNU);

		Path asgtag = pkgdir.resolve(gtag);
		Path tarbasedir = rootdir.resolve(pkgdir);
		Path asgbasedir = rootdir.resolve(asgtag);
		log.debug("The root directory of the tar ASG is " + asgtag);
		log.debug("The tarbasedir dir is " + tarbasedir);
		log.debug("The asgbase dir is " + asgbasedir);

		for (Path path : pathEntries) {
			log.debug("Adding entry to tar: " + path);
			Path savedlink = getLink(path, rootdir);
			if (savedlink != null) {
				Path relativepath = rootdir.relativize(path);
				archieve(tarArchive, null, relativepath, savedlink.toString(), true);
			}
			Path pkg = getPackageFromLink(savedlink);
			Path pkgasgrelpath = null;
			
			File file = rootdir.resolve(path).toFile();
			if (file.isDirectory()) {
				log.debug("Entry " + file.getAbsolutePath() + " is directory...open it");
				List<File> files = new ArrayList<File>();
				files.addAll(this.recurseDirectory(file));
				for (File dirfile : files) {
					Path dirfpath = Paths.get(dirfile.getPath());
					Path relativepath = rootdir.relativize(dirfpath);
					if (pkg != null) {
						Path tmppath = rootdir.resolve(pkg);
						log.debug(" == Package path is " + tmppath);
						Path relativepkgpath = asgbasedir.relativize(dirfpath);
						log.debug(" == Package entry is " + relativepkgpath + " from " + dirfpath);
						pkgasgrelpath = tmppath.resolve(relativepkgpath);
						relativepath = rootdir.relativize(pkgasgrelpath);
					}
					log.debug("Directory entry is " + relativepath + " from " + dirfpath);
					archieve(tarArchive, dirfile, relativepath, null, false);
				}
				continue;
			}
			log.debug("Now storing " + file.getName() + " in path " + path);
			archieve(tarArchive, file, path, null, false);
		}
		tarArchive.finish();
		tarOutput.close();
		return ptar.toString();
	}

	
	protected Path getLink(Path path, Path rootdir) throws IOException {
		// Add the link to the tar...
		boolean islink = Files.isSymbolicLink(path);
		if (islink) {
			Path relativepath = rootdir.relativize(path);
			Path link = Files.readSymbolicLink(path);
			File filelink = rootdir.resolve(link).toFile();
			log.debug(
					"File link is " + filelink.getName() + " in relative path " + relativepath + " from link " + link);
			return link;
		}
		return null;
	}

	protected void archieve(ArchiveOutputStream tarArchive, File file, Path path, String linkname, boolean islink) {
		if (!islink) {
			try {
				TarArchiveEntry tarArchiveEntry = new TarArchiveEntry(file, path.toString());
				tarArchive.putArchiveEntry(tarArchiveEntry);
				FileInputStream fileInputStream = new FileInputStream(file);
				IOUtils.copy(fileInputStream, tarArchive);
				fileInputStream.close();
				tarArchive.closeArchiveEntry();
				log.debug("Entered file in tar " + path);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			try {
				TarArchiveEntry entry = new TarArchiveEntry(path.toString(), TarConstants.LF_SYMLINK);
				entry.setLinkName(linkname);
				tarArchive.putArchiveEntry(entry);
				tarArchive.closeArchiveEntry();
				log.debug("Entered link in tar " + path);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	protected Path getPackageFromLink(Path link) {
		if (link != null) {
			int count = link.getNameCount();
			return link.getName(count - 2);
		}
		return null;
	}


	public String getLocalrootdir() {
		return localrootdir;
	}

	public void setLocalrootdir(String localrootdir) {
		this.localrootdir = localrootdir;
	}
	
	
}
