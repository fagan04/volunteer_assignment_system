package client.gui;

import client.network.ClientNetworkManager;
import client.preferences.PreferencesManager;
import model.Assignment;
import model.Preference;
import model.Volunteer;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.LocalTime;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class VolunteerGUI {

    private static final String[] SERVICES = {
            "Reception", "Logistics", "Food Service", "Security", "Media",
            "Transport", "Medical", "Info Desk", "Cleanup", "Tech Support"
    };

    private final int volunteerId = new Random().nextInt(9000) + 1000; // 1000‑9999
    private final PreferencesManager preferencesManager = new PreferencesManager();
    private final ClientNetworkManager networkManager    = new ClientNetworkManager();

    private final List<JComboBox<String>> selectors = new ArrayList<>();
    private final JTextPane outputPane              = new JTextPane();
    private final JTextArea serviceSummaryArea      = new JTextArea(10, 30);
    private final JLabel status                     = new JLabel("Welcome!");
    private final JLabel totalCostLabel             = new JLabel("Total Cost: 0");

    public VolunteerGUI() {
        SwingUtilities.invokeLater(this::initUI);
        networkManager.onAssignmentReceived(this::renderAssignments);
    }

    private void initUI() {
        installLookAndFeel();

        JFrame frame = new JFrame("Volunteer #" + volunteerId);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(500, 680);
        frame.setLocationRelativeTo(null);

        JPanel root = new JPanel(new BorderLayout(10, 10));
        root.setBorder(new EmptyBorder(12, 12, 12, 12));
        frame.setContentPane(root);

        root.add(buildPreferencePanel(), BorderLayout.NORTH);
        root.add(buildOutputPanel(),     BorderLayout.CENTER);
        root.add(buildStatusBar(),       BorderLayout.SOUTH);

        frame.setVisible(true);
    }

    private JPanel buildPreferencePanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Rank your service preferences"));
        panel.setBackground(new Color(245, 248, 255));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets  = new Insets(4, 4, 4, 4);
        gbc.anchor  = GridBagConstraints.WEST;
        gbc.gridx   = 0;
        gbc.gridy   = 0;

        for (int i = 0; i < 5; i++) {
            selectors.add(new JComboBox<>(SERVICES));
            selectors.get(i).setSelectedIndex(-1);
            final int index = i;
            selectors.get(i).addActionListener(e -> enforceUniqueChoices(index));

            panel.add(new JLabel((i + 1) + ":"), gbc);
            gbc.gridx = 1;
            panel.add(selectors.get(i), gbc);
            gbc.gridx = 0;
            gbc.gridy++;
        }

        JButton submit  = new JButton("Submit / Update");
        JButton optimize = new JButton("Trigger Optimization");

        submit.addActionListener(e -> submitPreferences());
        optimize.addActionListener(e -> networkManager.postOptimizeRequest());

        gbc.gridwidth = 2;
        gbc.gridx     = 0;
        panel.add(submit, gbc);
        gbc.gridy++;
        panel.add(optimize, gbc);

        return panel;
    }

    private JPanel buildOutputPanel() {
        outputPane.setEditable(false);
        outputPane.setContentType("text/html");

        serviceSummaryArea.setEditable(false);
        serviceSummaryArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        serviceSummaryArea.setBackground(new Color(255, 255, 240));

        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createTitledBorder("Assignments & Service Summary"));
        panel.add(new JScrollPane(outputPane), BorderLayout.CENTER);
        panel.add(new JScrollPane(serviceSummaryArea), BorderLayout.SOUTH);
        panel.setBackground(new Color(250, 250, 255));
        return panel;
    }

    private JPanel buildStatusBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.add(status, BorderLayout.WEST);
        bar.add(totalCostLabel, BorderLayout.EAST);
        return bar;
    }

    private void enforceUniqueChoices(int changedIndex) {
        Set<String> chosen = selectors.stream()
                .map(cb -> (String) cb.getSelectedItem())
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        for (JComboBox<String> cb : selectors) {
            String current = (String) cb.getSelectedItem();
            DefaultComboBoxModel<String> model = new DefaultComboBoxModel<>();
            for (String s : SERVICES) {
                if (!chosen.contains(s) || s.equals(current)) {
                    model.addElement(s);
                }
            }
            cb.setModel(model);
            cb.setSelectedItem(current);
        }
    }

    private void submitPreferences() {
        List<Preference> prefs = new ArrayList<>();
        int rank = 1;
        for (JComboBox<String> cb : selectors) {
            String sel = (String) cb.getSelectedItem();
            if (sel != null && !sel.isBlank()) {
                prefs.add(new Preference(sel, rank++));
            }
        }
        if (prefs.isEmpty()) {
            JOptionPane.showMessageDialog(null, "Please choose at least one service.",
                    "Validation", JOptionPane.WARNING_MESSAGE);
            return;
        }

        preferencesManager.setPreferences(prefs);
        Volunteer v = new Volunteer(volunteerId, "Volunteer #" + volunteerId, prefs);
        networkManager.postPreferences(v);
        status.setText("Preferences sent at " + LocalTime.now().withNano(0));
    }

    private void renderAssignments(java.util.List<Assignment> assignments) {
        SwingUtilities.invokeLater(() -> {
            StringBuilder html = new StringBuilder("<html><body style='font-family:monospace'>");
            html.append("<b>Updated at ").append(LocalTime.now().withNano(0)).append("</b><br><br>");

            assignments.stream()
                    .filter(a -> a.getVolunteerId() == volunteerId)
                    .findFirst()
                    .ifPresent(a -> html.append("<b style='color:green'>*** YOUR ASSIGNMENT: ")
                            .append(a.getServiceName())
                            .append(" (cost ").append((int) a.getCost()).append(") ***</b><br><br>"));

            double totalCost = 0;
            for (Assignment a : assignments) {
                totalCost += a.getCost();
                String color = a.getCost() >= 10 ? "red" : "black";
                html.append(String.format("<span style='color:%s'>ID %4d → %-12s cost %2.0f</span><br>",
                        color, a.getVolunteerId(), a.getServiceName(), a.getCost()));
            }
            html.append("</body></html>");

            outputPane.setText(html.toString());
            totalCostLabel.setText("Total Cost: " + (int) totalCost);
            status.setText("Last update " + LocalTime.now().withNano(0));

            Map<String, Integer> serviceCounts = new HashMap<>();
            for (Assignment a : assignments) {
                String service = a.getServiceName();
                serviceCounts.put(service, serviceCounts.getOrDefault(service, 0) + 1);
            }

            StringBuilder summary = new StringBuilder("Service Assignment Summary:\n");
            for (String s : SERVICES) {
                int assigned = serviceCounts.getOrDefault(s, 0);
                summary.append(String.format("• %-12s: %d\n", s, assigned));
            }
            serviceSummaryArea.setText(summary.toString());
        });
    }

    private void installLookAndFeel() {
        try {
            UIManager.setLookAndFeel("com.formdev.flatlaf.FlatIntelliJLaf");
        } catch (Exception ignored) {
            // default LAF is fine if FlatLaf not on classpath
        }
    }

    public static void main(String[] args) {
        new VolunteerGUI();
    }
}