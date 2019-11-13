package dk.dtu.group22.beeware.dal.dto.interfaces;

public class NameIdPair {
    private String name;
    private int id;

    public NameIdPair(String name, int id) {
        this.name = name;
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public int getID() {
        return id;
    }

}
