package fastlocdisplay.goniometer;

import java.awt.Desktop;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.ProcessHandle.Info;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Stream;

import javax.swing.Timer;

// test  a few things about process starting monitoring and stopping
public class ProcessTest {

	String cmd = "C:\\Windows\\system32\\cmd.exe";
	String cmd2 = "C:\\ProgramData\\FastGPS Realtime Solution\\FastGPS_Realtime.exe";
	String opDir = "C:\\PAMGuardTest\\Goniometer\\TestOutput";
	String bat = "C:\\ProgramData\\FastGPS Realtime Solution\\cmdcmd.bat";
	
	private volatile Process process;
	private Thread inputThread, errorThread;
	
	public static void main(String[] args) {
		new ProcessTest().test4();
	}
	
	private void test4() {
//		try {
//			Desktop.getDesktop().open(new File(cmd));
////			Desktop.getDesktop().
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
////		System.console().
//
//		ArrayList<String> commands = new ArrayList<>();
//		commands.add(cmd);
//		commands.add("dir\r\n");
//
//		ProcessBuilder pb = new ProcessBuilder(commands);
//		
//		try {
//			pb.inheritIO();
//			process = pb.start();
//			process.waitFor();
//			System.out.println("Started process" + process);
//		} catch (IOException e) {
//			e.printStackTrace();
//		} catch (InterruptedException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		Runtime.getRuntime().
		File batFile = new File(bat);
		try {
			Desktop.getDesktop().open(batFile);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void test3() {
		String[] cmds = new String[10];
		cmds[0] = cmd2;
		cmds[1] = "-d";
		cmds[2] = "-n";
		cmds[3] = "COM3";
		cmds[4] = "-o";
		cmds[5] = "COM5";
		cmds[6] = "-oa";
		cmds[7] = "COM9";
		cmds[8] = "-out";
		cmds[9] = opDir;
//		cmds = Arrays.copyOf(cmds, 1);
		ArrayList<String> commands = new ArrayList<>();
		commands.add(cmd);
		commands.add("/c");
		for (int i = 0; i < 10; i++) {
			commands.add(cmds[i]);
		}
		
		ProcessBuilder pb = new ProcessBuilder(commands);
		
		try {
			process = pb.start();
			System.out.println("Started process" + process);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
//		try {
//			Thread.sleep(10000);
//		} catch (InterruptedException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		System.
//
		InputStreamMonitor isr = new InputStreamMonitor("Input", process.getInputStream());
		inputThread = new Thread(isr, "Input reader");
		inputThread.start();
		InputStreamMonitor esr = new InputStreamMonitor("Errors", process.getErrorStream());
		errorThread = new Thread(esr, "Error Reader");
		errorThread.start();
		
		try {
			inputThread.join();
			errorThread.join();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("Program terminated");
	}

	private void test2() {
		ProcessHandle cmdProc = findProcessHandle("cmd.exe");
		if (cmdProc == null) {
			System.out.println("Unable to find command process");
		}
		Info info = cmdProc.info();
//		info.
		
	}

	private void test() {
//		timerAction();
		
		Timer timer = new Timer(2000, new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				timerAction();
			}
		});
		
		String[] cmds = new String[10];
		cmds[0] = cmd2;
		cmds[1] = "-d";
		cmds[2] = "-n";
		cmds[3] = "COM3";
		cmds[4] = "-o";
		cmds[5] = "COM5";
		cmds[6] = "-oa";
		cmds[7] = "COM9";
		cmds[8] = "-out";
		cmds[9] = opDir;
		cmds = Arrays.copyOf(cmds, 4);
		try {
			// a process launched this way will exit when PAMGuard exits. 
			process = Runtime.getRuntime().exec(cmds, null, null);
			// wait a few seconds for it to launch
			for (int i = 0; i < 50; i++) {
				if (process.isAlive()) {
					break;
				}
				Thread.sleep(10);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
		System.out.printf("Program status %s - %s\n", process.isAlive() ? "Running" : "Dead", process.toString());
		timer.start();
		InputStreamMonitor isr = new InputStreamMonitor("Input", process.getInputStream());
		inputThread = new Thread(isr, "Input reader");
		inputThread.start();
		InputStreamMonitor esr = new InputStreamMonitor("Errors", process.getErrorStream());
		errorThread = new Thread(esr, "Error Reader");
		errorThread.start();
		
		try {
			inputThread.join();
			errorThread.join();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("Program terminated");
	}

	int callCount = 0;
	protected void timerAction() {
		if (callCount++ <1) {
			ProcessHandle procHandle = findProcessHandle("FastGPS_Realtime.exe");
			if (procHandle != null) {
				System.out.println("Found " + procHandle.pid() + " " + procHandle.info());
				Optional<ProcessHandle> parentH = procHandle.parent();
				while (parentH.isPresent()) {
					procHandle = parentH.get();
					System.out.println("Parent " + procHandle + " " + procHandle.info());
					parentH = procHandle.parent();
				}
			}
			else {
				System.out.println("Unable to find FastGPS_Realtime.exe");
			}
		}
		if (process != null) {
			boolean alive = process.isAlive();
			if (alive == false) {
//					try {
//						process.getInputStream().close();
//					} catch (IOException e) {
//						// TODO Auto-generated catch block
//						e.printStackTrace();
//					}
//				if (inputThread != null) {
//					inputThread.interrupt();
//					inputThread.;
//				}
//					isr.close();
			}
//			System.out.printf("Time check proc %s %s\n", process.info(), process.info().command());
		}
		
	}

	ProcessHandle foundHandle = null;
	protected ProcessHandle findProcessHandle(String commandName) {
//		try {
//			String cmd = System.getenv("windir") +"\\system32\\"+"tasklist.exe";
//			String [] cmds = {cmd};
//		    String line;
//		    Process p = Runtime.getRuntime().exec(cmds, null, null);
//		    BufferedReader input =
//		            new BufferedReader(new InputStreamReader(p.getInputStream()));
//		    while ((line = input.readLine()) != null) {
//		    	if (line.contains(commandName)) {
//		    		System.out.println(line); //<-- Parse data here.
//		    	}
//		    }
//		    input.close();
//		} catch (Exception err) {
//		    err.printStackTrace();
//		}
		foundHandle = null;
		Stream<ProcessHandle> processes = ProcessHandle.allProcesses();
		processes.forEach(new Consumer<ProcessHandle>() {
			@Override
			public void accept(ProcessHandle processHandle) {
				Info info = processHandle.info();
				Optional<String> icmd = info.command();
				if (icmd == null) {
					return;
				}
				if (icmd.toString().contains(commandName)) {
					foundHandle = processHandle;
				}
//				System.out.println(info.command());
			}
		});
		return foundHandle;
	}

//	private class OutputStreamMonitor implements Runnable {
//		private String streamType;
//		
//		private InputStream outputStream;
//		
//		public OutputStreamMonitor(String streamType, InputStream inputStream) {
//			super();
//			this.streamType = streamType;
//			this.outputStream = inputStream;
//		}
//
//		@Override
//		public void run() {
//			OutputStreamReade isr = new OutputStreamWriter(inputStream);
//			BufferedReader bufferedReader = new BufferedReader(isr);
//			String line;
//			try {
//				while ((line = bufferedReader.readLine()) != null) {
////					publish(new ProcessProgress(batchDataUnit, line));
//					System.out.printf("Job %d: %s: %s\n", getBatchDataUnit().getDatabaseIndex(), streamType, line);
//				}
//				/*
//				 *  will exit to here when the process ends without throwing exception so shouldn't 
//				 *  really use this to set complete. Need to do that using a more sophisticated 
//				 *  process monitor. 
//				 */
////				getBatchDataUnit().getBatchJobInfo().jobStatus = BatchJobStatus.COMPLETE;
//			}
//			catch (IOException e) {
////				getBatchDataUnit().getBatchJobInfo().jobStatus = BatchJobStatus.COMPLETE;
//			}
//			updateJobStatus();
//			
//		}
//	}
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
				int ch;
				char[] chBuf = new char[1];
//				while (2>1) {
//					while(isr.read(chBuf)>0) {
//						System.out.print(chBuf[0]);
//					}
//				}
				while ((line = bufferedReader.readLine()) != null) {
					n++;
					System.out.println(streamType + ":" + line);
				}
				//				while ((line = isr. .readLine()) != null) {
//					n++;
////					if (inputStream.available() > 0) {
////						;
//						System.out.println(streamType + ":" + line);
////					}
////					else {
////						try {
////							Thread.sleep(1);
////						} catch (InterruptedException e) {
////						}
////					}
//				}
				System.out.printf("Stream %s exited after %d lines, process %s\n", streamType, n, process.isAlive() ? "Alive" : "Dead");

			}
			catch (IOException e) {
				System.out.println("Input stream terminated");
			}
			
		}
		
	}

}
