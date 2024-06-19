package org.example;

public class User {

    private String name;
    private String role;

    public User(String name, String role) {
        this.name = name;
        this.role = role;
    }

    public String getUpperName() {
        return name.substring(0,1).toUpperCase() + name.substring(1);
    }

    public String getRole() {
        return role;
    }
}
