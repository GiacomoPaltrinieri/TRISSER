package org.lauchproject;

import org.json.simple.JSONObject;

import java.io.*;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.io.IOException;
import java.util.ListIterator;

public class prova {

/** This method takes a String containing one or more commands (command -> to use more commands, just insert command1 && command2...) to execute in the CMD, the result String you get in return is the output of the command you would see on the CMD**/
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
            System.out.println("an error occurred while reading the lines (r.readline)");
        } catch (InterruptedException e) {
            e.printStackTrace();
            System.out.println("an error occurred while starting the builder(builder.start)");
            //waitFor error
        }
        return result.toString();
    }
/** This function generates a random password of a specified length (len)**/
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
/** This function generates a password for bots to log into Mosquitto **/
    private static ArrayList<String> setPassword(ArrayList<String> users) {
        ArrayList<String> users_pwd = new ArrayList<>();
        ArrayList<String> pwds = new ArrayList<>();
        String separator = System.getProperty("file.separator");
        String absolutePath = "C:" + separator + "Program Files" + separator + "mosquitto" + separator + "pwfile.txt";
        for (String user : users) {
            String pwd = generateRandomPassword(8);

            users_pwd.add(user + ":" + pwd);
            pwds.add(pwd);
        }
        writeToFile(absolutePath,users_pwd);
        return pwds;
    }
/** This function writes on a file which path has to be specified (including file name) in absolutePath (note that you have to use a separator, or 2 \\ -> NOT C:\...\file.txt BUT C:\\...\\file.txt). every line that has to be written has to be placed in an Arraylist element (lines)**/
    public static void writeToFile(String absolutePath, ArrayList<String> lines) {
        File file = new File(absolutePath); // Creates File object with the specified path. The path must include the filename
        if (!file.exists()) {
            try {
                file.createNewFile(); // generates a new file, in case it's not present
                System.out.println("creating file");
            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("something went wrong while creating the file");
            }
        }
        FileWriter fw = null;
        try {
            fw = new FileWriter(file.getAbsoluteFile());
        } catch (IOException e) {
            System.out.println("something went wrong while starting the FileWriter");
        }

        ListIterator<String> line = lines.listIterator();
        while (line.hasNext()) {
            try {
                fw.write(line.next() + "\n"); // writes single line
            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("something went wrong while writing on the file");
            }
        }
        try {
            fw.flush();
            fw.close(); // closes the FileWriter
        } catch (IOException e) {
            System.out.println("something went wrong while closing the file");
        }
    }
/** This method sets ACL's for every user (topic restriction) **/
    private static ArrayList<String> setACLs(ArrayList<String> users, int game_number) {
        boolean existing_topic = false;
        ArrayList<String> topics = new ArrayList<>();
        for (int i = 0; i < users.size() - 1; i++)
        {
            for (String user : users) {
                if (!users.get(i).equals(user)) // the 2 users can't be equal
                {
                    if (topics.size() != 0) // only if it's not the first topic
                    {
                        for (String topic : topics) {
                            if (topic.contains(users.get(i)) && topic.contains(user)) {
                                existing_topic = true;
                                break;
                            }
                        }
                        if (!existing_topic)
                            topics.add(users.get(i) + "_" + user + "/");
                        existing_topic = false;
                    } else
                        topics.add(users.get(i) + "_" + user + "/");
                }
            }
        }
        for (String topic : topics) System.out.println(topic);

        if (game_number % users.size() == 0){ // the number of games can be divided equally between the bots
            for (int i = 0; i < topics.size(); i++)
                for (int j=0; j < game_number/users.size(); j++)
                    if (j == game_number/users.size() - 1)
                        topics.set(i, topics.get(i) + j + ";");
                    else
                        topics.set(i, topics.get(i) + j + ",");

            for (int i = 0; i < users.size(); i++)
                System.out.println(topics.get(i));
            return topics;
        }
        else
            System.out.println("An error occurred, the room number you chose isn't valid");
        return null;
    }
/** This function creates the string message that will be sent to every bot **/
    private static void generateMailContent(ArrayList<String> users, ArrayList<String> topics, ArrayList<String> pwds, JSONObject rules) {
        ArrayList<String> mails = new ArrayList<>();
        JSONObject singleMail = new JSONObject();
        for (int i = 0; i < users.size(); i++){
            singleMail.put("user", users.get(i));
            singleMail.put("pwd", pwds.get(i));
            singleMail.put("rules", rules);
            singleMail.put("topics", getTopicAccess(topics, users.get(i)));
            mails.add(singleMail.toString());
            SendMail.send(users.get(i), "GAME", mails.get(i));
            singleMail.clear();
        }
        System.out.println(mails);
    }
/** This function returns the topics that a user has access to **/
    private static String getTopicAccess(ArrayList<String> topics, String user) {
        String permittedTopics = "";
        for (String topic : topics)
            if (topic.contains(user))
                permittedTopics = permittedTopics + topic;
        return permittedTopics;
    }

    /** Main method **/
    public static void main(String[] args) {
        ArrayList<String> users = new ArrayList<>();
        JSONObject rules = new JSONObject();
        rules.put("time", 20);
        rules.put("bot_number", 150);
        rules.put("connection_time", 20);
        rules.put("date", "22/08/2002");
        rules.put("start_time", "15:30");

        users.add("TRISSER.server@gmail.com");
        users.add("giaco.paltri@gmail.com");             // list of users
        users.add("abdullah.ali@einaudicorreggio.it");

        ArrayList<String> topics = setACLs(users,150);
        ArrayList<String> pwds = setPassword(users);
        System.out.println(executeCommand("cd C:\\Program Files\\mosquitto\\ && Net start Mosquitto")); // Starts the mosquitto broker
        //new MQTTPubPrint(); // test send message
        System.out.println(executeCommand("Taskkill /IM \"mosquitto.exe\" /F")); // Closes the mosquitto broker
        generateMailContent(users, topics, pwds, rules);
    }


}
