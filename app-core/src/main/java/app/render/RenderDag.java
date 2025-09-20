package app.render;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class RenderDag {
    private final Map<UUID, RenderTask> tasks = new HashMap<>();
    private final Map<UUID, List<UUID>> adjacency = new HashMap<>();
    private final Map<UUID, Integer> indegree = new HashMap<>();

    public void addTask(RenderTask task) {
        tasks.put(task.getId(), task);
        adjacency.computeIfAbsent(task.getId(), id -> new ArrayList<>());
        indegree.putIfAbsent(task.getId(), 0);
        for (RenderTask dependency : task.dependencies()) {
            adjacency.computeIfAbsent(dependency.getId(), id -> new ArrayList<>()).add(task.getId());
            indegree.merge(task.getId(), 1, Integer::sum);
        }
    }

    public List<RenderTask> topologicalOrder() {
        ArrayDeque<UUID> queue = new ArrayDeque<>();
        indegree.forEach((id, count) -> {
            if (count == 0) {
                queue.add(id);
            }
        });
        List<RenderTask> order = new ArrayList<>();
        Map<UUID, Integer> remaining = new HashMap<>(indegree);
        while (!queue.isEmpty()) {
            UUID id = queue.poll();
            order.add(tasks.get(id));
            for (UUID neighbor : adjacency.getOrDefault(id, List.of())) {
                remaining.merge(neighbor, -1, Integer::sum);
                if (remaining.get(neighbor) == 0) {
                    queue.add(neighbor);
                }
            }
        }
        if (order.size() != tasks.size()) {
            throw new IllegalStateException("Render DAG contains cycles");
        }
        return order;
    }

    public Set<UUID> taskIds() {
        return tasks.keySet();
    }
}
