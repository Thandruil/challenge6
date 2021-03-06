package ns.tcphack;

public class TCPPacket {
    public enum ControlBit {
        URG(32), ACK(16), PSH(8), RST(4), SYN(2), FIN(1);

        private final int value;

        ControlBit(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }

    byte[] packet;

    public int getSourcePort() {
        return ((packet[0] & 0xFF) << 8) + (packet[1] & 0xFF);
    }

    public int getDestinationPort() {
        return ((packet[2] & 0xFF) << 8) + (packet[3] & 0xFF);
    }

    public int getSequenceNumber() {
        return ((packet[4] & 0xFF) << 24) + ((packet[5] & 0xFF) << 16) + ((packet[6] & 0xFF) << 8) + (packet[7] & 0xFF);
    }

    public int getAcknowledgementNumber() {
        return ((packet[8] & 0xFF) << 24) + ((packet[9] & 0xFF) << 16) + ((packet[10] & 0xFF) << 8) + (packet[11] & 0xFF);
    }

    public int getDataOffset() {
        return (packet[12] & 0xFF) >> 4;
    }

    public int getControlBits() {
        return packet[13] & 63;
    }

    public int getWindow() {
        return ((packet[14] & 0xFF) << 8) + (packet[15] & 0xFF);
    }

    public int getChecksum() {
        return ((packet[16] & 0xFF) << 8) + (packet[17] & 0xFF);
    }

    public int getUrgentPointer() {
        return (packet[18] << 8) + (packet[19] & 0xFF);
    }

    public byte[] getData() {
        int offset = getDataOffset();
        byte[] data = new byte[packet.length - getDataOffset() * 4];
        System.arraycopy(packet, getDataOffset() * 4, data, 0, packet.length - getDataOffset() * 4);
        return data;
    }

    public byte[] getPacket() {
        return packet;
    }

    public TCPPacket(byte[] data) {
        packet = data;
    }

    public void print(byte[] buffer) {
        for (int i = 0; i != buffer.length; ++i) {
            System.out.printf("%x ", buffer[i]);
            if (i%16 == 0) {
                System.out.println();
            }
        }
        System.out.println();
    }

    public TCPPacket(int sourcePort, int destinationPort, int sequenceNumber, int acknowledgementNumber, int controlBits, int window, byte[] data) {
        packet = new byte[5 * 4 + data.length]; // TODO: Options

        packet[0] = (byte) ((sourcePort & 0xFF00) >> 8);
        packet[1] = (byte) (sourcePort & 0xFF);
        packet[2] = (byte) ((destinationPort & 0xFF00) >> 8);
        packet[3] = (byte) (destinationPort & 0xFF);

        packet[4] = (byte) ((sequenceNumber & 0xFF000000) >> 24);
        packet[5] = (byte) ((sequenceNumber & 0xFF0000) >> 16);
        packet[6] = (byte) ((sequenceNumber & 0xFF00) >> 8);
        packet[7] = (byte) (sequenceNumber & 0xFF);

        packet[8] = (byte) ((acknowledgementNumber & 0xFF000000) >> 24);
        packet[9] = (byte) ((acknowledgementNumber & 0xFF0000) >> 16);
        packet[10] = (byte) ((acknowledgementNumber & 0xFF00) >> 8);
        packet[11] = (byte) (acknowledgementNumber & 0xFF);

        packet[12] = (byte) (5 << 4);
        packet[13] = (byte) (controlBits & 63);
        packet[14] = (byte) ((window & 0xFF00) >> 8);
        packet[15] = (byte) (window & 0xFF);

        packet[16] = 0;
        packet[17] = 0;
        packet[18] = 0;
        packet[19] = 0;

        System.arraycopy(data, 0, packet, 20, data.length);
        System.arraycopy(checksum(), 0, packet, 16, 2);
    }

    public byte[] checksum() {
        byte[] psuedoheader = new byte[40];
        System.arraycopy(IPv6.myIP, 0, psuedoheader, 0, IPv6.myIP.length);
        System.arraycopy(IPv6.remoteIP, 0, psuedoheader, 16, IPv6.remoteIP.length);

        psuedoheader[32] = (byte) ((packet.length & 0xFF000000) >> 24);
        psuedoheader[33] = (byte) ((packet.length & 0xFF0000) >> 16);
        psuedoheader[34] = (byte) ((packet.length & 0xFF00) >> 8);
        psuedoheader[35] = (byte) ((packet.length & 0xFF));

        psuedoheader[39] = (byte)MyTcpHandler.VERSION;

        byte[] temp = new byte[psuedoheader.length + packet.length];
        System.arraycopy(psuedoheader, 0, temp, 0, psuedoheader.length);
        System.arraycopy(packet, 0, temp, psuedoheader.length, packet.length);
        long result = 0;
        for (int i = 0; i < temp.length; i += 2) {
            long data;
            if (i+1 >= temp.length) {
                data = ((temp[i] << 8) & 0xFF00);
            } else {
                data = (((temp[i] << 8) & 0xFF00) |  (temp[i+1] & 0xFF));
            }
            result += data;
            if ((result & 0xFFFF0000) > 0) {
                result &= 0xFFFF;
                result++;
            }
        }
        result = ~result;
        result = result & 0xFFFF;
        return new byte[]{(byte) ((result & 0xFF00) >> 8), (byte) (result & 0xFF)};
    }
}
