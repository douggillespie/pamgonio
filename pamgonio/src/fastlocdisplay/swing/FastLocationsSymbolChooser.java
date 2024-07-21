package fastlocdisplay.swing;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

import PamView.GeneralProjector;
import PamView.dialog.PamGridBagContraints;
import PamView.symbol.PamSymbolOptions;
import PamView.symbol.StandardSymbolChooser;
import PamView.symbol.StandardSymbolManager;
import PamView.symbol.StandardSymbolOptions;
import PamView.symbol.StandardSymbolOptionsPanel;
import PamView.symbol.SwingSymbolOptionsPanel;
import PamView.symbol.SymbolData;
import PamguardMVC.PamDataBlock;

public class FastLocationsSymbolChooser extends StandardSymbolChooser {
	
	private FastLocSymbolOptions fastLocSymbolOptions = new FastLocSymbolOptions(FastAISStationsOverlay.defaultSymbol.getSymbolData());

	public FastLocationsSymbolChooser(StandardSymbolManager standardSymbolManager, PamDataBlock pamDataBlock,
			String displayName, SymbolData defaultSymbol, GeneralProjector projector) {
		super(standardSymbolManager, pamDataBlock, displayName, defaultSymbol, projector);
		// TODO Auto-generated constructor stub
	}

	@Override
	public SwingSymbolOptionsPanel getSwingOptionsPanel(GeneralProjector projector) {
		SwingSymbolOptionsPanel panel = new FastLocSymbolPanel(getSymbolManager(), this);
		
		
		return panel;
	}

	@Override
	public FastLocSymbolOptions getSymbolOptions() {
		if (super.getSymbolOptions() instanceof FastLocSymbolOptions) {
			return (FastLocSymbolOptions) super.getSymbolOptions();
		}
		else {
			return new FastLocSymbolOptions(getDefaultSymbol());
		}
	}

	@Override
	public void setSymbolOptions(PamSymbolOptions symbolOptions) {
		if (symbolOptions instanceof FastLocSymbolOptions) {
			super.setSymbolOptions(symbolOptions);
		}
		else {
			super.setSymbolOptions(new FastLocSymbolOptions(getDefaultSymbol()));
		}
	}
	
	private class FastLocSymbolPanel extends StandardSymbolOptionsPanel {

		private JCheckBox linkLines = new JCheckBox("Link fixes for each station");;
		private JCheckBox vesselLines = new JCheckBox("Draw lines from vessel track");
		
		public FastLocSymbolPanel(StandardSymbolManager standardSymbolManager,
				StandardSymbolChooser standardSymbolChooser) {
			super(standardSymbolManager, standardSymbolChooser);
			
			
			JPanel optsPanel = new JPanel(new GridBagLayout());
			GridBagConstraints c = new PamGridBagContraints();
			optsPanel.setBorder(new TitledBorder("Fastloc display options"));
			optsPanel.add(linkLines, c);
			c.gridy++;
			optsPanel.add(vesselLines, c);
			this.getDialogComponent().add(optsPanel);
			
		}

		@Override
		public void setParams() {
			if (linkLines == null) {
				return;
			}
			linkLines.setSelected(getSymbolOptions().drawLinkLines);
			vesselLines.setSelected(getSymbolOptions().drawVesselLines);
			super.setParams();
		}

		@Override
		public boolean getParams() {
			getSymbolOptions().drawLinkLines = linkLines.isSelected();
			getSymbolOptions().drawVesselLines = vesselLines.isSelected();
			return super.getParams();
		}
		
	}
	

}
