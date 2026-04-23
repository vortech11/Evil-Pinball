package com.goobers.evilpinball;

import java.io.File;
import java.util.Scanner;

import com.google.gson.*;

public class LevelLoader {
    public static String loadFileText(String fileName){
        File file = new File(fileName);

        String output = "";

        try {
            Scanner fileScanner = new Scanner(file);

            while (fileScanner.hasNextLine()){
                output += fileScanner.nextLine();
            }

            fileScanner.close();

        } catch (Exception e) {
            //System.out.println(e);
            // if error, don't
        }

        return output;
    }

    public static Level textFileParse(String textData){
        Gson parser = new Gson();
        
        Level levelData = parser.fromJson(textData, Level.class);

        return levelData;
    }

    public static Level loadLevel(String level){
        return textFileParse(loadFileText(level));
    }
}
