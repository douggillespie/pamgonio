package fastlocdisplay.data;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JPanel;

import PamView.dialog.PamDialogPanel;
import fastlocdisplay.FastStationDataBlock;
import fastlocdisplay.FastStationDataUnit;

public class AISSelDialogPanel implements PamDialogPanel {
	
	private AISStationSelectParams selectParams;
	private FastStationDataBlock fastStationDataBlock;
	
	private JPanel mainPanel;
	private ArrayList<FastStationDataUnit> stations;
	private JCheckBox[] checkBoxes;
	private JCheckBox selectAll;
	private Set<Integer> stationKeys;

	public AISSelDialogPanel(FastStationDataBlock fastStationDataBlock, AISStationSelectParams selectParams) {
		super();
		this.fastStationDataBlock = fastStationDataBlock;
		this.selectParams = selectParams;
		
		mainPanel = new JPanel();
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
		
		stations = fastStationDataBlock.getDataCopy();
		fastStationDataBlock.sortByIntegerId(stations);
		// make sure that every station is in the map, then work off the map
		for (FastStationDataUnit aStn : stations) {
			selectParams.isStationSelected(aStn.getIntegerId());
		}
		HashMap<Integer, Boolean> stationMap = selectParams.getStationMap();
		stationKeys = stationMap.keySet();
		selectAll = new JCheckBox("Select all");
		selectAll.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				doSelectAll();
			}
		});
		mainPanel.add(selectAll);
		checkBoxes = new JCheckBox[stationKeys.size()];
		Iterator<Integer> it = stationKeys.iterator();
		int iBox = 0;
		while (it.hasNext()) {
			Integer key = it.next();
			FastStationDataUnit dataUnit = fastStationDataBlock.findStationDataUnit(key);
			boolean sel = selectParams.isStationSelected(key);
			String txt = String.format("Tag %d", key);
			if (dataUnit != null) {
				txt += String.format(", 0x%X", dataUnit.getHexId());
			}
			checkBoxes[iBox] = new JCheckBox(txt);
			checkBoxes[iBox].setSelected(sel);
			checkBoxes[iBox].addActionListener(new BoxListener(key, checkBoxes[iBox]));
			mainPanel.add(checkBoxes[iBox]);
			iBox++;
		}
	}

	protected void doSelectAll() {
		boolean sel = selectAll.isSelected();
		for (int i = 0; i < checkBoxes.length; i++) {
			checkBoxes[i].setSelected(sel);
		}
	}
	
	private class BoxListener implements ActionListener {
		Integer key;
		public BoxListener(Integer key, JCheckBox box) {
			super();
			this.key = key;
			this.box = box;
		}
		JCheckBox box;
		@Override
		public void actionPerformed(ActionEvent e) {
			boolean sel = box.isSelected();
			if (sel == false) {
				selectAll.setSelected(false);
			}
		}
		
	}

	@Override
	public JComponent getDialogComponent() {
		return mainPanel;
	}

	@Override
	public void setParams() {
		
	}

	@Override
	public boolean getParams() {
		Iterator<Integer> it = stationKeys.iterator();
		int iBox = 0;
		while (it.hasNext()) {
			Integer key = it.next();
			selectParams.setStationSelected(key, checkBoxes[iBox].isSelected());
			iBox++;
		}
		return true;
	}

}
