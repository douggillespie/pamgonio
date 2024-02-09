package fastlocdisplay.aisfile;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import fastlocdisplay.FastlocViewProcess;

/**
 * Class to monitor for changed files within a folder. 
 * @author dg50
 *
 */
public class AISFileMonitor {

	/**
	 * Root folder
	 */
	private String root;
	
	/**
	 * Sub folders flag
	 */
	private boolean subFolders;
	
	
	private FilenameFilter filenameFilter = new AISFileFilter();
	
	/*
	 * file search mask
	 */
	private String mask;
	
	private HashMap<String, AISFileInformation> fileInformation = new HashMap<>();
	
	private Thread monitorThread;
	
	private volatile boolean runMonitor;
	
	private FastlocAISFile fastlocAISFile;
	
	public AISFileMonitor(AISDataMonitor aisDataMonitor) {
		fastlocAISFile = new FastlocAISFile();
		fastlocAISFile.setAisDataMonitor(aisDataMonitor);
	}

	public static void main(String[] args) {
		new AISFileMonitor(null).test();
	}
	
	private void test() {
		root = "C:\\ProjectData\\goniometer\\ArchivedData\\Iceland_2023\\Goniometer\\gonio_playback";
		subFolders = true;
		long t1 = System.nanoTime();
		ArrayList<File> newFiles = listAllFiles();
		long t2 = System.nanoTime();
		System.out.printf("Found %d files in %3.1fms\n", newFiles.size(), (double) (t2-t1)/1.e6);
		for (int i = 0; i < newFiles.size(); i++) {
			BasicFileAttributes attr = null;
			try {
				attr = Files.readAttributes(newFiles.get(i).toPath(), BasicFileAttributes.class, LinkOption.NOFOLLOW_LINKS);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			System.out.printf("%s %s\n", newFiles.get(i).getName(), attr.lastModifiedTime());
		}
	}
	
	public void startMonitor() {
		stopMonitor();
		runMonitor = true;
		monitorThread = new Thread(new Runnable() {
			
			@Override
			public void run() {
				monitorLoop();
			}

		});
		monitorThread.start();
	}

	/**
	 * Infinite loop of file monitoring. 
	 */
	private void monitorLoop() {
		while (runMonitor) {
			List<File> modifiedFiles = getModifiedFiles();
			if (modifiedFiles.size() > 0) {
				processModifiedFiles(modifiedFiles);
			}
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
//				e.printStackTrace();
			}			
		}
	}
	
	private void processModifiedFiles(List<File> modifiedFiles) {
		System.out.printf("Process %d modified AIS files\n", modifiedFiles.size());
		for (File aFile : modifiedFiles) {
			// get the old info about this file. 
			AISFileInformation fileInfo = fileInformation.get(aFile.getName()); // should always exist. 
			int newLines = fastlocAISFile.readFile(aFile, fileInfo.linesProcessed);
			if (newLines < 0) {
				System.out.printf("Error %d processing AIS file %s\n", newLines, aFile.getName());
			}
			else {
			fileInfo.linesProcessed += newLines;
			System.out.printf("File %s, processed %d new lines\n", aFile.getName(), newLines);
			}
		}
	}

	public void stopMonitor() {
		runMonitor = false;
		if (monitorThread == null) {
			return;
		}
		monitorThread.interrupt();
		try {
			monitorThread.join();
		} catch (InterruptedException e) {
		}
	}
	
	/**
	 * Get a list of files that are either new, or have been modified
	 * since the last call to this function. 
	 * @return list of new or modified files. 
	 */
	public List<File> getModifiedFiles() {
		ArrayList<File> allFiles = listAllFiles();
		ArrayList<File> modifiedFiles = new ArrayList<>();
		for (File aFile : allFiles) {
			AISFileInformation exInfo = fileInformation.get(aFile.getName());
			BasicFileAttributes attr = null;
			try {
				attr = Files.readAttributes(aFile.toPath(), BasicFileAttributes.class, LinkOption.NOFOLLOW_LINKS);
			} catch (IOException e) {
				e.printStackTrace();
			}
			if (exInfo == null) {
				// new file, not previously listed. 
				modifiedFiles.add(aFile);
				fileInformation.put(aFile.getName(), new AISFileInformation(aFile, attr));
			}
			else {
				if (exInfo.attributes.lastModifiedTime().compareTo(attr.lastModifiedTime()) == 0) {
					// file has same modified time, so nothing to do. 
					continue;
				}
				else {
					// file modified time has changed, so want this file too. 
					modifiedFiles.add(aFile); // add it to the modified list. 
					exInfo.attributes = attr; // and update it's attributes in the map. 
				}
			}
		}
		modifiedFiles.sort(new AISFileComparator());
		return modifiedFiles;
	}

	private class AISFileComparator implements Comparator<File> {

		@Override
		public int compare(File o1, File o2) {
			String n1 = o1.getName();
			String n2 = o2.getName();
			return n1.compareTo(n2);
		}
		
	}
	public void setOptions(String root, boolean subFolders, String mask) {
		this.root = root;
		this.subFolders = subFolders;
		this.mask = mask;
		startMonitor();
	}
	
	/**
	 * Make a list of all files using root, subFolders and mask. 
	 * @return
	 */
	public ArrayList<File> listAllFiles() {
		ArrayList<File> newList = new ArrayList();
		listFiles(newList, root);
		return newList;
	}

	/**
	 * List files in folder and where appropriate, go into sub folders. 
	 * @param newList
	 * @param folder
	 */
	private void listFiles(ArrayList<File> newList, String folder) {
		File dir = new File(folder);
		if (dir.exists() == false) {
			return;
		}
		if (dir.isDirectory() == false) {
			System.out.printf("This should never happen: %s is not a directory\n" , folder);
			return;
		}
		File[] fileList = dir.listFiles(filenameFilter);
		if (fileList == null)  return;
		for (int i = 0; i < fileList.length; i++) {
			if (fileList[i].isDirectory()) {
				listFiles(newList, fileList[i].getAbsolutePath());
			}
			else {
				newList.add(fileList[i]);
			}
		}
	}
	
	private class AISFileFilter implements FilenameFilter {

		@Override
		public boolean accept(File dir, String name) {
			if (name.startsWith("AIS_Stream") & name.endsWith(".dat")) {
				return true;
			}
			if (subFolders == false) {
				return false;
			}
			File fullFile = new File(dir.getAbsolutePath() + File.separator + name);
			if (fullFile.isDirectory()) {
				return true;
			}		
			
			return false;
		}
		
	}
}
