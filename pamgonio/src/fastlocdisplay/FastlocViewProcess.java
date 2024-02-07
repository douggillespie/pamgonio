package fastlocdisplay;

import PamController.PamControlledUnit;
import PamguardMVC.PamProcess;
import fastlocdisplay.aisfile.AISDataMonitor;
import fastlocdisplay.aisfile.AISFileLineInfo;
import fastlocdisplay.data.AISStationDataSelectCreator;
import fastlocdisplay.io.FastAISLogging;
import fastlocdisplay.swing.FastAISStationsOverlay;
import fastlocdisplay.swing.FastAISSymbolManager;

public class FastlocViewProcess extends PamProcess implements AISDataMonitor {

	private FastlocViewControl fastlocViewControl;
	
	private FastStationDataBlock fastStationDataBlock;
	
	private FastAISDataBlock fastAISDataBlock;

	public FastlocViewProcess(FastlocViewControl fastlocViewControl) {
		super(fastlocViewControl, null);
		this.fastlocViewControl = fastlocViewControl;
		
		fastAISDataBlock = new FastAISDataBlock(this);
		addOutputDataBlock(fastAISDataBlock);
		fastAISDataBlock.SetLogging(new FastAISLogging(fastAISDataBlock));
		
		fastStationDataBlock = new FastStationDataBlock(this);
		addOutputDataBlock(fastStationDataBlock);
		fastStationDataBlock.setOverlayDraw(new FastAISStationsOverlay());
		fastStationDataBlock.setPamSymbolManager(new FastAISSymbolManager(fastStationDataBlock));
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
		FastAISDataUnit aisDataUnit = new FastAISDataUnit(aisDataLine);
		fastAISDataBlock.addPamData(aisDataUnit);
		FastStationDataUnit exUnit = fastStationDataBlock.findStationDataUnit(aisDataLine.getIntegerId());
		if (exUnit == null) {
			exUnit = new FastStationDataUnit(aisDataUnit);
			fastStationDataBlock.addPamData(exUnit);
		}
		else {
			exUnit.addSubDetection(aisDataUnit);
			fastStationDataBlock.updatePamData(exUnit, aisDataLine.getTimeMillis());
		}
	}

}
