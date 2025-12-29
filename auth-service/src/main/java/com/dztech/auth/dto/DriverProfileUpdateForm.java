package com.dztech.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

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

    @Size(max = 255, message = "Current address must be at most 255 characters")
    private String currentAddress;

    @Size(max = 50, message = "Mother tongue must be at most 50 characters")
    private String motherTongue;

    @Size(max = 50, message = "Relationship must be at most 50 characters")
    private String relationship;

    private Boolean hillStation;

    private List<String> languages;

    @Size(max = 50, message = "License number must be at most 50 characters")
    private String licenseNumber;

    private List<String> licenseType;

    private List<Long> preferredBrands;

    @Size(max = 50, message = "Batch must be at most 50 characters")
    private String batch;

    private String expiryDate;

    private List<String> transmission;

    private String experience;

    @Size(max = 50, message = "Government ID type must be at most 50 characters")
    private String govIdType;

    @Size(max = 100, message = "Government ID number must be at most 100 characters")
    private String govIdNumber;

    private String expiryDateKyc;

    @Size(max = 10, message = "Blood group must be at most 10 characters")
    private String bloodGroup;

    @Size(max = 50, message = "Qualification must be at most 50 characters")
    private String qualification;

    @Size(max = 50, message = "Batch number must be at most 50 characters")
    private String batchNumber;

    private String batchExpiryDate;

    @Size(max = 150, message = "Father name must be at most 150 characters")
    private String fatherName;

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

    public String getCurrentAddress() {
        return currentAddress;
    }

    public void setCurrentAddress(String currentAddress) {
        this.currentAddress = currentAddress;
    }

    public String getMotherTongue() {
        return motherTongue;
    }

    public void setMotherTongue(String motherTongue) {
        this.motherTongue = motherTongue;
    }

    public String getRelationship() {
        return relationship;
    }

    public void setRelationship(String relationship) {
        this.relationship = relationship;
    }

    public Boolean getHillStation() {
        return hillStation;
    }

    public void setHillStation(Boolean hillStation) {
        this.hillStation = hillStation;
    }

    public List<String> getLanguages() {
        return languages;
    }

    public void setLanguages(List<String> languages) {
        this.languages = languages;
    }

    public String getLicenseNumber() {
        return licenseNumber;
    }

    public void setLicenseNumber(String licenseNumber) {
        this.licenseNumber = licenseNumber;
    }

    public List<String> getLicenseType() {
        return licenseType;
    }

    public void setLicenseType(List<String> licenseType) {
        this.licenseType = licenseType;
    }

    public List<Long> getPreferredBrands() {
        return preferredBrands;
    }

    public void setPreferredBrands(List<Long> preferredBrands) {
        this.preferredBrands = preferredBrands;
    }

    public String getBatch() {
        return batch;
    }

    public void setBatch(String batch) {
        this.batch = batch;
    }

    public String getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(String expiryDate) {
        this.expiryDate = expiryDate;
    }

    public List<String> getTransmission() {
        return transmission;
    }

    public void setTransmission(List<String> transmission) {
        this.transmission = transmission;
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

    public String getExpiryDateKyc() {
        return expiryDateKyc;
    }

    public void setExpiryDateKyc(String expiryDateKyc) {
        this.expiryDateKyc = expiryDateKyc;
    }

    public String getBloodGroup() {
        return bloodGroup;
    }

    public void setBloodGroup(String bloodGroup) {
        this.bloodGroup = bloodGroup;
    }

    public String getQualification() {
        return qualification;
    }

    public void setQualification(String qualification) {
        this.qualification = qualification;
    }

    public String getBatchNumber() {
        return batchNumber;
    }

    public void setBatchNumber(String batchNumber) {
        this.batchNumber = batchNumber;
    }

    public String getBatchExpiryDate() {
        return batchExpiryDate;
    }

    public void setBatchExpiryDate(String batchExpiryDate) {
        this.batchExpiryDate = batchExpiryDate;
    }

    public String getFatherName() {
        return fatherName;
    }

    public void setFatherName(String fatherName) {
        this.fatherName = fatherName;
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
