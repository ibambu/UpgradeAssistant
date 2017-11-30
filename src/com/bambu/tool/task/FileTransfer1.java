package com.bambu.tool.task;

import com.bambu.tool.ResponseMessage;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.Callable;

/**
 *
 * @author luotao
 */
public class FileTransfer1 implements Callable {

    private Socket clientSocket;
    private String filePath;
    private String upgradeSuccessMessage;
    private String upgradeFailMessage;
    private String upgradeErr;
    private String upgradeFail;
    private String correctHostMessage;
    private String endFlag;
    private String host;
    private int port;
    private int socketTimeOut;
    private DataInputStream inReader;
    private DataOutputStream outWriter;
    private String restartCmd;
    private long waitResultTime;
    private Properties properties;

    public FileTransfer1(Socket socket, Properties properties, DataInputStream inReader, DataOutputStream outWriter) {
        this.clientSocket = socket;
        this.properties = properties;
        this.inReader = inReader;
        this.outWriter = outWriter;
    }

    public FileTransfer1(Properties properties, String host) throws IOException {
        this.host = host;
        this.port = Integer.parseInt(properties.getProperty("upgrade.host.connect.port"));
        this.upgradeSuccessMessage = properties.getProperty("upgrade.success.response");
        this.upgradeFailMessage = properties.getProperty("upgrade.fail.response");
        this.correctHostMessage = properties.getProperty("upgrade.host.correct.response");
        this.filePath = properties.getProperty("upgrade.data.file");
        this.socketTimeOut = Integer.parseInt(properties.getProperty("upgrade.connect.timeout"));
        this.waitResultTime = Long.parseLong(properties.getProperty("upgrade.wait.result.timeout"));
        this.upgradeErr = upgradeFailMessage.split(",")[1];
        this.upgradeFail = upgradeFailMessage.split(",")[0];
        this.restartCmd = properties.getProperty("upgrade.host.restart.cmd");
        this.clientSocket = createConnect(host, port, socketTimeOut);
    }

    /**
     * 连接服务器
     *
     * @param ip
     * @param port
     * @param timeout
     * @param hostCorrectRsp
     * @return
     */
    private Socket createConnect(String ip, int port, int timeout) throws IOException {
        Socket socket = null;
        try {
            socket = new Socket(ip, port);
            socket.setSoTimeout(timeout);//设置连接超时时间
            writeToLogFile("[Info] connect to " + ip + " successful.");
        } catch (Exception e) {
            writeToLogFile("[Info] connect to " + ip + " fail.");
            e.printStackTrace();
        }

        return socket;
    }

    /**
     * 释放连接资源
     */
    private void destory() {
        try {
            if (outWriter != null) {
                outWriter.close();
            }
            if (inReader != null) {
                inReader.close();
            }
            if (clientSocket != null) {
                clientSocket.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public Object call() throws Exception {
        StringBuilder resultBuffer = new StringBuilder();
        ResponseMessage rspMessage = new ResponseMessage();
        List<String> endMsgList = new ArrayList();
        try {
            File file = new File(filePath);
            inReader = new DataInputStream(clientSocket.getInputStream());
            outWriter = new DataOutputStream(clientSocket.getOutputStream());
            writeToLogFile("[Info] get reader/writer successful");
            String outMessage = "";
            writeToLogFile("[Info] wait message...");
            endMsgList.add(correctHostMessage);
            String connectMsg = readMessage(endMsgList);
            writeToLogFile("[Info] accept message..." + connectMsg);
            if (connectMsg != null && connectMsg.contains(correctHostMessage)) {
                outMessage = "[Info] valid host :" + host;
                writeToLogFile(outMessage);
                /**
                 * 合法主机，可以升级。
                 */
                if (file.exists()) {
                    /**
                     * 发送升级文件
                     */
                    rspMessage = sendFile(file);
                } else {
                    rspMessage.setResponseCode(0);
                    rspMessage.setResponseMessage("[Info] upgrade file not found:" + file.getName());
                }
            } else {
                /**
                 * 非法主机，不能升级。
                 */
                rspMessage.setResponseCode(0);
                rspMessage.setResponseMessage("[Info] upgrade host invalid :" + host);
            }
            /**
             * 所有文件传输完后，开始读取返回信息。
             */
            String upgradeResult = rspMessage.getResponseMessage();
            if (upgradeResult != null) {
                if (upgradeResult.contains(upgradeSuccessMessage)) {
                    /**
                     * 1. 升级成功
                     */
                    outMessage = "[OK] " + host + " upgrade successful. [file:" + file.getName() + "][size:" + file.length() + "]";

                    outWriter.write(restartCmd.getBytes());//发送重启命令
                    outWriter.flush();
                    writeToLogFile("[Info] send restart cmd...[at!r\\r]");
                } else if (upgradeResult.contains(upgradeFailMessage)) {
                    /**
                     * 2. 升级失败
                     */
                    outMessage = "[Fail] " + host + " upgrade fail.";

                } else if (!upgradeResult.contains(this.upgradeSuccessMessage)
                        && !upgradeResult.contains(this.upgradeErr)
                        && !upgradeResult.contains(this.upgradeFail)) {
                    /**
                     * 3. socket 超时后直接推出，暂时不是处理。
                     */
                    outMessage = "[Info] " + host + "just config successful.";
                } else {
                    outMessage = "[Error] " + host + "upgrade error.";
                }
                writeToLogFile(outMessage);
            }
            rspMessage.setLogMessage(rspMessage.getLogMessage() + "\n" + outMessage);
            if (outMessage.trim().length() > 0) {
                resultBuffer.append(rspMessage.getLogMessage()).append("\n");
            }
        } catch (Exception e) {
            e.printStackTrace();
            writeToLogFile(resultBuffer.toString());
        }
        /**
         * 释放所有连接资源
         */
        Thread.sleep(3000);
        destory();

        return resultBuffer.toString();
    }

    /**
     * 写日志文件
     *
     * @param content
     * @param filename
     * @throws IOException
     */
    private void writeToLogFile(String content) throws IOException {
        BufferedWriter bufwriter = null;
        String filename = "upgrade-" + host.replaceAll("\\.", "-") + ".log";
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

    /**
     * 扫描目录，返回目录及其子目录所有文件集合。
     *
     * @param rootFile
     * @param fileList
     * @return
     */
    private List<File> scanFiles(File rootFile, List<File> fileList) {
        if (rootFile.isDirectory()) {
            File[] subList = rootFile.listFiles();
            for (File file : subList) {
                scanFiles(file, fileList);
            }
        } else if (rootFile.isFile()) {
            fileList.add(rootFile);
        }
        return fileList;
    }

    /**
     * 向服务端传输文件
     *
     * @param clientSocket
     * @param file
     * @throws Exception
     */
    private ResponseMessage sendFile(File file) throws InterruptedException {
        FileInputStream fileInputStream = null;
        StringBuilder logBuffer = new StringBuilder();
        ResponseMessage respMeesage = new ResponseMessage();
        String logMsg = "";
        try {
            if (file.exists()) {
                fileInputStream = new FileInputStream(file);

                // 开始传输文件  
                byte[] bytes = new byte[1024 * 10];
                int length = 0;
                long progress = 0;

                logMsg = "[Info] start to send file " + file.getName() + " ......";
                writeToLogFile(logMsg);

                logMsg = "[Info] progress:";

                while ((length = fileInputStream.read(bytes, 0, bytes.length)) != -1) {
                    outWriter.write(bytes, 0, length);
                    outWriter.flush();
                    progress += length;
                    logBuffer.append("| ").append(100 * progress / file.length()).append("% |");
                    System.out.print("| " + (100 * progress / file.length()) + "% |");
                }
                logBuffer.append("\n");
                writeToLogFile(logBuffer.toString());
                System.out.println();

                logMsg = "[Info] " + file.getName() + " send completed ......\n[Info] wait upgrade result .......\n";
                writeToLogFile(logMsg);

                List<String> endMsgList = new ArrayList();
                endMsgList.add(upgradeSuccessMessage);
                endMsgList.addAll(Arrays.asList(upgradeFailMessage.split(",")));
                String rspMessage = readMessage(endMsgList);//读取返回信息,关注点.
                logMsg = "[Info] Receive upgrade result:" + rspMessage;
                writeToLogFile(logMsg);
                respMeesage.setResponseCode(1);
                respMeesage.setResponseMessage(rspMessage);
            }
            respMeesage.setLogMessage(logBuffer.toString());
        } catch (IOException e) {
            respMeesage.setResponseCode(0);
            respMeesage.setResponseMessage("[Fail] send host: " + host + " file fail: " + file.getName());
        } finally {
            if (fileInputStream != null) {
                try {
                    fileInputStream.close();
                } catch (IOException ex) {

                }
            }
        }
        return respMeesage;
    }

    private String readMessage(List<String> endMessageList) {

        StringBuilder messageBuffer = new StringBuilder();
        byte[] b = new byte[1024 * 10];
        int length = 0;
        long startTime = System.currentTimeMillis();
        try {
            while ((length = inReader.read(b)) != -1) {
                String rsp = new String(b, 0, length);
                messageBuffer.append(rsp);
                boolean isEnd = false;
                for (String endstr : endMessageList) {
                    if (rsp.contains(endstr)) {
                        isEnd = true;
                        break;
                    }
                }
                long costtime = System.currentTimeMillis() - startTime;
                if (isEnd || costtime > this.waitResultTime) {
                    break;
                }
            }
        } catch (SocketTimeoutException timeoutex) {

        } catch (Exception e) {
        }
        System.out.println("[Info] Receive message:" + messageBuffer.toString());
        return messageBuffer.toString();
    }

}
