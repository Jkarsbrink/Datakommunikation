package com.company.lab2;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.nio.charset.StandardCharsets;
import java.util.Timer;
import java.util.TimerTask;


public class Sensor {
    String topic = "KYH/julius/sensor";
    String mqttBroker = "tcp://broker.hivemq.com:1883";
    String clientId = "e-TempSensor";
    MqttClient mqttClient;
    Timer timer;
    int oneMinute = 10;
    long delay = oneMinute * 1000L;
    int degree = 0;
    Sensor() {
        try {
            MemoryPersistence persistence = new MemoryPersistence();
            mqttClient = new MqttClient(mqttBroker, clientId, persistence);
            MqttConnectOptions connOpts = new MqttConnectOptions();
            connOpts.setCleanSession(true);
            System.out.println("Getting Temperature from sensor");
            System.out.println("Connecting to HiveMQ: " + mqttBroker);
            mqttClient.connect(connOpts);
            System.out.println("Connected and writing to topic: " + topic);
            timer = new Timer();
            timer.schedule(new TimerStarting(), delay);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    public String getStringDegree() {
        int degree = (int) ((Math.random()*10) +15);
        return String.valueOf(degree);
    }

    public int getIntDegree() {
        this.degree = (int) ((Math.random()*10) +15);
        return degree;
    }


    private class TimerStarting extends TimerTask {
        @Override
        public void run(){
            try{
                String temperature = getStringDegree() + "'C";
                MqttMessage message = new MqttMessage(temperature.getBytes(StandardCharsets.UTF_8));
                message.setQos(2);
                mqttClient.publish(topic,message);
                System.out.println("Sending temperature: " + temperature);
            }catch (MqttException e){
                e.printStackTrace();
            }
            timer.cancel();
            timer = new Timer();
            timer.schedule(new TimerStarting(), delay);
            System.out.println("Setting new timer");
        }
    }

    public static void main(String[] args) {
        new Sensor();
    }
}