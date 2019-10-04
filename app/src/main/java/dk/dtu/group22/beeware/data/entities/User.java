package dk.dtu.group22.beeware.data.entities;

public class User {

    private int id = 0;
    private String username;

    public void setId(int id) {
        this.id =id;
    }

    public int getId() {
        return this.id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
