package fastlocdisplay.data;

import PamguardMVC.PamDataBlock;
import PamguardMVC.dataSelector.DataSelectParams;
import PamguardMVC.dataSelector.DataSelector;
import PamguardMVC.dataSelector.DataSelectorCreator;
import fastlocdisplay.FastStationDataBlock;

public class AISStationDataSelectCreator extends DataSelectorCreator {

	private FastStationDataBlock fastStationDataBlock;

	public AISStationDataSelectCreator(FastStationDataBlock fastStationDataBlock) {
		super(fastStationDataBlock);
		this.fastStationDataBlock = fastStationDataBlock;
	}

	@Override
	public DataSelector createDataSelector(String selectorName, boolean allowScores, String selectorType) {
		return new AISStationDataSelector(fastStationDataBlock, selectorName);
	}

	@Override
	public DataSelectParams createNewParams(String name) {
		// TODO Auto-generated method stub
		return null;
	}

}
