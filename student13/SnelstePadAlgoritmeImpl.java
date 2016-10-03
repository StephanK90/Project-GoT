/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.gameoftrades.student13;

import io.gameoftrades.debug.*;
import io.gameoftrades.model.algoritme.SnelstePadAlgoritme;
import io.gameoftrades.model.kaart.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

/**
 *
 * @author Charlie
 */
public class SnelstePadAlgoritmeImpl implements SnelstePadAlgoritme, Debuggable {

    private Debugger debug = new DummyDebugger();

    PadImpl pad = new PadImpl();
    HashMap<Terrein, Terrein> parents = new HashMap<>();
    HashMap<Terrein, Richting> parents2 = new HashMap<>();

    @Override
    public Pad bereken(Kaart kaart, Coordinaat crdnt, Coordinaat crdnt1) {

        Terrein start = kaart.getTerreinOp(crdnt);
        Terrein eind = kaart.getTerreinOp(crdnt1);
        ArrayList<Terrein> openList = new ArrayList<>();
        HashMap<Terrein, Integer> terreinKosten = new HashMap<>();
        ArrayList<Terrein> closedList = new ArrayList<>();
        openList.add(start);
        terreinKosten.put(start, start.getTerreinType().getBewegingspunten());

        while (openList.size() > 0) {
            Terrein huidig = openList.get(0);
            for (Terrein t : openList) {
                if ((calcF(t, eind) + terreinKosten.get(t)) < (calcF(huidig, eind) + terreinKosten.get(huidig)) || (calcF(t, eind) + terreinKosten.get(t)) == (calcF(huidig, eind) + terreinKosten.get(huidig)) && t.getCoordinaat().afstandTot(crdnt1) < huidig.getCoordinaat().afstandTot(crdnt1)) {
                    huidig = t;
                }
            }
            openList.remove(huidig);
            closedList.add(huidig);

            if (huidig == eind) {
                hetPad(start, eind);
                break;
            }

            Richting[] richtingen = huidig.getMogelijkeRichtingen();
            for (Richting richting : richtingen) {
                Terrein neighbour = kaart.kijk(huidig, richting);
                if (closedList.contains(neighbour) || !neighbour.getTerreinType().isToegankelijk()) {
                    continue;
                }
                int nieuweKosten = terreinKosten.get(huidig) + neighbour.getTerreinType().getBewegingspunten();
                if (nieuweKosten < neighbour.getTerreinType().getBewegingspunten() || !openList.contains(neighbour)) {
                    if (!openList.contains(neighbour) && neighbour.getTerreinType().isToegankelijk()) {
                        parents.put(neighbour, huidig);
                        parents2.put(neighbour, richting);
                        openList.add(neighbour);
                        terreinKosten.put(neighbour, nieuweKosten);
                    } else {
                        terreinKosten.put(neighbour, nieuweKosten);
                    }
                }
            }
        }
        debug.debugPad(kaart, crdnt, pad);
        return pad;
    }

    public void hetPad(Terrein start, Terrein eind) {
        ArrayList<Terrein> terreinen = new ArrayList<>();
        ArrayList<Richting> bewegingen = new ArrayList<>();
        Terrein current = eind;

        while (current != start) {
            terreinen.add(current);
            bewegingen.add(parents2.get(current));
            current = parents.get(current);
        }
        Collections.reverse(terreinen);
        Collections.reverse(bewegingen);

        Richting[] richtingen = new Richting[bewegingen.size()];
        for (int i = 0; i < bewegingen.size(); i++) {
            richtingen[i] = bewegingen.get(i);
        }
        pad.setTotaleTijd(terreinen);
        pad.setBewegingen(richtingen);
    }

    public double calcF(Terrein terrein, Terrein eind) {
        double g = terrein.getTerreinType().getBewegingspunten();
        double h = terrein.getCoordinaat().afstandTot(eind.getCoordinaat());
        return (g + h);
    }

    @Override
    public void setDebugger(Debugger debugger) {
        this.debug = debugger;
    }
}
