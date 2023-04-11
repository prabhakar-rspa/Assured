package com.rspcaassured.ui.Checklist;

public class Dropdown {
    private String name;
    private boolean isAnswered;


    public Dropdown() {
    }

    public Dropdown(String name, boolean isAnswered) {
        this.name = name;
        this.isAnswered = isAnswered;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isAnswered() {
        return isAnswered;
    }

    public void setAnswered(boolean answered) {
        isAnswered = answered;
    }

    @Override
    public String toString() {
        return "Dropdown{" +
                "name='" + name + '\'' +
                ", isAnswered=" + isAnswered +
                '}';
    }
}

