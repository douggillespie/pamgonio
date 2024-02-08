package fastlocdisplay.goniometer;

import java.io.Serializable;

public class GoniometerParams implements Serializable, Cloneable {

	public static final long serialVersionUID = 1L;
	
	/**
	 * Control the external software from within PAMGuard. 
	 */
	public boolean controlFastRealtime = true;
	/**
	 * Name of fast GPS Executable (probably won't ever change)
	 */
	public String fastGPSexe = "FastGPS_Realtime.exe";
	
	/**
	 * Folder for executable, may change with installation. 
	 */
	public String fastGPSFolder = "C:\\ProgramData\\FastGPS Realtime Solution";
	
	/**
	 * Com port for gonio (-o option)
	 */
	public String gonioComPort;
	
	/**
	 * Com port for nav ephemerus data (-n options)
	 */
	public String navPort;
	
	/**
	 * Com port for AIS output (-ao option) 
	 */
	public String outPort;
	
	/**
	 * Output folder, to be set by user.
	 */
	public String outputDirectory; 
	
	/**
	 * Automatically make dated sub folders. 
	 */
	public boolean autoDatedFolders = true;

	@Override
	protected GoniometerParams clone() {
		try {
			return (GoniometerParams) super.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
			return null;
		}
	}

	
}
