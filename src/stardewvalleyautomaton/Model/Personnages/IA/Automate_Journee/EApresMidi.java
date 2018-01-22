/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package stardewvalleyautomaton.Model.Personnages.IA.Automate_Journee;

import static stardewvalleyautomaton.Model.Personnages.IA.Automate_Journee.Enum_EtatJournee.EApresMidi;
import stardewvalleyautomaton.Model.Personnages.IA.IA_Abigail;

/**
 *
 * @author perro
 */
public class EApresMidi extends EtatJournee{

    @Override
    public EtatJournee getEtatSuivant(IA_Abigail abi) {
        EtatJournee res = this;
        timer = timer + 1;
        
        if(timer==endApresMidi){
            res = new ESoir();
        }
        
        return res;    
    }

    @Override
    public Enum_EtatJournee getType() {
        return EApresMidi;
    }
}
