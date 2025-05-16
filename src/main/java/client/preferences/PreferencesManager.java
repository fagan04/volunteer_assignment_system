package client.preferences;

import model.Preference;

import java.util.List;

/**
 * Manages the currently selected service preferences of the volunteer.
 * Acts as a simple in-memory storage.
 */
public class PreferencesManager {

    // Holds the volunteer's current list of preferences
    private List<Preference> currentPreferences;

    /**
     * Sets the current list of preferences.
     * Called when the user submits or updates their preferences.
     *
     * @param preferences List of Preference objects ranked by user
     */
    public void setPreferences(List<Preference> preferences) {
        this.currentPreferences = preferences;
    }

    /**
     * Retrieves the current list of preferences.
     *
     * @return List of current Preference objects
     */
    public List<Preference> getPreferences() {
        return currentPreferences;
    }
}
