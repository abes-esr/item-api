package fr.abes.item.core.exception;

public class FileLineException extends Exception {
    public FileLineException(String message){
        super("Problème lors du traitement de la ligne du fichier : " + message);
    }
}

