import org.jsoup.Jsoup;
import org.jsoup.nodes.*;

import javax.mail.*;
import java.io.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.*;

/**
 * Created by Mengqi on 2014/8/12 0012.
 */

public class grabYouhui {
    private String _useragent = "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.31 (KHTML, like Gecko) Chrome/26.0.1410.64 Safari/537.31";
    private String _selectTitle = "h2.itemName>a";

    private HashMap<String, String> _titleWithUrls; //保存所有关键字相关优惠信息标题和链接

    private String _keyword;             //关注的关键词
    private String _s00;

    public grabYouhui(String keyword) {
        _titleWithUrls = new HashMap<String, String>();

        _keyword = keyword;
    }

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
//            e.printStackTrace();
            System.err.println("保存文件失败！");
            return false;
        }
        return true;
    }
    public boolean getAllTitles() {
        String lastTitle = readLastTitle();

        Document doc = null;
        int pageNum = 0;
        while(true) {
            pageNum++;
            try {
                doc = Jsoup.connect("http://www.smzdm.com/youhui/p" + pageNum).userAgent(_useragent).timeout(20 * 1000).get();
                System.out.println("正在抓取第"+pageNum+"页");
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

    public void mailToMe() {
        getPassword();
        //创建JavaMail需要用到的属性类：
        Properties props = new Properties();
        //设置smtp服务器
        props.put("mail.smtp.host", "smtp.163.com");
        //设置验证为true
        props.put("mail.smtp.auth", "true");

        //创建验证器
        Authenticator authenticator = new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication("mailme_mengqi@163.com",_s00);
            }
        };

        Session session = Session.getDefaultInstance(props, authenticator);
        MimeMessage mimeMessage = new MimeMessage(session);

        try {
            mimeMessage.setFrom(new InternetAddress("mailme_mengqi@163.com"));               //设置发送者
        } catch (MessagingException e) {
            System.err.println("发送人设置失败");
            return;
        }
        try {
            mimeMessage.setRecipients(Message.RecipientType.TO, "mengqipei@qq.com");    //设置收件人邮箱
        } catch (MessagingException e) {
            System.err.println("设置收件人邮箱失败");
            return;
        }

        try {
            mimeMessage.setSubject("新的优惠信息！");      //设置标题
            mimeMessage.setSentDate(new Date());           //设置发送时间
            String content = "关于关键字 "+_keyword+" ，发现了新的优惠信息：\n";
            content += "======================================\n";
            Iterator iter = _titleWithUrls.entrySet().iterator();
            while (iter.hasNext()) {
                Map.Entry entry = (Map.Entry) iter.next();
                content += (entry.getKey()+"\n");
                content += (entry.getValue()+"\n");
                content += "-----------------------------------\n";
            }
            content += "======================================\n";
            mimeMessage.setText(content);
            mimeMessage.saveChanges();
        } catch (MessagingException e) {
            System.err.println("邮件创建失败！");
            return;
        }
        try {
            Transport.send(mimeMessage);
        } catch (MessagingException e) {
            System.err.println("邮件发送失败！");
            return;
        }
        System.out.println("邮件已发送！");
    }

    private void getPassword() {
        File file = new File("00.txt");
        FileReader fileReader = null;
        BufferedReader bufferedReader = null;
        try {
            fileReader = new FileReader(file);
            bufferedReader = new BufferedReader(fileReader);
            _s00 = bufferedReader.readLine();

            bufferedReader.close();
            fileReader.close();
        } catch (FileNotFoundException e) {
            System.err.println("文件'00.txt'未找到！");
            return;
        } catch (IOException e) {
            System.err.println("读文件失败！");
            return;
        }
        return;
    }

    public static void main(String[] args) {
        grabYouhui gy = new grabYouhui("AKG");
        if (gy.getAllTitles()) {
            gy.listAllTitles();
            gy.mailToMe();
        } else {
            System.out.println("未发现新的优惠信息！");
        }

    }
}
