package com.yeemos.app.chat.rpcBean;

import com.gigabud.core.http.IResponseBean;

/**
 * RPC消息
 */
public class RPCResponseBaseBean<T> implements IResponseBean
{
	private int success;
	private String errorCode;
	private String errorDes;
	private int costTime;
	private long timestamp;
	private T data;

	public RPCResponseBaseBean()
	{
		super();

	}


	/**
	 * 返回这次请求是否成功，由success字段或errorCode判断
	 * @return
	 */
	public boolean isSuccess()
	{
		return 1== getSuccess()	? true : false;
	}

	/**
	 * 出错时返回errorCode,不出错时可能为null
	 * @return
	 */
	public String getErrorCode() {
		return errorCode;
	}

	/**
	 * 出错时设置errorCode
	 * @return
	 */
	public void setErrorCode(String errorCode) {
		this.errorCode = errorCode;
	}
	/**
	 * 出错的相关描述，可能为null
	 * @return
	 */
	public String getErrorMessage()
	{
		return getErrorDes();
	}







	public int getSuccess() {
		return success;
	}

	public void setSuccess(int success) {
		this.success = success;
	}




	public String getErrorDes() {
		return errorDes;
	}

	public void setErrorDes(String errorDes) {
		this.errorDes = errorDes;
	}

	public int getCostTime() {
		return costTime;
	}

	public void setCostTime(int costTime) {
		this.costTime = costTime;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

	public T getData() {
		return data;
	}

	public void setData(T data) {
		this.data = data;
	}
}
