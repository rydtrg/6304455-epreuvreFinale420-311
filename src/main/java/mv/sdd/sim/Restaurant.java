package mv.sdd.sim;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import mv.sdd.io.Action;
import mv.sdd.model.Client;
import mv.sdd.model.Commande;
import mv.sdd.model.EtatClient;
import mv.sdd.model.EtatCommande;
import mv.sdd.model.Horloge;
import mv.sdd.model.MenuPlat;
import mv.sdd.model.Plat;
import mv.sdd.model.Stats;
import mv.sdd.sim.thread.Cuisinier;
import mv.sdd.utils.Constantes;
import mv.sdd.utils.Formatter;
import mv.sdd.utils.Logger;

public class Restaurant {
    private final Logger logger;
    // TODO : Ajouter les attributs nécessaires ainsi que les getters et les setters
    private List<Client> clients;
    private Queue<Commande> fileCommandes;
    private List<Cuisinier> cuisiniers;
    private List<Commande> commandesEnPreparation;
    private Horloge horloge;
    private Stats stats;
    private boolean enService;

    // TODO : Ajouter le(s) constructeur(s)
    public Restaurant(Logger logger) {
        this.logger = logger;
        this.clients = new ArrayList<>();
        this.fileCommandes = new LinkedList<>();
        this.cuisiniers = new ArrayList<>();
        this.commandesEnPreparation = new ArrayList<>();
    }

    // TODO : implémenter les méthodes suivantes
    public void executerAction(Action action){
        switch (action.getType()) {
            case DEMARRER_SERVICE:
                demarrerService(action.getParam1(), action.getParam2());
                break;
            case AJOUTER_CLIENT:
                ajouterClient(action.getParam1(), action.getParam3(), action.getParam2());
                break;
            case PASSER_COMMANDE:
                passerCommande(action.getParam1(), MenuPlat.valueOf(action.getParam3()));
                break;
            case AVANCER_TEMPS:
                avancerTemps(action.getParam1());
                break;
            case AFFICHER_ETAT:
                afficherEtat();
                break;
            case AFFICHER_STATS:
                afficherStatistiques();
                break;
            case QUITTER:
                arreterService();
                break;
            default:
                break;
        }
    }

    public void demarrerService(int dureeMax, int nbCuisiniers) {
        this.horloge = new Horloge();
        this.stats = new Stats(horloge);
        this.enService = true;
        
        logger.logLine(String.format(Constantes.DEMARRER_SERVICE, dureeMax, nbCuisiniers));
        
        for (int i = 0; i < nbCuisiniers; i++) {
            Cuisinier c = new Cuisinier(this);
            cuisiniers.add(c);
            c.start();
        }
    }

    public void avancerTemps(int minutes) {
        logger.logLine(Constantes.AVANCER_TEMPS + minutes);
        for (int i = 0; i < minutes; i++) {
            tick();
        }
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void arreterService(){
        if (!this.enService) return;
        this.enService = false;
        
        for (Cuisinier c : cuisiniers) {
            c.arreterService();
        }
        logger.logLine(Constantes.FOOTER_APP);
    }

    // TODO : Déclarer et implémenter les méthodes suivantes
    private void tick() {
        if (!enService) return;
        
        horloge.avancerTempsSimule(1);
        
        synchronized(commandesEnPreparation) {
            for (Commande c : commandesEnPreparation) {
                c.decrementerTempsRestant(1);
            }
        }
        
       
        for (Client c : clients) {
            if (c.getEtat() == EtatClient.EN_ATTENTE) {
                c.diminuerPatience(1);
                if (c.getEtat() == EtatClient.PARTI_FACHE) {
                   stats.incrementerNbFaches();
                   if (c.getCommande() != null && c.getCommande().getEtat() == EtatCommande.EN_ATTENTE) {
                       fileCommandes.remove(c.getCommande());
                   }
                   logger.logLine(String.format(Constantes.EVENT_CLIENT_FACHE, horloge.getTempsSimule(), c.getNom()));
                }
            }
        }
    }

    public void afficherEtat() {
        logger.logLine(String.format(Constantes.RESUME_ETAT, 
            horloge.getTempsSimule(),
            clients.size(),
            statsEquals(EtatClient.SERVI),
            statsEquals(EtatClient.PARTI_FACHE),
            fileCommandes.size(),
            commandesEnPreparation.size()));
            
        for (Client c : clients) {
            if (c.getEtat() != EtatClient.PARTI_FACHE) {
                 MenuPlat plat = (c.getCommande() != null && !c.getCommande().getPlats().isEmpty()) ? c.getCommande().getPlats().get(0).getCode() : null;
                 logger.logLine(Formatter.clientLine(c, plat));
            }
        }
    }
    
    private int statsEquals(EtatClient etat) {
        int count = 0;
        for (Client c : clients) {
            if (c.getEtat() == etat) count++;
        }
        return count;
    }

    public void afficherStatistiques() {
        logger.logLine(Constantes.HEADER_AFFICHER_STATS);
        logger.logLine(stats.toString());
    }

    public void ajouterClient(int id, String nom, int patienceInitiale) {
        Client c = new Client(id, nom, patienceInitiale);
        clients.add(c);
        stats.incrementerTotalClients();
        logger.logLine(String.format(Constantes.EVENT_ARRIVEE_CLIENT, horloge.getTempsSimule(), id, nom, patienceInitiale));
    }

    public void passerCommande(int idClient, MenuPlat codePlat) {
        Client client = null;
        for (Client c : clients) {
            if (c.getId() == idClient) {
                client = c;
                break;
            }
        }
        
        if (client != null && client.getEtat() == EtatClient.EN_ATTENTE) {
            Commande cmd = new Commande(client, codePlat);
            client.setCommande(cmd);
            ajouterAuFile(cmd);
            
            String platsStr = "";
             for(Plat p : cmd.getPlats()) {
                 platsStr += Formatter.emojiPlat(p.getCode());
             }
            
            logger.logLine(String.format(Constantes.EVENT_CMD_CREE, horloge.getTempsSimule(), cmd.getId(), client.getNom(), platsStr));
        }
    }
    
    private synchronized void ajouterAuFile(Commande cmd) {
        fileCommandes.offer(cmd);
    }

    public synchronized Commande retirerProchaineCommande() {
        Commande cmd = fileCommandes.poll();
        if (cmd != null) {
            synchronized(commandesEnPreparation) {
                 commandesEnPreparation.add(cmd);
            }
            logger.logLine(String.format(Constantes.EVENT_CMD_DEBUT, horloge.getTempsSimule(), cmd.getId(), cmd.calculerTempsPreparationTotal()));
        }
        return cmd;
    }

    public synchronized void marquerCommandeTerminee(Commande commande) {
        commande.setEtat(EtatCommande.LIVREE);
        synchronized(commandesEnPreparation) {
            commandesEnPreparation.remove(commande);
        }
        
        Client client = commande.getClient();
        if (client.getEtat() != EtatClient.PARTI_FACHE) {
            client.setEtat(EtatClient.SERVI);
            stats.incrementerNbServis();
            double montant = commande.calculerMontant();
            stats.incrementerChiffreAffaires(montant);
            for (Plat p : commande.getPlats()) {
                stats.incrementerVentesParPlat(p.getCode());
            }
            logger.logLine(String.format(Constantes.EVENT_CMD_TERMINEE, horloge.getTempsSimule(), commande.getId(), client.getNom()));
        }
    }
}
