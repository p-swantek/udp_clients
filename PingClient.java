import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

/**
 * This class will create a client that will send a series of datagram packets
 * to a server indicated by the host and port command line arguments. Client
 * will send 10 packets to the server and wait for responses for each packet.
 * After the 10 packets are sent, client will print out the results of
 * performing the pinging of the server
 * 
 * @author Peter Swantek
 *
 */
public class PingClient {

    //constants to signify either normal or abnormal termination of the program
    private static final int EXIT_SUCCESS = 0;
    private static final int EXIT_FAILURE = 1;

    public static void main(String[] args) {

        //if improper amount of command line arguments are supplied, print a message then exit
        if (args.length != 2) {
            System.out.println("Usage: $java UDPClient <host> <port number>");
            System.exit(EXIT_FAILURE); //abnormal termination due to invalid amount of command line arguments
        }

        String host = args[0]; //get the host address
        int port = Integer.parseInt(args[1]); //get the port the host is listening on

        Client client = null;

        try {
            client = new Client(host, port); //create a new client to ping the server at the given address and port
            client.pingServer(); //have the client perform the pinging of the server
            client.printResults(); //have the client print out the results from doing the pinging
        }

        catch (SocketException e) {
            System.out.println("Error occured during socket creation...");
        }

        catch (UnknownHostException e) {
            System.out.println("Client attempted to connect to an unknown host...");
        }

        finally {
            client.shutDown(); //after client has finished, have the client shut down any open sockets it created
        }

        System.exit(EXIT_SUCCESS); //program has terminated normally

    }

}

/**
 * Represents a client that will send UDP packets to a server. Will ping a
 * server indicated by the host and port that the server is running on, will
 * send a series of datagram packets to the server in order to see how many
 * packets get lost and how long it takes for a sent packet to be sent back from
 * the server in order to calculate the round trip time (RTT) of the sent
 * packets
 * 
 * @author Peter Swantek
 *
 */
class Client {

    private static final int NUM_PACKETS = 10; //ping client will send a series of 10 packets
    private static final int TIMEOUT = 1000; //timeout time in milliseconds
    private static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss"); //format of the date to be used in ping message
    private static final int BUFFER_SIZE = 1024; //size of the buffer to read data into, in bytes
    private static final String CRLF = "\r\n"; //used to end the lines of the data sent on a packet

    private InetAddress _host; //address of the server
    private int _port; //port that the server is listening on
    private int _receivedPackets; //amount of responses received from the server
    private DatagramSocket _sock; //socket connection to the server to send datagram packets on
    private List<Long> _rtts; //list of the round trip times for each packet sent
    private Calendar _calendar; //used to get the current time

    /**
     * Create a new client to connect to a server at the given host and port
     * 
     * @param host String representing the IP address of the server
     * @param port integer representing the port number the server is listening
     *            on
     * @throws SocketException if there is an error creating a socket connection
     *             to the server
     * @throws UnknownHostException if the IP address cannot be resolved
     */
    public Client(String host, int port) throws SocketException, UnknownHostException {

        _host = InetAddress.getByName(host);
        _port = port;
        _receivedPackets = 0;
        _sock = new DatagramSocket(); //socket connection uses a Datagram socket connection
        _sock.setSoTimeout(TIMEOUT); //set the socket to have a timeout time of 1 second so client doesn't wait indefinitely for packets
        _rtts = new ArrayList<>();
    }

    /**
     * Loop, sending a series of packets to the server and wait for the server
     * to send a data packet back in response.
     * 
     */
    public void pingServer() {

        int count = 0;

        //loop to send a total of 10 packets to the server
        while (count < NUM_PACKETS) {

            count++;

            _calendar = Calendar.getInstance(); //get current time
            DATE_FORMAT.setCalendar(_calendar);
            String data = String.format("PING %d %s%s", count, DATE_FORMAT.format(_calendar.getTime()), CRLF); //message to send to server

            // Create packet and send to server
            byte[] toSend = data.getBytes();
            DatagramPacket sendPacket = new DatagramPacket(toSend, toSend.length, _host, _port); //create DatagramPacket to hold the message

            long start = System.currentTimeMillis(); //time the packet was sent

            try {

                _sock.send(sendPacket); //send the packet thru the socket

                DatagramPacket received = new DatagramPacket(new byte[BUFFER_SIZE], BUFFER_SIZE); //packet to hold the data sent in response
                _sock.receive(received); //try to receive a packet back from the server, will throw exception if we wait longer than 1 second for the data

                long total = System.currentTimeMillis() - start; //get the total time it took to send and receive a response

                _receivedPackets++; //increment the amount of packets received by 1

                _rtts.add(total); //add the RTT for this packet to the list of RTTs

                String addr = received.getAddress().toString(); //get the IP address of the server sending the response

                //print to the console the response we receive from the server
                System.out.println(String.format("PING received from %s: seq#=%d time=%d", addr.substring(1, addr.length()), count, total));

            }

            //socket will throw an exception if we wait more than 1 second for a response from the server
            catch (SocketTimeoutException e) {

                System.out.println("Request timed out.");

            }

            catch (IOException e) {
                System.out.println("I/O Error occured...");

            }

        } //end while loop

    }

    /**
     * prints out the total statistics from pinging the server, will print out
     * the percentage of packets that were lost while pinging the server as well
     * as the min, average, and max RTTs that were obtained while pinging the
     * server
     * 
     */
    public void printResults() {

        int size = _rtts.size();

        //get the min, average, and max value from the list of RTTs
        long min = size > 0 ? Collections.min(_rtts) : 0;
        long avg = size > 0 ? sumUp(_rtts) / size : 0;
        long max = size > 0 ? Collections.max(_rtts) : 0;

        //print all the relevant statistics out
        System.out.println("\n--- ping statistics ---");
        int packetLoss = (int) (100 - (((float) _receivedPackets / (float) NUM_PACKETS) * 100));
        System.out.println(String.format("%d packets transmitted, %d received, %d%s packet loss", NUM_PACKETS, _receivedPackets, packetLoss, "%"));
        System.out.println(String.format("rtt min/avg/max = %d %d %d ms", min, avg, max));

    }

    /*
     * helper method to add up all the RTTs that were in the list of RTTs
     * 
     */
    private long sumUp(List<Long> rtts) {
        long sum = 0;

        for (long l : rtts) {
            sum += l;
        }

        return sum;
    }

    /**
     * Shut down the client, will close the open socket that the client opened
     * to the server
     * 
     */
    public void shutDown() {
        _sock.close(); //close the datagram socket
    }

}