package fastlocdisplay.goniometer;

import java.io.File;
import java.io.Serializable;

import PamUtils.PamCalendar;

public class GoniometerParams implements Serializable, Cloneable {

	public static final long serialVersionUID = 1L;
	
	public static final int GONIOMETER_NOCONTROL = 0;
	public static final int GONIOMETER_INTERNALCONTROL = 1;
	public static final int GONIOMETER_EXTERNALCONTROL = 2;
	
	/**
	 * Control the external software from within PAMGuard. 
	 */
	public int controlFastRealtime = 1;
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
	
	/**
	 * Get an output directory, adding in the data if the user wants it. 
	 * This is only used for the FASTGPs software control. The reader of 
	 * data from the output files will start at the root folder and scan
	 * sub directories. 
	 * @return
	 */
	public String getChosenOutputDirectory() {
		if (autoDatedFolders == false) {
			return outputDirectory;
		}
		return outputDirectory + File.separator + PamCalendar.formatDBDate(System.currentTimeMillis());
	}
	
	/**
	 * Set debug output flag on FastGPS_REaltime;
	 */
	public boolean debugOutput = true;

	/**
	 * Get the executable. 
	 * @param fullPath use the full path rather than just the file name
	 * @return
	 */
	public File getExecutable() {
		if (fastGPSFolder == null || fastGPSexe == null) {
			return null;
		}
		File f;
		f = new File(fastGPSFolder + File.separator + fastGPSexe);
		return f;
	}

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
