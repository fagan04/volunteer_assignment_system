package client.preferences;

import model.Preference;

import java.util.List;

public class PreferencesManager {
    private List<Preference> currentPreferences;

    public void setPreferences(List<Preference> preferences) {
        this.currentPreferences = preferences;
    }

    public List<Preference> getPreferences() {
        return currentPreferences;
    }
}
