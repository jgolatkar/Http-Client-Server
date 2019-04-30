import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

public class HttpClient {
	public static void main(String[] args) {
		Socket socket = null;
		try {
			String hostname = args[0];
			int port = Integer.parseInt(args[1]);
			String httpMethod = args[2];
			String file = args.length > 3 ? args[3] : "";

			if (httpMethod.equalsIgnoreCase("GET")) {
				sendGetRequest(hostname, port, file, socket);
			} else if (httpMethod.equalsIgnoreCase("PUT")) {
				sendPutRequest(hostname, port, file, socket);
			} else {
				sendOtherRequest(httpMethod, hostname, port, file, socket);
			}

		} catch (UnknownHostException ue) {
			System.out.println("SERVER ERROR: " + ue.getMessage());
		} catch (IOException ioe) {
			System.out.println("ERROR: " + ioe.getMessage());
		} finally {
			if (socket != null && !socket.isClosed()) {
				try {
					socket.close();
				} catch (IOException e) {
					System.out.println("I/O ERROR: " + e.getMessage());
				}
			}
		}
	}

	// send other types of method (only header is sent)
	private static void sendOtherRequest(String httpMethod, String hostname, int port, String file, Socket socket)
			throws UnknownHostException, IOException {
		socket = new Socket(hostname, port);
		String response = null;
		System.out.println("Connected to host: " + hostname + " on port " + port);
		PrintWriter requestWriter = new PrintWriter(socket.getOutputStream(), true);
		BufferedReader serverResponse = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		requestWriter.print(httpMethod + " /" + file + " HTTP/1.1\r\n");
		requestWriter.print("Host: " + hostname + "\r\n");
		requestWriter.print("Accept-Language: en-us\r\n");
		requestWriter.print("Connection: close\r\n");
		requestWriter.print("Content-Type: text/html\r\n");
		requestWriter.print("\r\n");
		System.out.println(httpMethod + " Request Sent\n");
		requestWriter.flush();
		while ((response = serverResponse.readLine()) != null) {
			System.out.println(response);
		}
		requestWriter.close();
		serverResponse.close();
		socket.close();

	}

	private static void sendPutRequest(String hostname, int port, String file, Socket socket)
			throws UnknownHostException, IOException {
		socket = new Socket(hostname, port);
		String response = null;
		String fileData = null;

		fileData = readLocalFile(file);
		System.out.println("Connected to host: " + hostname + " on port " + port);
		PrintWriter requestWriter = new PrintWriter(socket.getOutputStream(), true);
		BufferedReader serverResponse = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		requestWriter.print("PUT /" + file + " HTTP/1.1\r\n");
		requestWriter.print("Host: " + hostname + "\r\n");
		requestWriter.print("Accept-Language: en-us\r\n");
		requestWriter.print("Connection: keep-alive\r\n");
		requestWriter.print("Content-Type: text/html\r\n");
		requestWriter.print("\r\n");
		System.out.println("PUT Request Sent\n");
		requestWriter.println(fileData);

		requestWriter.flush();
		while ((response = serverResponse.readLine()) != null) {
			System.out.println(response);
		}
		requestWriter.close();
		serverResponse.close();
		socket.close();
	}

	private static String readLocalFile(String file) {
		File f = null;
		BufferedReader fileReader = null;
		StringBuilder content = null;
		try {
			f = new File(file);
			fileReader = new BufferedReader(new FileReader(f));
			content = new StringBuilder();
			String line = null;
			while ((line = fileReader.readLine()) != null) {
				content.append(line);
				content.append(System.lineSeparator());
			}
		} catch (FileNotFoundException e) {
			System.err.println("No file found at " + f.getAbsolutePath());
		} catch (IOException e) {
			System.err.println("Error reading file " + f.getAbsolutePath());
		} finally {
			try {
				if (fileReader != null)
					fileReader.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return content.toString();
	}

	private static void sendGetRequest(String hostname, int port, String file, Socket socket)
			throws UnknownHostException, IOException {
		if (!file.equals("")) {
			socket = new Socket(hostname, port);
			String response = null;
			System.out.println("Connected to host: " + hostname + " on port " + port);
			PrintWriter requestWriter = new PrintWriter(socket.getOutputStream(), true);
			BufferedReader serverResponse = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			requestWriter.println("GET /" + file + " HTTP/1.1");
			requestWriter.println("Host: " + hostname);
			requestWriter.println("Connection: close");
			requestWriter.println();
			requestWriter.flush();
			System.out.println("GET Request Sent\n");
			while ((response = serverResponse.readLine()) != null) {
				System.out.println(response);
			}
			requestWriter.close();
			serverResponse.close();
			socket.close();
		} else {
			System.err.println("GET method parameter required");
		}

	}
}
