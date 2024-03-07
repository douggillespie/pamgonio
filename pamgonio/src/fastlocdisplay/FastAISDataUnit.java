package fastlocdisplay;

import java.util.ArrayList;

import AIS.AISPositionReport;
import PamController.PamController;
import PamController.masterReference.MasterReferencePoint;
import PamUtils.LatLong;
import PamUtils.PamCalendar;
import PamUtils.PamUtils;
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
		String txt = String.format("<html><strong>%s</strong><br>Fastloc location", PamCalendar.formatDBDateTime(getTimeMilliseconds()));
		if (PamController.getInstance().getRunMode() == PamController.RUN_NORMAL) {
			long ago = System.currentTimeMillis() - getTimeMilliseconds();
			txt += String.format("<br>%s ago", PamCalendar.formatDuration(ago));
			if (isLast() ) {
				txt += " (latest from station)";
			}
		}
		txt += String.format("<br>Id %d, 0x%X", integerId, hexId);
		LatLong ll = positionReport.latLong;
		txt += String.format("<br>%s, %s",ll.formatLatitude(),ll.formatLongitude());
		// try to get a GPS location and a range. 
		LatLong masterLatLong = MasterReferencePoint.getLatLong();
		if (masterLatLong != null) {
			double range = masterLatLong.distanceToMetres(ll);
			double bearing = masterLatLong.bearingTo(ll);
			txt += String.format("<br>Range %3.0fm, Bearing %3.0f%sT", range, bearing, LatLong.deg);
		}
		
		return txt;
	}
	/**
	 * See if it's the last in the chain for this station
	 * @return true if it's the last one. 
	 */
	public boolean isLast() {
		SuperDetection superDet = null;
		synchronized (getSuperDetectionSyncronisation()) {
			superDet = getSuperDetection(FastStationDataUnit.class);
		}
		if (superDet == null) {
			return false;
		}
		synchronized (superDet.getSubDetectionSyncronisation()) {
			ArrayList<PamDataUnit> subDets = superDet.getSubDetections();
			if (subDets == null || subDets.size() == 0) {
				return false;
			}
			PamDataUnit lastSub = subDets.get(subDets.size()-1);
			return lastSub == this;
		}
	}


}
