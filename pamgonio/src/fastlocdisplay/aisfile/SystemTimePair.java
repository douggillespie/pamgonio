package fastlocdisplay.aisfile;

import PamUtils.PamCalendar;

/**
 * Data read from a system time file. contains GPS time and System time
 * but seemingly both printed as local time. 
 * @author dg50
 *
 */
public class SystemTimePair {


	private long gpsTime, systemTime;

	public SystemTimePair(long gpsTime, long systemTime) {
		this.gpsTime = gpsTime;
		this.systemTime = systemTime;
	}

	/**
	 * @return the gpsTime
	 */
	public long getGpsTime() {
		return gpsTime;
	}

	/**
	 * @return the systemTime
	 */
	public long getSystemTime() {
		return systemTime;
	}

	@Override
	public String toString() {
		return String.format("GPS Time %s, System Time %s", PamCalendar.formatDBDateTime(gpsTime),  PamCalendar.formatDBDateTime(systemTime));
	}
	
	
	
}
