package server_1_client;

import lombok.Data;
import org.msgpack.annotation.Message;

/**
 * @Author: xiaohuan
 * @Date: 2020-01-28 21:24
 */
//@Data
@Message
public class UserInfo {
	private int userId;
	private String userName;

	public int getUserId() {
		return userId;
	}

	public void setUserId(int userId) {
		this.userId = userId;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	@Override
	public String toString() {
		return "User{" +
				"userId='" + userId + '\'' +
				", userName=" + userName +
				'}';
	}
}
