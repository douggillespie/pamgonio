package pamgoniometer.aisfile;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import PamUtils.LatLong;
import PamUtils.PamCalendar;

/**
 * Other info from a line in an AIS data file that's not included
 * in the standard AIS data unit. 
 * @author dg50
 *
 */
public class AISFileLineInfo {
	
	private long timeMillis;
	private LatLong latLong;
	private int integerId;
	private int hexId;
	

	public AISFileLineInfo(long timeMillis, double lat, double lon, int integerId, int hexId) {
		super();
		this.timeMillis = timeMillis;
		this.latLong = new LatLong(lat, lon);
		this.integerId = integerId;
		this.hexId = hexId;
	}

	public static AISFileLineInfo interpretLine(String aLine) {
		// e.g. 22/06/17,09:20:17.152,pos,63.4436,-20.2695,uid,  215143,2444479,NMEA,!AIVDM,1,1,,A,100=8Ih0?wvS=bFTCGwv4?vR00Rn,0*35
		String[] bits = aLine.split(",");
		long date = 0;
		long time = 0;
		double lat = 0, lon = 0;
		int intId = 0, hexId = 0;
		try {
			date = getDate(bits[0]);
		} catch (ParseException e) {
			System.out.println("Unable to parse date string " + bits[0]);
		}
		try {
			time = getTime(bits[1]);
		} catch (ParseException e) {
			System.out.println("Unable to parse time string " + bits[1]);
		}
		long datetime = date+time;
		try {
			lat = Double.valueOf(bits[3]);
			lon = Double.valueOf(bits[4]);
		}
		catch (NumberFormatException e) {
			System.out.println("Unable to parse lat or long values");
		}
		try {
			intId = Integer.valueOf(bits[6].strip());
			hexId = Integer.decode("0x"+bits[7]);
		}
		catch (NumberFormatException e) {
			System.out.println("Unable to parse data id values");
		}
		
		return new AISFileLineInfo(date+time, lat, lon, intId, hexId);
	}

	/**
	 * Interpret the time string as milliseconds. 
	 * @param string
	 * @return
	 * @throws ParseException 
	 */
	private static long getTime(String string) throws ParseException {
		DateFormat dateInst = new SimpleDateFormat("HH:mm:ss.SSS");
		dateInst.setTimeZone(PamCalendar.defaultTimeZone);
		Date date = dateInst.parse(string);
		return date.getTime();
	}

	/**
	 * Interpret the date string as milliseconds. Hoping
	 * that it's a standard format across machines ? 
	 * @param string
	 * @return
	 * @throws ParseException 
	 */
	private static long getDate(String string) throws ParseException {
		DateFormat dateInst = new SimpleDateFormat("yy/MM/dd");
		dateInst.setTimeZone(PamCalendar.defaultTimeZone);
		Date date = dateInst.parse(string);
		return date.getTime();
	}

	@Override
	public String toString() {
		return String.format("%s,%6d,0x%07X,%s", PamCalendar.formatDBDateTime(timeMillis, true), integerId, hexId, latLong.toString());
	}
}
