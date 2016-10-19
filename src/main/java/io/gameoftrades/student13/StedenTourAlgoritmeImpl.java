package io.gameoftrades.student13;

import io.gameoftrades.debug.*;
import io.gameoftrades.model.algoritme.StedenTourAlgoritme;
import io.gameoftrades.model.kaart.*;
import java.util.ArrayList;
import java.util.List;

public class StedenTourAlgoritmeImpl implements StedenTourAlgoritme, Debuggable {

    private Debugger debug = new DummyDebugger();                                                   // debugger

    // bereken stedentourpad
    @Override
    public List<Stad> bereken(Kaart kaart, List<Stad> steden) {

        SnelstePadAlgoritmeImpl snelstePad = new SnelstePadAlgoritmeImpl();                         // om het snelstePad naar een stad te berekenen
        Coordinaat huidigeStad;                                                                     // huidig coordinaat
        Stad bezochteStad = null;                                                                   // stad die bezocht is
        int kortsteTourTijd = 0;                                                                    // tijd van het korste stedentourpad
        int huidigeTourTijd = 0;                                                                    // tijd van het huidige stedentourpad
        int stadIndex = 0;                                                                          // index nummer van de te bezoeken stad                                                                                                                                  
        int tijdVanPad = 0;                                                                         // tijd van het pad tussen 2 steden
        ArrayList<Stad> stedenHuidig;                                                               // lijst met steden van huidige stedentour
        ArrayList<Stad> stedenKortst = null;                                                        // lijst met steden van kortste stedentour
        ArrayList<Stad> teBezoekenSteden;                                                           // lijst met steden die nog niet bezocht zijn

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

                // check of directe afstand korter is dan korste pad tussen steden
                if (tijdVanPad == 0 || afstand(huidigeStad, teBezoekenStad.getCoordinaat()) <= tijdVanPad) {                   

                    // bereken tijd van huidige stad naar volgende stad
                    Pad pad = snelstePad.bereken(kaart, huidigeStad, teBezoekenStad.getCoordinaat());

                    // check of er al een pad is opgeslagen of dat huidige pad kleine is als korste
                    if (tijdVanPad == 0 || pad.getTotaleTijd() < tijdVanPad) {
                        tijdVanPad = pad.getTotaleTijd();                                           // zet de tijd van het pad
                        bezochteStad = teBezoekenStad;                                              // zet de bezochteStad
                    }
                } else {
                    stadIndex = teBezoekenSteden.size() - 1;                                        // zet stadIndex op size-1 zodat loop eerder stopt
                }

                // check of stadIndex gelijk is aan teBezoekenSteden size - 1
                if (stadIndex == teBezoekenSteden.size() - 1) {
                    huidigeTourTijd += tijdVanPad;                                                  // tel tijd van berekende pad op bij de tijd van de huidige tour 
                    stedenHuidig.add(bezochteStad);                                                 // sla de bezochte stad op in de huidige steden list
                    huidigeStad = bezochteStad.getCoordinaat();                                     // zet bezochte stad als de nieuwe huidige stad
                    teBezoekenSteden.remove(bezochteStad);                                          // verwijder de bezochte stad uit de list met teBezoekenSteden
                    sorteerSteden(teBezoekenSteden, huidigeStad);                                   // sorteer de steden aan de hand van nieuwe huidige stad
                    tijdVanPad = 0;                                                                 // reset de tijd van pad
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
                stedenKortst = new ArrayList<>(stedenHuidig);                                       // kopieer de lijst met huidge steden in de lijst van korste
            }
        }
        debug.debugSteden(kaart, stedenKortst);                                                     // set debugger

        // return de lijst met steden
        return stedenKortst;
    }

    // sorteer de lijst met steden aan de hand van coordinaten van dichtstbij naar verste weg
    public void sorteerSteden(List<Stad> steden, Coordinaat huidig) {
        for (int i = 0; i < steden.size() - 1; i++) {
            for (int j = i + 1; j < steden.size(); j++) {
                int a = afstand(huidig, steden.get(i).getCoordinaat());
                int b = afstand(huidig, steden.get(j).getCoordinaat());

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
}
