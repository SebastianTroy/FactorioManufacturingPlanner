package application;

import java.util.prefs.Preferences;

public class PlannerApplicationPreferences
{
	private static final String factorioInstallationDirectoryKey = "installationDirectory";
	private static final Preferences prefs = Preferences.userNodeForPackage(PlannerApplicationPreferences.class);

	public static String getFactorioInstallationDirectory()
	{
		return prefs.get(factorioInstallationDirectoryKey, "C:/Program Files (x86)/Steam/steamapps/common/Factorio");
	}

	public static void setFactorioInstallationDirectory(String installationDirectory)
	{
		prefs.put(factorioInstallationDirectoryKey, installationDirectory);
	}
}
