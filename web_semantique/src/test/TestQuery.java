package test;

import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.*;
import java.util.*;
import java.sql.SQLException;

import javax.xml.xpath.*;
import javax.xml.parsers.*;
import org.w3c.dom.*;
import org.xml.sax.*;

import search.TermQuery;
import store.BaseReader;


/**
* Classe de test pour les requetes La requete peut etre composee de plusieurs mots cles separes par des espaces
* Utilisation : java test/TestQuery fichierconfig "terme1 terme2 terme3" fichiersortie
*
*/
public class TestQuery {
   
public static void main(String argv[])  {
	int seuil=50;
        
     try  {
             FileWriter out= new FileWriter(new File("log.txt"));
                           
			 String monfichier= new String(argv[0]);
	         System.out.println(monfichier);
	         String q= "";
             
		
             BufferedReader config = new BufferedReader (new FileReader(monfichier));
			 System.out.println(monfichier);
			 String ConnectURL = "";
		     String login="";
		     String pass=""; 
			 ConnectURL=config.readLine();
			
			 if (ConnectURL!=null) {login=config.readLine(); }
			 if (login!=null) {pass=config.readLine();}
			 if(pass!=null){ q=config.readLine();}
			 
			 TermQuery query = new TermQuery(q);
			 config.close();
			 
			 BaseReader base=new BaseReader(ConnectURL,login,pass);
			 // recherche de tous les documents pertinents de l'index et on calcule le score de pertinence
             TreeMap results = query.score(base);
		 
             System.out.println(results.size() +" resultats");
            
			// on trie la TreeMap de resultats selon les valeurs,
			// cad selon le score (et non selon la cle, cad
			// selon l'identifiant du doc )
			List cles = new ArrayList(results.keySet());
			Collections.sort(cles, new CompScore(results));
		
			int i=1;
			for (Iterator it=cles.listIterator();it.hasNext();) {
				// on recupere l'id du document et on va chercher son nom
				Integer docid = (Integer) it.next();
				String nom_fichier= base.document(docid).name;
				out.write(nom_fichier+"\t"+results.get(docid)+"\n");
					//out.write(node.getTextContent()+"\n\n");			
			}
			
	   		
	   		base.close();
	   		out.close();
        	}
     		catch (IOException e) { System.out.println("Problem : End of file."+e.getMessage());}
     		catch (SQLException e2) {System.out.println("SQL Error ."+e2.getMessage());}
     		catch (Exception e) {System.out.println("Error : Unable to process files. "+e.getMessage());}  
        
 		 
   
        
      
        
}


/* Classe permettant de trier les resultats sur les scores et non sur les identifiants de noeuds
*/
public static class CompScore implements Comparator {

private Map copieresults;

public CompScore(Map results) {
this.copieresults= results;
}


public int compare (Object o1, Object o2) {
Float s1= (Float) copieresults.get((Integer)o1);
Float s2= (Float) copieresults.get((Integer) o2);
return (s2.compareTo(s1));
}

}

} // TestQuery.java      
  
  
        
