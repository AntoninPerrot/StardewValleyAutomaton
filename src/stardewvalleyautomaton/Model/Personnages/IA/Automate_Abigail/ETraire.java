/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package stardewvalleyautomaton.Model.Personnages.IA.Automate_Abigail;

import static stardewvalleyautomaton.Model.Personnages.IA.Automate_Abigail.Enum_EtatAbi.ETraire;
import static stardewvalleyautomaton.Model.Personnages.IA.Automate_Journee.Enum_EtatJournee.ESoir;
import stardewvalleyautomaton.Model.Personnages.IA.IA_Abigail;

/**
 *
 * @author perro
 */
public class ETraire extends EtatAbi{

    @Override
    public EtatAbi getEtatSuivant(IA_Abigail abi) {
        EtatAbi res;
        
        if(abi.getEtatJournee().getType()==ESoir){
            res = new EAllerReposer();
        }
        
        else if(abi.getSoif()>70 && abi.getLait()){
            res = new EBoire();
        }
        
        else if(abi.getSoif()>70 && abi.getVachePlusProche()!=null){
            res = new EAllerLait();
        }
        
        else if(abi.getFaim()>70 && abi.getFromage()){
            res = new EManger();
        }
        
        else if(abi.getFaim()>70 && abi.getLait()){
            res = new EAllerFromage();
        }
        
        else if(abi.getFaim()>70 && abi.getVachePlusProche()!=null){
            res = new EAllerLait();
        }
        
        else if(abi.getOeufPlusProche()!=null){
            res = new EAllerOeuf();
        }
        
        else{
            res = new EAttendre();
        }
        
        return res;
    }

    @Override
    public Enum_EtatAbi getType() {
        return ETraire;
    }
}
