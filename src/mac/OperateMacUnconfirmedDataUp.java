package mac;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import javax.xml.bind.DatatypeConverter;

import aes.LoRaMacCrypto;
import base64.base64__;
import main.LoRaMain;
import util.*;

import static util.RedisDB.jedisPool;

public class OperateMacUnconfirmedDataUp implements OperateMacPkt {

    @Override
    public MacPktForm MacParseData(byte[] data, String gatawayId, String syscontent) {

        // System.out.println("======hello lora======");
        System.out.print("received from End-Node:");
        base64__.myprintHex(data);
        MacUnconfirmedDataUpForm macunconfirmeddataup = new MacUnconfirmedDataUpForm();

        System.arraycopy(data, 1, macunconfirmeddataup.DevAddr, 0, 4);
        System.out.print("devAddr:");
        base64__.myprintHex(macunconfirmeddataup.DevAddr);

        RedisDB redisDB = new RedisDB();
        redisDB.setup();
        byte[] nwk1 = {0x2b, 0x7e, 0x15, 0x16, 0x28, (byte) 0xae, (byte) 0xd2, (byte) 0xa6, (byte) 0xab, (byte) 0xf7, 0x15, (byte) 0x88, 0x09, (byte) 0xcf, 0x4f, 0x3c};
        redisDB.jedisPool.getResource().set(base64__.toHex(macunconfirmeddataup.DevAddr) + "NwkSKey", ObjectToString.byteToStr(nwk1));
        redisDB.jedisPool.getResource().set(base64__.toHex(macunconfirmeddataup.DevAddr) + "AppSKey", ObjectToString.byteToStr(nwk1));

        macunconfirmeddataup.fctrl.setFctrl(data[5]);
        System.out.print("fctrl:");
        System.out.println(data[5]);

        System.arraycopy(data, 6, macunconfirmeddataup.Fcnt, 0, 2);
        System.out.print("Fcnt:");
        base64__.myprintHex(macunconfirmeddataup.Fcnt);

        int foptlen = macunconfirmeddataup.fctrl.FOptslen;
        macunconfirmeddataup.Fopts = new byte[foptlen];
        System.arraycopy(data, 8, macunconfirmeddataup.Fopts, 0, foptlen);
        System.out.print("Fopts:");
        base64__.myprintHex(macunconfirmeddataup.Fopts);

        System.arraycopy(data, 8 + foptlen, macunconfirmeddataup.Fport, 0, 1);
        System.out.print("Fport:");
        base64__.myprintHex(macunconfirmeddataup.Fport);
        int framelen;
        framelen = data.length - (13 + foptlen);
        if (macunconfirmeddataup.fctrl.RFU == 1)
            framelen = data.length - (15 + foptlen);
        macunconfirmeddataup.FRMPayload = new byte[framelen];
        System.arraycopy(data, 8 + foptlen + 1, macunconfirmeddataup.FRMPayload, 0, framelen);
        // TODO 测试 MIC
        // byte[] datain = Arrays.copyOfRange(data, 0, 8);
        // byte[] micComputed = LoRaMacCrypto.LoRaMacComputeMic(
        // datain, datain.length, LoRaMacCrypto.APPKEY,
        // macunconfirmeddataup.DevAddr, (byte)0x00, macunconfirmeddataup.Fcnt);
        // byte[] micReceived = new byte[4];
        // System.arraycopy(data, 8, micReceived, 0, 4);
        // if(!ObjectToString.byteToStr(micComputed).equals(ObjectToString.byteToStr(micReceived)))
        // return null;

        // 对于 FRMPayload 要进行解密
        byte[] fcnt = new byte[4];
        fcnt[2] = 0x00;
        fcnt[3] = 0x00;
        System.out.println(ObjectToString.byteToStrWith(macunconfirmeddataup.getDevAddr()) + ":AppSKey");
        byte[] appSKey = DatatypeConverter.parseHexBinary(jedisPool.getResource()
                .get(ObjectToString.byteToStrWith(macunconfirmeddataup.getDevAddr()) + ":AppSKey"));
        System.arraycopy(macunconfirmeddataup.Fcnt, 0, fcnt, 0, 2);
        macunconfirmeddataup.FRMPayload = LoRaMacCrypto.LoRaMacPayloadDecrypt(macunconfirmeddataup.FRMPayload, framelen,
                appSKey, macunconfirmeddataup.DevAddr, (byte) 0x00, fcnt);// TODO
        // fcnt
        // 需要
        // 4
        // 字节的，这里是
        // 2
        // 字节的,
        // 加密秘钥也需要变动,
        // 应为
        // AppSKey

        System.out.print("FRMPayload:");
        base64__.myprintHex(macunconfirmeddataup.FRMPayload);
        HextoString.convertHexToString(base64__.recvtohex(macunconfirmeddataup.FRMPayload));

        // TODO 先测试，，，，，暂时不存 redis 数据库
        // Date currentTime = new Date();
        // SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        // String dateString = formatter.format(currentTime);
        //
        // String userInfo = null;
        // String nodeid = null;
        // RedisDB.operateUp(gatawayId, nodeid, dateString, userInfo,
        // syscontent);

        // Timer timer = new Timer();
        // timer.schedule(new printpingoffset(), new Date(), 128000);


        String nodeid = "";
        byte[] buffer = macunconfirmeddataup.DevAddr;
        for (int i = 0; i < buffer.length; i++) {
            String hex = Integer.toHexString(buffer[i] & 0xFF);
            if (hex.length() == 1)
                hex = '0' + hex;
            if ((i != buffer.length - 1))
                hex = hex + ":";
            nodeid = nodeid + hex;
        }
        System.out.println("nodeid:" + nodeid);
        Date currentTime = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHH:mm:ss");
        String dateString = formatter.format(currentTime);
        float temperatur = (float) (18.0 + 0.5 * Math.random());
        String userInfo1 = "{\"lati\":30," + "\"long\":103.55601,\"alti\":531," + "\"temperature\":" + temperatur + ","
                + "\"gatewayID\":\"AA555A0000000000\"}";
        String userInfo = "{\"lati\":30.75401,"
                + "\"long\":103.92783,\"alti\":531," + "\"temperature\":17," + "\"gatewayID\":\"AA555A0000000000\"}";
        redisDB.operateRequest(gatawayId, nodeid, dateString, syscontent);
        redisDB.operateUp(gatawayId, nodeid, dateString, userInfo, syscontent);

        return macunconfirmeddataup;
    }

    /**
     * 返回 MacUnconfirmedDataDownForm, 其中的 FRMPayload 已加密
     */
    @Override
    public MacPktForm MacConstructData(MacPktForm macpkt) {
        /**
         * 应用层数据
         */
        byte[] frmPayload = {0x0a, 0x0b, 0x0c, 0x0d};

        MacUnconfirmedDataUpForm macunconfirmeddataup = (MacUnconfirmedDataUpForm) macpkt;
        MacUnconfirmedDataDownForm macunconfirmeddatadown = new MacUnconfirmedDataDownForm();
        macunconfirmeddatadown.DevAddr = macunconfirmeddataup.DevAddr;

        macunconfirmeddatadown.fctrl.ADR = 1;
        //macunconfirmeddataup.fctrl.RFU = 0; //强制转换协议0为Class A或C,1为Class B
        // System.out.println(macunconfirmeddataup.fctrl.RFU);
        if (macunconfirmeddataup.fctrl.RFU == 1) {
            LoRaMain.classMod = ClassMod.Class_B;
        }
        macunconfirmeddatadown.fctrl.ACK = 0;
        macunconfirmeddatadown.fctrl.FPending = 0;
        if (LoRaMain.classMod == ClassMod.Class_B) {
            /*
             * 计算pingoffset
             * */
            //beacon receive
            CalPingoffset calPingoffset = new CalPingoffset();
            calPingoffset.calpingoffset(macunconfirmeddataup.DevAddr);
        }
        if (macunconfirmeddataup.Fopts.length != 0) {
            switch (macunconfirmeddataup.Fopts[0]) {
                // case LoRaMain.BeaconTimingReq:
                // //构造 BeaconTimingAns
                // macunconfirmeddatadown.fctrl.FOptslen = 4;
                // macunconfirmeddatadown.Fopts = new
                // byte[macunconfirmeddatadown.fctrl.FOptslen];
                // macunconfirmeddatadown.Fopts[0] = 0x12;
                // long RTime = LoRaMain.beacon_time + 128000 -
                // System.currentTimeMillis();
                // int delay = (int) (RTime / 30) ;
                //
                // BeaconTimingAns ans = new BeaconTimingAns();
                //
                // byte channel[] = {0x00};
                // byte beacon_delay[] = new byte[2];
                // beacon_delay[0] = (byte) (delay & 0x00ff) ;
                // beacon_delay[1] = (byte) (delay & 0xff00 >> 8);
                //
                // ans.setDelay(beacon_delay);
                // ans.setChannel(channel);
                // System.arraycopy(ans.getDelay(), 0, macunconfirmeddataup.Fopts,
                // 1, 3);
                // System.arraycopy(ans.getChannel(), 0, macunconfirmeddataup.Fopts,
                // 3, 4);
                // break;
                case LoRaMain.DeviceTimeReq:
                    System.out.println("DeviceTimeReq");
                    macunconfirmeddatadown.fctrl.FOptslen = 6;
                    macunconfirmeddatadown.Fopts = new byte[macunconfirmeddatadown.fctrl.FOptslen];
                    macunconfirmeddatadown.Fopts[0] = LoRaMain.DeviceTimeReq;
                    int SecondSinceEpoch = (int) (System.currentTimeMillis() / 1000 - 315964800);
                    macunconfirmeddatadown.Fopts[1] = (byte) (SecondSinceEpoch & 0xFF);
                    macunconfirmeddatadown.Fopts[2] = (byte) ((SecondSinceEpoch >> 8) & 0xFF);
                    macunconfirmeddatadown.Fopts[3] = (byte) ((SecondSinceEpoch >> 16) & 0xFF);
                    macunconfirmeddatadown.Fopts[4] = (byte) ((SecondSinceEpoch >> 24) & 0xFF);
                    long steps = (System.currentTimeMillis() % 1000) * 256 / 1000;
                    macunconfirmeddatadown.Fopts[5] = (byte) (steps & 0xFF);
                    break;
                case LoRaMain.pingSlotInfoReq:
                    // 构造 pingSoltInfoAns
                    System.out.println("pingSlotInfoReq");
                    macunconfirmeddatadown.fctrl.FOptslen = 2;
                    macunconfirmeddatadown.Fopts = new byte[macunconfirmeddatadown.fctrl.FOptslen];
                    macunconfirmeddatadown.Fopts[0] = LoRaMain.pingSlotInfoReq;
                    int DataRate = macunconfirmeddataup.Fopts[1] & 0x0f;
                    int Perioditity = (macunconfirmeddataup.Fopts[1] & 0x70) >> 4;
                    LoRaMain.periodicity = Perioditity;
                    System.out.println("Perioditity:" + Perioditity);
                    int RFU = (macunconfirmeddataup.Fopts[0] & 0x80) >> 7;
                    macunconfirmeddatadown.Fopts[1] = (byte) (2 << (5 + Perioditity));
                    break;
                case LoRaMain.PingSlotChannelReq:
                    break;
                case LoRaMain.DeviceModeInd:
                    System.out.println("DeviceModeInd");
                    macunconfirmeddatadown.Fopts = new byte[2];
                    macunconfirmeddatadown.Fopts[0] = LoRaMain.DeviceModeInd;
                    macunconfirmeddatadown.Fopts[1] = macunconfirmeddataup.Fopts[1];
                    switch (macunconfirmeddataup.Fopts[1]) {
                        case 0x00:
                            LoRaMain.classMod = ClassMod.Class_A;
                            break;
                        case 0x02:
                            LoRaMain.classMod = ClassMod.Class_C;
                            break;
                        default:
                    }
                    break;
                case LoRaMain.LinkCheckReq:
                    System.out.println("LinkCheckReq");
                    macunconfirmeddatadown.Fopts = new byte[3];
                    macunconfirmeddatadown.Fopts[0] = LoRaMain.LinkCheckReq;
                    macunconfirmeddatadown.Fopts[1] = macunconfirmeddataup.Fopts[1];
                    macunconfirmeddatadown.Fopts[2] = macunconfirmeddataup.Fopts[2];
                    break;
                case LoRaMain.LinkADRReq:
                    break;
                case LoRaMain.RXParamSetupReq:
                    break;
                case LoRaMain.DutyCycleReq:
                    System.out.println("DutyCycleReq");
                    byte MaxDCycle = macunconfirmeddataup.Fopts[0];
                    byte AggregatedDCycle = (byte) (1 << MaxDCycle);
                    macunconfirmeddatadown.Fopts[0] = (byte) (AggregatedDCycle & 0xFF);
                case LoRaMain.DevStatusReq:
                    System.out.println("DevStatusReq");
                    macunconfirmeddatadown.Fopts = new byte[2];
                    macunconfirmeddatadown.Fopts[0] = macunconfirmeddataup.Fopts[0];
                    switch (macunconfirmeddataup.Fopts[0]) {
                        case 0x00:
                            System.out.println("The end-device is connnected to an external power source.");
                            break;
                        case (byte) 0xff:
                            System.out.println("The end-device was not able to measure the batter level.");
                            break;
                        default:
                            System.out.println("The battery level is " + macunconfirmeddataup.Fopts[0]);
                            break;

                    }
                    // Margin是之前成功收到的DevStatusReq命令的解调信噪比进行四舍五入的值，它是一个有符号6bit整数，最小值为-32，最大值31.
                    // int margin = (int)(Math.random()*(31-(-32))+1);
                    // macunconfirmeddatadown.Fopts[1] = (byte) (margin & 0xFF);
                    macunconfirmeddatadown.Fopts[1] = macunconfirmeddataup.Fopts[1];

                    break;
                default:
                    macunconfirmeddatadown.fctrl.FOptslen = 4;
                    macunconfirmeddatadown.Fopts = new byte[macunconfirmeddatadown.fctrl.FOptslen];
                    macunconfirmeddatadown.Fopts = macunconfirmeddataup.DevAddr;

                    break;
            }
        }

        macunconfirmeddatadown.Fcnt = macunconfirmeddataup.Fcnt;
        // macunconfirmeddatadown.Fopts = new
        // byte[macunconfirmeddatadown.fctrl.FOptslen];
        // macunconfirmeddatadown.Fopts = macunconfirmeddataup.DevAddr;
        macunconfirmeddatadown.Fport = macunconfirmeddataup.Fport;
        try {
            String order = new String();
            if ((order = LoRaMain.OrderQueue.poll(10, TimeUnit.NANOSECONDS)) != null) {
                if (order.equals("classC")) {
                    System.out.println("CCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCC");
                    System.out.println("CCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCC");
                    System.out.println("CCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCC");
                    System.out.println("CCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCC");

                    frmPayload[0] = 0x0a;
                    frmPayload[1] = 0x0b;
                    frmPayload[2] = 0x0c;
                    frmPayload[3] = 0x0d;
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        macunconfirmeddatadown.FRMPayload = new byte[frmPayload.length];

        // 对 FRMPayload 进行加密
        byte[] fcnt = new byte[4];

        fcnt[2] = 0x00;
        fcnt[3] = 0x00;
        byte[] appSKey = DatatypeConverter.parseHexBinary(jedisPool.getResource()
                .get(ObjectToString.byteToStrWith(macunconfirmeddatadown.getDevAddr()) + ":AppSKey"));
        System.out.print("AppSKey:");
        base64__.myprintHex(appSKey);
        System.arraycopy(macunconfirmeddataup.Fcnt, 0, fcnt, 0, 2);
        macunconfirmeddatadown.FRMPayload = LoRaMacCrypto.LoRaMacPayloadEncrypt(frmPayload, frmPayload.length, appSKey,
                macunconfirmeddatadown.DevAddr, (byte) 0x01, fcnt); // TODO fcnt
        // 需要 4
        // 字节的，这里是 2
        // 字节的,
        // 加密秘钥也需要变动,
        // 应为
        // AppSKey

        return macunconfirmeddatadown;
    }

    public static void main(String[] args) {


    }

    private class printpingoffset extends TimerTask {
        @Override

        public void run() {
            //beacon receive
            System.out.println("----beaconTime----receive---");
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//设置日期格式
            System.out.println(df.format(new Date()));
            int SecondSinceEpoch1 = (int) (System.currentTimeMillis() / 1000 - 315964800);
            int mode = SecondSinceEpoch1 % 128;
            int SecondSinceEpoch2 = SecondSinceEpoch1 - mode;
            //int SecondSinceEpoch1 = 315964800;
            LoRaMain.beacon_time = ByteArrayandInt.intToByteArray(SecondSinceEpoch2);
            byte[] dev = {0x59, (byte) 0xa6, 0x73, 0x01};
            base64__.myprintHex(LoRaMain.beacon_time);
            try {
                byte[] Rank = LoRaMacCrypto.LoRaMacBeaconComputeOffset(LoRaMacCrypto.APPSKEY0, LoRaMain.beacon_time, dev);
                base64__.myprintHex(Rank);
                LoRaMain.pingOffset = (Rank[0] + Rank[1] * 256) % LoRaMain.ping_period;
                System.out.println("receive pingOffset is " + LoRaMain.pingOffset);
                byte[] pingoffset = new byte[2];
                pingoffset[0] = (byte) (LoRaMain.pingOffset & 0xFF);
                pingoffset[1] = (byte) ((LoRaMain.pingOffset >> 8) & 0xFF);
                System.out.println("hex_pingoffset is ");
                base64__.myprintHex(pingoffset);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
