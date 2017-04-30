// Chat among threads of a client

import java.util.List;
import java.util.ArrayList;

class ClientChat {
	List<MyProtocol> messageList = new ArrayList<MyProtocol>();
	// boolean flag = false;

	public synchronized void send(MyProtocol protocolMessage) {
		// flag = false;
		messageList.add(0, protocolMessage);
		MyPrint.printDebug(protocolMessage.toThread);
		// flag = true;
		notifyAll();
	}

	public synchronized MyProtocol receive(String receiver) {
		while (true) {
			try {
				wait();
			} catch(InterruptedException e) {
				MyPrint.printDebug(receiver + " : ClientChat receive error.");
			}

			if (messageList.isEmpty()) {
				continue;
			} else {
				MyProtocol receivedMessage = new MyProtocol(messageList.get(0));

				MyPrint.printDebug(receivedMessage.toThread + " : " + receivedMessage.toString());
				if ((receivedMessage.toThread).equals(receiver)) {
					MyPrint.printDebug(receiver + " is not waiting.");
					messageList.remove(0);
					notifyAll();
					return receivedMessage;
				}
			}
		}

	}
}