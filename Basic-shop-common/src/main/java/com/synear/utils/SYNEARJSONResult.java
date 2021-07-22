package com.synear.utils;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 *
 * @Title: SYNEARJSONResult.java
 * @Description: 自定义响应数据结构
 * 				200：表示成功
 * 				500：表示错误，错误信息在msg字段中
 * 				501：bean验证错误，不管多少个错误都以map形式返回
 * 				502：拦截器拦截到用户token出错
 * 				555：异常抛出信息
 * 				556: 用户qq校验异常
 * 				557: 临时凭证校验异常
 * @author Synear
 */
public class SYNEARJSONResult {

    // 定义jackson对象
    private static final ObjectMapper MAPPER = new ObjectMapper();

    // 响应业务状态
    private Integer status;

    // 响应消息
    private String msg;

    // 响应中的数据
    private Object data;

    @JsonIgnore
    private String ok;	// 不使用

    public static SYNEARJSONResult build(Integer status, String msg, Object data) {
        return new SYNEARJSONResult(status, msg, data);
    }

    public static SYNEARJSONResult build(Integer status, String msg, Object data, String ok) {
        return new SYNEARJSONResult(status, msg, data, ok);
    }

    public static SYNEARJSONResult ok(Object data) {
        return new SYNEARJSONResult(data);
    }

    public static SYNEARJSONResult ok() {
        return new SYNEARJSONResult(null);
    }

    public static SYNEARJSONResult errorMsg(String msg) {
        return new SYNEARJSONResult(500, msg, null);
    }

    public static SYNEARJSONResult errorUserTicket(String msg) {
        return new SYNEARJSONResult(500, msg, null);
    }

    public static SYNEARJSONResult errorMap(Object data) {
        return new SYNEARJSONResult(501, "error", data);
    }

    public static SYNEARJSONResult errorTokenMsg(String msg) {
        return new SYNEARJSONResult(502, msg, null);
    }

    public static SYNEARJSONResult errorException(String msg) {
        return new SYNEARJSONResult(555, msg, null);
    }

    public static SYNEARJSONResult errorUserQQ(String msg) {
        return new SYNEARJSONResult(556, msg, null);
    }

    public SYNEARJSONResult() {

    }

    public SYNEARJSONResult(Integer status, String msg, Object data) {
        this.status = status;
        this.msg = msg;
        this.data = data;
    }

    public SYNEARJSONResult(Integer status, String msg, Object data, String ok) {
        this.status = status;
        this.msg = msg;
        this.data = data;
        this.ok = ok;
    }

    public SYNEARJSONResult(Object data) {
        this.status = 200;
        this.msg = "OK";
        this.data = data;
    }

    public Boolean isOK() {
        return this.status == 200;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

	public String getOk() {
		return ok;
	}

	public void setOk(String ok) {
		this.ok = ok;
	}

}
