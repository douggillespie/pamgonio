package fastlocdisplay.data;

import PamguardMVC.PamDataBlock;
import PamguardMVC.dataSelector.DataSelectParams;
import PamguardMVC.dataSelector.DataSelector;
import PamguardMVC.dataSelector.DataSelectorCreator;
import fastlocdisplay.FastAISDataBlock;
import fastlocdisplay.FastlocViewProcess;
import fastlocdisplay.swing.FastAISLocationsOverlay;

public class AISLocationDataSelectCreator extends DataSelectorCreator {

	private FastAISDataBlock fastAISDataBlock;
	private FastlocViewProcess fastlocViewProcess;
	
	public AISLocationDataSelectCreator(FastlocViewProcess fastlocViewProcess, FastAISDataBlock fastAISDataBlock) {
		super(fastAISDataBlock);
		this.fastlocViewProcess = fastlocViewProcess;
		this.fastAISDataBlock = fastAISDataBlock;
	}

	@Override
	public DataSelector createDataSelector(String selectorName, boolean allowScores, String selectorType) {
		return new AISLocationDataSelector(fastlocViewProcess, fastAISDataBlock, selectorName);
	}

	@Override
	public AISLocationSelectParams createNewParams(String name) {
		return new AISLocationSelectParams();
	}

}
