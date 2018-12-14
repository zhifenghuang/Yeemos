package com.yeemos.app.chat.rpcBean;

/**
 * User
 */
public class User
{
	private long userId;
	private String userName;
	private String avatar;

	public long getUserId() {
		return userId;
	}

	public void setUserId(long userId) {
		this.userId = userId;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getAvatar() {
		return avatar;
	}

	public void setAvatar(String avatar) {
		this.avatar = avatar;
	}

	public User()
	{
		super();

	}
}
