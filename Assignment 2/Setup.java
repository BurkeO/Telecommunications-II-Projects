
import java.net.SocketException;

//Name : Owen Burke
//Student Number : 15316452

public class Setup 
{
	
	public static void main(String[] args) 
	{
		try 
		{
			Controller controller = new Controller();
			Client client1 = new Client("Client1", 40000, 10001, 50000);
			Client client2 = new Client("Client2", 50000, 30002, 40000);
			Router router1 = new Router("Router1", 10000, new int[]{5000 /*controller port*/, 40000, 20001});
			Router router2 = new Router("Router2", 20000, new int[]{5000 /*controller port*/, 10002, 30001});
			Router router3 = new Router("Router3", 30000, new int[]{5000 /*controller port*/, 20002, 50000});
		}
		catch (Exception e) 
		{
			e.printStackTrace();
		}

	}

}