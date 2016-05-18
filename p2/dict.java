
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.TreeMap;
import java.util.Vector;

public class dict {
	private Hashtable<String, term> terms_list;
	private int docID_max;
	private double doc_length[];
	private String file_dictionary;
	private String file_postings;
	
	public dict(String fileDict, String filePost) throws IOException {
		terms_list = new Hashtable<String, term>();
		docID_max = 0;
		file_dictionary = fileDict;
		file_postings = filePost;
		
		File fin = new File(file_dictionary);
		if (!fin.exists())
			throw new FileNotFoundException();
		BufferedReader br = new BufferedReader(new FileReader(fin));
		
		String readLine = null;
		String term_in = null;
		int df = 0;
		int offset = 0;
		
		while((readLine = br.readLine()) != null) {
			String[] dictionary = readLine.split(" ");
			if (dictionary.length == 3) {
				term_in = dictionary[0];
				df = Integer.parseInt(dictionary[1]);
				offset = Integer.parseInt(dictionary[2]);
				insert(term_in, df, offset);
			}
			else {
				System.out.println("Incorrect input file");
				break;
			}
		}
		br.close();
		update_doc_len();
	}
	
	private void update_doc_len() throws IOException {
		File fin = new File(this.file_postings);
		if (!fin.exists())
			throw new FileNotFoundException();
		BufferedReader br = new BufferedReader(new FileReader(fin));
		
		String readLine = null;
		int docID = 0;
		int tf = 0;
		double doc_len_temp = 0;
		
		Hashtable<Integer, Double> doc_len = new Hashtable<Integer, Double>();
		
		while((readLine = br.readLine()) != null) {
			String[] postings = readLine.split(" ");
			if (postings.length == 2) {
				docID = Integer.parseInt(postings[0]);
				tf = Integer.parseInt(postings[1]);
				
				if (doc_len.containsKey(docID)) {
					doc_len_temp = Math.pow(tfw(tf), 2) + doc_len.get(docID);
					doc_len.put(docID, doc_len_temp);
				}
				else {
					doc_len_temp = Math.pow(tfw(tf), 2);
					doc_len.put(docID, doc_len_temp);
				}
			}
			else {
				System.out.println("Incorrect input file");
				break;
			}
		}
		br.close();
		
		docID_max = doc_len.size()-1;
		doc_length = new double[docID_max+1];
		
		for (int i=0; i<=docID_max; i++) {
			doc_length[i] = Math.sqrt(doc_len.get(i));
			//System.out.print("doc ");
			//System.out.print(i);
			//System.out.print(" -> ");
			//System.out.println(doc_length[i]);
		}
	}
	
	public void insert(String term_in, int df, int offset) {
		if (!terms_list.containsKey(term_in)) {
			term temp = new term(df, offset);
			terms_list.put(term_in, temp);
		}	
		else {
			System.out.println("term exists in the dictionary");
		}
	}
	
	public String stemmer_term(String term_str) {
		term_str = term_str.replaceAll("[^0-9a-zA-Z]", ""); // remove all symbols except numbers and letters
		term_str = term_str.toLowerCase(); // make term to lower case

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
	
	public void query(String query_in, int n) throws IOException {
		TreeMap<String, Integer> terms_query = new TreeMap<String, Integer>();
		
		String terms[] = query_in.split(" ");
		for (int i=0; i<terms.length; i++) {
			terms[i] = stemmer_term(terms[i]);
			if (terms_list.containsKey(terms[i])) {
				if (terms_query.containsKey(terms[i])) {
					int df_query = terms_query.get(terms[i]) + 1;
					terms_query.put(terms[i], df_query);
				}
				else {
					terms_query.put(terms[i], 1);
				}
			}
		}
		if (terms_query.size() == 0) {
			System.out.println("invalid query");
			return;
		}
		
		String query_term[] = new String[terms_query.size()];
		int query_tf[] = new int[terms_query.size()];
		double query_weight[] = new double[terms_query.size()];
		
		Iterator<String> iter = terms_query.keySet().iterator();
		for (int i=0; i<terms_query.size(); i++) {
			String str = iter.next();
			query_term[i] = str;
			query_tf[i] = terms_query.get(str);
		}
		iter = terms_query.keySet().iterator();
		for (int i=0; i<terms_query.size(); i++) {
			String str = iter.next();
			query_weight[i] = tfw(query_tf[i]) * idf(terms_list.get(str).getDf());
		}
		double normalize_factor = 0;
		for (int i=0; i<terms_query.size(); i++) {
			normalize_factor = normalize_factor + Math.pow(query_weight[i], 2);
		}
		normalize_factor = Math.sqrt(normalize_factor);
		for (int i=0; i<terms_query.size(); i++) {
			query_weight[i] = query_weight[i]/normalize_factor;
		}

		
		// get posting lists for query
		
		File fin = new File(this.file_postings);
		if (!fin.exists())
			throw new FileNotFoundException();
		BufferedReader br = new BufferedReader(new FileReader(fin));
		
		@SuppressWarnings("unchecked")
		Vector<posting> p[] = new Vector[terms_query.size()];
		for (int i=0; i<terms_query.size(); i++) {
			p[i] = new Vector<posting>();
		}
		String readLine = null;
		String key = null;
		int df = 0;
		int offset = 0;
		int docID = 0;
		int tf = 0;
		int lines = 0;
		int query_index = 0;
		
		iter = terms_query.keySet().iterator();
		key = iter.next();
		df = terms_list.get(key).getDf();
		offset = terms_list.get(key).getOffset();
		
		while((readLine = br.readLine()) != null) {
			if (lines >= offset && lines < offset+df) {
				String[] postings = readLine.split(" ");
				docID = Integer.parseInt(postings[0]);
				tf = Integer.parseInt(postings[1]);
				p[query_index].add(new posting(docID, tf));
				if (lines == offset+df-1) {
					if (!iter.hasNext()) {
						break;
					}
					else {
						key = iter.next();
						df = terms_list.get(key).getDf();
						offset = terms_list.get(key).getOffset();
						query_index++;
					}
				}
			}
			lines++;
		}
		br.close();

		
		// start computation of scores
		// document-at-a-time
		
		int docID_result = 0;
		double score_result = 0;
		int docID_temp = 0;
		int tf_temp = 0;
		Vector<similarity> result = new Vector<similarity>();
		TreeMap<Integer, Double> result_intermediate = new TreeMap<Integer, Double>();
		
		// compute score & rank the docs
		
		while(true) {
			for (int i=0; i<terms_query.size(); i++) {
				if (!p[i].isEmpty()) {
					docID_temp = p[i].get(0).getDocID();
					tf_temp = p[i].get(0).getTf();
					if (result_intermediate.containsKey(docID_temp)) {
						score_result = result_intermediate.get(docID_temp) + query_weight[i]*tfw(tf_temp)/doc_length[docID_temp];
						result_intermediate.put(docID_temp, score_result);
					}
					else {
						score_result = query_weight[i]*tfw(tf_temp)/doc_length[docID_temp];
						result_intermediate.put(docID_temp, score_result);
					}
					p[i].remove(0);
				}
			}
			if (result_intermediate.isEmpty()) 
				break;
			else {
				docID_result = result_intermediate.firstKey();
				score_result = result_intermediate.get(docID_result);
				result.add(new similarity(docID_result, score_result));
				result_intermediate.remove(docID_result);
			}
		}
		
		// output the ranking
		String output_file_name = "query" + n + "result.txt";
		BufferedWriter bw = new BufferedWriter(new FileWriter(output_file_name));
		
		Collections.sort(result);
		int ranking = 0;
		int num_of_output = 10;
		
		for (similarity i : result) {
			docID_result = i.getDocID();
			score_result = i.getSimilarity();
			
			System.out.print("docID = ");
			System.out.print(docID_result);
			System.out.print("	");
			System.out.println(score_result);
			
			bw.write(Integer.toString(docID_result));
			bw.write(",");
			bw.write(Double.toString(score_result));
			bw.newLine();
			bw.flush();
			
			ranking++;
			if (ranking >= num_of_output)
				break;
		}
		bw.close();
		
		
		/*
		// start computation of scores
		// term-at-a-time
		double score[] = new double[docID_max+1];
		for (int i=0; i<=docID_max; i++) {
			score[i] = 0;
		}
		
		// compute score & rank the docs
		query_index = 0;
		docID = 0;
		for (int i=0; i<terms_query.size(); i++) {
			for (posting p_index : p[i]) {
				docID = p_index.getDocID();
				score[docID] = score[docID] + query_weight[i]*tfw(p_index.getTf())/doc_length[docID];
			}
		}
		
		// output the ranking
		String output_file_name = "query" + n + "result.txt";
		BufferedWriter bw = new BufferedWriter(new FileWriter(output_file_name));
		
		for (int i=0; i<10; i++) {
			double max_score = 0;
			int doc_index = 0;
			for (int j=0; j<=docID_max; j++) {
				if (max_score < score[j]) {
					max_score = score[j];
					doc_index = j;
				}
			}
			score[doc_index] = 0;
			if (max_score == 0)
				break;
			System.out.print("docID = ");
			System.out.print(doc_index);
			System.out.print("	");
			System.out.println(max_score);
			
			bw.write(doc_index);
			bw.write(",");
			bw.write(Double.toString(max_score));
			bw.newLine();
			bw.flush();
		}
		bw.close();
		*/
	}

	public double tfw(int tf) {
		if (tf > 0)
			return Math.log10(tf) + 1;
		else 
			return 0;
	}
	
	public double idf(int df) {
		if (df > 0)
			return Math.log10((docID_max+1)/df);
		else 
			return 0;
	}
	
}
