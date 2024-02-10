package gob.regionancash.obresec.controller;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
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

    @GET
    @Path("/execute")
    @Produces(MediaType.TEXT_PLAIN)
    public String executeSSHCommand(@QueryParam("command") String command) {
        JSch jsch = new JSch();
        try {
            Session session = jsch.getSession(user, host);
            session.setPassword(password);
            session.setConfig("StrictHostKeyChecking", "no");
            session.connect();

            ChannelExec channel = (ChannelExec) session.openChannel("exec");
            channel.setCommand(command);

            channel.connect();

            StringBuilder output = new StringBuilder();
            byte[] tmp = new byte[1024];
            output.append("OUTPUT:");
            while (true) {
                while (channel.getInputStream().available() > 0) {
                    int i = channel.getInputStream().read(tmp, 0, 1024);
                    if (i < 0) break;
                    output.append(new String(tmp, 0, i));
                }
                if (channel.isClosed()) {
                    if (channel.getInputStream().available() > 0) continue;
                    break;
                }
                try {
                    Thread.sleep(1000);
                } catch (Exception ee) {
                }
            }
            channel.disconnect();
            session.disconnect();
            return output.toString();
        } catch (JSchException | java.io.IOException e) {
            e.printStackTrace();
            return "Error executing SSH command: " + e.getMessage();
        }
    }
}
