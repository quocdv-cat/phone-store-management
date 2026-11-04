package com.phonestore.model;

import java.time.LocalDateTime;

public class Employee {

    private int employeeId;
    private String employeeCode;
    private String fullName;
    private String phone;
    private String email;
    private String address;
    private String position;
    private String status;
    private LocalDateTime createdAt;

    public Employee() {
    }

    public Employee(int employeeId, String employeeCode, String fullName, String phone, String email, String address, String position, String status) {
        this.employeeId = employeeId;
        this.employeeCode = employeeCode;
        this.fullName = fullName;
        this.phone = phone;
        this.email = email;
        this.address = address;
        this.position = position;
        this.status = status;
    }

    public int getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(int employeeId) {
        this.employeeId = employeeId;
    }

    public String getEmployeeCode() {
        return employeeCode;
    }

    public void setEmployeeCode(String employeeCode) {
        this.employeeCode = employeeCode;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return fullName + " (" + employeeCode + ")";
    }
}
