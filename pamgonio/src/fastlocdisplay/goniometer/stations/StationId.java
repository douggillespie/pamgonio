package fastlocdisplay.goniometer.stations;

import java.io.Serializable;

public class StationId implements Serializable {

	private static final long serialVersionUID = 1L;

	private int integerId;
	
	private int hexId;

	public StationId(int integerId, int hexId) {
		super();
		this.integerId = integerId;
		this.hexId = hexId;
	}

	public StationId(int integerId, String hexId) {
		super();
		this.integerId = integerId;
		try {
			this.hexId = Integer.decode("0x"+hexId);
		}
		catch (NumberFormatException e) {
			e.printStackTrace();
		}
	}

	/**
	 * @return the integerId
	 */
	public int getIntegerId() {
		return integerId;
	}

	/**
	 * @param integerId the integerId to set
	 */
	public void setIntegerId(int integerId) {
		this.integerId = integerId;
	}

	/**
	 * @return the hexId
	 */
	public int getHexId() {
		return hexId;
	}

	/**
	 * @param hexId the hexId to set
	 */
	public void setHexId(int hexId) {
		this.hexId = hexId;
	}

	@Override
	public String toString() {
		return String.format("0x%07X,%06d", hexId, integerId);
	}
	
	
}
