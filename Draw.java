import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.*;
import java.net.*;

public class Draw extends JFrame {
    private Paper paper = new Paper();
    private DatagramSocket socketSend;
    private DatagramSocket socketReceive;
    private String remoteHost;
    private int remotePort;

    public static void main(String[] args) {
        if (args.length != 3) {
            System.out.println("Usage: java Draw <local port> <remote host> <remote port>");
            System.exit(1);
        }
        new Draw(Integer.parseInt(args[0]), args[1], Integer.parseInt(args[2]));
    }

    public Draw(int localPort, String remoteHost, int remotePort) {
        this.remoteHost = remoteHost;
        this.remotePort = remotePort;
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        getContentPane().add(paper, BorderLayout.CENTER);
        setSize(640, 480);
        setVisible(true);

        try {
            socketSend = new DatagramSocket();
            socketReceive = new DatagramSocket(localPort);
            startReceiver();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void startReceiver() {
        Thread receiverThread = new Thread(() -> {
            try {
                byte[] buffer = new byte[256];
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                while (true) {
                    socketReceive.receive(packet);
                    String message = new String(packet.getData(), 0, packet.getLength());
                    paper.addPointFromString(message);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        receiverThread.start();
    }

    public void sendPoint(Point point) {
        try {
            String message = point.x + " " + point.y;
            byte[] buffer = message.getBytes();
            InetAddress address = InetAddress.getByName(remoteHost);
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length, address, remotePort);
            socketSend.send(packet);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

class Paper extends JPanel {
    // Ändra definitionen av HashSet för att specificera att den innehåller Point-objekt
    private HashSet<Point> hs = new HashSet<>();

    public Paper() {
        setBackground(Color.white);
        addMouseListener(new L1());
        addMouseMotionListener(new L2());
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.setColor(Color.black);
        for (Point p : hs) { // Använda förbättrad for-loop för att iterera över Point-objekt
            g.fillOval(p.x, p.y, 2, 2);
        }
    }

    private void addPoint(Point p, boolean send) {
        hs.add(p);
        repaint();
        if (send) {
            ((Draw) SwingUtilities.getWindowAncestor(this)).sendPoint(p); // Send point to remote
        }
    }

    class L1 extends MouseAdapter {
        public void mousePressed(MouseEvent me) {
            addPoint(me.getPoint(), true);
        }
    }

    class L2 extends MouseMotionAdapter {
        public void mouseDragged(MouseEvent me) {
            addPoint(me.getPoint(), true);
        }
    }

    public void addPointFromString(String message) {
        String[] parts = message.split(" ");
        if (parts.length == 2) {
            try {
                int x = Integer.parseInt(parts[0]);
                int y = Integer.parseInt(parts[1]);
                Point p = new Point(x, y);
                addPoint(p, false);
            } catch (NumberFormatException e) {
                System.err.println("Error parsing point from string: " + message);
            }
        }
    }
}