package io.gameoftrades.student13;

import io.gameoftrades.model.Wereld;
import io.gameoftrades.model.kaart.*;
import io.gameoftrades.model.markt.*;
import io.gameoftrades.model.lader.WereldLader;
import java.io.*;
import java.util.ArrayList;

public class WereldLaderImpl implements WereldLader {

    private Wereld wereld;
    private int regelNummer = 0;
    private int hoogte = 0;
    private int breedte = 0;
    private String regel;
    private Kaart kaart = null;
    private final ArrayList<Stad> steden = new ArrayList<>();
    private final ArrayList<Handel> handel = new ArrayList<>();
    private InputStream input;

    @Override
    public Wereld laad(String resource) {

        input = this.getClass().getResourceAsStream(resource);

        leesTxtFile();

        Markt markt = new Markt(handel);                                        // maak markt

        this.wereld = new Wereld(kaart, steden, markt);                         // maak wereld

        return this.wereld;
    }

    public void leesTxtFile() {
        BufferedReader br = new BufferedReader(new InputStreamReader(input));
        int aantalSteden = 0;
        int aantalMarkt = 0;

        //probeer de while loop, en anders vangt hij een IOException onder op in de catch
        try {
            // leest de txt file zolang er regels zijn
            while ((regel = br.readLine()) != null) {
                regelNummer++;

                if (regelNummer == 1) {
                    leesKaart();                                                // leest de kaart
                }

                if (regelNummer >= 2 && regelNummer < (hoogte + 2)) {
                    leesLayoutKaart();                                          // leest layout van de kaart
                }

                if (hoogte != 0 && regelNummer == (hoogte + 2)) {
                    aantalSteden = Integer.parseInt(regel.trim());              // leest hoeveel steden er zijn van de file
                }

                if (regelNummer >= (hoogte + 3) && regelNummer <= (hoogte + 2 + aantalSteden)) {
                    leesStedenMetCoordinaten();                                 // leest steden met coordinaten
                }

                if (hoogte != 0 && regelNummer == (hoogte + aantalSteden + 3)) {
                    aantalMarkt = Integer.parseInt(regel.trim());               // leest hoeveel markt er is van de file
                }

                if (regelNummer >= (hoogte + aantalSteden + 4) && regelNummer <= hoogte + aantalSteden + aantalMarkt + 3) {
                    leesMarkt();                                                // leest de markt
                }
            }

            input.close();                                                      // sluit de reader
        } catch (IOException ex) {
            System.out.println("Fout bij het lezen van de text file");
        }
    }

    //leest de kaart van de file af
    public void leesKaart() {
        String[] split = regel.trim().split(",");
        breedte = Integer.parseInt(split[0]);
        hoogte = Integer.parseInt(split[1]);
        kaart = new Kaart(Integer.parseInt(split[0]), Integer.parseInt(split[1]));
    }

    //leest layout van kaart van de file af
    public void leesLayoutKaart() {
        String[] letters = regel.trim().split("");
        if (letters.length == breedte && regel.contains("Z") || regel.contains("B") || regel.contains("R") || regel.contains("S") || regel.contains("G")) {
            for (int i = 0; i < breedte; i++) {
                Terrein terrein = new Terrein(kaart, Coordinaat.op(i, regelNummer - 2), TerreinType.fromLetter(letters[i].charAt(0)));
            }
        } else {
            throw new IllegalArgumentException("De grootte en breedte van de kaart kloppen niet");
        }
    }

    //leest steden met de coordinaten van de file af
    public void leesStedenMetCoordinaten() {
        String[] split = regel.trim().split(",");
        int x = Integer.parseInt(split[0]);
        int y = Integer.parseInt(split[1]);
        if ((x > 0 && y > 0) && (x <= breedte && y <= hoogte)) {
            steden.add(new Stad(Coordinaat.op(x - 1, y - 1), split[2]));
        } else {
            throw new IllegalArgumentException("Fout bij lezen van de steden");
        }
    }

    //leest de markt van de file af
    public void leesMarkt() {
        String[] split = regel.trim().split(",");
        for (int i = 0; i < steden.size(); i++) {
            if (split[0].equalsIgnoreCase(steden.get(i).getNaam())) {
                handel.add(new Handel(steden.get(i), HandelType.valueOf(split[1]), new Handelswaar(split[2]), Integer.parseInt(split[3])));
                break;
            } else if ((!split[0].equalsIgnoreCase(steden.get(i).getNaam())) && (i + 1 == steden.size())) {
                throw new IllegalArgumentException("Fout bij het lezen van de markt");
            }
        }
    }
}
