/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.bambu.tool.main;

import com.bambu.tool.net.IPUtil;
import com.bambu.tool.task.UpgradeTask;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 *
 * @author luotao
 */
public class UpgradeClientMain {

    /**
     * 入口
     *
     * @param args
     */
    public static void main(String[] args) {
        String os = "";
        String osname = System.getProperty("os.name");
        if (osname.toLowerCase().startsWith("win")) {
            os = "1";
        } else {
            os = "0";
        }
        /**
         * 扫描网卡获取IP地址(待完成）
         */
        List<String> hostList = new ArrayList();//升级主机，通过扫描获得。
        /**
         * 尝试连接
         */
        String confFile = System.getProperty("user.dir") + "/conf.properties";
        Properties prop = new Properties();
        try {
            /**
             * 加重配置文件 conf.properties
             */
            InputStream in = new BufferedInputStream(new FileInputStream(confFile));
            prop.load(new InputStreamReader(in, "UTF-8"));
            in.close();

            /**
             * 测试开关是否开启
             */
            Boolean isTest = Boolean.parseBoolean(prop.getProperty("upgrade.host.test.on"));
            int maxPoolNum = Integer.parseInt(prop.getProperty("upgrade.thread.pool.max"));
            List<String> localIpList = new ArrayList();//本机所有IP,可能有多网网卡.
            ExecutorService pool = Executors.newFixedThreadPool(maxPoolNum);
            if (isTest) {
                String hosts = prop.getProperty("upgrade.host.test");
                String[] ips = hosts.split(",");
                hostList.addAll(Arrays.asList(ips));
            } else {
                Enumeration nis = NetworkInterface.getNetworkInterfaces();
                while (nis.hasMoreElements()) {
                    NetworkInterface ni = (NetworkInterface) nis.nextElement();
                    List<InterfaceAddress> list = ni.getInterfaceAddresses();
                    for (InterfaceAddress localAddress : list) {
                        InetAddress ia = localAddress.getAddress();
                        localIpList.add(ia.getHostAddress());
                        if (ia instanceof Inet4Address && !ia.getHostAddress().equals("127.0.0.1")) {
                            String startIp = IPUtil.getBeginIpStr(ia.getHostAddress(), String.valueOf(localAddress.getNetworkPrefixLength()));
                            String endIp = IPUtil.getEndIpStr(ia.getHostAddress(), String.valueOf(localAddress.getNetworkPrefixLength()));
                            List<String> ipList = IPUtil.getPossibleIP(startIp, endIp);
                            hostList.addAll(ipList);
                        }
                    }
                }
                boolean localHostCommit = false;//本机升级是否已执行
                for (String ip : hostList) {
                    boolean isLocalHost = false;
                    if (localIpList.contains(ip)) {
                        isLocalHost = true;
                        if (!localHostCommit) {
                            Callable upgradeTask = new UpgradeTask(prop, InetAddress.getByName(ip), isLocalHost);
                            Future taskResult = pool.submit(upgradeTask);
                            localHostCommit = true;
                        }
                    } else {
                        Callable upgradeTask = new UpgradeTask(prop, InetAddress.getByName(ip), isLocalHost);
                        Future taskResult = pool.submit(upgradeTask);
                    }
                }
            }
            pool.shutdown();

        } catch (SocketTimeoutException timeoutex) {

        } catch (Exception e) {

        }
    }
}
