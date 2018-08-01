package util.StringHex;

public class ParseByte2HexStr {

	 public static String Byte2HexStr(byte[] buf) {
         StringBuffer sb = new StringBuffer();
         String rstr;
         for (int i = 0; i < buf.length; i++) {
                 String hex = Integer.toHexString(buf[i] & 0xFF);
                 if (hex.length() == 1) {
                         hex = '0' + hex;
                 }
                 sb.append("0x"+hex+",");
         }
         rstr  = sb.toString();
         rstr = rstr.substring(0,rstr.length()-1);
         return rstr;
 }
}
