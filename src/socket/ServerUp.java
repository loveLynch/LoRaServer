package socket;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;

import aes.Aes;
import mac.MacUnconfirmedDataUpForm;
import main.Constant;
import main.LoRaMain;
import util.ClassMod;
import util.ParseJson;

public class ServerUp implements Runnable {
    private int port;
    private DatagramSocket dsock;
    public static String recv_info;
    private InetSocketAddress socketAddress = null;
    private static final byte PKT_PUSH_DATA = 0x00;
    private static final byte PKT_PUSH_ACK = 0x01;
    public byte Mtype;
    public static final byte JoinRequest = 0;
    public static final byte JoinAccept = 1;
    public static final byte UnconfiredDataUp = 2;
    public static final byte UnconfiredDataDown = 3;
    public static final byte ConfiredDataUP = 4;
    public static final byte ConfiredDataDown = 5;
    public static final byte RFU = 6;
    public static final byte Proprietary = 7;

    public ServerUp(int port) {
        this.port = port;
        this.socketAddress = new InetSocketAddress(Constant.LOCAL_ADDRESS, this.port);
        try {
            this.dsock = new DatagramSocket(this.socketAddress);
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }

    public ServerUp() {
        this(1780);
    }

    @Override
    public void run() {
        System.out.println("|******************************************************|");
        System.out.println("|********************* hello lora *********************|");
        System.out.println("|******************************************************|");


        while (true) {
            byte[] buffer = new byte[8192];
            DatagramPacket recv_pkt = new DatagramPacket(buffer, buffer.length);
//			System.out.println("********************************");
            try {
                dsock.receive(recv_pkt);
                //String recv_info;
                DatagramPacket send_pkt;
                switch (buffer[3]) {
                    case PKT_PUSH_DATA:
                        recv_info = new String(recv_pkt.getData(), 12, recv_pkt.getLength() - 12);
                        System.out.println("PUSH_DATA" + recv_info);
                        // 先回 ack
                        buffer[3] = PKT_PUSH_ACK;
                        send_pkt = new DatagramPacket(
                                buffer, 4,
                                recv_pkt.getAddress(), recv_pkt.getPort());
                        dsock.send(send_pkt);
                        // 存到 queueUp
                        ParseJson parseJson = new ParseJson();
                        parseJson.parseOfJson(recv_info);
                        break;
                    default:
//	                  System.out.println(", unexpected command");
                }


            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}