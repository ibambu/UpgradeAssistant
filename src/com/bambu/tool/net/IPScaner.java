/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.bambu.tool.net;

import com.bambu.tool.IP;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Luo Tao
 */
public class IPScaner {

    private static final String WINDOWS = "1";
    private static final String LINUX = "2";

    /**
     * 获取 Windows IP 信息
     *
     * @return
     */
    public static List<IP> getLocalWindowsIP() {
        List<IP> ipList = new ArrayList();//返回对象
        BufferedReader readerBuffer = null;
        try {
            Process pro = Runtime.getRuntime().exec("ipconfig");
            readerBuffer = new BufferedReader(new InputStreamReader(pro.getInputStream(), "GBK"));
            String msg = null;
            IP ip = new IP();
            while ((msg = readerBuffer.readLine()) != null) {
                System.out.println(msg);
                if (msg.contains("IPv4 地址")) {
                    String[] ipInfo = msg.split(":");
                    String ipstr = ipInfo[1].trim();
                    ip = new IP();
                    ip.setIpAddress(ipstr);
                    ipList.add(ip);
                } else if (msg.contains("子网掩码")) {
                    String[] ipInfo = msg.split(":");
                    String subnetMask = ipInfo[1].trim();
                    ip.setSubnetMask(subnetMask);
                } else if (msg.contains("默认网关")) {
                    String[] ipInfo = msg.split(":");
                    String getway = ipInfo[1].trim();
                    ip.setGetway(getway);
                }
            }
        } catch (IOException ex) {
            System.out.println("[Info] read ip fail.");
        } finally {
            if (readerBuffer != null) {
                try {
                    readerBuffer.close();
                } catch (IOException ex) {

                }
            }
        }
        return ipList;
    }

    public static List<IP> getLocalLinuxIP() {
        List<IP> ipList = new ArrayList();//返回对象
        BufferedReader readerBuffer = null;
        try {
            Process pro = Runtime.getRuntime().exec("ping");
            readerBuffer = new BufferedReader(new InputStreamReader(pro.getInputStream(), "GBK"));
            String msg = null;
            IP ip = new IP();
            while ((msg = readerBuffer.readLine()) != null) {
                System.out.println(msg);
                if (msg.contains("IPv4 地址")) {
                    String[] ipInfo = msg.split(":");
                    String ipstr = ipInfo[1].trim();
                    ip = new IP();
                    ip.setIpAddress(ipstr);
                    ipList.add(ip);
                } else if (msg.contains("子网掩码")) {
                    String[] ipInfo = msg.split(":");
                    String subnetMask = ipInfo[1].trim();
                    ip.setSubnetMask(subnetMask);
                } else if (msg.contains("默认网关")) {
                    String[] ipInfo = msg.split(":");
                    String getway = ipInfo[1].trim();
                    ip.setGetway(getway);
                }
            }
        } catch (IOException ex) {
            System.out.println("[Info] read ip fail.");
        } finally {
            if (readerBuffer != null) {
                try {
                    readerBuffer.close();
                } catch (IOException ex) {

                }
            }
        }
        return ipList;
    }

    /**
     * ping IP地址
     *
     * @param ip
     * @param os
     * @return
     */
    public static boolean ping(String ip, String os) {
        boolean pingflag = false;
        StringBuilder respBuffer = new StringBuilder();
        BufferedReader readerBuffer = null;
        try {
            String pingcmd = "";
            if (WINDOWS.equals(os)) {
                pingcmd = "ping " + ip;
            } else if (LINUX.equals(os)) {
                pingcmd = "ping -c 3 " + ip;
            }
            if (pingcmd.trim().length() == 0) {
                return false;
            }
            Process pro = Runtime.getRuntime().exec(pingcmd);
            readerBuffer = new BufferedReader(new InputStreamReader(pro.getInputStream(), "GBK"));
            String response = null;
            while ((response = readerBuffer.readLine()) != null) {
                respBuffer.append(response);
            }
            if (WINDOWS.equals(os)) {
                if (respBuffer.toString().contains("TTL=") | respBuffer.toString().contains("ttl=")) {
                    pingflag = true;
                }
            } else if (LINUX.equals(os)) {
                if (respBuffer.toString().contains("TTL=") || respBuffer.toString().contains("ttl=")) {
                    pingflag = true;
                }
            }
            System.out.println("[Info] ping "+ip+" "+pingflag);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("[Info] ping " + ip + " error.");
        } finally {
            if (readerBuffer != null) {
                try {
                    readerBuffer.close();
                } catch (IOException ex) {
                }
            }
        }
        return pingflag;
    }

   
}
