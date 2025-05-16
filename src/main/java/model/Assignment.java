package model;

/**
 * Represents an assignment of a volunteer to a service,
 * including the volunteer ID, the assigned service name, and the cost of the assignment.
 */
public class Assignment {
    private int volunteerId;     // Unique identifier of the volunteer
    private String serviceName;  // Name of the service to which the volunteer is assigned
    private double cost;         // Cost associated with the assignment

    /**
     * Constructs an Assignment instance with given volunteer ID, service name, and cost.
     *
     * @param volunteerId the ID of the volunteer
     * @param serviceName the name of the assigned service
     * @param cost        the cost of the assignment
     */
    public Assignment(int volunteerId, String serviceName, double cost) {
        this.volunteerId = volunteerId;
        this.serviceName = serviceName;
        this.cost = cost;
    }

    /**
     * Gets the ID of the volunteer.
     *
     * @return volunteer ID
     */
    public int getVolunteerId() {
        return volunteerId;
    }

    /**
     * Gets the name of the service assigned.
     *
     * @return service name
     */
    public String getServiceName() {
        return serviceName;
    }

    /**
     * Gets the cost of the assignment.
     *
     * @return cost
     */
    public double getCost() {
        return cost;
    }

    /**
     * Returns a string representation of the assignment.
     *
     * @return string containing volunteer ID, service name, and cost
     */
    @Override
    public String toString() {
        return "Assignment{" +
                "volunteerId=" + volunteerId +
                ", serviceName='" + serviceName + '\'' +
                ", cost=" + cost +
                '}';
    }
}
