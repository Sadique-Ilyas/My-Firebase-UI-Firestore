package com.example.myfirebaseuifirestore;

public class Note
{
    public String title, description;
    public int priority;

    public Note(){}

    public Note(String title, String description, int priority) {
        this.title = title;
        this.description = description;
        this.priority = priority;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public int getPriority() {
        return priority;
    }
}
