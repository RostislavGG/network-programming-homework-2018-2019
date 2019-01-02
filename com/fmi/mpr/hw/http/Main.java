package com.fmi.mpr.hw.http;

import java.io.*;
import java.net.*;

public class Main {

	private ServerSocket s;
	private String fileName;
	private boolean isActive;
	
	public Main() throws IOException {
		this.s = new ServerSocket(8888);
	}
	
	public void run() throws IOException {
		while (isActive) {
			try {
				Socket client = null;
				listen(client);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	public void start() throws IOException {
		if (!this.isActive) {
			this.isActive = true;
			run();
		}
	}
	
	private void listen(Socket client) throws IOException {
		try {
			client = s.accept();
			System.out.println(client.getInetAddress() + " is connected");
			processClient(client);
		} finally {
			if (client != null) {
				client.close();
			}
		}
	}
	
	private void processClient(Socket client) throws IOException {
		try (BufferedInputStream br = new BufferedInputStream(client.getInputStream());
			PrintStream ps = new PrintStream(client.getOutputStream(), true)) {
			String response = read(ps, br, client);
			write(ps, response);		
		}
	}
	
	private String read(PrintStream ps, BufferedInputStream b, Socket client) throws IOException {
		if (b != null) {
			StringBuilder req = new StringBuilder();
			byte[] buffer = new byte[1024];
			int bytesRead = 0;
			
			while ((bytesRead = b.read(buffer, 0, 1024)) > 0) {
				req.append(new String(buffer, 0, bytesRead));
				
				if (bytesRead < 1024) {
					break;
				}
			}
			return parseReq(ps, req.toString(), client);
		}
		return "Error";
	}
	
	private void write(PrintStream ps, String response) {
		if (ps != null) {
			ps.println("HTTP/1.0 200 OK");
			ps.println();
			ps.println("<!DOCTYPE html>\n" + 
					   "<html>\n" + 
					   "<head>\n" + 
					   "	<title></title>\n" + 
					   "</head>\n" + 
					   "<body>\n" + 
					   "<form action=\"/action_page.php\">\n" + 
					   "			  <input type=\"file\" name=\"pic\" accept=\"image/*\">\n" + 
					   "			  <input type=\"submit\">\n" + 
					   "			</form> " +
					   "</body>\n" + 
					   "</html>");
		}
	}
	
	private String parseReq(PrintStream ps, String req, Socket client) throws IOException {
		String[] lines = req.split("\n");
		String firstHeader = req.split("\n")[0];
		String type = firstHeader.split(" ")[0];
		String uri = firstHeader.split(" ")[1];
		this.fileName = uri.substring(1);
		
		String typeEx = uri.split("\\.")[1];
		
		if (type.equals("GET")) {
			return get(ps, typeEx);
		} else if (type.equals("POST")) {
			return post(ps, lines, client);
		}
		
		return null;
	}
	
	private String get(PrintStream ps, String extensionType) {
		ps.println("HTTP/1.1 200 OK");
		ps.println();
		
		if (extensionType.equals("txt")) {
			try {
				ps.println();
				sendText(ps);
			} catch (IOException e) {
				ps.println();
				ps.println("<!DOCTYPE html>\n" + "<html>\n" + 
						   "<head>\n" + "	<title></title>\n" + "</head>\n" + "<body>\n" + 
						   "			  Error! This file doesn't exist \n" + "</body>\n" + "</html>");
			}
		}
		else if (extensionType.equals("png") || extensionType.equals("jpg") || extensionType.equals("bmp")) {
			try {
				ps.println();
				sendPicture(ps);
			} catch (IOException e) {
				ps.println();
				ps.println("<!DOCTYPE html>\n" + "<html>\n" + 
						   "<head>\n" + "	<title></title>\n" + "</head>\n" + "<body>\n" + 
						   "			  Error! This file doesn't exist \n" + "</body>\n" + "</html>");
			}
		}
		
		else if (extensionType.equals("mp4") || extensionType.equals("avi")) {
			try {
				ps.println("Content-Type: video/mp4");
				ps.println();
				sendVideo(ps);
			} catch (IOException e) {
				ps.println();
				ps.println("<!DOCTYPE html>\n" + "<html>\n" + 
						   "<head>\n" + "	<title></title>\n" + "</head>\n" + "<body>\n" + 
						   "			  Error! This file doesn't exist \n" + "</body>\n" + "</html>");
			}
		}
		return null;
	}
	
	private void sendText(PrintStream ps) throws IOException {
		File f = new File(fileName);
		String path = f.getAbsolutePath();

		FileInputStream fis = new FileInputStream(path);

		int bytesRead = 0;
		byte[] buffer = new byte[8192];
 		
 		while ((bytesRead = fis.read(buffer, 0, 8192)) > 0) {
 			ps.write(buffer, 0, bytesRead);
 		}
 		
 		ps.flush();
 		System.out.println("Send text");
		fis.close();	
	}

	private void sendPicture(PrintStream ps) throws IOException {
		File f = new File(fileName);
		String path = f.getAbsolutePath();

		FileInputStream fis = new FileInputStream(path);


		int bytesRead = 0;
		byte[] buffer = new byte[8192];
 		
 		while ((bytesRead = fis.read(buffer, 0, 8192)) > 0) {
 			ps.write(buffer, 0, bytesRead);
 		}
 		
 		ps.flush();
 		System.out.println("Send picture");
		fis.close();	
	}
	
	private void sendVideo(PrintStream ps) throws IOException {
		File f = new File(fileName);
		String path = f.getAbsolutePath();

		FileInputStream fis = new FileInputStream(path);


		int bytesRead = 0;
		byte[] buffer = new byte[8192];
 		
 		while ((bytesRead = fis.read(buffer, 0, 8192)) > 0) {
 			ps.write(buffer, 0, bytesRead);
 		}
 		
 		ps.flush();
 		System.out.println("Send video");
		fis.close();	
	}
	
	private String post(PrintStream ps, String[] lines, Socket client) throws IOException {
		String header = lines[0];
		String url = header.split(" ")[1];
		
		if(url.length() != 1) {
			
			url = url.substring(1);
		}
		
		if(url.equals("upload.php")) {
			StringBuilder body = new StringBuilder();
			
			boolean readBody = false;
			for (String line : lines) {
				if (readBody) { 		
					body.append(line);
				}
				
				if (line.trim().isEmpty()) {	
					readBody = true;
				}				
			}		
			return parse(client, body.toString());
		}
		
		return null;
	}
	
	private String parse(Socket client, String body) throws IOException {
			if (body != null && !body.trim().isEmpty()) {
			
			String[] operands = body.split(";");
			fileName = operands[2].split("=")[1].split("\"")[1];
			
			String ex = fileName.split("\\.")[1];
			BufferedInputStream bis = new BufferedInputStream(client.getInputStream());
			String data = null;
			PrintStream ps = new PrintStream(client.getOutputStream(), true);
			
			if(ex.equals("jpg") || ex.equals("jpeg") || ex.equals("bmp") || ex.equals("mp4") || ex.equals("avi") || ex.contentEquals("txt")) {
				
				data = sendFile(bis, ps);
			}
			
			File file = new File(fileName);
			FileOutputStream is = new FileOutputStream(file.getAbsolutePath());
			is.write(data.getBytes());
	        is.close();
	        System.out.println("File sent!");
		}
		return null;
	}
	
	private String sendFile(BufferedInputStream bis, PrintStream ps) throws IOException {
		int bytes = 0;
		byte[] buffer = new byte[8192];
	
		while((bytes = bis.read(buffer, 0, 8192)) > 0) {
		
			ps.write(buffer, 0, bytes);
		}
		
		return ps.toString();
		
	}
	
	public static void main(String[] args) throws IOException {
		Main n = new Main();
		n.start();
	}
	
}