package org.example;

public class User {

    private int UserID;
    private String name;
    private String userName;
    private String password;
    private String role;

    public User(int userID, String name, String userName, String password, String role) {
        UserID = userID;
        this.name = name;
        this.userName = userName;
        this.password = password;
        this.role = role;
    }

    public int getUserID() {
        return UserID;
    }

    public void setUserID(int userID) {
        UserID = userID;
    }

    public String getName() {
        return name;
    }

    public String getUpperName() {
        return name.charAt(0) + name.substring(1);
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
