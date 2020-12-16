package com.company.lab2;

import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;

public class ReadData {
    String subscribeTopic = "KYH/julius/#";
    String mqttBroker = "tcp://broker.hivemq.com:1883";
    String clientId = "e-readerData";
    MqttClient mqttClient;

    ReadData() {
        try {
            MemoryPersistence persistence = new MemoryPersistence();
            mqttClient = new MqttClient(mqttBroker, clientId, persistence);
            MqttConnectOptions connOpts = new MqttConnectOptions();
            connOpts.setCleanSession(true);
            System.out.println("Connecting to HiveMQ " + mqttBroker);
            mqttClient.connect(connOpts);
            System.out.println("Connected and listening to topic: " + subscribeTopic);
            mqttClient.subscribe(subscribeTopic, new MqttPostPropertyMessageListener());
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    class MqttPostPropertyMessageListener implements IMqttMessageListener {
        @Override
        public void messageArrived(String topic, MqttMessage content) throws IOException {
            Date date = new Date();
            String receivedContent = topic + ", " + content.toString();
            System.out.println(date + ": " +receivedContent);
            FileWriter fw = new FileWriter("C:\\Users\\juliu\\GitHub\\Datakommunikation2\\Datakommunikation\\src\\com\\company\\lab2\\log.txt", true);
            fw.write(date + ", " + receivedContent + "\n");
            fw.close();
        }
    }

    public static void main(String[] args) {
        new ReadData();
    }
}
