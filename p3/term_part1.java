

import java.util.TreeMap;

public class term_part1 {
	private String term_str;
	private int df;
	private TreeMap<Integer, Integer> postings_list;
	
	public term_part1(String term_in, int docID_in) {
		this.term_str = term_in;
		this.df = 1;
		postings_list = new TreeMap<Integer, Integer>();
		postings_list.put(docID_in, 1);
	}

	public void insert_posting(int docID_in) {
		if (postings_list.containsKey(docID_in)) {
			int tf_temp = postings_list.get(docID_in);
			postings_list.put(docID_in, tf_temp+1);
		}
		else {
			df++;
			postings_list.put(docID_in, 1);
		}
	}

	public String getTerm_str() {
		return term_str;
	}

	public int getDf() {
		return df;
	}

	public TreeMap<Integer, Integer> getPostings_list() {
		return postings_list;
	}
}
