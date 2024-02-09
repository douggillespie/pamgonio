package fastlocdisplay.goniometer;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.PosixFilePermission;
import java.util.Set;

/**
 * Utility functions to check on some file permissions to see if we can 
 * automatically edit some of the Goniometer properties. 
 * @author dg50
 *
 */
public class FilePermissions {

//	public static void main(String[] args) {
//		new FilePermissions().test();
//	}
//
//	private void test() {
//		// TODO Auto-generated method stub
//		String tstFile = "C:\\ProgramData\\FastGPS Realtime Solution\\fastgps.properties";
//		try {
//			checkFile(tstFile);
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//	}
//
//	private void checkFile(String tstFile) throws Exception {
//		File file = new File(tstFile);
//		Path path = file.toPath();
//		BasicFileAttributes attr = null;
//		boolean write = false;
//		try {
//			attr = Files.readAttributes(path, BasicFileAttributes.class, LinkOption.NOFOLLOW_LINKS);
//			write = file.canWrite();
//			
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		String user = System.getProperty("user.name");
//		String domain = System.getProperty("domain");
//		System.out.println("User is " + user);
//		System.out.println("Domain is " + domain);
//		System.out.println("File attributes " + attr);
//		System.out.println("Can write: " + write);
//		
//		String[] commands = new String[2];
//		commands[0] = "icacls";
//		commands[1] = tstFile;
//		ProcessBuilder proB = new ProcessBuilder(commands);
//		Process process = proB.start();
//		BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()));
//		String line;
//		while ((line = br.readLine()) != null) {
//			System.out.println(line);
//		}
//		
//		String permissions = getFilePermission(tstFile);
//		System.out.println("File permissions are " + permissions);
//		System.out.println("Write access is " + canWrite(tstFile));
//		
//		System.out.println("Done");
////		// try copying it, then copying it back on itself. 
////		String copyName = tstFile + ".copy";
////		File copyFile = new File(copyName);
////		
////		boolean del = file.delete();
////		Files.copy(copyFile.toPath(), path);
//		
//	}
	/**
	 * Get the permissions for this file for either users or for this user.  
	 * @param filePath
	 * @return
	 * @throws IOException 
	 */
	public static String getFilePermission(String filePath) throws IOException {
		String[] commands = new String[2];
		commands[0] = "icacls";
		commands[1] = filePath;
		ProcessBuilder proB = new ProcessBuilder(commands);
		Process process = proB.start();
		BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()));
		String line;
		String user = System.getProperty("user.name");
		if (user != null) {
			user += ":";
		}
		else {
			user = "never a value";
		}
		String goodLine = null;
		while ((line = br.readLine()) != null) {
			if (line.contains(user)) {
				goodLine = line;
				break;
			}
			if (line.contains("BUILTIN\\Users:")) {
				goodLine = line;
				break;
			}
		}
		if (goodLine == null) {
			return null;
		}
		int lastP = goodLine.lastIndexOf("(");
		int lastQ = goodLine.lastIndexOf(")");
		if (lastP < 0 || lastQ < 0) {
			return null;
		}
		String permissions = goodLine.substring(lastP+1, lastQ);
		return permissions;
	}
	
	/**
	 * Use Windows shell to see if we have write access to a file. 
	 * @param filePath file path
	 * @return true if we have full or write control over the file. 
	 */
	public static boolean canWrite(String filePath) {
		String permissions = null;
		try {
			permissions = getFilePermission(filePath);
		} catch (IOException e) {
			System.out.println(e.getMessage());
		}
		if (permissions == null) {
			return false;
		}
		return (permissions.contains("F") || permissions.contains("W"));
	}
}
