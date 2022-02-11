package org.lauchproject;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class GamePreparation {
     public static void main(String[] args) {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        LocalDateTime now = LocalDateTime.now();
        System.out.println(dtf.format(now));

    }

}
