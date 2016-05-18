

public class posting {
	private int docID;
	private int tf;
	private double tf_w;
	
	public posting() {
		this.docID = -1;
		this.tf = 0;
		this.tf_w = 0;
	}
	
	public posting(int docID, int tf, double tf_w) {
		this.docID = docID;
		this.tf = tf;
		this.tf_w =tf_w;
	}

	public int getDocID() {
		return docID;
	}

	public int getTf() {
		return tf;
	}
	
	public double getTf_w() {
		return tf_w;
	}

	public void setTf_w(double tf_w) {
		this.tf_w = tf_w;
	}

	public void set_posting(int docID, int tf, double tf_w) {
		this.docID = docID;
		this.tf = tf;
		this.tf_w =tf_w;
	}

}
