package fastlocdisplay;

import AIS.AISPositionReport;
import PamController.PamController;
import PamUtils.LatLong;
import PamUtils.PamCalendar;
import PamguardMVC.PamDataUnit;
import PamguardMVC.superdet.SuperDetection;
import fastlocdisplay.aisfile.AISFileLineInfo;

/**
 * Class to hold Fastloc AIS position reports. Mostly accessed through their
 * super detections. 
 * @author dg50
 *
 */
public class FastAISDataUnit extends PamDataUnit<PamDataUnit, FastStationDataUnit> {

	private int integerId;
	private int hexId;
	private AISPositionReport positionReport;
	
	public FastAISDataUnit(AISFileLineInfo fileLineInfo) {
		super(fileLineInfo.getTimeMillis());
		this.integerId = fileLineInfo.getIntegerId();
		this.hexId = fileLineInfo.getHexId();
		this.positionReport = fileLineInfo.getPositionReport();
		this.setChannelBitmap(integerId);
	}

	public FastAISDataUnit(long timeMilliseconds, int integerId, int hexId, AISPositionReport positionReport) {
		super(timeMilliseconds);
		this.integerId = integerId;
		this.hexId = hexId;
		this.positionReport = positionReport;
		this.setChannelBitmap(integerId);
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

	/**
	 * @return the positionReport
	 */
	public AISPositionReport getPositionReport() {
		return positionReport;
	}

	@Override
	public String getSummaryString() {
		FastStationDataUnit superDet = (FastStationDataUnit) this.getSuperDetection(FastStationDataUnit.class);
		String txt = String.format("<html>Fastloc location %s", PamCalendar.formatDBDateTime(getTimeMilliseconds()));
		if (PamController.getInstance().getRunMode() == PamController.RUN_NORMAL) {
			long ago = System.currentTimeMillis() - getTimeMilliseconds();
			txt += String.format("<br>%s ago", PamCalendar.formatDuration(ago));
		}
		txt += String.format("<br>Id %d, 0X%X", integerId, hexId);
		LatLong ll = positionReport.latLong;
		txt += String.format("<br>%s, %s",ll.formatLatitude(),ll.formatLongitude());
		
		return txt;
	}


}
