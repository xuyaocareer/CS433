


import java.io.IOException;
import java.util.Scanner;


public class Drive {

	public static void main(String[] args) throws IOException {
		// part1
		// output the dictionary.txt and postings.txt, used as the input for query
	 	String fileIn = "200_content.txt";
		String fileOut1 = "dictionary.txt";
		String fileOut2 = "postings.txt";
		
		dict_part1 d_part1 = new dict_part1();
		d_part1.input(fileIn);
		d_part1.output(fileOut1, fileOut2);
		
	
		// part2
		// query part
		String fileIn1 = "dictionary.txt";
		String fileIn2 = "postings.txt";
		dict_part2 d_part2 = new dict_part2(fileIn1, fileIn2, d_part1.getDocID_max());
		
		int n = 1;
		Scanner in=new Scanner(System.in);
		while (in.hasNextLine()) {
			String query_in = in.nextLine();
			if (query_in.equals("q")) // input "q" and exit
				break;
			d_part2.query(query_in, n);
			n++;
		}
	}
}
