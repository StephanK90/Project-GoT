/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.gameoftrades.student13;

import io.gameoftrades.model.algoritme.SnelstePadAlgoritme;
import io.gameoftrades.model.kaart.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

/**
 *
 * @author Stephan
 */
public class SnelstePadAlgoritmeImpl implements SnelstePadAlgoritme {

    PadImpl pad = new PadImpl();
    HashMap<Terrein, Terrein> parents = new HashMap<>();
    HashMap<Terrein, Richting> parents2 = new HashMap<>();

    @Override
    public Pad bereken(Kaart kaart, Coordinaat crdnt, Coordinaat crdnt1) {

        Terrein start = kaart.getTerreinOp(crdnt);
        Terrein eind = kaart.getTerreinOp(crdnt1);
        ArrayList<Terrein> openList = new ArrayList<>();
        ArrayList<Terrein> closedList = new ArrayList<>();
        openList.add(start);

        while (openList.size() > 0) {
            Terrein huidig = openList.get(0);
            for (int i = 0; i < openList.size(); i++) {
                if (calcF(openList.get(i), eind) < calcF(huidig, eind) || calcF(openList.get(i), eind) == calcF(huidig, eind) && openList.get(i).getCoordinaat().afstandTot(crdnt1) < huidig.getCoordinaat().afstandTot(crdnt1)) {
                    huidig = openList.get(i);
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
                    // doe niets
                } else if (!openList.contains(neighbour) && neighbour.getTerreinType().isToegankelijk()) {
                    parents.put(neighbour, huidig);
                    parents2.put(neighbour, richting);
                    openList.add(neighbour);
                }
            }
        }
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
}
