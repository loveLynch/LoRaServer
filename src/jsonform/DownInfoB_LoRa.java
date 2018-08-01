package jsonform;

import base64.base64__;
import main.LoRaMain;
import util.ByteArrayandInt;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;


public class DownInfoB_LoRa implements DownInfoForm {

    /**
     * class B 的各项 JSON 数据
     */
    private boolean imme;
    private double tmms;
    //private double tmst;
    private boolean ncrc;
    private float freq;
    private int rfch;
    private int powe;
    private String modu;
    private String datr;
    private String codr;
    private boolean ipol;
    private int prea;
    private int size;
    private String data;

    @Override
    public DownInfoForm ConstructDownInfo(UpInfoForm info, byte[] data, int type) {
        InfoLoraModEndForm infoLoraModEndForm = (InfoLoraModEndForm) info;
        DownInfoB_LoRa downInfoB_LoRa = new DownInfoB_LoRa();
        downInfoB_LoRa.setImme(false);
        long millBeacon_time = (long) (ByteArrayandInt.byteArrayToInt(LoRaMain.beacon_time)) * 1000;
        System.out.println(millBeacon_time);
        System.out.println("数据：" + millBeacon_time + "   " + LoRaMain.pingOffset + "   " + LoRaMain.beacon_reserved + "     " + LoRaMain.periodicity);
        for (int i = 0; i < LoRaMain.pingNb; i++) {
            double time = millBeacon_time + (LoRaMain.pingOffset + i * LoRaMain.ping_period) * LoRaMain.slotLen + LoRaMain.beacon_reserved;
            BigDecimal bigDecimal = new BigDecimal(time);
            String strtime = bigDecimal.toPlainString();
            // System.out.println(strtime);
            long finaltime = Long.parseLong(strtime);
            //System.out.println("time:" + finaltime);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS");
            double cha = (double) 315964800 * 1000;
            //System.out.println(finaltime + cha);
            String sd = sdf.format(new Date((long) (finaltime + cha)));   // 时间戳转换成时间
            System.out.println(sd);
            System.out.println("finaltime:" + finaltime);
            //可以和当前时间进行对比，从而抛弃已过时间点
            //将已执行的时间舍弃
            //downInfoB_LoRa.setTmst(infoLoraModEndForm.getTmst());
            if ((finaltime + cha) - System.currentTimeMillis() < 60000) {
                downInfoB_LoRa.setTmms(finaltime);
            }
            //System.out.println(infoLoraModEndForm.getTmst());
            downInfoB_LoRa.setNcrc(false);
            downInfoB_LoRa.setFreq((float) 434.665);
            downInfoB_LoRa.setRfch(0); //下行
            downInfoB_LoRa.setCodr(infoLoraModEndForm.getCodr());
            downInfoB_LoRa.setPowe(0);
            downInfoB_LoRa.setModu("LORA");
            downInfoB_LoRa.setDatr(infoLoraModEndForm.getDatr_lora());
            downInfoB_LoRa.setIpol(false);
            downInfoB_LoRa.setPrea(8);
            downInfoB_LoRa.setSize(infoLoraModEndForm.getSize());
            downInfoB_LoRa.setData(base64__.encode(data));
        }
        return downInfoB_LoRa;
    }

    public boolean isImme() {
        return imme;
    }

    public void setImme(boolean imme) {
        this.imme = imme;
    }

    public double getTmms() {
        return tmms;
    }

    public void setTmms(double tmms) {
        this.tmms = tmms;
    }

    public boolean isNcrc() {
        return ncrc;
    }

    public void setNcrc(boolean ncrc) {
        this.ncrc = ncrc;
    }

    public float getFreq() {
        return freq;
    }

    public void setFreq(float freq) {
        this.freq = freq;
    }

    public int getRfch() {
        return rfch;
    }

    public void setRfch(int rfch) {
        this.rfch = rfch;
    }

    public int getPowe() {
        return powe;
    }

    public void setPowe(int powe) {
        this.powe = powe;
    }

    public String getModu() {
        return modu;
    }

    public void setModu(String modu) {
        this.modu = modu;
    }

    public String getDatr() {
        return datr;
    }

    public void setDatr(String datr) {
        this.datr = datr;
    }

    public String getCodr() {
        return codr;
    }

    public void setCodr(String codr) {
        this.codr = codr;
    }

    public boolean isIpol() {
        return ipol;
    }

    public void setIpol(boolean ipol) {
        this.ipol = ipol;
    }

    public int getPrea() {
        return prea;
    }

    public void setPrea(int prea) {
        this.prea = prea;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

//
//    public double getTmst() {
//        return tmst;
//    }
//
//    public void setTmst(double tmst) {
//        this.tmst = tmst;
//    }
}
