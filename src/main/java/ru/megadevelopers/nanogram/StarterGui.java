package ru.megadevelopers.nanogram;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import ru.megadevelopers.nanogram.gui.NanogramFrame;
import ru.megadevelopers.nanogram.model.NanogramBoard;

import javax.swing.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class StarterGui {

    public static void main(String[] args) throws Exception {
        URL resource = StarterGui.class.getResource("/source_small.json");
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(resource);

        List<List<Integer>> dataTop = parseClueArray(root.get("data_top"));
        List<List<Integer>> dataLeft = parseClueArray(root.get("data_left"));
        int width = root.get("width").asInt();
        int height = root.get("height").asInt();

        NanogramBoard nanogram = new NanogramBoard(dataTop, dataLeft, width, height);

        UIManager.setLookAndFeel("javax.swing.plaf.metal.MetalLookAndFeel");
        SwingUtilities.invokeLater(() -> {
            NanogramFrame frame = new NanogramFrame(nanogram);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.addComponentsToPane(frame.getContentPane());
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
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
