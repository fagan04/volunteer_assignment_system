package model;

public class Assignment {
    private final long volunteerId;
    private final String serviceName;
    private final double cost;

    public Assignment(long volunteerId, String serviceName, double cost)
    {
        this.volunteerId = volunteerId;
        this.serviceName = serviceName;
        this.cost = cost;
    }

    public long getVolunteerId()
    {
        return volunteerId;
    }

    public String getServiceName()
    {
        return serviceName;
    }

    public double getCost()
    {
        return cost;
    }

    @Override
    public String toString()
    {
        return "Assignment{" + "volunteerId=" + volunteerId + ", serviceName='" + serviceName + '\'' + ", cost=" + cost + '}';
    }
}
