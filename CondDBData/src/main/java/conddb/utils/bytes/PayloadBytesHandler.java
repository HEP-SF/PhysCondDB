package conddb.utils.bytes;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Blob;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.apache.commons.compress.utils.IOUtils;
import org.hibernate.engine.jdbc.StreamUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
public class PayloadBytesHandler {

	private Logger log = LoggerFactory.getLogger(this.getClass());

	@Autowired
	@Qualifier("daoDataSource")
	private DataSource ds;

	private static Integer MAX_LENGTH=1024;
	
	public byte[] getBytesFromInputStream(InputStream is) {
		try {
			ByteArrayOutputStream buffer = new ByteArrayOutputStream();

			int nRead;
			byte[] data = new byte[16384];

			while ((nRead = is.read(data, 0, data.length)) != -1) {
				buffer.write(data, 0, nRead);
			}
			buffer.flush();
			return buffer.toByteArray();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	public void saveToFile(InputStream uploadedInputStream, String uploadedFileLocation) {

		try {
			OutputStream out = null;
			int read = 0;
			byte[] bytes = new byte[MAX_LENGTH];

			out = new FileOutputStream(new File(uploadedFileLocation));
			while ((read = uploadedInputStream.read(bytes)) != -1) {
				out.write(bytes, 0, read);
			}
			out.flush();
			out.close();
		} catch (IOException e) {

			e.printStackTrace();
		}
	}

	public void saveStreamToFile(InputStream uploadedInputStream, String uploadedFileLocation) {

		try {
			OutputStream out = new FileOutputStream(new File(uploadedFileLocation));
			StreamUtils.copy(uploadedInputStream, out);
			out.close();
		} catch (IOException e) {

			e.printStackTrace();
		}
	}

	public byte[] readFromFile(String uploadedFileLocation) {

		try {
			java.nio.file.Path path = Paths.get(uploadedFileLocation);
			byte[] data = Files.readAllBytes(path);
			return data;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;

	}

	public long lengthOfFile(String uploadedFileLocation) {

		try {
			java.nio.file.Path path = Paths.get(uploadedFileLocation);
			Files.size(path);
			return Files.size(path);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return 0;
	}

	
	public Blob createBlobFromFile(String filelocation) {
		Blob blob=null;
		try {
			File f = new File(filelocation);
			BufferedInputStream fstream = new BufferedInputStream(new FileInputStream(f));
			blob = ds.getConnection().createBlob();
			BufferedOutputStream bstream = new BufferedOutputStream(blob.setBinaryStream(1));
			// stream copy runs a high-speed upload across the network
			StreamUtils.copy(fstream, bstream);
			return blob;
		} catch (IOException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return blob;
	}
	
	@Deprecated
	public File dumpBlobIntoFile(Blob blob, String outfilelocation) {
		try {
			File f = new File(outfilelocation);
			OutputStream os = dumpBlobIntoStream(blob, new BufferedOutputStream(new FileOutputStream(f)));
			os.close();
			if (f.exists()){
				log.info("File created in server with length "+f.length()+" and name "+outfilelocation);
			}
			return new File(outfilelocation);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	@Deprecated
	public OutputStream dumpBlobIntoStream(Blob blob, OutputStream outstream) {
		try {
			InputStream bstream = blob.getBinaryStream();
			BufferedOutputStream fstream = new BufferedOutputStream(outstream);
			// stream copy runs a high-speed upload across the network
	        IOUtils.copy(bstream, fstream);
			return fstream;
		} catch (IOException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}

}
