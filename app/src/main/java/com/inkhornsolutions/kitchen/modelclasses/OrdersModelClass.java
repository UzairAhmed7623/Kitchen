package com.inkhornsolutions.kitchen.modelclasses;

public class OrdersModelClass {
    private String resId, id, pId, itemName, finalPrice, price,  items_Count, status, resName, totalPrice, date, orderId, userId, subTotal, promotedOrder;
    private Double lat, lng;
    private boolean expanded;

    public boolean isExpanded() {
        return expanded;
    }

    public void setExpanded(boolean expanded) {
        this.expanded = expanded;
    }

    public OrdersModelClass() {
    }

    public OrdersModelClass(String resId, String id, String pId, String itemName, String finalPrice, String price, String items_Count, String status, String resName, String totalPrice, String date, String orderId, String userId, String subTotal, String promotedOrder, Double lat, Double lng, boolean expanded) {
        this.resId = resId;
        this.id = id;
        this.pId = pId;
        this.itemName = itemName;
        this.finalPrice = finalPrice;
        this.price = price;
        this.items_Count = items_Count;
        this.status = status;
        this.resName = resName;
        this.totalPrice = totalPrice;
        this.date = date;
        this.orderId = orderId;
        this.userId = userId;
        this.subTotal = subTotal;
        this.promotedOrder = promotedOrder;
        this.lat = lat;
        this.lng = lng;
        this.expanded = expanded;
    }

    public String getPromotedOrder() {
        return promotedOrder;
    }

    public void setPromotedOrder(String promotedOrder) {
        this.promotedOrder = promotedOrder;
    }

    public String getSubTotal() {
        return subTotal;
    }

    public void setSubTotal(String subTotal) {
        this.subTotal = subTotal;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Double getLat() {
        return lat;
    }

    public void setLat(Double lat) {
        this.lat = lat;
    }

    public Double getLng() {
        return lng;
    }

    public void setLng(Double lng) {
        this.lng = lng;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getpId() {
        return pId;
    }

    public void setpId(String pId) {
        this.pId = pId;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public String getFinalPrice() {
        return finalPrice;
    }

    public void setFinalPrice(String finalPrice) {
        this.finalPrice = finalPrice;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getItems_Count() {
        return items_Count;
    }

    public void setItems_Count(String items_Count) {
        this.items_Count = items_Count;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getResName() {
        return resName;
    }

    public void setResName(String resName) {
        this.resName = resName;
    }

    public String getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(String totalPrice) {
        this.totalPrice = totalPrice;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getResId() {
        return resId;
    }

    public void setResId(String resId) {
        this.resId = resId;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }
}
