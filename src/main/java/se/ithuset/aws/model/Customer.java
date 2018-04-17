package se.ithuset.aws.model;

import com.amazonaws.services.dynamodbv2.document.Item;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class Customer {
    private String customerName;
    private String birthDate;
    private String accountId;
    private String startDate;
    private int balance;

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(String birthDate) {
        this.birthDate = birthDate;
    }

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public int getBalance() {
        return balance;
    }

    public void setBalance(int balance) {
        this.balance = balance;
    }

    public String asJson() {
        try {
            return new ObjectMapper().writeValueAsString(this);
        } catch (JsonProcessingException e) {
            return null;
        }
    }

    public Item toItem() {
        return new Item().withString("accountId", accountId)
                .withString("dateOfBirth", birthDate)
                .withString("startDate", startDate)
                .withNumber("balance", balance)
                .withString("owner", customerName);
    }

    public static Customer fromItem(Item item) {
        Customer customer = new Customer();
        customer.setCustomerName(item.getString("owner"));
        customer.setStartDate(item.getString("startDate"));
        customer.setBirthDate(item.getString("dateOfBirth"));
        customer.setBalance(item.getNumber("balance").intValue());
        customer.setAccountId(item.getString("accountId"));
        return customer;
    }

    @Override
    public String toString() {
        return "Customer{" +
                "customerName='" + customerName + '\'' +
                ", birthDate='" + birthDate + '\'' +
                ", accountId='" + accountId + '\'' +
                ", startDate='" + startDate + '\'' +
                ", balance=" + balance +
                '}';
    }
}
