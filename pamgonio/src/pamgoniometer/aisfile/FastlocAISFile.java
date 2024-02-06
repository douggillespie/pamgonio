package pamgoniometer.aisfile;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import AIS.AISDataUnit;
import AIS.AISPositionReport;
import NMEA.AcquireNmeaData;

/**
 * Functions for reading and also remembering state of AIS data files. 
 * @author dg50
 *
 */
public class FastlocAISFile {

	public static void main(String[] args) {
		new FastlocAISFile().test();
	}

	private void test() {
//		String tstfile = "C:\\ProjectData\\goniometer\\ArchivedData\\Iceland_2022\\Goniometer\\Processing Log Files\\boat_test\\17-Jun-2022\\AIS_Stream_2022-06-17-091128.dat";
		String tstfile = "C:\\ProjectData\\goniometer\\ArchivedData\\Iceland_2023\\Goniometer\\gonio_playback\\8_Jul\\AIS_Stream_2023-07-08-075139.dat";
		File tstFile = new File(tstfile);
		readFile(tstFile);
	}

	private boolean readFile(File tstFile) {
		BufferedReader bir = null;
		try {
			FileReader fr = new FileReader(tstFile);
			bir = new BufferedReader(fr);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (bir == null) {
			return false;
		}
		while (true) {
			String aLine = null;
			try {
				aLine = bir.readLine();
			} catch (IOException e) {
				e.printStackTrace();
			}
			if (aLine == null) {
				break;
			}
//			System.out.println(aLine);
			AISFileLineInfo lineInfo = AISFileLineInfo.interpretLine(aLine);
			AISDataUnit aisData = extractAISLine(aLine);
			AISPositionReport report = aisData.getPositionReport();
			System.out.printf("%s : %s\n", lineInfo.toString(), report.toString());
			
		}
		
		
		return true;
	}
	
	/**
	 * Unpacks the AIS data from a line from one of the files. 
	 * does this by finding the start of the !AIVDM string and then 
	 * unpacking it as a standard AIS message. 
	 * @param aLine full line read from the file. 
	 * @return and AIS data unit with a single position report. Note that the mmsi number is within the data unit. 
	 */
	private AISDataUnit extractAISLine(String aLine) {
		/*
		 * note that the AIS data provide position information to 1/600000 degrees, i.e. about 1e-6 
		 * accuracy or 6 decimal places. The lat long in the files is only to 4 dp, which works out at
		 * about 1e-4*1852*60 which is probably more than good enough. 
		 */
		int aisStart = aLine.indexOf("!AIVDM");
		if (aisStart < 0) {
			return null;
		}
		String aisString = aLine.substring(aisStart);
		StringBuffer sb = new StringBuffer(aisString);
		byte sum = AcquireNmeaData.createStringChecksum(sb);
		byte checkSum = AcquireNmeaData.getStringChecksum(sb);
		boolean stringOK = sum==checkSum;
		if (stringOK == false) {
			System.out.println("Invalid checksum in AIS String " + aisString);
			return null;
		}
		String[] bits = aisString.split(",");	
		String aisBit = bits[5];
		AISDataUnit newVDM = new AISDataUnit(System.currentTimeMillis(), aisBit, 0);
		boolean ok = newVDM.decodeMessage();
		AISPositionReport report = newVDM.getPositionReport();
		if (report == null) {
			System.out.println("no available position in AIS data: " + aisString);
		}
		else {
//			System.out.printf("Report for mmsi %d: %s\n", newVDM.mmsiNumber, report.toString());
		}
		return newVDM;
	}
	
	
}
