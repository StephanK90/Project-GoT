/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.gameoftrades.student13;

import io.gameoftrades.model.algoritme.StedenTourAlgoritme;
import io.gameoftrades.model.kaart.*;
import java.util.ArrayList;
import java.util.List;
import io.gameoftrades.debug.Debuggable;
import io.gameoftrades.debug.Debugger;
import io.gameoftrades.debug.DummyDebugger;

/**
 *
 * @author Charlie
 */
public class StedenTourAlgoritmeImpl implements StedenTourAlgoritme, Debuggable {

    private Debugger debug = new DummyDebugger();

    @Override
    public List<Stad> bereken(Kaart kaart, List<Stad> steden) {

        SnelstePadAlgoritmeImpl snelstePad = new SnelstePadAlgoritmeImpl();
        Coordinaat huidigeStad = null;
        Stad bezochteStad = null;
        int kortstePad = 0;
        int huidigePad = 0;
        ArrayList<Stad> stedenHuidig = new ArrayList<>();
        ArrayList<Stad> stedenKortst = null;
        ArrayList<Stad> teBezoekenSteden = null;
        int count = 0;
        int tijdVanPad = 0;

        for (int i = 0; i < steden.size(); i++) {
            huidigeStad = steden.get(i).getCoordinaat();
            stedenHuidig.add(steden.get(i));
            teBezoekenSteden = new ArrayList<>(steden);
            teBezoekenSteden.remove(steden.get(i));

            while (!teBezoekenSteden.isEmpty()) {
                Pad pad = snelstePad.bereken(kaart, huidigeStad, teBezoekenSteden.get(count).getCoordinaat());
                if (tijdVanPad == 0 || pad.getTotaleTijd() < tijdVanPad) {
                    tijdVanPad = pad.getTotaleTijd();
                    bezochteStad = teBezoekenSteden.get(count);
                }
                if (count == teBezoekenSteden.size() - 1) {
                    huidigePad += tijdVanPad;
                    stedenHuidig.add(bezochteStad);
                    huidigeStad = bezochteStad.getCoordinaat();
                    teBezoekenSteden.remove(bezochteStad);
                    count = -1;
                    tijdVanPad = 0;
                }
                count++;
            }
            if (kortstePad == 0 || huidigePad < kortstePad) {
                kortstePad = huidigePad;
                stedenKortst = new ArrayList<>(stedenHuidig);
            }
        }
        debug.debugSteden(kaart, stedenKortst);
        return stedenKortst;
    }

    @Override
    public void setDebugger(Debugger debugger) {
        this.debug = debugger;
    }
}
