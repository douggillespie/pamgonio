package fastlocdisplay.swing;

import PamView.symbol.PamSymbolChooser;
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

}
