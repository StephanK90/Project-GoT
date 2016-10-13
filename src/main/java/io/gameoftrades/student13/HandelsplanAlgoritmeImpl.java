package io.gameoftrades.student13;

import io.gameoftrades.model.Wereld;
import io.gameoftrades.model.algoritme.HandelsplanAlgoritme;
import io.gameoftrades.model.algoritme.SnelstePadAlgoritme;
import io.gameoftrades.model.kaart.Pad;
import io.gameoftrades.model.kaart.Richting;
import io.gameoftrades.model.markt.*;
import io.gameoftrades.model.markt.actie.*;
import java.util.ArrayList;

public class HandelsplanAlgoritmeImpl implements HandelsplanAlgoritme {

    private Wereld wereld;
    private final ArrayList<Handelroute> handelroutes = new ArrayList<>();

    @Override
    public Handelsplan bereken(Wereld wereld, HandelsPositie positie) {

        this.wereld = wereld;
        ArrayList<Actie> acties = new ArrayList<>();
        int routeIndex = -1;
        
        // maak de handelroutes
        maakHandelroutes();

        // voer uit zolang er nog acties zijn
        while (positie.getMaxActie() != 0) {

            for (int i = 0; i < handelroutes.size(); i++) {
                SnelstePadAlgoritme snelstePad = new SnelstePadAlgoritmeImpl();
                Pad pad = snelstePad.bereken(wereld.getKaart(), positie.getCoordinaat(), handelroutes.get(i).getStartPunt().getCoordinaat());
                handelroutes.get(i).setPadNaarBegin(pad);
                if (positie.isActieBeschikbaar(handelroutes.get(i).getRouteTijd() + 2)) {

                    double handelScore = handelroutes.get(i).berekenHandelScore(positie.getKapitaal(), positie.getRuimte());

                    if (routeIndex == -1 || handelScore > handelroutes.get(routeIndex).berekenHandelScore(positie.getKapitaal(), positie.getRuimte())) {
                        routeIndex = i;
                    }
                }
            }
            // als route nog steeds null is, dan stop, want er is geen route meer beschikbaar voor het aantal beschikbare acties
            if (routeIndex == -1) {
                StopActie stopActie = new StopActie();
                stopActie.voerUit(positie);
                acties.add(stopActie);
                break;
            }
            
            // navigeer naar de stad waar handel gekocht moet worden
            for (Richting richting : handelroutes.get(routeIndex).getPadNaarBegin().getBewegingen()) {
                NavigeerActie navigeer = new NavigeerActie(positie.getCoordinaat(), richting);
                positie = navigeer.voerUit(positie);
                acties.add(navigeer);
            }

            // koop de handel
            KoopActie koop = new KoopActie(handelroutes.get(routeIndex).getAanbod());
            positie = koop.voerUit(positie);
            acties.add(koop);

            // beweeg naar de stad waar de handel verkocht moet worden
            BeweegActie beweegActie = new BeweegActie(wereld.getKaart(), handelroutes.get(routeIndex).getStartPunt(), handelroutes.get(routeIndex).getEindPunt(), handelroutes.get(routeIndex).getPadTussenSteden());
            positie = beweegActie.voerUit(positie);
            acties.addAll(beweegActie.naarNavigatieActies());

            // verkoop de handel
            VerkoopActie verkoop = new VerkoopActie(handelroutes.get(routeIndex).getVraag());
            positie = verkoop.voerUit(positie);
            acties.add(verkoop);

            //zet routeindex weer op -1
            routeIndex = -1;
        }
        // return het handelsplan
        return new Handelsplan(acties);
    }

    // maak alle handel routes die winst opleveren
    public void maakHandelroutes() {
        ArrayList<Handel> vraag = (ArrayList<Handel>) wereld.getMarkt().getVraag();
        ArrayList<Handel> aanbod = (ArrayList<Handel>) wereld.getMarkt().getAanbod();

        for (int i = 0; i < aanbod.size(); i++) {
            for (int j = 0; j < vraag.size(); j++) {

                int opbrengst = vraag.get(j).getPrijs() - aanbod.get(i).getPrijs();

                if (aanbod.get(i).getHandelswaar().equals(vraag.get(j).getHandelswaar()) && opbrengst > 0) {
                    handelroutes.add(new Handelroute(wereld.getKaart(), aanbod.get(i), vraag.get(j)));
                }
            }
        }
    }
}