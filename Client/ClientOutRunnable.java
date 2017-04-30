// Send messages to server (message, broadcast... )

import java.lang.InterruptedException;
import java.util.concurrent.BlockingQueue;
import java.net.Socket;
import java.io.DataOutputStream;
import java.io.IOException;

class ClientOutRunnable implements Runnable{
	public ClientChat clientCloud;
	public Socket socket;
	public String username;

	ClientOutRunnable(ClientChat clientCloud, Socket clientSocket, String username) {
		this.clientCloud = clientCloud;
		this.socket = clientSocket;
		this.username = username;
	}

	public void run() {
		MyProtocol fromCloud = null;
		MyProtocol ackProtocol = null;
		String sentence = "_sentence";
		DataOutputStream outToServer = null;

		try {
			outToServer = new DataOutputStream(socket.getOutputStream());
		} catch (IOException e) {
			MyPrint.printDebug("ClientInRunnable In From Server Error.");
			return;
		}
		while(true) {
			try	{
				while (true) {
					fromCloud = clientCloud.receive("ClientOutRunnable");
					if (fromCloud != null) {
						break;
					}
				}

				if ((fromCloud.name).equals("logout")) {
					ackProtocol = new MyProtocol(fromCloud, "ClientKernelRunnable", "ClientOutRunnable");
					clientCloud.send(ackProtocol);
					try {
						Thread.sleep(300);
					} catch (InterruptedException e) {
						MyPrint.printDebug("ClientOutRunnable: logout delay failed.");
					}
				}

				if ((fromCloud.name).equals("startprivate")) {
					if ((ClientKernelRunnable.privateList).contains(fromCloud.whom)) {
						fromCloud.status = "Error";
						fromCloud.type = "Ack";
						fromCloud.data = "You are already in private connection with " + fromCloud.whom + ".";
						fromCloud.whom = username;
						ackProtocol = new MyProtocol(fromCloud, "ClientKernelRunnable", "ClientOutRunnable");
						clientCloud.send(ackProtocol);
						continue;
					}
				}

				if ((fromCloud.name).equals("private")) {
					ackProtocol = new MyProtocol(fromCloud, "ClientKernelRunnable", "ClientOutRunnable");
					clientCloud.send(ackProtocol);
					continue;
				}

				if ((fromCloud.name).equals("stopprivate")) {
					ackProtocol = new MyProtocol(fromCloud, "ClientKernelRunnable", "ClientOutRunnable");
					clientCloud.send(ackProtocol);
					continue;
				}

				fromCloud.type = "Request";

				sentence = fromCloud.toString();
				MyPrint.printDebug(sentence);
				// outToServer = new DataOutputStream(socket.getOutputStream());
				outToServer.writeBytes(sentence + '\n');

			} catch	(IOException e) {
				MyPrint.printDebug("ClientOutRunnable Out To Server Error.");
				break;
			}
		}
	}
}
