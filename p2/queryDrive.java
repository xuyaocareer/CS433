

import java.io.IOException;
import java.util.Scanner;

public class queryDrive {

	public static void main(String[] args) throws IOException {
		String fileIn1 = "dictionary.txt";
		String fileIn2 = "postings.txt";
		
		dict dictionary = new dict(fileIn1, fileIn2);
		
		int n = 1;
		Scanner in=new Scanner(System.in);
		while (in.hasNextLine()) {
			String query_in = in.nextLine();
			if (query_in.equals("q")) // input "q" and exit
				break;
			dictionary.query(query_in, n);
			n++;
		}
		
	}
}
