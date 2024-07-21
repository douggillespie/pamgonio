package fastlocdisplay.swing;

import PamView.GeneralProjector;
import PamView.symbol.PamSymbolChooser;
import PamView.symbol.StandardSymbolChooser;
import PamView.symbol.StandardSymbolManager;
import PamView.symbol.modifier.RotateColoursModifier;
import PamguardMVC.PamDataBlock;

public class FastLocationsSymbolManager extends StandardSymbolManager {

	public FastLocationsSymbolManager(PamDataBlock pamDataBlock) {
		super(pamDataBlock, FastAISStationsOverlay.defaultSymbol.getSymbolData());
		addSymbolOption(HAS_SYMBOL);
	}

	@Override
	public void addSymbolModifiers(PamSymbolChooser psc) {
		addAnnotationModifiers(psc);
		psc.addSymbolModifier(new FastLocationsSymbolModifier(psc));
	}

	@Override
	protected StandardSymbolChooser createSymbolChooser(String displayName, GeneralProjector projector) {
		return new FastLocationsSymbolChooser(this, getPamDataBlock(), displayName, getDefaultSymbol(), projector);
	}
}
