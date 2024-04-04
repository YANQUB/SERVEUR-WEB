import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;

public class HttpRequestHandler extends Thread {
    private Socket socket;
    private String staticDir;

    public HttpRequestHandler(Socket socket, String staticDir) {
        this.socket = socket;
        this.staticDir = staticDir;
    }

   //ETIENNE sene, je vais de faire les variables et certaines methodes en Anglais, meme tout le code 
   // Si c'est possible meme tout le code , ca me pemerttra de mieux comprendre en Anglais
    // Alors si tu vois ce code , et t as des questions sur cette partie demande moi
    //Je mets en commentaire pourque tu puisses voir
    @Override
    public void run() {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String requete = reader.readLine();
            if (requete != null) {
                String[] partieReq = requete.split("\\s+");
                String method = partieReq[0];
                String resourcePath = partieReq[1];

                if ("GET".equals(method)) {
                    ObtenirRequete(resourcePath);
                } else {
                    ErreurCode(501, "Non Implemente");
                }
            }
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void ObtenirRequete(String resourcePath) throws IOException {
        File file = new File(staticDir + resourcePath);
        if (!file.exists()) {
            ErreurCode(404, "NOT FOUND, Veuillez choisir un fichier existant");
            return;
        }

        if (file.isDirectory()) {
            ListerRepertoire(file);
            return;
        }

        if (!NonAutorise(resourcePath)) {
            ErreurCode(403, "Forbidden , Acces non autorise sur ce document");
            return;
        }

        String nomFichier = file.getName();
        String fileExtension = nomFichier.substring(nomFichier.lastIndexOf(".") + 1);

        switch (fileExtension.toLowerCase()) {
            case "py":
                executePythonScript(file);
                break;
            default:
                ContenuFichier(file);
                break;
        }
    }

    private void executePythonScript(File file) throws IOException {
        String Chemin = "C:\\Users\\dell\\AppData\\Local\\Programs\\Python\\Python311\\python.exe";    
        ProcessBuilder processBuilder = new ProcessBuilder(Chemin, file.getAbsolutePath());
        processBuilder.redirectErrorStream(true);
        Process process = processBuilder.start();
        sendProcessOutput(process);
    }
    

    private void sendProcessOutput(Process process) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            StringBuilder outputBuilder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                outputBuilder.append(line).append("\n");
            }
            String output = outputBuilder.toString();
    
            // Crée la réponse HTML avec un style CSS pour un affichage esthétique
            String response = "HTTP/1.1 200 OK\r\nContent-Type: text/html\r\n\r\n";
            response += "<!DOCTYPE html>\r\n";
            response += "<html lang=\"en\">\r\n";
            response += "<head>\r\n";
            response += "<meta charset=\"UTF-8\">\r\n";
            response += "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\r\n";
            response += "<title>Résultat du script Python</title>\r\n";
            response += "<style>\r\n";
            response += "body { font-family: Arial, sans-serif; background-color: #f8f9fa; }\r\n";
            response += ".container { max-width: 800px; margin: 0 auto; padding: 20px; }\r\n";
            response += "h1 { color: #333; text-align: center; }\r\n";
            response += "pre { font-size: 18px; white-space: pre-wrap; background-color: #ffffff; padding: 20px; border-radius: 5px; box-shadow: 0 2px 5px rgba(0, 0, 0, 0.1); }\r\n";
            response += "</style>\r\n";
            response += "</head>\r\n";
            response += "<body>\r\n";
            response += "<div class=\"container\">\r\n";
            response += "<h1>Resultat du script Python</h1>\r\n";
            response += "<pre>" + output + "</pre>\r\n"; // Utilisez <pre> pour conserver la mise en forme du texte
            response += "</div>\r\n";
            response += "</body>\r\n";
            response += "</html>\r\n";
    
            // Envoie la réponse au client
            socket.getOutputStream().write(response.getBytes());
        }
    }
    
    




    private boolean NonAutorise(String resourcePath) {
        // Simulation de la vérification d'autorisation
        if (resourcePath.contains("Protected.txt")) {
            // Si la demande concerne le fichier Protected.txt, renvoyer false pour interdire l'accès
            return false;
        }
        return true; // Pour toutes les autres ressources, l'accès est autorisé
    }
    

    private void ContenuFichier(File file) throws IOException {
        String cheminFichier = file.getAbsolutePath();
        byte[] tailleFichier = Files.readAllBytes(Paths.get(cheminFichier));
        String TypeContenu = Files.probeContentType(Paths.get(cheminFichier));
        String reponse = "HTTP/1.1 200 OK\r\nContent-Type: " + TypeContenu + "\r\n\r\n";
        OutputStream outputStream = socket.getOutputStream();
        outputStream.write(reponse.getBytes());
        outputStream.write(tailleFichier);
        outputStream.flush();
    }
    private void ListerRepertoire(File repertoire) throws IOException {
        StringBuilder responseBuilder = new StringBuilder();
        responseBuilder.append("HTTP/1.1 200 OK\r\n\r\n");
        responseBuilder.append("<!DOCTYPE html>\r\n");
        responseBuilder.append("<html lang=\"en\">\r\n");
        responseBuilder.append("<head>\r\n");
        responseBuilder.append("<meta charset=\"UTF-8\">\r\n");
        responseBuilder.append("<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\r\n");
        responseBuilder.append("<title>YangServer</title>\r\n");
        responseBuilder.append("<link rel=\"stylesheet\" href=\"https://stackpath.bootstrapcdn.com/bootstrap/4.5.2/css/bootstrap.min.css\">\r\n");
        responseBuilder.append("<style>\r\n");
        responseBuilder.append("body { background-color: #f8f9fa; }\r\n");
        responseBuilder.append(".container { margin-top: 20px; }\r\n");
        responseBuilder.append(".repertoire { font-weight: bold; color: #98FB98; font-size: 1.2em; }\r\n"); // Couleur vert menthe pour les répertoires
        responseBuilder.append(".file { color: #3498DB; }\r\n"); // Bleu ciel pour les fichiers
        responseBuilder.append(".repertoire-item { background-color: #D5F5E3; border: 2px solid #2ECC71; margin-bottom: 5px; }\r\n"); // Couleur de fond pour les répertoires (vert pâle) avec une bordure verte
        responseBuilder.append(".file-item { background-color: #AEE8FA; border: 2px solid #3498DB; margin-bottom: 5px; }\r\n"); // Couleur de fond pour les fichiers (bleu pâle) avec une bordure bleue
        responseBuilder.append(".category-label { color: #6C7A89; }\r\n"); // Nouvelle couleur douce pour les libellés des catégories
        responseBuilder.append(".sub-folder { margin-left: 20px; }\r\n"); // Ajouter une marge à gauche pour les sous-dossiers
        responseBuilder.append(".repertoire-item, .file-item { transition: background-color 0.3s ease; }\r\n"); // Ajouter une transition pour le changement de couleur de fond
        responseBuilder.append(".welcome-msg { text-align: center; font-size: 2em; color: #3498DB; margin-bottom: 30px; padding: 10px; border: 2px solid #3498DB; border-radius: 10px; background-color: #EAF2F8; }\r\n"); // Style pour le message de bienvenue avec fond doux et encadré
        responseBuilder.append("</style>\r\n");
        responseBuilder.append("</head>\r\n");
        responseBuilder.append("<body>\r\n");
        responseBuilder.append("<div class=\"container\">\r\n");
    
        // Message de bienvenue
        responseBuilder.append("<div class=\"welcome-msg\">");
        responseBuilder.append("<h1>Bienvenue sur YangServer </h1>\r\n");
        responseBuilder.append("</div>\r\n");
    
        responseBuilder.append("<h2 class=\"category-label\">REPERTOIRES ET LEURS CONTENUS</h2>\r\n");
        responseBuilder.append("<ul class=\"list-group\">\r\n");
        ListerFichiersAndRepertoire(repertoire, responseBuilder, 0);
        responseBuilder.append("</ul>\r\n");
    
        responseBuilder.append("</div>\r\n");
        responseBuilder.append("</body>\r\n");
        responseBuilder.append("</html>\r\n");
    
        socket.getOutputStream().write(responseBuilder.toString().getBytes());
    }
    
    
      private void ListerFichiersAndRepertoire(File repertoire, StringBuilder creationReponse, int level) {
        File[] files = repertoire.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    creationReponse.append("<li class=\"list-group-item directory-item\">");
                    creationReponse.append("<a class=\"directory\" href=\"").append(file.getName()).append("/\">");
                    creationReponse.append(file.getName()).append("</a>");
                    creationReponse.append("</li>\r\n");
    
                    // Ajouter une flèche pour indiquer les sous-fichiers
                    creationReponse.append("<ul class=\"list-group sub-files\">\r\n");
                    ListerFichierINDirectory(file, creationReponse, level + 1);
                    creationReponse.append("</ul>\r\n");
                }
            }
    
            // Libellé pour les fichiers
            creationReponse.append("<h2 class=\"category-label\">FICHIERS</h2>\r\n");
            creationReponse.append("<ul class=\"list-group\">\r\n");
            for (File file : files) {
                if (!file.isDirectory()) {
                    creationReponse.append("<li class=\"list-group-item file-item\">");
                    creationReponse.append("<a class=\"file\" href=\"").append(file.getName()).append("\">");
                    creationReponse.append(file.getName()).append("</a>");
                    creationReponse.append("</li>\r\n");
                }
            }
            creationReponse.append("</ul>\r\n");
        }
    }
    
    private void ListerFichierINDirectory(File repertoire, StringBuilder creationReponse, int level) {
        File[] files = repertoire.listFiles();
        if (files != null) {
            for (File file : files) {
                if (!file.isDirectory()) {
                    creationReponse.append("<li class=\"list-group-item file-item\">");
                    for (int i = 0; i < level; i++) {
                        creationReponse.append("&nbsp;&nbsp;&nbsp;&nbsp;"); // Ajouter des espaces pour l'indentation
                    }
                    creationReponse.append("<span class=\"sub-file-arrow\">&#9658;</span>");
                    creationReponse.append("<a class=\"file\" href=\"").append(repertoire.getName()).append("/").append(file.getName()).append("\">");
                    creationReponse.append(file.getName()).append("</a>");
                    creationReponse.append("</li>\r\n");
                }
            }
        }
    }
    

    private void ErreurCode(int statusCode, String statusMessage) throws IOException {
        StringBuilder responseBuilder = new StringBuilder();
        responseBuilder.append("HTTP/1.1 ").append(statusCode).append(" ").append(statusMessage).append("\r\n");
        responseBuilder.append("Content-Type: text/html\r\n\r\n");
        responseBuilder.append("<!DOCTYPE html>\r\n");
        responseBuilder.append("<html lang=\"en\">\r\n");
        responseBuilder.append("<head>\r\n");
        responseBuilder.append("<meta charset=\"UTF-8\">\r\n");
        responseBuilder.append("<title>Error</title>\r\n");
        responseBuilder.append("<style>\r\n");
        responseBuilder.append("body { font-family: Arial, sans-serif; }\r\n");
        responseBuilder.append(".error-container { margin: 50px auto; padding: 20px; max-width: 600px; background-color: #f8d7da; border: 1px solid #f5c6cb; border-radius: 5px; }\r\n");
        responseBuilder.append(".error-code { font-size: 24px; color: #721c24; }\r\n");
        responseBuilder.append(".error-message { font-size: 18px; color: #721c24; }\r\n");
        responseBuilder.append("</style>\r\n");
        responseBuilder.append("</head>\r\n");
        responseBuilder.append("<body>\r\n");
        responseBuilder.append("<div class=\"error-container\">\r\n");
        responseBuilder.append("<div class=\"error-code\">Error ").append(statusCode).append("</div>\r\n");
        responseBuilder.append("<div class=\"error-message\">").append(statusMessage).append("</div>\r\n");
        responseBuilder.append("</div>\r\n");
        responseBuilder.append("</body>\r\n");
        responseBuilder.append("</html>\r\n");
    
        OutputStream outputStream = socket.getOutputStream();
        outputStream.write(responseBuilder.toString().getBytes());
        outputStream.flush();
    }
    

  
}
