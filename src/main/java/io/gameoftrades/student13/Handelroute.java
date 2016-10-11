package io.gameoftrades.student13;

import io.gameoftrades.model.kaart.*;
import io.gameoftrades.model.markt.Handel;
import io.gameoftrades.model.markt.Handelswaar;

public class Handelroute implements Comparable<Handelroute> {

    private final SnelstePadAlgoritmeImpl snelstePadTussenSteden = new SnelstePadAlgoritmeImpl();
    private final Pad padTussenSteden;
    private Pad padNaarBegin;
    private final Handel aanbod;
    private final Handel vraag;

    public Handelroute(Kaart kaart, Handel aanbod, Handel vraag) {       
        this.aanbod = aanbod;
        this.vraag = vraag;
        this.padTussenSteden = snelstePadTussenSteden.bereken(kaart, aanbod.getStad().getCoordinaat(), vraag.getStad().getCoordinaat());
    }

    public Stad getStartPunt() {
        return this.aanbod.getStad();
    }

    public Stad getEindPunt() {
        return this.vraag.getStad();
    }
    
    public Handel getAanbod() {
        return this.aanbod;
    }
    
    public Handel getVraag() {
        return this.vraag;
    }

    public Handelswaar getHandelswaar() {
        return this.aanbod.getHandelswaar();
    }

    public int getRouteTijd() {
        int tijd = this.padNaarBegin.getTotaleTijd() + this.padTussenSteden.getTotaleTijd();
        return tijd;
    }
    
    public void setPadNaarBegin(Pad pad) {
        this.padNaarBegin = pad;
    }
    
    public Pad getPadNaarBegin() {
        return this.padNaarBegin;
    }
    
    public Pad getPadTussenSteden() {
        return this.padTussenSteden;
    }

    public double berekenHandelScore(int kapitaal, int capaciteit) {
        int maxAantal = (int) Math.floor(kapitaal / this.aanbod.getPrijs());
        if (maxAantal > capaciteit) {
            maxAantal = capaciteit;
        } else if (maxAantal < 0) {
            maxAantal = 0;
        }
        double efficientie = (maxAantal * (this.vraag.getPrijs() - this.aanbod.getPrijs())) / getRouteTijd();
        return efficientie;
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