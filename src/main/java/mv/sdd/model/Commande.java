package mv.sdd.model;

public class Commande {
    private int id;
    private static int nbCmd = 0;
    private final Client client;
    private EtatCommande etat = EtatCommande.EN_ATTENTE;
    private int tempsRestant; // en minutes simulées
    // TODO : ajouter l'attribut plats et son getter avec le bon type et le choix de la SdD adéquat
    // private final <Votre structure de choix adéquat> plats
    private final java.util.List<Plat> plats;

    public java.util.List<Plat> getPlats() {
        return plats;
    }

    // TODO : Ajout du ou des constructeur(s) nécessaires ou compléter au besoin
    public Commande(Client client, MenuPlat plat) {
        id = ++nbCmd;
        this.client = client;
        // À compléter
        this.plats = new java.util.ArrayList<>();
        ajouterPlat(plat);
    }

    public int getId() {
        return id;
    }

    public Client getClient() {
        return client;
    }

    public EtatCommande getEtat() {
        return etat;
    }

    public int getTempsRestant() {
        return tempsRestant;
    }
    
    public void setTempsRestant(int tempsRestant) {
        this.tempsRestant = tempsRestant;
    }

    public void setEtat(EtatCommande etat) {
        this.etat = etat;
    }

    // TODO : Ajoutez la méthode ajouterPlat
    public void ajouterPlat(MenuPlat menuPlat) {
        mv.sdd.model.Plat plat = mv.sdd.utils.Constantes.MENU.get(menuPlat);
        if (plat != null) {
            this.plats.add(plat);
        }
    }

    // TODO : Ajoutez la méthode demarrerPreparation
    public void demarrerPreparation() {
        this.etat = EtatCommande.EN_PREPARATION;
        this.tempsRestant = calculerTempsPreparationTotal();
    }

    // TODO : Ajoutez la méthode decrementerTempsRestant
    public void decrementerTempsRestant(int minutes) {
        if (this.etat == EtatCommande.EN_PREPARATION) {
            this.tempsRestant -= minutes;
        }
    }

    // TODO : Ajoutez la méthode estTermineeParTemps
    public boolean estTermineeParTemps() {
        return tempsRestant <= 0;
    }

    // TODO : Ajoutez la méthode calculerTempsPreparationTotal
    public int calculerTempsPreparationTotal() {
        int total = 0;
        for (mv.sdd.model.Plat plat : plats) {
            total += plat.getTempsPreparation();
        }
        return total;
    }

    // TODO : Ajoutez la méthode calculerMontant
    public double calculerMontant() {
        double total = 0.0;
        for (mv.sdd.model.Plat plat : plats) {
            total += plat.getPrix();
        }
        return total;
    }
}
