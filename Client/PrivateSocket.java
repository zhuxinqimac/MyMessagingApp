// PrivateSocket class contains established private connection

import java.net.Socket;

class PrivateSocket {
	private String toUsername;
	private Socket connectionSocket;
	public Thread listenThread;

	PrivateSocket(String toUsername, Socket connectionSocket, Thread listenThread) {
		this.toUsername = toUsername;
		this.connectionSocket = connectionSocket;
		this.listenThread = listenThread;
	}

	public String getToUsername() {
		return toUsername;
	}

	public Socket getConnectionSocket() {
		return connectionSocket;
	}

	public Thread getListenThread() {
		return listenThread;
	}
}