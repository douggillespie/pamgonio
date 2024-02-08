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
import fastlocdisplay.swing.FastRTDisplayProvider;
import fastlocdisplay.swing.GoniometerDialog;
import userDisplay.UserDisplayControl;

public class GoniometerControl implements PamSettings{

	private FastlocViewControl fastLocControl;
	
	private GoniometerParams goniometerParams = new GoniometerParams();
	
	private AISFileMonitor aisFileMonitor;
	
	private boolean normalMode;

	private ExtProcessControl processControl;
	
	public GoniometerControl(FastlocViewControl fastLocControl) {
		super();
		this.fastLocControl = fastLocControl;
		PamSettingManager.getInstance().registerSettings(this);
		aisFileMonitor = new AISFileMonitor(fastLocControl.getFastlocViewProcess());
		normalMode = PamController.getInstance().getRunMode() == PamController.RUN_NORMAL;
		UserDisplayControl.addUserDisplayProvider(new FastRTDisplayProvider(this));
		processControl = new ExtProcessControl();
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
			relaunchReceiver();
		}
	}
	
	private void relaunchReceiver() {
		// TODO Auto-generated method stub
		
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
		checkFastRTProcess();
	}
	
	private String getExternalCommand() {
		String exe = goniometerParams.fastGPSFolder + File.separator + goniometerParams.fastGPSexe;
		File exeFile = new File(exe);
		if (exeFile.exists() == false) {
			return null;
		}
		String cmd = String.format("\"%s\" -n %s -o %s -oa %s -d -out \"%s\"", exe, goniometerParams.navPort,
				goniometerParams.gonioComPort, goniometerParams.outPort, goniometerParams.outputDirectory);
		return cmd;
	}

	private void checkFastRTProcess() {
		String command = getExternalCommand();
		if (command == null) {
			return;
		}
		processControl.launchProcess(command);
	}
	
}
