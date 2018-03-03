package src;

import java.io.*;
import java.nio.*;
import java.util.*;

/**
 * This program encodes a file given. Uses the Huffman algorithm.
 * 
 * @author Grant Zukowski
 * @version 1.2
 */
public class HuffmanEncode {
	private File theFile;
	private File compressedFile;
	private ArrayList<Character> characters = new ArrayList<Character>();
	private ArrayList<Character> uniqueChars = new ArrayList<Character>();
	
	private PriorityQueue<CharNode> nodeQueue = new PriorityQueue<CharNode>();
	
	
	private HashMap<Character, Integer> charFrequencyMap = new HashMap<Character, Integer>();
	private CharNode overallRoot;

	private String traverser = "";
	
	private HashMap<Integer, Integer> myFirstMap = new HashMap<Integer, Integer>();
	private HashMap<Character, String> encodedCharacterMap = new HashMap<Character, String>();

	/**
	 * Constructor for objects of class HuffmanEncode to start with a file also
	 * creates the gzip file from the input file name.
	 * 
	 * @param File
	 */
	public HuffmanEncode(File input) {

		this.theFile = input;
		String whatever = input.getName().substring(0, input.getName().indexOf("."));
		compressedFile = new File(whatever + ".gzip");

	}

	/**
	 * Constructor using String input
	 * 
	 * @param String
	 */
	public HuffmanEncode(String fileName) {
		this.theFile = new File(fileName);
		compressedFile = new File(fileName + ".gzip");
	}

	/**
	 * A method to encode a file Reads the File Object and counts the occurrence of
	 * every character (including special characters like newline and blanks).
	 * Use is just to create a tree with values and frequencies
	 */
	public void encode() {
		BufferedReader inputReader = null;

		// open the file
		try {
			// use side method so it throws the error
			// and I can explain here
			inputReader = createBufferedReader();
			System.out.println("file was opened!");
		} catch (FileNotFoundException e) {

			e.printStackTrace();
		}

		try {
			// use side method so it throws the error
			loadCharArray(inputReader);
			System.out.println("File read!");
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("File was opened, but data could not be read");
		}

		uniqueToArrayList();
		
		// private method to check my work, can add to tests later
		// checkUniques();
		
		
		fillQueue();
		
		buildTree();
		
		//Method to check if the tree was constructed correctly
		printSideways(overallRoot, 0);
		
		//Call this here to fill up the hashmap of the character and new encoding
		//value if you choose to write the bits using a hash.
		traverseHuffTree();

	}

	/**
	 * Method to create the fileReader to hide the error throwing from the user
	 * having to handle it
	 * 
	 * @throws FileNotFoundException
	 */
	private BufferedReader createBufferedReader() throws FileNotFoundException {
		BufferedReader inputReader = null;
		inputReader = new BufferedReader(new FileReader(theFile));
		return inputReader;
	}

	/**
	 * Private method for reading the BufferedReader to load the data from the file
	 * into an array
	 * 
	 * @throws IOExceptiion
	 */
	private void loadCharArray(BufferedReader inputReader) throws IOException {
		int readValue = 0;
		System.out.println("reading the file");

		// prime the while reading loop
		readValue = inputReader.read();

		// add the first value and cast it as you add
		characters.add((char) readValue);

		// Load the chars into the array
		while (readValue != -1) {
			readValue = inputReader.read();
			characters.add((char) readValue);
		}

		inputReader.close();
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

	/**
	 * Class is only used in this program, useful because
	 * can implement Comparable so priority queue can keep largest 
	 * Node at top of queue for making tree
	 * @author grant
	 *
	 */
	private class CharNode implements Comparable<CharNode> {
		CharNode right;
		CharNode left;
		int frequency;
		char character;

		public CharNode() {
			this.right = null;
			this.left = null;
			this.frequency = 0;
			this.character = '\u0000';
		}

		public CharNode(int frequency, char character) {
			this.frequency = frequency;
			this.character = character;
		}

		public CharNode(int frequency, CharNode left, CharNode right) {
			this.frequency = frequency;
			this.left = left;
			this.right = right;
		}

		public int compareTo(CharNode other) {
			int answer = 0;
			if (this.frequency == other.frequency) {
				answer = 0;
			} else if (this.frequency > other.frequency) {
				answer = 1;
			} else if (this.frequency < other.frequency) {
				answer = -1;
			}
			return answer;
		}

		public boolean equals(CharNode other) {
			if (this.frequency == other.frequency && this.character == other.character) {
				return true;
			} else {
				return false;
			}
		}
	}

	/**
	 * helper method for me to check my work.
	 */
	private void checkUniques() {
		for (int i = 0; i < uniqueChars.size(); i++) {
			System.out.println(uniqueChars.get(i));
		}
	}

	/**
	 * Helper method to put the unique characters into an array
	 */
	private void uniqueToArrayList() {
		for (int i = 0; i < characters.size(); i++) {
			if (!uniqueChars.contains(characters.get(i))) {
				uniqueChars.add(characters.get(i));
			}
		}
	}

	/**
	 * Fills the Priority Queue with the nodes that you create out of the characters
	 * and occurrences.
	 */
	private void fillQueue() {
		for (int i = 0; i < uniqueChars.size(); i++) {
			CharNode newNode = new CharNode(Collections.frequency(characters, uniqueChars.get(i)), uniqueChars.get(i));
			charFrequencyMap.put(uniqueChars.get(i), Collections.frequency(characters, uniqueChars.get(i)));
			nodeQueue.add(newNode);
		}
	}

	/**
	 * Uses a priority queue to build the tree
	 */
	private void buildTree() {
		while (nodeQueue.size() > 1) {
			CharNode newParent = new CharNode();
			newParent.left = nodeQueue.poll();
			newParent.right = nodeQueue.poll();
			newParent.frequency = newParent.right.frequency + newParent.left.frequency;
			nodeQueue.add(newParent);
		}
		if (nodeQueue.size() == 1) {
			overallRoot = nodeQueue.poll();
		}
	}

	/**
	 * Wrapper method to call a traversal on the tree's leaves.
	 * so I can fill the map with values //Perhaps the values are being set incorrectly 
	 * by "traverser string"
	 */
	private void traverseHuffTree() {
		leftOrderTraversal(overallRoot);
	}

	/**
	 * Reads the FileInputStream and counts the occurrence of every byte. Fills the
	 * Priority Queue with the nodes that you create out of the bytes and
	 * occurrences. Builds the Huffman Tree Traverses the Huffman Tree
	 */
	public void writeToFile(String fileName) {

		int currentData;
		
		//Write the header to be able to re-construct the tree later
		//something that I can call buildTree on, so all the components of that
		//May just convert the entire structure to bytes, and write it that way
		//honestly still not positive on this part, just know the tree will be very useful
		//for decoding the byte stream.
		
		//Data needed for the tree
		
		
		//going to write the bits while I traverse the tree so when I'm writing the byte
		//and have to go to the next, I just leave the pointer in place and keep traversing
		//because I think this will be faster than dealing with bit shifting
		
		
		
		//get these from the map
		int numberOfBytes = 0;
		int numberOfSymbols = 0;
		
		
		
		
		FileOutputStream headerByteStream = null;
		
		try {

			headerByteStream = new FileOutputStream(compressedFile);
			// System.out.println(wrapperNumberOfBytes.byteValue());
		} catch (FileNotFoundException e) {
			System.out.println(e.getMessage());
		}
		
		
		
		// Create the long value as bytes
		ByteBuffer myLongByteBuffer = ByteBuffer.allocate(8);
		myLongByteBuffer.putLong(numberOfBytes);
		// System.out.println(myByteBuffer.toString());
		byte[] theLongArray = myLongByteBuffer.array();
		// System.out.println(wrapperNumberOfBytes.BYTES);
		
		try {
			headerByteStream.write(theLongArray);
		} catch (IOException e) {
			System.out.println("Sorry, could not write to the header byte stream,\n perhaps ");
			System.out.println(e.getMessage());
		}
		// Create the int value as 4 bytes
		ByteBuffer myIntByteBuffer = ByteBuffer.allocate(4);
		myIntByteBuffer.putInt(numberOfSymbols);
		// System.out.println(myByteBuffer.toString());
		byte[] theIntArray = myIntByteBuffer.array();
		
		try {
		
			headerByteStream.write(theIntArray);
		
			// write the actual Huffman value to bytes and shift over to the left
			// if there are between 8 and bits, then take the first 8 and shift the
			// rest over in the next byte. Possibly will add more up to 64 later.
		
			// This seems to add bytes to the header to be written so I have the 
			// map when I decode.
			
			// I"m leaving this for now because this is all the header and the main
			// part that I want to do is not done anyways, but I will have to remember to 
			// populate the encodedBytesMap in the traversal
			
			/**
			for (Map.Entry<Integer, String> entry : encodedBytesMap.entrySet()) {
				headerByteStream.write(entry.getKey());
				headerByteStream.write(entry.getValue().length());
				// check if there are more than 8 bits
				if (entry.getValue().length() <= 8) {
					byte answer;
					Byte theValue = Byte.parseByte(entry.getValue(), 2);
					// System.out.println(entry.getValue());
					answer = (byte) (theValue << (8 - entry.getValue().length()));
					headerByteStream.write(answer);
				} else if (entry.getValue().length() > 8 && entry.getValue().length() >= 16) {
					byte answer1;
					byte answer2;
					Byte theValue1 = Byte.parseByte(entry.getValue().substring(0, 8), 2);
					Byte theValue2 = Byte.parseByte(entry.getValue().substring(9, (entry.getValue().length() - 8)),
							2);
					// System.out.println(entry.getValue());
					answer1 = (byte) (theValue1);
					answer2 = (byte) (theValue2 << (8 - (entry.getValue().length() - 8)));
					headerByteStream.write(answer1);
					headerByteStream.write(answer2);
					// add in code here later that makes a substring and keeps processing it until
					// its gone I guess.
					System.out.println("testing a huff code that is longer than 8 bits");
				}
				// System.out.printf("Key : %s and Value: %s %n", entry.getKey(),
				// entry.getValue());
			}
			*/
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		// |------------------Done with Header-----------------------------|

		// System.out.println(wrapperNumberOfBytes.byteValue());
		// System.out.println(wrapperNumberOfBytes.BYTES);
		
		// Grab each character of the input file
		
		int inputData;
		try {

			FileInputStream inputStream = new FileInputStream(theFile);
			while (inputStream.available() > 0) {
				inputData = inputStream.read();
				
				// Smallest unit I can write is byte
				byte[] encodedByte = {0,0,0,0,0,0,0,0};
				int counter = 0;
				
				// Get frequency so I can use it to traverse tree
				int frequency = charFrequencyMap.get((char) inputData);
				
				String encodedValueAsString = encodedCharacterMap.get((char) inputData);
				
				//Picking up here, going to fill up the bytes and write them when they equal 8
				// using a pointer for index
				
				if (encodedValueAsString.length() > 9) {
					encodedByte = encodedValueAsString.getBytes();
				}
				
				// Set byte as I turn left and right
				// +_________________-----------Left off here
				// This idea may not work because its hard to navigate to 
				// the leaf with the frequency, I don't see the pattern
				// unless my tree is incorrectly constructed. Won't work because
				// can't tell the difference between two characters with same frequency
				
				// Now I have a map of the encoded values. 
				
				
				//System.out.println((char) inputData);
				
				// System.out.println(currentData);
				// System.out.println(Integer.toBinaryString(inputStream.read()));
				//String huffCode = encodedBytesMap.get(inputData);

				if (counter == 8) {

					headerByteStream.write(encodedByte);
				}
			}
			inputStream.close();
		} catch (IOException ioe) {
			System.out.println("Trouble reading from the file: " + ioe.getMessage());
		}


		try {
			headerByteStream.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * Useful for decoding and encoded file
	 */
	
	public void decode() {
		
		/**
		 * May use this code for the decode
		 * 
		 * CharNode pointer = overallRoot;
		
		
		if (pointer.right == null && pointer.left == null) {
			pointer = overallRoot;
		}
		else if (pointer.right.frequency > frequency) {
			pointer = pointer.right;
			encodedByte = (byte) (encodedByte | (0 << counter));
			counter += 1;
		} else if (pointer.right.frequency > frequency) {
			
		}
		 */
		
	}


	/**
	 * Useful for getting a sideways visual representation
	 * of the tree, leaves are the characters.
	 * @param root
	 * @param level
	 */
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
	 * Used to traverse through the tree leaves with left order
	 * traversal
	 * 
	 * May use this method to set up a map of characters and their encoding
	 * 
	 * @param root
	 */
	private void leftOrderTraversal(CharNode root) {

		if (root.right == null && root.left == null) {
			System.out.println(root.character + " " + traverser + " " + root.frequency);
			// So I can use these values later to write the encoded file
			encodedCharacterMap.put(root.character, traverser);
			
		} else if (root != null) {
			traverser += "0";
			leftOrderTraversal(root.left);
			traverser = traverser.substring(0, traverser.length() - 1);

			traverser += "1";
			leftOrderTraversal(root.right);
			traverser = traverser.substring(0, traverser.length() - 1);

		}

		/**
		 * if (root.right == null && root.left == null) {
		 * System.out.println(root.left.character + " " + traverser + " " +
		 * root.left.frequency); } else { traverser += "0";
		 * leftOrderTraversal(root.left); traverser += "1";
		 * leftOrderTraversal(root.right); }
		 */
		/**
		 * if (root.left != null) { if (root.left.right == null && root.left.left ==
		 * null) { System.out.println(root.left.character + " " + traverser + " " +
		 * root.left.frequency); } { traverser += "0"; leftOrderTraversal(root.left); }
		 * } if (root.right != null) { if (root.right.right == null && root.right.left
		 * == null) { System.out.println(root.right.character + " " + traverser + " " +
		 * root.right.frequency); } else { traverser += "1";
		 * leftOrderTraversal(root.right); } }
		 */

	}
}
