package io.gameoftrades.student13;

import io.gameoftrades.model.Wereld;
import io.gameoftrades.model.kaart.*;
import io.gameoftrades.model.markt.*;
import io.gameoftrades.model.lader.WereldLader;
import java.io.*;
import java.util.ArrayList;

public class WereldLaderImpl implements WereldLader {

    private Wereld wereld;

    @Override
    public Wereld laad(String resource) {
        try {

            InputStream input = this.getClass().getResourceAsStream(resource);
            BufferedReader br = new BufferedReader(new InputStreamReader(input));

            String regel;
            Kaart kaart = null;
            Markt markt;
            int regelNummer = 0;
            int hoogte = 0;
            int breedte = 0;
            int aantalSteden = 0;
            int aantalMarkt = 0;
            ArrayList<Stad> steden = new ArrayList<>();
            ArrayList<Handel> handel = new ArrayList<>();

            // leest de txt file zolang er regels zijn
            while ((regel = br.readLine()) != null) {
                regelNummer++;

                // leest de kaart
                if (regelNummer == 1) {
                    String[] split = regel.trim().split(",");
                    breedte = Integer.parseInt(split[0]);
                    hoogte = Integer.parseInt(split[1]);
                    kaart = new Kaart(Integer.parseInt(split[0]), Integer.parseInt(split[1]));
                }
                // leest layout van de kaart
                if (regelNummer >= 2 && regelNummer < (hoogte + 2)) {
                    String[] letters = regel.trim().split("");
                    if (letters.length == breedte && regel.contains("Z") || regel.contains("B") || regel.contains("R") || regel.contains("S") || regel.contains("G")) {
                        for (int i = 0; i < breedte; i++) {
                            Terrein terrein = new Terrein(kaart, Coordinaat.op(i , regelNummer - 2), TerreinType.fromLetter(letters[i].charAt(0)));
                        }
                    } else {
                        throw new IllegalArgumentException();
                    }
                }
                // leest hoeveel steden er zijn
                if (hoogte != 0 && regelNummer == (hoogte + 2)) {
                    aantalSteden = Integer.parseInt(regel.trim());
                }

                // leest steden met coordinaten
                if (regelNummer >= (hoogte + 3) && regelNummer <= (hoogte + 2 + aantalSteden)) {
                    String[] split = regel.trim().split(",");
                    int x = Integer.parseInt(split[0]);
                    int y = Integer.parseInt(split[1]);
                    if ((x > 0 && y > 0) && (x <= breedte && y <= hoogte)) {
                        steden.add(new Stad(Coordinaat.op(x-1, y-1), split[2]));
                    } else {
                        throw new IllegalArgumentException();
                    }
                }

                // leest hoeveel markt er is
                if (hoogte != 0 && regelNummer == (hoogte + aantalSteden + 3)) {
                    aantalMarkt = Integer.parseInt(regel.trim());
                }

                // leest de markt
                if (regelNummer >= (hoogte + aantalSteden + 4) && regelNummer <= hoogte + aantalSteden + aantalMarkt + 3) {
                    String[] split = regel.trim().split(",");
                    for (int i = 0; i < steden.size(); i++) {
                        if (split[0].equalsIgnoreCase(steden.get(i).getNaam())) {
                            handel.add(new Handel(steden.get(i), HandelType.valueOf(split[1]), new Handelswaar(split[2]), Integer.parseInt(split[3])));
                            break;
                        } else if ((!split[0].equalsIgnoreCase(steden.get(i).getNaam())) && (i + 1 == steden.size())) {
                            throw new IllegalArgumentException();
                        }
                    }
                }
            }
            // sluit de reader
            input.close();
            // maak markt
            markt = new Markt(handel);
            // maak wereld
            this.wereld = new Wereld(kaart, steden, markt);

        } catch (IOException ex) {

        }
        return this.wereld;
    }
}
