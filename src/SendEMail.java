import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.mail.*;
import javax.mail.internet.*;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.Properties;

/**
 * Created by Mengqi on 2014/8/12 0012.
 */
public class SendEMail {
    public static void sendmail(final String sender, final String sendserver, final String password,
                                String receiver, String title, String mailContent,
                                File[] attachements, String mimetype, String charset) {
        //创建JavaMail需要用到的属性类：
        Properties props = new Properties();
        //设置smtp服务器
        props.put("mail.smtp.host", sendserver);
        //设置验证为true
        props.put("mail.smtp.auth", "true");

        //创建验证器
        Authenticator authenticator = new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(sender, password);
            }
        };

        Session session = Session.getDefaultInstance(props, authenticator);
        MimeMessage mimeMessage = new MimeMessage(session);

        try {
            mimeMessage.setFrom(new InternetAddress(sender));               //设置发送者
        } catch (MessagingException e) {
            System.err.println("发送人设置失败");
            return;
        }
        try {
            mimeMessage.setRecipients(Message.RecipientType.TO, receiver);    //设置收件人邮箱
        } catch (MessagingException e) {
            System.err.println("设置收件人邮箱失败");
            return;
        }

        try {
            mimeMessage.setSubject(title);      //设置标题
            mimeMessage.setSentDate(new Date());           //设置发送时间

            //创建附件
            Multipart multipart = new MimeMultipart();
            //创建邮件内容
            MimeBodyPart body = new MimeBodyPart();
            //设置邮件内容
            body.setContent(mailContent, (mimetype!=null && !"".equals(mimetype) ? mimetype : "text/plain")+ ";charset="+ charset);
            multipart.addBodyPart(body);//发件内容
            //设置附件
            if(attachements!=null){
                for (File attachement : attachements) {
                    MimeBodyPart attache = new MimeBodyPart();
                    attache.setDataHandler(new DataHandler(new FileDataSource(attachement)));
                    String fileName = getExtensionName(attachement.getName());
                    attache.setFileName(MimeUtility.encodeText(fileName, charset, null));
                    multipart.addBodyPart(attache);
                }
            }
            //设置邮件内容（使用Multipart方式）
            mimeMessage.setContent(multipart);
        } catch (MessagingException e) {
            System.err.println("邮件创建失败！");
            return;
        } catch (UnsupportedEncodingException e) {
            System.err.println("编码格式不支持");
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

    private static String getExtensionName(String fileName) {
        int pos = fileName.lastIndexOf("\\");
        if (pos > -1) {
            fileName = fileName.substring(pos + 1);
        }
        pos = fileName.lastIndexOf("/");
        if (pos > -1) {
            fileName = fileName.substring(pos + 1);
        }
        return fileName;
    }
}
