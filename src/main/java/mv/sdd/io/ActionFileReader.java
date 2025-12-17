package mv.sdd.io;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

// Lecture du fichier d'actions
public class ActionFileReader {
    public static List<Action> readActions(String filePath) throws IOException {
        List<Action> actions = new ArrayList<>();

        // TODO : Ajouter le code qui permet de lire et parser un fichier d'actions
        java.nio.file.Path path = java.nio.file.Paths.get(filePath);
        List<String> lines = java.nio.file.Files.readAllLines(path);

        for (String line : lines) {
            if (line != null && !line.isBlank()) {
                try {
                    Action action = ActionParser.parseLigne(line);
                    actions.add(action);
                } catch (Exception e) {
                    System.err.println("Erreur parsing: " + line);
                }
            }
        }
        return actions;
    }
}
