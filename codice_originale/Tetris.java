import java.awt.*;
import java.awt.event.*;
import java.util.Random;
import javax.swing.*;

/**
 * Tetris
 *
 * Documentazione generale (scopo e possibili estensioni):
 * - Questa classe estende JPanel e implementa ActionListener per gestire il loop di gioco
 *   tramite un Timer Swing. Contiene sia la logica di gioco (gestione pezzi, collisioni,
 *   punteggio, livelli) sia il rendering (paintComponent) e l'input da tastiera.
 * - Il codice è volutamente in un singolo file per semplicità; per progetti più grandi è
 *   consigliabile separare il modello (GameState), la vista (Renderer) e il controller
 *   (Input/Timer) in classi distinte.
 * - Punti d'estensione suggeriti per feature future:
 *   * Salvataggio / caricamento stato partita (serializzare "griglia", punteggio, prossimo, hold).
 *   * Modalità multigiocatore o con classifiche online (aggiungere networking o API REST).
 *   * Personalizzazione controlli, skin, opzioni audio (aggiungere pannello di opzioni).
 *   * Replay / timeline delle mosse (registrare eventi actionPerformed/gestisciTasto).
 */
public class Tetris extends JPanel implements ActionListener {
    /* Costanti di configurazione del gioco. Mantieni qui i parametri principali per
       facilitare tuning e bilanciamento. */
    private static final int COLONNE = 10, RIGHE = 20, CELLA = 30;
    private static final int VELOCITA_INIZIALE = 500, VELOCITA_MIN = 100;
    private static final int[] PUNTI = {0, 100, 300, 500, 800}; // punti per linee contemporanee

    /**
     * Enum Tipo - definisce ogni tipo di tetramino con la sua forma di base e il colore.
     *
     * Dettagli utili per feature future:
     * - Le forme sono matrici 0/1; la rotazione viene gestita creando una matrice ruotata.
     * - Per supportare forme più complesse o dati aggiuntivi (es. offset di spawn, SRS)
     *   potremmo estendere l'enum con parametri addizionali (es. pivot point, spawnOffset).
     */
    enum Tipo {
        NESSUNO(new int[][]{{0}}, Color.BLACK),
        I(new int[][]{{0,0,0,0},{1,1,1,1},{0,0,0,0},{0,0,0,0}}, new Color(0,245,255)),
        O(new int[][]{{1,1},{1,1}}, new Color(255,235,59)),
        T(new int[][]{{0,1,0},{1,1,1},{0,0,0}}, new Color(224,64,251)),
        S(new int[][]{{0,1,1},{1,1,0},{0,0,0}}, new Color(105,240,174)),
        Z(new int[][]{{1,1,0},{0,1,1},{0,0,0}}, new Color(255,82,82)),
        J(new int[][]{{1,0,0},{1,1,1},{0,0,0}}, new Color(68,138,255)),
        L(new int[][]{{0,0,1},{1,1,1},{0,0,0}}, new Color(255,171,64));

        final int[][] forma; final Color colore;
        Tipo(int[][] f, Color c) { forma = f; colore = c; }

        /**
         * Restituisce una copia profonda della matrice forma. Importante per evitare
         * che modifiche temporanee sulla forma corrente modifichino la definizione
         * del Tipo (immutabilità desiderabile).
         *
         * Rischi/Edge-case:
         * - La funzione clona le righe ma non valida la forma; se si aggiungono forme
         *   irregolari, implementare controlli.
         */
        int[][] getForma() {
            int[][] copia = new int[forma.length][];
            for (int i = 0; i < forma.length; i++) copia[i] = forma[i].clone();
            return copia;
        }
    }

    private static final Tipo[] TIPI = {Tipo.I, Tipo.O, Tipo.T, Tipo.S, Tipo.Z, Tipo.J, Tipo.L};

    /**
     * Imposta la velocità di caduta dei pezzi in base alla difficoltà selezionata.
     * 
     * @param diff Stringa che indica la difficoltà ("facile", "normale", "difficile", "impossibile").
     *             Se null o non riconosciuta, viene applicata la velocità iniziale di default.
     * 
     * Valori di velocità (in millisecondi tra ogni caduta):
     * - Facile: 800ms (più lento)
     * - Normale: VELOCITA_INIZIALE (default)
     * - Difficile: 300ms
     * - Impossibile: 150ms (più veloce)
     */
    private void setDifficolta(String diff) {
        if (diff == null) { velocita = VELOCITA_INIZIALE; return; }
        switch (diff.toLowerCase()) {
            case "facile" -> velocita = 800;
            case "normale" -> velocita = VELOCITA_INIZIALE;
            case "difficile" -> velocita = 300;
            case "impossibile" -> velocita = 150;
            default -> velocita = VELOCITA_INIZIALE;
        }
    }

    /**
     * Mostra una finestra di dialogo modale per la selezione della difficoltà all'avvio del gioco.
     * Utilizza JOptionPane per presentare tre opzioni: Facile, Normale, Difficile.
     * 
     * Comportamento:
     * - L'opzione "Normale" è preselezionata di default
     * - Se l'utente chiude la finestra senza scegliere, viene applicata la difficoltà normale
     * - La scelta viene passata al metodo setDifficolta() per configurare la velocità
     */
    private void scegliDifficolta() {
        Object[] opzioni = {"Facile", "Normale", "Difficile"};
        int scelta = JOptionPane.showOptionDialog(
                null,
                "Scegli la difficoltà:",
                "Difficoltà",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                opzioni,
                opzioni[1]
        );
        switch (scelta) {
            case 0 -> setDifficolta("facile");
            case 1 -> setDifficolta("normale");
            case 2 -> setDifficolta("difficile");
            default -> setDifficolta("normale");
        }
    }

    /**
     * Costruttore: configura dimensioni pannello, colore di sfondo, listener dei tasti
     * e lancia una nuova partita.
     *
     * Note importanti per UX e testing:
     * - setFocusable(true) e addKeyListener servono per catturare input tastiera; in
     *   scenari più complessi privilegiare InputMap/ActionMap di Swing per testabilità.
     * - Il pannello prevede spazio extra a destra per pannello informazioni (prossimo/hold).
     */
    public Tetris() {
        // scegli difficoltà all'avvio
        scegliDifficolta();

        setPreferredSize(new Dimension(COLONNE * CELLA + 150, RIGHE * CELLA));
        setBackground(new Color(26, 26, 46));
        setFocusable(true);
        addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) { gestisciTasto(e.getKeyCode()); }
        });
        nuovaPartita();
    }

    /**
     * nuovaPartita(): Reset completo dello stato di gioco.
     *
     * Azioni eseguite:
     * - Ferma il timer esistente se presente.
     * - Pulisce la matrice griglia settando ogni cella a Tipo.NESSUNO.
     * - Reset di punteggio, livello, righe totali, velocità, hold e flag.
     * - Inizializza il prossimo pezzo casuale e genera il primo pezzo attivo.
     * - Crea e avvia un nuovo Timer con delay pari a 'velocita'.
     *
     * Considerazioni per feature future:
     * - Aggiungere argomento opzionale per ripristinare stato da salvataggio/seed.
     * - Emettere eventi o callback (es. onGameStart) per integrazione UI/analytics.
     */
    private void nuovaPartita() {
        if (timer != null) timer.stop();
        for (int r = 0; r < RIGHE; r++)
            for (int c = 0; c < COLONNE; c++) griglia[r][c] = Tipo.NESSUNO;
        punteggio = righeTotal = 0; livello = 1;
        // non toccare velocita qui: rimane quella impostata dalla difficoltà
        hold = null; puoHold = true;
        inCorso = true; pausa = gameOver = false;
        prossimo = TIPI[random.nextInt(7)];
        nuovoPezzo();
        timer = new Timer(velocita, this);
        timer.start();
        repaint();
    }

    /**
     * nuovoPezzo(): Sposta il "prossimo" nello stato corrente e genera un nuovo prossimo.
     *
     * Effetti collaterali e regole:
     * - Imposta formaPezzo come copia della forma del tipo scelto.
     * - Posiziona il pezzo nella colonna centrale (calcolo che tiene conto della larghezza della forma).
     * - Reimposta il flag puoHold a true (per consentire hold dopo spawn).
     * - Se la posizione iniziale non è valida, segnala gameOver e ferma il timer.
     *
     * Possibili miglioramenti:
     * - Implementare un sistema di "bag" per random più equo (7-bag), invece del puro random.
     * - Aggiungere spawn offset per pezzi I/O per corrispondere allo standard Tetris Guideline.
     */
    private void nuovoPezzo() {
        tipoPezzo = prossimo;
        prossimo = TIPI[random.nextInt(7)];
        formaPezzo = tipoPezzo.getForma();
        pezzoRiga = 0;
        pezzoCol = COLONNE / 2 - formaPezzo[0].length / 2;
        puoHold = true;
        if (!valido(formaPezzo, pezzoRiga, pezzoCol)) {
            gameOver = true; inCorso = false; if (timer != null) timer.stop();
        }
    }

    /**
     * valido(forma, riga, col): Verifica che la forma nella posizione (riga,col)
     * non esca dai bordi della griglia e non sovrascriva celle già occupate.
     *
     * Regole e casi particolari:
     * - Celle della forma con valore 0 sono ignorate (sono vuote).
     * - Se il blocco si trova sopra la griglia (gr < 0) la funzione non considera
     *   la collisione con righe non ancora visibili; questo permette lo spawn.
     * - Ritorna false se la forma esce a sinistra/destra o sotto la griglia.
     *
     * Complessità: O(n*m) dove n,m sono dimensioni della matrice forma.
     */
    private boolean valido(int[][] forma, int riga, int col) {
        for (int r = 0; r < forma.length; r++)
            for (int c = 0; c < forma[r].length; c++)
                if (forma[r][c] != 0) {
                    int gr = riga + r, gc = col + c;
                    if (gc < 0 || gc >= COLONNE || gr >= RIGHE) return false;
                    if (gr >= 0 && griglia[gr][gc] != Tipo.NESSUNO) return false;
                }
        return true;
    }

    /**
     * muovi(dr, dc): Prova a spostare il pezzo corrente di (dr righe, dc colonne).
     *
     * Comportamento:
     * - Controlla gli stati del gioco (inCorso, pausa, gameOver).
     * - Usa valido() per verificare che lo spostamento sia consentito.
     * - Se lo spostamento è valido, aggiorna pezzoRiga/pezzoCol e richiama repaint().
     *
     * Nota per test: separare la verifica e l'applicazione dello stato semplifica unit test.
     */
    private void muovi(int dr, int dc) {
        if (!inCorso || pausa || gameOver) return;
        if (valido(formaPezzo, pezzoRiga + dr, pezzoCol + dc)) {
            pezzoRiga += dr; pezzoCol += dc; repaint();
        }
    }

    /**
     * ruota(orario): Ruota la matrice formaPezzo in senso orario o antiorario.
     *
     * Implementazione e note:
     * - Per semplicità viene ruotata la matrice creando una nuova matrice ruotata di dimensione m x n.
     * - I blocchi O (quadrato) non vengono ruotati perché invarianti.
     * - Dopo la rotazione viene applicato un semplice wall-kick provando spostamenti orizzontali
     *   (offset 0, +1, -1, +2, -2) per trovare una posizione valida.
     *
     * Limiti:
     * - Questo wall-kick non segue lo SRS ufficiale (System Rotation Standard) e può
     *   fallire in alcune situazioni di gioco avanzate. Per compatibilità con i torneo
     *   Tetris, implementare SRS con tabelle di kick specifiche per ogni pezzo.
     */
    private void ruota(boolean orario) {
        if (!inCorso || pausa || gameOver || tipoPezzo == Tipo.O) return;
        int n = formaPezzo.length, m = formaPezzo[0].length;
        int[][] ruotata = new int[m][n];
        for (int r = 0; r < n; r++)
            for (int c = 0; c < m; c++)
                ruotata[orario ? c : m-1-c][orario ? n-1-r : r] = formaPezzo[r][c];

        for (int offset : new int[]{0, 1, -1, 2, -2})
            if (valido(ruotata, pezzoRiga, pezzoCol + offset)) {
                formaPezzo = ruotata; pezzoCol += offset; repaint(); return;
            }
    }

    /**
     * hardDrop(): Esegue il drop istantaneo del pezzo fino alla riga di collisione.
     *
     * Comportamento e punteggio:
     * - Incrementa il pezzoRiga finché valido() ritorna true per la riga successiva.
     * - Per ogni riga interamente attraversata durante l'hard drop incrementa il
     *   punteggio di 2 punti (implementazione semplice; può essere sostituita da
     *   punteggio basato sulla distanza o combo).
     * - Alla fine chiama blocca() per fissare il pezzo.
     */
    private void hardDrop() {
        if (!inCorso || pausa || gameOver) return;
        while (valido(formaPezzo, pezzoRiga + 1, pezzoCol)) { pezzoRiga++; punteggio += 2; }
        blocca();
    }

    /**
     * blocca(): Fissa il pezzo corrente nella griglia e gestisce la rimozione delle linee complete.
     *
     * Funzionalità chiave:
     * - Scrive il tipo del pezzo in tutte le celle occupate della griglia.
     * - Scorre la griglia dal basso verso l'alto per identificare righe piene.
     * - Quando trova una riga piena, la rimuove facendo scorrere verso il basso tutte le righe
     *   sovrastanti (clone di array per efficienza) e incrementa il contatore di righe rimosse.
     * - Aggiorna punteggio, righeTotal e livello; se si alza il livello, aggiorna la velocità del timer.
     *
     * Note di design e possibili miglioramenti:
     * - L'algoritmo usa clone di righe per spostare velocemente i riferimenti; se si volessero
     *   mantenere istanze immutabili o tracciare modifiche cella-per-cella, questo andrebbe cambiato.
     * - Per implementare combo, T-spin o altre regole avanzate inserire controlli aggiuntivi
     *   prima di chiamare nuovoPezzo().
     */
    private void blocca() {
        for (int r = 0; r < formaPezzo.length; r++)
            for (int c = 0; c < formaPezzo[r].length; c++)
                if (formaPezzo[r][c] != 0 && pezzoRiga + r >= 0)
                    griglia[pezzoRiga + r][pezzoCol + c] = tipoPezzo;

        int righe = 0;
        for (int r = RIGHE - 1; r >= 0; r--) {
            boolean piena = true;
            for (int c = 0; c < COLONNE; c++) if (griglia[r][c] == Tipo.NESSUNO) piena = false;
            if (piena) {
                for (int rr = r; rr > 0; rr--) griglia[rr] = griglia[rr-1].clone();
                for (int c = 0; c < COLONNE; c++) griglia[0][c] = Tipo.NESSUNO;
                righe++; r++;
            }
        }

        if (righe > 0) {
            punteggio += PUNTI[righe] * livello;
            righeTotal += righe;
            int nuovoLiv = righeTotal / 10 + 1;
            if (nuovoLiv > livello) {
                livello = nuovoLiv;
                velocita = Math.max(VELOCITA_MIN, VELOCITA_INIZIALE - (livello-1) * 50);
                if (timer != null) timer.setDelay(velocita);
            }
        }
        nuovoPezzo();
        repaint();
    }

    /**
     * usaHold(): Implementa la meccanica di hold che scambia il pezzo corrente con quello in hold.
     *
     * Comportamento:
     * - Se non c'è un pezzo in hold, il pezzo corrente viene messo in hold e viene generato
     *   un nuovo pezzo (equivalente a "passare" il pezzo).
     * - Se c'è già un pezzo in hold, viene effettuato lo scambio; il nuovo pezzo caricato viene
     *   resettato in cima.
     * - Dopo l'uso l'hold viene temporaneamente disabilitato (puoHold=false) fino allo spawn
     *   del nuovo pezzo: questo evita scambi infiniti nello stesso tick di spawn.
     *
     * Estensioni possibili:
     * - Permettere hold multipli con cooldown, o statistiche sull'uso dell'hold.
     */
    private void usaHold() {
        if (!inCorso || pausa || gameOver || !puoHold) return;
        Tipo temp = tipoPezzo;
        if (hold == null) { hold = temp; nuovoPezzo(); }
        else {
            tipoPezzo = hold; hold = temp;
            formaPezzo = tipoPezzo.getForma();
            pezzoRiga = 0; pezzoCol = COLONNE / 2 - formaPezzo[0].length / 2;
        }
        puoHold = false; repaint();
    }

    /**
     * rigaGhost(): Calcola la riga più bassa raggiungibile dal pezzo, senza modificarne lo stato.
     *
     * Utilizzi:
     * - Rendering del "ghost piece" per migliorare l'usabilità.
     * - Possibile uso per suggerimenti o AI che valuta posizionamenti.
     */
    private int rigaGhost() {
        int r = pezzoRiga;
        while (valido(formaPezzo, r + 1, pezzoCol)) r++;
        return r;
    }

    /**
     * actionPerformed: Metodo chiamato dal Timer ad intervalli regolari.
     *
     * Logica:
     * - Se il pezzo non può scendere ulteriormente, viene bloccato (blocca()).
     * - Altrimenti si incrementa la sua riga di 1 (caduta naturale).
     *
     * Nota:
     * - Questo metodo è il cuore del loop di gioco; per pause o freeze controllare
     *   i flag pausa e gameOver.
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        if (inCorso && !pausa && !gameOver) {
            if (!valido(formaPezzo, pezzoRiga + 1, pezzoCol)) blocca();
            else { pezzoRiga++; repaint(); }
        }
    }

    /**
     * paintComponent: Rendering del gioco.
     *
     * Elementi disegnati:
     * - Griglia di sfondo (linee guida).
     * - Celle bloccate (colorate secondo il Tipo).
     * - Ghost piece (trasparente) e pezzo corrente.
     * - Pannello informazioni a destra con punteggio, prossimo, hold e controlli.
     * - Overlay di PAUSA o GAME OVER.
     *
     * Considerazioni di performance:
     * - Usare double buffering è automatico con Swing; evitare operazioni allocanti in paintComponent.
     * - Se si volessero effetti avanzati (ombre, glow) valutare buffering separato per layer immutabili.
     */
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // Griglia
        g2.setColor(new Color(40, 40, 60));
        for (int r = 0; r <= RIGHE; r++) g2.drawLine(0, r*CELLA, COLONNE*CELLA, r*CELLA);
        for (int c = 0; c <= COLONNE; c++) g2.drawLine(c*CELLA, 0, c*CELLA, RIGHE*CELLA);
        
        // Pezzi bloccati
        for (int r = 0; r < RIGHE; r++)
            for (int c = 0; c < COLONNE; c++)
                if (griglia[r][c] != Tipo.NESSUNO) disegnaCella(g2, c, r, griglia[r][c].colore, 1f);
        
        // Ghost e pezzo corrente
        if (!gameOver && formaPezzo != null) {
            int ghost = rigaGhost();
            for (int r = 0; r < formaPezzo.length; r++)
                for (int c = 0; c < formaPezzo[r].length; c++)
                    if (formaPezzo[r][c] != 0) {
                        disegnaCella(g2, pezzoCol+c, ghost+r, tipoPezzo.colore, 0.3f);
                        disegnaCella(g2, pezzoCol+c, pezzoRiga+r, tipoPezzo.colore, 1f);
                    }
        }
        
        // Info pannello
        int x = COLONNE * CELLA + 15;
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Arial", Font.BOLD, 14));
        g2.drawString("PUNTI: " + punteggio, x, 25);
        g2.drawString("LIVELLO: " + livello, x, 45);
        g2.drawString("RIGHE: " + righeTotal, x, 65);
        
        g2.drawString("PROSSIMO", x, 100);
        if (prossimo != null) disegnaMini(g2, prossimo, x, 110);
        
        g2.drawString("HOLD [C]", x, 200);
        if (hold != null) disegnaMini(g2, hold, x, 210);
        
        g2.setFont(new Font("Arial", Font.PLAIN, 9));
        g2.setColor(Color.GRAY);
        String[] ctrl = {"←→ Muovi", "↓ Veloce", "↑/ X Ruota", "Z Antior.", "SPAZIO Drop", "P Pausa", "R Reset"};
        for (int i = 0; i < ctrl.length; i++) g2.drawString(ctrl[i], x, 320 + i*15);
        
        // Overlay
        if (pausa || gameOver) {
            g2.setColor(new Color(0, 0, 0, 180));
            g2.fillRect(0, 0, COLONNE*CELLA, RIGHE*CELLA);
            g2.setColor(Color.WHITE);
            g2.setFont(new Font("Arial", Font.BOLD, 28));
            String txt = gameOver ? "GAME OVER" : "PAUSA";
            g2.drawString(txt, (COLONNE*CELLA - g2.getFontMetrics().stringWidth(txt))/2, RIGHE*CELLA/2);
        }
    }

    /**
     * disegnaCella(): Disegna una singola cella nel board principale.
     *
     * Parametri:
     * - g: Graphics2D su cui disegnare.
     * - c, r: coordinate colonna/riga della cella nella griglia.
     * - col: colore base della cella.
     * - op: opacità (0.0 - 1.0) utile per ghost piece.
     *
     * Nota implementativa:
     * - Viene usato un padding di 1px per creare effetto di separazione tra celle.
     */
    private void disegnaCella(Graphics2D g, int c, int r, Color col, float op) {
        g.setColor(new Color(col.getRed(), col.getGreen(), col.getBlue(), (int)(255*op)));
        g.fillRect(c*CELLA+1, r*CELLA+1, CELLA-2, CELLA-2);
    }

    /**
     * disegnaMini(): Disegna una miniatura (prossimo / hold) in una posizione fissa del pannello informazioni.
     *
     * Implementazione:
     * - Scala la forma usando blocchi da 16x16 con spacing 18.
     * - Non applica trasparenze o stili avanzati; utile per una rapida preview.
     */
    private void disegnaMini(Graphics2D g, Tipo t, int x, int y) {
        int[][] f = t.getForma();
        for (int r = 0; r < f.length; r++)
            for (int c = 0; c < f[r].length; c++)
                if (f[r][c] != 0) {
                    g.setColor(t.colore);
                    g.fillRect(x + c*18, y + r*18, 16, 16);
                }
    }

    /**
     * gestisciTasto(k): Mappa i codici dei tasti alle azioni di gioco.
     *
     * Mappatura attuale:
     * - Left / Right: muovi orizzontalmente.
     * - Down: scendi velocemente (incrementa punteggio per le righe accelerate).
     * - Up / X: ruota orario.
     * - Z: ruota antiorario.
     * - Space: hard drop.
     * - C: hold.
     * - P: pausa / riprendi (disabilita timer per freeze preciso).
     * - R: reset partita.
     *
     * Possibili miglioramenti:
     * - Supporto per input ripetuto (key repeat) e ritardo iniziale configurabile.
     * - Mappatura personalizzabile tramite file di configurazione o UI.
     */
    private void gestisciTasto(int k) {
        switch (k) {
            case KeyEvent.VK_LEFT -> muovi(0, -1);
            case KeyEvent.VK_RIGHT -> muovi(0, 1);
            case KeyEvent.VK_DOWN -> { if (valido(formaPezzo, pezzoRiga+1, pezzoCol)) { pezzoRiga++; punteggio++; repaint(); } }
            case KeyEvent.VK_UP, KeyEvent.VK_X -> ruota(true);
            case KeyEvent.VK_Z -> ruota(false);
            case KeyEvent.VK_SPACE -> hardDrop();
            case KeyEvent.VK_C -> usaHold();
            case KeyEvent.VK_P -> { if (!gameOver) { pausa = !pausa; if (pausa) timer.stop(); else timer.start(); repaint(); } }
            case KeyEvent.VK_R -> nuovaPartita();
        }
    }

    /**
     * main(): Entry point. Crea una finestra JFrame e ci aggiunge il pannello Tetris.
     *
     * Note:
     * - Eseguito tramite SwingUtilities.invokeLater per rispettare il thread EDT di Swing.
     * - Impostazioni finestra base: non ridimensionabile, centrata e visibile.
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame f = new JFrame("Tetris");
            f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            f.setResizable(false);
            f.add(new Tetris());
            f.pack();
            f.setLocationRelativeTo(null);
            f.setVisible(true);
        });
    }
}
