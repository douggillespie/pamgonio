package fastlocdisplay.goniometer;

/**
 * Receive input stream and error stream messages from the external process. 
 * Note that these will be arriving is separate threads that monitor the 
 * process input and error streams. 
 * @author dg50
 *
 */
public interface ProcessMonitor {
 
	public void errorLine(String line);
	
	public void inputLine(String line);
	
	public void systemLine(String line);
	
}
