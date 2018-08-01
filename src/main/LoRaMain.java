package main;

import queue.QueueBData;
import queue.QueueDown;
import queue.QueueUp;
import socket.*;
import util.ClassMod;

import java.util.concurrent.LinkedBlockingQueue;

public class LoRaMain {

    public static QueueUp queueUp = new QueueUp();
    public static QueueDown queueDown = new QueueDown();
    public static LinkedBlockingQueue<String> OrderQueue = new LinkedBlockingQueue<>();
    public static QueueBData queueBData = new QueueBData();
    public static final String LORAMAC_STATUS_OK = "LoRa Status Ok";
    public static final String LORAMAC_STATUS_BUSY = "LoRa Status Busy";
    public static final byte DeviceTimeReq = 0x0D; //终端设备从网络请求当前网络的日期和时间
    public static final byte DeviceModeInd = 0x20; //终端用来指示当前的操作模式（A或C）

    public static final byte LinkCheckReq = 0x02; //用于终端验证网络连接
    public static final byte LinkADRReq = 0x03; //请求终端改变数据率、传输功率、接收率或者信道
    public static final byte DutyCycleReq = 0x04; //设置设备的最大总发射占空比
    public static final byte RXParamSetupReq = 0x05; //设置接收时隙相关参数
    public static final byte DevStatusReq = 0x06; //请求终端状态,电量和解调情况

    public static final byte pingSlotInfoReq = 0x10; //终端使用，告知服务器ping单播通信的时隙速率和周期
    public static final byte PingSlotChannelReq = 0x11; //服务器使用，设置终端的单播ping通信信道


    public static ClassMod classMod = ClassMod.Class_C;

    public static byte[] beacon_time = null;
    public static int pingNb = 4; //ping slot数量,必须为2^x幂次方，1<=x<=7
    public static int ping_period = (int) (Math.pow(2,12)/pingNb);
    public static int beacon_reserved = 2120;//2.12s=2120ms
    public static float beacon_guard = (float) 3.000;
    //beacon_window=becon_period - beacon_reserved -beacon_guard
    public static int pingOffset = 0;
    public static int periodicity = 32;
    public static int Delay = 0;
    public static int slotLen = 30;   //一个ping slot时长，30ms
    private byte[] Frequence = new byte[3];

    public byte[] getFrequence() {
        return Frequence;
    }

    public void setFrequence(byte[] frequence) {
        Frequence = frequence;
    }


    public static void main(String[] args) {
        new Thread(new ServerUp(1780)).start();
        new Thread(new ServerDown(1782)).start();
        new Thread(new HandleQueue()).start();
        new Thread(new RedisMessageHandle()).start();
    }
}
