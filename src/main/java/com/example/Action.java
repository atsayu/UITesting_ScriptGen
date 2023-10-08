package com.example;

public class Action implements Comparable<Action>
{
    private String type;
    private String locator;
    private String text;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getLocator() {
        return locator;
    }

    public void setLocator(String locator) {
        this.locator = locator;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Action(String type, String locator, String text) {
        this.type = type;
        this.locator = locator;
        this.text = text;
    }

    @Override
    public int compareTo(Action o) {
        return this.getLocator().compareTo(o.getLocator());
    }

    @Override
    public String toString() {
        return this.type + " " + this.text + " to " + this.locator;
    }
}
