


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

public class dict_part2 {
	private Hashtable<String, term_part2> terms_list;
	private int docID_max;
	private double doc_length[];
	private int doc_length_tf[];
	private double doc_length_tf_avg;
	private String file_dictionary;
	private String file_postings;
	private double k1 = 1.2;
	private double k3 = 1.2;
	private double b = 0.75;
	
	public dict_part2(String fileDict, String filePost, int docID_max_in) throws IOException {
		terms_list = new Hashtable<String, term_part2>();
		docID_max = docID_max_in;
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
			term_in = dictionary[0];
			df = Integer.parseInt(dictionary[1]);
			offset = Integer.parseInt(dictionary[2]);
			insert(term_in, df, offset);
		}
		br.close();
		update_doc_len();
	}
	
	
	// update document length
	private void update_doc_len() throws IOException {
		File fin = new File(this.file_postings);
		if (!fin.exists())
			throw new FileNotFoundException();
		BufferedReader br = new BufferedReader(new FileReader(fin));
		
		String readLine = null;
		int docID = 0;
		int tf = 0;
		double tf_w = 0;
		
		doc_length = new double[docID_max+1];
		doc_length_tf = new int[docID_max+1];
		doc_length_tf_avg = 0;
		for (int i=0; i<=docID_max; i++) {
			doc_length[i] = 0;
			doc_length_tf[i] = 0;
		}
	
		
		while((readLine = br.readLine()) != null) {
			String[] postings = readLine.split(" ");
			docID = Integer.parseInt(postings[0]);
			tf = Integer.parseInt(postings[1]);
			tf_w = Double.parseDouble(postings[2]); 
				
			doc_length[docID] += Math.pow(tf_w, 2); 
			doc_length_tf[docID] += tf;
		}
		br.close();
		
		for (int i=0; i<=docID_max; i++) {
			doc_length[i] = Math.sqrt(doc_length[i]);
			doc_length_tf_avg += doc_length_tf[i];
			//System.out.print("doc ");
			//System.out.print(i);
			//System.out.print(" -> ");
			//System.out.println(doc_length[i]);
		}
		doc_length_tf_avg = doc_length_tf_avg/(docID_max+1);
	}
	
	// insert terms into the dictionary
	private void insert(String term_in, int df, int offset) {
		if (!terms_list.containsKey(term_in)) {
			term_part2 temp = new term_part2(df, offset);
			terms_list.put(term_in, temp);
		}	
		else {
			System.out.println("term exists in the dictionary");
		}
	}
	
	private String stemmer_term(String term_str) {
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
	
	// do the query and return the ranking
	public void query(String query_in, int outputFile_index) throws IOException {
		
		// tokenization for the query
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
		
		double q_w_Cosine[] = new double[terms_query.size()]; // query weight for Cosine
		double q_w_Okapi[] = new double[terms_query.size()]; // query weight for Okapi
		
		Iterator<String> iter = terms_query.keySet().iterator();
		for (int i=0; i<terms_query.size(); i++) {
			String str = iter.next();
			query_term[i] = str;
			query_tf[i] = terms_query.get(str);
		}
		iter = terms_query.keySet().iterator();
		for (int i=0; i<terms_query.size(); i++) {
			String str = iter.next();
			q_w_Cosine[i] = tfw(query_tf[i]) * idf(terms_list.get(str).getDf());
			q_w_Okapi[i] = Okapi_qt(query_tf[i]) * Okapi_w(terms_list.get(str).getDf());
		}
		
		
		// get posting lists for query
		
		File fin = new File(this.file_postings);
		if (!fin.exists())
			throw new FileNotFoundException();
		BufferedReader br = new BufferedReader(new FileReader(fin));
		
		@SuppressWarnings("unchecked")
		Vector<posting> postinglist_query[] = new Vector[terms_query.size()]; // posting list for each terms in query
		@SuppressWarnings("unchecked")
		Hashtable<Integer, Integer> postinglist_query_tf[] = new Hashtable[terms_query.size()]; // posting list for result
		for (int i=0; i<terms_query.size(); i++) {
			postinglist_query[i] = new Vector<posting>();
			postinglist_query_tf[i] = new Hashtable<Integer, Integer>();
		}
		String readLine = null;
		String key = null;
		int df = 0;
		int offset = 0;
		int docID = 0;
		int tf = 0;
		double tf_w = 0; 
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
				tf_w = Double.parseDouble(postings[2]);  
				postinglist_query[query_index].add(new posting(docID, tf, tf_w)); 
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
		double tfw_temp = 0; 
		Vector<similarity> result_Cosine = new Vector<similarity>();
		Vector<similarity> result_Okapi = new Vector<similarity>();
		TreeMap<Integer, Double> result_intermediate_Cosine = new TreeMap<Integer, Double>();
		TreeMap<Integer, Double> result_intermediate_Okapi = new TreeMap<Integer, Double>();
		
		// compute score & rank the docs
		
		while(true) {
			for (int i=0; i<terms_query.size(); i++) {
				if (!postinglist_query[i].isEmpty()) {
					docID_temp = postinglist_query[i].get(0).getDocID();
					tf_temp = postinglist_query[i].get(0).getTf();
					tfw_temp = postinglist_query[i].get(0).getTf_w();
					
					// compute Cosine similarity
					if (result_intermediate_Cosine.containsKey(docID_temp)) {
						score_result = result_intermediate_Cosine.get(docID_temp) + q_w_Cosine[i]*tfw_temp/doc_length[docID_temp]; 
						result_intermediate_Cosine.put(docID_temp, score_result);
					}
					else {
						score_result = q_w_Cosine[i]*tfw_temp/doc_length[docID_temp]; 
						result_intermediate_Cosine.put(docID_temp, score_result);
					}
					
					// compute Okapi similarity
					if (result_intermediate_Okapi.containsKey(docID_temp)) {
						score_result = result_intermediate_Okapi.get(docID_temp) + q_w_Okapi[i]*Okapi_dt(tf_temp, docID_temp);
						result_intermediate_Okapi.put(docID_temp, score_result);
					}
					else {
						score_result = q_w_Okapi[i]*Okapi_dt(tf_temp, docID_temp);
						result_intermediate_Okapi.put(docID_temp, score_result);
					}
					
					// add the first element to the posting list for result
					// remove the first element in posting list for calculating similarity
					postinglist_query_tf[i].put(docID_temp, tf_temp);
					if (docID_temp == 140) {
						System.out.println(docID_temp);
						System.out.println(i);
					}
					postinglist_query[i].remove(0);
				}
			}
			if (result_intermediate_Cosine.isEmpty() && result_intermediate_Okapi.isEmpty()) 
				break;
			else {
				docID_result = result_intermediate_Cosine.firstKey();
				score_result = result_intermediate_Cosine.get(docID_result);
				result_Cosine.add(new similarity(docID_result, score_result));
				result_intermediate_Cosine.remove(docID_result);
				
				docID_result = result_intermediate_Okapi.firstKey();
				score_result = result_intermediate_Okapi.get(docID_result);
				result_Okapi.add(new similarity(docID_result, score_result));
				result_intermediate_Okapi.remove(docID_result);
			}
		}
		
		// output the ranking
		String output_file_name_Cosine = "cosquery" + outputFile_index + "result.txt";
		String output_file_name_Okapi = "okaquery" + outputFile_index + "result.txt";
		BufferedWriter bw_Cosine = new BufferedWriter(new FileWriter(output_file_name_Cosine));
		BufferedWriter bw_Okapi = new BufferedWriter(new FileWriter(output_file_name_Okapi));
		//BufferedWriter bw_Cosine_relevant = new BufferedWriter(new FileWriter("Cosine_relevant"+outputFile_index+".txt"));
		//BufferedWriter bw_Okapi_relevant = new BufferedWriter(new FileWriter("Okapi_relevant"+outputFile_index+".txt"));
		
		Collections.sort(result_Cosine);
		Collections.sort(result_Okapi);
		
		int ranking = 0;
		int num_of_output = 10;
		
		
		ranking = 0;
		System.out.println("Ranking By Cosine");
		for (similarity i : result_Cosine) {
			docID_result = i.getDocID();
			score_result = i.getSimilarity();
		
			System.out.print("docID = ");
			System.out.print(docID_result);
			System.out.print("	");
			System.out.println(score_result);
			
			// (ti, wtq, wtd, 1/||D||) 
			//double s = 0;
			for (int j=0; j<query_term.length; j++) {
				if (postinglist_query_tf[j].get(docID_result)==null)
					continue;
				//s +=  q_w_Cosine[j] * tfw(postinglist_query_tf[j].get(docID_result)) * 1.0/doc_length[docID_result];
				bw_Cosine.write(query_term[j]);
				bw_Cosine.write(",");
				bw_Cosine.write(Double.toString(q_w_Cosine[j]));
				bw_Cosine.write(",");
				bw_Cosine.write(Double.toString(tfw(postinglist_query_tf[j].get(docID_result))));
				bw_Cosine.write(",");
				bw_Cosine.write(Double.toString(1.0/doc_length[docID_result]));
				bw_Cosine.newLine();
				bw_Cosine.flush();
			}
			/*
			System.out.print("docID = ");
			System.out.print(docID_result);
			System.out.print("	");
			System.out.println(s);
			*/
			
			
			// (docID, similarity) 
			bw_Cosine.write(Integer.toString(docID_result));
			bw_Cosine.write(",");
			bw_Cosine.write(Double.toString(score_result));
			bw_Cosine.newLine();
			bw_Cosine.flush();
			
			// output for manually relevant check 
			//bw_Cosine_relevant.write(Integer.toString(docID_result));
			//bw_Cosine_relevant.write(",");
			
			ranking++;
			if (ranking >= num_of_output)
				break;
		}
		bw_Cosine.close();
		//bw_Cosine_relevant.close();
		
		ranking = 0;
		System.out.println("Ranking By Okapi");
		for (similarity i : result_Okapi) {
			docID_result = i.getDocID();
			score_result = i.getSimilarity();
			
			System.out.print("docID = ");
			System.out.print(docID_result);
			System.out.print("	");
			System.out.println(score_result);
			
			// (ti, wi, dti, qti)
			//double s = 0;
			for (int j=0; j<query_term.length; j++) {
				if (postinglist_query_tf[j].get(docID_result)==null) 
					continue;
				//s += Okapi_w(terms_list.get(query_term[j]).getDf()) * Okapi_dt(postinglist_query_tf[j].get(docID_result), docID_result) * Okapi_qt(query_tf[j]);
				bw_Okapi.write(query_term[j]);
				bw_Okapi.write(",");
				bw_Okapi.write(Double.toString(Okapi_w(terms_list.get(query_term[j]).getDf())));
				bw_Okapi.write(",");
				bw_Okapi.write(Double.toString(Okapi_dt(postinglist_query_tf[j].get(docID_result), docID_result)));
				bw_Okapi.write(",");
				bw_Okapi.write(Double.toString(Okapi_qt(query_tf[j])));
				bw_Okapi.newLine();
				bw_Okapi.flush();
			}
			/*
			System.out.print("docID = ");
			System.out.print(docID_result);
			System.out.print("	");
			System.out.println(s);
			*/
			
			
			// (docID, similarity) 
			bw_Okapi.write(Integer.toString(docID_result));
			bw_Okapi.write(",");
			bw_Okapi.write(Double.toString(score_result));
			bw_Okapi.newLine();
			bw_Okapi.flush();
			
			// output for manually relevant check 
			//bw_Okapi_relevant.write(Integer.toString(docID_result));
			//bw_Okapi_relevant.write(",");
			
			ranking++;
			if (ranking >= num_of_output)
				break;
		}
		bw_Okapi.close();
		//bw_Okapi_relevant.close();
		
		
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
				score[docID] = score[docID] + q_w_Cosine[i]*tfw(p_index.getTf())/doc_length[docID];
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

	private double tfw(int tf) {
		if (tf > 0)
			return Math.log10((double)tf) + 1;
		else 
			return 0;
	}
	
	private double idf(int df) {
		if (df > 0)
			return Math.log10((double)(docID_max+1)/df);
		else 
			return 0;
	}
	
	private double Okapi_w(int df) {
		return Math.log10( (double)(docID_max+1 - df + 0.5) / (df + 0.5) );
	}
	
	private double Okapi_dt(int tf, int docID) {
		return ((k1+1) * tf) / ( k1 * ((1-b) + b*doc_length_tf[docID]/doc_length_tf_avg) ) + tf;
	}

	private double Okapi_qt(int qtf) {
		return (double) (k3+1) * qtf / (k3+qtf);
	}
	
}
