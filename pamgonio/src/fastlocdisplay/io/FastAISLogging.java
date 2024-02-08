package fastlocdisplay.io;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.HashMap;

import AIS.AISPositionReport;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import fastlocdisplay.FastAISDataBlock;
import fastlocdisplay.FastAISDataUnit;
import generalDatabase.DBProcess;
import generalDatabase.PamConnection;
import generalDatabase.PamTableDefinition;
import generalDatabase.PamTableItem;
import generalDatabase.SQLLogging;
import generalDatabase.SQLTypes;

/**
 * Provides some basic logging, but also some functions for holding 
 * last positions of each station, etc. 
 * @author dg50
 *
 */
public class FastAISLogging extends SQLLogging {

	private FastAISDataBlock fastAISDataBlock;

	private String tableName = "FastAISPositions";

	private PamTableItem integerId, hexId, latitude, longitude, accuracy;

	private HashMap<Integer, FastAISDataUnit> lastPositions = new HashMap<>();

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
		accuracy.setValue(posReport.positionAccuracy == 0 ? "0" : "1");
	}

	@Override
	protected FastAISDataUnit createDataUnit(SQLTypes sqlTypes, long timeMilliseconds, int databaseIndex) {
		try {
			int intId = integerId.getIntegerValue();
			String hexString = hexId.getDeblankedStringValue();
			int hexVal = Integer.decode("0x"+hexString);
			double lat = latitude.getDoubleValue();
			double lon = longitude.getDoubleValue();
			String accStr = accuracy.getDeblankedStringValue();
			int acc = 0;
			if (accStr != null) {
				if (accStr.startsWith("1")) {
					acc = 1;
				}
			}
			AISPositionReport posRep = new AISPositionReport(timeMilliseconds, 0, 0, 0, lat, lon, 0, 0);
			posRep.positionAccuracy = acc;
			return new FastAISDataUnit(timeMilliseconds, intId, hexVal, posRep);
			
		}
		catch (Exception e) {
			return null;
		}
	}

	@Override
	public PamTableDefinition getBaseTableDefinition() {
		PamTableDefinition tableDefinition = new PamTableDefinition(tableName);
		tableDefinition.addTableItem(integerId = new PamTableItem("IntegerId", Types.INTEGER));
		tableDefinition.addTableItem(hexId = new PamTableItem("hexId", Types.CHAR, 20));
		tableDefinition.addTableItem(latitude = new PamTableItem("Latitude", Types.DOUBLE));
		tableDefinition.addTableItem(longitude = new PamTableItem("Longitude", Types.DOUBLE));
		tableDefinition.addTableItem(accuracy = new PamTableItem("Accuracy", Types.CHAR, 1));

		return tableDefinition;
	}

	/**
	 * Populate a hashmap of last positions of the current database. 
	 * @param con
	 * @return
	 */
	public boolean findLastPositions(PamConnection con) {
		ResultSet resultSet = createViewResultSet(con, "ORDER BY UTC");

		SQLTypes sqlTypes = con.getSqlTypes();
		try {
			while (resultSet.next()) {

				transferDataFromResult(sqlTypes, resultSet);
				PamDataUnit newDataUnit = createDataUnit(sqlTypes, getLastTime(), getLastLoadIndex());
				if (newDataUnit == null) {
					continue;
				}
				if (newDataUnit instanceof FastAISDataUnit == false) {
					System.out.println("Not a FAST AIS DataUnit");
					continue;
				}
				FastAISDataUnit fdu = (FastAISDataUnit) newDataUnit;
				lastPositions.put(fdu.getIntegerId(), fdu);
			}

		}
		catch (SQLException ex) {
			System.err.printf("Error in SQLLogging.loadViewData(...)");
			ex.printStackTrace();
		}

		return true;
	}
	
	/**
	 * Get the last data unit for a given tag id or null if there isn't one. 
	 * @param integerId station integer is
	 * @return last data unit logged in database, or null
	 */
	public FastAISDataUnit getLastDataUnit(int integerId) {
		return lastPositions.get(integerId);
	}

	@Override
	public synchronized boolean logData(PamConnection con, PamDataUnit dataUnit) {
		boolean ans = super.logData(con, dataUnit);
		if (ans) {
			FastAISDataUnit fdu = (FastAISDataUnit) dataUnit;
			lastPositions.put(fdu.getIntegerId(), fdu);
		}
		return ans;
	}

	@Override
	public synchronized boolean logData(PamConnection con, PamDataUnit dataUnit, PamDataUnit superDetection) {
		// TODO Auto-generated method stub
		boolean ans = super.logData(con, dataUnit, superDetection);
		if (ans) {
			FastAISDataUnit fdu = (FastAISDataUnit) dataUnit;
			lastPositions.put(fdu.getIntegerId(), fdu);
		}
		return ans;
	}

	@Override
	public boolean doExtraChecks(DBProcess dbProcess, PamConnection connection) {
		boolean ok = super.doExtraChecks(dbProcess, connection);
		findLastPositions(connection);
		return ok;
	}

}
