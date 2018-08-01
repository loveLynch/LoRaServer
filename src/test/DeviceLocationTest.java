package test;

import com.mathworks.toolbox.javabuilder.*;
import Location.*;
import util.Point;

public class DeviceLocationTest {


    public static void main(String[] args) {

        MWNumericArray tdoa = null;
        /*网关时差二维数组
            -a[]各组网关坐标及tdoa
            -网关坐标，{x1,y1,x2,y2,tdoa}，{x1,y1,x3,y3,tdoa}...
            -tdoa=接收时差*3*10*e8
        */
        double a[][] = new double[][]{{0, 0, 0, 1000, 10}, {0, 0, 1000, 0, 0}, {0, 0, 1000, 1000, -3}};
        tdoa = new MWNumericArray(a, MWClassID.DOUBLE);
        Object[] result = null;
        Tdoa b;
        try {
            b = new Tdoa();
            //有多种方法可选，还可选择chan(2,tdoa,noise)或taylor(2,tdoa,noise,x0,y0)
            result = b.GA(2, tdoa, 30);
        } catch (MWException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        Point point = new Point("id");//传入终端id


        point.setX(((MWNumericArray) result[0]).getDouble(1));
        point.setY(((MWNumericArray) result[1]).getDouble(1));
        System.out.println(point);
    }
}