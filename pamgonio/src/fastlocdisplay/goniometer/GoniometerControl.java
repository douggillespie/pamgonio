package fastlocdisplay.goniometer;

import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.Serializable;

import javax.swing.JMenuItem;
import javax.swing.SwingUtilities;

import PamController.PamControlledUnitSettings;
import PamController.PamController;
import PamController.PamSettingManager;
import PamController.PamSettings;
import fastlocdisplay.FastlocViewControl;
import fastlocdisplay.aisfile.AISFileMonitor;
import fastlocdisplay.swing.FastRTDisplay;
import fastlocdisplay.swing.FastRTDisplayProvider;
import fastlocdisplay.swing.GoniometerDialog;
import userDisplay.UserDisplayControl;

public class GoniometerControl implements PamSettings, ProcessMonitor {

	private FastlocViewControl fastLocControl;
	
	private GoniometerParams goniometerParams = new GoniometerParams();
	
	private AISFileMonitor aisFileMonitor;
	
	private boolean normalMode;

	private ExtProcessControl processControl;
	
	private FastRTDisplay fastRTDisplay;
	
	private volatile boolean destroying = false;
	
	public GoniometerControl(FastlocViewControl fastLocControl) {
		super();
		this.fastLocControl = fastLocControl;
		PamSettingManager.getInstance().registerSettings(this);
		aisFileMonitor = new AISFileMonitor(fastLocControl.getFastlocViewProcess());
		normalMode = PamController.getInstance().getRunMode() == PamController.RUN_NORMAL;
		UserDisplayControl.addUserDisplayProvider(new FastRTDisplayProvider(this));
		processControl = new ExtProcessControl(this);
	}

	public JMenuItem getControlMenuItem(Window parentFrame) {
		JMenuItem menuItem = new JMenuItem("Goniometer Control");
		menuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				showGoniometerDialog(parentFrame);
			}
		});
		return menuItem;
	}

	private void showGoniometerDialog(Window parentFrame) {
		GoniometerParams newSettings = GoniometerDialog.showDialog(parentFrame, goniometerParams);
		if (newSettings != null) {
			goniometerParams = newSettings;
			setupGoniometer();
		}
	}


	@Override
	public String getUnitName() {
		return fastLocControl.getUnitName();
	}

	@Override
	public String getUnitType() {
		return "Goniometer Control";
	}

	@Override
	public Serializable getSettingsReference() {
		return goniometerParams;
	}

	@Override
	public long getSettingsVersion() {
		return GoniometerParams.serialVersionUID;
	}

	@Override
	public boolean restoreSettings(PamControlledUnitSettings pamControlledUnitSettings) {
		this.goniometerParams = (GoniometerParams) pamControlledUnitSettings.getSettings();
		return goniometerParams != null;
	}

	/**
	 * Passed through from main controlled unit. 
	 * @param changeType
	 */
	public void notifyModelChanged(int changeType) {
		if (changeType == PamController.INITIALIZATION_COMPLETE) {
			initialise();
		}
		if (changeType == PamController.DESTROY_EVERYTHING) {
			destroyEverything();
		}
	}

	public void destroyEverything() {
		destroying = true;
		try {
			processControl.stopProcess();
		} catch (GoniometerException e) {
			e.printStackTrace();
		}
	}

	private void initialise() {
		if (normalMode) {
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					setupGoniometer();
				}
			});
		}
	}

	protected void setupGoniometer() {
		aisFileMonitor.setOptions(goniometerParams.outputDirectory, goniometerParams.autoDatedFolders, null);
		try {
			processControl.stopProcess();
		} catch (GoniometerException e) {
			if (fastRTDisplay != null) {
				fastRTDisplay.goniometerMessage("Exception", e.getMessage());
			}
		}
		Thread t = new Thread(new FastGPSMonitor());
		t.start();
	}
	
	private class FastGPSMonitor implements Runnable {

		@Override
		public void run() {
			while (destroying == false) {
				if (goniometerParams.controlFastRealtime) {
					checkFastRTProcess();
				}
				try {
					Thread.sleep(5000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		
	}
	
	private void checkFastRTProcess() {
		if (goniometerParams.controlFastRealtime == false) {
			return;
		}
		try {
			if (processControl.processRunning() == false) {
				processControl.relaunchProcess();
			}
		} catch (GoniometerException e) {
			if (fastRTDisplay != null) {
				fastRTDisplay.goniometerMessage("Exception", e.getMessage());
			}
		}
	}

	/**
	 * @return the goniometerParams
	 */
	public GoniometerParams getGoniometerParams() {
		return goniometerParams;
	}

	@Override
	public void errorLine(String line) {
		if (fastRTDisplay != null) {
			fastRTDisplay.goniometerMessage("Error", line);
		}
	}

	@Override
	public void inputLine(String line) {
		if (fastRTDisplay != null) {
			fastRTDisplay.goniometerMessage("Input", line);
		}
		
	}
	
	@Override
	public void systemLine(String line) {
		if (fastRTDisplay != null) {
			fastRTDisplay.goniometerMessage("System", line);
		}
		
	}

	/**
	 * @return the fastRTDisplay
	 */
	public FastRTDisplay getFastRTDisplay() {
		return fastRTDisplay;
	}

	/**
	 * @param fastRTDisplay the fastRTDisplay to set
	 */
	public void setFastRTDisplay(FastRTDisplay fastRTDisplay) {
		this.fastRTDisplay = fastRTDisplay;
	}
	
}
