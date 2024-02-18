package fastlocdisplay.goniometer.stations;

import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JMenuItem;

import PamView.dialog.GenericSwingDialog;
import PamView.dialog.PamDialog;
import PamView.dialog.PamDialogPanel;
import PamView.dialog.warn.WarnOnce;
import fastlocdisplay.goniometer.GoniometerControl;
import fastlocdisplay.goniometer.GoniometerException;

/**
 * Functions for managing stations file. 
 * @author dg50
 *
 */
public class StationsFileManager {

	private GoniometerControl goniometerControl;
	
	private GoniometerStationsFile goniometerStationsFile;

	public StationsFileManager(GoniometerControl goniometerControl) {
		super();
		this.goniometerControl = goniometerControl;
		goniometerStationsFile = new GoniometerStationsFile();
	}
	
	public JMenuItem getDialogMenuItem(Window owner) {
		JMenuItem menuItem = new JMenuItem("Argos stations ...");
		menuItem.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				showDialog(owner);
			}
		});
		return menuItem;
	}

	protected void showDialog(Window owner) {
		String path = goniometerControl.getGoniometerParams().fastGPSFolder;
		StationsFileData fileData = null;
		try {
			fileData = goniometerStationsFile.readFile(path);
		} catch (GoniometerException e) {
			e.printStackTrace();
			return;
		}
		
		StationsDialogPanel dialogPanel = new StationsDialogPanel(fileData);
		PamDialogPanel[] panels = {dialogPanel};
		dialogPanel.setParams();
		boolean ok = GenericSwingDialog.showDialog(owner, "Argos Stations", panels);
		if (ok) {
			// need to rewrite the file.
			// first write a temp file, then rename to the old one. 
			boolean ok2 = goniometerStationsFile.reWriteFile(path, fileData);
			if (ok2 == false) {
				WarnOnce.showWarning("Argos stations", "Unable to rewrite the Argos Stations file", WarnOnce.WARNING_MESSAGE);
			}
			else {
				goniometerControl.updateStationList();
			}
		}
	}
	
}
