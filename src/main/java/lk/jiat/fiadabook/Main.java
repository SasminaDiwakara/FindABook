package lk.jiat.fiadabook;

import lk.jiat.fiadabook.config.AppConfig;
import org.apache.catalina.Context;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.Wrapper;
import org.apache.catalina.startup.Tomcat;
import org.glassfish.jersey.servlet.ServletContainer;

import java.awt.*;
import java.io.File;
import java.net.URI;

public class Main {

    private static final int SERVER_PORT = 8080;
    public static final String CONTEXT_PATH = "/findabook";

    public static void main(String[] args) {
        try {
            Tomcat tomcat = new Tomcat();
            tomcat.setPort(SERVER_PORT);
            tomcat.getConnector();

            Context context = tomcat.addWebapp(CONTEXT_PATH, new File("src/main/webapp").getAbsolutePath());
            context.setSessionTimeout(30);
            Tomcat.addServlet(context, "JerseyServlet", new ServletContainer(new AppConfig()));
            context.addServletMappingDecoded("/api/*", "JerseyServlet");

            tomcat.start();

            String url = "http://localhost:" + SERVER_PORT + CONTEXT_PATH ;
            openInBrowser(url);

            System.out.println("\u001B[36mApp URL : http://localhost:"+SERVER_PORT+CONTEXT_PATH);
            tomcat.getServer().await();
        } catch (LifecycleException e) {
            throw new RuntimeException("\u001B[35mTomcat Embeded Server Starting Failed" + e.getMessage());
        }


    }

    private static void openInBrowser(String url){

        if(Desktop.isDesktopSupported()){

            try {
                Desktop.getDesktop().browse(URI.create(url));
                System.out.println("\u001B[32mBrowser opened successfully!");
            }catch (Exception e){
                System.out.println("\u001B[31mFailed to open browser: " + e.getMessage());
            }

        }else{
            System.out.println("\u001B[33mDesktop not supported. Please open manually: " + url);
        }

    }


}
