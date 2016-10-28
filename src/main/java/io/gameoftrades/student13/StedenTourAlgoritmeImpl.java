package io.gameoftrades.student13;

import io.gameoftrades.debug.*;
import io.gameoftrades.model.algoritme.StedenTourAlgoritme;
import io.gameoftrades.model.kaart.*;
import java.util.ArrayList;
import java.util.List;

public class StedenTourAlgoritmeImpl implements StedenTourAlgoritme, Debuggable {

    private Debugger debug = new DummyDebugger();                                                   // debugger
    private ArrayList<Stad> stedenKortst = null;                                                    // List met steden van de kortste route
    private int kortsteTourTijd = 0;                                                                // tijd van de kortste route
    private Kaart map;                                                                              // kaart
    private long startTime;
    private long stopTime;

    // bereken stedentourpad
    @Override
    public List<Stad> bereken(Kaart kaart, List<Stad> steden) {

        this.map = kaart;                                                                           // zet kaart
        SnelstePadAlgoritmeImpl snelstePad = new SnelstePadAlgoritmeImpl();                         // om het snelstePad naar een stad te berekenen
        Coordinaat huidigeStad;                                                                     // huidig coordinaat
        Stad bezochteStad = null;                                                                   // stad die bezocht is
        int huidigeTourTijd = 0;                                                                    // orgineleRouteTijd van het huidige stedentourpad
        int stadIndex = 0;                                                                          // index nummer van de te bezoeken stad                                                                                                                                  
        int tijdVanKortstePad = 0;                                                                  // orgineleRouteTijd van het snelste tussen 2 steden
        ArrayList<Stad> stedenHuidig;                                                               // lijst met steden van huidige stedentour
        ArrayList<Stad> teBezoekenSteden;                                                           // lijst met steden die nog niet bezocht zijn
        startTime = System.currentTimeMillis();

        // loop door de lijst met steden gebruik iedere stad een keer als startpunt
        for (int i = 0; i < steden.size(); i++) {
            huidigeStad = steden.get(i).getCoordinaat();                                            // zet huidige stad
            stedenHuidig = new ArrayList<>();                                                       // nieuwe list om steden in op te slaan
            stedenHuidig.add(steden.get(i));                                                        // voeg huidig toe aan de list
            teBezoekenSteden = new ArrayList<>(steden);                                             // kopieer de steden in de teBezoekenSteden list
            teBezoekenSteden.remove(steden.get(i));                                                 // verwijder de huidige stad uit teBezoekenSteden list
            sorteerSteden(teBezoekenSteden, huidigeStad);                                           // sorteer de steden
            boolean checkRouteTijd = true;                                                          // boolean om eerder te stoppen wanneer huide route langer is dan de kortste

            // voer uit zolang er nog teBezoekenSteden zijn en checkRouteTijd true is
            while (!teBezoekenSteden.isEmpty() && checkRouteTijd == true) {
                Stad teBezoekenStad = teBezoekenSteden.get(stadIndex);

                // check of directe afstand korter is dan korste snelste tussen steden
                if (tijdVanKortstePad == 0 || afstand(huidigeStad, teBezoekenStad.getCoordinaat()) <= tijdVanKortstePad) {

                    // bereken orgineleRouteTijd van huidige stad naar volgende stad
                    Pad pad = snelstePad.bereken(kaart, huidigeStad, teBezoekenStad.getCoordinaat());

                    // check of er al een snelste is opgeslagen of dat huidige snelste kleine is als korste
                    if (tijdVanKortstePad == 0 || pad.getTotaleTijd() < tijdVanKortstePad) {
                        tijdVanKortstePad = pad.getTotaleTijd();                                    // zet de orgineleRouteTijd van het snelste
                        bezochteStad = teBezoekenStad;                                              // zet de bezochteStad
                    }
                } else {
                    stadIndex = teBezoekenSteden.size() - 1;                                        // zet stadIndex op size-1 zodat loop eerder stopt
                }

                // check of stadIndex gelijk is aan teBezoekenSteden size - 1
                if (stadIndex == teBezoekenSteden.size() - 1) {
                    huidigeTourTijd += tijdVanKortstePad;                                           // tel orgineleRouteTijd van berekende snelste op bij de orgineleRouteTijd van de huidige tour 
                    stedenHuidig.add(bezochteStad);                                                 // sla de bezochte stad op in de huidige steden list
                    huidigeStad = bezochteStad.getCoordinaat();                                     // zet bezochte stad als de nieuwe huidige stad
                    teBezoekenSteden.remove(bezochteStad);                                          // verwijder de bezochte stad uit de list met teBezoekenSteden
                    sorteerSteden(teBezoekenSteden, huidigeStad);                                   // sorteer de steden aan de hand van nieuwe huidige stad
                    tijdVanKortstePad = 0;                                                          // reset de orgineleRouteTijd van snelste
                    stadIndex = -1;                                                                 // reset de stadIndex

                    // check of huideTourPad al langer duurt als korsteTourPad
                    if (kortsteTourTijd != 0 && huidigeTourTijd > kortsteTourTijd) {
                        checkRouteTijd = false;                                                     // zet checkRouteTijd false, zodat algoritme stopt en opnieuw begint met nieuwe start stad
                    }
                }
                stadIndex++;                                                                        // verhoog stadIndex om volgende stad te proberen
            }

            // zet huidigeTourTijd als korsteTourPad nog 0 is of als huidigeTourTijd kleiner is
            if (kortsteTourTijd == 0 || huidigeTourTijd < kortsteTourTijd) {
                kortsteTourTijd = huidigeTourTijd;
                this.stedenKortst = new ArrayList<>(stedenHuidig);                                  // kopieer de lijst met huidge steden in de lijst van korste
            }
        }
        // optimaliseer de gevonden route
        optimaliseerRoute(this.stedenKortst);

        // print route info
        printRouteInfo();

        debug.debugSteden(kaart, this.stedenKortst);                                                // set debugger

        // return de lijst met steden
        return this.stedenKortst;
    }

    // sorteer de lijst met steden aan de hand van coordinaten van dichtstbij naar verste weg
    public void sorteerSteden(List<Stad> steden, Coordinaat huidig) {
        for (int i = 0; i < steden.size() - 1; i++) {
            for (int j = i + 1; j < steden.size(); j++) {
                double a = huidig.afstandTot(steden.get(i).getCoordinaat());
                double b = huidig.afstandTot(steden.get(j).getCoordinaat());

                // als afstand van checkRouteTijd kleiner is dan a, verwissel steden van positie
                if (b < a) {
                    Stad temp = steden.get(j);
                    steden.remove(j);
                    steden.add(i, temp);
                }
            }
        }
    }

    // bereken minimale afstand tussen huidige coordinaat en eindpunt
    public int afstand(Coordinaat huidig, Coordinaat eind) {
        int x = Math.abs(huidig.getX() - eind.getX());
        int y = Math.abs(huidig.getY() - eind.getY());
        return (x + y);
    }

    // debugger
    @Override
    public void setDebugger(Debugger debugger) {
        this.debug = debugger;
    }

    // optimaliseert de route
    public void optimaliseerRoute(ArrayList<Stad> steden) {
        ArrayList<Stad> nieuweStedenList;
        boolean b = false;
        for (int i = 0; i < steden.size() - 1; i++) {
            for (int k = i + 1; k < steden.size(); k++) {

                // verwissel de steden van plaats in de array
                nieuweStedenList = new ArrayList<>(swapSteden(steden, i, k));

                // bereken de nieuwe tijd
                int nieuweTijd = berekenTijd(nieuweStedenList);

                // als nieuwe tijd korter is dan oude tijd, sla deze op
                if (nieuweTijd < kortsteTourTijd) {
                    this.stedenKortst = new ArrayList<>(nieuweStedenList);
                    this.kortsteTourTijd = nieuweTijd;
                    b = true;                                                                       // true als route korter is geworden
                }
            }
        }
        // als b true is, optimaliseer nogmaals
        if (b == true) {
            optimaliseerRoute(this.stedenKortst);
        }
    }

    // maakt een nieuwe List van steden met daarin omgewisselde steden
    public ArrayList<Stad> swapSteden(ArrayList<Stad> steden, int i, int k) {
        ArrayList<Stad> nieuweStedenList = new ArrayList<>();
        // add alle steden die voor index i zitten in normale volgorde 
        for (int j = 0; j < i; j++) {
            nieuweStedenList.add(steden.get(j));
        }
        // add alle steden tussen i en k in omgekeerde volgorde
        int value = k;
        while (value >= i) {
            nieuweStedenList.add(steden.get(value));
            value--;
        }
        // add alle steden die na index k komen in normale volgorde
        int value2 = k + 1;
        while (value2 < steden.size()) {
            nieuweStedenList.add(steden.get(value2));
            value2++;
        }
        // return nieuwe list met steden
        return nieuweStedenList;
    }

    // bereken de totale tourtijd
    public int berekenTijd(ArrayList<Stad> steden) {
        int routeTijd = 0;
        SnelstePadAlgoritmeImpl snelste = new SnelstePadAlgoritmeImpl();
        for (int i = 0; i < steden.size() - 1; i++) {
            Pad pad = snelste.bereken(map, steden.get(i).getCoordinaat(), steden.get(i + 1).getCoordinaat());
            routeTijd = routeTijd + pad.getTotaleTijd();
        }
        // return de tourtijd
        return routeTijd;
    }

    // print tijd van de route en tijd voor het maken van de route
    public void printRouteInfo() {
        stopTime = System.currentTimeMillis();
        System.out.println("Route gevonden in: " + (stopTime - startTime) + "ms");
        System.out.println("Kosten van het pad: " + kortsteTourTijd);
        System.out.println("----------------------------------------------------");
    }
}
