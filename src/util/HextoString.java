package util;

/**
 * Created by lynch on 2018/6/5. <br>
 **/
public class HextoString {

    /**
     * @param hex
     * @return
     * 将hex转换为ascll
     */
    public static String convertHexToString(String hex){
        StringBuilder stringBuilder = new StringBuilder();
        StringBuilder temp = new StringBuilder();
        for (int i=0; i <hex.length()-1;i+=2){
            String output  = hex.substring(i,(i+2));
            int decimal = Integer.parseInt(output,16);
            stringBuilder.append((char)decimal);
            temp.append(decimal);

        }
        System.out.println(stringBuilder.toString());
        return stringBuilder.toString() ;
    }
}
