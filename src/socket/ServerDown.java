package socket;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;

import base64.base64__;
import jsonform.DownInfoForm;
import main.Constant;
import main.LoRaMain;
import util.ByteToObject;
import util.ConstructJson;
import util.ObjectToString;

public class ServerDown implements Runnable {
    private int port;
    private DatagramSocket dsock;
    private InetSocketAddress socketAddress = null;
    private static final byte PKT_PULL_DATA = 0x02;
    private static final byte PKT_PULL_RESP = 0x03;
    private static final byte PKT_PULL_ACK = 0x04;
    public byte Mtype;
    public static final byte JoinRequest = 0;
    public static final byte JoinAccept = 1;
    public static final byte UnconfiredDataUp = 2;
    public static final byte UnconfiredDataDown = 3;
    public static final byte ConfiredDataUP = 4;
    public static final byte ConfiredDataDown = 5;
    public static final byte RFU = 6;
    public static final byte Proprietary = 7;

    public ServerDown(int port) {
        this.port = port;
        this.socketAddress = new InetSocketAddress(Constant.LOCAL_ADDRESS, this.port);
        try {
            this.dsock = new DatagramSocket(this.socketAddress);
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }

    public ServerDown() {
        this(1782);
    }

    @Override
    public void run() {

        while (true) {
            byte[] buffer = new byte[8192];
            DatagramPacket recv_pkt = new DatagramPacket(buffer, buffer.length);
//			System.out.println("********************************");
            try {
                dsock.receive(recv_pkt);
                DatagramPacket send_pkt;
                switch (buffer[3]) {
                    case PKT_PULL_DATA:
                        buffer[3] = PKT_PULL_ACK;
                        send_pkt = new DatagramPacket(
                                buffer, 4,
                                recv_pkt.getAddress(), recv_pkt.getPort());
                        dsock.send(send_pkt);

                        DownInfoForm info;
                        byte[] down;
                        buffer[3] = PKT_PULL_RESP;

                        for (int i = 0; i < 8; i++) {
                            synchronized (LoRaMain.queueDown) {
                                info = LoRaMain.queueDown.pull();
                            }
                            if (info == null) {
                                continue;
                            }
                            // 构造 JSON 数据
                            down = (ConstructJson.ToJsonStr(info)).getBytes();
                            byte[] phyDown = new byte[down.length + 4];
                            System.arraycopy(buffer, 0, phyDown, 0, 4);
                            System.arraycopy(down, 0, phyDown, 4, down.length);

                            send_pkt = new DatagramPacket(
                                    phyDown, phyDown.length,
                                    recv_pkt.getAddress(), recv_pkt.getPort());
//						System.out.println("asdasdasdsadasdasd");
//						base64__.myprintHex(phyDown);
                            dsock.send(send_pkt);
                        }

                        break;
                    case PKT_PULL_ACK:
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