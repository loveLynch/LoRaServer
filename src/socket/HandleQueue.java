package socket;

import java.lang.reflect.Constructor;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import base64.base64__;
import jsonform.DownInfoB_LoRa;
import jsonform.DownInfoForm;
import jsonform.UpInfoForm;
import mac.MacPktForm;
import mac.OperateMacPkt;
import main.LoRaMain;
import phy.PhyConstruct;
import util.ByteArrayandInt;
import util.ClassMod;

public class HandleQueue implements Runnable {

    public List<String> OPMACLISTNAME = new ArrayList<String>();
    public List<String> JSONFORMLIST = new ArrayList<String>();

    @Override
    public void run() {

        OPMACLISTNAME.add("mac.OperateMacJoinRequest");
        OPMACLISTNAME.add("mac.OperateMacJoinAccept");
        OPMACLISTNAME.add("mac.OperateMacUnconfirmedDataUp");
        OPMACLISTNAME.add("mac.OperateMacConfirmedDataUp");
        OPMACLISTNAME.add("mac.OperateMacUnconfirmedDataDown");
        OPMACLISTNAME.add("mac.OperateMacConfirmedDataDown");
        OPMACLISTNAME.add("mac.OperateMacRFUn");
        OPMACLISTNAME.add("mac.OperateMacProprietary");

        JSONFORMLIST.add("jsonform.DownInfoA_LoRa");
        JSONFORMLIST.add("jsonform.DownInfoB_LoRa");
        JSONFORMLIST.add("jsonform.DownInfoC_LoRa");
        JSONFORMLIST.add("jsonform.DownInfoA_FSK");
        JSONFORMLIST.add("jsonform.DownInfoB_FSK");
        JSONFORMLIST.add("jsonform.DownInfoC_FSK");

        // 把 upQueue 中的 data(mac层数据)数据解析
        // 并构造含回送 data 数据的 InfoForm, 存到 downQueue）
        UpInfoForm upInfo;
        DownInfoForm DownInfo = null;
        byte[] upMacData;        // 解 base64 但未解密的 phy 数据
        byte[] downMacData;        // 加密但未 base64 编码
        MacPktForm macPktFormUp;
        OperateMacPkt upOperateMacPkt;
        byte upMhdr;


        while (true) {
            // TODO 是否能取到
            synchronized (LoRaMain.queueUp) {
                upInfo = LoRaMain.queueUp.pull();
            }

            if (upInfo == null)
                continue;
            // data : MAC
            upMacData = upInfo.getData();    // 解 base64 后的
            if (upMacData == null) {
                break;
            }
            upMhdr = upMacData[0];
            try {
                // 通过反射创建不同的对象,用于不同的 Mtype 类型
                Class<?> cls = Class.forName(OPMACLISTNAME.get(((upMhdr & 0xff) >> 5)));
                Constructor<?> ctr = cls.getConstructor();
                upOperateMacPkt = (OperateMacPkt) ctr.newInstance();


                String sysInfo = upInfo.getSysInfo();
                // 6 种不同类型的 Mtype 会调用不同的 解析和构造 操作
                // MacParseData: 先将 byte[] 解密后解析并生成 macPktFormUp 对象.
                // macPktFormUp 含 mac 层各数据的对象
                /*
                 * 存 用户信息和系统信息，问题是终端节点devEui 和 devaddr 的映射
                 */
                macPktFormUp = upOperateMacPkt.MacParseData(upMacData, "AA555A0000000000", sysInfo);
                System.out.println("rrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrr");
                if (macPktFormUp == null) {
                    continue;
                }
                /**
                 * 构造 phy 层数据, 即 downMacData
                 * MacConstructData(macPktFormUp):
                 * 		先构造回复的对象 macPktFormDown
                 * 		accept 帧不加密, 数据帧需要加密 frame 部分
                 *
                 * 再通过 PhyPkt2Byte() 函数, 将其转为 byte[]
                 * 		accept 帧的加密操作在 Phypkt2byte() 中完成，便于 MIC 的生成
                 *
                 * 非确认帧的 downMacData 应该为 null
                 */
                MacPktForm macPktFormDown = upOperateMacPkt.MacConstructData(macPktFormUp);
                System.out.println("oooooooooooooooooooooooooo");
                if (macPktFormDown == null) {
                    continue;
                }
                downMacData = PhyConstruct.PhyPkt2Byte(macPktFormDown, ((upMhdr & 0xff) >> 5));
                System.out.print("send to End-Node: ");
                base64__.myprintHex(downMacData);
                if (downMacData == null) {
                    continue;
                }

                /**
                 * 构造回送的 DownInfoForm, 并加到 downQueue 中
                 * 如何调用不同的 ConstructDownInfo 以构造出不同类型的 DownInfo
                 * 		有没有能够识别是 class A\B\C 的字段
                 * 		需要根据 downMacData 是否为 null , 判断是否需要构造 downInfo
                 */
                int downjsonformType = 0; // 0: A_LoRa 1:B_LoRa 2:C_LoRa 3:A_FSK 4:B_FSK 5:C_FSK
                if (upInfo.getModu().equals("LORA")) {
                    downjsonformType = macPktFormUp.getEndType() + downjsonformType;
                } else {
                    downjsonformType = macPktFormUp.getEndType() + 3;
                }
                System.out.println("协议模式：" + LoRaMain.classMod);
                Class<?> clsInfo = Class.forName(JSONFORMLIST.get(downjsonformType));// TODO A/B/C
                Constructor<?> ctrInfo = clsInfo.getConstructor();
                DownInfo = (DownInfoForm) ctrInfo.newInstance();
                //CLass B数据下发
//                if (LoRaMain.classMod == ClassMod.Class_B) {
//                    long millBeacon_time = (long) (ByteArrayandInt.byteArrayToInt(LoRaMain.beacon_time)) * 1000;
//                    System.out.println(millBeacon_time);
//                    System.out.println("数据：" + millBeacon_time + "   " + LoRaMain.pingOffset + "   " + LoRaMain.beacon_reserved + "     " + LoRaMain.periodicity);
//                    int number = LoRaMain.beacon_period / 32;
//                    for (int i = 0; i < number; i++) {
//                        DownInfoB_LoRa down = new DownInfoB_LoRa();
//                        double time = millBeacon_time + (LoRaMain.pingOffset + i * LoRaMain.ping_period) * LoRaMain.slotLen + LoRaMain.beacon_reserved;
//                        BigDecimal bigDecimal = new BigDecimal(time);
//                        String strtime = bigDecimal.toPlainString();
//                        System.out.println(strtime);
//                        double finaltime = Double.parseDouble(strtime);
//                        System.out.println("time:" + finaltime);
//                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS");
//                        double cha = (double) 315964800 * 1000;
//                        System.out.println(finaltime + cha);
//                        String sd = sdf.format(new Date((long) (finaltime + cha)));   // 时间戳转换成时间
//                        System.out.println(sd);
//                        down.setImme(true);
//                        down.setTmms(finaltime);
//                        synchronized (LoRaMain.queueDown) {
//                            LoRaMain.queueDown.push(down.ConstructDownInfo(upInfo, downMacData, ((upMhdr & 0xff) >> 5)));
//                        }
//                    }
//                } else {

                synchronized (LoRaMain.queueDown) {
                    LoRaMain.queueDown.push(DownInfo.ConstructDownInfo(upInfo, downMacData, ((upMhdr & 0xff) >> 5)));
                    System.out.println("PUSH-------------Scucsacas");
                }
                // }


            } catch (Exception e) {
                e.printStackTrace();
            }
        }    // end while


    }

}
