package com.example.homebudgetpollub;

public class Data {
    String item, date,id, itemNday, itemNweek, itemNmonth;
    private int amount, month, week;
    String notes;
    private boolean isCyclical = false;

    public Data() {
    }

    public Data(String item, String date, String id, String itemNday, String itemNweek, String itemNmonth, int amount, int month, int week, String notes) {
        this.item = item;
        this.date = date;
        this.id = id;
        this.itemNday = itemNday;
        this.itemNweek = itemNweek;
        this.itemNmonth = itemNmonth;
        this.amount = amount;
        this.month = month;
        this.week = week;
        this.notes = notes;
    }

    public boolean isCyclical() {
        return isCyclical;
    }

    public void setCyclical(boolean cyclical) {
        isCyclical = cyclical;
    }

    public String getItem() {
        return item;
    }

    public void setItem(String item) {
        this.item = item;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getItemNday() {
        return itemNday;
    }

    public void setItemNday(String itemNday) {
        this.itemNday = itemNday;
    }

    public String getItemNweek() {
        return itemNweek;
    }

    public void setItemNweek(String itemNweek) {
        this.itemNweek = itemNweek;
    }

    public String getItemNmonth() {
        return itemNmonth;
    }

    public void setItemNmonth(String itemNmonth) {
        this.itemNmonth = itemNmonth;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public int getMonth() {
        return month;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    public int getWeek() {
        return week;
    }

    public void setWeek(int week) {
        this.week = week;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
