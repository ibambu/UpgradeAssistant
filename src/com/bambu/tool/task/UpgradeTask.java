/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.bambu.tool.task;

import com.bambu.tool.RetMessage;
import com.bambu.tool.file.FileTransfer;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.Properties;
import java.util.concurrent.Callable;

/**
 *
 * @author Luo Tao
 */
public class UpgradeTask implements Callable {

    private Properties properties;//配置信息
    private InetAddress inetAddress;//主机地址

    private Socket socket;//套接字
    private DataInputStream dataReader;//数据读
    private DataOutputStream dataWriter;//数据写
    private boolean isLocalHost;

    public UpgradeTask(Properties properties, InetAddress inetAddress, boolean isLocalHost) {
        this.properties = properties;
        this.inetAddress = inetAddress;
        this.isLocalHost = isLocalHost;
    }

    @Override
    public Object call() throws Exception {
        /**
         * 探测主机是否可达,3秒后无响应当作不可达。
         */
        RetMessage retMessage = new RetMessage();
        boolean isValidHost = false;
        try {
            boolean isReachable = inetAddress.isReachable(2000);
            if (isReachable) {
                int port = Integer.parseInt(properties.getProperty("upgrade.host.connect.port", "5001"));
                int timeout = Integer.parseInt(properties.getProperty("upgrade.connect.timeout", "40000"));
                socket = new Socket(inetAddress.getHostAddress(), port);
                socket.setSoTimeout(timeout);//设置连接超时时间
                String host = socket.getInetAddress().getHostAddress();
                dataReader = new DataInputStream(socket.getInputStream());
                dataWriter = new DataOutputStream(socket.getOutputStream());
                String validHostMsg = properties.getProperty("upgrade.host.correct.response");
                isValidHost = isValidHost(validHostMsg);
                if (isValidHost) {
                    FileTransfer transfer = new FileTransfer(socket, properties, dataReader, dataWriter);
                    retMessage = transfer.sendFile();//发送文件
                    switch (retMessage.getCode()) {
                        case -1:
                            //文件缺少。
                            retMessage.addLog("upgrade file not found...");
                            break;
                        case 0:
                            //文件发送成功,配置成功.
                            retMessage.addLog("[Fail] " + host + " not upgrade but config config successful.");
                            break;
                        case 1:
                            //文件发送成功，升级成功,注意本机升级不自动重启.
                            if (!isLocalHost) {
                                byte[] bystes = "at!r".getBytes();
                                char enter = 0x0d;
                                byte[] enterbytes = charToByte(enter);
                                byte[] alldata = new byte[bystes.length + enterbytes.length];
                                System.arraycopy(bystes, 0, alldata, 0, bystes.length);
                                System.arraycopy(enterbytes, 0, alldata, bystes.length, enterbytes.length);
                                dataWriter.write(alldata);
                                dataWriter.flush();
                            }
                            retMessage.addLog("[OK] " + host + " upgrade successful.");
                            break;
                        case 2:
                            //文件发送成功,升级失败
                            retMessage.addLog("[Fail] " + host + " upgrade fail.");
                    }
                }
            }
        } catch (Exception e) {
            retMessage.addLog("upgrade exception :" + e.getMessage());
        } finally {
            if (isValidHost) {
                writeToLogFile(retMessage.getLogBuffer().toString(), "upgrade_" + inetAddress.getHostAddress().replaceAll("\\.", "_") + ".log");
                writeToLogFile(retMessage.getLogBuffer().toString(), "upgrade_" + inetAddress.getHostAddress().replaceAll("\\.", "_") + ".log");
            }
            Thread.sleep(2000);
            if (socket != null) {
                socket.close();
            }
            if (dataReader != null) {
                dataReader.close();
            }
            if (dataWriter != null) {
                dataWriter.close();
            }
        }
        return retMessage;
    }

    private byte[] charToByte(char c) {
        byte[] b = new byte[2];
        b[0] = (byte) ((c & 0xFF00) >> 8);
        b[1] = (byte) (c & 0xFF);
        return b;
    }

    /**
     * 是否合法主机
     *
     * @param validHostMsg
     * @return
     */
    private boolean isValidHost(String validHostMsg) {
        boolean isValid = false;
        byte[] b = new byte[1024 * 10];
        int length = 0;
        try {
            while ((length = dataReader.read(b)) != -1) {
                String rsp = new String(b, 0, length);
                if (rsp.contains(validHostMsg)) {
                    isValid = true;
                    break;
                }
            }
        } catch (SocketTimeoutException ex) {
        } catch (Exception e) {
        }
        return isValid;
    }

    /**
     * 写日志文件
     *
     * @param content
     * @throws IOException
     */
    private void writeToLogFile(String content, String filename) throws IOException {
        BufferedWriter bufwriter = null;
        String file = System.getProperty("user.dir") + "/logs";
        System.out.println(content);
        File fileObj = new File(file);
        if (!fileObj.exists()) {
            fileObj.mkdir();
        }
        try {
            OutputStreamWriter writerStream = new OutputStreamWriter(new FileOutputStream(file + "/" + filename, true), "UTF-8");
            bufwriter = new BufferedWriter(writerStream);
            bufwriter.write(content);
            bufwriter.newLine();
            bufwriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (bufwriter != null) {
                bufwriter.close();
            }
        }
    }
}
