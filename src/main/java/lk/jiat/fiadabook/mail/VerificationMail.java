package lk.jiat.fiadabook.mail;

import io.rocketbase.mail.model.HtmlTextEmail;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.InternetAddress;
import lk.jiat.fiadabook.util.Env;


public class VerificationMail extends Mailable {
    private final String to;
    private final String verificationCode;

    public VerificationMail(String to, String verificationCode) {
        this.to = to;
        this.verificationCode = verificationCode;
    }

    @Override
    public void build(Message message) throws MessagingException {
        message.setRecipient(Message.RecipientType.TO, new InternetAddress(to));
        message.setSubject("New User Verification Code - " + Env.getProperty("app.name"));

        String appURL = Env.getProperty("app.url");
        String verifyURL = appURL + "/verify.html?email=" + to + "&verificationCode=" + verificationCode;

        HtmlTextEmail htmlTextEmail = getEmailTemplateBuilder()
                .header()
                .and()
                .text("Have our warm Welcome " + to).h1().center().and()
                .text("Have a nice shopping experience !").center().and()
                .text("To verify your email please click on the button below.").center().and()
                .text("Your verification code is \n" + verificationCode).center().and()
                .button("Verify your email", verifyURL).blue().center().and()
                .text("If you have any problem please paste this link on your browser.").center().and()
                .copyright(Env.getProperty("app.name")).url(appURL).suffix("All right Reserved").and()
                .build();

        message.setContent(htmlTextEmail.getHtml(), "text/html; charset=utf-8");


    }
}
