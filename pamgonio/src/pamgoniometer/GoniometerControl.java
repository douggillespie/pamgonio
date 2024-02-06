package pamgoniometer;

import PamController.PamConfiguration;
import PamController.PamControlledUnit;

public class GoniometerControl extends PamControlledUnit {
	
	public static final String unitType = "Goniometer";

	public GoniometerControl(PamConfiguration pamConfiguration, String unitName) {
		super(pamConfiguration, unitType, unitName);
	}

	public GoniometerControl(String unitName) {
		super(unitType, unitName);
	}

}
