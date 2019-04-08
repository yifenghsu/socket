import java.net.*;
import java.io.*;


//server
class chatserver implements Runnable {
	private ServerSocket serverSocket;
	private int port;
	private boolean isFile =false;
	Thread  rec;
	
	public chatserver() throws IOException {
		serverSocket = new ServerSocket(9999);
		serverSocket.setSoTimeout(0);
		isFile =true;
	}
	public chatserver(int port) throws IOException {
		serverSocket = new ServerSocket(port);
		serverSocket.setSoTimeout(0);
	}
	public void received(Socket server) throws IOException{
		System.out.println("receive file...");
		
		InputStream ins = server.getInputStream();						//�nŪ����Ʀ�
																		//Ū�ɮצW
		byte[] bufIn = new byte[128];
		//System.out.println( new String(bufIn,0,ins.read(bufIn)) ); 	//(byte [],start,int len)
		FileOutputStream fos = new FileOutputStream("1" + new String(bufIn,0,ins.read(bufIn)));
																		//��X���ɮ�		
		byte[] buf = new byte[1024];
		int filelen=0; 
		while((filelen = ins.read(buf)) != -1) {	fos.write(buf,0,filelen);	}
		fos.close(); 
		ins.close(); 
		/*
		OutputStream out = server.getOutputStream();
		out.write("�W�Ǧ��\".getBytes()); //�নBytes�ǰe�L�h
		
		*/
		isFile = false;
		System.out.println("server finish");
	}
	public void run() {
      while(true) {
         try {
            System.out.println("Waiting for client on port " + serverSocket.getLocalPort() + "...");
            Socket server = serverSocket.accept();	//��ť�s�u
            //���쪺���
			if(isFile){
				received(server);
				server.close();
				serverSocket.close(); //�S���U�@���|�����}9999Port
				break;
			}
			DataInputStream in = new DataInputStream(server.getInputStream());
			String fort =in.readUTF();
			
			if( fort.equals("aaa")){
				//�s�����
				rec = new Thread(new chatserver());
				rec.start();
			}else{
				System.out.println(fort + "\t from " + server.getRemoteSocketAddress());
			}
			server.close();
            
         } catch (IOException e) {
            System.out.println("client stop connected!");
         }
      }
   }
   
}//server end

//client
public class chat extends Thread{
	private boolean isFile =false;
	private String serverName = "127.0.0.1";
	private int port;
	
	public chat(int p){
		port = p;
		isFile = true;
	}
	public chat(){
		port = isnum();
	}
	public int isnum(){
		System.out.print("connected to other server port:");
		while(true){
			try {
				BufferedReader b = new BufferedReader(new InputStreamReader(System.in));
				return Integer.parseInt(b.readLine());
			} catch (Exception e){
					System.out.println("connected to other server port:");
			}
		}
	}
	public void sendfile(Socket client)throws IOException{
		
		System.out.println("sendfile");
		
		//load file
		String filename = "what.jpg";
		FileInputStream filestream = new FileInputStream(filename);
		
		OutputStream outToServer = client.getOutputStream();	//send to server
		//�ǵ�server����Ƭy
		outToServer.write(filename.getBytes());					//��server���ɮצW
		byte[] buf = new byte[1024]; 							//1kB buffer
		int  filelen = 0;
		while((filelen = filestream.read(buf)) != -1) outToServer.write(buf,0,filelen);
		filestream.close();
		client.shutdownOutput(); 								//�����ǿ���SERVER���D�ǧ��ɮפF
		//�������A������T
		/*InputStream in = client.getInputStream();
		byte[] bufIn = new byte[1024];
		int num = in.read(bufIn);
		System.out.println(new String(bufIn,0,num));*/
		
		System.out.println("client finish sendfile");
		isFile =false;
	}
	public void inputMessage(Socket client)throws IOException{
		//�ǳƶǥhSERVER
		OutputStream outToServer = client.getOutputStream();
		DataOutputStream out = new DataOutputStream(outToServer);
		//�bSERVER�L�X
		
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		String st = br.readLine();
		if(st.equals("aaa") ){		
			// ���ɮ�
			//�����A�����D�n���ɮ�
			out.writeUTF(st);
			chat f = new chat(9999);
			f.start();
		}else{
			out.writeUTF(st);
		}
	}
	public void run(){
		while(true){
			try {
				Socket client = new Socket(serverName, port);
				//�ۤv�e���L�X		getRemoteSocketAddress�L�X/IP:PORT
				System.out.println("Just connected to " + client.getRemoteSocketAddress());
				//�ǥhSERVER
				if(isFile){
					sendfile(client);
					client.close();
					break;
				}else{ 
					inputMessage(client);
					client.close();
				}
			} catch (IOException e) {
				System.out.println("No server can be connected...");
			}
		}
	}

	public static void main(String [] args)throws IOException{
		//�T�wlocalhost������K����
		
		BufferedReader b = new BufferedReader(new InputStreamReader(System.in));
		System.out.print("my server port:");
		int sp = Integer.parseInt(b.readLine());
		
		chat c = new chat();			//client
			try {
				Thread t = new Thread(new chatserver(sp));		//server
				t.start();									//server
			} catch (IOException e) {
				e.printStackTrace();
			}
		c.start();											//client
	}
}