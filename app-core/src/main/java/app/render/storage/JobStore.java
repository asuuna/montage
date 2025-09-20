package app.render.storage;

import app.render.RenderJob;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class JobStore {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper().registerModule(new JavaTimeModule());
    private final Path storePath;
    private final ConcurrentMap<UUID, RenderJob> jobs = new ConcurrentHashMap<>();

    public JobStore(Path storePath) throws IOException {
        this.storePath = storePath;
        if (storePath.getParent() != null) {
            Files.createDirectories(storePath.getParent());
        }
        load();
    }

    public void save(RenderJob job) throws IOException {
        jobs.put(job.getId(), job);
        persist();
    }

    public void remove(UUID jobId) throws IOException {
        jobs.remove(jobId);
        persist();
    }

    public List<RenderJob> pending() {
        return new ArrayList<>(jobs.values());
    }

    private void load() throws IOException {
        if (!Files.exists(storePath) || Files.size(storePath) == 0) {
            return;
        }
        RenderJob[] array = OBJECT_MAPPER.readValue(storePath.toFile(), RenderJob[].class);
        for (RenderJob job : array) {
            jobs.put(job.getId(), job);
        }
    }

    private void persist() throws IOException {
        OBJECT_MAPPER.writerWithDefaultPrettyPrinter().writeValue(storePath.toFile(), jobs.values());
    }
}
