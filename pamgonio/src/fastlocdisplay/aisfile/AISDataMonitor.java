package fastlocdisplay.aisfile;

/**
 * Monitor for new AIS data read from a file. 
 * @author dg50
 *
 */
public interface AISDataMonitor {

	/**
	 * Notification of a new line of AIS data
	 * @param aisDataLine
	 */
	void newAISData(AISFileLineInfo aisDataLine);

}
