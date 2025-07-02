package fr.abes.item.core.exception;

public class FileLineL035Exception extends FileLineException {
    public FileLineL035Exception(int lineNumber) {
        super("Valeur L035 manquante à la ligne " + lineNumber);
    }
}
