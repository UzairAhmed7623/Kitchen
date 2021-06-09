package com.inkhornsolutions.kitchen.modelclasses;

public class ItemsModelClass {
    private String resName, itemName, price, image, availability, schedule, description;

    public ItemsModelClass() {
    }

    public ItemsModelClass(String resName, String itemName, String price, String image, String availability, String schedule, String description) {
        this.resName = resName;
        this.itemName = itemName;
        this.price = price;
        this.image = image;
        this.availability = availability;
        this.schedule = schedule;
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getResName() {
        return resName;
    }

    public void setResName(String resName) {
        this.resName = resName;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getAvailability() {
        return availability;
    }

    public void setAvailability(String availability) {
        this.availability = availability;
    }

    public String getSchedule() {
        return schedule;
    }

    public void setSchedule(String schedule) {
        this.schedule = schedule;
    }
}
