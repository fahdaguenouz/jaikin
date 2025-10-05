import chaikin.ChaikinPanel;

import javax.swing.JFrame;
public class Main {
    public static void main(String[] args) {
        JFrame frame = new JFrame("Chaikin Animation");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setContentPane(new ChaikinPanel());
        frame.setSize(800, 600);
        frame.setVisible(true);
    }
}

