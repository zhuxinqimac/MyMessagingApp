// Protocol used to send message between server and clients

class MyProtocol {
	public String name = ""; // i.e. logout
	public String type = ""; // i.e. Ack
	public String status = ""; // i.e. OK
	public String whom = ""; // i.e. yoda
	public String from = ""; // i.e. hans
	public String data = ""; // i.e. Hello guys!
	public String fromThread = ""; // i.e. Client
	public String toThread = ""; // i.e. ClientOutRunnable

	MyProtocol() {
		name = ""; // i.e. logout
		type = ""; // i.e. Ack
		status = ""; // i.e. OK
		whom = ""; // i.e. yoda
		from = ""; // i.e. hans
		data = ""; // i.e. Hello guys!
	}

	MyProtocol(String sentence) {
		String [] word = sentence.split(Constant.PROTOCOL_SPLITER);

		for (int i = 0; i<word.length; i++) {
			MyPrint.printDebug(word[i]);
		}
		
		if (word.length >= 1) {
			this.name = word[0];
		}

		if (word.length >= 2) {
			this.type = word[1];
		}
		
		if (word.length >= 3) {
			this.status = word[2];
		}

		if (word.length >= 4) {
			this.whom = word[3];
		}
		

		if (word.length >= 5) {
			this.from = word[4];
		}
		
		if (word.length >= 6) {
			this.data = word[5];
		}
	}

	MyProtocol(String name, String type, String status, String whom, String from, String data) {
		this.name = name;
		this.type = type;
		this.status = status;
		this.whom = whom;
		this.from = from;
		this.data = data;
	}

	MyProtocol(MyProtocol origin) {
		this.name = new String(origin.name);
		this.type = new String(origin.type);
		this.status = new String(origin.status);
		this.whom = new String(origin.whom);
		this.from = new String(origin.from);
		this.data = new String(origin.data);
		if (origin.toThread != null) {
			this.toThread = new String(origin.toThread);
		}
		if (origin.fromThread != null) {
			this.fromThread = new String(origin.fromThread);
		}
	}

	MyProtocol(MyProtocol origin, String toThread, String fromThread) {
		this.name = new String(origin.name);
		this.type = new String(origin.type);
		this.status = new String(origin.status);
		this.whom = new String(origin.whom);
		this.from = new String(origin.from);
		this.data = new String(origin.data);
		this.toThread = toThread;
		this.fromThread = fromThread;
	}

	public static MyProtocol toProtocol(String sentence, String typeToSend, String fromName) {
		String [] word = sentence.split(" ");
		MyProtocol result = new MyProtocol();
		result.name = word[0];
		result.type = typeToSend;
		result.status = "OK";
		result.from = fromName;
		if (result.name.equals("message") || result.name.equals("block")
			|| result.name.equals("unblock") || result.name.equals("startprivate")
			|| result.name.equals("private") || result.name.equals("stopprivate")) {
			result.whom = word[1];
		}

		if (word.length>2) {
			result.data = word[2];
			for (int i = 3; i < word.length; i++) {
				result.data = result.data + " " + word[i];
			}
		}

		if (result.name.equals("broadcast")||result.name.equals("whoelsesince")) {
			result.data = word[1];
			for (int i = 2; i < word.length; i++) {
				result.data = result.data + " " + word[i];
			}
		}

		return result;
	}

	public static MyProtocol toProtocol(String sentence, String typeToSend, String fromName, String toThread, String fromThread) {
		String [] word = sentence.split(" ");
		MyProtocol result = new MyProtocol();
		result.name = word[0];
		result.type = typeToSend;
		result.status = "OK";
		result.from = fromName;
		result.toThread = toThread;
		result.fromThread = fromThread;
		if (result.name.equals("message") || result.name.equals("block")
			|| result.name.equals("unblock") || result.name.equals("startprivate")
			|| result.name.equals("private") || result.name.equals("stopprivate")) {
			result.whom = word[1];
		}

		if (word.length>2) {
			result.data = word[2];
			for (int i = 3; i < word.length; i++) {
				result.data = result.data + " " + word[i];
			}
		}

		if (result.name.equals("broadcast")||result.name.equals("whoelsesince")||result.name.equals("setTimeout")) {
			result.data = word[1];
			for (int i = 2; i < word.length; i++) {
				result.data = result.data + " " + word[i];
			}
		}

		return result;
	}

	@Override
	public String toString() {
		return name+Constant.PROTOCOL_SPLITER+type+Constant.PROTOCOL_SPLITER+status+Constant.PROTOCOL_SPLITER+whom+Constant.PROTOCOL_SPLITER+from+Constant.PROTOCOL_SPLITER+data;
	}
}