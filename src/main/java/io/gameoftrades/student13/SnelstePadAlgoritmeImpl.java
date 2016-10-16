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

    private Debugger debug = new DummyDebugger();                                               // debugger
    private final PadImpl pad = new PadImpl();                                                  // het pad
    private final HashMap<Coordinaat, Coordinaat> parents = new HashMap<>();                    // map met de parents

    @Override
    public Pad bereken(Kaart kaart, Coordinaat start, Coordinaat eind) {

        ArrayList<Coordinaat> openList = new ArrayList<>();                                     // lijst met coordinaten die nog niet bezocht zijn
        ArrayList<Coordinaat> closedList = new ArrayList<>();                                   // lijst met coordinaten die al bezocht zijn
        openList.add(start);                                                                    // voeg de start coordinaat toe aan de openList
        HashMap<Coordinaat, Integer> terreinKosten = new HashMap<>();                           // map waarin ge-updatete terreinkosten in opgeslagen worden
        terreinKosten.put(start, bewegingsKosten(kaart, start) + afstand(start, eind));         // voeg start toe aan map met terreinkosten

        // voer uit zolang de openList nog coordinaten bevat
        while (openList.size() > 0) {
            Coordinaat huidig = openList.get(0);

            // zoek de coordinaat met de laagste kosten en zet deze als huidig
            for (Coordinaat c : openList) {
                if (terreinKosten.get(c) < terreinKosten.get(huidig) || terreinKosten.get(c).equals(terreinKosten.get(huidig)) && afstand(c, eind) < afstand(huidig, eind)) {
                    huidig = c;
                }
            }
            openList.remove(huidig);                                                            // verwijder huidige coordinaat uit de openList
            closedList.add(huidig);                                                             // voeg de huidige coordinaat toe aan de closedList

            // pad is gevonden als huidig gelijk is aan eind
            if (huidig.equals(eind)) {
                maakPad(kaart, start, eind);                                                    // maak het pad
                break;                                                                          // break uit de while loop
            }

            // check de neighbours van het huidige coordinaat
            Richting[] richtingen = kaart.getTerreinOp(huidig).getMogelijkeRichtingen();
            for (Richting richting : richtingen) {
                Coordinaat neighbour = huidig.naar(richting);

                // check of neighbour toegankelijk is of in closedList staat
                if (closedList.contains(neighbour) || !kaart.getTerreinOp(neighbour).getTerreinType().isToegankelijk()) {
                    continue;
                }

                // bereken (nieuwe) kosten voor neighbour
                int nieuweKosten = (terreinKosten.get(huidig) - afstand(huidig, eind)) + (bewegingsKosten(kaart, neighbour) + afstand(neighbour, eind));

                // vergelijk nieuwe kosten met oude kosten
                if (terreinKosten.containsKey(neighbour) && nieuweKosten < terreinKosten.get(neighbour) || !openList.contains(neighbour)) {

                    // check of neighbour in openList staat
                    if (!openList.contains(neighbour)) {
                        parents.put(neighbour, huidig);                                         // voeg huidig toe als parent van de neighbour
                        openList.add(neighbour);                                                // voeg neighbour toe aan de openList
                        terreinKosten.put(neighbour, nieuweKosten);                             // voeg neighbour toe aan de terreinkosten map
                    } else {
                        parents.put(neighbour, huidig);                                         // update de parent van de neighbour
                        terreinKosten.put(neighbour, nieuweKosten);                             // update de kosten van neighbour
                    }
                }
            }
            terreinKosten.remove(huidig);                                                       // verwijder huidig coordinaat uit de map met terreinkosten
        }
        // set debugger om algoritme visueel zichtbaar te maken
        debug.debugPad(kaart, start, pad);

        // return het pad
        return pad;
    }

    // maak het pad door de parents te volgen van eind naar start
    public void maakPad(Kaart kaart, Coordinaat start, Coordinaat eind) {
        ArrayList<Coordinaat> coordinaten = new ArrayList<>();                                  // lijst met coordinaten van pad  
        Coordinaat current = eind;                                                              // zet huidig coordinaat op eindpunt

        // volg de parents vanaf het eind tot het bij start is
        while (!current.equals(start)) {
            coordinaten.add(current);
            current = parents.get(current);
        }
        coordinaten.add(start);                                                                 // voeg de start toe aan de lijst met coordinaten
        Collections.reverse(coordinaten);                                                       // reverse de lijst, zodat hij van start naar eind gaat

        ArrayList terreinen = new ArrayList<>();                                                // lijst met terreinen
        Richting[] richtingen = new Richting[coordinaten.size() - 1];                           // lijst met richtingen

        // converteer de coordinaten naar terreinen en richtingen
        for (int i = 0; i < coordinaten.size() - 1; i++) {
            terreinen.add(kaart.getTerreinOp(coordinaten.get(i + 1)));
            richtingen[i] = Richting.tussen(coordinaten.get(i), coordinaten.get(i + 1));
        }
        pad.setTotaleTijd(terreinen);                                                           // set de totale tijd van het pad
        pad.setBewegingen(richtingen);                                                          // set de bewegingen met richtingen
    }

    // bereken minimale afstand tussen huidige coordinaat en eindpunt
    public int afstand(Coordinaat huidig, Coordinaat eind) {
        int x = Math.abs(huidig.getX() - eind.getX());
        int y = Math.abs(huidig.getY() - eind.getY());
        return (x + y);
    }

    // nieuwe bewegingskosten om terreinsoort hoger af te wegen tegen afstand
    public int bewegingsKosten(Kaart kaart, Coordinaat c) {
        return kaart.getTerreinOp(c).getTerreinType().getBewegingspunten();
    }

    // debugger
    @Override
    public void setDebugger(Debugger debugger) {
        this.debug = debugger;
    }
}
