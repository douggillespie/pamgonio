package fastlocdisplay;

import java.util.Comparator;
import java.util.List;
import java.util.ListIterator;

import PamController.PamController;
import PamguardMVC.PamDataBlock;

/**
 * Datablock for fastloc data. Nothing is ever deleted and there is only 
 * ever one unit per tag (station);
 * @author dg50
 *
 */
public class FastStationDataBlock extends PamDataBlock<FastStationDataUnit> {

	public FastStationDataBlock(FastlocViewProcess fastlocViewProcess) {
		super(FastStationDataUnit.class, "Fstloc Locations", fastlocViewProcess, 0);
	}

	@Override
	protected int removeOldUnitsT(long currentTimeMS) {
		//		return super.removeOldUnitsT(currentTimeMS);
		return 0;
	}

	@Override
	protected int removeOldUnitsS(long mastrClockSample) {
		//		return super.removeOldUnitsS(mastrClockSample);
		return 0;
	}

	/**
	 * Find a station data unit based on the integer id. 
	 * @param integerId
	 * @return
	 */
	public FastStationDataUnit findStationDataUnit(int integerId) {
		synchronized (getSynchLock()) {
			ListIterator<FastStationDataUnit> it = getListIterator(0);
			while (it.hasNext()) {
				FastStationDataUnit du = it.next();
				if (du.getIntegerId() == integerId) {
					return du;
				}
			}
		}
		return null;
	}

	@Override
	public void clearAll() {
		if (PamController.getInstance().getRunMode() == PamController.RUN_NORMAL) {
			return;
		}
		super.clearAll();
	}

	/**
	 * Sort a list by the integer station id. 
	 * @param data
	 */
	public void sortByIntegerId(List<FastStationDataUnit> data) {
		if (data == null) {
			return;
		}
		data.sort(new IntegerIdComparator());
	}
	
	private class IntegerIdComparator implements Comparator<FastStationDataUnit> {

		@Override
		public int compare(FastStationDataUnit o1, FastStationDataUnit o2) {
			int id1 = o1.getIntegerId();
			int id2 = o2.getIntegerId();
			return id2-id1;
		}
		
	}
	
}
