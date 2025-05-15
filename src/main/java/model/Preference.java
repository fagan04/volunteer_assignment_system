package model;

public class Preference {
    private String serviceName;
    private int priority; // 1 = most preferred, up to 5

    public Preference(String serviceName, int priority) {
        this.serviceName = serviceName;
        this.priority = priority;
    }

    public String getServiceName() {
        return serviceName;
    }

    public int getPriority() {
        return priority;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    @Override
    public String toString() {
        return serviceName + " (priority: " + priority + ")";
    }
}
