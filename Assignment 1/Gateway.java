//Name : Owen Burke
//Student Number : 15316452

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;

import tcdIO.Terminal;

public class Gateway extends Node 
{
	static final int DEFAULT_PORT = 50001;
	static final int DEFAULT_SERVER_DST_PORT = Server.DEFAULT_PORT;
	static final String DEFAULT_FINAL_DST_NODE = "localhost";	

	Terminal terminal;
	InetSocketAddress dstServerAddress;
	
	/*
	 * 
	 */
	Gateway(Terminal terminal,int port, String dstHost, int dstServerPort) 
	{
		try 
		{
			dstServerAddress= new InetSocketAddress(dstHost, dstServerPort);
			this.terminal= terminal;
			socket= new DatagramSocket(port);
			listener.go();
		}
		catch(java.lang.Exception e) 
		{
			e.printStackTrace();
		}
	}

	/**
	 * Assume that incoming packets contain a String and print the string.
	 */
	public void onReceipt(DatagramPacket packet) 
	{
		try 
		{
			if (packet.getSocketAddress().equals(dstServerAddress))
			{
				StringContent content= new StringContent(packet);
				String[] messageContent = content.toString().split("\n");
				String message = messageContent[0];
				String clientPort = messageContent[1];
				int clientPortNumber = Integer.parseInt(clientPort);
				InetSocketAddress clientAddress = new InetSocketAddress(DEFAULT_FINAL_DST_NODE, clientPortNumber);
				DatagramPacket response;
				response = (new StringContent(message + "\n" + messageContent[2])).toDatagramPacket();
				response.setSocketAddress(clientAddress);
				terminal.println("Forwarding packet to client...\n");
				socket.send(response);
			}
			else
			{
				packet.setSocketAddress(dstServerAddress);
				terminal.println("Forwarding packet to server...");
				socket.send(packet);
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}

	
	public synchronized void start() throws Exception 
	{
		terminal.println("Gateway waiting for contact...");
		this.wait();
	}
	
	/*
	 * 
	 */
	public static void main(String[] args) 
	{
		try 
		{					
			Terminal terminal= new Terminal("Gateway");
			(new Gateway(terminal, DEFAULT_PORT, DEFAULT_FINAL_DST_NODE, DEFAULT_SERVER_DST_PORT)).start();
		}
		catch(java.lang.Exception e)
		{
			e.printStackTrace();
		}
	}
}




