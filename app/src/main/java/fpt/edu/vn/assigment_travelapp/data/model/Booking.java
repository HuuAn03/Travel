package fpt.edu.vn.assigment_travelapp.data.model;

import java.util.Date;

public class Booking {
    private String bookingId;
    private String destinationId;
    private String userId;
    private Date checkInDate;
    private Date checkOutDate;
    private int numberOfPeople;
    private String message;
    private double totalPrice;
    private String paymentMethodId;
    private String status; // Pending, Confirmed, Completed, Cancelled
    private long timestamp;

    public Booking() {
        // Default constructor for Firebase
    }

    public Booking(String bookingId, String destinationId, String userId,
                   Date checkInDate, Date checkOutDate, int numberOfPeople,
                   String message, double totalPrice, String paymentMethodId,
                   String status, long timestamp) {
        this.bookingId = bookingId;
        this.destinationId = destinationId;
        this.userId = userId;
        this.checkInDate = checkInDate;
        this.checkOutDate = checkOutDate;
        this.numberOfPeople = numberOfPeople;
        this.message = message;
        this.totalPrice = totalPrice;
        this.paymentMethodId = paymentMethodId;
        this.status = status;
        this.timestamp = timestamp;
    }

    // Getters and Setters
    public String getBookingId() {
        return bookingId;
    }

    public void setBookingId(String bookingId) {
        this.bookingId = bookingId;
    }

    public String getDestinationId() {
        return destinationId;
    }

    public void setDestinationId(String destinationId) {
        this.destinationId = destinationId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Date getCheckInDate() {
        return checkInDate;
    }

    public void setCheckInDate(Date checkInDate) {
        this.checkInDate = checkInDate;
    }

    public Date getCheckOutDate() {
        return checkOutDate;
    }

    public void setCheckOutDate(Date checkOutDate) {
        this.checkOutDate = checkOutDate;
    }

    public int getNumberOfPeople() {
        return numberOfPeople;
    }

    public void setNumberOfPeople(int numberOfPeople) {
        this.numberOfPeople = numberOfPeople;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public double getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(double totalPrice) {
        this.totalPrice = totalPrice;
    }

    public String getPaymentMethodId() {
        return paymentMethodId;
    }

    public void setPaymentMethodId(String paymentMethodId) {
        this.paymentMethodId = paymentMethodId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}

