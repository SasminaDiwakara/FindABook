package lk.jiat.fiadabook.provider;

import jakarta.mail.Authenticator;
import jakarta.mail.PasswordAuthentication;
import lk.jiat.fiadabook.mail.Mailable;
import lk.jiat.fiadabook.util.Env;


import java.util.Properties;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.*;

public class MailServiceProvider {
    private ThreadPoolExecutor executor;
    private Authenticator authenticator;
    private final BlockingDeque<Runnable> blockingQueue = new LinkedBlockingDeque<>();
    private final Properties properties = new Properties();
    private static MailServiceProvider mailServiceProvider;

    private MailServiceProvider(){
        properties.put("mail.smtp.auth",true);
        properties.put("mail.smtp.starttls.enable",true);
        properties.put("mail.smtp.host", Env.getProperty("mail.host"));
        properties.put("mail.smtp.port", Env.getProperty("mail.port"));
    }

    public static MailServiceProvider getInstance(){
        if(mailServiceProvider == null){
            mailServiceProvider = new MailServiceProvider();

        }
        return mailServiceProvider;
    }

    public void start(){
        authenticator = new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(Env.getProperty("mail.username"), Env.getProperty("mail.password"));
            }
        };

        System.out.println(Env.getProperty("mail.username")+" "+Env.getProperty("mail.password"));

        executor = new ThreadPoolExecutor(2, 2, 5,
                TimeUnit.SECONDS, blockingQueue, new ThreadPoolExecutor.AbortPolicy());

        executor.prestartCoreThread();
        System.out.println("\u001B[35mEmail ServiceProvider Initialized...");

    }

    public Properties getProperties(){
        return properties;
    }

    public Authenticator getAuthenticator(){
        return authenticator;
    }

    public void shutdown(){
        if(executor != null){
            executor.shutdown();
        }
    }

    public void sendMail(Mailable mailable){
        boolean offer  = blockingQueue.offer(mailable);
    }

}
