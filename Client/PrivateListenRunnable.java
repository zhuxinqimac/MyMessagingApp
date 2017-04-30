// Thread to receive private message

import java.net.Socket;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;

class PrivateListenRunnable implements Runnable{
	public Socket connectionSocket = null;
	public ClientChat clientCloud = null;
	public String listenFrom = "";

	PrivateListenRunnable(Socket connectionSocket, ClientChat clientCloud, String listenFrom) {
		MyPrint.printDebug("Creating PrivateListenRunnable.");
		this.connectionSocket = connectionSocket;
		this.clientCloud = clientCloud;
		this.listenFrom = listenFrom;
	}

	public void run() {
		BufferedReader inFromPeer = null;
		String sentence = "_sentence";
		MyProtocol packet = null;

		try {
			inFromPeer = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
		} catch (IOException e) {
			MyPrint.printDebug("PrivateListenRunnable In From Peer Error.");
			return;
		}

		while (true) {
			try {
				sentence = inFromPeer.readLine();
				if (sentence == null) {
					continue;
				}
				MyPrint.printDebug(sentence);
				packet = new MyProtocol(sentence);

				if ((packet.name).equals("private")) {
					MyPrint.printMessage(packet.from + " (private) : " + packet.data);
					continue;
				}

				if ((packet.name).equals("stopprivate")) {
					(ClientKernelRunnable.privateList).remove(ClientKernelRunnable.getPosition(listenFrom));
					return;
				}

			} catch	(IOException e) {
				MyPrint.printDebug("ClientInRunnable In From Server Error.");
				break;
			}
		}

	}
}