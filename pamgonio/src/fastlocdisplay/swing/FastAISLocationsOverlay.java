package fastlocdisplay.swing;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.HashMap;

import Map.MapDetectionData;
import Map.MapPanel;
import Map.MapRectProjector;
import Map.SimpleMap;
import PamUtils.Coordinate3d;
import PamView.GeneralProjector;
import PamView.PamKeyItem;
import PamView.PamSymbol;
import PamView.PamSymbolType;
import PamView.PanelOverlayDraw;
import PamView.GeneralProjector.ParameterType;
import PamView.GeneralProjector.ParameterUnits;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import PamguardMVC.superdet.SuperDetection;
import fastlocdisplay.FastAISDataUnit;
import fastlocdisplay.FastStationDataUnit;

public class FastAISLocationsOverlay  extends PanelOverlayDraw {
	
	public static final PamSymbol defaultSymbol = new PamSymbol(PamSymbolType.SYMBOL_CIRCLE, 10, 10, true, Color.WHITE, Color.white);
	public static final PamSymbol crossSymbol = new PamSymbol(PamSymbolType.SYMBOL_CROSS, 10, 10, true, Color.BLACK, Color.BLACK);
	
	HashMap<Integer, Point> pointHistory;

	public FastAISLocationsOverlay() {
		super(defaultSymbol);
		pointHistory = new HashMap<>();
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
		FastAISDataUnit aisUnit = (FastAISDataUnit) pamDataUnit;
		Point prevXY = pointHistory.get(aisUnit.getIntegerId());
		Coordinate3d pos = generalProjector.getCoord3d(aisUnit.getPositionReport().getLatitude(), aisUnit.getPositionReport().getLongitude(), 0);
		Point2D posXY = pos.getPoint2D();
		PamSymbol symbol = getPamSymbol(pamDataUnit, generalProjector);
		Point p = new Point((int) posXY.getX(), (int) posXY.getY());
		Rectangle r = symbol.draw(g, p);
		if (prevXY != null) {
			g.drawLine(prevXY.x, prevXY.y, p.x, p.y);
		}
		pointHistory.put(aisUnit.getIntegerId(), p);
		generalProjector.addHoverData(pos, aisUnit);
		if (aisUnit.isLast()) {
			// draw a black cross on top of the symbol to better mark the last one. 
			crossSymbol.setWidth(symbol.getWidth()+4);
			crossSymbol.setHeight(symbol.getHeight()+4);
			crossSymbol.draw(g,  p);
		}
		return r;
	}
		
	/**
	 * Try to find the detection data settings for the specified data block to 
	 * avoid drawing everything. 
	 * @param projector
	 * @param dataBlock
	 * @return
	 */
	private MapDetectionData findDisplayOptions(GeneralProjector projector, PamDataBlock dataBlock) {
		if (projector instanceof MapRectProjector) {
			return null;
		}
		MapRectProjector mapProj = (MapRectProjector) projector;
		MapPanel mapPanel = mapProj.getMapPanelRef();
		if (mapPanel == null) {
			return null;
		}
		SimpleMap mapRef = mapPanel.getSimpleMapRef();
		MapDetectionData mapDetectionData = mapRef.getMapDetectionsManager().findDetectionData(dataBlock);
		return mapDetectionData;
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

	@Override
	public boolean preDrawAnything(Graphics g, PamDataBlock pamDataBlock, GeneralProjector projector) {
		pointHistory.clear();
		return super.preDrawAnything(g, pamDataBlock, projector);
	}


}
