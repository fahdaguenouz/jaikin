import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

/**
 * ChaikinAnimation — Swing implementation of Chaikin's algorithm with step-by-step animation.
 */
public class Jaikin {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Chaikin");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            CanvasPanel panel = new CanvasPanel();
            frame.add(panel);
            frame.setSize(1000, 700);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
            panel.requestFocusInWindow();
            System.out.println("Window created! Click to add points, Enter to start animation.");
        });
    }

    static class CanvasPanel extends JPanel implements MouseListener, MouseMotionListener, KeyListener, ActionListener {
        private final List<Point2D.Float> positions = new ArrayList<>();            // control points
        private final List<List<Point2D.Float>> animationSteps = new ArrayList<>(); // precomputed steps
        private final Timer animationTimer;                                         // drives step changes
        private final Timer messageTimer;                                           // hides empty-message after delay

        private boolean isAnimating = false;
        private boolean showEmptyMessage = false;
        private boolean closed = false; // closed polyline toggle (L)
        private int currentStep = 0;
        private final int maxSteps = 6;
        private final int stepDurationMs = 1000; // 1 second per step
        private int draggingIndex = -1;

        CanvasPanel() {
            setBackground(Color.BLACK);
            setFocusable(true);
            addMouseListener(this);
            addMouseMotionListener(this);
            addKeyListener(this);

            animationTimer = new Timer(stepDurationMs, this);
            messageTimer = new Timer(2000, e -> {
                showEmptyMessage = false;
                // messageTimer.stop();
                repaint();
            });
            messageTimer.setRepeats(false);
        }

        // Timer tick: advance animation
        @Override
        public void actionPerformed(ActionEvent e) {
            if (e.getSource() == animationTimer) {
                currentStep = (currentStep + 1) % (maxSteps + 1);
                if (currentStep == 0) System.out.println("Animation restarted");
                repaint();
            }
        }

        // Chaikin's algorithm. If closed==true we treat polyline as closed.
        private List<Point2D.Float> chaikin(List<Point2D.Float> pts, boolean closed) {
            List<Point2D.Float> out = new ArrayList<>();
            int n = pts.size();
            if (n < 2) return copy(pts);
            if (closed) {
                for (int i = 0; i < n; i++) {
                    Point2D.Float p1 = pts.get(i);
                    Point2D.Float p2 = pts.get((i + 1) % n);
                    out.add(lerp(p1, p2, 0.25f));
                    out.add(lerp(p1, p2, 0.75f));
                }
            } else {
                // preserve endpoints for open polyline
                out.add(new Point2D.Float(pts.get(0).x, pts.get(0).y));
                for (int i = 0; i < n - 1; i++) {
                    Point2D.Float p1 = pts.get(i);
                    Point2D.Float p2 = pts.get(i + 1);
                    out.add(lerp(p1, p2, 0.25f));
                    out.add(lerp(p1, p2, 0.75f));
                }
                out.add(new Point2D.Float(pts.get(n - 1).x, pts.get(n - 1).y));
            }
            return out;
        }

        private Point2D.Float lerp(Point2D.Float a, Point2D.Float b, float t) {
            float x = a.x * (1 - t) + b.x * t;
            float y = a.y * (1 - t) + b.y * t;
            return new Point2D.Float(x, y);
        }

        private List<Point2D.Float> copy(List<Point2D.Float> src) {
            List<Point2D.Float> c = new ArrayList<>(src.size());
            for (Point2D.Float p : src) c.add(new Point2D.Float(p.x, p.y));
            return c;
        }

        // Start (or restart) animation by precomputing steps
        private void startAnimation() {
            animationSteps.clear();
            animationSteps.add(copy(positions)); // step 0 = original control points
            List<Point2D.Float> current = copy(positions);
            for (int i = 0; i < maxSteps; i++) {
                current = chaikin(current, closed);
                animationSteps.add(copy(current));
            }
            isAnimating = true;
            currentStep = 0;
            animationTimer.setDelay(stepDurationMs);
            animationTimer.start();
            System.out.println("Starting Chaikin animation with " + positions.size() + " points (" + (closed ? "closed" : "open") + " polyline)");
        }

        private void stopAnimation() {
            animationTimer.stop();
            isAnimating = false;
            currentStep = 0;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // Instructions / top-left helper text
            g2.setFont(g2.getFont().deriveFont(14f));
            g2.setColor(Color.WHITE);
            int y = 20;
            g2.drawString("Click to add points. Drag points to move. Enter to start animation.", 10, y);
            y += 18;
            g2.drawString("2 points -> straight line, 3+ points -> smooth curve. C to clear, L to toggle closed/open, Esc to exit.", 10, y);
            y += 18;
            g2.drawString("Mode: " + (closed ? "CLOSED" : "OPEN") + "    Points: " + positions.size(), 10, y);

            // Draw persistent straight line if exactly 2 points (and not animating)
            g2.setStroke(new BasicStroke(2f));
            if (!isAnimating && positions.size() == 2) {
                g2.setColor(Color.WHITE);
                Point2D.Float a = positions.get(0), b = positions.get(1);
                g2.drawLine(Math.round(a.x), Math.round(a.y), Math.round(b.x), Math.round(b.y));
            }

            // Draw animated polyline if animating
            if (isAnimating && !animationSteps.isEmpty()) {
                List<Point2D.Float> pts = animationSteps.get(currentStep);
                if (pts.size() >= 2) {
                    g2.setColor(Color.WHITE);
                    for (int i = 0; i < pts.size() - 1; i++) {
                        Point2D.Float p1 = pts.get(i);
                        Point2D.Float p2 = pts.get(i + 1);
                        g2.drawLine(Math.round(p1.x), Math.round(p1.y), Math.round(p2.x), Math.round(p2.y));
                    }
                    // if closed, close the loop visually
                    if (closed && pts.size() >= 2) {
                        Point2D.Float last = pts.get(pts.size() - 1);
                        Point2D.Float first = pts.get(0);
                        g2.drawLine(Math.round(last.x), Math.round(last.y), Math.round(first.x), Math.round(first.y));
                    }
                }
                // step counter
                g2.setColor(Color.WHITE);
                g2.setFont(g2.getFont().deriveFont(20f).deriveFont(Font.BOLD));
                String stepText = String.format("Step %d / %d", currentStep, maxSteps);
                g2.drawString(stepText, 20, getHeight() - 30);
            }

            // Draw control points (always visible)
            for (Point2D.Float p : positions) {
                int radius = 6;
                int cx = Math.round(p.x) - radius;
                int cy = Math.round(p.y) - radius;
                g2.setColor(Color.GRAY);
                g2.fillOval(cx, cy, radius * 2, radius * 2);
                g2.setColor(Color.WHITE);
                g2.drawOval(cx, cy, radius * 2, radius * 2);
            }

            // Show empty-message when user pressed Enter with no points
            if (showEmptyMessage) {
                g2.setColor(Color.RED);
                g2.setFont(g2.getFont().deriveFont(18f));
                String msg = "No points to process! Please add points.";
                g2.drawString(msg, 20, 120);
            }

            g2.dispose();
        }

        // --- Mouse / drag handling ---
        @Override
        public void mousePressed(MouseEvent e) {
            requestFocusInWindow();
            if (SwingUtilities.isLeftMouseButton(e)) {
                if (isAnimating) return; // do not add/drag while animating
                Point2D.Float clicked = new Point2D.Float(e.getX(), e.getY());
                int idx = findNearest(clicked, 8f);
                if (idx >= 0) {
                    draggingIndex = idx;
                } else {
                    positions.add(clicked);
                    System.out.printf("Point %d added at (%.1f, %.1f)%n", positions.size(), clicked.x, clicked.y);
                    repaint();
                }
            }
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            draggingIndex = -1;
        }

        @Override
        public void mouseDragged(MouseEvent e) {
            if (draggingIndex >= 0 && draggingIndex < positions.size()) {
                positions.get(draggingIndex).x = e.getX();
                positions.get(draggingIndex).y = e.getY();
                repaint();
            }
        }

        private int findNearest(Point2D.Float p, float maxDist) {
            for (int i = 0; i < positions.size(); i++) {
                if (p.distance(positions.get(i)) <= maxDist) return i;
            }
            return -1;
        }

        // --- Keyboard handling ---
        @Override
        public void keyPressed(KeyEvent e) {
            int code = e.getKeyCode();
            if (code == KeyEvent.VK_ENTER) {
                if (isAnimating) {
                    // ignore Enter while animating (keeps cycling)
                } else {
                    switch (positions.size()) {
                        case 0:
                            System.out.println("No points to process! Please add points.");
                            showEmptyMessage = true;
                            messageTimer.restart();
                            repaint();
                            break;
                        case 1:
                            System.out.println("Only one point — displaying point only.");
                            repaint();
                            break;
                        case 2:
                            // draw straight persistent line (just repaint)
                            System.out.println(String.format("Drawing persistent line from (%.1f, %.1f) to (%.1f, %.1f)",
                                    positions.get(0).x, positions.get(0).y, positions.get(1).x, positions.get(1).y));
                            repaint();
                            break;
                        default:
                            startAnimation();
                            break;
                    }
                }
            } else if (code == KeyEvent.VK_C) {
                // clear everything
                stopAnimation();
                positions.clear();
                animationSteps.clear();
                showEmptyMessage = false;
                System.out.println("Cleared all points and animation.");
                repaint();
            } else if (code == KeyEvent.VK_ESCAPE) {
                System.out.println("Escape pressed, exiting...");
                System.exit(0);
            } else if (code == KeyEvent.VK_L) {
                closed = !closed;
                System.out.println("Polyline mode toggled. Now: " + (closed ? "CLOSED" : "OPEN"));
                if (isAnimating && positions.size() >= 3) {
                    // recompute animation steps with new closed/open mode
                    startAnimation();
                } else {
                    repaint();
                }
            }
        }

        // unused interface methods
        @Override public void mouseClicked(MouseEvent e) {}
        @Override public void mouseEntered(MouseEvent e) {}
        @Override public void mouseExited(MouseEvent e) {}
        @Override public void mouseMoved(MouseEvent e) {}
        @Override public void keyTyped(KeyEvent e) {}
        @Override public void keyReleased(KeyEvent e) {}
    }
}
