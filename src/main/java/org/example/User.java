package org.example;

public class User {

    private String name;
    private String role;

    public User(String name, String role) {
        this.name = name;
        this.role = role;
    }

    public String getUpperName() {
        return name.charAt(0) + name.substring(1);
    }

    public String getRole() {
        return role;
    }
}
