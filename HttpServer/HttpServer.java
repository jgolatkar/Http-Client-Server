
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;

public class HttpServer implements Runnable {
	static final String DEFAULT_FILE = "index.html";
	static final String FILE_NOT_FOUND = "file_not_found.html";
	static final File WEB_ROOT = new File(".");
	static final String NO_METHOD = "no_method.html";
	private String filePath = null;
	// client socket
	private Socket conn = null;

	public HttpServer(Socket conn) {
		this.conn = conn;
	}

	public static void main(String[] args) {

		ServerSocket serverSocket;
		HttpServer server;
		final int PORT = Integer.parseInt(args[0]);
		try {
			serverSocket = new ServerSocket(PORT);
			System.out.println("Listening to port: " + PORT + "..");

			Runtime.getRuntime().addShutdownHook(new Thread() {
				public void run() {
					try {
						serverSocket.close();
						System.out.println("The server is shut down!");
					} catch (IOException e) {
						System.err.println(e.getMessage());
					}
				}
			});

			while (true) {
				server = new HttpServer(serverSocket.accept());
				System.out.println("\nConnection opened on " + new Date());

				Thread serverThread = new Thread(server);
				serverThread.start();
			}

		} catch (IOException e) {
			System.err.println(e.getMessage());
		}

	}

	@Override
	public void run() {

		try {
			BufferedReader request = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			String requestHeader = "";
			String line = ".";
			while (!line.equals("")) {
				line = request.readLine();
				System.out.println(line);
				requestHeader += line + "\n";
			}
			filePath = requestHeader.split("\n")[0].split(" ")[1];
			if (!filePath.equals("/")) {
				filePath = filePath.split("/")[1];
			}
			String httpMethod = requestHeader.split("\n")[0].split(" ")[0];
			if (filePath.endsWith("/"))
				filePath += DEFAULT_FILE;

			switch (httpMethod) {
			case "GET":
				getRequest(conn, filePath);
				break;
			case "PUT":
				putRequest(conn, filePath, request);
				break;
			default:
				getErrorFile(conn);
				System.err.println("Method Not Supported.");

			}

		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	// returns error html file in case http method is not supported
	private void getErrorFile(Socket conn) throws IOException {
		PrintWriter out = null;
		BufferedOutputStream bOut = null;
		try {
			out = new PrintWriter(conn.getOutputStream());
			File file = new File(WEB_ROOT, NO_METHOD);
			int file_len = (int) file.length();
			String contentType = "text/html";
			byte[] data = readFileData(file, file_len);

			out.println("HTTP/1.1 501 Not Implemented");
			out.println("Content-Type:" + contentType);
			out.println();

			bOut = new BufferedOutputStream(conn.getOutputStream());
			bOut.write(data);

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			out.flush();
			bOut.flush();
			bOut.close();
			conn.close();
		}

	}

	private byte[] readFileData(File file, int file_len) throws FileNotFoundException {
		FileInputStream fileIn = null;
		byte[] fileContent = new byte[file_len];
		try {
			fileIn = new FileInputStream(file);
			try {
				fileIn.read(fileContent);
			} catch (IOException e) {
				e.printStackTrace();
			}
		} finally {
			if (fileIn != null)
				try {
					fileIn.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
		}
		return fileContent;
	}

	private void putRequest(Socket conn, String fileRequested, BufferedReader request) throws IOException {
		File file = new File(WEB_ROOT, fileRequested);
		String data = "";
		String line = "";
		BufferedWriter out = null;
		try {
			line = request.readLine().toString();
			data += line;
			while (line.contains("<html>") || line.contains("<!doctype html>")) {
				while (!(line = request.readLine()).contains("</html>")) {
					System.out.println(line);
					data += line;
				}
			}
			data += "</html>";
			if (!data.isEmpty()) {
				writeDataToFile(data, file);
			}
			out = new BufferedWriter(new OutputStreamWriter(conn.getOutputStream()));
			out.write("HTTP/1.1 200 OK File Created\n");
			out.write("Date: " + new Date() + "\n");
			out.write("Content-Type: tex/html\n");
			out.write("Content-Length:" + file.length() + "\n");
			out.flush();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (out != null) {
					out.flush();
					out.close();
				}
				conn.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private void writeDataToFile(String data, File file) {
		BufferedWriter writer = null;
		try {
			writer = new BufferedWriter(new FileWriter(file));
			writer.write(data);
			writer.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void getRequest(Socket conn, String fileRequested) {
		PrintWriter out = null;
		BufferedOutputStream bOut = null;
		try {
			out = new PrintWriter(conn.getOutputStream());
			File file = new File(WEB_ROOT, fileRequested);

			int file_len = (int) file.length();
			String contentType = "text/html";
			byte[] data = readFileData(file, file_len);

			out.println("HTTP/1.1 200 OK");
			out.println("Date: " + new Date());
			out.println("Content-Type:" + contentType);
			out.println("Content-Length:" + file_len);
			out.println();

			bOut = new BufferedOutputStream(conn.getOutputStream());
			bOut.write(data);

		} catch (FileNotFoundException e) {
			try {
				fileNotFound(conn);
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			out.flush();
			try {
				if (bOut != null) {
					bOut.flush();
					bOut.close();
				}
				conn.close();
			} catch (IOException e) {
				e.printStackTrace();
			}

		}
	}

	private void fileNotFound(Socket conn) throws IOException {
		PrintWriter out = null;
		try {
			out = new PrintWriter(conn.getOutputStream());
			out.println("HTTP/1.1 404 File Not Found");
			out.println("Date: " + new Date());
			out.println();
		} finally {
			out.flush();
			out.close();
		}

	}

}
