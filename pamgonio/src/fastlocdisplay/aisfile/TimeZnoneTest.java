package fastlocdisplay.aisfile;

import java.util.TimeZone;

public class TimeZnoneTest {

	public static void main(String[] args) {

		new TimeZnoneTest().run();
	}

	private void run() {
		sayZone(null);
		String[] zones = TimeZone.getAvailableIDs();
		sayZone("Europe/Paris");
		sayZone("Canada/Eastern");
//		for (int i = 0; i < zones.length; i++) {
//			sayZone(zones[i]);
//		}
	}

	private void sayZone(String zoneName) {
		TimeZone tz;
		if (zoneName == null) {
			tz = TimeZone.getDefault();
		}
		else {
			tz = TimeZone.getTimeZone(zoneName);
		}
		long offset = tz.getOffset(System.currentTimeMillis());
		long rawOff = tz.getRawOffset();
		double onehour = 3600000;
		System.out.printf("Zone %s: offset %3.1fH, rawoffset %3.1fH\n", zoneName, offset/onehour, rawOff/onehour);
	}
}
