package com.throttle;

import com.throttle.model.Gateway;
import org.junit.jupiter.api.Test;

import java.util.concurrent.Executors;

//@Disabled
public class GatewayHarness {

    @Test
    public void harness() throws Exception{
        int maxRequestsInterval = 2;
        int maxRequestsIntervalSeconds = 1;
        int noRequestsIntervalSeconds = 2;

        GatewayFactory gatewayFactory = new GatewayFactory(maxRequestsInterval, maxRequestsIntervalSeconds, noRequestsIntervalSeconds);
        Gateway gateway = gatewayFactory.getGateway();

        for(int i =0;i<1;i++){
            Thread.sleep(330);
            final int k = i;
            Executors.newSingleThreadExecutor().submit(() -> {
                try {
                    String client = "client_"+k;

                    gateway.connect(client);
                    gateway.connect(client);

                    gateway.submit(client, "stuff");
                    gateway.submit(client, "stuffx");
                    gateway.submit(client, "stuffy");
                    gateway.submit(client, "stuffz");

                    Thread.sleep(500);

                    gateway.submit(client, "more Stuff");
                    Thread.sleep(250);

                    gateway.submit(client, "even more Stuff");
                    gateway.submit(client, "yet more Stuff z");
                    Thread.sleep(2_750);

                    gateway.submit(client, "yet more Stuff");

                    Thread.sleep(2_750);

                    gateway.disconnect(client);
                }catch (Exception e){
                    throw new RuntimeException(e);
                }
            });
        }



        Executors.newSingleThreadExecutor().submit(() -> {
            try {
                gateway.connect("client2");

                Thread.sleep(2_750);

                gateway.submit("client2", "client 2 stuff");

                Thread.sleep(2_750);
                gateway.submit("client2", "client 2 slow stuff");

                Thread.sleep(10_000);

                gateway.disconnect("client2");

            }catch (Exception e){
                throw new RuntimeException(e);
            }
        });


        Thread.sleep(11_000);
    }
}
