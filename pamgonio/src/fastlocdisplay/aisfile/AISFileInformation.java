package fastlocdisplay.aisfile;

import java.io.File;
import java.nio.file.attribute.BasicFileAttributes;

/**
 * Information about an AIS file that will be held in a hashmap during 
 * a PAMGuard run, to know which files are new and which modified. 
 * @author dg50
 *
 */
public class AISFileInformation {

	/**
	 * The file identifier
	 */
	public File file;
	
	/**
	 * Previous attributes. 
	 */
	public BasicFileAttributes attributes;
	
	/**
	 * Lines previously processed
	 */
	public int linesProcessed;

	public AISFileInformation(File file, BasicFileAttributes attributes) {
		super();
		this.file = file;
		this.attributes = attributes;
	}
	
}
