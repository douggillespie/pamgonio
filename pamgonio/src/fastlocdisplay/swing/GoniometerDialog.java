package fastlocdisplay.swing;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import PamUtils.PamFileChooser;
import PamUtils.PamFileFilter;
import PamUtils.SelectFolder;
import PamView.dialog.PamDialog;
import PamView.dialog.PamGridBagContraints;
import fastlocdisplay.goniometer.GoniometerParams;
import serialComms.SerialPortPanel;

public class GoniometerDialog extends PamDialog {
	
	private static final long serialVersionUID = 1L;
	private static GoniometerDialog singleInstance;
	private GoniometerParams goniometerParams;
	
	private JRadioButton noControl, controlInternal, controlExternal;
	private JTextField exeFolder;
	private JTextField exeName;
	private JButton browseButton;
	private SerialPortPanel navPort, goniPort, outPort;
	private SelectFolder outputFolder;
	private JCheckBox debugOut;

	private GoniometerDialog(Window parentFrame) {
		super(parentFrame, "Goniometer Settings", true);
		
		noControl = new JRadioButton(GoniometerParams.STR_NOCONTROL);
		controlInternal = new JRadioButton(GoniometerParams.STR_INTERNALCONTROL);
		controlExternal = new JRadioButton(GoniometerParams.STR_EXTERNALCONTROL);
		ButtonGroup bg = new ButtonGroup();
		bg.add(noControl);
		bg.add(controlInternal);
		bg.add(controlExternal);
		exeFolder = new JTextField(50);
		exeName = new JTextField(20);
		browseButton = new JButton("Browse");
		navPort = new SerialPortPanel("Nav port", false, false, false, false, false);
		goniPort = new SerialPortPanel("Goniometer", false, false, false, false, false);
		outPort = new SerialPortPanel("Output port", false, false, false, false, false);
		outputFolder = new SelectFolder("Output folder", 50, true);
		debugOut = new JCheckBox("Show debug output");
		outputFolder.setSubFolderButtonName("Put files into separate folders by date");
		outputFolder.setSubFolderButtonToolTip("Sub folders will be automatically created for each date");
		navPort.getPanel().setToolTipText("Com PORT for GPS ephemeris data input");
		goniPort.getPanel().setToolTipText("Com PORT for Goniometer data input");
		outPort.getPanel().setToolTipText("Com PORT for AIS data output");
		noControl.setToolTipText("No control. you must launch FastGPS_Realtime yourself in a command window");
		controlInternal.setToolTipText("PAMGuard will control FastGPS_Realtime. Feedback within PAMGuard, but FAST_GPS_Realtime will close if PAMGUard closes");
		controlExternal.setToolTipText("PAMGuard will launch FastGPS_Realtime. No feedback, but FAST_GPS_Realtime will continue if PAMGUard closes");
		debugOut.setToolTipText("Launch FastGPS_Realtime with -d option");
		
		JPanel goniPanel = new JPanel(new GridBagLayout());
		goniPanel.setBorder(new TitledBorder("Parameters for goniometer data collection and FastGPS_Realtime control"));
		
		GridBagConstraints c = new PamGridBagContraints();
		c.gridwidth = 4;
		goniPanel.add(noControl, c);
		c.gridy++;
		goniPanel.add(controlInternal, c);
		c.gridy++;
		goniPanel.add(controlExternal, c);
		c.gridy++;
		goniPanel.add(new JLabel("Folder for FastGPS_Realtime executable", JLabel.LEFT), c);
		c.gridy++;
		goniPanel.add(exeFolder, c);
		c.gridx = 0;
		c.gridwidth = 1;
		c.gridy++;
		goniPanel.add(new JLabel("Executable", JLabel.RIGHT), c);
		c.gridx+=c.gridwidth;
		c.gridwidth = 2;
		goniPanel.add(exeName, c);
		c.gridx += c.gridwidth;
		c.gridwidth = 1;
		goniPanel.add(browseButton, c);
		c.gridy++;
		c.gridx = 0;
		c.gridwidth = 2;
		goniPanel.add(new JLabel("COM Ports for Fastloc software", JLabel.LEFT), c);
		c.gridx += c.gridwidth;
		c.gridwidth = 1;
		goniPanel.add(debugOut, c);
		c.gridwidth = 1;
		c.gridy++;
		c.gridx=0;
		goniPanel.add(navPort.getPanel(), c);
		c.gridx++;
		goniPanel.add(goniPort.getPanel(), c);
		c.gridx++;
		goniPanel.add(outPort.getPanel(), c);
		c.gridx = 0;
		c.gridy++;
		c.gridwidth = 4;
		goniPanel.add(new JLabel("Output Folder", JLabel.LEFT), c);
		c.gridy++;
		goniPanel.add(outputFolder.getFolderPanel(), c);
		
		
		
		setDialogComponent(goniPanel);
		
		exeFolder.setEditable(false);
		exeName.setEditable(false);
		noControl.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				selectControlRealtime();
			}
		});
		controlInternal.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				selectControlRealtime();
			}
		});
		controlExternal.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				selectControlRealtime();
			}
		});
		browseButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				browseExecutable();
			}
		});
		
		setHelpPoint("docs.goniometer");
	}
	
	protected void selectControlRealtime() {
		enableControls();
	}

	private void enableControls() {
		boolean isRT = noControl.isSelected() == false;
//		browseButton.setEnabled(isRT);
//		navPort.getPortList().setEnabled(isRT);
//		goniPort.getPortList().setEnabled(isRT);
//		outPort.getPortList().setEnabled(isRT);
//		debugOut.setEnabled(isRT);
	}

	protected void browseExecutable() {
		PamFileFilter fileFilter = new PamFileFilter("FastGPS_Realtime executable", ".exe");
//		String currDir = exeFolder.getText();
//		if (currDir != null) {
//			File currD = new File(currDir);
//		}
		PamFileChooser fc = new PamFileChooser(exeFolder.getText());
		fc.setFileFilter(fileFilter);
//		fc.set
		int ans = fc.showOpenDialog(getParent());
		if (ans == JFileChooser.APPROVE_OPTION) {
			File sel = fc.getSelectedFile();
			if (sel == null || sel.exists() == false) {
				return;
			}
			else {
				setExecutable(sel);
			}
		}
	}

	private void setExecutable(File sel) {
		String name = sel.getName();
		String fold = sel.getParent();
		exeName.setText(name);
		exeFolder.setText(fold);
	}

	public static GoniometerParams showDialog(Window parentFrame, GoniometerParams goniometerParams) {
//		if (singleInstance == null) {
			singleInstance = new GoniometerDialog(parentFrame);
//		}
		singleInstance.goniometerParams = goniometerParams;
		singleInstance.setParams();
		singleInstance.setVisible(true);
		return singleInstance.goniometerParams;
	}

	private void setParams() {
		noControl.setSelected(goniometerParams.controlFastRealtime == GoniometerParams.GONIOMETER_NOCONTROL);
		controlInternal.setSelected(goniometerParams.controlFastRealtime == GoniometerParams.GONIOMETER_INTERNALCONTROL);
		controlExternal.setSelected(goniometerParams.controlFastRealtime == GoniometerParams.GONIOMETER_EXTERNALCONTROL);
		exeFolder.setText(goniometerParams.fastGPSFolder);
		exeName.setText(goniometerParams.fastGPSexe);
		navPort.setPort(goniometerParams.navPort);
		goniPort.setPort(goniometerParams.gonioComPort);
		outPort.setPort(goniometerParams.outPort);
		outputFolder.setFolderName(goniometerParams.outputDirectory);
		outputFolder.setIncludeSubFolders(goniometerParams.autoDatedFolders);
		debugOut.setSelected(goniometerParams.debugOutput);
		
		enableControls();
	}

	@Override
	public boolean getParams() {
		if (noControl.isSelected()) {
			goniometerParams.controlFastRealtime = GoniometerParams.GONIOMETER_NOCONTROL;
		}
		if (controlInternal.isSelected()) {
			goniometerParams.controlFastRealtime = GoniometerParams.GONIOMETER_INTERNALCONTROL;
		}
		if (controlExternal.isSelected()) {
			goniometerParams.controlFastRealtime = GoniometerParams.GONIOMETER_EXTERNALCONTROL;
		}
		goniometerParams.debugOutput = debugOut.isSelected();
		goniometerParams.navPort = navPort.getPort();
		goniometerParams.gonioComPort = goniPort.getPort();
		goniometerParams.outPort = outPort.getPort();
		
		
		boolean runExe = goniometerParams.controlFastRealtime > 0;
		if (runExe) {
			File exeFile = getExeFile();
			if (exeFile == null || exeFile.exists() == false) {
				return showWarning("you must select a valid FastGPS_Realtime.exe");
			}
			if (goniometerParams.navPort == null) {
				return showWarning("you must select a valid COM port for navigation data");
			}
			if (goniometerParams.gonioComPort == null) {
				return showWarning("you must select a valid COM port for goniometer data");
			}
			if (goniometerParams.gonioComPort.equals(goniometerParams.navPort)) {
				return showWarning("The goniometer COM port cannot be the same as the navigation data port");
			}
			if (goniometerParams.outPort == null) {
				return showWarning("you must select a valid COM port for output data");
			}
			if (goniometerParams.outPort.equals(goniometerParams.navPort)) {
				return showWarning("The output COM port cannot be the same as the navigation data port");
			}
			if (goniometerParams.outPort.equals(goniometerParams.gonioComPort)) {
				return showWarning("The output COM port cannot be the same as the goniometer data port");
			}
		}
		String outfolder = outputFolder.getFolderName(true);
		if (outfolder == null) {
			return showWarning("you must specify a valid output folder");
		}
		File outDir = new File(outfolder);
		if (outDir.exists() == false) {
			return showWarning("The output folder does not exist");
		}
		
		goniometerParams.outputDirectory = outfolder;
		goniometerParams.autoDatedFolders = outputFolder.isIncludeSubFolders();
		
		return true;
	}
	
	File getExeFile() {
		String folder = exeFolder.getText();
		String name = exeName.getText();
		if (folder == null || name == null) {
			return null;
		}
		if (folder.length() == 0 || name.length() == 0) {
			return null;
		}
		return new File(folder + File.separator + name);
	}

	@Override
	public void cancelButtonPressed() {
		// TODO Auto-generated method stub

	}

	@Override
	public void restoreDefaultSettings() {
		// TODO Auto-generated method stub

	}

}
