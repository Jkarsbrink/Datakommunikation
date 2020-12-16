package com.company.lab2;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.util.Timer;
import java.util.TimerTask;

public class Client {
    String topicToPublish = "KYH/sensor/julius/data";
    String topicToRead = "KYH/sensor/julius";
    String broker = "tcp://broker.hivemq.com:1883";
    String clientId = "JavaSample";
    Sensor sensor = new Sensor();
    MqttClient sampleClient;

    Client() {
        MemoryPersistence persistence = new MemoryPersistence();
        try {
            sampleClient = new MqttClient(broker, clientId, persistence);
            MqttConnectOptions connOpts = new MqttConnectOptions();
            connOpts.setCleanSession(true);
            System.out.println("Connecting to broker: " + broker);
            sampleClient.connect(connOpts);
            System.out.println("Connected");

            startTimer();

            sampleClient.subscribe(topicToRead, new MqttPostPropertyMessageListener());

        } catch (MqttException me) {
            System.out.println("reason " + me.getReasonCode());
            System.out.println("msg " + me.getMessage());
            System.out.println("loc " + me.getLocalizedMessage());
            System.out.println("cause " + me.getCause());
            System.out.println("excep " + me);
            me.printStackTrace();
        }
    }

    public void startTimer() {
        Timer timer = new Timer();
        timer.schedule(new RemindTask(), 5 * 1000);
    }

    class MqttPostPropertyMessageListener implements IMqttMessageListener {
        @Override
        public void messageArrived(String var1, MqttMessage var2) throws Exception {
            if (var2.toString().equals("exit")) {
                System.out.println("Disconnecting");
                sampleClient.close();
            }
            String receivedMessage = var1 + ": " + var2.toString();
            System.out.println(receivedMessage);
            String received = var2.toString();
            int receivedTemp = Integer.parseInt(received);
            String controlCMDtoPublish;
            if (receivedTemp == sensor.getIntDegree()) {
                controlCMDtoPublish = "ctrl, =";
            } else if (receivedTemp < sensor.getIntDegree()) {
                controlCMDtoPublish = "ctrl, +";
            } else {
                controlCMDtoPublish = "ctrl, -";
            }
            MqttMessage messageToPublish = new MqttMessage(controlCMDtoPublish.getBytes(StandardCharsets.UTF_8));
            logWriter(receivedMessage);
            logWriter(controlCMDtoPublish);
            sampleClient.publish(topicToPublish, messageToPublish);
        }

    }

    class RemindTask extends TimerTask {
        public void run() {
            String tempSource = "Temperature";
            String tempValue = sensor.getStringDegree();
            String tempToPublish = tempSource + tempValue;
            MqttMessage messageToPublish = new MqttMessage(tempToPublish.getBytes(StandardCharsets.UTF_8));
            try {
                sampleClient.publish(topicToPublish, messageToPublish);
                System.out.println("Degree sent");
                logWriter(tempSource, tempValue);
            } catch (MqttException e) {
                e.printStackTrace();
            }
            startTimer();
        }
    }

    void logWriter(String source, String content) {
        try {
            File file = new File("src/log.txt");
            Timestamp tid = new Timestamp(System.currentTimeMillis());
            FileWriter fw = new FileWriter(file, true);
            fw.write("\n" + tid + ", " + source + ", " + content);
            fw.close();
        } catch (IOException ioe) {
            System.out.println("IOException...");
            ioe.printStackTrace();
        }
    }

    void logWriter(String content) {
        try {
            File file = new File("src/log.txt");
            Timestamp tid = new Timestamp(System.currentTimeMillis());
            FileWriter fw = new FileWriter(file, true);
            fw.write("\n" + tid + ", " + content);
            fw.close();
        } catch (IOException ioe) {
            System.out.println("IOException...");
            ioe.printStackTrace();
        }
    }
    public static void main(String[] args) {
        new Client();
        System.out.println("Preforming task");
    }
}