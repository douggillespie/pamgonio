package fastlocdisplay.swing;

import PamView.GeneralProjector;
import PamView.symbol.PamSymbolChooser;
import PamView.symbol.SymbolData;
import PamView.symbol.modifier.RotateColoursModifier;
import PamguardMVC.PamDataUnit;
import PamguardMVC.superdet.SuperDetection;
import fastlocdisplay.FastStationDataUnit;

public class FastLocationsSymbolModifier extends RotateColoursModifier {

	public FastLocationsSymbolModifier(PamSymbolChooser symbolChooser) {
		super(symbolChooser);
	}

	public FastLocationsSymbolModifier(String name, PamSymbolChooser symbolChooser, int modifyableBits) {
		super(name, symbolChooser, modifyableBits);
	}

	@Override
	public SymbolData getSymbolData(GeneralProjector projector, PamDataUnit dataUnit) {
		SuperDetection superDet = dataUnit.getSuperDetection(FastStationDataUnit.class);
		if (superDet != null) {
			return super.getSymbolData(projector, superDet);
		}
		else {
			return super.getSymbolData(projector, dataUnit);
		}
	}

	
}
