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
 * @author Stephan
 */
public class SnelstePadAlgoritmeImpl implements SnelstePadAlgoritme, Debuggable {

    private Debugger debug = new DummyDebugger();
    private Kaart kaart;
    private PadImpl pad = new PadImpl();
    private HashMap<Coordinaat, Coordinaat> parents = new HashMap<>();

    @Override
    public Pad bereken(Kaart kaart, Coordinaat crdnt, Coordinaat crdnt1) {

        this.kaart = kaart;
        Coordinaat start = crdnt;
        Coordinaat eind = crdnt1;
        ArrayList<Coordinaat> openList = new ArrayList<>();
        ArrayList<Coordinaat> closedList = new ArrayList<>();
        openList.add(start);
        HashMap<Coordinaat, Integer> terreinKosten = new HashMap<>();
        terreinKosten.put(start, kaart.getTerreinOp(start).getTerreinType().getBewegingspunten() * 10);

        while (openList.size() > 0) {
            Coordinaat huidig = openList.get(0);
            for (Coordinaat c : openList) {
                if ((calcF(c, eind) + terreinKosten.get(c)) < (calcF(huidig, eind) + terreinKosten.get(huidig)) || (calcF(c, eind) + terreinKosten.get(c)) == (calcF(huidig, eind) + terreinKosten.get(huidig)) && c.afstandTot(crdnt1) < huidig.afstandTot(crdnt1)) {
                    huidig = c;
                }
            }
            openList.remove(huidig);
            closedList.add(huidig);

            if (huidig.equals(eind)) {
                hetPad(start, eind);
                break;
            }

            Richting[] richtingen = kaart.getTerreinOp(huidig).getMogelijkeRichtingen();
            for (Richting richting : richtingen) {
                Coordinaat neighbour = huidig.naar(richting);
                if (closedList.contains(neighbour) || !kaart.getTerreinOp(neighbour).getTerreinType().isToegankelijk()) {
                    continue;
                }
                int nieuweKosten = terreinKosten.get(huidig) + kaart.getTerreinOp(neighbour).getTerreinType().getBewegingspunten() * 10;
                if (nieuweKosten < kaart.getTerreinOp(neighbour).getTerreinType().getBewegingspunten() * 10 || !openList.contains(neighbour)) {
                    if (!openList.contains(neighbour) && kaart.getTerreinOp(neighbour).getTerreinType().isToegankelijk()) {
                        parents.put(neighbour, huidig);
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

    public void hetPad(Coordinaat start, Coordinaat eind) {
        ArrayList<Coordinaat> coordinaten = new ArrayList<>();
        Coordinaat current = eind;

        while (!current.equals(start)) {
            coordinaten.add(current);
            current = parents.get(current);
        }
        coordinaten.add(start);
        Collections.reverse(coordinaten);

        ArrayList terreinen = new ArrayList<>();
        Richting[] richtingen = new Richting[coordinaten.size() - 1];
        for (int i = 0; i < coordinaten.size() - 1; i++) {
            terreinen.add(kaart.getTerreinOp(coordinaten.get(i + 1)));
            richtingen[i] = Richting.tussen(coordinaten.get(i), coordinaten.get(i + 1));
        }
        pad.setTotaleTijd(terreinen);
        pad.setBewegingen(richtingen);
    }

    public int calcF(Coordinaat huidig, Coordinaat eind) {
        int x = Math.abs(huidig.getX() - eind.getX());
        int y = Math.abs(huidig.getY() - eind.getY());
        return (x + y);
    }

    @Override
    public void setDebugger(Debugger debugger) {
        this.debug = debugger;
    }
}
