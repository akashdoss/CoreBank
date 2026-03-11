package com.corebank.repository;

import java.io.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/**
 * Generic File-based Repository.
 * Demonstrates Java I/O, Serialization, and Collections.
 */
public class FileRepository<T extends Serializable, ID> implements Repository<T, ID> {
    private final String filePath;
    private final Map<ID, T> dataMap = new ConcurrentHashMap<>();
    private final Function<T, ID> idExtractor;

    public FileRepository(String filePath, Function<T, ID> idExtractor) {
        this.filePath = filePath;
        this.idExtractor = idExtractor;
        loadData();
    }

    @Override
    public T save(T entity) {
        ID id = idExtractor.apply(entity);
        dataMap.put(id, entity);
        saveData();
        return entity;
    }

    @Override
    public Optional<T> findById(ID id) {
        return Optional.ofNullable(dataMap.get(id));
    }

    @Override
    public List<T> findAll() {
        return new ArrayList<>(dataMap.values());
    }

    @Override
    public void deleteById(ID id) {
        dataMap.remove(id);
        saveData();
    }

    @SuppressWarnings("unchecked")
    private void loadData() {
        File file = new File(filePath);
        if (!file.exists()) return;

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            Map<ID, T> loadedData = (Map<ID, T>) ois.readObject();
            dataMap.putAll(loadedData);
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Error loading data from " + filePath + ": " + e.getMessage());
        }
    }

    private void saveData() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filePath))) {
            oos.writeObject(new HashMap<>(dataMap));
        } catch (IOException e) {
            System.err.println("Error saving data to " + filePath + ": " + e.getMessage());
        }
    }
}
