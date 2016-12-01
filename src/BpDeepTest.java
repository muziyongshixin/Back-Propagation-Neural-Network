/**
 * Created by 32706 on 2016/11/29.
 */

import java.io.File;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Scanner;
import java.util.Vector;

public class BpDeepTest {

    public static Vector<String> indata = new Vector<>();  //存储从文件中读取的原始数据
    public static Vector<double[]> data = new Vector<>();//存储预处理和归一化后的训练集

    static double[] max = new double[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
    static double[] min = new double[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
    static double[] weigth = new double[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};


    public static boolean loadData(String url) {//加载测试的数据文件
        try {
            Scanner in = new Scanner(new File(url));//读入文件
            while (in.hasNextLine()) {
                String str = in.nextLine();//将文件的每一行存到str的临时变量中
                indata.add(str);//将每一个样本点的数据追加到Vector 中
            }
            return true;
        } catch (Exception e) { //如果出错返回false
            return false;
        }
    }


    public static void pretreatment(Vector<String> indata) {   //数据预处理，将原始数据中的每一个属性值提取出来并进行归一化存放到Vector<double[]>  data中
        Vector<double[]> temdata = new Vector<>();

        int i = 1;
        String t;
        while (i < indata.size()) {//取出indata中的每一行值
            double[] tem = new double[14];
            t = indata.get(i);
            String[] sourceStrArray = t.split(",", 16);//使用字符串分割函数提取出各属性值

            for (int j = 0; j < 13; j++) {
                tem[j] = Double.parseDouble(sourceStrArray[j + 2]);//将每一个的样本的各属性值类型转换后依次存入到double[]数组中
                if (tem[j] > max[j])
                    max[j] = tem[j];
                if (tem[j] < min[j])
                    min[j] = tem[j];
            }
            switch (sourceStrArray[15]) {
                case "Very High": {
                    tem[13] = 1;
                    break;
                }
                case "High": {
                    tem[13] = 2;
                    break;
                }
                case "Moderate": {
                    tem[13] = 3;
                    break;
                }
                case "Low": {
                    tem[13] = 4;
                    break;
                }
                case "Very low": {
                    tem[13] = 5;
                    break;
                }
                default:
                    break;

            }
            temdata.add(tem);//将每一个样本加入到temdata中
            i++;
        }
        /*******以下部分对数据进行归一化处理**********/
        for (int r = 0; r < max.length; r++) {
            weigth[r] = max[r] - min[r];
        }

        for (int r = 0; r < temdata.size(); r++) {
            double[] t1 = temdata.get(r);
            for (int j = 0; j < t1.length - 1; j++) {
                t1[j] = t1[j] / weigth[j];
            }

            data.add(t1);
        }

    }


    public static String Show_air_quality(double[] result) {//根据结果返回空气质量
        String rt = "";
        int NO = 0;
        double max = 0;
        for (int i = 0; i < result.length; i++) {
            if (result[i] >= max) {
                max = result[i];
                NO = i;
            }

        }
        switch (NO) {
            case 0: {
                rt = "Very high";
                break;
            }
            case 1: {
                rt = "High";
                break;
            }
            case 2: {
                rt = "Moderate";
                break;
            }
            case 3: {
                rt = "Low";
                break;
            }
            case 4: {
                rt = "Very low";
                break;
            }
            default:
                break;
        }

        return rt;
    }


    public static void main(String[] args) {
        long startTime = System.currentTimeMillis();//或得程序开始运行时间

        loadData("AirQualityUCI.data");//载入训练数据
        pretreatment(indata);//预处理数据


        double[][] train_data = new double[data.size()][data.get(0).length - 1];//构建训练样本集
        int r = 0;
        while (r < data.size()) {
            double[] tem = data.get(r);
            for (int j = 0; j < tem.length - 1; j++) {
                train_data[r][j] = tem[j];
            }
            r++;
        }

        double[][] target = new double[data.size()][5];//构建训练样本集的结果集
        r = 0;
        while (r < data.size()) {
            int t = (int) data.get(r)[13];
            switch (t) {
                case 1: {
                    target[r] = new double[]{1.0, 0.0, 0.0, 0.0, 0.0};
                    break;
                }
                case 2: {
                    target[r] = new double[]{0.0, 1.0, 0.0, 0.0, 0.0};
                    break;
                }
                case 3: {
                    target[r] = new double[]{0.0, 0.0, 1.0, 0.0, 0.0};
                    break;
                }
                case 4: {
                    target[r] = new double[]{0.0, 0.0, 0.0, 1.0, 0.0};
                    break;
                }
                case 5: {
                    target[r] = new double[]{0.0, 0.0, 0.0, 0.0, 1.0};
                    break;
                }
                default:
                    break;
            }
            r++;
        }


//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        BP bp1 = new BP(13, 13, 5);//新建一个神经网络

        for (int s = 0; s < 10000; s++) {//循环训练10000次

            for (int i = 0; i < data.size(); i++) {    //训练
                bp1.train(train_data[i], target[i]);
            }

            int correct = 0;
            for (int j = 0; j < data.size(); j++) {   //测试
                double[] result = bp1.test(train_data[j]);
                double max = 0;
                int NO = 0;
                for (int i = 0; i < result.length; i++) {
                    if (result[i] >= max) {
                        max = result[i];
                        NO = i;
                    }

                }
                if (target[j][NO] == 1.0) {
                    correct++;
                }
                else if(s==9999)//输出训练10000次后测试的错误结果
                    System.out.println("第"+(s+1)+"次训练后，第"+j+"号测试用例预测错误--------------");
            }

            double b=(correct * 1.0 / data.size()) * 100;//计算正确率
            DecimalFormat df = new DecimalFormat( "0.00 ");//设置输出精度
            System.out.println("第 " + (s+1) + " 次训练后，使用训练集检测的正确率为==" +df.format(b) + "%");
        }


        double[] x = new double[]{-200,883,-200,1.3,530,63,997,46,1102,617,13.7,68.2,1.0611};
        System.out.print("使用测试用例" + Arrays.toString(x) + "   根据神经网络计算预计空气质量为：");
        for(int i=0;i<x.length;i++)
            x[i]=x[i]/weigth[i];//对数据归一化

        double[] result = bp1.test(x);
        System.out.println(Show_air_quality(result));




        System.out.println("程序运行时间为：" + (System.currentTimeMillis() - startTime) * 1.0 / 1000 + " s");
    }
}