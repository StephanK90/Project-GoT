/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.gameoftrades.student13;

import io.gameoftrades.debug.*;
import io.gameoftrades.model.algoritme.StedenTourAlgoritme;
import io.gameoftrades.model.kaart.*;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Stephan
 */
public class StedenTourAlgoritmeImpl implements StedenTourAlgoritme, Debuggable {

    private Debugger debug = new DummyDebugger();
    private Coordinaat huidigeStad;
    

    @Override
    public List<Stad> bereken(Kaart kaart, List<Stad> steden) {

        SnelstePadAlgoritmeImpl snelstePad = new SnelstePadAlgoritmeImpl();
        Stad bezochteStad = null;
        Pad pad;
        int kortstePad = 0;
        int huidigPad = 0;
        int count = 0;
        int tijdVanPad = 0;
        ArrayList<Stad> stedenHuidig;
        ArrayList<Stad> stedenKortst = null;
        ArrayList<Stad> teBezoekenSteden;

        for (int i = 0; i < steden.size(); i++) {
            huidigeStad = steden.get(i).getCoordinaat();
            stedenHuidig = new ArrayList<>();
            stedenHuidig.add(steden.get(i));
            teBezoekenSteden = new ArrayList<>(steden);
            teBezoekenSteden.remove(steden.get(i));
            sorteerSteden(teBezoekenSteden);
            boolean b = true;

            while (!teBezoekenSteden.isEmpty() && b == true) {
                if (tijdVanPad == 0 || huidigeStad.afstandTot(teBezoekenSteden.get(count).getCoordinaat()) <= tijdVanPad) {
                    pad = snelstePad.bereken(kaart, huidigeStad, teBezoekenSteden.get(count).getCoordinaat());
                    if (tijdVanPad == 0 || pad.getTotaleTijd() < tijdVanPad) {
                        tijdVanPad = pad.getTotaleTijd();
                        bezochteStad = teBezoekenSteden.get(count);
                    }
                } else {
                    count = teBezoekenSteden.size() - 1;
                }
                if (count == teBezoekenSteden.size() - 1) {
                    huidigPad += tijdVanPad;
                    stedenHuidig.add(bezochteStad);
                    huidigeStad = bezochteStad.getCoordinaat();
                    teBezoekenSteden.remove(bezochteStad);
                    sorteerSteden(teBezoekenSteden);
                    tijdVanPad = 0;
                    count = -1;
                    if (kortstePad != 0 && huidigPad > kortstePad) {
                        b = false;
                    }
                }
                count++;
            }
            if (kortstePad == 0 || huidigPad < kortstePad) {
                kortstePad = huidigPad;
                stedenKortst = new ArrayList<>(stedenHuidig);
            }
        }
        debug.debugSteden(kaart, stedenKortst);
        return stedenKortst;
    }

    public void sorteerSteden(List<Stad> steden) {
        for (int i = 0; i < steden.size() - 1; i++) {
            for (int j = i + 1; j < steden.size(); j++) {
                int a = (int) huidigeStad.afstandTot(steden.get(i).getCoordinaat());
                int b = (int) huidigeStad.afstandTot(steden.get(j).getCoordinaat());
                if (b < a) {
                    Stad temp = steden.get(j);
                    steden.remove(j);
                    steden.add(i, temp);
                }
            }
        }
    }

    @Override
    public void setDebugger(Debugger debugger) {
        this.debug = debugger;
    }
}
