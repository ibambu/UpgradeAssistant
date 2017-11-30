/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.bambu.tool;

/**
 *
 * @author Luo Tao
 */
public class IP {

    private String ipAddress;//ipv4地址
    private String subnetMask;//子网掩码
    private String getway;//网关

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getSubnetMask() {
        return subnetMask;
    }

    public void setSubnetMask(String subnetMask) {
        this.subnetMask = subnetMask;
    }

    public String getGetway() {
        return getway;
    }

    public void setGetway(String getway) {
        this.getway = getway;
    }

    @Override
    public String toString() {
        return "IP{" + "ipAddress=" + ipAddress + ", subnetMask=" + subnetMask + ", getway=" + getway + '}';
    }

    
}
