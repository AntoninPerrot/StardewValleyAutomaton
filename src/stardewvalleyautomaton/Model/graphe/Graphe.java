    /*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package stardewvalleyautomaton.Model.graphe;

import java.util.ArrayList;
import java.util.HashMap;
import stardewvalleyautomaton.Model.Carte;
import stardewvalleyautomaton.Model.Cases.Case;
import static stardewvalleyautomaton.Model.Cases.Enum_Case.dirt;
import static stardewvalleyautomaton.Model.Cases.Enum_Case.grass;
import static stardewvalleyautomaton.Model.Cases.Enum_Case.lightgrass;

/**
 *
 * @author simonetma
 */
public class Graphe {
    
    //Attributs
    private HashMap<Couple,Integer> matriceAdjacence;                           //un graphe est défini par sa matrice d'adjacence
    private int nombreDeSommets;
    private int infini;
    private boolean mark[];
    private int predecesseur[];
    private int distance[];
    
    
    /**
     * Constructeur du graphe
     * @param _nombreDeSommets int
     */
    public Graphe(int _nombreDeSommets) {
        this.nombreDeSommets = _nombreDeSommets;
        this.matriceAdjacence = new HashMap<>();
        
        mark = new boolean[this.nombreDeSommets+1];
        predecesseur = new int[this.nombreDeSommets+1];
        distance = new int[this.nombreDeSommets+1];
    }
    
    /**
     * Méthode qui créer les liens entre les sommets avec un poids de 1
     */
    public void Grille(){
        int m = this.nombreDeSommets;
        int n = this.nombreDeSommets;
        int T = Carte.get().taille();
        for(int i=0; i<n; i++){
            for(int j=0;j<m;j++){
                //voisin en lignes : (i, j+1)
                if((j+1<m) && ((j+1)%T!=0)){ 
                    this.ajouterArete(this.numeroCase(i, j , m), this.numeroCase(i, j+1, m));
                }
                //voisin en colonnes : (i+30, j)
                if ((i+T)<n) {
                    this.ajouterArete(this.numeroCase(i, j , n), this.numeroCase(i, j+Carte.get().taille(), n));
                }
            }
        }
    }
    
    /**
     * Méthode qui créer les liens entre les sommets avec un poids de 1, 2 ou 3 pour gérer la fatigue
     */
    public void GrilleFatigue(){
        int m = this.nombreDeSommets;
        int n = this.nombreDeSommets;
        int T = Carte.get().taille();
        for(int i=0; i<n; i++){
            for(int j=0;j<m;j++){
                //voisin en lignes (droite): (i, j+1)
                if((j+1<m) && ((j+1)%T!=0)){ 
                    if(Carte.get().getCase(i%T, j%T).getType()==dirt){
                        this.ajouterArc(this.numeroCase(i, j , n), this.numeroCase(i, j+1, n),1);
                    }
                    if(Carte.get().getCase(i%T, j%T).getType()==lightgrass){
                        this.ajouterArc(this.numeroCase(i, j , n), this.numeroCase(i, j+1, n),2);
                    }
                    if(Carte.get().getCase(i%T, j%T).getType()==grass){
                        this.ajouterArc(this.numeroCase(i, j , n), this.numeroCase(i, j+1, n),3);
                    }
                }
                
                //voisin en lignes (gauche): (i, j-1)
                if((j-1>=0) && ((j-1)%(Carte.get().taille()-1)!=0)){ 
                    if(Carte.get().getCase(i%T, j%T).getType()==dirt){
                        this.ajouterArc(this.numeroCase(i, j , n), this.numeroCase(i, j-1, n),1);
                    }
                    if(Carte.get().getCase(i%T, j%T).getType()==lightgrass){
                        this.ajouterArc(this.numeroCase(i, j , n), this.numeroCase(i, j-1, n),2);
                    }
                    if(Carte.get().getCase(i%T, j%T).getType()==grass){
                        this.ajouterArc(this.numeroCase(i, j , n), this.numeroCase(i, j-1, n),3);
                    }
                }
                
                //voisin en colonnes (bas): (i+30, j)
                if ((i+T)<n) {
                    if(Carte.get().getCase(i%T, j%T).getType()==dirt){
                        this.ajouterArc(this.numeroCase(i, j , n), this.numeroCase(i, j+Carte.get().taille(), n),1);
                    }
                    if(Carte.get().getCase(i%T, j%T).getType()==lightgrass){
                        this.ajouterArc(this.numeroCase(i, j , n), this.numeroCase(i, j+Carte.get().taille(), n),2);
                    }
                    if(Carte.get().getCase(i%T, j%T).getType()==grass){
                        this.ajouterArc(this.numeroCase(i, j , n), this.numeroCase(i, j+Carte.get().taille(), n),3);
                    }
                }
                //voisin en colonnes (haut): (i-30, j)
                if ((i-T)>=0) {
                    if(Carte.get().getCase(i%T, j%T).getType()==dirt){
                        this.ajouterArc(this.numeroCase(i, j , n), this.numeroCase(i, j-Carte.get().taille(), n),1);
                    }
                    if(Carte.get().getCase(i%T, j%T).getType()==lightgrass){
                        this.ajouterArc(this.numeroCase(i, j , n), this.numeroCase(i, j-Carte.get().taille(), n),2);
                    }
                    if(Carte.get().getCase(i%T, j%T).getType()==grass){
                        this.ajouterArc(this.numeroCase(i, j , n), this.numeroCase(i, j-Carte.get().taille(), n),3);
                    }
                }
            }
        }
    }
      
    /**
     * Méthode qui place "valeur" en position (i,j) de la matrice 
     * @param i int
     * @param j int 
     * @param valeur int
     */
    public void modifierMatrice(int i,int j,int valeur) {
        this.matriceAdjacence.put(new Couple(i,j), valeur);
    }
    
    /**
     * Méthode qui ajoute une arete entre i et j avec un poids de 1
     * @param i int
     * @param j int
     */
    public void ajouterArete(int i,int j) {
        this.modifierMatrice(i, j, 1);
        this.modifierMatrice(j, i, 1);
    }
    
    /**
     * Méthode qui ajoute un arc de i à j avec un poids de 1
     * @param i int
     * @param j int
     */
    public void ajouterArc(int i,int j) {
        this.modifierMatrice(i, j, 1);
    }

    /**
     * Méthode qui ajoute une arete entre i et j avec un certain poids
     * @param i int
     * @param j int
     * @param poids int 
     */
    public void ajouterArete(int debut, int fin, int poids){
        this.modifierMatrice(debut, fin, poids);
        this.modifierMatrice(fin, debut, poids);
    }
    
    /**
     * Méthode qui ajoute un arc de i à j avec un certain poids
     * @param i int
     * @param j int
     * @param poids int 
     */
    public void ajouterArc(int i,int j, int poids) {
        this.modifierMatrice(i, j, poids);
    }
    
    /**
     * Fonction qui renvoie la valeur de la matrice en position (i,j)
     * @param i int
     * @param j int
     * @return  int
     */
    public int Matrice(int i,int j) {
        //valeur par défaut
        int res = 0;
        Couple c = new Couple(i,j);
        //si (i,j) est bien présent dans la matrice
        if(this.matriceAdjacence.containsKey(c)) {
            res = this.matriceAdjacence.get(c);
        }
        return res;
    }
    
    /**
     * Fonction qui renvoie le nombre de sommet d'un graphe
     * @return int
     */
    public int NombreSommet() {
        return this.nombreDeSommets;
    }

    /**
     * Fonction qui donne le numero d'une case en fonction de ses coordonnées i et j
     * @param i int 
     * @param j int
     * @param taille int
     * @return int
     */
    public int numeroCase(int i, int j, int taille){          
        return taille*i+j;
    }
    
    /**
     * Fonction qui donne le numero d'une case c
     * @param c Case
     * @param taille int
     * @return int
     */
    public int numeroCase(Case c, int taille){
        return taille*c.getColonne()+c.getLigne();
    }
    
    /**
     * getter de la distance
     * @return int[]
     */
    public int[] getDistance(){
        return this.distance;
    }
    
    /**
     * getter du predecesseur
     * @return int[]
     */
    public int[] getPredecesseur(){
        return this.predecesseur;
    }    
    
    /**
     *
     * @param depart
     * @param arrivee
     * @return
     */
    public void dijkstra(int depart) {
        this.initialisation(depart);
        while (existeUnSommetNonMarque()) {
            int a = this.selectSommet();
            mark[a] = true;
            for (int i=0; i<=this.nombreDeSommets; i++) {
                relachement(a, i);
            }
        }
    }
    
    /**
     * Methode qui initialise les attributs de dijkstra
     * @param sommet1 int
     */
    private void initialisation(int sommet1) {
        if(infini==0){
            infini = infini();
        }
        for (int i = 0; i <= this.nombreDeSommets; i++) {
            mark[i] = false;
            distance[i] = this.infini;
            predecesseur[i] = -1;
        }
        distance[sommet1] = 0;
    }
    
    /**
     * Fonction qui créer l'infini par rapport au graphe
     * @return int
     */
    private int infini() {
        int res = 0;
        for (int i=0; i<=this.nombreDeSommets; i++) {
            for (int j=0; j<=this.nombreDeSommets; j++) {
                res += this.Matrice(i,j);
            }
        }
        return res+1; //histoire d'être sûr qu'on est pas de soucis
    }
    
    /**
     * Fonction qui effectue un relachement entre les sommets a et b
     * @param a int
     * @param b  int
     */
    private void relachement(int a, int b) {
        if (distance[b] > distance[a] + this.Matrice(a, b) && this.Matrice(a, b) != 0) {
            distance[b] = distance[a] + this.Matrice(a, b);
            predecesseur[b] = a;
        }
    }
    
    /**
     * Fonction qui choisit un sommet pour dijkstra
     * @return int
     */
    private int selectSommet() {
        int min = infini+1;
        int indice = -1;
        for (int i=0; i<=this.nombreDeSommets; i++) {
            if ((!mark[i]) && (min>distance[i])) {
                min = distance[i];
                indice = i;
            }
        }
        return indice;
    }
    
    /**
     * Fonction qui retourne si chaque sommet a été marqué ou non
     * @return boolean
     */
    private boolean existeUnSommetNonMarque() {
        boolean resultat = false;
        for (int i=0; i<=this.nombreDeSommets;i++) {
            if (!mark[i]) {
                resultat = true;
            }
        }
        return resultat;
    }
    
    /**
     * Fonction qui renvoie la matrice d'adjacence 
     * @return String
     */
    @Override
    public String toString() {
        String res = "";
        for(int i=0;i<=this.nombreDeSommets;i++) {
            for(int j=0;j<=this.nombreDeSommets;j++) {
                res += this.Matrice(i, j);
                if(j!= this.nombreDeSommets) {
                    res += " / ";
                }
            }
            if(i!= this.nombreDeSommets) {
                res += "\n";
            }
        }
        return res;
    }
    
    /**
     * Méthode qui affiche la matrice du graphe
     */
    public void afficher() {
        for(int i=0;i<this.nombreDeSommets;i++) {
            String res = i+ " : ";
            for(int j=0;j<this.nombreDeSommets;j++) {
                res += this.Matrice(i, j);
                if(j!= this.nombreDeSommets) {
                    res += " / ";
                }
            }
            System.out.println(res);
        }
    }
}
