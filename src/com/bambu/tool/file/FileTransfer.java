/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.bambu.tool.file;

import com.bambu.tool.RetMessage;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Properties;

/**
 *
 * @author Luo Tao
 */
public class FileTransfer {

    private Socket clientSocket;
    private DataInputStream inReader;
    private DataOutputStream outWriter;
    private Properties properties;

    public FileTransfer(Socket socket, Properties properties, DataInputStream inReader, DataOutputStream outWriter) {
        this.clientSocket = socket;
        this.properties = properties;
        this.inReader = inReader;
        this.outWriter = outWriter;
    }

    /**
     * 向目标主机发送文件
     *
     * @return
     */
    public RetMessage sendFile() {
        String host = clientSocket.getInetAddress().getHostAddress();
        RetMessage retMessage = new RetMessage();
        FileInputStream fileInputStream = null;
        StringBuilder upgradeRspBuffer = new StringBuilder();
        try {
            File file = new File(properties.getProperty("upgrade.data.file"));
            if (!file.exists()) {
                retMessage.setCode(-1);//文件缺失
            } else {
                fileInputStream = new FileInputStream(file);
                byte[] bytes = new byte[1024 * 10];
                int length = 0;
                long progress = 0;
                /**
                 * 开始发送文件
                 */
                StringBuilder progressBuffer = new StringBuilder("progress:");
                while ((length = fileInputStream.read(bytes, 0, bytes.length)) != -1) {
                    outWriter.write(bytes, 0, length);
                    outWriter.flush();
                    progress += length;
                    progressBuffer.append("| " + (100 * progress / file.length()) + "% |");
                }
                retMessage.addLog(progressBuffer.toString());
                retMessage.setCode(0);//发送成功/配额成功
                retMessage.addLog("send file " + file.getName() + " to " + host + " successful.");
                /**
                 * 发送文件后，等待返回结果。
                 */
                String[] failmsgs = properties.getProperty("upgrade.fail.response").split(",");
                String successmsg = properties.getProperty("upgrade.success.response");
                retMessage.addLog("wait upgrade result from  " + host + " ...");
                while ((length = inReader.read(bytes)) != -1) {
                    String rsp = new String(bytes, 0, length);
                    upgradeRspBuffer.append(rsp);
                    boolean canEnd = upgradeRspBuffer.toString().contains(successmsg);
                    if (canEnd) {
                        break;
                    }
                    for (String endmsg : failmsgs) {
                        if (upgradeRspBuffer.toString().contains(endmsg)) {
                            canEnd = true;
                            break;
                        }
                    }
                    if (canEnd) {
                        break;
                    }
                }
                retMessage.addLog("receive upgrade result: " + upgradeRspBuffer.toString());
                if (upgradeRspBuffer.toString().contains(successmsg)) {
                    retMessage.setCode(1);//升级成功
                } else {
                    for (String failmsg : failmsgs) {
                        if (upgradeRspBuffer.toString().contains(failmsg)) {
                            break;
                        }
                    }
                    retMessage.setCode(2);//升级失败
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            retMessage.addLog("upgrade exception: " + ex.toString());
        } finally {
            if (fileInputStream != null) {
                try {
                    fileInputStream.close();
                } catch (IOException ex) {
                }
            }
        }
        return retMessage;
    }

}
