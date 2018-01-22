/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package stardewvalleyautomaton.Model.Personnages.IA.Automate_Abigail;

import static stardewvalleyautomaton.Model.Personnages.IA.Automate_Abigail.Enum_EtatAbi.EAllerOeuf;
import stardewvalleyautomaton.Model.Personnages.IA.IA_Abigail;

/**
 *
 * @author perro
 */
public class EAllerOeuf extends EtatAbi{

    @Override
    public EtatAbi getEtatSuivant(IA_Abigail abi) {
        EtatAbi res = this;
        
        if(abi.getFatiguePrevision()>=100){
            res = new EAttendre();
        }else
        if(abi.getPosAbi()==abi.getCaseChoisie()){
            res = new ECollecterOeuf();
        }
        
        return res;
    }
    
    @Override
    public Enum_EtatAbi getType() {
        return EAllerOeuf;
    }
}
