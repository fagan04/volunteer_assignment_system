package model;

import java.util.List;

public class Volunteer {
    private int id;
    private String name;
    private List<Preference> preferences;

    public Volunteer(int id, String name, List<Preference> preferences) {
        this.id = id;
        this.name = name;
        this.preferences = preferences;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public List<Preference> getPreferences() {
        return preferences;
    }

    public void setPreferences(List<Preference> preferences) {
        this.preferences = preferences;
    }

    @Override
    public String toString() {
        return "Volunteer{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", preferences=" + preferences +
                '}';
    }
}
