package com.dztech.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import org.springframework.web.multipart.MultipartFile;

public class DriverProfileUpdateForm {

    @Size(max = 150, message = "Full name must be at most 150 characters")
    private String fullName;

    private String dob;

    @Size(max = 20, message = "Gender must be at most 20 characters")
    private String gender;

    @Email(message = "Email must be a valid email address")
    @Size(max = 150, message = "Email must be at most 150 characters")
    private String email;

    @Size(max = 25, message = "Phone must be at most 25 characters")
    private String phone;

    @Size(max = 150, message = "Emergency contact name must be at most 150 characters")
    private String emergencyContactName;

    @Size(max = 25, message = "Emergency contact number must be at most 25 characters")
    private String emergencyContactNumber;

    @Size(max = 255, message = "Permanent address must be at most 255 characters")
    private String permanentAddress;

    @Size(max = 255, message = "Languages must be at most 255 characters")
    private String languages;

    @Size(max = 50, message = "License number must be at most 50 characters")
    private String licenseNumber;

    @Size(max = 50, message = "License type must be at most 50 characters")
    private String licenseType;

    private String experience;

    @Size(max = 50, message = "Government ID type must be at most 50 characters")
    private String govIdType;

    @Size(max = 100, message = "Government ID number must be at most 100 characters")
    private String govIdNumber;

    private MultipartFile profilePhoto;
    private MultipartFile licenseFront;
    private MultipartFile licenseBack;
    private MultipartFile govIdFront;
    private MultipartFile govIdBack;

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getDob() {
        return dob;
    }

    public void setDob(String dob) {
        this.dob = dob;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmergencyContactName() {
        return emergencyContactName;
    }

    public void setEmergencyContactName(String emergencyContactName) {
        this.emergencyContactName = emergencyContactName;
    }

    public String getEmergencyContactNumber() {
        return emergencyContactNumber;
    }

    public void setEmergencyContactNumber(String emergencyContactNumber) {
        this.emergencyContactNumber = emergencyContactNumber;
    }

    public String getPermanentAddress() {
        return permanentAddress;
    }

    public void setPermanentAddress(String permanentAddress) {
        this.permanentAddress = permanentAddress;
    }

    public String getLanguages() {
        return languages;
    }

    public void setLanguages(String languages) {
        this.languages = languages;
    }

    public String getLicenseNumber() {
        return licenseNumber;
    }

    public void setLicenseNumber(String licenseNumber) {
        this.licenseNumber = licenseNumber;
    }

    public String getLicenseType() {
        return licenseType;
    }

    public void setLicenseType(String licenseType) {
        this.licenseType = licenseType;
    }

    public String getExperience() {
        return experience;
    }

    public void setExperience(String experience) {
        this.experience = experience;
    }

    public String getGovIdType() {
        return govIdType;
    }

    public void setGovIdType(String govIdType) {
        this.govIdType = govIdType;
    }

    public String getGovIdNumber() {
        return govIdNumber;
    }

    public void setGovIdNumber(String govIdNumber) {
        this.govIdNumber = govIdNumber;
    }

    public MultipartFile getProfilePhoto() {
        return profilePhoto;
    }

    public void setProfilePhoto(MultipartFile profilePhoto) {
        this.profilePhoto = profilePhoto;
    }

    public MultipartFile getLicenseFront() {
        return licenseFront;
    }

    public void setLicenseFront(MultipartFile licenseFront) {
        this.licenseFront = licenseFront;
    }

    public MultipartFile getLicenseBack() {
        return licenseBack;
    }

    public void setLicenseBack(MultipartFile licenseBack) {
        this.licenseBack = licenseBack;
    }

    public MultipartFile getGovIdFront() {
        return govIdFront;
    }

    public void setGovIdFront(MultipartFile govIdFront) {
        this.govIdFront = govIdFront;
    }

    public MultipartFile getGovIdBack() {
        return govIdBack;
    }

    public void setGovIdBack(MultipartFile govIdBack) {
        this.govIdBack = govIdBack;
    }
}
