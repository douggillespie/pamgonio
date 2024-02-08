package fastlocdisplay.swing;

import fastlocdisplay.goniometer.GoniometerControl;
import userDisplay.UserDisplayComponent;
import userDisplay.UserDisplayControl;
import userDisplay.UserDisplayProvider;

public class FastRTDisplayProvider implements UserDisplayProvider {

	private GoniometerControl goniometerControl;
	
	public FastRTDisplayProvider(GoniometerControl goniometerControl) {
		super();
		this.goniometerControl = goniometerControl;
	}

	@Override
	public String getName() {
		return "Goniometer data";
	}

	@Override
	public UserDisplayComponent getComponent(UserDisplayControl userDisplayControl, String uniqueDisplayName) {
		return new FastRTDisplay(goniometerControl);
	}

	@Override
	public Class getComponentClass() {
		return FastRTDisplay.class;
	}

	@Override
	public int getMaxDisplays() {
		return 1;
	}

	@Override
	public boolean canCreate() {
		return true;
	}

	@Override
	public void removeDisplay(UserDisplayComponent component) {
		// TODO Auto-generated method stub

	}

}
