package ru.itmo.seals.storage;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.*;

public class JsonMapper {
    private final Gson gson;

    public JsonMapper() {
        this.gson = new GsonBuilder().setPrettyPrinting().create();
    }

    public void save(DataDto data, String path) throws IOException {
        try (FileWriter writer = new FileWriter(path)) {
            gson.toJson(data, writer);
        }
    }

    public DataDto load(String path) throws IOException {
        try (FileReader reader = new FileReader(path)) {
            return gson.fromJson(reader, DataDto.class);
        }
    }
}