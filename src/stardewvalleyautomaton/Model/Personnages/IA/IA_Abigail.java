/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package stardewvalleyautomaton.Model.Personnages.IA;

import stardewvalleyautomaton.Model.Personnages.IA.Automate_Abigail.EtatAbi;
import stardewvalleyautomaton.Model.Personnages.IA.Automate_Abigail.EAttendre;
import java.util.ArrayList;
import java.util.Collections;
import stardewvalleyautomaton.Model.Carte;
import stardewvalleyautomaton.Model.Cases.Case;
import stardewvalleyautomaton.Model.Cases.Enum_Case;
import static stardewvalleyautomaton.Model.Cases.Enum_Case.*;
import stardewvalleyautomaton.Model.Gestionnaires.GestionnaireDesObjets;
import stardewvalleyautomaton.Model.Gestionnaires.GestionnaireDesPersonnages;
import static stardewvalleyautomaton.Model.Objets.Enum_Objet.Machine_Fromage;
import static stardewvalleyautomaton.Model.Objets.Enum_Objet.Oeuf;
import stardewvalleyautomaton.Model.Objets.Objet;
import stardewvalleyautomaton.Model.Personnages.Abigail;
import static stardewvalleyautomaton.Model.Personnages.Enum_Personnage.Vache;
import static stardewvalleyautomaton.Model.Personnages.IA.Automate_Abigail.Enum_EtatAbi.*;
import stardewvalleyautomaton.Model.Personnages.IA.Automate_Journee.*;
import static stardewvalleyautomaton.Model.Personnages.IA.Enum_Action.*;
import stardewvalleyautomaton.Model.Personnages.Personnage;
import stardewvalleyautomaton.Model.Personnages.Vache;
import stardewvalleyautomaton.Model.graphe.Graphe;

/**
 *
 * @author simonetma
 */
public class IA_Abigail extends IA {
    
    private Graphe monGraphe;
    private Graphe monGrapheFatigue;
    private int[] distance;
    private int[] predecesseur;
    private Case posAbiDij; //permet de voir si Abigail s'est déplacée après un dijsktra
    
    private Case oeufPlusProche;
    private Case vachePlusProche;
    private Case cheminMachine;
    private Case caseChoisie;
    
    private Enum_Action action;
    private ArrayList<Enum_Action> listeActions = new ArrayList<>();
    
    private EtatAbi etatAbi;
    private int fatigue;
    private int fatiguePrevision;
    private ArrayList<Integer> fatigueFuture;
    private ArrayList<Enum_Case> casePrevision;
    private int faim ;
    private int soif ;
    
    private EtatJournee etatJournee;
    private int multipleFatigue;    //si la fatigue est multipliée par 2 ou pas à cause de l'après midi
    private Case maisonAbigail;     //localisation de la maison d'abigail
    
    @Override
    protected void setActionValide() {
        this.addActionValide(attendre);
        this.addActionValide(moveLeft);
        this.addActionValide(moveRight);
        this.addActionValide(moveTop);
        this.addActionValide(moveBottom);
        this.addActionValide(traire);
        this.addActionValide(produireFromage);
        this.addActionValide(collecterOeuf);
    }
    
    public IA_Abigail(){
        monGraphe = null;
        monGrapheFatigue = null;
        distance = null;
        predecesseur = null;
        posAbiDij = null;
        
        oeufPlusProche = null;
        vachePlusProche = null;
        cheminMachine = null;
        caseChoisie = null;
        
        action = attendre;
        listeActions = new ArrayList<>();
        fatigue = 0;
        fatiguePrevision = 0;
        fatigueFuture = new ArrayList<>();
        casePrevision = new ArrayList<>();
        
        etatAbi = new EAttendre();
        faim = 0;
        soif = 0;
        
        etatJournee = new EMatin();
        maisonAbigail = null;
    }
    
    //IA D'ABIGAIL (A IMPLEMENTER) ---------------------------------------------
    @Override
    public Enum_Action action() {
        if(this.monGraphe == null) {
            matriceGraphe(Carte.get().taille()*Carte.get().taille());
            
            monGraphe.dijkstra(monGraphe.numeroCase(this.personnage().getCase(), Carte.get().taille()));
            distance = monGraphe.getDistance();
            predecesseur = monGraphe.getPredecesseur();
            maisonAbigail = this.personnage().getCase();
        }
        
        oeufPlusProche(createListeOeufs());
        vachePlusProche(createListeVache());
        fatiguePrevision = fatigue;
        evoluer();//passage à l'etat suivant
        return action;
    }

    /** 
     * Methode qui permet de generer deux graphes de la Carte
     * @param nbrSommet permet de determiner le nombre de sommets
     */
    public void matriceGraphe(int nbrSommet) {
        this.monGraphe = new Graphe(nbrSommet);
        this.monGrapheFatigue = new Graphe(nbrSommet);
        this.monGraphe.Grille();
        this.monGrapheFatigue.GrilleFatigue();

        for (Objet objet : GestionnaireDesObjets.getListeDesObjets()) {
            if(objet.getType() != Oeuf) {
                int numSommet = monGraphe.numeroCase(objet.getCase(),Carte.get().taille());
                for(int i = 0 ; i<nbrSommet ; i++) {
                    monGraphe.ajouterArete(numSommet, i,0);
                    monGrapheFatigue.ajouterArete(numSommet, i, 0);
                }
            }
        }
        //monGraphe.afficher();
    }

    /**
     * Fonction qui renvoie le mouvement de deplacement qu'Abigail va effectuer
     * @return Enum_Action action de déplacement (ou attente)
     */
    public Enum_Action deplacement(){
        Enum_Action res = attendre;
        
        if(this.listeActions.isEmpty() && caseChoisie != null){ //si la liste est vide et qu'on a choisit une case cible
            dijkstra();     //on initialise au cas où un dijkstra (si abigail a changé de position depuis le dernier dijsktra
            listeActions(); //on initialise la liste de mouvement et la fatigue future d'abigail
        }
        
        if(!listeActions.isEmpty() && caseChoisie!=null){       //si la liste contient des actions et qu'il y a une case cible
            res = listeActions.get(listeActions.size()-1);      //elle prend le dernier mouvement (dans l'ordre)
            listeActions.remove(listeActions.size()-1);         //on retire ce mouvement
            if(soif<100){ 
                soif = soif +1;                                 //on ajoute +1 de soif à chaque déplacement
            }
            fatigue = fatigue + fatigueFuture.get(0);           //après avoir calculer la fatigue, on ajoute la fatigue au déplacement correspondant
            fatigueFuture.remove(0);                            //on retire cette fatigue
        }
        return res;
    }
    
    /**
     * Methode qui permet de connaitre les predecesseurs et la getDistance de chaque case par rapport à la position d'Abigail, et ce, en fonction de la fatigue d'abigail
     */
    public void dijkstra(){
        if(posAbiDij==null || posAbiDij!=this.personnage().getCase()){      //si abigail a changé de position depuis le dernier dijkstra ou qu'elle effectue son premier
            if(fatigue<50){         //si abigail est à la moitié de sa fatigue
                this.monGraphe.dijkstra(monGraphe.numeroCase(this.personnage().getCase(), Carte.get().taille()));
                this.distance = monGraphe.getDistance();
                this.predecesseur = monGraphe.getPredecesseur();
                posAbiDij = this.personnage().getCase();
            }else{                  //si abigail est à plus de la moitié de sa fatigue on effectue un dijkstra sur l'autre graphe à poids 1, 2 et 3
                this.monGraphe.dijkstra(monGraphe.numeroCase(this.personnage().getCase(), Carte.get().taille()));
                this.distance = monGraphe.getDistance();
                this.predecesseur = monGraphe.getPredecesseur();
                posAbiDij = this.personnage().getCase();
            }
        }
    }
    /**
     * Methode qui initialise la liste d'actions pour se rendre à la case choisie, ainsi que la fatigue qui y correspond
     */
    private void listeActions(){
        listeActions = new  ArrayList<>(); //on vide les listes au cas où
        casePrevision = new ArrayList<>();
        fatigueFuture = new ArrayList<>();
        int i = monGraphe.numeroCase(caseChoisie, Carte.get().taille()); //on initialise la position de départ de la fonction avec la case Choisie
        boolean vache = true;       //variable qui permet de retirer la premiere case d'arrivée lorsque la case choisie et une vache (permet d'éviter de "tuer" la vache)
        while(distance[i]>0){       //tant qu'on a pas parcouru le chemin jusqu'à abigail
            if(caseChoisie==vachePlusProche && vache){
                vache = false;
                i = predecesseur[i];
            }else{
                if(this.predecesseur[i] == i+1){                    //pour se déplacer en haut
                    listeActions.add(moveTop);
                }
                if(this.predecesseur[i] == i-1){                    //pour se déplacer en bas
                    listeActions.add(moveBottom);
                }
                if(this.predecesseur[i] == i+Carte.get().taille()){ //pour se déplacer à gauche
                    listeActions.add(moveLeft);
                }
                if(this.predecesseur[i] == i-Carte.get().taille()){ //pour se déplacer à droite
                    listeActions.add(moveRight);
                }
                ajoutCase(i);                                       //on ajoute la case sur laquelle abigail va marcher pour prevoir la fatigue
                i = predecesseur[i];                                //on passe à la case précédente du chemin

                if(distance[i]==0){                                 //lorsqu'on a remonté le chemin jusqu'à abigail
                    int timerFutur = etatJournee.getTimer();        //on initialise une variable futur du timer pour prevoir la fatigue en fonction de l'état de la journée

                    Collections.reverse(casePrevision);             //on inverse tout les éléments du tableaux des cases pour prevoir la fatigue depuis la position d'abigail
                    for(Enum_Case cases : casePrevision){           //on parcourt chaque case du chemin
                    
                    if(timerFutur>etatJournee.getEndMatin()&& timerFutur<etatJournee.getEndApresMidi()){ //si le timer futur est durant l'après midi
                        multipleFatigue = 2;    //la fatigue sera multipliée par 2.
                    }else{
                        multipleFatigue = 1;    //sinon elle n'est pas multipliée
                    }
                    
                        if(fatiguePrevision>=50){   //lors du déplacement, si la fatigue d'abigail sera supérieure à 50
                            
                            switch(cases){  //en fonction de la case sur laquelle abigail va marcher, on ajoute la fatigue future et on augmente la fatigue previsionnelle qu'elle aurait
                                case dirt: fatiguePrevision = fatiguePrevision+(1*multipleFatigue); fatigueFuture.add(1*multipleFatigue); break;

                                case lightgrass: fatiguePrevision = fatiguePrevision+(2*multipleFatigue); fatigueFuture.add(2*multipleFatigue); break;

                                case grass: fatiguePrevision = fatiguePrevision+(3*multipleFatigue); fatigueFuture.add(3*multipleFatigue); break;
                            }
                        }else{ //sinon si la fatigue sera en dessous de 50, peut importe la case, on augmente de 1.
                            fatiguePrevision = fatiguePrevision+(1*multipleFatigue); fatigueFuture.add(1*multipleFatigue);
                        }
                        timerFutur = timerFutur + 1;    //on augmente le timer future pour gérer la fatigue d'abigail
                    }

                    if(fatiguePrevision>=100){ //après tout le calcul du chemin, si le déplacement lui fera atteindre ou depasser 100 de fatigue
                    //on réinitialise toute les prévisions pour qu'Abigail se repose
                    listeActions = new ArrayList<>();
                    fatigueFuture = new ArrayList<>();
                    }
                }
            }   
        }
    }
    
    /**
     * Méthode qui ajoute la case à la liste des cases sur lesquelles on va marcher pour calculer plus tard la fatigue
     * @param i case sur laquelle on effectue les calcul de déplacement
     */
    private void ajoutCase(int i){
        int T = Carte.get().taille();
        if(Carte.get().getCase(i%T, (i-i%T)/T).getType()==dirt){
            casePrevision.add(dirt);
        }
        if(Carte.get().getCase(i%T, (i-i%T)/T).getType()==lightgrass){
            casePrevision.add(lightgrass);
        }
        if(Carte.get().getCase(i%T,(i-i%T)/T).getType()==grass){
            casePrevision.add(grass);
        }
    }
    
    
    /**
     * Methode qui attribut une case cible manuellement si aucune case n'a été déterminée précédemment
     * @param cible case cible
     */
    public void caseChoisie(Case cible){
        if(caseChoisie==null){
            caseChoisie=cible;
        }
    }
    
    /**
     * Fonction qui créer une liste contenant les oeufs et leur localisation, et les retourne
     * @return ArrayList<Objet> liste des oeufs
     */
    public ArrayList<Objet> createListeOeufs() {
        ArrayList<Objet> listeOeufs = new ArrayList();
        for (Objet objet : GestionnaireDesObjets.getListeDesObjets()) {
            if((objet.getType()==Oeuf)){
                listeOeufs.add(objet);
            }
        }
        return listeOeufs;
    }
    
    /**
     * Fonction qui modifie la case de l'oeuf le plus proche
     * @param listeOeufs comporte la liste des oeufs présents sur la carte
     */
    public void oeufPlusProche(ArrayList<Objet> listeOeufs){
        Case oeufPlusProche = null;
        for(Objet oeuf : listeOeufs){
            if(oeufPlusProche==null){
                oeufPlusProche = oeuf.getCase();
            }       //si la getDistance entre abigail et l'oeuf en question est plus petite que celle entre l'oeuf le plus proche
            if(distance[monGraphe.numeroCase(oeuf.getCase(),Carte.get().taille())] < distance[monGraphe.numeroCase(oeufPlusProche, Carte.get().taille())]){
                oeufPlusProche = oeuf.getCase();  //alors l'oeuf en question devient le plus proche
           }
        }
        this.oeufPlusProche = oeufPlusProche;
    }
   
    /**
     * Fonction qui initialise la liste des vaches et leur localisation
     * @return ArrayList<Personnage> liste des vaches
     */
    public ArrayList<Personnage> createListeVache(){
        ArrayList<Personnage> listeVache = new ArrayList<>();
        for(Personnage personnage : GestionnaireDesPersonnages.getListeDesPersonnages() ){
            if (personnage.getType() == Vache){
                listeVache.add(personnage);
            }
        } 
        return listeVache;
    }
    
    /**
     * Méthode qui modifie la case de la vache ayant du lait la plus proche d'Abiguail
     * @param listeVache 
     */
    public void vachePlusProche(ArrayList<Personnage> listeVache){
        Case VachePlusProche = null;
        ArrayList<Personnage> listeVacheLait = new ArrayList();
  
        if(!listeVache.isEmpty()){
        //initialise la liste des vaches ayant du lait (ces vaches son immobiles)
            for(int i=0 ; i<=listeVache.size()-i ; i++){  
                if(((Vache)listeVache.get(i)).lait() == true){
                    listeVacheLait.add((Vache)listeVache.get(i));
                }
            }
        }
        
        if(!listeVacheLait.isEmpty()){
        for(Personnage vache : listeVacheLait){
            if(VachePlusProche==null){
              VachePlusProche = vache.getCase();
            }
            //si la getDistance entre abigail et la vache en question est plus petite que celle entre la vache le plus proche
            int T = Carte.get().taille();
            
            if(distance[monGraphe.numeroCase(vache.getCase(), Carte.get().taille())] < distance[monGraphe.numeroCase(VachePlusProche, Carte.get().taille())]){ 
                VachePlusProche = vache.getCase();
            }
        }
        vachePlusProche = VachePlusProche ;
      }
    }
    
    /**
     * Méthode qui modifie la case de la machine à fromage
     */
    public void cheminMachine(){
        
        //on récupère la machine Ã  fromage de la liste des objets
        for (Objet fro : GestionnaireDesObjets.getListeDesObjets()){
            if((fro.getType() == Machine_Fromage)){                                             
                //on regarde si les cases a cote de la machine sont libres et on attribue à cheminMachine une des case libres.
                int ligne = fro.getCase().getLigne();
                int colonne = fro.getCase().getColonne();
                if(Carte.get().getObjet(ligne-1, colonne) == null) {

                    cheminMachine=Carte.get().getCase(ligne-1, colonne);

                } else
                if(Carte.get().getObjet(ligne+1, colonne) == null) {

                    cheminMachine=Carte.get().getCase(ligne+1, colonne);
                } else
                if(Carte.get().getObjet(ligne, colonne-1) == null) {

                    cheminMachine=Carte.get().getCase(ligne, colonne-1);
                } else
                if(Carte.get().getObjet(ligne, colonne+1) == null) {

                    cheminMachine=Carte.get().getCase(ligne, colonne+1);
                }
            }
        }
    }
    
    /**
     * Méthode effectuée chaque tour lorsque c'est le matin
     */
    public void EMatin(){
        if(etatJournee.getTimer()==0){
            System.out.println("            COCORICO ! C'est le matin !");
        }
    }
    
    /**
     * Méthode effectuée chaque tour lorsque c'est l'après-midi
     */
    public void EApresMidi(){
        if(etatJournee.getTimer()==etatJournee.getEndMatin()){
            System.out.println("            Le soleil est haut plus haut ! Quelle journée fatigante !");
        }
        multipleFatigue = 2;
    }
    
    /**
     * Méthode effectuée chaque tour lorsque c'est le soir
     */
    public void ESoir(){
        if(etatJournee.getTimer()==etatJournee.getEndApresMidi()){
            System.out.println("            La nuit commence à tomber ! Abigail devrait aller se reposer chez elle !");
        }
    }
    
    /**
     * Méthode effectuée chaque tour lorsqu'Abigail va chercher un oeuf
     */
    public void EAllerOeuf(){
        caseChoisie(oeufPlusProche);
        action = deplacement();
        if(fatiguePrevision>=100){
            evoluer();
        }else{
            System.out.println("Abigail se déplace vers un oeuf !    fatigue = " + fatigue     + " soif = " + soif);
        }
    }   
    
    /**
     * Méthode effectuée lorsqu'Abigail a fini d'aller chercher son oeuf et qu'elle doit le ramasser
     */
    public void ECollecterOeuf(){
        action = collecterOeuf;
        if(faim<100){
            faim = faim + 5;
        }
        oeufPlusProche=null;
        caseChoisie=null;
        System.out.println("faim =" + faim);
        dijkstra();
    }
    
    /**
     * Méthode effectuée chaque tour lorsqu'Abigail va chercher du lait
     */
    public void EAllerLait(){
        caseChoisie(vachePlusProche);
        action = deplacement();
        if(fatiguePrevision>=100){
            evoluer();
        }else{
            System.out.println("Abigail se déplace vers la vache !    fatigue = " + fatigue     + " soif = " + soif);
        }
    }
    
    /**
     * Méthode effectuée lorsqu'Abigail a fini d'aller vers la vache et qu'elle doit la traire
     */
    public void ETraire(){
        action = traire;
        vachePlusProche=null;
        caseChoisie=null;
        System.out.println("Abigail trait la vache !");
        dijkstra();
    }
    
    /**
     * Méthode effectuée chaque tour lorsqu'Abigail va vers la machine à fromage
     */
    public void EAllerFromage(){
        if(caseChoisie!=cheminMachine || caseChoisie==null){
            cheminMachine();
        }
        caseChoisie(cheminMachine);
        action = deplacement();
        if(fatiguePrevision>=100){
            evoluer();
        }else{
            System.out.println("Abigail se déplace vers la machine !    fatigue = " + fatigue     + " soif = " + soif);
        }
    }
    
    /**
     * Méthode effectuée lorsqu'Abigail a fini d'aller vers la machine à fromage et qu'elle doit en faire
     */
    public void EFaireFromage(){
        action = produireFromage;
        caseChoisie=null;
        cheminMachine = null;
        dijkstra();
    }
    
    /**
     * Méthode effectuée lorsqu'Abigail est en attente
     */
    public void EAttendre(){
        action = attendre;
        if(fatigue<=50){
            fatigue = 0;
        }else{
            fatigue = fatigue - 50;
            fatiguePrevision = fatigue;
        }
        System.out.println("Abigail vient de se reposer !       fatigue = " + fatigue);
        dijkstra();
    }
    
    /**
     * Méthode effectuée lorsqu'Abigail rentre chez elle le soir pour se reposer
     */
   public void EAllerReposer(){
        caseChoisie(maisonAbigail);
        action = deplacement();
        if(fatiguePrevision>=100){
            evoluer();
        }else{
            System.out.println("Abigail rentre chez elle !          fatigue = " + fatigue + " soif = " + soif);
        }
    }
    
   /**
    * Méthode effectuée une fois qu'Abigail est rentrée chez elle et qu'elle doit se reposer
    */
    public void EReposer(){
        fatigue = 0;
        fatiguePrevision = 0;
        System.out.println("C'est le soir, Abigial se repose chez elle !");
        action = attendre;
        caseChoisie = null;
        dijkstra();
    }
    
    /**
     * Méthode effectuée une fois qu'Abigail doit boire
     */
    public void EBoire(){
        boire();
        soif=0;
        System.out.println("Abigail vient de boire !             soif = " + soif);
        action=attendre;
    }
    
    /**
     * Méthode effectuée une fois qu'Abigail doit manger
     */
    
    public void EManger(){
        manger();
        faim=0;
        System.out.println("Abigail vient de manger !           faim = " + faim);
        action= attendre;
        }
    
    /**
     * Méthode qui lit l'état d'abigail et de la journée, puis qui déclenche la méthode liée à ces états
     */
    public void lireEtat(){
        switch(etatJournee.getType()){
            case EMatin: EMatin();break;
            
            case EApresMidi: EApresMidi();break;
            
            case ESoir: ESoir();break;
        }
        
        switch(etatAbi.getType()){
            case EAllerOeuf: EAllerOeuf();break;
            
            case ECollecterOeuf: ECollecterOeuf();break;
            
            case EAllerLait: EAllerLait();break;
            
            case ETraire: ETraire();break;
            
            case EAllerFromage: EAllerFromage();break;
            
            case EFaireFromage: EFaireFromage();break;
            
            case EManger: EManger();break;
            
            case EBoire: EBoire();break;
            
            case EAttendre: EAttendre();break;
            
            case EAllerReposer: EAllerReposer();break;
            
            case EReposer: EReposer();break;
        }
    }
    
    /**
     * Méthode qui permet d'évoluer à chaque tour et de modifier les états d'Abigail et de la journée
     */
    public void evoluer(){
        etatJournee = etatJournee.getEtatSuivant(this);
        etatAbi = etatAbi.getEtatSuivant(this);
        lireEtat();
    }
    
    /**
     * Méthode qui retire le lait de l'inventaire d'Abigail
     */
    public void boire(){
        ((Abigail)personnage()).setLait(); //On retire le lait de son inventaire
    }
    
    /**
     * Méthode qui retire le fromage de l'inventaire d'Abigail
     */
    public void manger(){
        ((Abigail)personnage()).setFromage(); //On retire le fromage de son inventaire
    }
    
/*******************************GETTER*****************************************/
 
    /**
     * Fonction qui retourne si Abigail a du lait
     * @return boolean
     */
    public boolean getLait(){
        return ((Abigail)personnage()).getLait();
    }
    
    /**
     * Fonction qui retourne si Abigail a du fromage
     * @return boolean
     */
    public boolean getFromage(){
        return ((Abigail)personnage()).getFromage();
    }
    
    /**
     * Fonction qui retourne la fatigue prévisionnelle
     * @return int
     */
    public int getFatiguePrevision(){
        return fatiguePrevision;
    }
    
    /**
     * Fonction qui retourne la fatigue prévisionnelle
     * @return int
     */
    public int getFaim(){
        return faim;
    }
    
    /**
     * Fonction qui retourne la fatigue prévisionnelle
     * @return int
     */
    public int getSoif(){
        return soif;
    }
    
    /**
     * Fonction qui retourne la fatigue prévisionnelle
     * @return int
     */
    public Case getOeufPlusProche(){
        return oeufPlusProche;
    }
    
    /**
     * Fonction qui retourne la case de la vache la plus proche
     * @return Case
     */
    public Case getVachePlusProche(){
        return vachePlusProche;
    }
    
    /**
     * Fonction qui retourne la case de la machine à fromage
     * @return Case
     */
    public Case getCheminMachine(){
        return cheminMachine;
    }
    
    /**
     * Fonction qui retourne la case choisie
     * @return Case
     */
    public Case getCaseChoisie(){
        return caseChoisie;
    }
    
    /**
     * Fonction qui retourne la position d'Abigail
     * @return Case
     */
    public Case getPosAbi(){
        return this.personnage().getCase();
    }
    
    /**
     * Fonction qui retourne la liste des déplacements d'Abigail
     * @return ArrayList<Enum_Action>
     */
    public ArrayList<Enum_Action> getListeActions(){
        return listeActions;
    }

    /**
     * Fonction qui retourne l'état de la journée
     * @return EtatJournée
     */
    public EtatJournee getEtatJournee(){
        return etatJournee;
    }
}
