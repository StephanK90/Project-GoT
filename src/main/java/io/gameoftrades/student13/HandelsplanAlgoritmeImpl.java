/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.gameoftrades.student13;

import io.gameoftrades.model.Wereld;
import io.gameoftrades.model.algoritme.HandelsplanAlgoritme;
import io.gameoftrades.model.algoritme.SnelstePadAlgoritme;
import io.gameoftrades.model.kaart.*;
import io.gameoftrades.model.markt.*;
import io.gameoftrades.model.markt.actie.*;
import java.util.ArrayList;

/**
 *
 * @author Stephan
 */
public class HandelsplanAlgoritmeImpl implements HandelsplanAlgoritme {

    private final ArrayList<Handelroute> handelroutes = new ArrayList<>();                      // lijst waar alle handelroutes in worden opgeslagen
    private Handelroute besteRoute = null;                                                      // hierin wordt de best mogelijke route in opgeslagen

    @Override
    public Handelsplan bereken(Wereld wereld, HandelsPositie positie) {

        ArrayList<Actie> acties = new ArrayList<>();                                            // lijst waar alle acties in worden opgeslagen

        // maak de handelroutes
        maakHandelroutes(wereld);

        // voer uit zolang er nog acties zijn
        while (positie.getMaxActie() != 0) {

            // haal de best mogelijke handelroute
            getBestMogelijkeRoute(wereld, positie);

            // als besteRoute nog steeds null is, dan stop 
            // want er is geen besteRoute meer beschikbaar voor het aantal beschikbare acties
            if (besteRoute == null) {
                break;
            }

            // navigeer naar de stad waar handel gekocht moet worden
            for (Richting richting : this.besteRoute.getPadNaarBegin().getBewegingen()) {
                NavigeerActie navigeer = new NavigeerActie(positie.getCoordinaat(), richting);
                positie = navigeer.voerUit(positie);
                acties.add(navigeer);
            }

            // koop de handel
            KoopActie koop = new KoopActie(this.besteRoute.getAanbod());
            positie = koop.voerUit(positie);
            acties.add(koop);

            // beweeg naar de stad waar de handel verkocht moet worden
            BeweegActie beweegActie = new BeweegActie(wereld.getKaart(), this.besteRoute.getAanbod().getStad(), this.besteRoute.getVraag().getStad(), this.besteRoute.getPadTussenSteden());
            positie = beweegActie.voerUit(positie);
            acties.addAll(beweegActie.naarNavigatieActies());

            // verkoop de handel
            VerkoopActie verkoop = new VerkoopActie(this.besteRoute.getVraag());
            positie = verkoop.voerUit(positie);
            acties.add(verkoop);

            //zet besteRoute weer op null
            this.besteRoute = null;
        }
        // return het handelsplan
        return new Handelsplan(acties);
    }

    // maak alle handel routes die winst opleveren
    public void maakHandelroutes(Wereld wereld) {
        ArrayList<Handel> vraag = (ArrayList<Handel>) wereld.getMarkt().getVraag();             // lijst met vraag van alle steden   
        ArrayList<Handel> aanbod = (ArrayList<Handel>) wereld.getMarkt().getAanbod();           // lijst met aanbod van alle steden

        for (int i = 0; i < aanbod.size(); i++) {
            for (int j = 0; j < vraag.size(); j++) {

                // bereken de opbrengst van de route
                int opbrengst = vraag.get(j).getPrijs() - aanbod.get(i).getPrijs();

                // als de handelwaar hetzelfde is en het geeft winst, sla dan de route op
                if (aanbod.get(i).getHandelswaar().equals(vraag.get(j).getHandelswaar()) && opbrengst > 0) {
                    handelroutes.add(new Handelroute(wereld.getKaart(), aanbod.get(i), vraag.get(j)));
                }
            }
        }
    }

    // haalt de best mogelijke route uit de lijst en slaat deze op als besteRoute
    public void getBestMogelijkeRoute(Wereld wereld, HandelsPositie positie) {
        for (int i = 0; i < handelroutes.size(); i++) {
            Handelroute huidig = handelroutes.get(i);
            SnelstePadAlgoritme snelstePad = new SnelstePadAlgoritmeImpl();
            
            // bereken het pad van huidge positie naar begin van route
            Pad pad = snelstePad.bereken(wereld.getKaart(), positie.getCoordinaat(), huidig.getAanbod().getStad().getCoordinaat());           
            huidig.setPadNaarBegin(pad);
            
            // check of route wel mogelijk is qua beschikbare acties (tijd van route + koop en verkoop)
            if (positie.isActieBeschikbaar(huidig.getRouteTijd() + 2)) {

                // bereken handelscore voor huidige route
                double handelScore = huidig.berekenHandelScore(positie.getKapitaal(), positie.getRuimte());

                // als besteRoute nog null is of huidige route is beter dan de besteRoute, zet huidige route als besteRoute
                if (this.besteRoute == null || handelScore > this.besteRoute.berekenHandelScore(positie.getKapitaal(), positie.getRuimte())) {
                    this.besteRoute = huidig;
                }
            }
        }
    }
}