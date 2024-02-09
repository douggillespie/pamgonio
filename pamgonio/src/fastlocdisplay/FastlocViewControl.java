package fastlocdisplay;

import java.awt.Frame;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JMenu;
import javax.swing.JMenuItem;

import PamController.PamConfiguration;
import PamController.PamControlledUnit;
import fastlocdisplay.goniometer.GoniometerControl;

public class FastlocViewControl extends PamControlledUnit {
	
	public static final String unitType = "Fastloc Tag Viewer";
	
	private GoniometerControl goniometerControl;
	
	private FastlocViewProcess fastlocViewProcess;
	
	public FastlocViewControl(PamConfiguration pamConfiguration, String unitName) {
		super(pamConfiguration, unitType, unitName);
		fastlocViewProcess = new FastlocViewProcess(this);
		addPamProcess(fastlocViewProcess);
		goniometerControl = new GoniometerControl(this);
	}

	public FastlocViewControl(String unitName) {
		super(unitType, unitName);
	}

	@Override
	public JMenuItem createDetectionMenu(Frame parentFrame) {
		JMenu settingsMenu = new JMenu(getUnitName());
		settingsMenu.add(goniometerControl.getControlMenuItem(parentFrame));
		return settingsMenu;
	}

	/**
	 * @return the fastlocViewProcess
	 */
	public FastlocViewProcess getFastlocViewProcess() {
		return fastlocViewProcess;
	}

	@Override
	public void notifyModelChanged(int changeType) {
		super.notifyModelChanged(changeType);
		goniometerControl.notifyModelChanged(changeType);
	}

	@Override
	public boolean canClose() {
		return super.canClose();
	}

	@Override
	public void pamClose() {
		goniometerControl.destroyEverything();
		super.pamClose();
	}
	

}
