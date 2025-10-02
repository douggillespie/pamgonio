package fastlocdisplay.goniometer;

import java.awt.Desktop;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.ProcessHandle.Info;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Stream;

import GPS.GPSControl;
import GPS.GPSDataBlock;
import GPS.GpsDataUnit;
import PamUtils.LatLong;
import PamUtils.PamCalendar;
import PamUtils.PamUtils;
import PamView.dialog.warn.WarnOnce;
import fastlocdisplay.FastlocViewControl;
import serialComms.jserialcomm.PJSerialComm;

/**
 * Control an external process that must be kept running if at all possible. 
 * @author dg50
 *
 */
public class ExtProcessControl {
	
	private String previousCommand;
	private GoniometerControl goniometerControl; 
	private Process internalProcess;
	private Thread inputThread, errorThread;
	private ProcessMonitor processMonitor;

	public ExtProcessControl(GoniometerControl goniometerControl) {
		this.goniometerControl = goniometerControl;
		this.processMonitor = goniometerControl;
	}
	
	/**
	 * Check to see if the process is up and alive. 
	 * @return true if it seems to be running either internally or externally. . 
	 */
	public boolean processRunning() {
		ProcessHandle proc = findFastGPSProcess();
		if (proc != null && proc.isAlive()) {
			return proc.isAlive(); 
		}
		
		if (internalProcess != null) {
			return internalProcess.isAlive();
		}
		ProcessHandle exHandle = findFastGPSProcess();
		if (exHandle == null) {
			return false;
		}
		return exHandle.isAlive();
	}
	
	/**
	 * Stop the process. if it's running
	 * @throws GoniometerException
	 */
	public synchronized boolean stopProcess() throws GoniometerException {
		if (internalProcess == null) {
			return false;
		}
		internalProcess.destroy();
		try {
			if (inputThread != null) {
				inputThread.join(2000);
			}
			if (errorThread != null) {
				errorThread.join(2000);
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
			throw new GoniometerException("Problems stopping executable: " + e.getMessage());
		}
		internalProcess.destroyForcibly();
		return true;
	}
	
	/**
	 * Kill the process, whether internal or external. 
	 * @return true
	 */
	private boolean killProcess() {
		
		boolean stopInternal = false;
		try {
			stopInternal = stopProcess();
		} catch (GoniometerException e) {
			
		}
		
		// other
//		ProcessHandle handle = findFastGPSProcess();
//		if (handle == null) {
//			return false;
//		}
//		
//		boolean destroyed = handle.destroy();
//		if (destroyed == false) {
//			destroyed = handle.destroyForcibly();
//		}
		killAllFastGPSProcesses();

		ProcessHandle handle = findFastGPSProcess();
		return (handle == null);
	}

	/**
	 * Stop (if needed) and relaunch the process. 
	 * @return true if launched OK. 
	 */
	public boolean relaunchProcess() throws GoniometerException {
		stopProcess();
		return launchProcess();
	}

	private synchronized boolean launchProcess() throws GoniometerException {
//		Thread current = Thread.currentThread();
//		StackTraceElement[] stack = current.getStackTrace();
//		String now = PamCalendar.formatTime(System.currentTimeMillis());
//		System.out.printf("Launch at %s in thread %s\n", now, current.toString());
//		for (int i = 0; i < stack.length; i++) {
//			System.out.printf("%s Launch process called from %s:%s:%d\n", now, stack[i].getClassName(), stack[i].getMethodName(), stack[i].getLineNumber());
//		}
		
		ProcessHandle exHandle = findFastGPSProcess();
		if (exHandle != null) {
			return true;
		}
		int type = goniometerControl.getGoniometerParams().controlFastRealtime;
		switch (type) {
		case GoniometerParams.GONIOMETER_NOCONTROL:
			return true;
		case GoniometerParams.GONIOMETER_INTERNALCONTROL:
			return launchInternal();
		case GoniometerParams.GONIOMETER_EXTERNALCONTROL:
			return launchExternal();
		}
		return false;
	}

	private boolean launchExternal() throws GoniometerException {
		ProcessHandle exHandle = findFastGPSProcess();
		if (exHandle != null && exHandle.isAlive()) {
//			throw new GoniometerException("Goniometer process already running");
			return true;
		}
		killAllFastGPSProcesses();
		return launchExternally();
	}
	/**
	 * Launch the process. This is launched using ProcessBuilder
	 * which makes the Goni process a sub process of the JVM. This is 
	 * good since PAMGeurd will get the output streams and can more 
	 * easily monitor it's state, but on the other hand, the process 
	 * will terminate if PAMGuard terminates. <br>  
	 * Private since anything call into this class
	 * will need to stop the process first, so should use relaunchProcess()
	 * @return true if successful. 
	 * @throws GoniometerException
	 */
	private synchronized boolean launchInternal() throws GoniometerException {
		
		killAllFastGPSProcesses();
		
		ArrayList<String> cmds = null;
		cmds = generateCommand(true);
		
		ProcessBuilder procBuilder = new ProcessBuilder(cmds);
		try {
			internalProcess = procBuilder.start();
		} catch (IOException e) {
			internalProcess = null;
			throw new GoniometerException("Can't start process: " + e.getMessage());
		}
		inputThread = new Thread(new InputStreamMonitor("Input", internalProcess.getInputStream()), "Goniometer input stream");
		inputThread.start();
		errorThread = new Thread(new InputStreamMonitor("Errors", internalProcess.getErrorStream()), "Goniometer error stream");
		errorThread.start();

		return internalProcess != null;
	}
	
	/**
	 * Launch the process externally in a command window. 
	 * To do this, need to make a batch file and open it 
	 * with Desktop, otherwise it ends up as a sub-process. 
	 * @return true on success. 
	 * @throws GoniometerException 
	 */
	private boolean launchExternally() throws GoniometerException {
		ArrayList<String> cmds = null;
		cmds = generateCommand(true);
		String oneLine = makeOneLineCommand(cmds);
		processMonitor.systemLine("Starting external FastGPS process:\n\t" + oneLine);
		// then wait up to 10 seconds to check that it's launched OK. 
		
		// need to add command exe to the front of the main list. 
//		cmds.add(0, "cmd.exe");
//		cmds.add(1, "/C");
		File batFile = writeBatchCommand(cmds);
		try {
			Desktop.getDesktop().open(batFile);
		} catch (IOException e) {
			throw new GoniometerException("Unable to launch from desktop: " + e.getMessage());
		}
		
		long tic = System.currentTimeMillis();
		ProcessHandle procHandle = null;
		while (System.currentTimeMillis() - tic < 10000) {
			procHandle = findFastGPSProcess();
			if (procHandle != null) {
				break;
			}
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
			}
		}
		if (procHandle == null) {
			processMonitor.systemLine("Unable to launch " + goniometerControl.getGoniometerParams().fastGPSexe + " externally");
		}
		else {
			String str = String.format("External Process %s launched in %3.1f seconds\n", 
					goniometerControl.getGoniometerParams().fastGPSexe, (double) (System.currentTimeMillis()-tic)/1000.);
			processMonitor.systemLine(str);
		}
		
		return procHandle != null;
	}
	
	public String getProcessSummary() {
		ProcessHandle procHandle = findFastGPSProcess();
		if (procHandle == null) {
			return null;
		}
		String intext = "externally";
		if (isInternalProcess()) {
			intext = "internally";
		}
		Info info = procHandle.info();
		Boolean alive = procHandle.isAlive();
		long pid = procHandle.pid();
		if (info == null) {
			return String.format("Process %s pid %d, no info available", alive.toString(), pid);
		}
		else {
			Optional<Duration> oDuration = info.totalCpuDuration();
			Duration duration = null;
			if (oDuration.isPresent()) {
				duration  = oDuration.get();
			}
			String runTime = "unknown";
			Optional<Instant> oStartInstant = info.startInstant();
			if (oStartInstant.isPresent()) {
				Instant startInstant = oStartInstant.get();
				long millis = System.currentTimeMillis() -  startInstant.toEpochMilli();
				runTime = PamCalendar.formatDuration(millis);
			}
			return String.format("Process %s running %s, pid %d, Run time %s", alive ? "running" : "dead", intext, pid, runTime);
		}
	}
	
	/**
	 * Is it an internal process. This can be confusing since not sure if 
	 * the internalProcess points at the java or at the exe?
	 * @return
	 */
	private boolean isInternalProcess() {
		if (internalProcess == null) {
			return false;
		}
//		ArrayList<ProcessHandle> procs = findFastJavaProcesses();
//		for (int i = 0; i < procs.size(); i++) {
//			ProcessHandle aProc = procs.get(i);
//			if (aProc == internalProcess.toHandle()) {
//				return true;
//			}
//		}
//		return false;
		return internalProcess.isAlive();
	}

	/**
	 * For some reason, this needs to be declared outside the find function. 
	 */
	private ProcessHandle foundHandle;
	
	/**
	 * 
	 * Search the process list to find the fastGPS process id. 
	 * @return -1 if not found, pid otherwise. 
	 */
	private ProcessHandle findFastGPSProcess() {
		foundHandle = null;
		Stream<ProcessHandle> processes = ProcessHandle.allProcesses();
		String fastFolder = goniometerControl.getGoniometerParams().fastGPSFolder;
		processes.forEach(new Consumer<ProcessHandle>() {
			@Override
			public void accept(ProcessHandle processHandle) {
				Info info = processHandle.info();
				Optional<String> icmd = info.command();
				if (icmd == null) {
					return;
				}
				if (icmd.toString().contains(goniometerControl.getGoniometerParams().fastGPSexe)) {
					foundHandle = processHandle;
				}
//				if (icmd.toString().contains("java.exe")) {
////					System.out.println("Java process: " + processHandle);
//					Optional<String> command = info.command();
//					if (command.isPresent()) {
//						String cmd = command.get();
//						if (cmd.contains(fastFolder)) {
//							foundHandle = processHandle;
//						}
//					}
//				}
//				System.out.println(info.command());
			}
		});
		return foundHandle;		
	}

	/**
	 * Get a full list of processes associated with FastLoc GPS
	 * This will include java.exe processes that were in the same folder 
	 * as FastLocGPS and also any processes with the name FastGPS_Realtime.exe
	 * @return list of processes. 
	 */ 
	private ArrayList<ProcessHandle> findFastJavaProcesses() {
		ArrayList<ProcessHandle> foundProcs = new ArrayList<>();
		foundHandle = null;
		Stream<ProcessHandle> processes = ProcessHandle.allProcesses();
		String fastFolder = goniometerControl.getGoniometerParams().fastGPSFolder;
		processes.forEach(new Consumer<ProcessHandle>() {
			@Override
			public void accept(ProcessHandle processHandle) {
				Info info = processHandle.info();
				Optional<String> icmd = info.command();
				if (icmd == null) {
					return;
				}
				if (icmd.toString().contains(goniometerControl.getGoniometerParams().fastGPSexe)) {
					foundHandle = processHandle;
					foundProcs.add(processHandle);
				}
				if (icmd.toString().contains("java.exe")) {
//					System.out.println("Java process: " + processHandle);
					Optional<String> command = info.command();
					if (command.isPresent()) {
						String cmd = command.get();
						if (cmd.contains(fastFolder)) {
							foundHandle = processHandle;
							foundProcs.add(processHandle);
						}
					}
				}
				//				System.out.println(info.command());
			}
		});
		return foundProcs;
	}
	
	public synchronized int killAllFastGPSProcesses() {
		ArrayList<ProcessHandle> allProcs = findFastJavaProcesses();
//		return 0;
		long tic = System.currentTimeMillis();
		int nKill = 0;
		int nNotDead = 0;
		for (int i = 0; i < allProcs.size(); i++) {
			try {
				boolean killed = allProcs.get(i).destroyForcibly();
				if (killed) {
					nKill++;
				}
				else {
					nNotDead ++;
				}
			}
			catch (Exception e) {
				nNotDead ++;
			}
		}
//		int nNotDead = 0;
//		while (true) {
//			if (System.currentTimeMillis() - tic > 10000) {
//				break;
//			}
//			ProcessHandle proc = findFastGPSProcess();
//			if (proc == null) {
//				break;
//			}
//			boolean killed = proc.destroyForcibly();
//			if (killed) {
//				nKill++;
//			}
//			else {
//				nNotDead ++;
//			}
//		}
		internalProcess = null;
		System.out.printf("FastLocGPS Killed %d processes, unable to kill %d others\n", nKill, nNotDead);
		return nKill;
	}
	
	/**
	 * Generate a bat file with a single line command to launch program. 
	 * @param commands
	 * @return
	 * @throws GoniometerException
	 */
	private File writeBatchCommand(ArrayList<String> commands) throws GoniometerException {
		GoniometerParams params = goniometerControl.getGoniometerParams();
		File batFile = new File(params.fastGPSFolder + File.separator + "pamguardlaunch.bat");
		if (batFile.exists()) {
			batFile.delete();
		}
		BufferedWriter bw;
		try {
			bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(batFile)));
		} catch (FileNotFoundException e) {
			throw new GoniometerException("Unable to create bat file : " + e.getMessage());
		}
		String cmd = makeOneLineCommand(commands);
		String cdCommand = String.format("cd \"%s\"\r\n", params.fastGPSFolder);
		String cmdcmd = "cmd.exe /C\r\n";
//		System.out.println("written command: " + cmd);
		try {
			bw.write(cdCommand); // change directory
			bw.write(cmdcmd); // the command line, with /C to make it run the next line
			bw.write(cmd + "\r\n"); // this needs to go on the next line or cmd.exe doesn't like paths with spaces. 
		} catch (IOException e) {
			throw new GoniometerException("Unable to write command to bat file : " + e.getMessage());
		}
		try {
			bw.close();
		} catch (IOException e) {
		}
		return batFile;
	}
	
	private class InputStreamMonitor implements Runnable {

		private String streamType;
		
		private InputStream inputStream;

		private BufferedReader bufferedReader;
		
		InputStreamReader isr ;
		
		public InputStreamMonitor(String streamType, InputStream inputStream) {
			super();
			this.streamType = streamType;
			this.inputStream = inputStream;
		}

		@Override
		public void run() {
			isr = new InputStreamReader(inputStream);
			bufferedReader = new BufferedReader(isr);
			String line;
			try {
//				System.out.println(streamType + " stream entered");
				int n = 0;
				while ((line = bufferedReader.readLine()) != null) {
					n++;
					//					System.out.println(streamType + ":" + line);
					if (processMonitor != null) {
						if (streamType.equals("Errors")) {
							processMonitor.errorLine(line);
						}
						else {
							processMonitor.inputLine(line);
						}
					}
				}
//				System.out.printf("Stream %s exited after %d lines, process %s\n", streamType, n, process.isAlive() ? "Alive" : "Dead");

			}
			catch (IOException e) {
				System.out.println("Input stream terminated");
			}
			
		}
		
	}
	
	/**
	 * Generate the goniometer launch command. throw exception if 
	 * any com port doesn't exist. 
	 * @return array list of command line instructions. 
	 * @throws GoniometerException
	 */
	public ArrayList<String> generateCommand(boolean fullPath) throws GoniometerException {
		GoniometerParams params = goniometerControl.getGoniometerParams();
		File exe = params.getExecutable();
		if (exe == null) {
			throw new GoniometerException("No executable FastGPS defined");
		}
		if (exe.exists() == false) {
			throw new GoniometerException("Executable " + exe.getAbsolutePath() + " does not exist");
		}
		// check all the COM ports exist and throw an exception if any of them don't. 
		if (portExists(params.navPort) == false) {
			throw new GoniometerException(String.format("Navigation com port %s does not exist", params.navPort));
		}
		if (portExists(params.gonioComPort) == false) {
			throw new GoniometerException(String.format("Goniometer com port %s does not exist", params.gonioComPort));
		}
		if (portExists(params.outPort) == false) {
			throw new GoniometerException(String.format("Output com port %s does not exist", params.outPort));
		}
		LatLong latLong = findGPSPosition();

		ArrayList<String> cmd = new ArrayList<>();
		if (fullPath) {
			cmd.add(exe.getAbsolutePath());
		}
		else {
			cmd.add(params.fastGPSexe);
		}
		if (params.debugOutput) {
			cmd.add("-d");
		}
		cmd.add("-n");
		cmd.add(params.navPort);
		cmd.add("-o");
		cmd.add(params.gonioComPort);
		cmd.add("-oa");
		cmd.add(params.outPort);
		cmd.add("-out");
		cmd.add(params.getChosenOutputDirectory());
		if (latLong != null) {
			cmd.add("-lat");
			cmd.add(String.format("%6.4f", latLong.getLatitude()));
			cmd.add("-lon");
			// seems like it only likes longitudes between 0 and 360
			cmd.add(String.format("%6.4f", PamUtils.constrainedAngle(latLong.getLongitude(),360)));
			cmd.add("-alt");
			cmd.add("0");
		}
		
		
		return cmd;
	}
	
	/**
	 * Get the current vessel position to use as a seed. 
	 * @return GPS Position or null
	 */
	private LatLong findGPSPosition() {
		GPSControl gpsControl = GPSControl.getGpsControl();
		if (gpsControl == null) {
			return null;
		}
		GPSDataBlock gpsBlock = gpsControl.getGpsDataBlock();
		GpsDataUnit lastData = gpsBlock.getLastUnit();
		if (lastData == null) {
			return null;
		}
		return lastData.getGpsData();
	}

	/**
	 * Make a single line command from the commands array list. 
	 * @param commands
	 * @return
	 */
	public String makeOneLineCommand(ArrayList<String> commands) {
		if (commands == null || commands.size() == 0) {
			return null;
		}
		String oneLine = wrapCommandPart(commands.get(0));
		for (int i = 1; i < commands.size(); i++) {
			oneLine += " " + wrapCommandPart(commands.get(i));
		}
		return oneLine;
	}
	
	/**
	 * wrap a command part in quotes if it contains any spaces. 
	 * @param part
	 * @return
	 */
	private String wrapCommandPart(String part) {
		if (part == null) {
			return null;
		}
		part = part.strip();
		if (part.contains(" ")) {
			part = "\"" + part + "\"";
		}
		return part;
	}
	
	private boolean portExists(String portName) {
		String[] portStrings = PJSerialComm.getSerialPortNames();
		if (portStrings == null) {
			return false;
		}
		for (int i = 0; i < portStrings.length; i++) {
			if (portStrings[i].equals(portName)) {
				return true;
			}
		}
		return false;
	}

	public void updateStationsList() {
		boolean running = processRunning();
		if (running == false) {
			return; // no need to do anythig. 
		}
		String msg = "The Argos ID stations list has been modified. You should stop and restart the FastGPS_Realtime app";
		int ans = WarnOnce.showWarning("Argos station id's", msg, WarnOnce.OK_CANCEL_OPTION);
		if (ans == WarnOnce.OK_OPTION) {
			killProcess();
		}
		
	}

	/**
	 * @return the internalProcess
	 */
	public Process getInternalProcess() {
		return internalProcess;
	}
	
}
