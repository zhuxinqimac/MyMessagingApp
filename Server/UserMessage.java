// The entry of user and the message he sent

class UserMessage {
	private String username;
	private String message;

	UserMessage(String username, String message) {
		this.username = username;
		this.message = message;
	}

	public String getUsername() {
		return username;
	}

	public String getMessage() {
		return message;
	}

	public String toString() {
		return username + ":'" + message + "'";
	}
}