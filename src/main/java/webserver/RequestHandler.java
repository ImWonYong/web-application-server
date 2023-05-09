package webserver;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.util.Map;

import model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.HttpRequestUtils;

public class RequestHandler extends Thread {
    private static final Logger log = LoggerFactory.getLogger(RequestHandler.class);

    private Socket connection;

    public RequestHandler(Socket connectionSocket) {
        this.connection = connectionSocket;
    }

    public void run() {
        log.debug("New Client Connect! Connected IP : {}, Port : {}", connection.getInetAddress(),
                connection.getPort());

        try (InputStream in = connection.getInputStream(); OutputStream out = connection.getOutputStream()) {

            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            String input = br.readLine();
            log.debug(input);

            String[] tokens = input.split(" ");
            String method = tokens[0];
            String url = tokens[1];
            byte[] body = new byte[0];


            if (url.equals("/") || url.equals("/index.html")) {
                body = Files.readAllBytes(new File("./webapp/index.html" ).toPath());
            }
            else if (url.startsWith("/user")) {
                int index = url.indexOf('?');
                String requestPath = index == -1 ? url : url.substring(0, index);
                String params = index == -1 ? null : url.substring(index + 1);
                if (requestPath.equals("/user/form.html")) {
                    body = Files.readAllBytes(new File("./webapp/user/form.html").toPath());
                }
                else if (requestPath.equals("/user/create")) {
                    Map<String, String> query = HttpRequestUtils.parseQueryString(params);
                    User user = new User(query.get("userId"),
                            query.get("password"),
                            query.get("name"),
                            query.get("email"));
                    log.debug("User : {}", user);
                }

            }

            DataOutputStream dos = new DataOutputStream(out);
            response200Header(dos, body.length);
            responseBody(dos, body);
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void response200Header(DataOutputStream dos, int lengthOfBodyContent) {
        try {
            dos.writeBytes("HTTP/1.1 200 OK \r\n");
            dos.writeBytes("Content-Type: text/html;charset=utf-8\r\n");
            dos.writeBytes("Content-Length: " + lengthOfBodyContent + "\r\n");
            dos.writeBytes("\r\n");
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void responseBody(DataOutputStream dos, byte[] body) {
        try {
            dos.write(body, 0, body.length);
            dos.flush();
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }
}
