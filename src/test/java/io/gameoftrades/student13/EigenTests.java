/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.gameoftrades.student13;

import io.gameoftrades.model.Handelaar;
import io.gameoftrades.model.Wereld;
import io.gameoftrades.model.algoritme.SnelstePadAlgoritme;
import io.gameoftrades.model.kaart.Coordinaat;
import io.gameoftrades.model.kaart.Pad;
import io.gameoftrades.model.lader.WereldLader;
import io.gameoftrades.model.markt.Handel;
import io.gameoftrades.model.markt.actie.HandelsPositie;
import java.util.ArrayList;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Before;

/**
 *
 * @author Stephan
 */
public class EigenTests {

    private Handelaar handelaar;

    @Before
    public void init() {
        handelaar = new HandelaarImpl();
    }

    // test om te controleren of de score van een handelroute wel juist berekent wordt
    @Test
    public void berekenScoreTest() {

        // maak wereld
        WereldLader lader = handelaar.nieuweWereldLader();
        Wereld wereld = lader.laad("/kaarten/voorbeeld-kaart.txt");

        // check of wereld niet null is
        assertNotNull(wereld);

        // maak het SnelstePadAlgoritme
        SnelstePadAlgoritme algoritme = handelaar.nieuwSnelstePadAlgoritme();

        // check of algoritme niet null is
        assertNotNull(algoritme);

        // maak de handel en de handelroute
        Handel aanbod = wereld.getMarkt().getAanbod().get(0);
        Handel vraag = wereld.getMarkt().getVraag().get(0);
        Handelroute route = new Handelroute(wereld.getKaart(), aanbod, vraag);

        // maak de handelspositie
        HandelsPositie positie = new HandelsPositie(wereld, wereld.getSteden().get(3), 150, 10, 100);

        // maak het pad
        Pad pad = algoritme.bereken(wereld.getKaart(), positie.getCoordinaat(), aanbod.getStad().getCoordinaat());
        route.setPadNaarBegin(pad);

        // bereken de score
        double score = route.berekenHandelScore(positie.getKapitaal(), positie.getRuimte());

        // check of score klopt
        assertEquals(5.35, score, 0.01);
    }

    // test om te controleren of het juiste aantal handelroutes worden gemaakt
    @Test
    public void maakHandelroutesTest() {
        // maak de wereld
        WereldLader lader = handelaar.nieuweWereldLader();
        Wereld wereld = lader.laad("/kaarten/voorbeeld-kaart.txt");

        // check of wereld niet null is
        assertNotNull(wereld);

        // maak handelsplan
        HandelsplanAlgoritmeImpl plan = new HandelsplanAlgoritmeImpl();

        // maak de handelroutes
        ArrayList<Handelroute> routes = plan.maakHandelroutes(wereld);

        // check of juiste aantal routes gemaakt zijn
        assertEquals(3, routes.size());
    }

    // test de methode die de afstand tussen 2 coordinaten berekend
    @Test
    public void berekenAfstandTest() {
        // maak wereld
        WereldLader lader = handelaar.nieuweWereldLader();
        Wereld wereld = lader.laad("/kaarten/voorbeeld-kaart.txt");

        // check of wereld niet null is
        assertNotNull(wereld);

        // maak coordinaten
        Coordinaat crdnt1 = Coordinaat.op(1, 7);
        Coordinaat crdnt2 = Coordinaat.op(7, 1);

        // maak SnelstePadAlgoritme
        StedenTourAlgoritmeImpl tour = new StedenTourAlgoritmeImpl();

        // bereken afstand
        int afstand = tour.afstand(crdnt1, crdnt2);

        // check of afstand klopt
        assertEquals(12, afstand);

    }
}
