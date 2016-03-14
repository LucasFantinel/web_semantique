package index;


import java.util.Vector;
import java.util.Hashtable;
import java.util.Map;
import java.util.Enumeration;
import java.util.TreeMap;
import java.io.File;
import java.sql.SQLException;


import java.io.PrintWriter;
import java.io.IOException;






import org.jsoup.Jsoup;
//import org.w3c.dom.*;
//import org.xml.sax.*;

//import javax.xml.parsers.*;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.tartarus.snowball.ext.frenchStemmer;

import utils.FileList;
import store.BaseWriter;


/**
  * IndexWriter ecrit l'index (c'est a dire les 3 tables de la BD). Les documents sont parses avec Jsoup.
  *
  */

public final class IndexWriter {
  
		  
  
  /** Vecteur contenant des objets Document
  * @see DocumentAIndexer
  */
  public Vector<DocumentAIndexer> documentVector;
  

  /** Vecteur contenant des objets Noeud
  * @see NodeAIndexer
  */
  public Vector<?> pathTable;
  
  
  /** Hashtable contenant des objets Term 
  * @see Term
  */
  public Hashtable<TextObject, Term> postingTable;

  
// compteur pour l'identifiant du terme  
protected int count_id_term;
// compteur pour l'identifiant de document
protected int count_id_doc;

// nombre de termes dans un document
protected int term_count;


protected BaseWriter maBase;

// liste des fichiers a indexer
protected Vector<?> fileList;

  
// mots vides du français
public static final String[] STOP_WORDS = {"a","à","afin","ai","aie","aient","aient","ainsi","ais","ait","alors","as","assez","au","auquel","auquelle","aussi","aux","auxquelles","auxquels","avaient","avais","avait","avant","avec","avoir","beaucoup","ca","ça","car","ce","cela","celle","celles","celui","certain","certaine","certaines","certains","ces","cet","cette","ceux","chacun","chacune","chaque","chez","ci","comme","comment","concern","concernant","connait","connaît","conseil","contre","d","dans","de","des","desquelles","desquels","differe","different","différent","differente","différente","differentes","différentes","differents","différents","dois","doit","doivent","donc","dont","du","dû","duquel","dus","e","elle","elles","en","encore","ensuite","entre","es","est","et","etai","etaient","étaient","etais","étais","etait","était","etant","étant","etc","ete","été","etiez","étiez","etion","etions","étions","etre","être","eu","eux","evidenc","evidence","évidence","expliqu","explique","fai","faire","fais","fait","faite","faites","faits","fera","feras","fini","finie","finies","finis","finit","font","grace","grâce","ici","il","ils","intere","interessant","intéressant","interesse","intéressé","j","jamais","je","l","la","laquell","laquelle","le","lequel","les","lesquelles","lesquels","leur","leurs","lors","lorsque","lui","m","ma","mainten","maintenant","mais","mal","me","meme","même","memes","mêmes","mes","mettre","moi","moins","mon","n","ne","ni","no","non","nos","notre","nôtre","notres","nôtres","nou","nous","obtenu","obtenue","obtenues","obtenus","on","ont","or","ou","où","par","parfois","parle","pars","part","pas","permet","peu","peut","peuvent","peux","plus","pour","pourquo","pourquoi","pouvez","pouvons","prendre","pres","près","princip","principal","principaux","qu","quand","que","quel","quelle","quelles","quelques","quels","qui","quoi","sa","savoir","se","seront","ses","seul","seuls","si","soient","soit","son","sont","sous","souvent","sui","suis","sur","t","ta","te","tel","telle","telleme","tellement","telles","tels","tes","ton","toujour","toujours","tous","tout","toute","toutes","traite","tres","très","trop","tu","unv","une","unes","uns","utilise","utilisé","utilisee","utilisée","utilisées","utilisees","uilisés","utilises","va","venir",
	"vers","veut","veux","vont","voulez","voulu","vous"};


Hashtable<String, String> Stoptable;

/**
* COnstructeur. Met les compteurs a zero et initialise les structures des stockage, instancie le parseur.
*/   
public IndexWriter(String direc, BaseWriter base) throws
IOException{

fileList = FileList.list(direc);
maBase= base;

documentVector=new Vector<DocumentAIndexer>();
pathTable = new Vector<Object>();
postingTable = new Hashtable<TextObject, Term>();

count_id_doc=0;
count_id_term=0;
//count_id_node=0;

term_count=0;
//node_count=0;
//leaf_count=0;

Stoptable= new Hashtable<String,String>();
for (int i=0; i<STOP_WORDS.length; i++)
{
	Stoptable.put(STOP_WORDS[i],STOP_WORDS[i]);
}

 
}  
  
  
  /**
  * Permet de remplir la base avec toutes les informations contenues dans la memoire
  */
  public  void construct() {
  
  
  for (int i=0; i<fileList.size();i++) {
  	String monNom = (String) fileList.elementAt(i);
  	
	System.out.println("traitement du fichier"+monNom);
	File fichier = new File(monNom);

	term_count=0;
	
	try {
		// parsage du fichier
		Document document = Jsoup.parse(fichier, "UTF-8");	
		//on recupère le texte contenu dans le body et on l'index
		//Element body = document.body();
		
		// Get page title
		String title = document.title();
		constructTerme(title);
		
		// Get page meta description
		Elements metasDescription = document.select("meta[name=\"description\"]");
		for(Element metaDescription : metasDescription){
			constructTerme(metaDescription.attr("content"));
		}
		
		// Get page meta keywords
		Elements metasKeyword = document.select("meta[name=\"keywords\"]");
		for(Element metaKeyword : metasKeyword){
			String allKeywords = metaKeyword.attr("content").replace(", ", ",");
			constructTerme(allKeywords);
		}
		
		// Get all h tags
		Elements hTags = document.select("h1, h2, h3, h4, h5, h6");
		for(Element hTag : hTags){
			constructTerme(hTag.text());
		}
		
		// get all paragraphes
		Elements paragraphes = document.select("p");
		for(Element p : paragraphes){
			constructTerme(p.text());
		}
		//System.out.println("corps doc "+body.text());
	    //constructTerme(body.text());
		}
		
	catch (IOException io){
		System.out.println("Erreur de d'entree/sortie");
		}
	DocumentAIndexer dtoindex= new DocumentAIndexer(count_id_doc, monNom);
	documentVector.add(dtoindex);
	count_id_doc++;
  }//on a fini de parcourir tous les documents

  // calcul idf
  Enumeration<TextObject> enumeration = postingTable.keys();
  while(enumeration.hasMoreElements()){
	  TextObject key = enumeration.nextElement();
	  Term term = (Term) postingTable.get(key);
	  Map<Integer, TermFrequency> frequency = term.frequency;
	  int nb_occur_document = frequency.size();
	  double q = fileList.size()/nb_occur_document;
	  float idf = (float) Math.log(q);
	  for (Map.Entry<Integer, TermFrequency> entry : frequency.entrySet()) {
		  float freq = idf * entry.getValue().frequency;
		  entry.setValue(new TermFrequency(entry.getKey(), freq));
	  }
	  Term newTerm = new Term(term.term_id, term.text, (TreeMap<Integer, TermFrequency>) frequency);
	  postingTable.remove(key);
	  postingTable.put(key, newTerm);
	  
  }
  
  
  
  
  
// on insere les donnees sur les documents dans la base
	try{
	//PrintDocumentTable();
	BaseWriter.insertDocument(documentVector);
	}
	catch (SQLException sqle) {
		System.out.println("Erreur insertion document et noeuds "+sqle.getMessage());
	}

	
	// on insere les termes dans la base
try{
	PrintPostingTable();
	BaseWriter.insertPosting(postingTable);	
}
catch (SQLException sqle2) {
	System.out.println("Erreur insertion termes "+sqle2.getMessage());
}  
  
  } // construct() 

/**
* Permet de remplir la table de posting avec le texte.
*/
public final void constructTerme (String texte) {

Hashtable<TextObject, Term> new_document= new Hashtable<TextObject, Term>();
// il faut traiter tout ce texte...
				
// on passe en minuscules
texte= texte.toLowerCase();
				
// on commence par remplacer
texte=texte.replace('.',' ');
texte=texte.replace('/',' ');
texte=texte.replace('!',' ');
texte=texte.replace(';',' ');
texte=texte.replace(',',' ');
texte=texte.replace('+',' ');
texte=texte.replace('*',' ');
texte=texte.replace('-',' ');
texte=texte.replace('?',' ');
texte=texte.replace('[',' ');
texte=texte.replace(']',' ');
texte=texte.replace('(',' ');
texte=texte.replace(')',' ');
texte=texte.replace('\'',' ');
texte=texte.replace('\"',' ');
texte=texte.replace(':',' ');
texte=texte.replace('\\',' ');
texte=texte.replace('}',' ');
texte=texte.replace('{',' ');
texte=texte.replace('&',' ');
texte=texte.replace('©',' ');
				
String[] mots=texte.split(" ");
				
for (int j=0;j<mots.length; j++) {
	String mot=mots[j];		// on pourrair utiliser Porter ou la troncature ...!
	frenchStemmer stemmer = new frenchStemmer();
	stemmer.setCurrent(mot);
	if (stemmer.stem()){
//	    System.out.println(mot+"aprés lemmatiseur = "+stemmer.getCurrent());
	    mot=stemmer.getCurrent();
	}
	// on verifie que le mot n'est pas un mot vide ou un mot qui contient un @ ou un %
	if (Stoptable.get(mot)==null) {
		TextObject myTermText = new TextObject(mot);
		term_count++;
		 if (new_document.containsKey(myTermText)) { // si la table de posting contient deja le terme car rencontrer soit dans une autre doc, soit dans le même
           Term myTerm=(Term) postingTable.get(myTermText); //on récupère les infos qu'on a jusqu'ici
           //postingTable.remove(myTermText);
           new_document.remove(myTermText);
           TreeMap<Integer, TermFrequency> freq = new TreeMap<Integer, TermFrequency>();
           freq = myTerm.frequency; // on recupère les occurences dans les autre documents
           if (freq.containsKey(count_id_doc)) { // si le terme a déjà été trouvé pour le document
		       TermFrequency myTermFrequency = (TermFrequency) freq.get(count_id_doc);
		       freq.remove(count_id_doc);
		        myTermFrequency.frequency++;
		       freq.put(count_id_doc, myTermFrequency);
		       Term myNewTerm = new Term(myTerm.term_id, myTerm.text, freq);
		       //postingTable.put(myTermText, myNewTerm);
		       new_document.put(myTermText, myNewTerm);
           }      
             
           else { // si le terme est trouve dans un nouvel docuemnt
    		short un =1;
        	TermFrequency myTermFrequency = new TermFrequency(count_id_doc,un);   
         	freq.put(count_id_doc, myTermFrequency);
        	                                 
         	Term myNewTerm = new Term(myTerm.term_id, myTerm.text, freq); 
         	//postingTable.put(myTermText, myNewTerm); 
         	new_document.put(myTermText, myNewTerm);
         	Boolean myNewBoolean = new Boolean(false);             
                 	
           }
          
 		} //if postinTable.containsKey
     	else { // si la table de posting ne contient pas le terme, on l'insere!                                                                     
            short un=1;
            TermFrequency myTermFrequency = new TermFrequency(count_id_doc,un );
   
            TreeMap<Integer, TermFrequency> freq = new TreeMap<Integer, TermFrequency>();
            freq.put(count_id_doc, myTermFrequency);
            Term myTerm = new Term(count_id_term, mot, freq);     
            count_id_term++;
            postingTable.put(myTermText, myTerm); 
            new_document.put(myTermText, myTerm);
     } //else  

	}	// if

} // for
	Enumeration<TextObject> enumeration = new_document.keys();
	while(enumeration.hasMoreElements()){
		TextObject key = enumeration.nextElement();
		Term term = (Term) new_document.get(key);
		TreeMap<Integer, TermFrequency> frequency = term.frequency;
		TermFrequency termFrequency = (TermFrequency) frequency.get(count_id_doc);
		float weight = termFrequency.frequency;
		float tf = weight/count_id_term;
		termFrequency.frequency = tf;
		frequency.remove(count_id_doc);
		frequency.put(count_id_doc, termFrequency);
		Term newTerm = new Term(term.term_id, term.text, frequency);
		postingTable.put(key, newTerm);
	}
	//PrintPostingTable();
}

  /** Prints the documentVector */
   public final void PrintDocumentTable() {
          
      for (Enumeration<DocumentAIndexer> e=documentVector.elements(); e.hasMoreElements(); ) {
	        DocumentAIndexer tempDocument=new DocumentAIndexer();
	        tempDocument= (DocumentAIndexer) e.nextElement();
	        tempDocument.PrintDocument();
	   }    
          
          
  } // PrintDocumentTable()
 
   /** Prints the postingTable*/
   public final void PrintPostingTable() {
          
    for (Enumeration<Term> e=postingTable.elements(); e.hasMoreElements(); ) {
            Term tempTerm=new Term();
            tempTerm= (Term) e.nextElement();
            tempTerm.PrintTerm();
       }         
   /* for (Enumeration e=postingTable.keys(); e.hasMoreElements(); ) {
                TextObject tempTerm= (TextObject) e.nextElement();
                System.out.println(tempTerm.value + "\t"+ tempTerm.hashCode());
           }  */       
  }  // PrintPostingTable()
   
   
   
   
   
} //IndexWriter.java


