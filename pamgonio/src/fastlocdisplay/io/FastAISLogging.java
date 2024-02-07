package fastlocdisplay.io;

import java.sql.Types;

import AIS.AISPositionReport;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import fastlocdisplay.FastAISDataBlock;
import fastlocdisplay.FastAISDataUnit;
import generalDatabase.PamTableDefinition;
import generalDatabase.PamTableItem;
import generalDatabase.SQLLogging;
import generalDatabase.SQLTypes;

public class FastAISLogging extends SQLLogging {

	private FastAISDataBlock fastAISDataBlock;
	
	private String tableName = "FastAISPositions";
	
	private PamTableItem integerId, hexId, latitude, longitude;

	public FastAISLogging(FastAISDataBlock fastAISDataBlock) {
		super(fastAISDataBlock);
		this.fastAISDataBlock = fastAISDataBlock;
		
		setTableDefinition(getBaseTableDefinition());
	}

	@Override
	public void setTableData(SQLTypes sqlTypes, PamDataUnit pamDataUnit) {
		FastAISDataUnit aisData = (FastAISDataUnit) pamDataUnit;
		integerId.setValue(aisData.getIntegerId());
		hexId.setValue(String.format("%X", aisData.getHexId()));
		AISPositionReport posReport = aisData.getPositionReport();
		latitude.setValue(posReport.getLatitude());
		longitude.setValue(posReport.getLongitude());
	}

	@Override
	public PamTableDefinition getBaseTableDefinition() {
		PamTableDefinition tableDefinition = new PamTableDefinition(tableName);
		tableDefinition.addTableItem(integerId = new PamTableItem("IntegerId", Types.INTEGER));
		tableDefinition.addTableItem(hexId = new PamTableItem("hexId", Types.CHAR, 20));
		tableDefinition.addTableItem(latitude = new PamTableItem("Latitude", Types.DOUBLE));
		tableDefinition.addTableItem(longitude = new PamTableItem("Longitude", Types.DOUBLE));
		
		return tableDefinition;
	}

}
