
public class posting {
	private int docID;
	private int tf;
	
	public posting() {
		this.docID = -1;
		this.tf = 0;
	}
	
	public posting(int docID, int tf) {
		this.docID = docID;
		this.tf = tf;
	}

	public int getDocID() {
		return docID;
	}

	public int getTf() {
		return tf;
	}
	
	public void set_posting(int docID, int tf) {
		this.docID = docID;
		this.tf = tf;
	}

}
