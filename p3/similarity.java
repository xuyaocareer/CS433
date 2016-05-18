

public class similarity implements Comparable<similarity>{
	private int docID;
	private double similarity;
	
	public similarity(int docID, double similarity) {
		this.docID = docID;
		this.similarity = similarity;
	}
	
	public int compareTo(similarity d) {
		if (this.getSimilarity() < d.getSimilarity())
			return 1;
		else 
			return 0;
    }

	public int getDocID() {
		return docID;
	}

	public double getSimilarity() {
		return similarity;
	}

}
