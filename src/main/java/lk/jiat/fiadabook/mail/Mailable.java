package lk.jiat.fiadabook.mail;

import io.rocketbase.mail.EmailTemplateBuilder;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import lk.jiat.fiadabook.provider.MailServiceProvider;
import lk.jiat.fiadabook.util.Env;


public abstract class Mailable implements Runnable {

    private final MailServiceProvider mailServiceProvider;
    private final EmailTemplateBuilder.EmailTemplateConfigBuilder emailTemplateConfigBuilder;

    public Mailable() {
        this.mailServiceProvider = MailServiceProvider.getInstance();
        this.emailTemplateConfigBuilder = EmailTemplateBuilder.builder();
    }

    @Override
    public void run() {
        try {
            Session mailInstance = Session.getInstance(mailServiceProvider.getProperties(), mailServiceProvider.getAuthenticator());
            MimeMessage mimeMessage = new MimeMessage(mailInstance);
            mimeMessage.setFrom(new InternetAddress(Env.getProperty("app.mail")));
            build(mimeMessage);


            if(mimeMessage.getRecipients(Message.RecipientType.TO).length > 0){
                Transport.send(mimeMessage);
                System.out.println("\\u001B[32mMail sending successful! ");
            }else{
                throw  new RuntimeException("Email recipients cant be empty...");
            }

        } catch (MessagingException e) {
            throw new RuntimeException(e.getMessage());
        }

    }

    public abstract void build(Message message) throws MessagingException;

    public EmailTemplateBuilder.EmailTemplateConfigBuilder getEmailTemplateBuilder() {
        return emailTemplateConfigBuilder;
    }
}
