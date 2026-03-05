import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Random;

public class JestJava extends JFrame {

    // --- Color Palette ---
    private static final Color BG_DARK       = new Color(10, 10, 18);
    private static final Color BG_CARD       = new Color(18, 18, 30);
    private static final Color ACCENT_BLUE   = new Color(80, 140, 255);
    private static final Color ACCENT_PURPLE = new Color(140, 80, 255);
    private static final Color TEXT_PRIMARY  = new Color(230, 230, 255);
    private static final Color TEXT_MUTED    = new Color(130, 130, 170);
    private static final Color BORDER_COLOR  = new Color(40, 40, 70);
    private static final Color SUCCESS_COLOR = new Color(50, 220, 140);
    private static final Color ERROR_COLOR   = new Color(255, 90, 90);

    private JPanel cardPanel;
    private CardLayout cardLayout;

    // State Variables
    private int userAge = 0;
    private String currentCaptcha = "";

    // UI Components
    private JTextField ageField, captchaField, searchField;
    private JLabel ageErrorLabel, captchaErrorLabel, statusLabel, captchaDisplay;

    public JestJava() {
        setTitle("Security Verified Portal");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(500, 650);
        setLocationRelativeTo(null);
        setResizable(false);

        cardLayout = new CardLayout();
        cardPanel  = new JPanel(cardLayout);
        cardPanel.setBackground(BG_DARK);

        // Build Screens
        cardPanel.add(buildAgeScreen(), "age");
        cardPanel.add(buildCaptchaScreen(), "captcha");
        cardPanel.add(buildSearchScreen(), "search");
        cardPanel.add(buildKidsScreen(), "kids");

        add(cardPanel);
        cardLayout.show(cardPanel, "age");
    }

    // ─── Shared UI Helpers ──────────────────────────────────────────────────

    private JPanel makeBasePanel() {
        return new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setPaint(new GradientPaint(0, 0, BG_DARK, getWidth(), getHeight(), new Color(15, 10, 30)));
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.setColor(new Color(255, 255, 255, 10));
                for (int x = 0; x < getWidth(); x += 30)
                    for (int y = 0; y < getHeight(); y += 30)
                        g2.fillOval(x, y, 2, 2);
                g2.dispose();
            }
        };
    }

    private JPanel makeCard(int width, int height) {
        JPanel card = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(BG_CARD);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 24, 24);
                g2.setColor(BORDER_COLOR);
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 24, 24);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setMaximumSize(new Dimension(width, height));
        card.setPreferredSize(new Dimension(width, height));
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(30, 40, 30, 40));
        return card;
    }

    private JTextField makeField(String placeholder) {
        JTextField f = new JTextField();
        f.setOpaque(false);
        f.setForeground(TEXT_PRIMARY);
        f.setCaretColor(ACCENT_BLUE);
        f.setFont(new Font("Monospaced", Font.BOLD, 16));
        f.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BORDER_COLOR, 1, true),
                new EmptyBorder(10, 15, 10, 15)
        ));
        return f;
    }

    private JButton makeButton(String text, Color c1, Color c2) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("SansSerif", Font.BOLD, 14));
        btn.setForeground(Color.WHITE);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setFocusPainted(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        // Custom painting for gradient and rounded corners
        btn.setUI(new javax.swing.plaf.basic.BasicButtonUI() {
            @Override
            public void paint(Graphics g, JComponent c) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setPaint(new GradientPaint(0, 0, c1, c.getWidth(), c.getHeight(), c2));
                g2.fillRoundRect(0, 0, c.getWidth(), c.getHeight(), 15, 15);
                super.paint(g2, c);
                g2.dispose();
            }
        });
        return btn;
    }

    // ─── Screen 1: Age Check ────────────────────────────────────────────────

    private JPanel buildAgeScreen() {
        JPanel base = makeBasePanel();
        JPanel center = new JPanel(new GridBagLayout()); center.setOpaque(false);
        JPanel card = makeCard(380, 350);

        JLabel title = new JLabel("Identity Check", SwingConstants.CENTER);
        title.setFont(new Font("SansSerif", Font.BOLD, 24));
        title.setForeground(TEXT_PRIMARY);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        ageField = makeField("Age");
        ageField.setAlignmentX(Component.CENTER_ALIGNMENT);

        ageErrorLabel = new JLabel("Please enter your age");
        ageErrorLabel.setForeground(TEXT_MUTED);
        ageErrorLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton nextBtn = makeButton("Proceed", ACCENT_BLUE, ACCENT_PURPLE);
        nextBtn.setMaximumSize(new Dimension(200, 45));
        nextBtn.setAlignmentX(Component.CENTER_ALIGNMENT);

        nextBtn.addActionListener(e -> {
            try {
                userAge = Integer.parseInt(ageField.getText());
                generateCaptcha();
                cardLayout.show(cardPanel, "captcha");
            } catch (Exception ex) {
                ageErrorLabel.setText("Invalid age format!");
                ageErrorLabel.setForeground(ERROR_COLOR);
            }
        });

        card.add(title); card.add(Box.createVerticalStrut(30));
        card.add(ageField); card.add(Box.createVerticalStrut(10));
        card.add(ageErrorLabel); card.add(Box.createVerticalStrut(30));
        card.add(nextBtn);

        center.add(card);
        base.add(center, BorderLayout.CENTER);
        return base;
    }

    // ─── Screen 2: Captcha ──────────────────────────────────────────────────

    private JPanel buildCaptchaScreen() {
        JPanel base = makeBasePanel();
        JPanel center = new JPanel(new GridBagLayout()); center.setOpaque(false);
        JPanel card = makeCard(380, 400);

        JLabel title = new JLabel("Human Verification", SwingConstants.CENTER);
        title.setFont(new Font("SansSerif", Font.BOLD, 20));
        title.setForeground(TEXT_PRIMARY);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        captchaDisplay = new JLabel("", SwingConstants.CENTER);
        captchaDisplay.setFont(new Font("Monospaced", Font.ITALIC | Font.BOLD, 32));
        captchaDisplay.setForeground(ACCENT_BLUE);
        captchaDisplay.setBorder(new LineBorder(BORDER_COLOR, 1));
        captchaDisplay.setAlignmentX(Component.CENTER_ALIGNMENT);

        captchaField = makeField("Type Captcha");
        captchaField.setAlignmentX(Component.CENTER_ALIGNMENT);

        captchaErrorLabel = new JLabel("Enter the code above to continue");
        captchaErrorLabel.setForeground(TEXT_MUTED);
        captchaErrorLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton verifyBtn = makeButton("Verify", SUCCESS_COLOR, new Color(30, 150, 100));
        verifyBtn.setMaximumSize(new Dimension(200, 45));
        verifyBtn.setAlignmentX(Component.CENTER_ALIGNMENT);

        verifyBtn.addActionListener(e -> {
            if (captchaField.getText().equals(currentCaptcha)) {
                if (userAge >= 18) cardLayout.show(cardPanel, "search");
                else cardLayout.show(cardPanel, "kids");
            } else {
                captchaErrorLabel.setText("Wrong code! Try again.");
                captchaErrorLabel.setForeground(ERROR_COLOR);
                generateCaptcha();
            }
        });

        card.add(title); card.add(Box.createVerticalStrut(20));
        card.add(captchaDisplay); card.add(Box.createVerticalStrut(20));
        card.add(captchaField); card.add(Box.createVerticalStrut(10));
        card.add(captchaErrorLabel); card.add(Box.createVerticalStrut(25));
        card.add(verifyBtn);

        center.add(card);
        base.add(center, BorderLayout.CENTER);
        return base;
    }

    // ─── Screen 3: Search (Your Original Logic) ─────────────────────────────

    private JPanel buildSearchScreen() {
        JPanel base = makeBasePanel();
        JPanel center = new JPanel(new GridBagLayout()); center.setOpaque(false);
        JPanel card = makeCard(400, 400);

        JLabel title = new JLabel("Adult Search", SwingConstants.CENTER);
        title.setFont(new Font("SansSerif", Font.BOLD, 22));
        title.setForeground(TEXT_PRIMARY);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        searchField = makeField("Search Keyword...");
        searchField.setAlignmentX(Component.CENTER_ALIGNMENT);

        statusLabel = new JLabel("Access Granted (18+)");
        statusLabel.setForeground(SUCCESS_COLOR);
        statusLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton searchBtn = makeButton("Search Now", ACCENT_BLUE, new Color(0, 100, 200));
        searchBtn.setMaximumSize(new Dimension(250, 50));
        searchBtn.setAlignmentX(Component.CENTER_ALIGNMENT);

        searchBtn.addActionListener(e -> {
            String q = searchField.getText().trim();
            if(q.isEmpty()) return;
            try {
                String encoded = URLEncoder.encode(q, StandardCharsets.UTF_8.toString());
                Desktop.getDesktop().browse(new URI("https://de.pornhub.com/video/search?search=" + encoded));
            } catch (Exception ex) {
                statusLabel.setText("Error opening browser");
            }
        });

        card.add(title); card.add(Box.createVerticalStrut(30));
        card.add(searchField); card.add(Box.createVerticalStrut(15));
        card.add(statusLabel); card.add(Box.createVerticalStrut(30));
        card.add(searchBtn);

        center.add(card);
        base.add(center, BorderLayout.CENTER);
        return base;
    }

    // ─── Screen 4: Kids ─────────────────────────────────────────────────────

    private JPanel buildKidsScreen() {
        JPanel base = makeBasePanel();
        JPanel center = new JPanel(new GridBagLayout()); center.setOpaque(false);
        JPanel card = makeCard(380, 300);

        JLabel icon = new JLabel("🎈", SwingConstants.CENTER);
        icon.setFont(new Font("Serif", Font.PLAIN, 60));
        icon.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel msg = new JLabel("<html><center>Too young for this portal!<br>Redirecting to safe content.</center></html>");
        msg.setForeground(TEXT_PRIMARY);
        msg.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton kidsBtn = makeButton("Open YouTube Kids", new Color(255, 80, 80), new Color(200, 0, 0));
        kidsBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        kidsBtn.addActionListener(e -> {
            try { Desktop.getDesktop().browse(new URI("https://www.youtubekids.com")); } catch (Exception ignored) {}
        });

        card.add(icon); card.add(Box.createVerticalStrut(20));
        card.add(msg); card.add(Box.createVerticalStrut(30));
        card.add(kidsBtn);

        center.add(card);
        base.add(center, BorderLayout.CENTER);
        return base;
    }

    // ─── Logic Helpers ──────────────────────────────────────────────────────

    private void generateCaptcha() {
        String chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
        StringBuilder sb = new StringBuilder();
        Random rnd = new Random();
        while (sb.length() < 6) {
            sb.append(chars.charAt(rnd.nextInt(chars.length())));
        }
        currentCaptcha = sb.toString();
        captchaDisplay.setText("  " + currentCaptcha + "  ");
        captchaField.setText("");
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new JestJava().setVisible(true));
    }
}