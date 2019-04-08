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
		
		InputStream ins = server.getInputStream();						//要讀的資料串
																		//讀檔案名
		byte[] bufIn = new byte[128];
		//System.out.println( new String(bufIn,0,ins.read(bufIn)) ); 	//(byte [],start,int len)
		FileOutputStream fos = new FileOutputStream("1" + new String(bufIn,0,ins.read(bufIn)));
																		//輸出的檔案		
		byte[] buf = new byte[1024];
		int filelen=0; 
		while((filelen = ins.read(buf)) != -1) {	fos.write(buf,0,filelen);	}
		fos.close(); 
		ins.close(); 
		/*
		OutputStream out = server.getOutputStream();
		out.write("上傳成功".getBytes()); //轉成Bytes傳送過去
		
		*/
		isFile = false;
		System.out.println("server finish");
	}
	public void run() {
      while(true) {
         try {
            System.out.println("Waiting for client on port " + serverSocket.getLocalPort() + "...");
            Socket server = serverSocket.accept();	//傾聽連線
            //收到的資料
			if(isFile){
				received(server);
				server.close();
				serverSocket.close(); //沒關下一次會打不開9999Port
				break;
			}
			DataInputStream in = new DataInputStream(server.getInputStream());
			String fort =in.readUTF();
			
			if( fort.equals("aaa")){
				//新執行緒
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
		//傳給server的資料流
		outToServer.write(filename.getBytes());					//跟server說檔案名
		byte[] buf = new byte[1024]; 							//1kB buffer
		int  filelen = 0;
		while((filelen = filestream.read(buf)) != -1) outToServer.write(buf,0,filelen);
		filestream.close();
		client.shutdownOutput(); 								//關閉傳輸讓SERVER知道傳完檔案了
		//接收伺服器的資訊
		/*InputStream in = client.getInputStream();
		byte[] bufIn = new byte[1024];
		int num = in.read(bufIn);
		System.out.println(new String(bufIn,0,num));*/
		
		System.out.println("client finish sendfile");
		isFile =false;
	}
	public void inputMessage(Socket client)throws IOException{
		//準備傳去SERVER
		OutputStream outToServer = client.getOutputStream();
		DataOutputStream out = new DataOutputStream(outToServer);
		//在SERVER印出
		
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		String st = br.readLine();
		if(st.equals("aaa") ){		
			// 傳檔案
			//讓伺服器知道要傳檔案
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
				//自己畫面印出		getRemoteSocketAddress印出/IP:PORT
				System.out.println("Just connected to " + client.getRemoteSocketAddress());
				//傳去SERVER
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
		//固定localhost本機方便測試
		
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