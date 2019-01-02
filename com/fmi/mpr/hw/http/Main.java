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
			String response = read(ps, br);
			write(ps, response);		
		}
	}
	
	private String read(PrintStream ps, BufferedInputStream b) throws IOException {
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
			return parseReq(ps, req.toString());
		}
		return "Error";
	}
	
	private void write(PrintStream ps, String response) {
		
	}
	
	private String parseReq(PrintStream ps, String req) throws IOException {
		String firstHeader = req.split("\n")[0];
		String type = firstHeader.split(" ")[0];
		String uri = firstHeader.split(" ")[1];
		this.fileName = uri.substring(1);
		
		String typeEx = uri.split("\\.")[1];
		
		if (type.equals("GET")) {
			return get(ps, typeEx);
		} else if (type.equals("POST")) {
			return post (ps, typeEx);
		}
		
		return null;
	}
	
	private String get(PrintStream ps, String extensionType) {
		return null;
	}
	
	private String post(PrintStream ps, String extensionType) {
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
	
	public static void main(String[] args) throws IOException {
		Main n = new Main();
		n.start();
	}
	
}