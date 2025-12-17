package mv.sdd.sim.thread;

public class Cuisinier extends Thread {
    private final mv.sdd.sim.Restaurant restaurant;
    private boolean enService = true;

    public Cuisinier(mv.sdd.sim.Restaurant restaurant) {
        this.restaurant = restaurant;
    }

    public void arreterService() {
        enService = false;
    }

    @Override
    public void run() {
        while (enService) {
            mv.sdd.model.Commande commande = restaurant.retirerProchaineCommande();
            if (commande != null) {
                commande.demarrerPreparation();
                while (!commande.estTermineeParTemps() && enService) {
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                if (commande.estTermineeParTemps()) {
                    restaurant.marquerCommandeTerminee(commande);
                }
            } else {
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
