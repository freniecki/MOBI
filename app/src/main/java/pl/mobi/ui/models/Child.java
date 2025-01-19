package pl.mobi.ui.models;

public class Child {
    private String id;
    private String email;

    public Child(String id, String email) {
        this.id = id;
        this.email = email;
    }

    public String getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    @Override
    public String toString() {
        return email;
    }
}
