package fastlocdisplay.goniometer;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.ProcessHandle.Info;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Stream;

import javax.swing.Timer;

// test  a few things about process starting monitoring and stopping
public class ProcessTest {

	String cmd = "C:\\Windows\\system32\\cmd.exe";
	
	private volatile Process process;
	
	public static void main(String[] args) {
		new ProcessTest().test();
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
		timerAction();
		
		Timer timer = new Timer(2000, new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				timerAction();
			}
		});
		timer.start();
		
		String[] cmds = {cmd};
		try {
			// a process launched this way will exit when PAMGuard exits. 
			process = Runtime.getRuntime().exec(cmds, null, null);
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("Result: " + process.toString());
		InputStreamMonitor isr = new InputStreamMonitor("Input", process.getInputStream());
		Thread t = new Thread(isr);
		t.start();
		try {
			t.join();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("Program temrinated");
	}

	int callCount = 0;
	protected void timerAction() {
		if (callCount++ == 0) {
			ProcessHandle procHandle = findProcessHandle("cmd.exe");
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
				System.out.println("Unable to find cmd.exe");
			}
		}
		System.out.println("Time check: " + process.info().command());
		
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
		
		public InputStreamMonitor(String streamType, InputStream inputStream) {
			super();
			this.streamType = streamType;
			this.inputStream = inputStream;
		}

		@Override
		public void run() {
			InputStreamReader isr = new InputStreamReader(inputStream);
			BufferedReader bufferedReader = new BufferedReader(isr);
			String line;
			try {
				System.out.println("Input stream entered");
				while ((line = bufferedReader.readLine()) != null) {
					System.out.println(line);
				}
				System.out.println("Input stream exited");
				/*
				 *  will exit to here when the process ends without throwing exception so shouldn't 
				 *  really use this to set complete. Need to do that using a more sophisticated 
				 *  process monitor. 
				 */
//				getBatchDataUnit().getBatchJobInfo().jobStatus = BatchJobStatus.COMPLETE;
			}
			catch (IOException e) {
				System.out.println("Input stream terminated");
			}
			
		}
		
	}

}
