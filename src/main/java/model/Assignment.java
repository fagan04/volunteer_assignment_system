package model;

public class Assignment {
    private int volunteerId;
    private String serviceName;
    private double cost;

    public Assignment(int volunteerId, String serviceName, double cost) {
        this.volunteerId = volunteerId;
        this.serviceName = serviceName;
        this.cost = cost;
    }

    public int getVolunteerId() {
        return volunteerId;
    }

    public String getServiceName() {
        return serviceName;
    }

    public double getCost() {
        return cost;
    }

    @Override
    public String toString() {
        return "Assignment{" +
                "volunteerId=" + volunteerId +
                ", serviceName='" + serviceName + '\'' +
                ", cost=" + cost +
                '}';
    }
}
