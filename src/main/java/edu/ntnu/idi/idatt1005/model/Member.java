package edu.ntnu.idi.idatt1005.model;

import java.util.Objects;

public class Member {
    private int id;
    private String name;
    private int userId;

    // Default constructor
    public Member() {
    }

    // Constructor for creating new members
    public Member(String name, int userId) {
        this.name = name;
        this.userId = userId;
    }

    // Constructor for retrieving from database
    public Member(int id, String name, int userId) {
        this.id = id;
        this.name = name;
        this.userId = userId;
    }

    // Getters and setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }



    @Override
    public String toString() {
        return "Member{" +
               "id=" + id +
               ", name='" + name + '\'' +
               ", userId=" + userId +
               '}';
    }
}