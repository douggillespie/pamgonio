package fastlocdisplay.aisfile;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import PamUtils.PamCalendar;

public class SystemTimeFile {

	public static void main(String[] args) {
		new SystemTimeFile().tests();
	}

	private void tests() {
		String demoFile = "C:\\ProjectData\\goniometer\\ArchivedData\\Iceland_2023\\Goniometer\\gonio_playback\\12_Jul\\COM16_2023-07-12_141344_systime.txt";
//		String demoFile = "C:\\ProjectData\\goniometer\\ArchivedData\\Bench_2024\\COM48_2024-02-06_152424_systime.txt";
		ArrayList<SystemTimePair> data = readTimefile(new File(demoFile));
		// get the file information ...
		BasicFileAttributes attr = null;
		try {
			attr = Files.readAttributes(new File(demoFile).toPath(), BasicFileAttributes.class, LinkOption.NOFOLLOW_LINKS);
		} catch (IOException e) {
			e.printStackTrace();
		}
		SystemTimePair lastData = data.get(data.size()-1);
		long modified = attr.lastModifiedTime().toMillis();
//		long created = atte.
		System.out.printf("\nFile modified %s (%s), last line %s\n", PamCalendar.formatDBDateTime(modified), attr.lastModifiedTime().toString(), lastData);
		
	}

	private ArrayList<SystemTimePair> readTimefile(File timeFile) {
		BufferedReader bir = null;
		try {
			FileReader fr = new FileReader(timeFile);
			bir = new BufferedReader(fr);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		if (bir == null) {
			return null;
		}
		ArrayList<SystemTimePair> data = new ArrayList<>();
		// skip first line
		String aLine = null;
		try {
			aLine = bir.readLine();
		} catch (IOException e) {
			return null;
		}
		while (true) {
			try {
				aLine = bir.readLine();
			} catch (IOException e) {
				break;
			}
			if (aLine == null) {
				break;
			}
			String[] parts = aLine.split("       ");
			if (parts == null || parts.length != 2) {
				continue;
			}
			long gpsTime = 0, systemTime = 0;
			try {
				gpsTime = getTime(parts[0]);
				systemTime = getTime(parts[1]);
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			data.add(new SystemTimePair(gpsTime, systemTime));
			
		}
		
		return data;
	}	
	
	/**
	 * Interpret the date time string as milliseconds. 
	 * @param string
	 * @return
	 * @throws ParseException 
	 */
	private long getTime(String string) throws ParseException {
		string = string.trim();
		DateFormat dateInst = new SimpleDateFormat("yyyy-mm-dd HH:mm:ss");
		dateInst.setTimeZone(PamCalendar.defaultTimeZone);
		Date date = dateInst.parse(string);
		return date.getTime();
	}
}
