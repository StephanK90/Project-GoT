/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.gameoftrades.student13;

import io.gameoftrades.model.kaart.*;
import io.gameoftrades.model.markt.Handel;

/**
 *
 * @author Stephan
 */
public class Handelroute implements Comparable<Handelroute> {

    private final SnelstePadAlgoritmeImpl snelstePad = new SnelstePadAlgoritmeImpl();           // snelstepadalgoritme om pad tussen de 2 steden te berekenen
    private final Pad padTussenSteden;                                                          // het pad tussen de 2 steden
    private Pad padNaarBegin;                                                                   // het pad naar de stad met de aanbod
    private final Handel aanbod;                                                                // de aanbod
    private final Handel vraag;                                                                 // de vraag

    // constructor
    public Handelroute(Kaart kaart, Handel aanbod, Handel vraag) {
        this.aanbod = aanbod;
        this.vraag = vraag;
        this.padTussenSteden = snelstePad.bereken(kaart, aanbod.getStad().getCoordinaat(), vraag.getStad().getCoordinaat());
    }

    // returned het aanbod
    public Handel getAanbod() {
        return this.aanbod;
    }

    // returned de vraag
    public Handel getVraag() {
        return this.vraag;
    }

    // returned de totale tijd (tijd tussen de handelsteden + tijd van huidige positie naar stad met aanbod)
    public int getRouteTijd() {
        int tijd = this.padNaarBegin.getTotaleTijd() + this.padTussenSteden.getTotaleTijd();
        return tijd;
    }

    // zet het pad naar de stad met het aanbod
    public void setPadNaarBegin(Pad pad) {
        this.padNaarBegin = pad;
    }

    // returned het pad naar stad met de aanbod
    public Pad getPadNaarBegin() {
        return this.padNaarBegin;
    }

    // returned het pad tussen de 2 steden
    public Pad getPadTussenSteden() {
        return this.padTussenSteden;
    }

    // berekent de score voor de handelroute qua opbrengst en tijdsduur
    public double berekenHandelScore(int kapitaal, int capaciteit) {
        double maxAantal = (int) Math.floor(kapitaal / this.aanbod.getPrijs());                 // berekent het max aantal producten die gekocht kunnen worden
        if (maxAantal > capaciteit) {
            maxAantal = capaciteit;
        } else if (maxAantal < 0) {
            maxAantal = 0;
        }
        // bereken de handelscore
        double score = (maxAantal * (this.vraag.getPrijs() - this.aanbod.getPrijs())) / getRouteTijd();
        return score;
    }

    @Override
    public int compareTo(Handelroute o) {
        if ((o.getVraag().getPrijs() - o.getAanbod().getPrijs()) < (this.vraag.getPrijs() - this.aanbod.getPrijs())) {
            return -1;
        } else if (o.getRouteTijd() == this.getRouteTijd()) {
            return 0;
        } else {
            return 1;
        }
    }
}
