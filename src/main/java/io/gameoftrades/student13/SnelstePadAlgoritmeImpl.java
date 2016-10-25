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

    private Debugger debug = new DummyDebugger();                                                       // debugger
    private final PadImpl pad = new PadImpl();                                                          // het pad
    private final HashMap<Coordinaat, Coordinaat> parents = new HashMap<>();                            // map met de parents

    @Override
    public Pad bereken(Kaart kaart, Coordinaat start, Coordinaat eind) {

        ArrayList<Coordinaat> openList = new ArrayList<>();                                             // lijst met coordinaten die nog niet bezocht zijn
        ArrayList<Coordinaat> closedList = new ArrayList<>();                                           // lijst met coordinaten die al bezocht zijn
        openList.add(start);                                                                            // voeg de start coordinaat toe aan de openList
        HashMap<Coordinaat, Double> terreinKosten = new HashMap<>();                                    // map waarin ge-updatete terreinkosten in opgeslagen worden
        terreinKosten.put(start, bewegingsKosten(kaart, start) + start.afstandTot(eind));               // voeg start toe aan map met terreinkosten

        // voer uit zolang de openList nog coordinaten bevat
        while (openList.size() > 0) {
            Coordinaat huidigeCrdnt = openList.get(0);

            // zoek de coordinaat met de laagste kosten en zet deze als huidigeCrdnt
            for (Coordinaat c : openList) {
                boolean lagereFcost = terreinKosten.get(c) < terreinKosten.get(huidigeCrdnt);           // true als fCost van c lager is
                boolean zelfdeGcost = terreinKosten.get(c).equals(terreinKosten.get(huidigeCrdnt));     // true als fCost gelijk is
                boolean kleinereAfstand = c.afstandTot(eind) < huidigeCrdnt.afstandTot(eind);           // true als afstand kleiner is
                if (lagereFcost || zelfdeGcost && kleinereAfstand) {
                    huidigeCrdnt = c;
                }
            }
            openList.remove(huidigeCrdnt);                                                              // verwijder huidige coordinaat uit de openList
            closedList.add(huidigeCrdnt);                                                               // voeg de huidige coordinaat toe aan de closedList

            // pad is gevonden als huidigeCrdnt gelijk is aan eind
            if (huidigeCrdnt.equals(eind)) {
                maakPad(kaart, start, eind);                                                            // maak het pad
                break;                                                                                  // break uit de while loop
            }

            // check de neighbours van het huidige coordinaat
            Richting[] richtingen = kaart.getTerreinOp(huidigeCrdnt).getMogelijkeRichtingen();
            for (Richting richting : richtingen) {
                Coordinaat neighbour = huidigeCrdnt.naar(richting);

                // check of neighbour toegankelijk is of in closedList staat
                boolean nietToegankelijk = !kaart.getTerreinOp(neighbour).getTerreinType().isToegankelijk();
                if (closedList.contains(neighbour) || nietToegankelijk) {
                    continue;
                }

                // bereken (nieuwe) kosten voor neighbour
                double kostenVanParent = (terreinKosten.get(huidigeCrdnt) - huidigeCrdnt.afstandTot(eind));
                double kostenVanNeighbour = (bewegingsKosten(kaart, neighbour) + neighbour.afstandTot(eind));
                double nieuweKostenNeighbour = kostenVanParent + kostenVanNeighbour;

                // vergelijk nieuwe kosten met oude kosten
                if (terreinKosten.containsKey(neighbour) && nieuweKostenNeighbour < terreinKosten.get(neighbour)
                        || !openList.contains(neighbour)) {

                    // check of neighbour in openList staat
                    if (!openList.contains(neighbour)) {
                        parents.put(neighbour, huidigeCrdnt);                                           // voeg huidigeCrdnt toe als parent van de neighbour
                        openList.add(neighbour);                                                        // voeg neighbour toe aan de openList
                        terreinKosten.put(neighbour, nieuweKostenNeighbour);                            // voeg neighbour toe aan de terreinkosten map
                    } else {
                        parents.put(neighbour, huidigeCrdnt);                                           // update de parent van de neighbour
                        terreinKosten.put(neighbour, nieuweKostenNeighbour);                            // update de kosten van neighbour
                    }
                }
            }
            terreinKosten.remove(huidigeCrdnt);                                                         // verwijder huidigeCrdnt coordinaat uit de map met terreinkosten
        }
        System.out.println(pad.getTotaleTijd());
        // set debugger om algoritme visueel zichtbaar te maken
        debug.debugPad(kaart, start, pad);

        // return het pad
        return pad;
    }

    // maak het pad door de parents te volgen van eind naar start
    public void maakPad(Kaart kaart, Coordinaat start, Coordinaat eind) {
        ArrayList<Coordinaat> coordinaten = new ArrayList<>();                                          // lijst met coordinaten van pad  
        Coordinaat huidig = eind;                                                                       // zet huidigeCrdnt coordinaat op eindpunt

        // volg de parents vanaf het eind tot het bij start is
        while (!huidig.equals(start)) {
            coordinaten.add(huidig);
            huidig = parents.get(huidig);
        }
        coordinaten.add(start);                                                                         // voeg de start toe aan de lijst met coordinaten
        Collections.reverse(coordinaten);                                                               // reverse de lijst, zodat hij van start naar eind gaat

        ArrayList terreinen = new ArrayList<>();                                                        // lijst met terreinen
        Richting[] richtingen = new Richting[coordinaten.size() - 1];                                   // lijst met richtingen

        // converteer de coordinaten naar terreinen en richtingen
        for (int i = 0; i < coordinaten.size() - 1; i++) {
            terreinen.add(kaart.getTerreinOp(coordinaten.get(i + 1)));
            richtingen[i] = Richting.tussen(coordinaten.get(i), coordinaten.get(i + 1));
        }
        pad.setTotaleTijd(terreinen);                                                                   // set de totale tijd van het pad
        pad.setBewegingen(richtingen);                                                                  // set de bewegingen met richtingen
    }

    // bewegingskosten om code wat compacter te maken
    public int bewegingsKosten(Kaart kaart, Coordinaat c) {
        return kaart.getTerreinOp(c).getTerreinType().getBewegingspunten();
    }

    // debugger
    @Override
    public void setDebugger(Debugger debugger) {
        this.debug = debugger;
    }
}
