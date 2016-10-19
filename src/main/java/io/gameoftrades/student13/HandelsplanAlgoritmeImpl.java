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

    private ArrayList<Handelroute> handelroutes;                                                // lijst waar alle handelroutes in worden opgeslagen
    private Handelroute besteRoute = null;                                                      // hierin wordt de best mogelijke route in opgeslagen

    @Override
    public Handelsplan bereken(Wereld wereld, HandelsPositie positie) {

        ArrayList<Actie> acties = new ArrayList<>();                                            // lijst waar alle acties in worden opgeslagen

        // maak de handelroutes
        handelroutes = maakHandelroutes(wereld);                                                // maak handelroutes

        // voer uit zolang er nog acties zijn
        while (positie.getMaxActie() != 0) {

            getBestMogelijkeRoute(wereld, positie);                                             // haal de best mogelijke route op

            // als besteRoute nog steeds null is, dan stop 
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
            Stad van = this.besteRoute.getAanbod().getStad();                                   // begin stad
            Stad naar = this.besteRoute.getVraag().getStad();                                   // eind stad
            Pad pad = this.besteRoute.getPadTussenSteden();                                     // pad tussen de steden
            BeweegActie beweegActie = new BeweegActie(wereld.getKaart(), van, naar, pad);
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
    public ArrayList<Handelroute> maakHandelroutes(Wereld wereld) {
        ArrayList<Handelroute> routes = new ArrayList<>();

        for (int i = 0; i < wereld.getMarkt().getAanbod().size(); i++) {
            for (int j = 0; j < wereld.getMarkt().getVraag().size(); j++) {

                // bereken de opbrengst van de route
                Handel aanbod = wereld.getMarkt().getAanbod().get(i);
                Handel vraag = wereld.getMarkt().getVraag().get(j);
                int opbrengst = vraag.getPrijs() - aanbod.getPrijs();

                // als de handelwaar hetzelfde is en het geeft winst, sla dan de route op
                if (aanbod.getHandelswaar().equals(vraag.getHandelswaar()) && opbrengst > 0) {
                    routes.add(new Handelroute(wereld.getKaart(), aanbod, vraag));
                }
            }
        }
        return routes;
    }

    // haalt de best mogelijke route uit de lijst en slaat deze op als besteRoute
    public void getBestMogelijkeRoute(Wereld wereld, HandelsPositie positie) {        
        for (int i = 0; i < handelroutes.size(); i++) {
            Handelroute huidig = handelroutes.get(i);

            // bereken pad naar begin van de route
            SnelstePadAlgoritme snelstePad = new SnelstePadAlgoritmeImpl();
            Pad pad = snelstePad.bereken(wereld.getKaart(), positie.getCoordinaat(), huidig.getAanbod().getStad().getCoordinaat());
            huidig.setPadNaarBegin(pad);

            // check of route mogelijk is
            if (positie.isActieBeschikbaar(huidig.getRouteTijd() + 2)) {

                // bereken handelscore voor huidige route
                double handelScore = huidig.berekenHandelScore(positie.getKapitaal(), positie.getRuimte());

                // als besteRoute nog null is of huidige route is beter dan de besteRoute, zet huidige route als besteRoute
                if (this.besteRoute == null || handelScore > this.besteRoute.getScore()) {
                    this.besteRoute = huidig;
                }
            }
        }
    }
}
