package org.lauchproject;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.*;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.io.IOException;
import java.util.ListIterator;

public class GameSettings {
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
        String file_name = "pwfile.txt";
        String separator = System.getProperty("file.separator");
        String absolutePath = "C:" + separator + "Program Files" + separator + "mosquitto" + separator + file_name;
        for (String user : users) {
            String pwd = generateRandomPassword(8);

            users_pwd.add(user + ":" + pwd);
            pwds.add(pwd);
        }
        writeToFile(absolutePath,users_pwd);
        executeCommand("cd " + absolutePath.replace(file_name, "") + " && mosquitto_passwd -U pwfile.txt"); // hashes the password file
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
    private static ArrayList<String> setACLs(ArrayList<String> users) {
        boolean existing_topic = false;
        JSONArray topics = new JSONArray();
        for (int i = 0; i < users.size() - 1; i++)
        {
            for (String user : users) {
                if (!users.get(i).equals(user)) // the 2 users can't be equal
                {
                    if (topics.size() != 0) // only if it's not the first topic
                    {
                        for (int j = 0; j < topics.size(); j++) {
                            if (topics.get(j).toString().contains(users.get(i)) && topics.get(j).toString().contains(user)) {//contains()
                                existing_topic = true;
                                break;
                            }
                        }
                        if (!existing_topic)
                            topics.add(users.get(i) + "_" + user);
                        existing_topic = false;
                    } else
                        topics.add(users.get(i) + "_" + user);
                }
            }
        }
        //for (String topic : topics) System.out.println(topic);
        System.out.println("CIAOOOO" + topics.toString());
        return topics;
    }
/** This function creates the string message that will be sent to every bot **/
    private static void generateMailContent(ArrayList<String> users, ArrayList<String> topics, ArrayList<String> pwds, JSONObject rules, int bot_instances) {
        ArrayList<String> mails = new ArrayList<>();
        JSONArray roomList;
        int subRoomList = subRoomGenerator(users.size(), bot_instances);
        JSONObject singleMail = new JSONObject();
        for (int i = 0; i < users.size(); i++){
            singleMail.put("user", users.get(i));
            singleMail.put("pwd", pwds.get(i));
            singleMail.put("rules", rules);
            roomList = getTopicAccess(topics, users.get(i));
            singleMail.put("rooms", roomList);
            singleMail.put("room_instance", subRoomList);
//            subRoomList = subRoomGenerator(users.size(), bot_instances);
//            singleMail.put("room_instance", subRoomList);


            mails.add(singleMail.toString().replace("\\",""));

            writeACLS(users, topics, subRoomList);

            //SendMail.send(users.get(i), "GAME", mails.get(i));
            singleMail.clear();
        }
        System.out.println(mails);
    }
/** This function writes the ACLS for every user on the config file **/
    private static void writeACLS(ArrayList<String> users, ArrayList<String> topics, int subRoomList) {
        JSONArray accessedTopics;
        String separator = System.getProperty("file.separator");
        String absolutePath = "C:" + separator + "Program Files" + separator + "mosquitto" + separator + "aclfile.txt";
        ArrayList<String> toWrite = new ArrayList<>();
        toWrite.add("# This affects access control for clients with no username.");
        toWrite.add("topic read $SYS/#");
        for (String user:users){ // for every user
            toWrite.add("user " + user);
            accessedTopics = getTopicAccess(topics,user);
            for (int i = 0; i < accessedTopics.size(); i++){
                for (int j = 0; j < subRoomList; j++)
                toWrite.add("topic " + accessedTopics.get(i) + "/" + j);
            }
        }

        writeToFile(absolutePath, toWrite);
    }

    private static int subRoomGenerator(int size, int bot_instances) {
        return bot_instances/size;
    }

    /**FUNCTION TEMPORARILY DEPRECATED**/
///** This function returns the set of subroom accessible to every player [1,2,3,4,5,6,7,8,9...] **/
//    private static JSONArray subRoomGenerator(int size, int bot_instances) {
//        JSONArray subRoomList = new JSONArray();
//        for (int i = 0; i < bot_instances/size; i++)
//            subRoomList.add(i);
//        return subRoomList;
//    }
/** This function returns the topics that a user has access to **/
    private static JSONArray getTopicAccess(ArrayList<String> topics, String user) {
        JSONArray permittedTopics = new JSONArray();
        for (String topic : topics)
            if (topic.contains(user))
                permittedTopics.add(topic);
        return permittedTopics;
    }

    /** Main method **/
    public static void main(String[] args) {
        ArrayList<String> users = new ArrayList<>();
        JSONObject rules = new JSONObject();
        rules.put("time", 20);
        rules.put("bot_number", 150);
        rules.put("connection_time", 20);
        rules.put("date", "22/08/2002 15:30");

        users.add("TRISSER.server@gmail.com");
        users.add("giaco.paltri@gmail.com");             // list of users
        users.add("abdullah.ali@einaudicorreggio.it");

        ArrayList<String> topics = setACLs(users);
        ArrayList<String> pwds = setPassword(users);
        System.out.println(executeCommand("cd C:\\Program Files\\mosquitto\\ && Net start Mosquitto")); // Starts the mosquitto broker
        //new MQTTPubPrint(); // test send message
        System.out.println(executeCommand("Taskkill /IM \"mosquitto.exe\" /F")); // Closes the mosquitto broker
        generateMailContent(users, topics, pwds, rules, 150);
    }
}
