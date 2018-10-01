/**
 * 
 */

import java.net.DatagramSocket;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetSocketAddress;
import java.net.ServerSocket;

import tcdIO.*;

//Name : Owen Burke
//Student Number : 15316452

/**
 *
 * Client class
 * 
 * An instance accepts user input 
 *
 */
public class Client extends Node 
{
	int test = 1;
	
	int sequenceNumber = 0;
	static int DEFAULT_SRC_PORT;
	static final String DEFAULT_FINAL_DST_NODE = "localhost";	
	static final int DEFAULT_GATEWAY_DST_PORT = Gateway.DEFAULT_PORT;	
	boolean hasReceived = false;
	
	Terminal terminal;
	InetSocketAddress dstAddress;
	InetSocketAddress gateAddress;
	
	/**
	 * Constructor
	 * 	 
	 * Attempts to create socket at given port and create an InetSocketAddress for the destinations
	 */
	Client(Terminal terminal, String dstHost, int srcPort, int gatePort) 
	{
		try 
		{
			this.terminal= terminal;
			gateAddress = new InetSocketAddress(dstHost, gatePort);
			socket= new DatagramSocket(srcPort);
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
	public synchronized void onReceipt(DatagramPacket packet) 
	{
			hasReceived = true;
			StringContent content= new StringContent(packet);
			this.notify();
			String[] messageContent = content.toString().split("\n");
			if ((messageContent[0]).equals("OK"))
			{
				terminal.println(messageContent[0] + "\nNext Sequence Number : " + messageContent[1] + "\n");
			}
			else
			{
				terminal.println(messageContent[0] + "\nThat was unexpected.\n");
			}
			sequenceNumber = Integer.parseInt(messageContent[1]);
	} 

	
	/**
	 * Sender Method
	 * 
	 */
	public synchronized void start() throws Exception 
	{
		//test++;											//tests negative acknowledgement sending
		//terminal.println(Integer.toString(test));
		//if (this.test == 3)
		//{
		//	this.sequenceNumber = 100;
		//}	
		//terminal.println(Integer.toString(sequenceNumber));	//tests negative acknowledgement sending*/
		
		DatagramPacket packet= null;

		byte[] payload= null;
		byte[] header= null;
		byte[] buffer= null;
		
			payload= (terminal.readString("String to send: ") + "\n" + DEFAULT_SRC_PORT + "\n" + sequenceNumber).getBytes();

			header= new byte[PacketContent.HEADERLENGTH];

			buffer= new byte[header.length + payload.length];
			System.arraycopy(header, 0, buffer, 0, header.length);
			System.arraycopy(payload, 0, buffer, header.length, payload.length);
			
			hasReceived = false;
			terminal.println("Sending packet...");
			packet= new DatagramPacket(buffer, buffer.length, gateAddress);
			handleSend(packet);
	}
	
	private void handleSend(DatagramPacket packet)
	{
		try 
		{
			socket.send(packet);
			this.wait(2000);
			if (hasReceived == false)
			{
				terminal.println("Timeout. Resending packet");
				handleSend(packet);
			}
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		} 
		catch (InterruptedException e) 
		{
			e.printStackTrace();
		}
		
	}


	/**
	 * Test method
	 * 
	 * Sends a packet to a given address
	 */
	public static void main(String[] args) 
	{
		try 
		{					
			ServerSocket s = new ServerSocket(0);			//allows for multiple client creation
			DEFAULT_SRC_PORT = s.getLocalPort();			//allows for multiple client creation
			
			Terminal terminal= new Terminal("Client");	
			Client myClient = new Client(terminal, DEFAULT_FINAL_DST_NODE, DEFAULT_SRC_PORT, DEFAULT_GATEWAY_DST_PORT);
			while (true)
			{
				myClient.start();
			}
		} 
		catch(java.lang.Exception e) 
		{
			e.printStackTrace();
		}
	}
}




