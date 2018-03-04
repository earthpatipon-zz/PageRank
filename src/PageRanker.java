import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

/**
 * This class implements PageRank algorithm on simple graph structure.
 * Put your name(s), ID(s), and section here.
 * Kanjanporn Sumitech 5888178 Sec 2
 * Patipon Suwanbol 5888218 Sec 1
 */
public class PageRanker {
	private static Map<Integer, Page> map = new HashMap<Integer, Page>();
	private static List<Double> Perplexity = new ArrayList<Double>();
	private static List<Page> sinkPageList;
	private static final double d = 0.85;
	/**
	 * This class reads the direct graph stored in the file "inputLinkFilename" into memory.
	 * Each line in the input file should have the following format:
	 * <pid_1> <pid_2> <pid_3> .. <pid_n>
	 * 
	 * Where pid_1, pid_2, ..., pid_n are the page IDs of the page having links to page pid_1. 
	 * You can assume that a page ID is an integer.
	 */
	public void loadData(String inputLinkFilename){
		String line;
	    String[] pageID = null;
		try (BufferedReader br = new BufferedReader(new FileReader(inputLinkFilename))) {
		    while ((line = br.readLine()) != null) {
		       pageID = line.trim().split("\\s+");
		       int initID= Integer.parseInt(pageID[0]);
		       if(!map.containsKey(initID)) {
		    	   		map.put(initID, new Page(initID));
		    	   }
		       Page initPage = map.get(initID);
		       for(int i=1; i<pageID.length; i++) {
		    	   		int currID = Integer.parseInt(pageID[i]);
		    	   		if(!map.containsKey(currID)) {
		    	   			Page p = new Page(currID);
		    	   			p.addOutPages(initPage);
		    	   			initPage.addInPages(p);
		    	   			map.put(currID, p);
		    	   		}
		    	   		else {
		    	   			Page p = map.get(currID);
		    	   			if(!p.getOutPages().contains(initPage)) {p.addOutPages(initPage);}
		    	   			if(!initPage.getInPages().contains(p)) {initPage.addInPages(p);}
		    	   		}
		       }
		    }
		} catch (FileNotFoundException e) {e.printStackTrace();
		} catch (NumberFormatException e) {e.printStackTrace();
		} catch (IOException e) {e.printStackTrace();}
	}
	
	/**
	 * This method will be called after the graph is loaded into the memory.
	 * This method initialize the parameters for the PageRank algorithm including
	 * setting an initial weight to each page.
	 */
	public void initialize(){
		sinkPageList = new ArrayList<Page>(); //init ArrayList to keep sinkPage
		double initPR = 1.0 / map.size();
		for(Page p : map.values()) {
			p.setPR(initPR);
			p.setNewPR(initPR);
			if(p.isSinkNode()) {
				sinkPageList.add(p);
			}
		}
	}
	
	/**
	 * Computes the Perplexity of the current state of the graph. The definition
	 * of Perplexity is given in the project specs.
	 */
	public double getPerplexity(){
		double sum = 0;
		double PR = 0;
		for(Page p : map.values()) {
			PR = p.getPR();
			sum += PR * (Math.log(PR) / Math.log(2)); //PR * log(base2)PR
		}
		return Math.pow(2, -sum);
	}
	
	/**
	 * Returns true if the Perplexity converges (hence, terminate the PageRank algorithm).
	 * Returns false otherwise (and PageRank algorithm continue to update the page scores). 
	 */
	public boolean isConverge(){
		if(Perplexity.size() < 4)
			return false;
		int index = Perplexity.size() - 1;
		double a = Math.floor(Perplexity.get(index-3));
		double b = Math.floor(Perplexity.get(index-2));
		double c = Math.floor(Perplexity.get(index-1));
		double d = Math.floor(Perplexity.get(index));
		if(a==b && b==c && c==d)
			return true;
		return false;
	}
	
	/**
	 * The main method of PageRank algorithm. 
	 * Can assume that initialize() has been called before this method is invoked.
	 * While the algorithm is being run, this method should keep track of the Perplexity
	 * after each iteration. 
	 * 
	 * Once the algorithm terminates, the method generates two output files.
	 * [1]	"PerplexityOutFilename" lists the Perplexity after each iteration on each line. 
	 * 		The output should look something like:
	 *  	
	 *  	183811
	 *  	79669.9
	 *  	86267.7
	 *  	72260.4
	 *  	75132.4
	 *  
	 *  Where, for example,the 183811 is the Perplexity after the first iteration.
	 *
	 * [2] "prOutFilename" prints out the score for each page after the algorithm terminate.
	 * 		The output should look something like:
	 * 		
	 * 		1	0.1235
	 * 		2	0.3542
	 * 		3 	0.236
	 * 		
	 * Where, for example, 0.1235 is the PageRank score of page 1.
	 * 
	 */
	public void runPageRank(String perplexityOutFilename, String prOutFilename){
		int N = map.size();
		double initNewPR = (1-d)/N;
		while(!isConverge()) {
			double sinkPR = 0;
			for(int i=0; i<sinkPageList.size(); i++) {
				sinkPR += sinkPageList.get(i).getPR();
			}
			for(Page p : map.values()) {
				p.setNewPR( (d*sinkPR/N) + initNewPR);
				for(Page q : p.getInPages()) {
					p.addNewPR( ( d*q.getPR() / q.getOutPages().size() ) );
				}
			}
			for(Page p : map.values()) {
				p.setPR(p.getNewPR());
			}
			Perplexity.add(getPerplexity());
		}
		//writing file
		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(perplexityOutFilename));
			StringBuilder sb = new StringBuilder();
			for(int i=0; i<Perplexity.size(); i++) {
				sb.append(Perplexity.get(i) + "\n");
			}
			bw.write(sb.toString());
			bw.close();
		} catch (IOException e) {e.printStackTrace();}
		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(prOutFilename));
			StringBuilder sb = new StringBuilder();
			TreeMap<Integer, Page> temp = new TreeMap<Integer,Page>();
			temp.putAll(map);
			for( Entry<Integer, Page> entry : temp.entrySet()) {
				sb.append(entry.getKey() + " " + entry.getValue().getPR() + "\n");
			}
			bw.write(sb.toString());
			bw.close();
		} catch (IOException e) {e.printStackTrace();
		}
	}
	
	
	/**
	 * Return the top K page IDs, whose scores are highest.
	 */
	public Integer[] getRankedPages(int K){
		Integer[] rank = new Integer[K];
		ArrayList<Page> list = new ArrayList<Page>(map.values());
		Collections.sort(list);
		for(int i=0; i<K; i++) {
			rank[i] = list.get(i).getID();
		}
		return rank;
	}
	
	public static void main(String args[])
	{
	long startTime = System.currentTimeMillis();
		PageRanker pageRanker =  new PageRanker();
		pageRanker.loadData("citeseer.dat");
		pageRanker.initialize();
		pageRanker.runPageRank("perplexity.out", "pr_scores.out");
		Integer[] rankedPages = pageRanker.getRankedPages(100);
	double estimatedTime = (double)(System.currentTimeMillis() - startTime)/1000.0;
		
		System.out.println("Top 100 Pages are:\n"+Arrays.toString(rankedPages));
		System.out.println("Proccessing time: "+estimatedTime+" seconds");
	}
}

//Class for Page object
class Page implements Comparator<Page>, Comparable<Page>{
	
	private int id;
	private ArrayList<Page> inPages;
	private ArrayList<Page> outPages;
	private Double score; //PR
	private Double newScore; //newPR
	
	public Page(int pageID) {
		id = pageID;
		inPages = new ArrayList<Page>();
		outPages = new ArrayList<Page>();
	}
	public void addInPages(Page p) {
		inPages.add(p);
	}
	public void addOutPages(Page p) {
		outPages.add(p);
	}
	public void setPR(Double d) {
		score = d;
	}
	public void setNewPR(Double d) {
		newScore = d;
	}
	public void addNewPR(Double d) {
		newScore += d;
	}
	public int getID() {
		return id;
	}
	public ArrayList<Page> getInPages(){
		return inPages;
	}
	public ArrayList<Page> getOutPages(){
		return outPages;
	}
	public Double getPR() {
		return score;
	}
	public Double getNewPR() {
		return newScore;
	}
	public boolean isSinkNode() {
		return outPages.isEmpty();
	}
	public String toStringIn() {
		StringBuilder sb = new StringBuilder();
		int size = getInPages().size();
		sb.append("[ ");
		for(int i=0; i<size; i++) {
			sb.append(getInPages().get(i).getID());
			if(i+1 != size)
				sb.append(", ");
		}
		sb.append(" ]");
		return sb.toString();
	}
	public String toStringOut() {
		StringBuilder sb = new StringBuilder();
		int size = getOutPages().size();
		sb.append("[ ");
		for(int i=0; i<size; i++) {
			sb.append(getOutPages().get(i).getID());
			if(i+1 != size)
				sb.append(", ");
		}
		sb.append(" ]");
		return sb.toString();
	}
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(toStringIn() + " ");
		sb.append(toStringOut() + " ");
		sb.append(getPR().toString());
		return sb.toString();
	}
	@Override
	public int compare(Page o1, Page o2) {
		return Double.compare(o2.score, o1.score); //Descending order
	}
	@Override
	public int compareTo(Page o) {
		return Double.compare(o.score, this.score); //Descending order
	}
}