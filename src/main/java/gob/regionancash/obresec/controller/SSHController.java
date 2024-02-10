package gob.regionancash.obresec.controller;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import com.jcraft.jsch.*;

@Path("/ssh")
public class SSHController {

    @ConfigProperty(name = "ssh.host")
    String host;

    @ConfigProperty(name = "ssh.user")
    String user;

    @ConfigProperty(name = "ssh.password")
    String password;

    @POST
    @Path("/execute")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.TEXT_PLAIN)
    public String executeSSHCommands(List<String> commands) {
        StringBuilder output = new StringBuilder();
        try {
            Session session = createSession();
            session.connect();
            for (String command : commands) {
                output.append(executeSingleSSHCommand(session, command)).append("\n\n");
            }
            session.disconnect();
        } catch (JSchException e) {
            e.printStackTrace();
            output.append("Error executing SSH commands: ").append(e.getMessage());
        }
        return output.toString();
    }

    private Session createSession() throws JSchException {
        JSch jsch = new JSch();
        Session session = jsch.getSession(user, host);
        session.setPassword(password);
        session.setConfig("StrictHostKeyChecking", "no");
        return session;
    }

    private String executeSingleSSHCommand(Session session, String command) {
        StringBuilder output = new StringBuilder();
        try {
            ChannelExec channel = (ChannelExec) session.openChannel("exec");
            channel.setCommand(command);
            channel.connect();    
            InputStream errorStream = channel.getErrStream();
            BufferedReader errorReader = new BufferedReader(new InputStreamReader(errorStream));
            String errorLine;
            while ((errorLine = errorReader.readLine()) != null) {
                output.append("ERROR: ").append(errorLine).append("\n");
            }
            byte[] tmp = new byte[1024];
            output.append("OUTPUT:" + command + ":");
            try {
                Thread.sleep(4000);
            } catch (Exception ee) {
            }
            while (true) {
                while (channel.getInputStream().available() > 0) {
                    int i = channel.getInputStream().read(tmp, 0, 1024);
                    if (i < 0)
                        break;
                    output.append(new String(tmp, 0, i));
                }
                if (channel.isClosed()) {
                    if (channel.getInputStream().available() > 0)
                        continue;
                    break;
                }
                try {
                    Thread.sleep(5000);
                } catch (Exception ee) {
                }
            }
            channel.disconnect();
        } catch (JSchException | IOException e) {
            e.printStackTrace();
            output.append("Error executing SSH command: ").append(e.getMessage());
        }
        return output.toString();
    }
}
