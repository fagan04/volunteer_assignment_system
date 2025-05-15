package server.core;

import model.Volunteer;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;

public class PreferenceStore {
    private final ConcurrentHashMap<Long, Volunteer> volunteerMap;

    public PreferenceStore()
    {
        volunteerMap = new ConcurrentHashMap<>();
    }

    /**
     * Add or update a volunteer's preferences.
     *
     * @param volunteer Volunteer object with ID, name, and preferences.
     */
    public void addOrUpdatePreferences(Volunteer volunteer)
    {
        volunteerMap.put(volunteer.getId(), volunteer);
    }

    /**
     * Get all current volunteer data.
     *
     * @return Collection of volunteers.
     */
    public Collection<Volunteer> getAllVolunteers()
    {
        return volunteerMap.values();
    }

    /**
     * Retrieve a specific volunteer by ID.
     *
     * @param id Volunteer ID.
     * @return Volunteer object or null.
     */
    public Volunteer getVolunteerById(long id)
    {
        return volunteerMap.get(id);
    }

    /**
     * Clear all stored data (used only if needed).
     */
    public void clear()
    {
        volunteerMap.clear();
    }
}
