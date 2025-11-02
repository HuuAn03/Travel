package fpt.edu.vn.assigment_travelapp.data.model;

public class PaymentMethod {
    private String paymentMethodId;
    private String userId;
    private String type; // Mastercard, PayPal, Visa, etc.
    private String cardNumber; // Masked or full
    private String cardHolderName;
    private String email; // For PayPal
    private String expiryDate;
    private String cvv; // Should be stored securely in production
    private boolean isDefault;

    public PaymentMethod() {
        // Default constructor for Firebase
    }

    public PaymentMethod(String paymentMethodId, String userId, String type,
                        String cardNumber, String cardHolderName, String email,
                        String expiryDate, String cvv, boolean isDefault) {
        this.paymentMethodId = paymentMethodId;
        this.userId = userId;
        this.type = type;
        this.cardNumber = cardNumber;
        this.cardHolderName = cardHolderName;
        this.email = email;
        this.expiryDate = expiryDate;
        this.cvv = cvv;
        this.isDefault = isDefault;
    }

    // Getters and Setters
    public String getPaymentMethodId() {
        return paymentMethodId;
    }

    public void setPaymentMethodId(String paymentMethodId) {
        this.paymentMethodId = paymentMethodId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getCardNumber() {
        return cardNumber;
    }

    public void setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
    }

    public String getCardHolderName() {
        return cardHolderName;
    }

    public void setCardHolderName(String cardHolderName) {
        this.cardHolderName = cardHolderName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(String expiryDate) {
        this.expiryDate = expiryDate;
    }

    public String getCvv() {
        return cvv;
    }

    public void setCvv(String cvv) {
        this.cvv = cvv;
    }

    public boolean isDefault() {
        return isDefault;
    }

    public void setDefault(boolean aDefault) {
        isDefault = aDefault;
    }

    // Helper method to mask card number
    public String getMaskedCardNumber() {
        if (cardNumber == null || cardNumber.length() < 4) {
            return "****";
        }
        return "**** **** " + cardNumber.substring(cardNumber.length() - 4);
    }
}

