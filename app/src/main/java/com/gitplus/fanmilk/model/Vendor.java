package com.gitplus.fanmilk.model;

public class Vendor {

    private String vendor_code;
    private String vendor_name;
    private String phone_main;
    private String image;
    private String amount;

    public Vendor(String vendor_code, String agent_name, String phone_main, String image, String amount) {
        this.vendor_code = vendor_code;
        this.vendor_name = agent_name;
        this.phone_main = phone_main;
        this.image = image;
        this.amount = amount;
    }

    public String getVendor_code() {
        return vendor_code;
    }

    public String getVendor_name() {
        return vendor_name;
    }

    public String getPhone_main() {
        return phone_main;
    }

    public String getImage() {
        return image;
    }

    public String getAmount() {
        return amount;
    }

}
