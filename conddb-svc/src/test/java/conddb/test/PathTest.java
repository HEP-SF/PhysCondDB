package conddb.test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.DirectoryIteratorException;
import java.nio.file.DirectoryStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.compress.archivers.ArchiveOutputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.compress.archivers.tar.TarConstants;
import org.apache.commons.compress.utils.IOUtils;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

public class PathTest {

	public static final String PATH_SEPARATOR = "/";

	public static void main(String[] args) {
		PathTest ptest = new PathTest();
		Resource resource = new FileSystemResource("/tmp/physconddb-dump/calib-data");

		try {
			Path rootdir = Paths.get(resource.getFile().getPath());
			String mypkg = "MyTestPkg/MyTestPkg-00-01";
			String asg = "ASG/ASG-02-01/MyTestPkg-00-01";
			Path pmypkg = Paths.get(mypkg);
			Path pasg = Paths.get(asg);
			Path asg_relativize_mypkg = pasg.relativize(pmypkg);
			System.out.println("asg_relativize_mypkg after relativize is " + asg_relativize_mypkg.toString());
			System.out.println("asg_relativize_mypkg after resolve is " + rootdir.resolve(pasg));
			boolean fileexists = Files.exists(rootdir.resolve(pasg).toRealPath());
			System.out.println("The file from path " + pasg.toString() + " exists = " + fileexists);
			if (!fileexists) {
				Path asg_mypkg_link = Files.createSymbolicLink(rootdir.resolve(pasg), asg_relativize_mypkg);
				System.out.println("asg_mypkg_link is " + asg_mypkg_link.toString());
			}

			Path newLink = Paths.get(resource.getFile().getPath() + PATH_SEPARATOR + "MyTestPkg/MyTestPkg-01-01");
			Path target = Paths.get(resource.getFile().getPath() + PATH_SEPARATOR + "ASG");
			System.out.println("newLink is " + newLink.toString());
			// Path rootpath = Paths.get(resource.getFile().getPath());
			Path mypkg_relativize = rootdir.relativize(newLink);
			System.out.println("mypkg_relativize after relativize is " + mypkg_relativize.toString());
			String asgtag = "ASG";
			Path asggtagpath = Paths.get(asgtag);
			String gtag = "ASG-02-01";
			String asggtag = "ASG/" + gtag;
			Path asggtagfullpath = Paths.get(asggtag);

			PrintFiles pf = new PrintFiles();
			Files.walkFileTree(rootdir.resolve(asggtagpath), pf);

			// Now you can create the tar file
			Resource tarresource = new FileSystemResource(rootdir.resolve(asggtagfullpath).toRealPath().toString());
			if (!tarresource.exists()) {
				System.out.println("Cannot create a tar, directory does not yet exists");
			}

			List<Path> pathlist = new ArrayList<>();
			DirectoryStream<Path> dstream = Files.newDirectoryStream(rootdir.resolve(asggtagfullpath));
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
			String tarname = "test.tar";
			ptest.fillFinalTar(tarname, gtag, rootdir, asggtagpath, ptar, pathlist);

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public Path getLink(Path path, Path rootdir) throws IOException {
		// Add the link to the tar...
		boolean islink = Files.isSymbolicLink(path);
		if (islink) {
			Path relativepath = rootdir.relativize(path);
			Path link = Files.readSymbolicLink(path);
			File filelink = rootdir.resolve(link).toFile();
			System.out.println(
					"File link is " + filelink.getName() + " in relative path " + relativepath + " from link " + link);
			return link;
		}
		return null;
	}

	public void archieve(ArchiveOutputStream tarArchive, File file, Path path, String linkname, boolean islink) {
		if (!islink) {
			try {
				TarArchiveEntry tarArchiveEntry = new TarArchiveEntry(file, path.toString());
				tarArchive.putArchiveEntry(tarArchiveEntry);
				FileInputStream fileInputStream = new FileInputStream(file);
				IOUtils.copy(fileInputStream, tarArchive);
				fileInputStream.close();
				tarArchive.closeArchiveEntry();
				System.out.println("Entered file in tar " + path);
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
				System.out.println("Entered link in tar " + path);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public Path getPackageFromLink(Path link) {
		if (link != null) {
			int count = link.getNameCount();
			return link.getName(count - 2);
		}
		return null;
	}
	
	public void fillFinalTar(final String tarName, final String gtag, Path rootdir, Path pkgdir, Path tardir,
			final List<Path> pathEntries) throws IOException {

		String tarfilepath = tardir.toString() + PATH_SEPARATOR + tarName;
		Path ptar = rootdir.resolve(tarfilepath);
		OutputStream tarOutput = new FileOutputStream(new File(ptar.toString()));

		ArchiveOutputStream tarArchive = new TarArchiveOutputStream(tarOutput);
		((TarArchiveOutputStream) tarArchive).setLongFileMode(TarArchiveOutputStream.LONGFILE_GNU);

		Path asgtag = pkgdir.resolve(gtag);
		Path tarbasedir = rootdir.resolve(pkgdir);
		Path asgbasedir = rootdir.resolve(asgtag);
		System.out.println("The root directory of the tar ASG is " + asgtag);
		System.out.println("The tarbasedir dir is " + tarbasedir);
		System.out.println("The asgbase dir is " + asgbasedir);

		for (Path path : pathEntries) {
			System.out.println("Adding entry to tar: " + path);
			Path savedlink = getLink(path, rootdir);
			if (savedlink != null) {
				Path relativepath = rootdir.relativize(path);
				archieve(tarArchive, null, relativepath, savedlink.toString(), true);
			}
			Path pkg = getPackageFromLink(savedlink);
			Path pkgasgrelpath = null;
			
			File file = rootdir.resolve(path).toFile();
			if (file.isDirectory()) {
				System.out.println("Entry " + file.getAbsolutePath() + " is directory...open it");
				List<File> files = new ArrayList<File>();
				files.addAll(this.recurseDirectory(file));
				for (File dirfile : files) {
					Path dirfpath = Paths.get(dirfile.getPath());
					Path relativepath = rootdir.relativize(dirfpath);
					if (pkg != null) {
						Path tmppath = rootdir.resolve(pkg);
						System.out.println(" == Package path is " + tmppath);
						Path relativepkgpath = asgbasedir.relativize(dirfpath);
						System.out.println(" == Package entry is " + relativepkgpath + " from " + dirfpath);
						pkgasgrelpath = tmppath.resolve(relativepkgpath);
						relativepath = rootdir.relativize(pkgasgrelpath);
					}
					System.out.println("Directory entry is " + relativepath + " from " + dirfpath);
					archieve(tarArchive, dirfile, relativepath, null, false);
				}
				continue;
			}
			System.out.println("Now storing " + file.getName() + " in path " + path);
			archieve(tarArchive, file, path, null, false);
		}
		tarArchive.finish();
		tarOutput.close();
	}

	public void fillTar(final String tarName, final String gtag, Path rootdir, Path pkgdir, Path tardir,
			final List<Path> pathEntries) throws IOException {

		String tarfilepath = tardir.toString() + PATH_SEPARATOR + tarName;
		Path ptar = rootdir.resolve(tarfilepath);
		System.out.println("Create tar file in " + ptar);
		OutputStream tarOutput = new FileOutputStream(new File(ptar.toString()));

		ArchiveOutputStream tarArchive = new TarArchiveOutputStream(tarOutput);
		((TarArchiveOutputStream) tarArchive).setLongFileMode(TarArchiveOutputStream.LONGFILE_GNU);

		Path asgtag = pkgdir.resolve(gtag);
		System.out.println("The root directory of the tar ASG is " + asgtag);

		System.out.println("Now loop into path list of size " + pathEntries.size());
		Path tarbasedir = rootdir.resolve(pkgdir);
		Path asgbasedir = rootdir.resolve(asgtag);
		System.out.println("Use asgbase dir " + asgbasedir);

		System.out.println("Base directory for tar is " + tarbasedir);
		for (Path path : pathEntries) {
			System.out.println("Add entry to tar " + path);
			// Path relativepath = rootdir.relativize(path);
			// log.info("Created relative file path (not used for the moment): "
			// + relativepath.toString());
			boolean islink = Files.isSymbolicLink(path);
			Path savedlink = null;
			if (islink) {
				// Add the link to the tar...
				Path relativepath = rootdir.relativize(path);
				Path link = Files.readSymbolicLink(path);
				File filelink = rootdir.resolve(link).toFile();
				System.out.println("File link is " + filelink.getName() + " in relative path " + relativepath
						+ " from link " + link);
				savedlink = link;
				TarArchiveEntry entry = new TarArchiveEntry(relativepath.toString(), TarConstants.LF_SYMLINK);
				entry.setLinkName(link.toString());
				tarArchive.putArchiveEntry(entry);
				tarArchive.closeArchiveEntry();
				System.out.println("Entered link in tar " + path);

			}
			File file = rootdir.resolve(path).toFile();
			if (file.isDirectory()) {
				System.out.println("Entry " + file.getAbsolutePath() + " is directory...open it");
				List<File> files = new ArrayList<File>();
				files.addAll(this.recurseDirectory(file));
				Path pkg = null;
				Path pkgasgrelpath = null;
				if (savedlink != null) {
					int count = savedlink.getNameCount();
					pkg = savedlink.getName(count - 2);
				}

				for (File dirfile : files) {
					Path dirfpath = Paths.get(dirfile.getPath());
					Path relativepath = rootdir.relativize(dirfpath);
					if (pkg != null) {
						Path pkgpath = rootdir.resolve(pkg);
						System.out.println("Package path is " + pkgpath);
						Path relativepkgpath = asgbasedir.relativize(dirfpath);
						// relativepath = rootdir.relativize(path);
						System.out.println("Package entry is " + relativepkgpath + " from " + dirfpath);
						pkgasgrelpath = pkgpath.resolve(relativepkgpath);
					}
					if (pkgasgrelpath != null) {
						relativepath = rootdir.relativize(pkgasgrelpath);
					}
					System.out.println("directory entry is " + relativepath + " from " + dirfpath);
					TarArchiveEntry tarArchiveEntry = new TarArchiveEntry(dirfile, relativepath.toString());
					tarArchive.putArchiveEntry(tarArchiveEntry);
					FileInputStream fileInputStream = new FileInputStream(dirfile);
					IOUtils.copy(fileInputStream, tarArchive);
					fileInputStream.close();
					tarArchive.closeArchiveEntry();
				}
				// TarArchiveEntry tarArchiveEntry = new
				// TarArchiveEntry(path.toString());
				// tarArchive.putArchiveEntry(tarArchiveEntry);
				// tarArchive.closeArchiveEntry();
				continue;
			}
			TarArchiveEntry tarArchiveEntry = new TarArchiveEntry(file, path.toString());
			System.out.println("Now storing " + file.getName() + " in path " + path);
			tarArchive.putArchiveEntry(tarArchiveEntry);
			FileInputStream fileInputStream = new FileInputStream(file);
			IOUtils.copy(fileInputStream, tarArchive);
			fileInputStream.close();
			tarArchive.closeArchiveEntry();
		}
		// List<File> files = new ArrayList<File>();
		// for (File file : pathEntries) {
		// files.addAll(this.recurseDirectory(file));
		// }
		// log.debug("Now loop into file list of size "+files.size());

		// for (File file : files) {
		// Path path = Paths.get(file.getPath());
		// log.info("Using file path " + path.toString());
		// Path relativepath = rootdir.relativize(path);
		// log.info("Created relative file path " + relativepath.toString());
		// TarArchiveEntry tarArchiveEntry = new TarArchiveEntry(file,
		// path.toString());
		// tarArchive.putArchiveEntry(tarArchiveEntry);
		// FileInputStream fileInputStream = new FileInputStream(file);
		// IOUtils.copy(fileInputStream, tarArchive);
		// fileInputStream.close();
		// tarArchive.closeArchiveEntry();
		// // tarArchiveEntry.setSize(file.length());
		// }
		tarArchive.finish();
		tarOutput.close();
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

	public static class PrintFiles extends SimpleFileVisitor<Path> {

		// Print information about
		// each type of file.
		@Override
		public FileVisitResult visitFile(Path file, BasicFileAttributes attr) {
			if (attr.isSymbolicLink()) {
				System.out.format("Symbolic link: %s ", file);
				Path link;
				try {
					link = Files.readSymbolicLink(file);
					System.out.println(" - linking to " + link);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} else if (attr.isRegularFile()) {
				System.out.format("Regular file: %s ", file);
			} else {
				System.out.format("Other: %s ", file);
			}
			System.out.println("(" + attr.size() + "bytes)");
			return FileVisitResult.CONTINUE;
		}

		// Print each directory visited.
		@Override
		public FileVisitResult postVisitDirectory(Path dir, IOException exc) {
			System.out.format("Directory: %s%n", dir);
			return FileVisitResult.CONTINUE;
		}

		// If there is some error accessing
		// the file, let the user know.
		// If you don't override this method
		// and an error occurs, an IOException
		// is thrown.
		@Override
		public FileVisitResult visitFileFailed(Path file, IOException exc) {
			System.err.println(exc);
			return FileVisitResult.CONTINUE;
		}
	}

}
