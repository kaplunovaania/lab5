package ru.itmo.seals.storage;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.*;
import java.nio.charset.StandardCharsets;

public class JsonMapper {
    private final Gson gson;

    public JsonMapper() {
        this.gson = new GsonBuilder().setPrettyPrinting().create();
    }

    public void save(DataDto data, String path) throws IOException {
        try (Writer writer = new OutputStreamWriter(
                new FileOutputStream(path),
                StandardCharsets.UTF_8)) {
            gson.toJson(data, writer);
        }
    }

    public DataDto load(String path) throws IOException {
        try (Reader reader = new InputStreamReader(
                new FileInputStream(path),
                StandardCharsets.UTF_8)) {
            return gson.fromJson(reader, DataDto.class);
        }
    }

    public void save(Object data, String path) throws IOException {
        try (Writer writer = new OutputStreamWriter(
                new FileOutputStream(path),
                StandardCharsets.UTF_8)) {
            gson.toJson(data, writer);
        }
    }
    public Gson getGson() {
        return gson;
    }
}