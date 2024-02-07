package fastlocdisplay;

import java.util.ArrayList;

import AIS.AISPositionReport;
import PamguardMVC.PamDataUnit;
import PamguardMVC.superdet.SuperDetection;
import fastlocdisplay.aisfile.AISFileLineInfo;

/**
 * Data unit that holds multiple position reports for a single 
 * station. 
 * @author dg50
 *
 */
public class FastStationDataUnit extends SuperDetection<FastAISDataUnit> {

	private int integerId;
	private int hexId;
	
	public FastStationDataUnit(FastAISDataUnit fastAISDataUnit) {
		super(fastAISDataUnit.getTimeMilliseconds());
		integerId = fastAISDataUnit.getIntegerId();
		hexId = fastAISDataUnit.getHexId();
		addSubDetection(fastAISDataUnit);
	}
	
	/**
	 * @return the integerId
	 */
	public int getIntegerId() {
		return integerId;
	}

	/**
	 * @return the hexId
	 */
	public int getHexId() {
		return hexId;
	}

}
