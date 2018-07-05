package com.mnemosyne;

//import com.sun.jersey.api.container.grizzly2.GrizzlyServerFactory;
//import com.sun.jersey.api.core.PackagesResourceConfig;
//import com.sun.jersey.api.core.ResourceConfig;
//import java.io.IOException;
//import java.net.URI;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
//import javax.ws.rs.core.UriBuilder;
//import org.glassfish.grizzly.http.server.HttpServer;

/**
 * Created by 希罗 on 2018/7/3
 */
@Path("preload")
public class MainTest {


    @Path("pre")
    @GET
    public String preload(){
        return "ok";
    }

//    public static void main(String[] args) {
//
//        URI uri = UriBuilder.fromUri("http://localhost").port(8080).build();
//        ResourceConfig rc = new PackagesResourceConfig("com.mnemosyne");
//        try {
//            HttpServer server = GrizzlyServerFactory.createHttpServer(uri, rc);
//            server.start();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//        Mnemosyne mnemosyne = new Mnemosyne();
//        mnemosyne.initContext();
//        mnemosyne.start();
//
//        System.out.println("finish");
//    }

}
