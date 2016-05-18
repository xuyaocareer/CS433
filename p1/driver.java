
import java.io.IOException;

public class driver {

	public static void main(String[] args) throws IOException {
		String fileIn = "200_title.txt";
		String fileOut1 = "dictionary.txt";
		String fileOut2 = "postings.txt";
		
		dict dictionary = new dict();
		dictionary.input(fileIn);
		dictionary.output(fileOut1, fileOut2);
	}
}
