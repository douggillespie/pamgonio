package fastlocdisplay.goniometer;

import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.Serializable;

import javax.swing.JMenuItem;
import javax.swing.SwingUtilities;

import PamController.PamControlledUnitSettings;
import PamController.PamSettings;
import fastlocdisplay.FastlocViewControl;
import fastlocdisplay.aisfile.AISFileMonitor;
import fastlocdisplay.swing.GoniometerDialog;

public class GoniometerControl implements PamSettings{

	private FastlocViewControl fastLocControl;
	
	private GoniometerParams goniometerParams = new GoniometerParams();
	
	private AISFileMonitor aisFileMonitor;

	public GoniometerControl(FastlocViewControl fastLocControl) {
		super();
		this.fastLocControl = fastLocControl;
//		PamSettingManager.getInstance().registerSettings(this);
		aisFileMonitor = new AISFileMonitor(fastLocControl.getFastlocViewProcess());
		SwingUtilities.invokeLater(new Runnable() {
			
			@Override
			public void run() {
				aisFileMonitor.setOptions("C:\\ProjectData\\goniometer\\ArchivedData\\Iceland_2023\\Goniometer\\gonio_playback", true, null);
			}
		});
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
	
}
