package src;
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
import java.io.*;

/**
 *
 * @author Julien Feis
 */
public class HuffmanTest {
   
    public static void main(String[] args){
        //
        String testString = "aGazillionSongs.txt";
        File testFile = new File("aGazillionSongs.txt");
        HuffmanEncode huffy = new HuffmanEncode(testFile);
        huffy.encode();
        //huffy.writeToFile(testString);
    }
}
