import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.*;
import java.net.*;

/**
 * The Draw class extends JFrame and represents the main application window
 * for a simple networked drawing program. It utilizes UDP (Datagram Sockets)
 * for sending and receiving drawing coordinates between instances of the program.
 */
public class Draw extends JFrame {
  private Paper paper = new Paper(); // The drawing area
  private DatagramSocket socketSend; // Socket for sending drawing data
  private DatagramSocket socketReceive; // Socket for receiving drawing data
  private String remoteHost; // The hostname or IP of the remote host
  private int remotePort; // The port number on the remote host

  /**
   * The entry point of the application.
   * @param args Command-line arguments specifying local port, remote host, and remote port.
   */
  public static void main(String[] args) {
    if (args.length != 3) {
      System.out.println("Usage: java Draw <local port> <remote host> <remote port>");
      System.exit(1);
    }
    new Draw(Integer.parseInt(args[0]), args[1], Integer.parseInt(args[2]));
  }

  /**
   * Constructor for the Draw class. Sets up the GUI and initializes network sockets.
   * @param localPort The local port to listen for incoming UDP packets.
   * @param remoteHost The remote host's IP or hostname to send UDP packets to.
   * @param remotePort The remote port to send UDP packets to.
   */
  public Draw(int localPort, String remoteHost, int remotePort) {
    this.remoteHost = remoteHost;
    this.remotePort = remotePort;
    setDefaultCloseOperation(EXIT_ON_CLOSE); // Ensure the application exits on close
    getContentPane().add(paper, BorderLayout.CENTER); // Add the drawing area to the window
    setSize(640, 480); // Set the initial size of the window
    setVisible(true); // Make the window visible

    try {
      socketSend = new DatagramSocket(); // Create a socket for sending data
      socketReceive = new DatagramSocket(localPort); // Create a socket for receiving data on the specified local port
      startReceiver(); // Start the thread for receiving drawing data
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * Starts a background thread to listen for incoming UDP packets and update the drawing accordingly.
   */
  private void startReceiver() {
    Thread receiverThread = new Thread(() -> {
      try {
        byte[] buffer = new byte[256]; // Buffer for incoming data
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length); // Datagram packet for receiving data
        while (true) {
          socketReceive.receive(packet); // Block and wait for an incoming packet
          String message = new String(packet.getData(), 0, packet.getLength()); // Convert packet data to a String
          paper.addPointFromString(message); // Add the received point to the drawing
        }
      } catch (Exception e) {
        e.printStackTrace();
      }
    });
    receiverThread.start(); // Start the receiver thread
  }

  /**
   * Sends the given point's coordinates to the remote host using UDP.
   * @param point The point to send.
   */
  public void sendPoint(Point point) {
    try {
      // Convert the point's coordinates to a string message
      String message = point.x + " " + point.y;
      byte[] buffer = message.getBytes(); // Convert the string message to bytes
      InetAddress address = InetAddress.getByName(remoteHost); // Resolve the remote host's address
      // Create a datagram packet with the point data, remote host address, and port
      DatagramPacket packet = new DatagramPacket(buffer, buffer.length, address, remotePort);
      socketSend.send(packet); // Send the packet through the sending socket
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}