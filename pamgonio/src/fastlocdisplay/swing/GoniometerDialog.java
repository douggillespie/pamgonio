package fastlocdisplay.swing;

import java.awt.Window;

import PamView.dialog.PamDialog;
import fastlocdisplay.goniometer.GoniometerParams;

public class GoniometerDialog extends PamDialog {
	
	private static final long serialVersionUID = 1L;
	private static GoniometerDialog singleInstance;
	private GoniometerParams goniometerParams;

	private GoniometerDialog(Window parentFrame) {
		super(parentFrame, "Goniometer Settings", true);
		
	}
	
	public static GoniometerParams showDialog(Window parentFrame, GoniometerParams goniometerParams) {
		if (singleInstance == null) {
			singleInstance = new GoniometerDialog(parentFrame);
		}
		singleInstance.goniometerParams = goniometerParams;
		singleInstance.setParams();
		singleInstance.setVisible(true);
		return singleInstance.goniometerParams;
	}

	private void setParams() {
		
	}

	@Override
	public boolean getParams() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void cancelButtonPressed() {
		// TODO Auto-generated method stub

	}

	@Override
	public void restoreDefaultSettings() {
		// TODO Auto-generated method stub

	}

}
