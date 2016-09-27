/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.gameoftrades.student13;

import io.gameoftrades.model.kaart.*;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author Stephan
 */
public class PadImpl implements Pad {    
    int totaleTijd;
    Richting[] bewegingen;
    Richting richtt;
    Coordinaat huidig;

    public void setTotaleTijd(List<Terrein> terrein) {
        for (Terrein t : terrein) {
            totaleTijd += t.getTerreinType().getBewegingspunten();
        }
    }

    @Override
    public int getTotaleTijd() {
        return this.totaleTijd;
    }

    public void setBewegingen(Richting[] bewegingen) {
        this.bewegingen = bewegingen;
    }

    @Override
    public Richting[] getBewegingen() {
        return this.bewegingen;
    }

    @Override
    public Pad omgekeerd() {
        List<Richting> omgekeerd = new LinkedList<>();
        for(int i = bewegingen.length - 1; i>=0; i--){
            omgekeerd.add(bewegingen[i].omgekeerd());
        }
        PadImpl pad = new PadImpl();
        pad.setBewegingen(omgekeerd.toArray(new Richting[bewegingen.length]));
        return pad;
    }

    @Override
    public Coordinaat volg(Coordinaat crdnt) {
        huidig = crdnt;
        for (Richting beweging : bewegingen) {
            huidig = huidig.naar(beweging); 
        }
        
        return huidig;
    }
}
