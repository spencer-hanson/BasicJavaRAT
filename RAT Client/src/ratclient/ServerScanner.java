package ratclient;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;

class ServerScanner extends Thread {
	public InetAddress broadcastAddress;
	public boolean stop;
    public MulticastSocket socket;
    private ArrayList<String> ips = new ArrayList<String>(20);
    
    public ServerScanner() throws IOException {
        super("ServerScanner");
        stop = false;
        this.setDaemon(true);
        this.socket = new MulticastSocket(6253);
        this.broadcastAddress = InetAddress.getByName("224.134.68.1");//let's all meet at this address
        this.socket.setSoTimeout(5000);
        this.socket.joinGroup(this.broadcastAddress);
    }

    public void run() {
        byte[] data = new byte[1024];

        while (!this.isInterrupted() && !stop) {
            DatagramPacket packet = new DatagramPacket(data, data.length); //setup packet to broadcast to address
            try {
                this.socket.receive(packet); //try to get data from that address
            } catch (SocketTimeoutException e) {
                continue;
            } catch (IOException e) {
                e.printStackTrace();
                break;
            }

            String strData = new String(packet.getData(), packet.getOffset(), packet.getLength()); //get data
            
            if(!ips.contains(strData)) {//if we don't have reference to the ip of the server stored,
            	ips.add(strData); //add it to list
            }
        }

        try {
            this.socket.leaveGroup(this.broadcastAddress);
        }
        catch (Exception e) {
        	e.printStackTrace();
        	System.exit(-1);
        }
        socket.close();
    }
    
    public void disconnect() {
    	interrupt();
    	stop = true;
    }
    
    public ArrayList<String> getServers() {
    	return ips;
    }
}