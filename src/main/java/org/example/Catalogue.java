package org.example;

import org.example.exception.BookNotFoundException;
import org.example.model.Publication;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class Catalogue {
    private List<Publication> publications;

    public Catalogue() {
        this.publications = new ArrayList<>();
    }

    public void addPublication(Publication p) {
        publications.add(p);
    }

    public void removePublicationByTitle(String title) throws BookNotFoundException {
        Publication toRemove = findPublicationByTitle(title);
        if (toRemove == null) {
            throw new BookNotFoundException("Publication with title '" + title + "' was not found.");
        }
        publications.remove(toRemove);
    }

    public Publication findPublicationByTitle(String title) {
        for (Publication p : publications) {
            if (p.getTitle().equalsIgnoreCase(title.trim())) {
                return p;
            }
        }
        return null;
    }

    public ArrayList<Publication> findAllByTitle(String title) {
        ArrayList<Publication> results = new ArrayList<>();
        for (Publication p : publications) {
            if (p.getTitle().toLowerCase().contains(title.toLowerCase())) {
                results.add(p);
            }
        }
        return results;
    }

    public List<Publication> getAllPublications() {
        return new ArrayList<>(publications);
    }

    public void saveToFile(String filename) throws IOException {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filename))) {
            oos.writeObject(publications);
        }
    }

    @SuppressWarnings("unchecked")
    public void loadFromFile(String filename) throws IOException, ClassNotFoundException {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(filename))) {
            this.publications = (List<Publication>) ois.readObject();
        }
    }
}