package fastlocdisplay.data;

import javax.swing.JComponent;
import javax.swing.JPanel;

import PamView.dialog.PamDialogPanel;
import fastlocdisplay.FastlocViewProcess;

public class AISLocDialogPanel implements PamDialogPanel {
	
//	private JPanel mainPanel;
	private FastlocViewProcess fastlocViewProcess;
	private AISLocationSelectParams aisLocationSelectParams;
	private AISSelDialogPanel stationDialogPanel;
	
	public AISLocDialogPanel(FastlocViewProcess fastlocViewProcess, AISLocationSelectParams aisLocationSelectParams) {
		this.fastlocViewProcess = fastlocViewProcess;
		this.aisLocationSelectParams = aisLocationSelectParams;
//		mainPanel = new JPanel();
		stationDialogPanel = new AISSelDialogPanel(fastlocViewProcess.getFastStationDataBlock(), aisLocationSelectParams);
	}

	@Override
	public JComponent getDialogComponent() {
		return stationDialogPanel.getDialogComponent();
	}

	@Override
	public void setParams() {
		stationDialogPanel.setParams();
	}

	@Override
	public boolean getParams() {
		return stationDialogPanel.getParams();
	}

}
