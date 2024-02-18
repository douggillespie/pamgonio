package fastlocdisplay.goniometer.stations;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * information read from a argosid.properties file
 * @author dg50
 *
 */
public class StationsFileData implements Serializable {

	private static final long serialVersionUID = 1L;
	
	protected ArrayList<String> otherLines;
	
	protected ArrayList<StationId> stationIds;
	
	protected String filePath;

	public StationsFileData(String filePath) {
		super();
		this.filePath = filePath;
		otherLines = new ArrayList<>();
		stationIds = new ArrayList<>();
	}
	
	public void addOtherLine(String aLine) {
		otherLines.add(aLine);
	}
	
	public void addStationId(StationId stationId) {
		stationIds.add(stationId);
	}

	/**
	 * @return the serialversionuid
	 */
	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	/**
	 * @return the otherLines
	 */
	public ArrayList<String> getOtherLines() {
		return otherLines;
	}

	/**
	 * @return the stationIds
	 */
	public ArrayList<StationId> getStationIds() {
		return stationIds;
	}

	/**
	 * @return the filePath
	 */
	public String getFilePath() {
		return filePath;
	}
	

}
