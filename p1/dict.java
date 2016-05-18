
import java.io.*;
import java.util.Iterator;
import java.util.TreeMap;

public class dict {
	public TreeMap<String, term> terms_list;
	
	public dict() {
		terms_list = new TreeMap<String, term>();
	}
	
	public void insert(String term_in, int docID_in) {
		if (!terms_list.containsKey(term_in)) {
			term temp = new term(term_in, docID_in);
			terms_list.put(term_in, temp);
		}	
		else {
			terms_list.get(term_in).insert_posting(docID_in);
		}
	}
	
	public String tokenizer(String term_in) {
		String term_str = term_in;
		/*
		String replace_oldChar[] = {"\\,", "\\.", "\\?", "\\$", "\\%"};
		for (int i=0; i<replace_oldChar.length; i++) {
			term_str = term_str.replaceAll(replace_oldChar[i], "");
		}
		*/
		term_str = term_str.replaceAll("[^\\w|\\&]", ""); // remove all symbols except numbers and letters
		term_str = term_str.replaceAll("[0-9]", ""); // remove numbers
		term_str = term_str.toLowerCase();
		
		return term_str;
	}
	
	public String stemmer_term(String term_in) {
		String term_str = term_in;
		Stemmer s = new Stemmer();
		java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("[a-zA-Z]+"); 
        java.util.regex.Matcher m = pattern.matcher(term_str);
        if (m.matches()==true) {
        	for (int i=0; i<term_str.length(); i++) {
        		s.add(term_str.charAt(i));
        	}
        	s.stem();
        	term_str = s.toString();
        }
        return term_str;
	}
	
	public void input(String fileIn) throws IOException {
		File fin =  new File(fileIn);
		if (!fin.exists())
			throw new FileNotFoundException();
		BufferedReader br = new BufferedReader(new FileReader(fin));
		
		int docID = 0;
		String readLine = null;
		while((readLine = br.readLine()) != null) {
			docID++;
			
			String[] word=readLine.split(" ");
			for (int i=0; i<word.length; i++) {
				word[i] = tokenizer(word[i]);
				word[i] = stemmer_term(word[i]);
				if (word[i].length() >= 2) // remove single letter
					insert(word[i], docID);
			}
		}
		br.close();
	}

	public void output(String fileOut1, String fileOut2) throws IOException {
		File fout1 =  new File(fileOut1);
		File fout2 =  new File(fileOut2);

		BufferedWriter br1 = new BufferedWriter(new FileWriter(fout1));
		BufferedWriter br2 = new BufferedWriter(new FileWriter(fout2));
		
		int offset = 0;

		Iterator<String> iter = terms_list.keySet().iterator();
		while (iter.hasNext()) {
			Object key = iter.next();
			term term_temp = terms_list.get(key);
			
			br1.write(term_temp.getTerm_str());
			br1.write(" ");
			br1.write(String.valueOf(term_temp.getDf()));
			br1.write(" ");
			br1.write(String.valueOf(offset));
			br1.newLine();
			br1.flush();

			Iterator<Integer> iter_posting = term_temp.getPostings_list().keySet().iterator();
			while (iter_posting.hasNext()) {
				Object key_posting = iter_posting.next();
				br2.write(String.valueOf(key_posting));
				br2.write(" ");
				br2.write(String.valueOf(term_temp.getPostings_list().get(key_posting)));
				br2.newLine();
				br2.flush();
			}
			offset = offset + term_temp.getDf();
		}
		br1.close();
		br2.close();
	}
}
