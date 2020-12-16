package com.company.lab1;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

public class SNTPMessage {
    private byte leapIndicator = 0;
    private byte versionNumber = 4;
    private byte mode = 0;
    private short stratum = 0;
    private short pollInterval = 0;
    private byte precision = 0;
    private double rootDelay = 0;
    private double rootDispersion = 0;
    private byte[] referenceIdentifier = {0, 0, 0, 0};
    private double referenceTimeStamp = 0;
    private double originateTimeStamp = 0;
    private double receiveTimeStamp = 0;
    private double transmitTimeStamp = 0;

    /*
                    1                   2                   3
   0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9  0  1
  +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
  |LI | VN  |Mode |    Stratum    |     Poll      |   Precision    |
  +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
  |                          Root  Delay                           |
  +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
  |                       Root  Dispersion                         |
  +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
  |                     Reference Identifier                       |
  +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
  |                                                                |
  |                    Reference Timestamp (64)                    |
  |                                                                |
  +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
  |                                                                |
  |                    Originate Timestamp (64)                    |
  |                                                                |
  +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
  |                                                                |
  |                     Receive Timestamp (64)                     |
  |                                                                |
  +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
  |                                                                |
  |                     Transmit Timestamp (64)                    |
  |                                                                |
  +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
  |                 Key Identifier (optional) (32)                 |
  +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
  |                                                                |
  |                                                                |
  |                 Message Digest (optional) (128)                |
  |                                                                |
  |                                                                |
  +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
     */
    SNTPMessage(byte[] buf) {
        byte b = buf[0];
        leapIndicator = (byte) ((b >> 6) & 0x3);
        versionNumber = (byte) ((b >> 3) & 0x7);
        mode = (byte) (b & 0x7); //0010 0100
        stratum = unsignedByteToShort(buf[1]); // 1
        pollInterval = unsignedByteToShort(buf[2]); // 0
        precision = buf[3]; // -25
        rootDelay = (((buf[4] * (0xFF + 1.0)) +
                (unsignedByteToShort(buf[5]))) +
                ((unsignedByteToShort(buf[6]) / (0xFF + 1.0)) +
                        (unsignedByteToShort(buf[7]) / (0xFFFF + 1.0)))); // 0,   0,  0,   0,

        rootDispersion = (((buf[8] * (0xFF + 1.0)) +
                (unsignedByteToShort(buf[9]))) +
                ((unsignedByteToShort(buf[10]) / (0xFF + 1.0)) +
                        (unsignedByteToShort(buf[11]) / (0xFFFF + 1.0)))); // 0,   0,  0,   2,
        referenceIdentifier[0] = buf[12]; // referenceIdentifier = 80, 80, 83, 0,
        referenceIdentifier[1] = buf[13];
        referenceIdentifier[2] = buf[14];
        referenceIdentifier[3] = buf[15];

        referenceTimeStamp = byteArrayToDouble(buf, 16); // -29, 116,  5,  61,  0,  0,    0,   0,
        originateTimeStamp = byteArrayToDouble(buf, 24); // -29, 116,  5,  61,  0,  0,    0,   0,
        receiveTimeStamp = byteArrayToDouble(buf, 32); // -29, 116,  5,  62,  0, 47, -121, -38,
        transmitTimeStamp = byteArrayToDouble(buf, 40); // -29, 116,  5,  62,  0, 47, -113,  -1}
    }

    public SNTPMessage() {
        mode = 3;
        transmitTimeStamp = ((System.currentTimeMillis() / 1000.0) + (2208988800.0)); // 1900 -> 1970 = 70 years = 2208988800.0 Seconds
    }

    private double byteArrayToDouble(byte[] buf, int index) {
        double result = 0.0;
        for (int i = 0; i < 8; i++) {
            result += unsignedByteToShort(buf[index + i]) * Math.pow(2, (3 - i) * 8);
        }
        return result;
    }

    private short unsignedByteToShort(byte b) {
        if (((b >> 7) & 0x1) == 1) {
            return (short) ((b & 0x7F) + 128); // 0111 1111 -> 64+32+16+8+4+2+1 = 127 -> 0x7F hexadecimal, 128 tack vare första biten
        } else {
            return (short) ((b & 0xFF));
        }
    }

    public byte[] toByteArray() {
        byte[] array = new byte[48];

        array[0] = (byte) ((leapIndicator << 6) | (versionNumber << 3) | mode);
        //  | LI |  VN  |  Mode |
        //    00  1 0 0   0 1 1 - (00100011) bytekod

        array[1] = (byte) stratum;
        array[2] = (byte) pollInterval;
        array[3] = precision;

        int dataRootDelay = (int) (rootDelay * (0xFF + 1));
        array[4] = (byte) ((dataRootDelay >> 24) & 0XFF);
        array[5] = (byte) ((dataRootDelay >> 16) & 0XFF);
        array[6] = (byte) ((dataRootDelay >> 8) & 0XFF);
        array[7] = (byte) (dataRootDelay & 0XFF);

        int dataRootDispersion = (int) (rootDispersion * (0xFF + 1));
        array[8] = (byte) ((dataRootDispersion >> 24) & 0XFF);
        array[9] = (byte) ((dataRootDispersion >> 16) & 0XFF);
        array[10] = (byte) ((dataRootDispersion >> 8) & 0XFF);
        array[11] = (byte) (dataRootDispersion & 0XFF);

        array[12] = referenceIdentifier[0];
        array[13] = referenceIdentifier[1];
        array[14] = referenceIdentifier[2];
        array[15] = referenceIdentifier[3];

        doubleToByteArray(array, 16, referenceTimeStamp);
        doubleToByteArray(array, 24, originateTimeStamp);
        doubleToByteArray(array, 32, receiveTimeStamp);
        doubleToByteArray(array, 40, transmitTimeStamp);

        return array;
    }

    private void doubleToByteArray(byte[] array, int index, double data) {
        for (int i = 0; i < 8; i++) {
            array[index + i] = (byte) (data / Math.pow(2, (3 - i) * 8));
            data -= (double) (unsignedByteToShort(array[index + i]) * Math.pow(2, (3 - i) * 8));
        }
    }

    public void printDataToConsole() throws UnsupportedEncodingException {
        System.out.println();
        System.out.println("--Printing data from the message--");
        System.out.println("LeapIndicator: " + leapIndicator);
        System.out.println("VersionNumber: " + versionNumber);
        System.out.println("Mode: " + mode);
        System.out.println("Stratum: " + stratum);
        System.out.println("pollInterval: " + pollInterval);
        System.out.println("precision: " + precision);
        System.out.println("rootDelay: " + rootDelay);
        System.out.println("rootDispersion: " + rootDispersion);
        System.out.println("ReferenceIdentifier: " + new String(referenceIdentifier, "US-ASCII"));
        System.out.println("referenceTimeStamp: " + referenceTimeStamp);
        System.out.println("originateTimeStamp: " + originateTimeStamp);
        System.out.println("receiveTimeStamp: " + referenceTimeStamp);
        System.out.println("transmitTimeStamp: " + transmitTimeStamp);
        System.out.println();
        System.out.println("Done.");
        System.out.println("-----------------------");
    }

    public double getReferenceTimeStamp() {
        return this.referenceTimeStamp;
    }

    public double getOriginateTimeStamp() {
        return this.originateTimeStamp;
    }

    public double getReceiveTimeStamp() {
        return this.receiveTimeStamp;
    }

    public double getTransmitTimeStamp() {
        return this.transmitTimeStamp;
    }

    public byte getMode() {
        return this.mode;
    }

    public String toString() {
        StringBuilder msg = new StringBuilder();
        msg.append("LeapIndicator: ").append(leapIndicator).append("\n");
        msg.append("VersionNumber: ").append(versionNumber).append("\n");
        msg.append("Mode: ").append(mode).append("\n");
        msg.append("Stratum: ").append(stratum).append("\n");
        msg.append("pollInterval: ").append(pollInterval).append("\n");
        msg.append("precision: ").append(precision).append("\n");
        msg.append("rootDelay: ").append(rootDelay).append("\n");
        msg.append("rootDispersion: ").append(rootDispersion).append("\n");
        msg.append("ReferenceIdentifier: ").append(new String(referenceIdentifier, StandardCharsets.US_ASCII)).append("\n");
        msg.append("referenceTimeStamp: ").append(referenceTimeStamp).append("\n");
        msg.append("originateTimeStamp: ").append(originateTimeStamp).append("\n");
        msg.append("receiveTimeStamp: ").append(referenceTimeStamp).append("\n");
        msg.insert(msg.length(), "transmitTimeStamp: " + transmitTimeStamp + "\n");
        return msg.toString();
    }
}