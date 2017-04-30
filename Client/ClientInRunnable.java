// Receive messages from server (ack, broadcast... )

import java.util.concurrent.BlockingQueue;
import java.net.Socket;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;


class ClientInRunnable implements Runnable{
	public Socket socket;
	public String username;
	public ClientChat clientCloud;

	ClientInRunnable(ClientChat clientCloud, Socket clientSocket, String username) {
		this.clientCloud = clientCloud;
		this.socket = clientSocket;
		this.username = username;
	}

	public void run() {
		String toClient = "_toClient";
		String sentence = "_sentence";
		MyProtocol packet;
		String fromServerName = "_fromServerName"; // i.e. logout
		String fromServerType = "_fromServerType"; // i.e. Ack
		String fromServerStatus = "_fromServerStatus"; // i.e. OK
		String fromServerData = "_fromServerData";  // i.e logout
		BufferedReader inFromServer = null;
		MyProtocol timeoutMessage = null;
		MyProtocol protocolMessage = null;
		String setTimeout = "_setTimeout";
		String [] word;

		try {
			inFromServer = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		} catch (IOException e) {
			MyPrint.printDebug("ClientInRunnable In From Server Error.");
			return;
		}

		while (true) {
			try {
				// inFromServer = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				sentence = inFromServer.readLine();
				MyPrint.printDebug(sentence);
				packet = new MyProtocol(sentence);

				if ((packet.name).equals("startprivate")) {
					if ((packet.status).equals("OK")) {
						if ((packet.type).equals("Ack")) {
							// try {
								protocolMessage = new MyProtocol(packet, "ClientKernelRunnable", "ClientInRunnable");
								MyPrint.printDebug("ClientInRunnable get startprivate: " + protocolMessage.toString());
								clientCloud.send(protocolMessage);
							// } catch (InterruptedException e) {
								// MyPrint.printDebug("ClientInRunnable startprivate interrupted.");
							// }
							continue;
						} else {
							if ((packet.type).equals("Response")) {
								// try {
									protocolMessage = new MyProtocol(packet, "ClientKernelRunnable", "ClientInRunnable");
									MyPrint.printDebug("ClientInRunnable get startprivate: " + protocolMessage.toString());
									clientCloud.send(protocolMessage);
								// } catch (InterruptedException e) {
									// MyPrint.printDebug("ClientInRunnable startprivate interrupted.");
								// }
							}
						}
					}
					else{
						MyPrint.printMessage(packet.data);
					}
					continue;
				}

				if ((packet.type).equals("Ack")) {
					if ((packet.name).equals("logout")) {
						if ((packet.status).equals("OK")) {
							break;
						}
					} else {
						MyPrint.printMessage(packet.data);
						continue;
					}
				}

				if ((packet.type).equals("Response")) {
					if ((packet.status).equals("OK")) {
						MyPrint.printMessage(packet.from + " : " + packet.data);
						continue;
					}
				}

			} catch	(IOException e) {
				MyPrint.printDebug("ClientInRunnable In From Server Error.");
				break;
			}
		}
	}
}