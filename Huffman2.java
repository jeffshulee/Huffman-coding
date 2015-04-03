import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Comparator;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.TreeMap;

import javax.swing.JFileChooser;

public class Huffman2 {
	
	public static String getFilePath() {		 // method provided for lab
	    JFileChooser fc = new JFileChooser(".");  
	    int returnVal = fc.showOpenDialog(null);
	    if(returnVal == JFileChooser.APPROVE_OPTION) {
	        File file = fc.getSelectedFile();
	        String pathName = file.getAbsolutePath();
	        return pathName;
	    }
	    else {
	        return "";  
	    }
	}
	// Create a map with characters as keys and frequencies as values. 
	private Map<Character, Integer> FrequencyMap(String file) throws IOException{			
		Map<Character, Integer> map = new TreeMap<Character, Integer>(); 	//read more on treemap

		BufferedReader in = new BufferedReader(new FileReader(file));
		int line;	// BufferedReader reads character as int
		while ((line = in.read()) != -1){ 	//Read the file one character at a time, add to map if it's not in there and increment
			char charKey = (char) line;  	
		
			if (map.containsKey(charKey)) {
				// Increment the count
				map.put(charKey, map.get(charKey)+1);
			}
			else {
				// Add the new word
				map.put(charKey, 1);
			}
		}
		in.close();
		return map;	
	}
	// Create a priority queue that sorts the map of characters and frequencies into ordered queue of singleton (c,f) pairs using comparator
	private PriorityQueue<BinaryTree<NodeData>> queueSingleton(Map<Character, Integer> FrequencyMap){		
		int initCapacity = FrequencyMap.size();
		// create an anonymous class within priority queue
		Comparator<BinaryTree<NodeData>> compareQueue = new Comparator<BinaryTree<NodeData>>() {		//where do i use the compae method?
			public int compare(BinaryTree<NodeData> n1, BinaryTree<NodeData> n2) {	//where do i pass in specific nodes of n1, n2?
				//return n1.getData().getFreq() - n2.getData().getFreq();
				if(n1.getData().getFreq() > n2.getData().getFreq()) 
					return 1;
				else if(n1.getData().getFreq() < n2.getData().getFreq()) 
					return -1;
				else
					return 0;
			}
		};	//this semicolon for inner class, syntax?
		
		// Make an instance of PriorityQueue<BinaryTree<NodeData>> called pq following the constructor format below:
		// PriorityQueue(int initialCapacity, Comparator<? super E> comparator), creating a PriorityQueue w/ 
		//specified initial capacity that orders its elements according to the specified comparator.
		PriorityQueue<BinaryTree<NodeData>> pq = new PriorityQueue<BinaryTree<NodeData>>(initCapacity, compareQueue); 
 
		for (char key : FrequencyMap.keySet()){	// for each character in the set of the keys contained in this map
			pq.add(new BinaryTree<NodeData>(new NodeData(key, FrequencyMap.get(key)))); // add the singleton node pair of (character,frequency)
		}
		return pq;
	}
	/** 
	 * Create a prioritized BinaryTree from a priority queue of singleton trees
	 * @param queueMap
	 * @return
	 */
	public BinaryTree<NodeData> newTree(PriorityQueue<BinaryTree<NodeData>> queueMap){
		if (queueMap.size() == 1) {		// this is the special case where you have only one character 
			BinaryTree<NodeData> T1 = queueMap.poll();	// poll() removes and return the head of this queue, or returns null if queue is empty.
			BinaryTree<NodeData> rootNode = new BinaryTree<NodeData>(new NodeData('\0',T1.getData().getFreq()), null, T1); // the root node's sum is just T1's frequency 
			queueMap.add(rootNode);
			return queueMap.poll();
		}
		while (queueMap.size()>1){ // tree is compiled when it is the only element left in queueMap
		
			BinaryTree<NodeData> T1 = queueMap.poll();	// poll() removes and return the head of this queue, or returns null if queue is empty.
			BinaryTree<NodeData> T2 = queueMap.poll();
			BinaryTree<NodeData> rootNode = new BinaryTree<NodeData>(new NodeData('\0',T1.getData().getFreq()+ T2.getData().getFreq()), T1, T2 );
			queueMap.add(rootNode);
		}
		return queueMap.poll();			
	}
	
	public Map<Character, String> codeMap(BinaryTree<NodeData> tree)  {
		Map<Character, String> encodingList = new TreeMap<Character, String>();		// tree map because it's a map of binary tree
		traverseRecurse(tree, encodingList, "");
		return encodingList;
	}
		
	public static void traverseRecurse(BinaryTree<NodeData> tree, Map<Character, String> encodingList, String pathSoFar) {  

		if (tree.hasLeft()){
			//pathSoFar.concat("0");
			traverseRecurse(tree.getLeft(), encodingList, pathSoFar+0);
		}
		if (tree.hasRight()){
			//pathSoFar.concat("1");
			traverseRecurse(tree.getRight(), encodingList, pathSoFar+1);
		}
		if (tree.isLeaf()){		//base case
			char c = tree.getData().getCh();
			encodingList.put(c, pathSoFar);
		}
	}

	public void compressor( String filename, String compressedPathName,Map<Character, String> map) throws IOException {
		
			BufferedBitWriter bitOutput = new BufferedBitWriter(compressedPathName);
			BufferedReader in = new BufferedReader(new FileReader(filename));
			int line;	// read character as int
		
			try{	
			while ((line = in.read()) != -1){ 
				char charKey = new Character((char) line); 	// convert from int to char

					String s = map.get(charKey);  // get bit string of a charkey
					
					for (int i = 0; i < s.length();i++){	
						int bit = s.charAt(i) ;
						if (bit == '1'){
							bitOutput.writeBit(1);	// write 1s and 0s to a file. Since bitWriter can only take 1 or 0 at a time, we loop through each bit of the bitstring
						}
						else{
							bitOutput.writeBit(0);
						}
					}
				}
			bitOutput.close();
			in.close();
			}
			catch(IOException e){
				System.err.println("File error");
			}
	}

		public void decompression(String compressedPathName, String deCompressedPathName, String filename, BinaryTree<NodeData> binTree) throws IOException{
			BufferedBitReader bitInput = new BufferedBitReader(compressedPathName);
			BufferedWriter output = new BufferedWriter(new FileWriter(deCompressedPathName));
			BinaryTree<NodeData> x = binTree;		// binTree is linked to the very top of the tree
			int current = bitInput.readBit();
			
			while(current != -1){			// while there is bit string to read
				if (!x.isLeaf()){			
					
					if (current == 0){
						x= x.getLeft();
					}
					if (current == 1){
						x= x.getRight();
					}
					current = bitInput.readBit(); // read the next bit of the string
				}
				else{
				   //getData().getCh();
					output.write(x.getData().getCh());
					x = binTree;		// put x back at the root
				}
			}
			bitInput.close();
			output.close();
		}
		
public static void main(String[] args) {
	try{	// calling methods that can throw an exception requires handling them by catching
		String file = getFilePath();
		Huffman2 ft = new Huffman2();			
		
		Map<Character, Integer> inputFrequencyMap = ft.FrequencyMap(file); 
		PriorityQueue<BinaryTree<NodeData>> inputQueueMap = ft.queueSingleton(inputFrequencyMap);
		BinaryTree<NodeData> tree = ft.newTree(inputQueueMap); 
		Map<Character, String> inputMap = ft.codeMap(tree);
		
		String compressedPathName = file.substring(0, file.length()-4) + "_compressed.txt";			//delete the ".txt" of the original file
		ft.compressor(file, compressedPathName,inputMap);
		
		String deCompressedPathName = file.substring(0, file.length()-4) + "_decompressed.txt";
		ft.decompression(compressedPathName,deCompressedPathName, file, tree);
	}	catch(IOException e){
		System.err.println("Trouble reading this file!");
	}
}
}
