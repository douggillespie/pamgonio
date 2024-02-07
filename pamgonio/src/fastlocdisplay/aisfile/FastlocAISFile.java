package fastlocdisplay.aisfile;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import AIS.AISDataUnit;
import AIS.AISPositionReport;
import NMEA.AcquireNmeaData;
import PamUtils.LatLong;

/**
 * Functions for reading and also remembering state of AIS data files. 
 * @author dg50
 *
 */
public class FastlocAISFile {

	private AISDataMonitor aisDataMonitor;
	
	public static void main(String[] args) {
		new FastlocAISFile().test();
	}

	private void test() {
	//	String tstfile = "C:\\ProjectData\\goniometer\\ArchivedData\\Iceland_2022\\Goniometer\\Processing Log Files\\boat_test\\17-Jun-2022\\AIS_Stream_2022-06-17-091128.dat";
		String tstfile = "C:\\ProjectData\\goniometer\\ArchivedData\\Iceland_2023\\Goniometer\\gonio_playback\\8_Jul\\AIS_Stream_2023-07-08-075139.dat";
		File tstFile = new File(tstfile);
		readFile(tstFile, 0);
	}

	public int readFile(File tstFile, int skipLines) {

		AISDataUnit firstAIS = null;
		AISDataUnit lastAIS = null;
		
		BufferedReader bir = null;
		try {
			FileReader fr = new FileReader(tstFile);
			bir = new BufferedReader(fr);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (bir == null) {
			return -1;
		}
		ArrayList<AISFileLineInfo> lineInfos = new ArrayList();
		int nLines = 0;
		int nGoodLines = 0;
//		System.out.printf("\nGood");
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
			nLines++;
			if (nLines <= skipLines) {
				continue;
			}
//			System.out.println(aLine);
			AISFileLineInfo lineInfo = AISFileLineInfo.interpretLine(aLine);
			AISDataUnit aisData = extractAISLine(aLine);
			AISPositionReport report = aisData.getPositionReport();
			lineInfo.setPositionReport(report);
			
			AISFileLineInfo duplicate = checkDuplicates(lineInfos, lineInfo);
			boolean isSeed = checkSeed(lineInfos, lineInfo);
			int error = (isSeed ? 1 : 0) + (duplicate != null ? 2 : 0);
			lineInfo.setErrorCode(error);
			if (error == 0) {
				nGoodLines++;
//				System.out.printf(",%d", nLines);
			}
			
//			if (firstAIS == null) {
//				firstAIS = aisData;
//				lastAIS = aisData;
//			}
			
//			LatLong thisLL = aisData.getPositionReport().latLong;
//			LatLong firstLL = firstAIS.getPositionReport().latLong;
//			LatLong prevLL = lastAIS.getPositionReport().latLong;
//			double distFirst = firstLL.distanceToMetres(thisLL);
//			double distLast = prevLL.distanceToMetres(thisLL);

//			System.out.printf("%s:%s %3.1fm from first, %3.1fm from previous\n", lineInfo.toString(), report.toString(), distFirst, distLast);
			if (aisDataMonitor != null) {
				aisDataMonitor.newAISData(lineInfo);
			}
			lastAIS = aisData;
			lineInfos.add(lineInfo);
		}
//		System.out.printf("\nRead %d lines from file, of which %d were good\n", nLines, nGoodLines);
		return nLines-skipLines;
	}

	/**
	 * Check to see if this is still the seed data. 
	 * @param lineInfos list of existing line infos. 
	 * @param lineInfo new line
	 * @return reference to first duplicate. 
	 */
	private boolean checkSeed(ArrayList<AISFileLineInfo> lineInfos, AISFileLineInfo lineInfo) {
		/*
		 *              This section is a workaround for another bug in
                        the gps position computation software from Paul.
                        If there is not Eph data available, the first
                        position in the file is used as a seed position.
                        This seed is used for all tags. However, if no Eph
                        data is available, the seed is simply stored as
                        output. Therefor, all positions equal to the first
                        line should be removed.
		 */
		if (lineInfos.size() == 0) {
			return false;
		}
		AISPositionReport firstReport = lineInfos.get(0).getPositionReport();
		AISPositionReport thisReport = lineInfo.getPositionReport();
		if (firstReport.getLatitude() == thisReport.getLatitude() & firstReport.getLongitude() == thisReport.getLongitude()) {
			return true;
		}
		else {
			return false;
		}
	}
	
	/**
	 * Check to see if this is a duplicate line. 
	 * @param lineInfos list of existing line infos. 
	 * @param lineInfo new line
	 * @return reference to first duplicate. 
	 */
	private AISFileLineInfo checkDuplicates(ArrayList<AISFileLineInfo> lineInfos, AISFileLineInfo lineInfo) {
		/*
		 *              This section is a work around for a bug in the GPS
                        position computation software from Paul.
                        Sometimes, duplicate positions are logged, that
                        have been associated with another tag. After the
                        first logged position, the new positions of other
                        tags at exactly the same position should be
                        ignored.
		 */
		/*
		 * Note that the TNO search for duplicates only seems to skip records which are
		 * a duplicate of the first record in the log file. This is clearly missing duplicates
		 * for other tags, so this will skip ANY duplicates of positions of the same tag. 
		 */
		for (AISFileLineInfo oldLine : lineInfos) {
			AISPositionReport oldPos = oldLine.getPositionReport();
			AISPositionReport newPos = lineInfo.getPositionReport();
			if (oldPos.getLatitude() == newPos.getLatitude() & 
					oldPos.getLongitude() == newPos.getLongitude()) {
//					oldLine.getIntegerId() == lineInfo.getIntegerId()) {
				return oldLine;
			}
		}
		return null;
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

	/**
	 * @return the aisDataMonitor
	 */
	public AISDataMonitor getAisDataMonitor() {
		return aisDataMonitor;
	}

	/**
	 * @param aisDataMonitor the aisDataMonitor to set
	 */
	public void setAisDataMonitor(AISDataMonitor aisDataMonitor) {
		this.aisDataMonitor = aisDataMonitor;
	}
	
	
}
