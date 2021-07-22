package com.synear.ThreadPool.pojo;

public class LogBean implements Runnable{

    private int id;

    private String logContent;

    private String createts;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getLogContent() {
        return logContent;
    }

    public void setLogContent(String logContent) {
        this.logContent = logContent;
    }

    public String getCreatets() {
        return createts;
    }

    public void setCreatets(String createts) {
        this.createts = createts;
    }


    @Override
    public String toString() {
        return "TestLogBean{" +
                "id=" + id +
                ", logContent='" + logContent + '\'' +
                ", createts='" + createts + '\'' +
                '}';
    }

    @Override
    public void run() {
        System.out.println("TestLogBean{" +
                "id=" + id +
                ", logContent='" + logContent + '\'' +
                ", createts='" + createts + '\'' +
                '}');
    }
}

