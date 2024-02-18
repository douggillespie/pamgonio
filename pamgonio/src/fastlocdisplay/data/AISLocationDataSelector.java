package fastlocdisplay.data;

import PamView.dialog.PamDialogPanel;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import PamguardMVC.dataSelector.DataSelectParams;
import PamguardMVC.dataSelector.DataSelector;
import PamguardMVC.superdet.SuperDetection;
import fastlocdisplay.FastAISDataBlock;
import fastlocdisplay.FastAISDataUnit;
import fastlocdisplay.FastStationDataUnit;
import fastlocdisplay.FastlocViewProcess;
import pamViewFX.fxSettingsPanes.DynamicSettingsPane;

public class AISLocationDataSelector extends DataSelector {

	private FastAISDataBlock fastAISDataBlock;
	private AISLocationSelectParams selectParams = new AISLocationSelectParams();
	private FastlocViewProcess fastlocViewProcess;
	
	public AISLocationDataSelector(FastlocViewProcess fastlocViewProcess, FastAISDataBlock  fastAISDataBlock, String selectorName) {
		super(fastAISDataBlock, selectorName, false);
		this.fastlocViewProcess = fastlocViewProcess;
		this.fastAISDataBlock = fastAISDataBlock;
	}

	@Override
	public void setParams(DataSelectParams dataSelectParams) {
		if (dataSelectParams instanceof AISLocationSelectParams) {
			this.selectParams = (AISLocationSelectParams) dataSelectParams;
		}
		
	}

	@Override
	public AISLocationSelectParams getParams() {
		return selectParams;
	}

	@Override
	public PamDialogPanel getDialogPanel() {
		return new AISLocDialogPanel(fastlocViewProcess, getParams());
	}

	@Override
	public DynamicSettingsPane<Boolean> getDialogPaneFX() {
		return null;
	}

	@Override
	public double scoreData(PamDataUnit pamDataUnit) {
		FastAISDataUnit fastUnit = (FastAISDataUnit) pamDataUnit;
//		FastStationDataUnit superDet = (FastStationDataUnit) fastUnit.getSuperDetection(FastStationDataUnit.class);
//		if (superDet == null) {
//			return 1;
//		}
		boolean sel = selectParams.isStationSelected(fastUnit.getIntegerId());
		return sel ? 1: 0;
	}


}
