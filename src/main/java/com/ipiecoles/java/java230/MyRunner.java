package com.ipiecoles.java.java230;


import com.ipiecoles.java.java230.exceptions.BatchException;
import com.ipiecoles.java.java230.model.Commercial;
import com.ipiecoles.java.java230.model.Employe;
import com.ipiecoles.java.java230.model.Manager;
import com.ipiecoles.java.java230.model.Technicien;
import com.ipiecoles.java.java230.repository.EmployeRepository;
import com.ipiecoles.java.java230.repository.ManagerRepository;
import com.sun.org.apache.xerces.internal.impl.xpath.regex.ParseException;

import org.joda.time.format.DateTimeFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class MyRunner implements CommandLineRunner {

    private static final String REGEX_MATRICULE = "^[MTC][0-9]{5}$";
    private static final String REGEX_NOM = ".*";
    private static final String REGEX_PRENOM = ".*";
    private static final String REGEX_MATRICULE_MANAGER = "^M[0-9]{5}$";
    private static final String REGEX_TYPE = "^[MTC]{1}.*";
    private static final int NB_CHAMPS_MANAGER = 5;
    private static final int NB_CHAMPS_TECHNICIEN = 7;
    private static final int NB_CHAMPS_COMMERCIAL = 7;

    @Autowired
    private EmployeRepository employeRepository;

    @Autowired
    private ManagerRepository managerRepository;

    private List<Employe> employes = new ArrayList<Employe>();

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public void run(String... strings) throws Exception {
        String fileName = "employes.csv";
        readFile(fileName);
    }

    /**
     * Méthode qui lit le fichier CSV en paramètre afin d'intégrer son contenu en BDD
     * @param fileName Le nom du fichier (à mettre dans src/main/resources)
     * @return une liste contenant les employés à insérer en BDD ou null si le fichier n'a pas pu être le
     */
    public List<Employe> readFile(String fileName) throws Exception {
        Stream<String> stream;
        stream = Files.lines(Paths.get(new ClassPathResource(fileName).getURI()));
        //TODO
        Integer i = 0;      
        for(String ligne : stream.collect(Collectors.toList())) {
        	i++;
        	try{
        		processLine(ligne);
        	} catch (BatchException e){
        		System.out.println("Ligne " + i + " : " + e.getMessage() + " => " + ligne);
        	}
        }
        return employes;
    }

    /**
     * Méthode qui regarde le premier caractère de la ligne et appelle la bonne méthode de création d'employé
     * @param ligne la ligne à analyser
     * @param regex 
     * @throws BatchException si le type d'employé n'a pas été reconnu
     */
    private void processLine(String ligne) throws BatchException {
        //TODO
    		
        // Transformer String en tableau
    	String [] tableau= ligne.split(",");
    	
    	// Test taille tableau pour pouvoir continuer, vérifier si tableau est vide
    	if (tableau.length > 0) {	   	

    		// Récupération de la 1ère lettre du matricule
    		String m1 = tableau[0].substring(0, 1).toUpperCase();
    		
    		// Type d'employé  	  	   		
    		if(!tableau[0].matches(REGEX_TYPE)) {
    		    throw new BatchException("Type d'employé inconnu : " + m1);
        	}
    		
    		// Test taille ligne, le bon nombre   	  	   		
    		if(!tableau[0].matches(REGEX_MATRICULE)) {
        		throw new BatchException("La chaîne " + tableau[0] + " ne respecte pas l'expression régulière ^[MTC][0-9]{5}$ =>  " + ligne.charAt(0));
        	}
    		
    		// On redirige en fonction de la 1ère lettre du matricule
    		switch(m1) {
    			case "T":
    				processTechnicien(ligne);
    		    break;
    		  case "M":
    			  	processManager(ligne);
    		    break;
    		  case "C":
    			  	processCommercial(ligne);
    		    break;
    		} 
 
	    }
	    	
    }
    	
    	
    
    
    /**
	 * Vérifie le format de la date
	 * @param date
	 * @throws BatchException si la date n'est pas conforme
	 * @return date
	 */
	private String verifDate(String date) throws BatchException {
		try {
        	DateTimeFormat.forPattern("dd/MM/yyyy").parseLocalDate(date);     	
        } catch (Exception e) {
        	throw new BatchException(date + " ne respecte pas le format de date dd/MM/yyyy ");
        }
		return date; 
	}

	/**
	 * Vérifie le salaire
	 * @param salaire
	 * @throws BatchException si le salaire n'est pas conforme
	 * @return salaire
	 */
	private String verifSalaire(String salaire) throws BatchException {
		try {
	    	Double.parseDouble(salaire);
	    } catch (Exception e) {
	    	throw new BatchException(salaire + " n'est pas un nombre valide pour un salaire ");
	    } 
		return salaire; 
	}
    
    
    
    /**
     * Méthode qui crée un Commercial à partir d'une ligne contenant les informations d'un commercial et l'ajoute dans la liste globale des employés
     * @param ligneCommercial la ligne contenant les infos du commercial à intégrer
     * @throws BatchException s'il y a un problème sur cette ligne
     */
    private void processCommercial(String ligneCommercial) throws BatchException {
        //TODO   	
    	String [] tableau= ligneCommercial.split(",");   	   	
    	
    	Commercial c = new Commercial();
		
    	// Récupération de la valeur de la date
		String date =  tableau[3];
		verifDate(date);
		
		// Champ salaire 
	    String salaire = tableau[4];
	    verifSalaire(salaire);
	    
	    // Récupération de la 1ère lettre du matricule
		String m1 = tableau[0].substring(0, 1).toUpperCase();
		
		// Longueur tableau
    	if (tableau.length != NB_CHAMPS_COMMERCIAL) {
    		throw new BatchException("La ligne commercial ne contient pas 7 éléments mais " + tableau.length + " ");
    	}
    	if (tableau.length == NB_CHAMPS_COMMERCIAL) {
    		
    		// Le chiffre d'affaire du commercial
    		String ca = tableau[5];
    		try {
    			Double.parseDouble(ca);
	        } catch (Exception e) {
	        	throw new BatchException("Le chiffre d'affaire du commercial est incorrect : " + ca + " ");
	        } 
    		
	        // La performance du commercial
    		String perf = tableau[6];
    		try {
	    		Double.parseDouble(perf);
	        } catch (Exception e) {
	        	throw new BatchException("La performance du commercial est incorrecte : " + perf + " ");
	        } 
    	}
    }

    
    /**
     * Méthode qui crée un Manager à partir d'une ligne contenant les informations d'un manager et l'ajoute dans la liste globale des employés
     * @param ligneManager la ligne contenant les infos du manager à intégrer
     * @throws BatchException s'il y a un problème sur cette ligne
     */
    private void processManager(String ligneManager) throws BatchException {
        //TODO
    	String [] tableau= ligneManager.split(",");   	   	  	
        
    	Manager m = new Manager();
    	
    	// Si bonne longueur de tableau
    	if (tableau.length != NB_CHAMPS_MANAGER) {
    		throw new BatchException("La ligne manager ne contient pas 5 éléments mais " + tableau.length + " ");
    	}
    	
    	// Récupération de la valeur de la date
		String date =  tableau[3];
		verifDate(date);
    	
		// Champ salaire 
	    String salaire = tableau[4];
	    verifSalaire(salaire);
	    
	    

    }

    /**
     * Méthode qui crée un Technicien à partir d'une ligne contenant les informations d'un technicien et l'ajoute dans la liste globale des employés
     * @param ligneTechnicien la ligne contenant les infos du technicien à intégrer
     * @throws BatchException s'il y a un problème sur cette ligne
     */
    private void processTechnicien(String ligneTechnicien) throws BatchException {
        //TODO
    	String [] tableau= ligneTechnicien.split(",");   	   	  	
        
    	Technicien t = new Technicien();
    	
    	// Récupération de la valeur de la date
		String date =  tableau[3];
		verifDate(date);
		
		// Champ salaire 
	    String salaire = tableau[4];
	    verifSalaire(salaire);
	 	
	 	// Si matricule technicien
    	if (tableau.length != NB_CHAMPS_TECHNICIEN) {
    		throw new BatchException("La ligne technicien ne contient pas 7 éléments mais " + tableau.length + " ");
    	}
    	if (tableau.length == NB_CHAMPS_TECHNICIEN) {
    		String grade = tableau[5];
	    	try {
	    		Integer.parseInt(grade); 
	    		
	        } catch (Exception e) {
	        	throw new BatchException("Le grade du technicien est incorrect : " + grade);
	        } 
	    	if (!grade.matches("[1-5]")) {	
	    		throw new BatchException("Le grade doit être compris entre 1 et 5 : " + grade + ", technicien : Technicien{grade=" + tableau[5] + "} "
	    				+ "Employe{nom='" + tableau[1] + "', prenom='" + tableau[2] + "', matricule='" + tableau[0] + "', dateEmbauche= " + tableau[3] + ", salaire=" + tableau[4] + "} ");
	    	}
	    	
	    	// Vérification du matricule du manager du technicien
	    	if (!tableau[6].matches(REGEX_MATRICULE_MANAGER)) {
	    		throw new BatchException("la chaîne " + tableau[6] + " ne respecte pas l'expression régulière ^M[0-9]{5}$ ");
	    	}
	    	
	    			
	    	// recherche du matricule du manager du technicien
    		Employe tech = employeRepository.findByMatricule(tableau[6]);
    		boolean matcsv = tableau[0].matches(tableau[6]);
    	
    		//System.out.println("--------------------- tech = " + tech + " / matcsv = " + matcsv);

    		if(tech == null && !matcsv) {
    			throw new BatchException("Le manager de matricule " + tableau[6] + " n'a pas été trouvé dans le fichier ou en base de données");
    		} 
    	}

    }

    

}
