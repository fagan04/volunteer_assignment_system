package model;

/**
 * Represents a volunteer's preference for a service,
 * including the service name and its ranking (priority).
 */
public class Preference {
    private String serviceName; // Name of the service
    private int priority;       // Priority ranking (1 = highest, 5 = lowest)

    /**
     * Constructs a Preference with the given service name and priority.
     *
     * @param serviceName the name of the service
     * @param priority    the priority rank (1 to 5)
     */
    public Preference(String serviceName, int priority) {
        this.serviceName = serviceName;
        this.priority = priority;
    }

    /**
     * Gets the service name.
     *
     * @return service name
     */
    public String getServiceName() {
        return serviceName;
    }

    /**
     * Gets the priority rank.
     *
     * @return priority (1 = highest, 5 = lowest)
     */
    public int getPriority() {
        return priority;
    }

    /**
     * Sets the service name.
     *
     * @param serviceName new service name
     */
    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    /**
     * Sets the priority rank.
     *
     * @param priority new priority value
     */
    public void setPriority(int priority) {
        this.priority = priority;
    }

    /**
     * Returns a string representation of the preference.
     *
     * @return service name with priority
     */
    @Override
    public String toString() {
        return serviceName + " (priority: " + priority + ")";
    }
}
