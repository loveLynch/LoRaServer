package util;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import base64.base64__;
import jsonform.InfoFSKModEndForm;
import jsonform.InfoGateWayStatForm;
import jsonform.InfoLoraModEndForm;
import main.LoRaMain;

public class ParseJson {
	private String json = "{\"rxpk\":[{\"time\":\"2013-03-31T16:21:17.528002Z\",\"tmst\":3512348611,\"chan\":2,\"rfch\":0,\"freq\":866.349812,\"stat\":1,\"modu\":\"LORA\",\"datr\":\"SF7BW125\",\"codr\":\"4/6\",\"rssi\":-35,\"lsnr\":5.1,\"size\":32,\"data\":\"-DS4CGaDCdG+48eJNM3Vai-zDpsR71Pn9CPA9uCON84\"},{\"time\":\"2013-03-31T16:21:17.530974Z\",\"tmst\":3512348514,\"chan\":9,\"rfch\":1,\"freq\":869.1,\"stat\":1,\"modu\":\"FSK\",\"datr\":50000,\"rssi\":-75,\"size\":16,\"data\":\"VEVTVF9QQUNLRVRfMTIzNA==\"},{\"time\":\"2013-03-31T16:21:17.532038Z\",\"tmst\":3316387610,\"chan\":0,\"rfch\":0,\"freq\":863.00981,\"stat\":1,\"modu\":\"LORA\",\"datr\":\"SF10BW125\",\"codr\":\"4/7\",\"rssi\":-38,\"lsnr\":5.5,\"size\":32,\"data\":\"ysgRl452xNLep9S1NTIg2lomKDxUgn3DJ7DE+b00Ass\"}],\"stat\":{\"time\":\"2014-01-12 08:59:28 GMT\",\"lati\":46.24000,\"long\":3.25230,\"alti\":145,\"rxnb\":2,\"rxok\":2,\"rxfw\":2,\"ackr\":100.0,\"dwnb\":2,\"txnb\":2}}";
	
	//private HashMap<String,Info> Map = new HashMap();
	public String getJson() {
		return json;
	}

	/**
	 * 这里实现 data 部分的 base64 解码
	 * @param jsonstr
	 * @return
	 */
	public void parseOfJson(String jsonstr) {
		try {
			JSONObject json = new JSONObject(jsonstr);
			
			InfoLoraModEndForm loraendpkt = new InfoLoraModEndForm();
			InfoFSKModEndForm fskendpkt = new InfoFSKModEndForm();
			InfoGateWayStatForm gwstat = new InfoGateWayStatForm();
			
			if(!json.isNull("rxpk")){
				JSONArray rxpk_arry = json.getJSONArray("rxpk");
				for(int i = 0; i < rxpk_arry.length(); i ++){
					if(rxpk_arry.getJSONObject(i).getString("modu").equals("LORA")){
//						loraendpkt.setTime(rxpk_arry.getJSONObject(i).getString("time"));
						loraendpkt.setTmst(rxpk_arry.getJSONObject(i).getDouble("tmst"));
						if (!rxpk_arry.getJSONObject(i).isNull("tmms")) {
							loraendpkt.setTmms(rxpk_arry.getJSONObject(i).getInt("tmms"));
						}
						loraendpkt.setChan(rxpk_arry.getJSONObject(i).getInt("chan"));
						loraendpkt.setRfch(rxpk_arry.getJSONObject(i).getInt("rfch"));
						loraendpkt.setFreq((float)rxpk_arry.getJSONObject(i).getDouble("freq"));
						loraendpkt.setStat(rxpk_arry.getJSONObject(i).getInt("stat"));
						loraendpkt.setModu(rxpk_arry.getJSONObject(i).getString("modu"));
						loraendpkt.setDatr_lora(rxpk_arry.getJSONObject(i).getString("datr"));
						loraendpkt.setCodr(rxpk_arry.getJSONObject(i).getString("codr"));
						loraendpkt.setRssi(rxpk_arry.getJSONObject(i).getInt("rssi"));
						loraendpkt.setLsnr(rxpk_arry.getJSONObject(i).getInt("lsnr"));
						loraendpkt.setSize(rxpk_arry.getJSONObject(i).getInt("size"));
						loraendpkt.setData(base64__.decode(rxpk_arry.getJSONObject(i).getString("data")));
						synchronized (LoRaMain.queueUp) {
							LoRaMain.queueUp.push(loraendpkt);
						}
					} else {
						fskendpkt.setTime(rxpk_arry.getJSONObject(i).getString("time"));
						fskendpkt.setTmst(rxpk_arry.getJSONObject(i).getInt("tmst"));
						if (!rxpk_arry.getJSONObject(i).isNull("tmms")) {
							fskendpkt.setTmms(rxpk_arry.getJSONObject(i).getInt("tmms"));
						}
						fskendpkt.setChan(rxpk_arry.getJSONObject(i).getInt("chan"));
						fskendpkt.setRfch(rxpk_arry.getJSONObject(i).getInt("rfch"));
						fskendpkt.setFreq(rxpk_arry.getJSONObject(i).getInt("freq"));
						fskendpkt.setStat(rxpk_arry.getJSONObject(i).getInt("stat"));
						fskendpkt.setModu(rxpk_arry.getJSONObject(i).getString("modu"));
						fskendpkt.setDatr_fsk(rxpk_arry.getJSONObject(i).getInt("datr"));
						fskendpkt.setRssi(rxpk_arry.getJSONObject(i).getInt("rssi"));
						fskendpkt.setSize(rxpk_arry.getJSONObject(i).getInt("size"));
						fskendpkt.setData(base64__.decode(rxpk_arry.getJSONObject(i).getString("data")));
						synchronized (LoRaMain.queueUp) {
							LoRaMain.queueUp.push(fskendpkt);
						}
					}
				}
			}
			if(!json.isNull("stat")) {					
				JSONObject stat = json.getJSONObject("stat");
				gwstat.time = stat.getString("time");
//				gwstat.lati = stat.getInt("lati");
//				gwstat.longe = stat.getInt("long");
//				gwstat.alti = stat.getInt("alti");
				gwstat.rxnb = stat.getInt("rxnb");
				gwstat.rxok = stat.getInt("rxok");
				gwstat.rxfw = stat.getInt("rxfw");
				gwstat.ackr = stat.getInt("ackr");
				gwstat.dwnb = stat.getInt("dwnb");
				gwstat.txnb = stat.getInt("txnb");
				if(!stat.isNull("error")) {
					gwstat.ackr = stat.getInt("ackr");
//					gwstat.alti = stat.getInt("alti");
					gwstat.dwnb = stat.getInt("dwnb");
//					gwstat.lati = stat.getInt("lati");
//					gwstat.longe = stat.getInt("long");
					gwstat.rxfw = stat.getInt("rxfw");
					gwstat.rxnb = stat.getInt("rxnb");
					gwstat.rxok = stat.getInt("rxok");
					gwstat.time = stat.getString("time");
					gwstat.txnb = stat.getInt("txnb");
				}
			}
		}catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
//	// 用于本地测试 json 解析及存到数据库
//	public static void main(String[] args){
//		ParseJson pjs = new ParseJson();
//		ParseJson.parseOfJson(pjs.getJson());
//		ParseJson.parseOfJson(pjs.json);
//		InfoForm info; 
//		Set set = InfoMap.keySet(); 
//	    for(Iterator itr=set.iterator();itr.hasNext();){ 
//	    	String value =(String) itr.next(); 
//	    	info = InfoMap.get(value); 
////	      	info.saveData();
//	    	if(info instanceof InfoFSKModEndForm){
//	    	  System.out.println("fsk");
//	    	}
//	    	if(info instanceof InfoLoraModEndForm){
//		    	  System.out.println("lora");
//		    }
//	     } 
//	    DataBaseAction.SaveData(InfoMap);
//	}

}