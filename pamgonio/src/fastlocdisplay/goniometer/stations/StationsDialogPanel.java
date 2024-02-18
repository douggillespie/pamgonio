package fastlocdisplay.goniometer.stations;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;

import PamController.PamController;
import PamView.dialog.PamDialogPanel;
import PamView.dialog.warn.WarnOnce;
import PamView.tables.SwingTableColumnWidths;
import PamView.tables.TableColumnWidthData;
import fastlocdisplay.goniometer.FilePermissions;

/**
 * dialog panel for editing station id's list. 
 * @author dg50
 *
 */
public class StationsDialogPanel implements PamDialogPanel {
	
	private StationsFileData stationsFileData;
	
	private JPanel mainPanel;

	private TableModel tableModel;

	private JTable table;

	private static String[] colNames = {"Hex ID", "Integer ID"}; 
	
	private JLabel fileEditStatus;
	
	private JButton addButton;

	private boolean fileEditable;
	
	public StationsDialogPanel(StationsFileData stationsFileData) {
		this.stationsFileData = stationsFileData;
		this.mainPanel = new JPanel(new BorderLayout());
//		mainPanel.setb
		tableModel = new TableModel();
		table = new JTable(tableModel);
		JScrollPane scrollPane = new JScrollPane(table);
		mainPanel.add(BorderLayout.CENTER, scrollPane);
		JPanel topPanel = new JPanel(new BorderLayout());
		topPanel.add(BorderLayout.NORTH, fileEditStatus = new JLabel(" ", JLabel.LEFT));
		mainPanel.add(BorderLayout.NORTH, topPanel);
		JPanel ctrlPanel = new JPanel();
		ctrlPanel.setLayout(new BoxLayout(ctrlPanel, BoxLayout.Y_AXIS));
		ctrlPanel.add(addButton = new JButton("Add ID..."));
		mainPanel.add(BorderLayout.EAST, ctrlPanel);
		
		new SwingTableColumnWidths("Goniometer Stations Table", table);
		
		addButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				addLine();
			}
		});
		
		table.addMouseListener(new TableMouse());
	}

	protected void addLine() {
		stationsFileData.addStationId(new StationId(0,0));
		tableModel.fireTableDataChanged();
	}

	@Override
	public JComponent getDialogComponent() {
		return mainPanel;
	}

	private class TableMouse extends MouseAdapter {

		@Override
		public void mousePressed(MouseEvent e) {
			if (e.isPopupTrigger()) {
				showPopMenu(e);
			}
		}

		@Override
		public void mouseReleased(MouseEvent e) {
			if (e.isPopupTrigger()) {
				showPopMenu(e);
			}
		}
		
	}
	
	public void showPopMenu(MouseEvent e) {
		if (fileEditable == false) {
			return;
		}
		int row = table.getSelectedRow();
		if (row < 0 || row > stationsFileData.getStationIds().size()) {
			return;
		}
		StationId station = stationsFileData.getStationIds().get(row);
		String str = "Delete station " + station;
		JPopupMenu popMenu = new JPopupMenu();
		JMenuItem menuItem = new JMenuItem(str);
		menuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				deleteStation(station);
			}
		});
		popMenu.add(menuItem);
		popMenu.show(e.getComponent(), e.getX(), e.getY());;
	}

	protected void deleteStation(StationId station) {
		String msg = "Are you sure you want to delete station " + station.toString();
		int ans = WarnOnce.showNamedWarning("gonistationdelete", PamController.getMainFrame(), "Delete Station", msg, WarnOnce.OK_CANCEL_OPTION);
		if (ans == WarnOnce.CANCEL_OPTION) {
			return;
		}
		stationsFileData.stationIds.remove(station);
		tableModel.fireTableDataChanged();
	}

	@Override
	public void setParams() {
		tableModel.fireTableDataChanged();
		fileEditable = FilePermissions.canWrite(stationsFileData.filePath);
		if (fileEditable) {
			fileEditStatus.setText(stationsFileData.filePath + " (writable)");
		}
		else {
			fileEditStatus.setText(stationsFileData.filePath + " (locked)");
		}
		if (fileEditable == false) {
			String msg = String.format("The file \"%s\" is locked for editing, so you cannot make changes"
					+ " to the stations list until you change the file permissions from file explorer", stationsFileData.filePath);
			WarnOnce.showWarning("Argos Stations", msg, WarnOnce.WARNING_MESSAGE);
		}
		enableControls(fileEditable);
	}

	private void enableControls(boolean fileEditable) {
		addButton.setEnabled(fileEditable);
	}

	@Override
	public boolean getParams() {
		return fileEditable; // always OK I think. 
	}
	
	/**
	 * Called when a cell has been manually edited. 
	 * @param aValue 
	 * @param rowIndex
	 * @param columnIndex 
	 */
	public void updateStationData(Object aValue, int rowIndex, int columnIndex) {
		int hexVal, intVal;
		if (aValue instanceof String == false) {
			return;
		}
		StationId station = stationsFileData.getStationIds().get(rowIndex);
		String str = (String) aValue;
		if (columnIndex == 0) {
			try {
				hexVal = Integer.decode("0x"+str);
			}
			catch (NumberFormatException e) {
				WarnOnce.showWarning("Invalid hex id", e.getMessage(), WarnOnce.WARNING_MESSAGE);
				return;
			}
			station.setHexId(hexVal);
		}
		if (columnIndex == 1) {
			try {
				intVal = Integer.valueOf(str);
			}
			catch (NumberFormatException e) {
				WarnOnce.showWarning("Invalid Integer id", e.getMessage(), WarnOnce.WARNING_MESSAGE);
				return;
			}
			station.setIntegerId(intVal);
		}
	}

	private class TableModel extends AbstractTableModel {

		private static final long serialVersionUID = 1L;

		@Override
		public int getRowCount() {
			return stationsFileData.getStationIds().size();
		}

		@Override
		public int getColumnCount() {
			return colNames.length;
		}

		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			StationId station = stationsFileData.getStationIds().get(rowIndex);
			switch (columnIndex) {
			case 0:
				return String.format("%07X", station.getHexId());
			case 1:
				return station.getIntegerId();
			}
			return null;
		}

		@Override
		public String getColumnName(int column) {
			return colNames[column];
		}

		@Override
		public boolean isCellEditable(int rowIndex, int columnIndex) {
			return fileEditable;
		}

		@Override
		public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
			updateStationData(aValue, rowIndex, columnIndex);
		}

		@Override
		public void fireTableCellUpdated(int row, int column) {
			super.fireTableCellUpdated(row, column);
		}
		
	}

}
