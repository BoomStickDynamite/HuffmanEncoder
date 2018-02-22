package src;

import java.io.*;
import java.nio.*;
import java.util.*;


/**
 * This program encodes a file given.
 * Uses the Huffman algorithm.
 * 
 * @author Grant Zukowski
 * @version 1.2
 */
public class HuffmanEncode
{
    File theFile;
    File compressedFile;
    ArrayList<Character> characters = new ArrayList<Character>();
    PriorityQueue<CharNode> nodeQueue = new PriorityQueue<CharNode>();
    PriorityQueue<ByteNode> nodeByteQueue = new PriorityQueue<ByteNode>();
    ArrayList<Character> uniqueChars = new ArrayList<Character>();
    HashMap<Character, Integer> charMap = new HashMap<Character, Integer>();
    CharNode overallRoot;
    ByteNode overallByteRoot;
    String traverser = "";
    ArrayList<Integer> byteArray = new ArrayList<Integer>();
    HashMap<Integer, Integer> myFirstMap = new HashMap<Integer, Integer>();
    HashMap<Integer, String> encodedBytesMap = new HashMap<Integer, String>();
    
    /**
     * Constructor for objects of class HuffmanEncode to start with a file
     * also creates the gzip file from the input file name.
     * 
     * @param File
     */
    public HuffmanEncode (File input)
    {
    	
        this.theFile = input;
        String whatever = input.getName().substring(0, input.getName().indexOf("."));
        compressedFile = new File(whatever + ".gzip");
    	
    	
    }
    
    /** 
     * Constructor using String input
     * @param String
     */
    public HuffmanEncode(String fileName)
    {
    	this.theFile = new File (fileName);
    	compressedFile = new File(fileName + ".gzip");
    }
    
    /**
     * Reads the FileInputStream and counts the occurrence of every byte.
     * Fills the Priority Queue with the nodes that you create out of the bytes and occurrences.
     * Builds the Huffman Tree
     * Traverses the Huffman Tree 
     */
    public void writeToFile(String fileName)
    {
        
        int currentData;
        try {
            
            FileInputStream inputStream = new FileInputStream(theFile);
            while (inputStream.available() > 0) 
            {
                currentData = inputStream.read();
                //System.out.println(currentData);
                //System.out.println(Integer.toBinaryString(inputStream.read()));
                byteArray.add(currentData);
            } 
            inputStream.close();
        } catch (IOException ioe) {
            System.out.println("Trouble reading from the file: " + ioe.getMessage());
        }
        
        // printing out byteArray to make sure its filled
        //for (int i = 0; i < byteArray.size();i++){
            //System.out.println(Integer.toBinaryString(byteArray.get(i)));
        //}
        // putting bytes into a hashmap based in the char as the key and frequency as the value
        for (int i = 0; i < byteArray.size();i++){
            if (!myFirstMap.containsKey(byteArray.get(i))){
                myFirstMap.put( byteArray.get(i), Collections.frequency(byteArray, byteArray.get(i)) );
                //System.out.println( byteArray.get(i) + " " + myFirstMap.get(byteArray.get(i)));
            }
            //System.out.println(myFirstMap);
        }
        //myFirstMap.forEach((k,v) -> System.out.println(k + "=" + v));
        myFirstMap.forEach((k,v) -> nodeByteQueue.add(new ByteNode(v, k)));
        //System.out.println(nodeByteQueue.peek().frequency + "=" + nodeByteQueue.peek().byteData);
        buildByteTree();
        //printSideways(overallByteRoot, 0);
        traverseByteHuffTree();
        
        //encodedBytesMap.forEach((k,v) -> System.out.println(k + "=" + v));
        
        //write the huffcode tree bytes to file
        
        
        
        // set number of bytes to the file length of bytes
        long numberOfBytes = theFile.length();
        
 
        //System.out.println(wrapperNumberOfBytes.BYTES);
        int numberOfSymbols = myFirstMap.size();

        //System.out.println(wrapperNumberOfBytes);
        //System.out.println(numberOfSymbols);
        //numberOfBytes 
        //byte(s) codeBits;
        try {
            FileOutputStream headerByteStream = new FileOutputStream(compressedFile);
            //System.out.println(wrapperNumberOfBytes.byteValue());
            try {
                
                // Create the long value as bytes
                ByteBuffer myLongByteBuffer = ByteBuffer.allocate(8);
                myLongByteBuffer.putLong(numberOfBytes);
                //System.out.println(myByteBuffer.toString());
                byte[] theLongArray = myLongByteBuffer.array();
                //System.out.println(wrapperNumberOfBytes.BYTES);
                headerByteStream.write(theLongArray);
                
                
                // Create the int value as 4 bytes
                ByteBuffer myIntByteBuffer = ByteBuffer.allocate(4);
                myIntByteBuffer.putInt(numberOfSymbols);
                //System.out.println(myByteBuffer.toString());
                byte[] theIntArray = myIntByteBuffer.array();
                
                headerByteStream.write(theIntArray);
                
                // write the actual Huffman value to bytes and shift over to the left
                // if there are between 8 and bits, then take the first 8 and shift the
                // rest over in the next byte. Possibly will add more up to 64 later.
                for(Map.Entry<Integer, String> entry : encodedBytesMap.entrySet()){
                    headerByteStream.write(entry.getKey());
                    headerByteStream.write(entry.getValue().length());
                    // check if there are more than 8 bits
                    if (entry.getValue().length() <= 8){
                    	byte answer;
                        Byte theValue = Byte.parseByte(entry.getValue(), 2);
                        //System.out.println(entry.getValue());
                        answer = (byte) (theValue << (8 - entry.getValue().length() ))  ;
                        headerByteStream.write(answer);
                    } else if (entry.getValue().length() > 8 && entry.getValue().length() >= 16)  {
                    	byte answer1;
                    	byte answer2;
                    	Byte theValue1 = Byte.parseByte(entry.getValue().substring(0, 8), 2);
                    	Byte theValue2 = Byte.parseByte(entry.getValue().substring(9, (entry.getValue().length() - 8)), 2);
                        //System.out.println(entry.getValue());
                        answer1 = (byte) (theValue1);
                        answer2 = (byte) (theValue2 << (8 - (entry.getValue().length() - 8) ));
                        headerByteStream.write(answer1);
                        headerByteStream.write(answer2);
                        // add in code here later that makes a substring and keeps processing it until its gone I guess.
                        System.out.println("testing a huff code that is longer than 8 bits");
                    }
                    //System.out.printf("Key : %s and Value: %s %n", entry.getKey(), entry.getValue()); 
                }
                
                //|------------------Done with Header-----------------------------|
                
                
                //System.out.println(wrapperNumberOfBytes.byteValue());
                //System.out.println(wrapperNumberOfBytes.BYTES);
                
                
                // helper code to shove bits into complete bytes before adding them.
                int inputData;
                try {
                    
                    FileInputStream inputStream = new FileInputStream(theFile);
                    while (inputStream.available() > 0) 
                    {
                    	inputData = inputStream.read();
                        //System.out.println(currentData);
                        //System.out.println(Integer.toBinaryString(inputStream.read()));
                    	String huffCode = encodedBytesMap.get(inputData);
                    	
                    	
                    	
                    	//|-----------------This is where you left off.  Going to put the bits
                    	// for each code next to each other ----------|
                    	
                    	
                    	
                    	//headerByteStream.write();
                    } 
                    inputStream.close();
                } catch (IOException ioe) {
                    System.out.println("Trouble reading from the file: " + ioe.getMessage());
                }
                
               
                               
                /**
                 *  //checking if the file is writing and storing correctly.
                if (compressedFile.canRead()){
                    //System.out.println(compressedFile.length());
                    FileInputStream compressedFileStream = new FileInputStream(compressedFile);
                    
                    System.out.println(compressedFileStream.read());
                    System.out.println(compressedFileStream.read());
                    System.out.println(compressedFileStream.read());
                    System.out.println(compressedFileStream.read());
                    System.out.println(compressedFileStream.read());
                    System.out.println(compressedFileStream.read());
                    System.out.println(compressedFileStream.read());
                    System.out.println(compressedFileStream.read());
                    System.out.println(compressedFileStream.read());
                    System.out.println(compressedFileStream.read());
                    System.out.println(compressedFileStream.read());
                    System.out.println(compressedFileStream.read());
                    System.out.println((char)(compressedFileStream.read()));
                    System.out.println(compressedFileStream.read());
                    System.out.println(Integer.toBinaryString(compressedFileStream.read()));
                    
                    System.out.println((char)(compressedFileStream.read()));
                    System.out.println(compressedFileStream.read());
                    System.out.println(Integer.toBinaryString(compressedFileStream.read()));
                    
                    System.out.println((char)(compressedFileStream.read()));
                    System.out.println(compressedFileStream.read());
                    System.out.println(Integer.toBinaryString(compressedFileStream.read()));
                    
                    System.out.println((char)(compressedFileStream.read()));
                    System.out.println(compressedFileStream.read());
                    System.out.println(Integer.toBinaryString(compressedFileStream.read()));
                    
                    System.out.println((char)(compressedFileStream.read()));
                    System.out.println(compressedFileStream.read());
                    System.out.println(Integer.toBinaryString(compressedFileStream.read()));
                    
                    System.out.println((char)(compressedFileStream.read()));
                    System.out.println(compressedFileStream.read());
                    System.out.println(Integer.toBinaryString(compressedFileStream.read()));
                    
                    compressedFileStream.close();

                }
                
                */
                
                headerByteStream.close();
            } catch (IOException ioe) {
                System.out.println("Trouble writing to the output byte stream" + ioe.getMessage());
            }
            
            
        } catch (FileNotFoundException fnfe){
            System.out.println("No File to write to: " + fnfe.getMessage());
        }
        //System.out.println();
        //int bytesOfLongTest = numberOfBytes.byteValue();
        //System.out.println(wrapperNumberOfBytes.byteValue());
        //headerByteStream.write(wrapperNumberOfBytes.byteValue());
  
    }
    
    private int pushCharCodeIntoContainer(int byteContainer, String code) {

        int newContainer = byteContainer;

        for (int i = 0; i <= code.length() - 1; i++) {

            newContainer *= 2;

            if ('1' == code.charAt(i)) {

                newContainer += 1;

            }

        }

        return newContainer;

    }
   
    //Builds the Huffman Tree for ByteNodes
    private void buildByteTree(){
        while (nodeByteQueue.size() > 1) 
        {
            ByteNode newParent = new ByteNode();
            newParent.left = nodeByteQueue.poll();
            newParent.right = nodeByteQueue.poll();
            newParent.frequency = newParent.right.frequency + newParent.left.frequency;
            nodeByteQueue.add(newParent);
        }
        if (nodeByteQueue.size() == 1)
        {
            overallByteRoot = nodeByteQueue.poll();
        }
    }
    
    private class ByteNode implements Comparable<ByteNode> 
    {
        ByteNode right;
        ByteNode left;
        int frequency;
        int byteData;
        public ByteNode()
        {
            this.right = null;
            this.left = null;
            this.frequency = 0;
        }
        public ByteNode(int frequency, int byteData)
        {
            this.frequency = frequency;
            this.byteData = byteData;
        }
        public ByteNode(int frequency, ByteNode left, ByteNode right)
        {
            this.frequency = frequency;
            this.left = left;
            this.right = right;
        }
        public int compareTo(ByteNode other)
        {
            int answer = 0;
            if (this.frequency == other.frequency)
            {
                answer = 0;
            } 
            else if (this.frequency > other.frequency)
            {
                answer = 1;
            }
            else if (this.frequency < other.frequency)
            {
                answer = -1;
            }
            return answer;
        }
        public boolean equals(ByteNode other) 
        {
            if (this.frequency == other.frequency && this.byteData == other.byteData)
            {
                return true;
            } 
            else
            {
                return false;
            }
        }
    }
    
    private class CharNode implements Comparable<CharNode> 
    {
        CharNode right;
        CharNode left;
        int frequency;
        char character;
        public CharNode()
        {
            this.right = null;
            this.left = null;
            this.frequency = 0;
            this.character = '\u0000';
        }
        public CharNode(int frequency, char character)
        {
            this.frequency = frequency;
            this.character = character;
        }
        public CharNode(int frequency, CharNode left, CharNode right)
        {
            this.frequency = frequency;
            this.left = left;
            this.right = right;
        }
        public int compareTo(CharNode other)
        {
            int answer = 0;
            if (this.frequency == other.frequency)
            {
                answer = 0;
            } 
            else if (this.frequency > other.frequency)
            {
                answer = 1;
            }
            else if (this.frequency < other.frequency)
            {
                answer = -1;
            }
            return answer;
        }
        public boolean equals(CharNode other) 
        {
            if (this.frequency == other.frequency && this.character == other.character)
            {
                return true;
            } 
            else
            {
                return false;
            }
        }
    }
    
    
    /**
     * A method to encode a file
     * 
     * 
     */
    public void encode()
    {
        // Reads the File Object and counts the occurrence 
        // of every character (including special characters like newline and blanks).
        try 
        {
            
            BufferedReader in = new BufferedReader(new FileReader(theFile));
            int readValue = in.read();
            characters.add((char)readValue);
            
            // Load the chars into the array
            while (readValue != -1)
            {
                readValue = in.read();
                characters.add((char)readValue);
            }
            in.close();

        } 
        catch (IOException FileNotFoundException)
        { 
            System.out.println("file not found");
        }
        uniqueToArrayList();
        //checkUniques();
        fillQueue();
        buildTree();
        //printSideways(overallRoot, 0);
        traverseHuffTree();
    
    }
    
    /**
     * helper method for me to check my work.
     */
    private void checkUniques()
    {
        for (int i = 0; i < uniqueChars.size(); i ++ )
        {
            System.out.println(uniqueChars.get(i));
        }
    }
    
    
    /**
     * Helper method to put the unique characters into an array
     */
    private void uniqueToArrayList() 
    {
        for (int i = 0; i < characters.size(); i ++ )
        {
            if (!uniqueChars.contains(characters.get(i)))
            {
                uniqueChars.add(characters.get(i));
            }
        }
    }
    
    /**
     * Fills the Priority Queue with the nodes that you create
     * out of the characters and occurrences. 
     */
    private void fillQueue()
    {
        for (int i = 0; i < uniqueChars.size(); i++) 
        {   
            CharNode newNode = new CharNode( Collections.frequency(characters, uniqueChars.get(i)), uniqueChars.get(i));
            nodeQueue.add(newNode);
        }
    }
    

    /**
     * Uses a priority queue to build the tree
     */
    private void buildTree(){
        while (nodeQueue.size() > 1) 
        {
            CharNode newParent = new CharNode();
            newParent.left = nodeQueue.poll();
            newParent.right = nodeQueue.poll();
            newParent.frequency = newParent.right.frequency + newParent.left.frequency;
            nodeQueue.add(newParent);
        }
        if (nodeQueue.size() == 1)
        {
            overallRoot = nodeQueue.poll();
        }
    }
    
    //Traverses the Huffman Tree
    private void traverseHuffTree()
    {
        leftOrderTraversal(overallRoot);
    }
    
    //Traverses the Huffman Tree
    private void traverseByteHuffTree()
    {
        leftOrderTraversal(overallByteRoot);
    }
    
    
    // helper method to check tree
    private void printSideways(CharNode root, int level) {
        if (root != null) {
            printSideways(root.right, level + 1);
            for (int i = 0; i < level; i++) {
                System.out.print("    ");
            }
            System.out.println(root.frequency + " " + root.character);
            printSideways(root.left, level + 1);
        }
    }
    
    /**
     * Display the tree sideways	
     * @param root
     * @param level
     */
    private void printSideways(ByteNode root, int level) {
        if (root != null) {
            printSideways(root.right, level + 1);
            for (int i = 0; i < level; i++) {
                System.out.print("    ");
            }
            System.out.println(root.frequency + " " + root.byteData);
            printSideways(root.left, level + 1);
        }
    }
    
    // private traversing method for ByteNode trees
    private void leftOrderTraversal(ByteNode root)
    {
        if (root.right == null && root.left == null)
        {
            //System.out.println(root.byteData + " " + traverser + " " + root.frequency);
            encodedBytesMap.put(root.byteData, traverser);
        } 
        else if  (root != null)
        {
            traverser += "0";
            leftOrderTraversal(root.left);
            traverser = traverser.substring(0, traverser.length()-1);
        
            traverser += "1";
            leftOrderTraversal(root.right);
            traverser = traverser.substring(0, traverser.length()-1);
        }
    }

    
    
    /**
     * Used to traverse through the tree with left order
     * @param root
     */
    private void leftOrderTraversal(CharNode root) {
        
            if (root.right == null && root.left == null)
            {
                System.out.println(root.character + " " + traverser + " " + root.frequency);
            } 
            else if  (root != null)
            {
                traverser += "0";
                leftOrderTraversal(root.left);
                traverser = traverser.substring(0, traverser.length()-1);
            
                traverser += "1";
                leftOrderTraversal(root.right);
                traverser = traverser.substring(0, traverser.length()-1);
            
            
            }
        
        /**
        if (root.right == null && root.left == null)
        {
            System.out.println(root.left.character + " " + traverser + " " + root.left.frequency);
        } 
        else
        {
            traverser += "0";
            leftOrderTraversal(root.left);
            traverser += "1";
            leftOrderTraversal(root.right);
        }
        */
        /**
        if (root.left != null) 
        {
            if (root.left.right == null && root.left.left == null)
            {
                System.out.println(root.left.character + " " + traverser + " " + root.left.frequency);
            }
             {
                traverser += "0";
                leftOrderTraversal(root.left);
            }
        }
        if (root.right != null) 
        {
            if (root.right.right == null && root.right.left == null)
            {
                System.out.println(root.right.character + " " + traverser + " " + root.right.frequency);
            }
            else
            {
                traverser += "1";
                leftOrderTraversal(root.right);
            }
        }
        */
        
    }
}
