package fastlocdisplay.swing;

import java.awt.BorderLayout;
import java.awt.Component;

import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.PlainDocument;

import PamUtils.PamCalendar;
import PamView.dialog.PamTextArea;
import PamView.panel.PamPanel;
import fastlocdisplay.goniometer.GoniometerControl;
import userDisplay.UserDisplayComponent;

public class FastRTDisplay implements UserDisplayComponent {

	private GoniometerControl goniometerControl;
	
	private JPanel mainPanel;

	private String uniqueName;
	
	private JTextArea mainTextArea;

	private JScrollPane scrollPane;
	
	public FastRTDisplay(GoniometerControl gniometerControl) {
		super();
		this.goniometerControl = gniometerControl;
		mainPanel = new PamPanel(new BorderLayout());
		mainTextArea = new PamTextArea("");
		scrollPane = new JScrollPane(mainTextArea);
		mainPanel.add(BorderLayout.CENTER, scrollPane);
		
		goniometerControl.setFastRTDisplay(this);
	}

	@Override
	public Component getComponent() {
		return mainPanel;
	}

	@Override
	public void openComponent() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void closeComponent() {
		goniometerControl.setFastRTDisplay(null);		
	}

	@Override
	public void notifyModelChanged(int changeType) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getUniqueName() {
		return uniqueName;
	}

	@Override
	public void setUniqueName(String uniqueName) {
		this.uniqueName = uniqueName;
	}

	@Override
	public String getFrameTitle() {
		return "Goniometer data";
	}

	public void goniometerMessage(String type, String line) {
		long now = System.currentTimeMillis();
		String t = PamCalendar.formatDBDateTime(now);
		String txt = String.format("\n%s %s %s", t, type, line);
		Document doc = mainTextArea.getDocument();
		trimDocument(doc);
		JScrollBar vert = scrollPane.getVerticalScrollBar();
		boolean atMax = false;
		if (vert != null) {
			int val = vert.getValue();
			int max = vert.getMaximum();
			int vis = vert.getVisibleAmount();
			atMax = vert.getValue() >= vert.getMaximum()-vert.getVisibleAmount()-100;
//			System.out.printf("Val %d max %d, vis %d diff = %d atmax \n" + atMax, val, max, vis, val-(max-vis));
		}
		try {
			doc.insertString(doc.getLength(), txt, null);
			if (doc instanceof PlainDocument) {
				PlainDocument pDoc = (PlainDocument) doc;
//				pDoc.
				if (vert != null) {
					if (atMax) {
					SwingUtilities.invokeLater(new Runnable() {
						@Override
						public void run() {
							vert.setValue(vert.getMaximum());
						}
					});
					}
				}
			}
//			doc.
		} catch (BadLocationException e) {
			e.printStackTrace();
		}
		
	}

	private void trimDocument(Document doc) {
		int len = doc.getLength();
		int maxLen = 10000;
		if (len > maxLen) {
			try {
				doc.remove(0, len-maxLen);
			} catch (BadLocationException e) {
				// TODO Auto-generated catch block
//				e.printStackTrace();
			}
		}
	}

}
