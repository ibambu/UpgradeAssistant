/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.bambu.tool.net;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Luo Tao
 */
public class IPUtil {

    

    /**
     * 把long类型的Ip转为一般Ip类型：xx.xx.xx.xx
     *
     * @param ip
     * @return
     */
    public static String getIpFromLong(Long ip) {
        String s1 = String.valueOf((ip & 4278190080L) / 16777216L);
        String s2 = String.valueOf((ip & 16711680L) / 65536L);
        String s3 = String.valueOf((ip & 65280L) / 256L);
        String s4 = String.valueOf(ip & 255L);
        return s1 + "." + s2 + "." + s3 + "." + s4;
    }

    /**
     * 把xx.xx.xx.xx类型的转为long类型的
     *
     * @param ip
     * @return
     */
    public static Long getIpFromString(String ip) {
        Long ipLong = 0L;
        String ipTemp = ip;
        ipLong = ipLong * 256
                + Long.parseLong(ipTemp.substring(0, ipTemp.indexOf(".")));
        ipTemp = ipTemp.substring(ipTemp.indexOf(".") + 1, ipTemp.length());
        ipLong = ipLong * 256
                + Long.parseLong(ipTemp.substring(0, ipTemp.indexOf(".")));
        ipTemp = ipTemp.substring(ipTemp.indexOf(".") + 1, ipTemp.length());
        ipLong = ipLong * 256
                + Long.parseLong(ipTemp.substring(0, ipTemp.indexOf(".")));
        ipTemp = ipTemp.substring(ipTemp.indexOf(".") + 1, ipTemp.length());
        ipLong = ipLong * 256 + Long.parseLong(ipTemp);
        return ipLong;
    }

    /**
     * 根据掩码位获取掩码
     *
     * @param maskBit 掩码位数，如"28"、"30"
     * @return
     */
    public static String getMaskByMaskBit(String maskBit) {
        return maskBit == null ? "error, maskBit is null !"
                : maskBitMap().get(maskBit);
    }

    /**
     * 根据 ip/掩码位 计算IP段的起始IP 如 IP串 218.240.38.69/30
     *
     * @param ip 给定的IP，如218.240.38.69
     * @param maskBit 给定的掩码位，如30
     * @return 起始IP的字符串表示
     */
    public static String getBeginIpStr(String ip, String maskBit) {
        return getIpFromLong(getBeginIpLong(ip, maskBit));
    }

    /**
     * 根据 ip/掩码位 计算IP段的起始IP 如 IP串 218.240.38.69/30
     *
     * @param ip 给定的IP，如218.240.38.69
     * @param maskBit 给定的掩码位，如30
     * @return 起始IP的长整型表示
     */
    public static Long getBeginIpLong(String ip, String maskBit) {
        return getIpFromString(ip) & getIpFromString(getMaskByMaskBit(maskBit));
    }

    /**
     * 根据 ip/掩码位 计算IP段的终止IP 如 IP串 218.240.38.69/30
     *
     * @param ip 给定的IP，如218.240.38.69
     * @param maskBit 给定的掩码位，如30
     * @return 终止IP的字符串表示
     */
    public static String getEndIpStr(String ip, String maskBit) {
        return getIpFromLong(getEndIpLong(ip, maskBit));
    }

    /**
     * 根据 ip/掩码位 计算IP段的终止IP 如 IP串 218.240.38.69/30
     *
     * @param ip 给定的IP，如218.240.38.69
     * @param maskBit 给定的掩码位，如30
     * @return 终止IP的长整型表示
     */
    public static Long getEndIpLong(String ip, String maskBit) {
        return getBeginIpLong(ip, maskBit)
                + ~getIpFromString(getMaskByMaskBit(maskBit));
    }

    /**
     * 计算子网大小
     *
     * @param maskBit 掩码位
     * @return
     */
    public static int getPoolMax(int maskBit) {
        if (maskBit <= 0 || maskBit >= 32) {
            return 0;
        }
        return (int) Math.pow(2, 32 - maskBit) - 2;
    }

    private static StringBuffer toBin(int x) {
        StringBuffer result = new StringBuffer();
        result.append(x % 2);
        x /= 2;
        while (x > 0) {
            result.append(x % 2);
            x /= 2;
        }
        return result;
    }

    /*
     * 存储着所有的掩码位及对应的掩码 key:掩码位 value:掩码（x.x.x.x）
     */
    private static Map<String, String> maskBitMap() {
        Map<String, String> maskBit = new HashMap<>();
        maskBit.put("1", "128.0.0.0");
        maskBit.put("2", "192.0.0.0");
        maskBit.put("3", "224.0.0.0");
        maskBit.put("4", "240.0.0.0");
        maskBit.put("5", "248.0.0.0");
        maskBit.put("6", "252.0.0.0");
        maskBit.put("7", "254.0.0.0");
        maskBit.put("8", "255.0.0.0");
        maskBit.put("9", "255.128.0.0");
        maskBit.put("10", "255.192.0.0");
        maskBit.put("11", "255.224.0.0");
        maskBit.put("12", "255.240.0.0");
        maskBit.put("13", "255.248.0.0");
        maskBit.put("14", "255.252.0.0");
        maskBit.put("15", "255.254.0.0");
        maskBit.put("16", "255.255.0.0");
        maskBit.put("17", "255.255.128.0");
        maskBit.put("18", "255.255.192.0");
        maskBit.put("19", "255.255.224.0");
        maskBit.put("20", "255.255.240.0");
        maskBit.put("21", "255.255.248.0");
        maskBit.put("22", "255.255.252.0");
        maskBit.put("23", "255.255.254.0");
        maskBit.put("24", "255.255.255.0");
        maskBit.put("25", "255.255.255.128");
        maskBit.put("26", "255.255.255.192");
        maskBit.put("27", "255.255.255.224");
        maskBit.put("28", "255.255.255.240");
        maskBit.put("29", "255.255.255.248");
        maskBit.put("30", "255.255.255.252");
        maskBit.put("31", "255.255.255.254");
        maskBit.put("32", "255.255.255.255");
        return maskBit;
    }

    /**
     * 根据子网掩码转换为掩码位 如 255.255.255.252转换为掩码位 为 30
     *
     * @param netmarks
     * @return
     */
    public static int getNetMask(String netmarks) {
        StringBuffer sbf;
        String str;
        int inetmask = 0, count = 0;
        String[] ipList = netmarks.split("\\.");
        for (int n = 0; n < ipList.length; n++) {
            sbf = toBin(Integer.parseInt(ipList[n]));
            str = sbf.reverse().toString();
            count = 0;
            for (int i = 0; i < str.length(); i++) {
                i = str.indexOf('1', i);
                if (i == -1) {
                    break;
                }
                count++;
            }
            inetmask += count;
        }
        return inetmask;
    }

    /**
     * 获取可能存在的IP
     *
     * @param startIp
     * @param endIp
     * @return
     */
    public static List<String> getPossibleIP(String startIp, String endIp) {
        List<String> ipList = new ArrayList<>();
        try {
            String[] startIps = startIp.split("\\.");
            String[] endIps = endIp.split("\\.");
            List<Integer> ipseg1 = new ArrayList<>();
            List<Integer> ipseg2 = new ArrayList<>();
            List<Integer> ipseg3 = new ArrayList<>();
            List<Integer> ipseg4 = new ArrayList<>();
            if (!startIps[0].equals(endIps[0])) {
                Integer ips = Integer.parseInt(startIps[0]);
                Integer ipe = Integer.parseInt(endIps[0]);
                for (int i = ips; i <= ipe; i++) {
                    ipseg1.add(i);
                }
            } else {
                ipseg1.add(Integer.parseInt(startIps[0]));
            }
            if (!startIps[1].equals(endIps[1])) {
                Integer ips = Integer.parseInt(startIps[1]);
                Integer ipe = Integer.parseInt(endIps[1]);
                for (int i = ips; i <= ipe; i++) {
                    ipseg2.add(i);
                }
            } else {
                ipseg2.add(Integer.parseInt(startIps[1]));
            }
            if (!startIps[2].equals(endIps[2])) {
                Integer ipe = Integer.parseInt(endIps[2]);
                for (int i = 1; i <= ipe; i++) {
                    ipseg3.add(i);
                }
            } else {
                ipseg3.add(Integer.parseInt(startIps[2]));
            }
            if (!startIps[3].equals(endIps[3])) {
                Integer ipe = Integer.parseInt(endIps[3]);
                for (int i = 1; i < ipe; i++) {
                    ipseg4.add(i);
                }
            } else {
                ipseg4.add(Integer.parseInt(startIps[3]));
            }
            for (Integer ip1 : ipseg1) {
                for (Integer ip2 : ipseg2) {
                    for (Integer ip3 : ipseg3) {
                        for (Integer ip4 : ipseg4) {
                            String aIP = String.valueOf(ip1.intValue()) + "." + String.valueOf(ip2.intValue())
                                    + "." + String.valueOf(ip3.intValue()) + "." + String.valueOf(ip4.intValue());
                            ipList.add(aIP);
                        }
                    }
                }
            }

        } catch (Exception e) {
            System.out.println("[Info] get all ip list error.");
        }
        return ipList;
    }

    public static void main(String[] args) {
        int a = getNetMask("255.255.252.0");
        String ip = "192.168.23.120";
        System.out.println(a);
        String startIp = getBeginIpStr(ip, String.valueOf(a));
        System.out.println(startIp);
        String endIp = getEndIpStr(ip, String.valueOf(a));
        System.out.println(endIp);
        String[] startIps = startIp.split("\\.");
        String[] endIps = endIp.split("\\.");
        List<Integer> ipseg1 = new ArrayList<>();
        List<Integer> ipseg2 = new ArrayList<>();
        List<Integer> ipseg3 = new ArrayList<>();
        List<Integer> ipseg4 = new ArrayList<>();
        if (!startIps[0].equals(endIps[0])) {
            Integer ips = Integer.parseInt(startIps[0]);
            Integer ipe = Integer.parseInt(endIps[0]);
            for (int i = ips; i <= ipe; i++) {
                ipseg1.add(i);
            }
        } else {
            ipseg1.add(Integer.parseInt(startIps[0]));
        }
        if (!startIps[1].equals(endIps[1])) {
            Integer ips = Integer.parseInt(startIps[1]);
            Integer ipe = Integer.parseInt(endIps[1]);
            for (int i = ips; i <= ipe; i++) {
                ipseg2.add(i);
            }
        } else {
            ipseg2.add(Integer.parseInt(startIps[1]));
        }
        if (!startIps[2].equals(endIps[2])) {
            Integer ipe = Integer.parseInt(endIps[2]);
            for (int i = 1; i <= ipe; i++) {
                ipseg3.add(i);
            }
        } else {
            ipseg3.add(Integer.parseInt(startIps[2]));
        }
        if (!startIps[3].equals(endIps[3])) {
            Integer ipe = Integer.parseInt(endIps[3]);
            for (int i = 1; i < ipe; i++) {
                ipseg4.add(i);
            }
        } else {
            ipseg4.add(Integer.parseInt(startIps[3]));
        }
        for (Integer ip1 : ipseg1) {
            for (Integer ip2 : ipseg2) {
                for (Integer ip3 : ipseg3) {
                    for (Integer ip4 : ipseg4) {
                        String ipa = String.valueOf(ip1.intValue()) + "." + String.valueOf(ip2.intValue())
                                + "." + String.valueOf(ip3.intValue()) + "." + String.valueOf(ip4.intValue());
                        System.out.println(ipa);
                    }
                }
            }
        }
    }
}
