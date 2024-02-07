package fastlocdisplay.swing;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.Point2D;
import java.util.ArrayList;

import PamUtils.Coordinate3d;
import PamView.GeneralProjector;
import PamView.GeneralProjector.ParameterType;
import PamView.GeneralProjector.ParameterUnits;
import PamView.PamKeyItem;
import PamView.PamSymbol;
import PamView.PamSymbolType;
import PamView.PanelOverlayDraw;
import PamguardMVC.PamDataUnit;
import fastlocdisplay.FastAISDataUnit;
import fastlocdisplay.FastStationDataUnit;

public class FastAISStationsOverlay extends PanelOverlayDraw {
	
	public static final PamSymbol defaultSymbol = new PamSymbol(PamSymbolType.SYMBOL_CIRCLE, 10, 10, true, Color.WHITE, Color.white);
	public static final PamSymbol crossSymbol = new PamSymbol(PamSymbolType.SYMBOL_CROSS, 10, 10, true, Color.BLACK, Color.BLACK);

	public FastAISStationsOverlay() {
		super(defaultSymbol);
		// TODO Auto-generated constructor stub
	}

	@Override
	public Rectangle drawDataUnit(Graphics g, PamDataUnit pamDataUnit, GeneralProjector generalProjector) {
		if (generalProjector.getParmeterType(1) == ParameterType.LONGITUDE
				&& generalProjector.getParmeterType(0) == ParameterType.LATITUDE) {
			return drawOnMap(g, pamDataUnit, generalProjector);
		}
		else {
			return null;
		}
	}
	
	public Rectangle drawOnMap(Graphics g, PamDataUnit pamDataUnit, GeneralProjector generalProjector) {
		// this is going to draw all the points for a single station. 
		// next iteration will time limit this to x hours. 
		FastStationDataUnit stationDataUnit = (FastStationDataUnit) pamDataUnit;
		ArrayList<FastAISDataUnit> aisUnits = null;
		synchronized (stationDataUnit.getSubDetectionSyncronisation()) {
			ArrayList<PamDataUnit<?, ?>> subDets = stationDataUnit.getSubDetections();
			aisUnits = new ArrayList<>(subDets.size());
			for (PamDataUnit aSub : subDets) {
				aisUnits.add((FastAISDataUnit) aSub);
			}
		}
		Point prevXY = null;
		for (int i = 0; i < aisUnits.size(); i++) {
			FastAISDataUnit aisUnit = aisUnits.get(i);
			Coordinate3d pos = generalProjector.getCoord3d(aisUnit.getPositionReport().getLatitude(), aisUnit.getPositionReport().getLongitude(), 0);
			Point2D posXY = pos.getPoint2D();
			PamSymbol symbol = getPamSymbol(pamDataUnit, generalProjector);
			Point p = new Point((int) posXY.getX(), (int) posXY.getY());
			symbol.draw(g, p);
			if (prevXY != null) {
				g.drawLine(prevXY.x, prevXY.y, p.x, p.y);
			}
			prevXY = p;
			generalProjector.addHoverData(pos, aisUnit);
			if (i == aisUnits.size()-1) {
				// draw a black cross on top of the symbol to better mark the last one. 
				crossSymbol.setWidth(symbol.getWidth()+4);
				crossSymbol.setHeight(symbol.getHeight()+4);
				crossSymbol.draw(g,  p);
			}
		}
		return null;
	}

	@Override
	public boolean canDraw(ParameterType[] parameterTypes, ParameterUnits[] parameterUnits) {
		if (parameterTypes[0] == ParameterType.LATITUDE
				&& parameterTypes[1] == ParameterType.LONGITUDE) {
			return true;
		}
		return false;
	}

	@Override
	public PamKeyItem createKeyItem(GeneralProjector generalProjector, int keyType) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getHoverText(GeneralProjector generalProjector, PamDataUnit dataUnit, int iSide) {
		// TODO Auto-generated method stub
		return null;
	}

}
