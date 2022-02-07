package org.lauchproject;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class prova {

/** This method takes a String containing one or more commands to execute in the CMD, the result String you get in return is the output of the command you would see on the CMD**/
    public static String executeCommand(String command) {
        String line;
        StringBuilder result = new StringBuilder();
        try {
            ProcessBuilder builder;

            builder = new ProcessBuilder("cmd.exe", "/c", command);

            builder.redirectErrorStream(true);
            Process p = builder.start();
            p.waitFor();
            BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()));
            while (true) {
                line = r.readLine();
                if (line == null) {
                    break;
                }
                result.append(line).append("\n");
            }
        } catch (IOException e) {
            System.out.println("Exception = " + e.getMessage());
        } catch (InterruptedException e) {
            e.printStackTrace();
            //waitFor error
        }
        return result.toString();
    }

    public static void main(String[] args) {

        System.out.println(executeCommand("cd C:\\Program Files\\mosquitto\\ && Net start Mosquitto")); // Starts the mosquitto broker
        new MQTTPubPrint(); // Sends some MQTT messages to the broker
        System.out.println(executeCommand("Taskkill /IM \"mosquitto.exe\" /F")); // Closes the mosquitto broker
    }
}
