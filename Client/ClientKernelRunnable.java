// Client kernel thread used to setup private communication

import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.net.InetAddress;
import java.io.IOException;
import java.io.DataOutputStream;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.List;
import java.util.ArrayList;

class ClientKernelRunnable implements Runnable {
	public ClientChat clientCloud;
	public int clientPort;
	public ServerSocket welcomeSocket;
	public Socket connectionSocket;
	public static List<PrivateSocket> privateList = new ArrayList<>();

	ClientKernelRunnable(ClientChat clientCloud, int clientPort) {
		this.clientCloud = clientCloud;
		this.clientPort = clientPort;
	}

	public static int getPosition(String whom) {
		for (int i = 0; i<privateList.size(); i++) {
			if (((privateList.get(i)).getToUsername()).equals(whom)) {
				return i;
			}
		}
		return 0;
	}

	public static Socket getSocket(String whom) {
		for (int i = 0; i<privateList.size(); i++) {
			if (((privateList.get(i)).getToUsername()).equals(whom)) {
				return (privateList.get(i)).getConnectionSocket();
			}
		}
		return null;
	}

	public static Thread getThread(String whom) {
		for (int i = 0; i<privateList.size(); i++) {
			if (((privateList.get(i)).getToUsername()).equals(whom)) {
				return (privateList.get(i)).getListenThread();
			}
		}
		return null;
	}

	public static boolean isInPrivateList(String whom) {
		boolean result = false;
		for (int i = 0; i<privateList.size(); i++) {
			if (((privateList.get(i)).getToUsername()).equals(whom)) {
				result = true;
				break;
			}
		}
		return result;
	}

	public void run() {
		MyProtocol fromCloud = null;
		String privateAnswer = "_privateAnswer";
		String [] addressInfo;
		String toPrivateIP;
		String toPrivatePort;
		String localIP;
		String localPort;
		String iSentence;
		Thread privateListenThread = null;
		PrivateSocket privateEntry = null;

		// BlockingQueue<String> outRequests = new LinkedBlockingQueue<>();
		// BlockingQueue<String> inReplies = new LinkedBlockingQueue<>();
		try {
			welcomeSocket = new ServerSocket(clientPort);
			welcomeSocket.setReuseAddress(true);
		} catch (IOException e) {
			MyPrint.printDebug("ClientKernelRunnable: welcomeSocket initial error.");
		}

		while (true) {
			fromCloud = clientCloud.receive("ClientKernelRunnable");
			if (fromCloud == null) {
				continue;
			} else {
				if ((fromCloud.name).equals("startprivate")) {
					if ((fromCloud.data).equals("asServer")) {
						try {
							connectionSocket = welcomeSocket.accept();
							welcomeSocket.setReuseAddress(true);
							privateListenThread = new Thread(new PrivateListenRunnable(connectionSocket, clientCloud, fromCloud.from));
							privateListenThread.start();
							privateEntry = new PrivateSocket(fromCloud.from, connectionSocket, privateListenThread);
							privateList.add(privateEntry);
						} catch(IOException e) {
							MyPrint.printDebug("ClientKernelRunnable: accept welcomeSocket error.");
						}
					} else {
						try {
							privateAnswer = fromCloud.data;
							MyPrint.printDebug("It went through.");
							MyPrint.printDebug(privateAnswer);
							addressInfo = privateAnswer.split(" ");
							toPrivateIP = addressInfo[0].substring(1);
							toPrivatePort = addressInfo[1];
							localIP = addressInfo[2].substring(1);
							localPort = addressInfo[3];

							Thread.sleep(3*500); // Wait for server side setup

							connectionSocket = new Socket(InetAddress.getByName(toPrivateIP), Integer.parseInt(toPrivatePort));
							connectionSocket.setReuseAddress(true);
							
							MyPrint.printDebug("Private socket ok.");
							privateListenThread = new Thread(new PrivateListenRunnable(connectionSocket, clientCloud, fromCloud.from));
							privateListenThread.start();

							privateEntry = new PrivateSocket(fromCloud.from, connectionSocket, privateListenThread);
							privateList.add(privateEntry);
							MyPrint.printMessage("Start private messaging with " + fromCloud.from + ".");

						} catch (InterruptedException e) {
							MyPrint.printDebug("ClientKernelRunnable startprivate interrupted.");
						} catch (UnknownHostException e) {
							MyPrint.printDebug("ClientKernelRunnable startprivate UnknownHostException.");
						} catch (IOException e) {
							MyPrint.printDebug("ClientKernelRunnable startprivate IOException.");
						}
					}
					continue;
				} else {
					if ((fromCloud.name).equals("private")) {
						if (isInPrivateList(fromCloud.whom)) {
							try {
								iSentence = fromCloud.toString();
								connectionSocket = getSocket(fromCloud.whom);
								DataOutputStream privateOut = new DataOutputStream(connectionSocket.getOutputStream());
								privateOut.writeBytes(iSentence + '\n');
							} catch (IOException e) {
								MyPrint.printDebug("ClientKernelRunnable: private out error.");
							}
						} else {
							MyPrint.printMessage("Error. Private messaging to " + fromCloud.whom + " not enabled.");
						}
						continue;
					} else {
						if ((fromCloud.name).equals("stopprivate")) {
							if (isInPrivateList(fromCloud.whom)) {
								try {
									iSentence = fromCloud.toString();
									connectionSocket = getSocket(fromCloud.whom);
									DataOutputStream privateOut = new DataOutputStream(connectionSocket.getOutputStream());
									privateOut.writeBytes(iSentence + '\n');

									privateListenThread = getThread(fromCloud.whom);
									privateListenThread.interrupt();
									MyPrint.printMessage("Private messaging to " + fromCloud.whom + " stopped.");

									privateList.remove(getPosition(fromCloud.whom));
								} catch (IOException e) {
									MyPrint.printDebug("ClientKernelRunnable: stopprivate out error.");
								}
							} else {
								MyPrint.printMessage("Error. Private messaging to " + fromCloud.whom + " not enabled.");
							}
							continue;
						} else {
							if ((fromCloud.name).equals("logout")) {
								try {
									while(privateList.size() != 0) {
										MyProtocol tempProtocol = new MyProtocol(fromCloud);
										tempProtocol.name = "stopprivate";
										tempProtocol.whom = (privateList.get(0)).getToUsername();
										iSentence = tempProtocol.toString();
										connectionSocket = (privateList.get(0)).getConnectionSocket();
										DataOutputStream privateOut = new DataOutputStream(connectionSocket.getOutputStream());
										privateOut.writeBytes(iSentence + '\n');

										privateListenThread = (privateList.get(0)).getListenThread();
										privateListenThread.interrupt();
										MyPrint.printMessage("Private messaging to " + tempProtocol.whom + " stopped.");

										privateList.remove(0);
									}
								} catch (IOException e) {
									MyPrint.printDebug("ClientKernelRunnable: stopprivate out error.");
								}

								continue;
							}
						}
					}
				}
			}
		}

		

	}
}
