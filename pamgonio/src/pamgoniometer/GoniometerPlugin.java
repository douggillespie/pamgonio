package pamgoniometer;

import PamModel.PamDependency;
import PamModel.PamPluginInterface;

public class GoniometerPlugin implements PamPluginInterface {

	private String jarFile;

	@Override
	public String getDefaultName() {
		return "Goniometer display";
	}

	@Override
	public String getHelpSetName() {
		return null;
	}

	@Override
	public void setJarFile(String jarFile) {
		this.jarFile = jarFile;
	}

	@Override
	public String getJarFile() {
		return jarFile;
	}

	@Override
	public String getDeveloperName() {
		return "Doug Gillespie";
	}

	@Override
	public String getContactEmail() {
		return "dg50@st-andrews.ac.uk";
	}

	@Override
	public String getVersion() {
		return "1.0";
	}

	@Override
	public String getPamVerDevelopedOn() {
		return "2.02.10";
	}

	@Override
	public String getPamVerTestedOn() {
		return "2.02.10";
	}

	@Override
	public String getAboutText() {
		return "Receive goniometer data";
	}

	@Override
	public String getClassName() {
		return GoniometerControl.class.getName();
	}

	@Override
	public String getDescription() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getMenuGroup() {
		return "Maps and Mapping";
	}

	@Override
	public String getToolTip() {
		return "Read goniometer AIS data";
	}

	@Override
	public PamDependency getDependency() {
		return null;
	}

	@Override
	public int getMinNumber() {
		return 0;
	}

	@Override
	public int getMaxNumber() {
		return 1;
	}

	@Override
	public int getNInstances() {
		return 0;
	}

	@Override
	public boolean isItHidden() {
		return false;
	}

	@Override
	public int allowedModes() {
		return PamPluginInterface.ALLMODES;
	}

}
