package org.lauchproject;


import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.io.IOException;

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
/** This function generates a random password of a specified length**/
    public static String generateRandomPassword(int len) {
        // ASCII range – alphanumeric (0-9, a-z, A-Z)
        final String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder();

        // each iteration of the loop randomly chooses a character from the given
        // ASCII range and appends it to the `StringBuilder` instance

        for (int i = 0; i < len; i++)
        {
            int randomIndex = random.nextInt(chars.length());
            sb.append(chars.charAt(randomIndex));
        }

        return sb.toString();
    }
/** This function generates credentials for the bot to log into Mosquitto **/
    private static void generateCredentials(String email) {
        String separator = System.getProperty("file.separator");
        String absolutePath = "C:" + separator + "Program Files" + separator + "mosquitto" + separator + "pwdfile.txt";
        Path path = Paths.get(absolutePath);

        File file = new File(absolutePath);
        if (!file.exists()) {
            try {
                file.createNewFile(); // generates a new file, in case it's not present
            } catch (IOException e) {
                e.printStackTrace();
            }

            try {
                String pwd = generateRandomPassword(8);
                FileWriter fw = new FileWriter(file.getAbsoluteFile());
                fw.write(email + ":" + pwd);
                fw.flush();
                fw.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
/** Main method **/
    public static void main(String[] args) {
        generateCredentials("giaco.paltri@gmail.com");
        generateCredentials("giaco2.paltri@gmail.com");
        System.out.println(executeCommand("cd C:\\Program Files\\mosquitto\\ && Net start Mosquitto")); // Starts the mosquitto broker
        System.out.println(executeCommand("Taskkill /IM \"mosquitto.exe\" /F")); // Closes the mosquitto broker
    }

}
