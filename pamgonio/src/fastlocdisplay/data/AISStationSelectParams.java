package fastlocdisplay.data;

import java.io.Serializable;
import java.util.HashMap;

import PamguardMVC.dataSelector.DataSelectParams;

public class AISStationSelectParams extends DataSelectParams implements Serializable, Cloneable {
	
	private HashMap<Integer, Boolean> stationSelection = new HashMap<>();

	private static final long serialVersionUID = 1L;
	
	public HashMap<Integer, Boolean> getStationMap() {
		if (stationSelection == null) {
			stationSelection = new HashMap<Integer, Boolean>();
		}
		return stationSelection;
	}
	
	/**
	 * Is station selected
	 * @param stationId station integer id. 
	 * @return true if selected. default is true
	 */
	public boolean isStationSelected(int stationId) {
		Boolean sel = getStationMap().get(stationId);
		if (sel == null) {
			setStationSelected(stationId, true);
			return true;
		}
		return sel;
	}
	
	/**
	 * Set station selection
	 * @param stationId station integer id
	 * @param sel selection, true or false
	 */
	public void setStationSelected(int stationId, boolean sel) {
		getStationMap().put(stationId, sel);
	}

	@Override
	protected AISStationSelectParams clone() {
		try {
			return (AISStationSelectParams) super.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
			return null;
		}
	}

}
