// Class to store user's state

import java.util.List;
import java.util.ArrayList;


class UserState {
	private String username;
	private String password;
	private String ipAddress;
	private int port;

	private boolean isOnline = false;
	private boolean isBlockedLogin = false;
	private boolean isOffline = true;
	public Boolean isReceiving = false;
	private List<MyProtocol> receivingBuffer = new ArrayList<MyProtocol>();
	private List<String> blockedByList = new ArrayList<String>();

	UserState(String user, String pass) {
		username = user;
		password = pass;
	}

	public synchronized void setIPAddress(String ip) {
		this.ipAddress = ip;
	}

	public synchronized void setPort(int port) {
		this.port = port;
	}

	public synchronized String getIPAddress() {
		return ipAddress;
	}

	public synchronized int getPort() {
		return port;
	}

	public String getUsername() {
		return username;
	}

	public String getState() {
		if (isOnline) {
			return "Online";
		} else {
			if (isBlockedLogin) {
				return "BlockedLogin";
			}
			else {
				return "Offline";
			}
		}
	}

	public synchronized List<String> getBlockedByList() {
		return blockedByList;
	}

	public synchronized void addToBlockedByList(String blockedOne) {
		blockedByList.add(blockedOne);
	}

	public synchronized void removeFromBlockedByList(String unblockedOne) {
		blockedByList.remove(unblockedOne);
	}

	public boolean isOnline() {
		return isOnline;
	}

	public boolean isOffline() {
		return isOffline;
	}

	public boolean isBlockedLogin() {
		return isBlockedLogin;
	}

	public void changeToOnline() {
		isOnline = true;
		isOffline = false;
		isBlockedLogin = false;
	}

	public void changeToOffline() {
		isOffline = true;
		isOnline = false;
		isBlockedLogin = false;
	}

	public void changeToBlockedLogin() {
		isOnline = false;
		isOffline = false;
		isBlockedLogin = true;
	}

	public void addMessage(MyProtocol message) {
		receivingBuffer.add(message);
		if (!isReceiving) {
			isReceiving = true;
		}
	}

	public MyProtocol getMessage() {
		if (isReceiving) {
			return receivingBuffer.get(0);
		}
		return null;
	}

	public void removeMessage() {
		receivingBuffer.remove(0);
		if (receivingBuffer.size() == 0) {
			isReceiving = false;
		}
	}
}