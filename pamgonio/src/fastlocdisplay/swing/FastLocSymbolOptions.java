package fastlocdisplay.swing;

import PamView.symbol.StandardSymbolOptions;
import PamView.symbol.SymbolData;

public class FastLocSymbolOptions extends StandardSymbolOptions {

	public FastLocSymbolOptions(SymbolData defaultSymbol) {
		super(defaultSymbol);
	}

	private static final long serialVersionUID = 1L;

	public boolean drawLinkLines = true;
	
	public boolean drawVesselLines = true;
	
}
