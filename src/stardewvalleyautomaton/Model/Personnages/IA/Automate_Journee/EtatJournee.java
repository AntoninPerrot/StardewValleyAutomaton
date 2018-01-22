/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package stardewvalleyautomaton.Model.Personnages.IA.Automate_Journee;

/**
 *
 * @author perro
 */
public abstract class EtatJournee implements EtatSuivantJournee{
    
    protected final int endMatin = 150;
    
    protected final int endApresMidi = 300;
    
    protected final int endSoir = 400;
    
    protected static int timer = 0;
    
    /**
     * getter du timer
     * @return int
     */
    public int getTimer() {
        return timer;
    }

    /**
     * getter de la fin du matin
     * @return int
     */
    public int getEndMatin() {
        return endMatin;
    }

    /**
     * getter de la fin de l'apres-midi
     * @return int
     */
    public int getEndApresMidi() {
        return endApresMidi;
    }

    /**
     * getter de la fin de soirée
     * @return int
     */
    public int getEndSoir() {
        return endSoir;
    }
}
