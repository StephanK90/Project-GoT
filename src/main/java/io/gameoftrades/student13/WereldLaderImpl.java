package io.gameoftrades.student13;

import io.gameoftrades.model.Wereld;
import io.gameoftrades.model.kaart.*;
import io.gameoftrades.model.markt.*;
import io.gameoftrades.model.lader.WereldLader;
import java.io.*;
import java.util.ArrayList;

public class WereldLaderImpl implements WereldLader {

    private Wereld wereld;                                                      //Wereld wordt aangemaakt door deze klasse
    private int regelNummer = 0;                                                //regelnummer voor de leesTxtFile, dmv regelnr weet de reader waar die moet zijn. Regel 1 is bijvoorbeeld alstijd de 2 coordinaten van de kaart(hoogte/breedte)
    private int hoogte = 0;                                                     //regelnr 1 de tweede getal is de hoogte
    private int breedte = 0;                                                    //regelnr 1 de eerste getal is de breedte
    private String regel;                                                       //regel voor de reader. De reader leest de regel op en slaat deze op in deze variabele
    private Kaart kaart = null;                                                 //Kaart wordt aangemaakt door deze klasse en wordt meegegeven aan wereld
    private final ArrayList<Stad> steden = new ArrayList<>();                   //een ArrayList van variabele Stad, met daarin de steden die worden uitgelezen van de textFile
    private final ArrayList<Handel> handel = new ArrayList<>();                 //een ArrayList van variable Handel, met daarin de handel die zijn uitgelezen van de textFile
    private InputStream input;                                                  //input om de resource path in om te slaan

    @Override
    public Wereld laad(String resource) {

        input = this.getClass().getResourceAsStream(resource);                  //deze input bevat de Sting resource als Stream

        leesTxtFile();                                                          //deze leest de textFile uit

        Markt markt = new Markt(handel);                                        // maak markt met de meegeven handelList

        this.wereld = new Wereld(kaart, steden, markt);                         // maak wereld, met de meegegeven kaart, stedenList en markt

        return this.wereld;
    }

    public void leesTxtFile() {
        BufferedReader br = new BufferedReader(new InputStreamReader(input));   //Deze reader bevat de input omgezet naar StreamReader.
        int aantalSteden = 0;
        int aantalMarkt = 0;

        //probeer de while loop, en anders vangt hij een IOException onder op in de catch
        try {
            // leest de txt file zolang de regels niet null/leeg 
            while ((regel = br.readLine()) != null) {
                regelNummer++;

                if (regelNummer == 1) {                                         //als de regelnr 1 is.Dit is waar de breedte hoogte van de kaart staan.
                    leesKaart();                                                // leest de kaart
                }

                if (regelNummer >= 2 && regelNummer <= (hoogte + 1)) {           //als de regelnr 2 of hoger is, maar minimaal hoogte+1 of kleiner is. Hij moet lezen tot hoogte + 1 Dat is waar layout van kaart gelezen kan worden
                    leesLayoutKaart();                                          // leest layout van de kaart
                }

                if (hoogte != 0 && regelNummer == (hoogte + 2)) {               //als de regelnr niet 0 is en gelijk is aan hoogte + 2. 
                    aantalSteden = Integer.parseInt(regel.trim());              // leest hoeveel steden er zijn van de file
                }

                if (regelNummer >= (hoogte + 3) && regelNummer <= (hoogte + 2 + aantalSteden)) {//als de regelnr minimaal hoogte+3 is EN hoogte+2 + aantalSteden of kleiner is. hij moet lezen tot hoogte +2 +aantalSteden. Dat is waar steden met coordinaten in de file staan
                    leesStedenMetCoordinaten();                                 // leest steden met coordinaten
                }

                if (hoogte != 0 && regelNummer == (hoogte + aantalSteden + 3)) {//als de regelnr niet 0 is en gelijk is aan hoogte + aantalSteden + 3 is. Dat is waar aantalmarkten in de file te lezen is.
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
        String[] split = regel.trim().split(",");                               //split de regel op. Haal de spaties met trim ertussenuit en split ze aan de hand van de komma's
        breedte = Integer.parseInt(split[0]);                                   //breedte bevat de eerste split, omgezet naar integer
        hoogte = Integer.parseInt(split[1]);                                    //hoogte de tweede split van de regel, omgezet naar integer
        kaart = new Kaart(breedte, hoogte);                                     //maak kaart aan en plaats de breedte en hoogte erin.
    }

    //leest layout van kaart van de file af
    public void leesLayoutKaart() {
        String[] letters = regel.trim().split("");
        //als de opgesplitste regel 'letters' bevat van (Z of B of R of S of G) en de lengte van aantal letters in de regel is even breed als de breedte van de kaart
        if (letters.length == breedte && regel.contains("Z") || regel.contains("B") || regel.contains("R") || regel.contains("S") || regel.contains("G")) {
            for (int i = 0; i < breedte; i++) {                                 //zolang i 0 en kleiner dan breedte i++
                Terrein terrein = new Terrein(kaart, Coordinaat.op(i, regelNummer - 2), TerreinType.fromLetter(letters[i].charAt(0))); //vul de terrein met de kaart, de coordinaat welke op positie i van de forloop is en regelnummer -2)
            }
        } else {
            throw new IllegalArgumentException("De grootte en breedte van de kaart kloppen niet");//anders vang deze exception op met deze melding
        }
    }

    //leest steden met de coordinaten van de file af
    public void leesStedenMetCoordinaten() {
        String[] split = regel.trim().split(",");
        int x = Integer.parseInt(split[0]);
        int y = Integer.parseInt(split[1]);
        if ((x > 0 && y > 0) && (x <= breedte && y <= hoogte)) {                //als x en y groter zijn dan 0, maar wel evengroot of kleiner dan breedte en hoogte(oftewl staat het op de kaart)
            steden.add(new Stad(Coordinaat.op(x - 1, y - 1), split[2]));        //voeg de nieuwe stad te aan de stedenArrayList met de coordinaten - 1. 
        } else {
            throw new IllegalArgumentException("Fout bij lezen van de steden");
        }
    }

    //leest de markt van de file af
    public void leesMarkt() {
        String[] split = regel.trim().split(",");
        for (int i = 0; i < steden.size(); i++) {
            if (split[0].equalsIgnoreCase(steden.get(i).getNaam())) {           //als de eerste split van de regel gelijk is aan(negeer hoofdletters/kleineletters) de naam van de stad waarop i van forloop op dat moment is
                handel.add(new Handel(steden.get(i), HandelType.valueOf(split[1]), new Handelswaar(split[2]), Integer.parseInt(split[3]))); //voeg nieuwe handel met de stad van positie i, met Handeltype positie 2 van regel, met niet handerswaar met positie 3 van regel en positie 4 van regel
                break;                                                          //ga eruit
            } else if ((!split[0].equalsIgnoreCase(steden.get(i).getNaam())) && (i + 1 == steden.size())) { //anders als niet overeenkomt EN i + 1 is evengroot als de lengte van de stedenLijst
                throw new IllegalArgumentException("Fout bij het lezen van de markt");
            }
        }
    }
}
