package com.company.lab2;

import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

public class ReadData {
    String subscribeTopic = "KYH/julius/sensor/data";
    String mqttBroker = "tcp://broker.hivemq.com:1883";
    String clientId = "e-readerData";
    MqttClient mqttClient;

    ReadData() {
        try {
            MemoryPersistence persistence = new MemoryPersistence();
            mqttClient = new MqttClient(mqttBroker, clientId, persistence);
            MqttConnectOptions connOpts = new MqttConnectOptions();
            connOpts.setCleanSession(true);
            System.out.println("Connecting to HiveMq " + mqttBroker);
            mqttClient.connect(connOpts);
            System.out.println("Connected and listening to topic: " + subscribeTopic);
            mqttClient.subscribe(subscribeTopic, new MqttPostPropertyMessageListener());
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    class MqttPostPropertyMessageListener implements IMqttMessageListener {
        @Override
        public void messageArrived(String topic, MqttMessage content) {
            String receivedContent = topic + ", " + content.toString();
            System.out.println(receivedContent);
        }
    }

    public static void main(String[] args) {
        new ReadData();
    }
}
