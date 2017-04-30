// Main file of Client

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.net.Socket;
import java.net.ServerSocket;
import java.io.IOException;
import java.io.InputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.DataOutputStream;
import java.net.SocketException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.lang.ArrayIndexOutOfBoundsException;

public class Client {
	public static void main(String[] args) {
		String serverName = "localhost";
		int serverPort = Constant.SERVER_PORT;
		// InetAddress serverIP = Constant.SERVER_IP;
		InetAddress serverIP = null;
		Socket clientSocket = null;
		BufferedReader inFromUser = null;
		BufferedReader inFromServer = null;
		DataOutputStream outToServer = null;

		ClientChat clientCloud;

		String sentence = "_sentence";
		String sentenceFromServer = "_fromserver";
		String username = "_username";
		String password = "_password";

		int timesTry = 1;
		boolean isLoginOK = false;
		boolean isOnline = false;
		boolean isBlocked = false;
		boolean isinvalid = false;
		boolean isPassed = false;

		boolean isLogout = false;

		String response = "_response";
		Thread outThread = null;
		Thread inThread = null;
		Thread kernelThread = null;

		String [] word;
		String clientIPAddress;
		int clientPort = 0;
		ServerSocket welcomeSocket = null;

		MyProtocol protocolSentence = null;
		MyProtocol logoutMessage = null;
		long timeout = 60;

		BlockingQueue<String> inRequests = new LinkedBlockingQueue<>();
		BlockingQueue<String> inReplies = new LinkedBlockingQueue<>();
		BlockingQueue<String> outRequests = new LinkedBlockingQueue<>();
		BlockingQueue<String> outReplies = new LinkedBlockingQueue<>();

		MyPrint.printMessage("\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n");

		if (args.length == 2) {
			serverName = args[0];
			serverPort = Integer.parseInt(args[1]);
		} else {
			System.out.println("Please input 1 IP address and 1 port number.");
			return;
		}

		

		// Establish socket connection
		try {
			serverIP = InetAddress.getByName(serverName);
		} catch (UnknownHostException e) {
			System.out.println("Connect to server IP failed.");
			return;
		}

		try {
			clientSocket = new Socket(serverIP, serverPort);
			clientSocket.setReuseAddress(true);
		} catch(IOException e) {
			System.out.println("Client Socket Initialization Error.");
			return;
		}

		System.out.println("*****************************************************************");
		System.out.println("Welcome to my simple instant messaging application 1.0.");
		System.out.println("Author: Xinqi/Steven ZHU.");
		System.out.println("All rights reserved.");
		System.out.println("This is a client side program.");
		System.out.println("If you want to know more about this program, please head to ");
		System.out.println("http://zhuxinqi.space/project/messagingapplication.html ");
		System.out.println("for more information.");
		System.out.println("\n");
		System.out.println("Please enjoy.");
		System.out.println("*****************************************************************");

		// Input username
		try {
			System.out.print("Username: ");
			inFromUser = new BufferedReader(new InputStreamReader(System.in));
			username = inFromUser.readLine();
		} catch(IOException e) {
			System.out.println("Username Input Error.");
			return;
		}

		// Try password & authentication
		while (timesTry <= 3) {
			try {
				System.out.print("Password: ");
				inFromUser = new BufferedReader(new InputStreamReader(System.in));
				password = inFromUser.readLine();
			} catch (IOException e) {
				System.out.println("Password Input Error.");
				return;
			}

			try {
				outToServer = new DataOutputStream(clientSocket.getOutputStream());
				outToServer.writeBytes(username + ' ' + password + '\n');
			} catch	(IOException e) {
				MyPrint.printDebug("Out To Server Error.");
			}

			try {
				inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
				sentenceFromServer = inFromServer.readLine();			
				// MyPrint.printDebug(sentenceFromServer);
			} catch	(IOException e) {
				MyPrint.printDebug("In From Server Error.");
			}

			if (sentenceFromServer.equals("true")) {
				isLoginOK = true;
				break;
			} else {
				if (sentenceFromServer.equals("alreadyLogin")) {
					MyPrint.printMessage("You have already been logged in!!!");
					isOnline = true;
					break;
				} else {
					if (sentenceFromServer.equals("blockedLogin")) {
						MyPrint.printMessage("Your account is blocked due to multiple login failures. Please try again later.");
						isBlocked = true;
						break;
					} else {
						if (sentenceFromServer.equals("invalidName")) {
							MyPrint.printMessage("This is not a valid username. Please connect again.");
							isinvalid = true;
							break;
						} else {
							MyPrint.printMessage("Invalid Password. Please try again.");
						}
					}
				}
			}

			timesTry = timesTry + 1;
		}

		// Check authentication
		if (!isLoginOK && !isOnline && !isBlocked && !isinvalid) {
			MyPrint.printMessage("Invalid Password. Your account has been blocked. Please try again later.");
			try {
				clientSocket.close();
			} catch (IOException e) {
				MyPrint.printDebug("Socket Close Error.");
			}
			return;
		}
		else {
			// This is the only case of successful login
			if (!isOnline && !isBlocked && !isinvalid) {
				isPassed = true;
				MyPrint.printMessage("Welcome to the greatest messaging application ever!");
			}
		}

		if (!isPassed) {
			try {
				clientSocket.close();
			} catch (IOException e) {
				MyPrint.printDebug("Socket Close Error.");
			}
			return;
		}

		// Get client port to open welcome socket (for private use)
		try {
			inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
			sentenceFromServer = inFromServer.readLine();
			MyPrint.printDebug(sentenceFromServer);
			word = sentenceFromServer.split(" ");
			clientIPAddress = word[0];
			clientPort = Integer.parseInt(word[1]);
			// welcomeSocket = new ServerSocket(clientPort);
			MyPrint.printDebug(clientIPAddress);
			MyPrint.printDebug(Integer.toString(clientPort));
			// MyPrint.printDebug(sentenceFromServer);
		} catch	(IOException e) {
			MyPrint.printDebug("WelcomeInfo In From Server Error.");
		}

		// Get timeout
		try {
			inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
			sentenceFromServer = inFromServer.readLine();
			MyPrint.printDebug(sentenceFromServer);
			word = sentenceFromServer.split(" ");
			timeout = Long.parseLong(word[1]);
			MyPrint.printDebug("Client get : " + timeout);
			// MyPrint.printDebug(sentenceFromServer);
		} catch	(IOException e) {
			MyPrint.printDebug("WelcomeInfo In From Server Error.");
		} catch (ArrayIndexOutOfBoundsException e) {
			System.out.println("Error: ArrayIndexOutOfBoundsException!!!");
			return;
		}

		clientCloud = new ClientChat();

		outThread = new Thread(new ClientOutRunnable(clientCloud, clientSocket, username));
		outThread.start();

		inThread = new Thread(new ClientInRunnable(clientCloud, clientSocket, username));
		inThread.start();

		kernelThread = new Thread(new ClientKernelRunnable(clientCloud, clientPort));
		kernelThread.start();


		// Main loop of user input (send message, broadcast, logout... )
		while (true) {

			// Handle timeout
	        try {
                long startTime = System.currentTimeMillis();
                while ((System.currentTimeMillis() - startTime) < timeout * 1000 && !inFromUser.ready()) {
                        if ((System.currentTimeMillis() - startTime) % 1000 == 0) {
                           // System.out.println(username + ((System.currentTimeMillis() - startTime) / 1000));
                        }
                }
                if (inFromUser.ready()) {
                        sentence = inFromUser.readLine();
                } else {
                        sentence = "logout";
                }
	        } catch (IOException e) {
                MyPrint.printDebug("User Input Error.");
	        }

			if (!ValidCommands.check(sentence)) {
				MyPrint.printMessage("Invalid Command. Please try another one.");
				continue;
			}

			// Logout
			try {
				if (sentence.equals("logout")) {
					// toProtocol(sentence, type, fromName, toThread, fromThread);
					logoutMessage =  MyProtocol.toProtocol(sentence, "Client", username, "ClientOutRunnable", "Client");
					MyPrint.printDebug(logoutMessage.toString());
					clientCloud.send(logoutMessage);
					// outRequests.put(sentence);	
					inThread.join(Constant.THREAD_WAIT * 1000);
					MyPrint.printDebug("In thread joint.");
					outThread.interrupt();
					MyPrint.printDebug("Out thread interrupted.");
					// kernelThread.interrupt();
					// MyPrint.printDebug("Kernel thread interrupted.");
					break;
				}
			} catch (InterruptedException e) {
				MyPrint.printDebug("Client logout interrupted.");
			}

			// Send command to server
			protocolSentence = MyProtocol.toProtocol(sentence, "Client", username, "ClientOutRunnable", "Client");
			MyPrint.printDebug(protocolSentence.toString());
			clientCloud.send(protocolSentence);

		}

		try {
			clientSocket.close();
			MyPrint.printDebug("Socket closed.");
		} catch (IOException e) {
			MyPrint.printDebug("Socket Close Error.");
		}
		System.exit(0);
	}
}
