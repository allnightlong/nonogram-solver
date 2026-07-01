package ru.megadevelopers.nanogram;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import com.google.common.base.Stopwatch;
import ru.megadevelopers.nanogram.model.NanogramBoard;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class StarterCli {

    public static void main(String[] args) throws Exception {
        URL resource = StarterCli.class.getResource("/source_small.json");
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(resource.openStream());

        List<List<Integer>> dataTop = parseClueArray(root.get("data_top"));
        List<List<Integer>> dataLeft = parseClueArray(root.get("data_left"));
        int width = root.get("width").asInt();
        int height = root.get("height").asInt();

        NanogramBoard nanogram = new NanogramBoard(dataTop, dataLeft, width, height);

        Stopwatch stopwatch = Stopwatch.createStarted();
        nanogram.solve();
        System.out.println("computed in " + stopwatch);

        nanogram.print(true);
    }

    private static List<List<Integer>> parseClueArray(JsonNode array) {
        List<List<Integer>> result = new ArrayList<>();
        for (JsonNode row : array) {
            List<Integer> clues = new ArrayList<>();
            for (JsonNode element : row) {
                clues.add(element.isNumber() ? element.asInt() : 0);
            }
            result.add(clues);
        }
        return result;
    }
}
