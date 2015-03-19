package ns.tcphack;

import java.io.UnsupportedEncodingException;

/**
 * Created by cotix on 3/19/15.
 */
public class TransportLayer {
    private NetworkLayer network;
    private byte[] dst;
    private int srcPort;
    private int dstPort;
    private int ackNumber;
    boolean connected;
    private int seqNumber;
    public TransportLayer(NetworkLayer netLayer, byte[] target, int port) {
        network = netLayer;
        dst = target;
        srcPort = (int)(Math.random()*Short.MAX_VALUE);
        dstPort = port;
        connected = false;
        seqNumber = 0;
        ackNumber = 0;
    }

    public void send(String message) {
        byte[] data = null;
        try {
            data = message.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        byte[] headers = IPv6.makeHeaders(20 + data.length, network.getOwnAddress(), dst);
        TCPPacket packet = new TCPPacket(srcPort, dstPort, seqNumber, ackNumber, TCPPacket.ControlBit.PSH.getValue()
                | TCPPacket.ControlBit.ACK.getValue(), 64, data, headers);
        seqNumber += data.length;
        network.send(dst, packet.getPacket());
    }

    public String recv() {
        byte[] data = network.recv();
        if (data.length == 0) {
            return null;
        }
        TCPPacket packet = new TCPPacket(data);
        ackNumber = packet.getSequenceNumber() + packet.getData().length;
        sendAck();
        try {
            return new String(packet.getData(), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return "Error";
    }

    public void sendAck() {
        byte[] data = new byte[0];
        byte[] headers = IPv6.makeHeaders(20, network.getOwnAddress(), dst);
        TCPPacket packet = new TCPPacket(srcPort, dstPort, seqNumber, ackNumber,
                TCPPacket.ControlBit.ACK.getValue(), 64, data, headers);
        network.send(dst, packet.getPacket());
    }

    public void connect() {
        //Lets send a SYN packet to connect.
        byte[] data = new byte[0];
        byte[] headers = IPv6.makeHeaders(20, network.getOwnAddress(), dst);
        TCPPacket packet = new TCPPacket(srcPort, dstPort, seqNumber, 0, TCPPacket.ControlBit.SYN.getValue(), 64, data, headers);
        network.send(dst, packet.getPacket());
    }
}
