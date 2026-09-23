package lk.jiat.fiadabook.mail;

import io.rocketbase.mail.model.HtmlTextEmail;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.InternetAddress;
import lk.jiat.fiadabook.util.Env;


public class ForgotPasswordMail extends Mailable{

    private final String to;
    private final String verificationCode;

    public ForgotPasswordMail(String to, String verificationCode) {
        this.to = to;
        this.verificationCode = verificationCode;
    }

    @Override
    public void build(Message message) throws MessagingException {

        message.setRecipient(Message.RecipientType.TO, new InternetAddress(to));
        message.setSubject("Forgot Password Reset Code - " + Env.getProperty("app.name"));
        String appURL = Env.getProperty("app.url");

        HtmlTextEmail htmlTextEmail = getEmailTemplateBuilder()
                .header()
                .and()
                .text("Forgot Password Reset Code" + to).h1().center().and()
                .text("Use to verify that it's you.").center().and()
                .text("Your verification code is \n" + verificationCode).center().and()
                .copyright(Env.getProperty("app.name")).url(appURL).suffix("All right Reserved").and()
                .build();
        message.setContent(htmlTextEmail.getHtml(), "text/html; charset=utf-8");
    }
}
