package fastlocdisplay.aisfile;

import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;

/**
 * information extracted from a file file. 
 * @author dg50
 *
 */
public class TimeFileInfo {

	protected ArrayList<SystemTimePair> timePairs = new ArrayList<>();
	
	protected BasicFileAttributes basicFileAttributes;
	
	public SystemTimePair getLastTimePair() {
		if (timePairs.size() == 0) {
			return null;
		}
		return timePairs.get(timePairs.size()-1);
	}

//	@Override
//	public String toString() {
//		String str;
//		if (basicFileAttributes != null) {
//			long modified = basicFileAttributes.lastModifiedTime().toMillis();
//			str = String.format("file modified %s", null);
//		}
//	}
	
	
	
}
