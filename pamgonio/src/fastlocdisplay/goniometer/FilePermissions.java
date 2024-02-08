package fastlocdisplay.goniometer;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.PosixFilePermission;
import java.util.Set;

public class FilePermissions {

	public static void main(String[] args) {
		new FilePermissions().test();
	}

	private void test() {
		// TODO Auto-generated method stub
		String tstFile = "C:\\ProgramData\\FastGPS Realtime Solution\\argosid.properties";
		try {
			checkFile(tstFile);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void checkFile(String tstFile) throws Exception {
		File file = new File(tstFile);
		Path path = file.toPath();
		BasicFileAttributes attr = null;
		boolean write = false;
		try {
			attr = Files.readAttributes(path, BasicFileAttributes.class, LinkOption.NOFOLLOW_LINKS);
			write = file.canWrite();
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("File attributes " + attr);
		System.out.println("Can write: " + write);
		
		// try copying it, then copying it back on itself. 
		String copyName = tstFile + ".copy";
		File copyFile = new File(copyName);
		
		boolean del = file.delete();
		Files.copy(copyFile.toPath(), path);
		
	}
}
