import sys
import time
from socket import *


def print_statistics(rtt_list, num_sent, num_received):
    '''
    will take as input a list of all the calculated RTTs, the total number
    of packets that were sent when pinging the server, and the number
    of packets that were received in response from the server after pinging
    it.  Will calculate the percentage of packets lost during pinging as
    well as the minimum, average, and maximum RTT during the pinging process.
    Will then print a message to the standard output that details this
    information.
    '''

    size = len(rtt_list)

    min_time = min(rtt_list) if size > 0 else 0 #get min RTT
    max_time = max(rtt_list) if size > 0 else 0 #get max RTT
    avg_time = sum(rtt_list)/size if size > 0 else 0 #get the average RTT

    #print out the statistics of pinging the server
    print("\n--- ping statistics ---")
    print("{} packets transmitted, {} received, {:.2f}% packet loss".format(num_sent, num_received, 100 - ((num_received/num_sent)*100)))
    print("rtt min/avg/max = {:.2f} {:.2f} {:.2f} ms".format(min_time, avg_time, max_time))

NUM_PACKETS = 10 #total amount of packets to be sent during the pinging process
BUFFER_SIZE = 1024 #size of the buffer for data, 1 kilobyte

# Get the server hostname and port as command line arguments
argv = sys.argv

# if invalid amount of arguments given, print a message and then exit
if len(argv) != 3:
    print("Usage: $py PingClient.py <hostname> <port>")
    sys.exit(1)

# otherwise, get the hostname and port that were passed in as command line args
host = argv[1]
port = argv[2]
timeout = 1  # in second, change as needed

# Create UDP client socket
# Note the use of SOCK_DGRAM for UDP datagram packet
client_socket = socket(AF_INET, SOCK_DGRAM)
# Set socket timeout as 1 second
client_socket.settimeout(timeout)
# Command line argument is a string, change the port into integer
port = int(port)
# Sequence number of the ping message
ptime = 0

packets_received = 0 #amount of packets successfully received from the server in reponse to a ping
rtt_times = [] #list to accumulate the RTTs of packets sent and then received back

# Ping for 10 times
while ptime < 10:
    ptime += 1
    # Format the message to be sent.
    # use time.asctime() for currTime
    data = "PING {} {}\r\n".format(ptime, time.asctime(time.localtime())) #data to send to server

    try:
        # Sent time. from time.time()
        RTTb = time.time()
        
        # Send the UDP packet with the ping message
        client_socket.sendto(data.encode(), (host, port))
        
        # Receive the server response
        message, address = client_socket.recvfrom(1024) #throws exception if timeout threshold is reached
        
        # Received time. use time.time()
        RTTa = time.time()

        #increment the amount of packets successfully received back
        packets_received += 1

        #get the RTT for this packet, multiply by 1000 to convert to milliseconds
        total = (RTTa - RTTb) * 1000

        #add this RTT to the list of RTTs
        rtt_times.append(total)

        # Display the server response as an output
        print("PING received from {}: seq#={} time={:.2f}".format(address[0], ptime, total))

    except:
        #if we get here, the server took too long to respond, just continue sending the other packets without waiting any more
        print("Request timed out.")
        continue

#print out the statistics from pinging the given server
print_statistics(rtt_times, NUM_PACKETS, packets_received)

# Close the client socket
client_socket.close()
