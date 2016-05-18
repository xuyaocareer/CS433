

import java.io.*;
import java.util.Iterator;
import java.util.TreeMap;

public class dict_part1 {
	private TreeMap<String, term_part1> terms_list;
	private int docID_max;
	
	public dict_part1() {
		terms_list = new TreeMap<String, term_part1>();
		docID_max = 0;
	}
	
	private void insert(String term_in, int docID_in) {
		if (!terms_list.containsKey(term_in)) {
			term_part1 temp = new term_part1(term_in, docID_in);
			terms_list.put(term_in, temp);
		}	
		else {
			terms_list.get(term_in).insert_posting(docID_in);
		}
	}
	
	private String tokenizer(String term_in) {
		String term_str = term_in;
		/*
		String replace_oldChar[] = {"\\,", "\\.", "\\?", "\\$", "\\%"};
		for (int i=0; i<replace_oldChar.length; i++) {
			term_str = term_str.replaceAll(replace_oldChar[i], "");
		}
		*/
		term_str = term_str.replaceAll("[^0-9a-zA-Z]", ""); // remove all symbols except numbers and letters
		//term_str = term_str.replaceAll("[0-9]", ""); // remove numbers
		term_str = term_str.toLowerCase();
		//System.out.println(term_str);
		return term_str;
	}
	
	private String stemmer_term(String term_in) {
		String term_str = term_in;
		Stemmer s = new Stemmer();
		java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("[a-z]+"); 
        java.util.regex.Matcher m = pattern.matcher(term_str);
        if (m.matches()==true) {
        	for (int i=0; i<term_str.length(); i++) {
        		s.add(term_str.charAt(i));
        	}
        	s.stem();
        	term_str = s.toString();
        }
        //System.out.println(term_str);
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
			String[] word=readLine.split(" ");
			for (int i=0; i<word.length; i++) {
				word[i] = tokenizer(word[i]);
				word[i] = stemmer_term(word[i]);
				if (word[i].length() >= 1)
					insert(word[i], docID);
			}
			docID++;
		}
		br.close();
		docID_max = docID - 1;
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
			term_part1 term_temp = terms_list.get(key);
			
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
				br2.write(" ");
				br2.write(String.valueOf(tfw(term_temp.getPostings_list().get(key_posting))));
				br2.newLine();
				br2.flush();
			}
			offset = offset + term_temp.getDf();
		}
		br1.close();
		br2.close();
		//System.out.println(docID_max);
	}
	
	private double tfw(int tf) {
		if (tf > 0)
			return Math.log10((double)tf) + 1;
		else 
			return 0;
	}

	public int getDocID_max() {
		return docID_max;
	}
}
