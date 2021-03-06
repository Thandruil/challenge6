package ns.tcphack;

class MyTcpHandler extends TcpHandler {
    public static int VERSION = 253;

	public static void main(String[] args) {
		new MyTcpHandler();
	}

	public MyTcpHandler() {
		super();

		boolean done = false;
        boolean sent = false;
        NetworkLayer nLayer = new NetworkLayer(this, IPv6.myIP);
        TransportLayer tLayer = new TransportLayer(nLayer, IPv6.remoteIP, 7712);
        tLayer.connect();

        long lastMsg = System.currentTimeMillis();
		while (!done) {
            String result = tLayer.recv();
            if (result != null) {
                lastMsg = System.currentTimeMillis();
                System.out.print(result);
            }
            if (tLayer.connected() && !sent) {
                sent = true;
                tLayer.send("GET /?nr=1570900 HTTP/1.0\n\n");
            }
            if (System.currentTimeMillis() - lastMsg >= 10*1000) {
                tLayer.close();
                done = true;
            }
		}   
	}
}
