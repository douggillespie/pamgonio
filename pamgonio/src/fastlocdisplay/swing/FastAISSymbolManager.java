package fastlocdisplay.swing;

import PamView.symbol.PamSymbolChooser;
import PamView.symbol.StandardSymbolManager;
import PamView.symbol.modifier.RotateColoursModifier;
import PamguardMVC.PamDataBlock;

public class FastAISSymbolManager extends StandardSymbolManager {

	public FastAISSymbolManager(PamDataBlock pamDataBlock) {
		super(pamDataBlock, FastAISStationsOverlay.defaultSymbol.getSymbolData());
		addSymbolOption(HAS_SYMBOL);
	}

	@Override
	public void addSymbolModifiers(PamSymbolChooser psc) {
		addAnnotationModifiers(psc);
		psc.addSymbolModifier(new RotateColoursModifier(psc));
	}

}
