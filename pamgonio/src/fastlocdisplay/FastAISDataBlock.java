package fastlocdisplay;

import PamView.GeneralProjector;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;

public class FastAISDataBlock extends PamDataBlock<FastAISDataUnit> {

	public FastAISDataBlock(FastlocViewProcess fastLocViewProcess) {
		super(FastAISDataUnit.class, "Fast AIS Positions", fastLocViewProcess, 0);
		// TODO Auto-generated constructor stub
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

	@Override
	public String getHoverText(GeneralProjector generalProjector, PamDataUnit dataUnit, int iSide) {
		return dataUnit.getSummaryString();
	}

}
