package fastlocdisplay.swing;

import java.awt.Color;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ButtonGroup;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.Timer;
import javax.swing.border.TitledBorder;

import PamController.PamController;
import PamView.dialog.PamGridBagContraints;
import PamView.panel.PamPanel;
import PamView.panel.WestAlignedPanel;
import PamguardMVC.PamDataUnit;
import PamguardMVC.PamObservable;
import PamguardMVC.PamObserver;
import PamguardMVC.PamObserverAdapter;
import fastlocdisplay.FastAISDataBlock;
import fastlocdisplay.FastAISDataUnit;
import fastlocdisplay.goniometer.GoniometerControl;
import fastlocdisplay.goniometer.GoniometerParams;

public class FastRTSummaryPanel {

	private GoniometerControl goniometerControl;
	
	private JPanel mainPanel;
	
	private JPanel leftPanel;

	private JLabel exeSummary;
	
	private JLabel lastPosition;
	
	private JRadioButton[] ctrlButtons;
	
	private Color warningCol = Color.ORANGE;
	
	private Color okCol;

	private FastAISDataUnit latestDataUnit;

	private FastAISDataBlock aisDataBlock;
	
	public FastRTSummaryPanel(GoniometerControl goniometerControl) {
		super();
		this.goniometerControl = goniometerControl;
		aisDataBlock = goniometerControl.getFastlocViewProcess().getFastAISDataBlock();
		aisDataBlock.addObserver(new DataObserver());
		
		mainPanel = new JPanel(new GridBagLayout());
		okCol = mainPanel.getBackground();
		GridBagConstraints c = new PamGridBagContraints();
		
		ctrlButtons = new JRadioButton[3];
		ButtonGroup bg = new ButtonGroup();
		for (int i = 0; i < 3; i++) {
			ctrlButtons[i] = new JRadioButton(GoniometerParams.getControlName(i));
			bg.add(ctrlButtons[i]);
			ctrlButtons[i].addActionListener(new ControlTypeAction(i));
			c.gridwidth = 2;
			if (PamController.getInstance().getRunMode() != PamController.RUN_PAMVIEW) {
				mainPanel.add(ctrlButtons[i], c);
				c.gridy++;
			}
		}

		c.gridwidth = 1;
		mainPanel.add(new JLabel("Fast GPS Status: ", JLabel.RIGHT), c);
		c.gridx++;
		mainPanel.add(exeSummary = new JLabel(""), c);
		c.gridx = 0;
		c.gridy++;
		mainPanel.add(new JLabel("Last position: ", JLabel.RIGHT), c);
		c.gridx++;
		mainPanel.add(lastPosition = new JLabel(""), c);
		
		if (PamController.getInstance().getRunMode() == PamController.RUN_NORMAL) {
			Timer timer = new Timer(3000, new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					checkSoftwareStatus();
				}

			});
			timer.start();
		}
		
		leftPanel = new WestAlignedPanel(mainPanel);
		leftPanel.setBorder(new TitledBorder("Goniometer status"));
		
		getLatestDataUnit();
		
		updateSettings();
	}

	/**
	 * Set the type of fastloc control - none, internal, or external
	 * @param controlType
	 */
	protected void setControlType(int controlType) {
		goniometerControl.getGoniometerParams().controlFastRealtime = controlType;
		goniometerControl.setupGoniometer();
	}

	/**
	 * Called once in constructor to get latest data unit, whichmay
	 * be from the database, not incoming ...
	 */
	private void getLatestDataUnit() {
		if (aisDataBlock == null) {
			return;
		}
		FastAISDataUnit latest = aisDataBlock.getLastUnit();
		updateLatestUnit(latest);
	}

	public void updateLatestUnit(FastAISDataUnit latestDataUnit) {
		this.latestDataUnit = latestDataUnit;
		if (latestDataUnit == null) {
			lastPosition.setText(null);
		}
		else {
			String summary = latestDataUnit.getSummaryString();
			summary = summary.replace("<br>", "; ");
			lastPosition.setText(summary);
		}
	}

	/**
	 * @return the mainPanel
	 */
	public JPanel getMainPanel() {
		return leftPanel;
	}

	private void checkSoftwareStatus() {
		String summary = goniometerControl.getProcessControl().getProcessSummary();
		if (summary == null) {
			exeSummary.setText("Unable to find running process for " + goniometerControl.getGoniometerParams().fastGPSexe);
		}
		else {
			exeSummary.setText(summary);
		}
		setWarningColour(exeSummary == null);
		getLatestDataUnit(); // may as well update this too
	}
	
	public void setWarningColour(boolean inError) {
		Color col = inError ? Color.RED : okCol;
		colourPanels(mainPanel, col);
	}

	private void colourPanels(JComponent component, Color col) {
		component.setBackground(col);
		int nChild = component.getComponentCount();
		for (int i = 0; i < nChild; i++) {
			Component aChild = component.getComponent(i);
			if (aChild instanceof JComponent) {
				colourPanels((JComponent) aChild, col);
			}
		}
		
	}
	
	private class ControlTypeAction implements ActionListener {
	
		int actionType;
		
		
		public ControlTypeAction(int actionType) {
			super();
			this.actionType = actionType;
		}
	
	
		@Override
		public void actionPerformed(ActionEvent e) {
			setControlType(actionType);
		}
		
	}

	private class DataObserver extends PamObserverAdapter {

		@Override
		public String getObserverName() {
			return "Fastloc Summary Panel";
		}

		@Override
		public void addData(PamObservable observable, PamDataUnit pamDataUnit) {
			if (pamDataUnit instanceof FastAISDataUnit) {
				latestDataUnit = (FastAISDataUnit) pamDataUnit;
				updateLatestUnit(latestDataUnit);
			}
		}
		
	}

	public void updateSettings() {
		GoniometerParams params = goniometerControl.getGoniometerParams();
		for (int i = 0; i < ctrlButtons.length; i++) {
			ctrlButtons[i].setSelected(params.controlFastRealtime == i);
		}
	}
	
}
