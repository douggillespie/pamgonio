package fastlocdisplay.goniometer;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import serialComms.jserialcomm.PJSerialComm;

/**
 * Control an external process that must be kept running if at all possible. 
 * @author dg50
 *
 */
public class ExtProcessControl {
	
	private String previousCommand;
	private GoniometerControl goniometerControl; 
	private Process process;
	private Thread inputThread, errorThread;
	private ProcessMonitor processMonitor;

	public ExtProcessControl(GoniometerControl goniometerControl) {
		this.goniometerControl = goniometerControl;
		this.processMonitor = goniometerControl;
	}
	
	/**
	 * Check to see if the process is up and alive. 
	 * @return true if it seems to be running. 
	 */
	public boolean processRunning() {
		return (process != null && process.isAlive());
	}
	
	/**
	 * Stop the process. if it's running
	 * @throws GoniometerException
	 */
	public void stopProcess() throws GoniometerException {
		if (process == null) {
			return;
		}
		process.destroy();
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
		process.destroyForcibly();
		return;
	}
	
	/**
	 * Stop (if needed) and relaunch the process. 
	 * @return true if launched OK. 
	 */
	public boolean relaunchProcess() throws GoniometerException {
		stopProcess();
		return launchProcess();
	}

	/**
	 * Launch the process. Private since anything call into this class
	 * will need to stop the process first, so should use relaunchProcess()
	 * @return true if successful. 
	 * @throws GoniometerException
	 */
	private boolean launchProcess() throws GoniometerException {
		ArrayList<String> cmds = null;
		cmds = generateCommand();
		
		ProcessBuilder procBuilder = new ProcessBuilder(cmds);
		try {
			process = procBuilder.start();
		} catch (IOException e) {
			process = null;
			throw new GoniometerException("Can't start process: " + e.getMessage());
		}
		inputThread = new Thread(new InputStreamMonitor("Input", process.getInputStream()), "Goniometer input stream");
		inputThread.start();
		errorThread = new Thread(new InputStreamMonitor("Errors", process.getErrorStream()), "Goniometer error stream");
		errorThread.start();

		return process != null;
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
				System.out.println(streamType + " stream entered");
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
				System.out.printf("Stream %s exited after %d lines, process %s\n", streamType, n, process.isAlive() ? "Alive" : "Dead");

			}
			catch (IOException e) {
				System.out.println("Input stream terminated");
			}
			
		}
		
	}
	
	
	public ArrayList<String> generateCommand() throws GoniometerException {
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

		ArrayList<String> cmd = new ArrayList<>();
		cmd.add(exe.getAbsolutePath());
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
		cmd.add(params.outputDirectory);
		
		
		return cmd;
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
	
}
