/**
 * 
 */
package temporary;

import java.io.File;
import java.nio.file.Paths;

/**
 * @author aformic
 *
 */
public class JavaPath {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		File file = new File("/tmp/JavaDocPkg-00-03/JavaDocPkg/Computing/Generics/jquery-getting-started/t0.pdf");
    	java.nio.file.Path rootpath = Paths.get("/tmp");
    	java.nio.file.Path path = Paths.get(file.getPath());
    	java.nio.file.Path relativepath = rootpath.relativize(path);
    	System.out.println("rootpath "+rootpath);
    	System.out.println("path "+path);
    	System.out.println("relative "+relativepath);
	}

}
