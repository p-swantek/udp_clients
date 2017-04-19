# udp_clients
UDP clients written in both java and python that will allow for pinging of a server.  After pinging, the clients will display the statistics of the pinging.  Will show all the packets sent along with the associated round trip time (RTT) for each packet.  The min, average, and max RTTs will be displayed.

### To compile the server:
> $javac PingServer.java

### To compile the Java client:
> $javac PingClient.java

### To start the server, pass in an integer command line argument to indicate the port the server will listen on.  For example, the following will start the server and have it listen for connections on port 2000:
> $java PingServer 2000

### To run the java client, pass in the hostname of the server and the port.  For example, if the server is running on localhost at port 2000:
> $java PingClient localhost 2000

### To run the Python client, pass in similar arguments as the Java client:
> $python PingClient.py localhost 2000

### Server output:
![server output](img/server_output.png)






