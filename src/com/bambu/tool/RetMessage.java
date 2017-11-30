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
public class RetMessage {

    private int code;
    private StringBuilder logBuffer = new StringBuilder();//记录日志信息
    private StringBuilder noteBuffer = new StringBuilder();//记录提示信息

    public void addLog(String message) {
        String msg = "[Info] "+message;
        System.out.println(msg);
        getLogBuffer().append(msg).append("\n");
    }

    public void addInfo(String message) {
        getNoteBuffer().append("[Info] ").append(message).append("\n");
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public StringBuilder getLogBuffer() {
        return logBuffer;
    }

    public void setLogBuffer(StringBuilder logBuffer) {
        this.logBuffer = logBuffer;
    }

    public StringBuilder getNoteBuffer() {
        return noteBuffer;
    }

}
