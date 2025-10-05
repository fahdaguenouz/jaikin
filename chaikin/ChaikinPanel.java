package chaikin;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;

public class ChaikinPanel extends JPanel implements MouseListener, KeyListener, ActionListener {
    private final List<Point> points = new ArrayList<>();
    private List<Point> linesPoints = new ArrayList<>();
    private boolean animationStarted = false;
    private int steps = 0;
    private final int MAX_STEPS = 7;

    private Timer timer;

    public ChaikinPanel() {
        setBackground(Color.BLACK);
        addMouseListener(this);
        addKeyListener(this);
        setFocusable(true);

        timer = new Timer(500, this); // 0.5 seconds per step
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Draw points
        g.setColor(Color.GREEN);
        for (Point p : points) {
            g.fillOval((int) (p.x - 3), (int) (p.y - 3), 6, 6);
        }

        // Draw lines for the animation
        if (linesPoints.size() >= 2) {
            for (int i = 1; i < linesPoints.size(); i++) {
                Point p1 = linesPoints.get(i - 1);
                Point p2 = linesPoints.get(i);
                g.drawLine((int) p1.x, (int) p1.y, (int) p2.x, (int) p2.y);
            }
        } else if (linesPoints.size() == 1) {
            Point p = linesPoints.get(0);
            g.fillOval((int) (p.x - 6), (int) (p.y - 6), 12, 12);
        }

        // Draw instructions
        g.setFont(new Font("Arial", Font.PLAIN, 16));
        if (!animationStarted && points.isEmpty()) {
            g.drawString("Click to add points (at least 2) then press Enter", 10, 50);
        } else {
            g.drawString("ESC to quit | Click to place points | Enter to start animation | R to reset", 10, 20);
        }
        if (animationStarted) {
            g.setColor(Color.YELLOW);
            g.drawString("Chaikin step: " + steps + " / " + MAX_STEPS, 10, getHeight() - 20);
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {
        if (!animationStarted && e.getButton() == MouseEvent.BUTTON1) {
            points.add(new Point(e.getX(), e.getY()));
            repaint();
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_ENTER:
                if (points.size() >= 2) {
                    animationStarted = true;
                    linesPoints = new ArrayList<>(points);
                    steps = 0;
                    repaint();
                    new Timer(100, evt -> {
                        timer.start();
                        ((Timer) evt.getSource()).stop();
                    }).start();
                }
                break;
            case KeyEvent.VK_R:
                points.clear();
                linesPoints.clear();
                animationStarted = false;
                steps = 0;
                timer.stop();
                repaint();
                break;
            case KeyEvent.VK_ESCAPE:
                System.exit(0);
                break;
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        // If only two points, just draw the line and stop the animation
        if (points.size() == 2) {
            timer.stop();
            linesPoints = new ArrayList<>(points);
            repaint();
            return;
        }

        // Apply Chaikin only when there are more than two points
        if (steps < MAX_STEPS) {
            linesPoints = chaikinAlgorithm(linesPoints);
            steps++;
            repaint();
        } else {
            linesPoints = new ArrayList<>(points);
            steps = 0;
            repaint();
        }
    }

    private List<Point> chaikinAlgorithm(List<Point> input) {
        List<Point> newPoints = new ArrayList<>();
        if (input.size() < 2)
            return input;

        // Duplicate first point
        newPoints.add(new Point(input.get(0).x, input.get(0).y));

        for (int i = 0; i < input.size() - 1; i++) {
            Point p0 = input.get(i);
            Point p1 = input.get(i + 1);

            Point q = new Point(
                    0.75 * p0.x + 0.25 * p1.x,
                    0.75 * p0.y + 0.25 * p1.y);

            Point r = new Point(
                    0.25 * p0.x + 0.75 * p1.x,
                    0.25 * p0.y + 0.75 * p1.y);

            newPoints.add(q);
            newPoints.add(r);
        }

        // Duplicate last point
        newPoints.add(new Point(input.get(input.size() - 1).x,
                input.get(input.size() - 1).y));

        return newPoints;
    }

    // Simple 2D point class with doubles for coordinates
    private static class Point {
        double x, y;

        Point(double x, double y) {
            this.x = x;
            this.y = y;
        }
    }

    // Unused interface methods - required by interfaces but unused here
    public void mouseReleased(MouseEvent e) {
    }

    public void mouseClicked(MouseEvent e) {
    }

    public void mouseEntered(MouseEvent e) {
    }

    public void mouseExited(MouseEvent e) {
    }

    public void keyTyped(KeyEvent e) {
    }

    public void keyReleased(KeyEvent e) {
    }
}
