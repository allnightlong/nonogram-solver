package ru.megadevelopers.nanogram;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import ru.megadevelopers.nanogram.model.Clue;
import ru.megadevelopers.nanogram.model.NanogramBoard;
import ru.megadevelopers.nanogram.solver.v3.HybridSolver;
import ru.megadevelopers.nanogram.solver.Puzzle;
import ru.megadevelopers.nanogram.solver.SolveResult;

import java.net.URL;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class StarterCli {

    public static void main(String[] args) throws Exception {
        URL resource = StarterCli.class.getResource("/source_small.json");
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(resource.openStream());

        List<Clue> dataTop = parseClueArray(root.get("data_top"));
        List<Clue> dataLeft = parseClueArray(root.get("data_left"));
        int width = root.get("width").asInt();
        int height = root.get("height").asInt();

        NanogramBoard nanogram = new NanogramBoard(dataTop, dataLeft, width, height);
        Puzzle puzzle = new Puzzle(dataLeft, dataTop, width, height);

        long start = System.nanoTime();
        SolveResult result = new HybridSolver().solve(puzzle, (r, c, v) -> nanogram.setValue(r, c, v));
        System.out.println("computed in " + Duration.ofNanos(System.nanoTime() - start));

        switch (result) {
            case SolveResult.Solved ignored -> nanogram.print(true);
            case SolveResult.Ambiguous ignored -> {
                System.out.println("Ambiguous (unexpected for hybrid solver)");
                nanogram.print(true);
            }
            case SolveResult.NoSolution ignored -> System.out.println("No solution");
        }
    }

    private static List<Clue> parseClueArray(JsonNode array) {
        List<Clue> result = new ArrayList<>();
        for (JsonNode row : array) {
            List<Integer> raw = new ArrayList<>();
            for (JsonNode element : row) {
                raw.add(element.isNumber() ? element.asInt() : 0);
            }
            result.add(new Clue(raw));
        }
        return result;
    }
}
