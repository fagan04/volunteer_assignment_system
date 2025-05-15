package client.gui;

import client.network.ClientNetworkManager;
import client.preferences.PreferencesManager;
import model.Preference;
import model.Volunteer;
import model.Assignment;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class VolunteerGUI {
    private final PreferencesManager preferencesManager = new PreferencesManager();
    private final ClientNetworkManager networkManager = new ClientNetworkManager();
    private final List<JComboBox<String>> preferenceBoxes = new ArrayList<>();
    private final JTextArea assignmentArea = new JTextArea();

    private final String[] services = {
            "Reception", "Logistics", "Food Service", "Security", "Media",
            "Transport", "Medical", "Info Desk", "Cleanup", "Tech Support"
    };

    private final int volunteerId = (int) (Math.random() * 10000);

    public VolunteerGUI() {
        SwingUtilities.invokeLater(this::createAndShowGUI);
    }

    private void createAndShowGUI() {
        JFrame frame = new JFrame("Volunteer Client - ID " + volunteerId);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 500);

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JLabel title = new JLabel("Volunteer Service Preferences");
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainPanel.add(title);

        for (int i = 0; i < 5; i++) {
            JComboBox<String> box = new JComboBox<>(services);
            box.setSelectedIndex(-1);
            preferenceBoxes.add(box);
            mainPanel.add(box);
        }

        JButton submitBtn = new JButton("Submit / Update Preferences");
        JButton optimizeBtn = new JButton("Trigger Optimization");

        submitBtn.addActionListener(e -> submitPreferences());
        optimizeBtn.addActionListener(e -> networkManager.postOptimizeRequest());

        mainPanel.add(submitBtn);
        mainPanel.add(optimizeBtn);

        assignmentArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(assignmentArea);
        scrollPane.setPreferredSize(new Dimension(350, 150));
        mainPanel.add(scrollPane);

        frame.getContentPane().add(mainPanel);
        frame.setVisible(true);

        networkManager.onAssignmentReceived(this::showAssignments);
    }

    private void submitPreferences() {
        List<Preference> prefs = new ArrayList<>();
        int priority = 1;
        for (JComboBox<String> box : preferenceBoxes) {
            String selected = (String) box.getSelectedItem();
            if (selected != null && !selected.isEmpty()) {
                prefs.add(new Preference(selected, priority++));
            }
        }

        preferencesManager.setPreferences(prefs);

        Volunteer v = new Volunteer(volunteerId, "Volunteer #" + volunteerId, prefs);
        networkManager.postPreferences(v);
    }

    private void showAssignments(List<Assignment> assignments) {
        SwingUtilities.invokeLater(() -> {
            StringBuilder sb = new StringBuilder("Assignment Results:\n");
            for (Assignment a : assignments) {
                sb.append("Volunteer ").append(a.getVolunteerId())
                        .append(" → ").append(a.getServiceName())
                        .append(" (Cost: ").append(a.getCost()).append(")\n");
            }
            assignmentArea.setText(sb.toString());
        });
    }

    // Entry point for Swing version
    public static void main(String[] args) {
        new VolunteerGUI();
    }
}
