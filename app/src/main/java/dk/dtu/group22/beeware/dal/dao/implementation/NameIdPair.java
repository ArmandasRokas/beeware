package dk.dtu.group22.beeware.dal.dao.implementation;

public class NameIdPair {
    private String name;
    private int id;
    private boolean active;
    private String location;

    public NameIdPair(String name, int id, boolean active, String location) {
        this.name = name;
        this.id = id;
        this.active = active;
        this.location = location;
    }

    @Override
    public String toString() {
        return "NameIdPair{" +
                "name='" + name + '\'' +
                ", id=" + id +
                ", active=" + active +
                ", location =" + location +
                '}';
    }

    public String getName() {
        return name;
    }

    public int getID() {
        return id;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getLocation() {
        return location;
    }

}
