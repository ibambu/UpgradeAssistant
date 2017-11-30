/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.bambu.tool.test;

import com.bambu.tool.net.IPUtil;
import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.List;

/**
 *
 * @author Luo Tao
 */
public class NewMain {

    /**
     * @param args the command line arguments
     */
    public static byte[] charToByte(char c) {
        byte[] b = new byte[2];
        b[0] = (byte) ((c & 0xFF00) >> 8);
        b[1] = (byte) (c & 0xFF);
        return b;
    }

    public static void main(String[] args) throws IOException {
        try {
            char a = 0x0d;
            System.out.println(charToByte(a).length);
            Enumeration nis = NetworkInterface.getNetworkInterfaces();
            while (nis.hasMoreElements()) {
                NetworkInterface ni = (NetworkInterface) nis.nextElement();
                Enumeration ias = ni.getInetAddresses();
                List<InterfaceAddress> list = ni.getInterfaceAddresses();
                for (InterfaceAddress bb : list) {
                    InetAddress ia = bb.getAddress();
                    if (ia instanceof Inet4Address && !ia.getHostAddress().equals("127.0.0.1")) {
                        String mask = IPUtil.getMaskByMaskBit(String.valueOf(bb.getNetworkPrefixLength()));
                        System.out.println(bb.getAddress().getHostAddress() + " mask.length:" + bb.getNetworkPrefixLength() + " mask:" + mask);
                        System.out.println("开始IP:" + IPUtil.getBeginIpStr(ia.getHostAddress(), String.valueOf(bb.getNetworkPrefixLength())));
                        System.out.println("结束IP:" + IPUtil.getEndIpStr(ia.getHostAddress(), String.valueOf(bb.getNetworkPrefixLength())));
                    }
                }
//                InterfaceAddress aa = null;
//                while (ias.hasMoreElements()) {
//                    InetAddress ia = (InetAddress) ias.nextElement();
//                    System.out.println(ia.getHostAddress());
//                    System.out.println(ia.isReachable(1500));
//                }
            }
        } catch (SocketException ex) {

        }
    }
}
