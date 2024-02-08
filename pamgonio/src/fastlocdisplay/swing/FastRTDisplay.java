package fastlocdisplay.swing;

import java.awt.Component;

import javax.swing.JPanel;

import PamView.panel.PamPanel;
import fastlocdisplay.goniometer.GoniometerControl;
import userDisplay.UserDisplayComponent;

public class FastRTDisplay implements UserDisplayComponent {

	private GoniometerControl goniometerControl;
	
	private JPanel mainPanel;

	private String uniqueName;
	
	public FastRTDisplay(GoniometerControl gniometerControl) {
		super();
		this.goniometerControl = gniometerControl;
		mainPanel = new PamPanel();
	}

	@Override
	public Component getComponent() {
		return mainPanel;
	}

	@Override
	public void openComponent() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void closeComponent() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void notifyModelChanged(int changeType) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getUniqueName() {
		return uniqueName;
	}

	@Override
	public void setUniqueName(String uniqueName) {
		this.uniqueName = uniqueName;
	}

	@Override
	public String getFrameTitle() {
		return "Goniometer data";
	}


}
