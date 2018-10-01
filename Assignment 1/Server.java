//Name : Owen Burke
//Student Number : 15316452

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.HashMap;

import tcdIO.Terminal;

public class Server extends Node 
{
	static final int DEFAULT_PORT = 50002;
	static int test = 0;
	private HashMap<Integer, DatagramPacket> packetMap;

	Terminal terminal;
	
	/*
	 * 
	 */
	Server(Terminal terminal, int port) 
	{
		try 
		{
			packetMap = new HashMap<Integer, DatagramPacket>();
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
		//if (test != 0)			//testing resending
		//{
			try 
			{
				StringContent content= new StringContent(packet);
				String[] messageContent = (content.toString()).split("\n");
				String message = messageContent[0];
				String clientPort = messageContent[1];
				int clientPortInt = Integer.parseInt(clientPort);
				int receivedSequenceNumber = Integer.parseInt(messageContent[2]);
				DatagramPacket mapPacket = packetMap.get(clientPortInt);
				if (mapPacket == null)
				{
					if (receivedSequenceNumber == 0)
					{
						packetMap.put(clientPortInt, packet);
						this.sendPosAck(packet, message, clientPort, receivedSequenceNumber);
					}
					else
					{
						this.sendNegAck(packet, message, clientPort, receivedSequenceNumber, 0);
					}
				}
				else
				{
					StringContent mapContent= new StringContent(mapPacket);
					String[] mapMessage = (mapContent.toString()).split("\n");
					int mapSequenceNumber = Integer.parseInt(mapMessage[2]);
					if (receivedSequenceNumber == mapSequenceNumber + 1)
					{
						packetMap.put(clientPortInt, packet);
						this.sendPosAck(packet, message, clientPort, receivedSequenceNumber);
					}
					else
					{
						this.sendNegAck(packet, message, clientPort, receivedSequenceNumber, mapSequenceNumber+1);
					}
				}
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		//}
		//else
		//{
		//	test++;
		//	System.out.println("test++");
		//}
	}
	
	private void sendPosAck(DatagramPacket packet, String message, String clientPort, int receivedSequenceNumber)
	{
		terminal.println(message + " : " + clientPort);
		DatagramPacket response;
		response = (new StringContent("OK" + "\n" + clientPort + "\n" + (receivedSequenceNumber+1))).toDatagramPacket();
		response.setSocketAddress(packet.getSocketAddress());
		terminal.println("Sending positive acknowledgement... \nReceived Sequence Number : " + receivedSequenceNumber +"\n");
		try 
		{
			socket.send(response);
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		}
	}
	
	private void sendNegAck(DatagramPacket packet, String message, String clientPort, int receivedSequenceNumber, int expectedSequenceNumber)
	{
		DatagramPacket response;
		response = (new StringContent("Negative" + "\n" + clientPort + "\n" + expectedSequenceNumber)).toDatagramPacket();
		response.setSocketAddress(packet.getSocketAddress());
		terminal.println("Sending negative acknowledgement... \nReceived Sequence Number : " + receivedSequenceNumber +"\n");
		try 
		{
			socket.send(response);
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		}
	}

	
	public synchronized void start() throws Exception 
	{
		terminal.println("Server waiting for contact");
		this.wait();
	}
	
	/*
	 * 
	 */
	public static void main(String[] args) 
	{
		try 
		{					
			Terminal terminal= new Terminal("Server");
			(new Server(terminal, DEFAULT_PORT)).start();
		} 
		catch(java.lang.Exception e) 
		{
			e.printStackTrace();
		}
	}
}



