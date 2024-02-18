package fastlocdisplay.data;

import PamView.dialog.PamDialogPanel;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import PamguardMVC.dataSelector.DataSelectParams;
import PamguardMVC.dataSelector.DataSelector;
import fastlocdisplay.FastStationDataBlock;
import fastlocdisplay.FastStationDataUnit;
import pamViewFX.fxSettingsPanes.DynamicSettingsPane;

public class AISStationDataSelector extends DataSelector {

	private FastStationDataBlock fastStationDataBlock;

	private AISStationSelectParams selectParams = new AISStationSelectParams();
	
	public AISStationDataSelector(FastStationDataBlock fastStationDataBlock, String selectorName) {
		super(fastStationDataBlock, selectorName, false);
		this.fastStationDataBlock = fastStationDataBlock;
	}

	@Override
	public void setParams(DataSelectParams dataSelectParams) {
		if (dataSelectParams instanceof AISStationSelectParams) {
			selectParams = (AISStationSelectParams) dataSelectParams;
		}
	}

	@Override
	public DataSelectParams getParams() {
		return selectParams;
	}

	@Override
	public PamDialogPanel getDialogPanel() {
		return new AISSelDialogPanel(fastStationDataBlock, selectParams);
	}

	@Override
	public DynamicSettingsPane<Boolean> getDialogPaneFX() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public double scoreData(PamDataUnit pamDataUnit) {
		if (selectParams == null) {
			return 1;
		}
		FastStationDataUnit station = (FastStationDataUnit) pamDataUnit;
		boolean sel = selectParams.isStationSelected(station.getIntegerId());
		return sel ? 1 : 0;
	}

}
