package org.lauchproject;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

public class MQTTPubPrint{

    public MQTTPubPrint(){
        Scanner sc = null;
        try {
            sc = new Scanner(new File("C:\\Users\\Giacomo\\Desktop\\textfile.txt"));
            // Location of the text file is placed here
            // Check if there is another line of input
            while(sc.hasNextLine()){
                String str = sc.nextLine();
                // parse each line using delimiter
                parseData(str);
            }
        } catch (IOException  exp) {
            // TODO Auto-generated catch block
            exp.printStackTrace();
        }finally{
            if(sc != null)
                sc.close();
        }
    }
    private static void parseData(String str){
        Scanner lineScanner = new Scanner(str);
        lineScanner.useDelimiter(",");
        while(lineScanner.hasNext()) {
            String topic        = "Pressure";
            String content1     = lineScanner.next() ;
            // String content1  = args[0]+"Pascal";
            int qos             =  1;
            String broker       = "tcp://localhost:1883";
            String PubId        = "127.0.0.1";
            MemoryPersistence persistence = new MemoryPersistence();
            // long startTime = System.nanoTime();
            try {
                MqttClient sampleClient = new MqttClient(broker, PubId, persistence);
                MqttConnectOptions connOpts = new MqttConnectOptions();
                connOpts.setCleanSession(true);
                connOpts.setConnectionTimeout(60);
                connOpts.setKeepAliveInterval(60);
                connOpts.setMqttVersion(MqttConnectOptions.MQTT_VERSION_3_1);
                System.out.println("Connecting to broker: "+ broker);
                sampleClient.connect(connOpts);
                System.out.println("Connected");
                System.out.println("Publishing message: "+ content1);
                MqttMessage message = new MqttMessage(content1.getBytes());
                message.setQos(qos);
                sampleClient.publish(topic,message);
                System.out.println("Message published");
            } catch(MqttException me) {
                System.out.println("Reason :"+ me.getReasonCode());
                System.out.println("Message :"+ me.getMessage());
                System.out.println("Local :"+ me.getLocalizedMessage());
                System.out.println("Cause :"+ me.getCause());
                System.out.println("Exception :"+ me);
                me.printStackTrace();
            }
        }
        lineScanner.close();
    }
}
