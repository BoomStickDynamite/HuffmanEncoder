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
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws FileNotFoundException {
        //
        String testString = "foxtext.txt";
        File testFile = new File("foxtext.txt");
        //HuffmanEncode huffy = new HuffmanEncode(testFile);
        //huffy.encode();
        HuffmanEncode huffy = new HuffmanEncode(testFile);
        huffy.writeToFile(testString);
    }
}
