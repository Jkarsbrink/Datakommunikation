package com.company.lab1;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;

public class Main {
    String[] server = new String[8];
    Main(){
        multipleConnection();
        SNTPMessage message = new SNTPMessage();
        SNTPMessage response = null;
        try{
            response = sendMessage(message);
        }catch (IOException e){
            e.printStackTrace();
        }
        if(response != null){
            System.out.println(response.toString());
            System.out.println("\n");
            calculateRoundTripTime(response);
        }else {
            System.out.println("Couldn't find a connection");
        }
    }

    private SNTPMessage sendMessage(SNTPMessage message) throws IOException {
        SNTPMessage response;
        DatagramSocket socket = new DatagramSocket();
        byte[] buf = message.toByteArray();
        socket.setSoTimeout(5);
        int i =0;
        while (true) {
            InetAddress address = InetAddress.getByName(server[i++]);
            DatagramPacket packet = new DatagramPacket(buf, buf.length, address, 123);
            socket.send(packet);
            System.out.println("Requesting to connect to server: " + address.getHostAddress() + ": 123");
            try {
                socket.receive(packet);
            } catch (SocketTimeoutException e) {
                System.out.println("Couldn't get a connection before timeout, trying next server:");
            }
            response = new SNTPMessage(packet.getData());
            if (response.getMode() == 4) {
                System.out.println("Received message from server: " + packet.getAddress().getHostAddress() + ":" + packet.getPort());
                break;
            } else if (i == server.length) {
                i = 0;
            }
        }
        System.out.println("Connection closed to server");
        socket.close();
        return response;
    }
    private static void calculateRoundTripTime(SNTPMessage message){
        double t1 = message.getOriginateTimeStamp();
        double t2 = message.getReceiveTimeStamp();
        double t3 = message.getTransmitTimeStamp();
        double t4 = message.getReferenceTimeStamp();
        double delay = (t4-t1) - (t3 - t2);
        double offset = ((t2 - t1) + (t3 - t4)) /2;
        System.out.println("Delay is: " + delay);
        System.out.println("Offset on server is: " + offset +" sec");

    }

    private void multipleConnection() {
        server[0] = "gbg1.ntp.se";
        server[1] = "gbg2.ntp.se";
        server[2] = "sth1.ntp.se";
        server[3] = "sth2.ntp.se";
        server[4] = "mmo1.ntp.se";
        server[5] = "mmo2.ntp.se";
    }
    void startUpServerAndPrintToConsole(SNTPMessage message) throws IOException {
        for (String s: server) {
            DatagramSocket socket = new DatagramSocket();
            byte[] buf = message.toByteArray();
            SNTPMessage response;
            InetAddress address = InetAddress.getByName(s);
            DatagramPacket packet = new DatagramPacket(buf, buf.length, address, 123);
            socket.send(packet);
            System.out.println("Requesting to connect to server: " + address.getHostName() + ":123");
            socket.receive(packet);
            response = new SNTPMessage(packet.getData());
            System.out.println("Closing connection");
            System.out.println(response.toString());
            calculateRoundTripTime(response);
            if(response.getMode() != 4){
                socket.close();
            }

        }
    }

    public static void main(String[] args) {
        new Main();
    }
}
