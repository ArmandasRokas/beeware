package dk.dtu.group22.beeware.dal.dto.interfaces;

public class NameIdPair {
    private String name;
    private int id;
    private boolean active;

    public NameIdPair(String name, int id, boolean active) {
        this.name = name;
        this.id = id;
        this.active = active;
    }

    @Override
    public String toString() {
        return "NameIdPair{" +
                "name='" + name + '\'' +
                ", id=" + id +
                ", active=" + active +
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
}
