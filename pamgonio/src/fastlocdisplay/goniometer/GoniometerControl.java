package fastlocdisplay.goniometer;

import java.awt.Toolkit;
import java.awt.Window;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.SwingUtilities;

import PamController.PamControlledUnitSettings;
import PamController.PamController;
import PamController.PamSettingManager;
import PamController.PamSettings;
import PamView.ClipboardCopier;
import PamView.dialog.warn.WarnOnce;
import fastlocdisplay.FastlocViewControl;
import fastlocdisplay.FastlocViewProcess;
import fastlocdisplay.aisfile.AISFileMonitor;
import fastlocdisplay.goniometer.stations.StationsFileManager;
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
	
	private StationsFileManager stationsFileManager;
	
	public GoniometerControl(FastlocViewControl fastLocControl) {
		super();
		this.fastLocControl = fastLocControl;
		stationsFileManager = new StationsFileManager(this);
		PamSettingManager.getInstance().registerSettings(this);
		aisFileMonitor = new AISFileMonitor(fastLocControl.getFastlocViewProcess());
		normalMode = PamController.getInstance().getRunMode() == PamController.RUN_NORMAL;
		UserDisplayControl.addUserDisplayProvider(new FastRTDisplayProvider(this));
		processControl = new ExtProcessControl(this);
	}

	public JMenuItem getControlMenuItem(Window parentFrame) {
		JMenu menu = new JMenu("Goniometer");
		
		JMenuItem menuItem = new JMenuItem("Goniometer settings");
		menuItem.setToolTipText("Control Goniometer COM ports and other settings");
		menuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				showGoniometerDialog(parentFrame);
			}
		});
		menu.add(menuItem);

		menu.add(stationsFileManager.getDialogMenuItem(parentFrame));
		
		menuItem = new JMenuItem("Copy FastLoc command line");
		menuItem.setToolTipText("Copy the FastGPS_Realtime launch command to the clipboard");
		menuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				copyCommandLine(parentFrame);
			}
		});
		menu.add(menuItem);
		
		menuItem = new JMenuItem("Kill FastGPS Processes");
		menuItem.setToolTipText("Kill / Destroy any running fastlog GPS processes");
		menuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				killFastLocs();
			}
		});
		menu.add(menuItem);
		
		return menu;
	}

	/**
	 * Kill all FastLoc GPS processes. 
	 */
	protected void killFastLocs() {
		int ans = WarnOnce.showWarning(fastLocControl.getGuiFrame(), "Kill Fastloc", "Are you sure you want to kill all FastGPS_Realtime processes?", WarnOnce.YES_NO_OPTION);
		if (ans == WarnOnce.OK_OPTION) {
			processControl.killAllFastGPSProcesses();
		}
	}

	/**
	 * Copy the goniometer command line to the clipboard to make it
	 * easy for people launching the software manually
	 * @param frame
	 */
	protected void copyCommandLine(Window frame) {
		ArrayList<String> cmds = null;
		try {
			cmds = processControl.generateCommand(true);
		} catch (GoniometerException e) {
//			e.printStackTrace();
			System.out.println("Unable to generate command line");
			return;
		}
		String oneLine = processControl.makeOneLineCommand(cmds);
//		put it in the clipboard
		Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
		StringSelection stringSel = new StringSelection(oneLine);
		clipboard.setContents(stringSel, stringSel);
		WarnOnce.showNamedWarning("goniCommandString", frame, "Command Copied to Clipboard", oneLine, WarnOnce.WARNING_MESSAGE);
	}

	/**
	 * Show the main dialog
	 * @param parentFrame
	 */
	private void showGoniometerDialog(Window parentFrame) {
		GoniometerParams newSettings = GoniometerDialog.showDialog(parentFrame, goniometerParams);
		if (newSettings != null) {
			goniometerParams = newSettings;
			setupGoniometer();
			if (fastRTDisplay != null) {
				fastRTDisplay.updateSettings();
			}
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
		goniometerParams.controlFastRealtime = GoniometerParams.GONIOMETER_NOCONTROL;
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

	public void setupGoniometer() {
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
				if (goniometerParams.controlFastRealtime>0) {
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
		if (goniometerParams.controlFastRealtime == 0) {
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

	/**
	 * @return the processControl
	 */
	public ExtProcessControl getProcessControl() {
		return processControl;
	}

	/**
	 * @return the fastLocControl
	 */
	public FastlocViewControl getFastLocControl() {
		return fastLocControl;
	}
	
	/**
	 * Get the main process, from where it's possible to access datablocks, etc. 
	 * @return
	 */
	public FastlocViewProcess getFastlocViewProcess() {
		return fastLocControl.getFastlocViewProcess();
	}

	/**
	 * Called when the goniometer stations list has been updated. 
	 */
	public void updateStationList() {
		processControl.updateStationsList();
	}
}
