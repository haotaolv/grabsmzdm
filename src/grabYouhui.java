import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.*;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by Mengqi on 2014/8/12 0012.
 */

public class grabYouhui {
    private String _useragent = "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.31 (KHTML, like Gecko) Chrome/26.0.1410.64 Safari/537.31";
    private String _selectTitle = "h2.itemName>a";

    private HashMap<String, String> _titleWithUrls; //保存所有关键字相关优惠信息标题和链接

    private String _keyword;             //关注的关键词
    private String _s00;

    /*****************************
     * 构造函数
     * @param keyword 抓取关键词
     *****************************/
    public grabYouhui(String keyword) {
        _titleWithUrls = new HashMap<String, String>();
        _keyword = keyword;
    }

    /*****************************
     * 读取最后一次抓取的标题以确定这次抓取的终点
     * @return 最后一次抓取的标题
     *****************************/
    private String readLastTitle() {
        File file = new File("lastTitle.txt");
        FileReader fileReader = null;
        BufferedReader bufferedReader = null;
        String title = "";
        try {

            fileReader = new FileReader(file);
            bufferedReader = new BufferedReader(fileReader);
            title = bufferedReader.readLine();
            fileReader.close();
            bufferedReader.close();

        } catch (FileNotFoundException e) {
            System.err.println("文件'lastTitle.txt'不存在！");
        } catch (IOException e) {
            System.err.println("读取文件失败！");
        }
        return title;
    }

    /***********************************
     * 保存抓取的第一条标题作为下次抓取的终点
     * @param title 要保存的标题
     * @return 保存是否成功
     **********************************/
    private boolean saveLastTitle(String title) {
        File file = new File("lastTitle.txt");
        FileWriter fileWriter = null;
        BufferedWriter bufferedWriter = null;
        try {
            fileWriter = new FileWriter(file);
            bufferedWriter = new BufferedWriter(fileWriter);

            bufferedWriter.write(title);

            bufferedWriter.close();
            fileWriter.close();
        } catch (IOException e) {
            System.err.println("保存文件失败！");
            return false;
        }
        return true;
    }

    /************************************
     * 获取什么值得买-优惠页面的标题
     * @return 是否存在与关键词相关的标题
     ***********************************/
    public boolean getAllTitles() {
        String lastTitle = readLastTitle();

        Document doc = null;
        int pageNum = 0;
        while (true) {
            pageNum++;
            try {
                doc = Jsoup.connect("http://www.smzdm.com/youhui/p" + pageNum).userAgent(_useragent).timeout(20 * 1000).get();
                System.out.println("正在抓取第" + pageNum + "页");
            } catch (Exception e) {
                e.printStackTrace();
            }
            //第一条优惠信息，保存作为下次抓取时循环的终止条件
            saveLastTitle(doc.select(_selectTitle).first().text());
            if (doc != null) {
                for (Element element : doc.select(_selectTitle)) {
                    //终止条件
                    if (element.text().equals(lastTitle)) {
                        System.out.println("已达到上次抓取进度");
                        return !_titleWithUrls.isEmpty();
                    }

                    if (element.text().toLowerCase().contains(_keyword.toLowerCase())) {
                        System.out.println("发现新优惠信息：" + element.text() + "\n" + element.attr("href"));
                        _titleWithUrls.put(element.text(), element.attr("href"));
                    }
                }
            } else {
                System.err.println("网页未抓取成功，正在重试...");
                pageNum--;
                continue;
            }
        }
    }

    /***********************************
     * 打印所有热点标题
     **********************************/
    public void listAllTitles() {
        System.out.println("======================================");
        System.out.println("您关注的优惠信息：");
        Iterator iter = _titleWithUrls.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry entry = (Map.Entry) iter.next();
            System.out.println(entry.getKey());
            System.out.println(entry.getValue());
            System.out.println("-----------------------------------");
        }
        System.out.println("======================================");
    }

    /*************************************
     * 将热点标题发送邮件
     *************************************/
    public void mailToMe() {
        //获取收件人密码
        getPassword();
        //发送邮件的主题
        String title = "新的优惠信息！";
        //发送邮件的内容
        String content = "关于关键字 " + _keyword + " ，发现了新的优惠信息：\n";
        content += "======================================\n";
        Iterator iter = _titleWithUrls.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry entry = (Map.Entry) iter.next();
            content += (entry.getKey() + "\n");
            content += (entry.getValue() + "\n");
            content += "-----------------------------------\n";
        }
        content += "======================================\n";

        SendEMail.sendmail("mailme_mengqi@163.com", "smtp.163.com", _s00, "mengqipei@qq.com",
                title, content, null, "", "utf-8");
    }

    /************************************
     * 读取邮箱密码
     ************************************/
    private void getPassword() {
        File file = new File("00.data");
        FileReader fileReader = null;
        BufferedReader bufferedReader = null;
        try {
            fileReader = new FileReader(file);
            bufferedReader = new BufferedReader(fileReader);
            _s00 = bufferedReader.readLine();

            bufferedReader.close();
            fileReader.close();
        } catch (FileNotFoundException e) {
            System.err.println("文件'00.data'未找到！");
            return;
        } catch (IOException e) {
            System.err.println("读文件失败！");
            return;
        }
    }

    public static void main(String[] args) {
        grabYouhui gy = new grabYouhui("kindle");
        if (gy.getAllTitles()) {
            gy.listAllTitles();
            gy.mailToMe();
        } else {
            System.out.println("未发现新的优惠信息！");
        }
    }
}
