package org.lauchproject;

import java.io.*;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class GamePreparation {
    static DateFormat df = new SimpleDateFormat("dd-MM-YYYY HH:mm:ss");

    static Timer timer = new Timer();

    private static class MyTimeTask extends TimerTask {
        public void run() {
            // start game
            System.out.println("enters here");
            timer.cancel();
        }
    }

    public static void main(String[] args) throws ParseException {

        System.out.println("Current Time: " + df.format( new Date()));

        //Date and time at which you want to execute
        Date date = df.parse(getGameTime());
        System.out.println("questo? " + date);

        timer.schedule(new MyTimeTask(), date);
    }

    private static String getGameTime() {
        try{
            String separator = System.getProperty("file.separator");
            String path = ".." + separator + "time.txt";
            File file = new File(path);
            FileReader  fr= new FileReader(file);
            BufferedReader br = new BufferedReader(fr);
            String time = br.readLine();
            System.out.println("time : " + time);
            br.close();
            fr.close();
            return time;

        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

}
