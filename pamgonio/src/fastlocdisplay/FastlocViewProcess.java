package fastlocdisplay;

import java.util.ArrayList;

import PamController.PamControlledUnit;
import PamController.PamController;
import PamController.PamViewParameters;
import PamguardMVC.PamProcess;
import fastlocdisplay.aisfile.AISDataMonitor;
import fastlocdisplay.aisfile.AISFileLineInfo;
import fastlocdisplay.data.AISLocationDataSelectCreator;
import fastlocdisplay.data.AISStationDataSelectCreator;
import fastlocdisplay.io.FastAISLogging;
import fastlocdisplay.swing.FastAISLocationsOverlay;
import fastlocdisplay.swing.FastAISStationsOverlay;
import fastlocdisplay.swing.FastLocationsSymbolManager;
import fastlocdisplay.swing.FastStationsSymbolManager;

public class FastlocViewProcess extends PamProcess implements AISDataMonitor {

	private FastlocViewControl fastlocViewControl;
	
	private FastStationDataBlock fastStationDataBlock;
	
	private FastAISDataBlock fastAISDataBlock;

	private FastAISLogging fastAISLogging;

	public FastlocViewProcess(FastlocViewControl fastlocViewControl) {
		super(fastlocViewControl, null);
		this.fastlocViewControl = fastlocViewControl;
		
		fastAISDataBlock = new FastAISDataBlock(this);
		addOutputDataBlock(fastAISDataBlock);
		fastAISDataBlock.SetLogging(fastAISLogging = new FastAISLogging(fastAISDataBlock));
		fastAISDataBlock.setOverlayDraw(new FastAISLocationsOverlay());
		fastAISDataBlock.setPamSymbolManager(new FastLocationsSymbolManager(fastAISDataBlock));
		fastAISDataBlock.setDataSelectCreator(new AISLocationDataSelectCreator(this, fastAISDataBlock));
		
		fastStationDataBlock = new FastStationDataBlock(this);
		addOutputDataBlock(fastStationDataBlock);
//		fastStationDataBlock.setOverlayDraw(new FastAISStationsOverlay());
		fastStationDataBlock.setPamSymbolManager(new FastStationsSymbolManager(fastStationDataBlock));
		fastStationDataBlock.setDataSelectCreator(new AISStationDataSelectCreator(fastStationDataBlock));
	}

	@Override
	public void pamStart() {
		// TODO Auto-generated method stub

	}

	@Override
	public void pamStop() {
		// TODO Auto-generated method stub

	}

	@Override
	public void newAISData(AISFileLineInfo aisDataLine) {
		/**
		 * May need to do some work here to check that data are not already loaded from database
		 * Will also need to think more about how the database will work. May have to make a dataunit
		 * for each of the position reports and have them in a separate data block, then added to the 
		 * station datablocks as sub detections. This will make databasing easier and will also make
		 * mouse hovers easier in the map and any other displays. 
		 */
		if (aisDataLine.getErrorCode() > 0) {
			return;
		}
		// get the last time for that station from the database. 
		long lastStationTime = 0;
		FastAISDataUnit lastData = fastAISLogging.getLastDataUnit(aisDataLine.getIntegerId());
		if (lastData != null) {
			lastStationTime = lastData.getTimeMilliseconds();
			if (aisDataLine.getTimeMillis() <= lastStationTime) {
				// this record should already be in the database, so don't do anything. 
				return;
			}
		}
		
		FastAISDataUnit aisDataUnit = new FastAISDataUnit(aisDataLine);
		fastAISDataBlock.addPamData(aisDataUnit);
		linkToStation(aisDataUnit);
	}

	@Override
	public void notifyModelChanged(int changeType) {
		super.notifyModelChanged(changeType);
		switch (changeType) {
		case PamController.INITIALIZATION_COMPLETE:
			fastAISLogging.loadEarlyData(new PamViewParameters(0, System.currentTimeMillis()));
			fastAISDataBlock.sortData();
			relinkStations();
			break;
		case PamController.DATA_LOAD_COMPLETE:
			fastAISDataBlock.sortData();
			relinkStations();
		}
	}

	/**
	 * Called after a load of offline data to relink the AIS reports
	 * to stations. 
	 */
	private void relinkStations() {
		fastStationDataBlock.clearAll();
		ArrayList<FastAISDataUnit> data = fastAISDataBlock.getDataCopy();
		for (FastAISDataUnit aData : data) {
			linkToStation(aData);
		}
	}
	
	private void linkToStation(FastAISDataUnit aisDataUnit) {
		FastStationDataUnit exUnit = fastStationDataBlock.findStationDataUnit(aisDataUnit.getIntegerId());
		if (exUnit == null) {
			exUnit = new FastStationDataUnit(aisDataUnit);
			fastStationDataBlock.addPamData(exUnit);
		}
		else {
			exUnit.addSubDetection(aisDataUnit);
			fastStationDataBlock.updatePamData(exUnit, aisDataUnit.getTimeMilliseconds());
		}
	}

	/**
	 * @return the fastlocViewControl
	 */
	public FastlocViewControl getFastlocViewControl() {
		return fastlocViewControl;
	}

	/**
	 * @return the fastStationDataBlock
	 */
	public FastStationDataBlock getFastStationDataBlock() {
		return fastStationDataBlock;
	}

	/**
	 * @return the fastAISDataBlock
	 */
	public FastAISDataBlock getFastAISDataBlock() {
		return fastAISDataBlock;
	}

	/**
	 * @return the fastAISLogging
	 */
	public FastAISLogging getFastAISLogging() {
		return fastAISLogging;
	}

}
