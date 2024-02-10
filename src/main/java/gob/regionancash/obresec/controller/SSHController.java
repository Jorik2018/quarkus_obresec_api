package gob.regionancash.obresec.controller;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
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
            ByteArrayOutputStream responseStream = new ByteArrayOutputStream();
            channel.setOutputStream(responseStream);
            channel.connect();
            InputStream errorStream = channel.getErrStream();
            BufferedReader errorReader = new BufferedReader(new InputStreamReader(errorStream));
            String errorLine;
            while ((errorLine = errorReader.readLine()) != null) {
                output.append("ERROR: ").append(errorLine).append("\n");
            }
            while (channel.isConnected()) {
                try {
                    Thread.sleep(500);
                } catch (Exception ee) {
                    output.append(ee);
                }
            }
            String responseString = new String(responseStream.toByteArray());
            output.append(responseString);
            channel.disconnect();
        } catch (JSchException | IOException e) {
            e.printStackTrace();
            output.append("Error executing SSH command: ").append(e.getMessage());
        }
        return output.toString();
    }
}
