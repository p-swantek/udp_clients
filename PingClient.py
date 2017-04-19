import sys
import time
from socket import *


def print_statistics(rtt_list, num_sent, num_received):

    min_time = min(rtt_list) if len(rtt_list) > 0 else 0
    max_time = max(rtt_list) if len(rtt_list) > 0 else 0
    avg_time = sum(rtt_list)/len(rtt_list) if len(rtt_list) > 0 else 0

    print("\n--- ping statistics ---")
    print("{} packets transmitted, {} received, {:.2f}% packet loss".format(num_sent, num_received, 100 - ((num_received/num_sent)*100)))
    print("rtt min/avg/max = {:.2f} {:.2f} {:.2f} ms".format(min_time, avg_time, max_time))

NUM_PACKETS = 10
BUFFER_SIZE = 1024

# Get the server hostname and port as command line arguments
argv = sys.argv
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
packets_received = 0
rtt_times = []

# Ping for 10 times
while ptime < 10:
    ptime += 1
    # Format the message to be sent.
    # use time.asctime() for currTime
    data = "PING {} {}\r\n".format(ptime, time.asctime(time.localtime()))

    try:
        # Sent time. from time.time()
        RTTb = time.time()
        # Send the UDP packet with the ping message
        client_socket.sendto(data.encode(), (host, port))
        # Receive the server response
        message, address = client_socket.recvfrom(1024)
        # Received time. use time.time()
        RTTa = time.time()
        packets_received += 1
        total = (RTTa - RTTb) * 1000
        rtt_times.append(total)

        # Display the server response as an output
        print("PING received from {}: seq#={} time={:.2f}".format(address[0], ptime, total))

    except:
        print("Request timed out.")
        continue

print_statistics(rtt_times, NUM_PACKETS, packets_received)

# Close the client socket
client_socket.close()
