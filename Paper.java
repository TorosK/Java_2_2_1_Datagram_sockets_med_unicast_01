import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.*;

/**
 * The Paper class extends JPanel and serves as the drawing area for the Draw application.
 * It captures mouse events to draw points and handles network communication to synchronize drawings.
 */
class Paper extends JPanel {
    // A HashSet to store drawn points. Points are unique; no duplicates are allowed.
    private HashSet<Point> hs = new HashSet<>();

    /**
     * Constructor for Paper. Sets up the drawing area, including background color and event listeners.
     */
    public Paper() {
        setBackground(Color.white); // Set the background color of the drawing area to white
        addMouseListener(new L1()); // Add a mouse listener to capture clicks
        addMouseMotionListener(new L2()); // Add a motion listener to capture drag events
    }

    /**
     * Overrides the paintComponent method of JPanel to custom draw the points.
     * @param g The Graphics object used for drawing.
     */
    public void paintComponent(Graphics g) {
        super.paintComponent(g); // Call the superclass method for necessary initial painting tasks
        g.setColor(Color.black); // Set the drawing color to black

        // Iterate over the HashSet of points and draw each point as a small oval
        for (Point p : hs) {
            g.fillOval(p.x, p.y, 2, 2); // Draw an oval at each point's location
        }
    }

    /**
     * Adds a point to the drawing area and optionally sends it to the remote instance.
     * @param p The point to add.
     * @param send A flag indicating whether the point should be sent to the remote instance.
     */
    private void addPoint(Point p, boolean send) {
        hs.add(p); // Add the point to the HashSet
        repaint(); // Request a repaint of the panel to reflect the added point

        // If send is true, use the Draw instance to send the point to the remote instance
        if (send) {
            ((Draw) SwingUtilities.getWindowAncestor(this)).sendPoint(p);
        }
    }

    /**
     * Inner class to handle mouse pressed events. Adds points where the mouse is pressed.
     */
    class L1 extends MouseAdapter {
        public void mousePressed(MouseEvent me) {
            addPoint(me.getPoint(), true); // Add the point where the mouse was pressed and send it
        }
    }

    /**
     * Inner class to handle mouse dragged events. Adds points as the mouse is dragged.
     */
    class L2 extends MouseMotionAdapter {
        public void mouseDragged(MouseEvent me) {
            addPoint(me.getPoint(), true); // Add points during dragging and send them
        }
    }

    /**
     * Adds a point from a received network message.
     * @param message The received message containing the point coordinates.
     */
    public void addPointFromString(String message) {
        String[] parts = message.split(" "); // Split the message into parts based on space

        // Expecting the message to have exactly 2 parts: x and y coordinates
        if (parts.length == 2) {
            try {
                // Parse the coordinates from the message parts
                int x = Integer.parseInt(parts[0]);
                int y = Integer.parseInt(parts[1]);
                Point p = new Point(x, y); // Create a new Point object with the parsed coordinates

                // Add the point without sending it back to avoid infinite loops
                addPoint(p, false);
            } catch (NumberFormatException e) {
                System.err.println("Error parsing point from string: " + message);
            }
        }
    }
}
