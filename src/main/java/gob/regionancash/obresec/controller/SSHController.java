package gob.regionancash.obresec.controller;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.io.*;
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
        output.append(">"+command+":").append("\n");
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

    @GET
    @Path("/download")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response downloadFile(@QueryParam("remotePath") String remotePath) {
        try {
            Session session = createSession();
            session.connect();

            ChannelSftp channelSftp = (ChannelSftp) session.openChannel("sftp");
            channelSftp.connect();
            File remoteFile = new File(remotePath);
            File localFile = new  File(remoteFile.getName());
            // Download file from remotePath to localPath
            channelSftp.get(remotePath, localFile.getName());

            channelSftp.disconnect();
            session.disconnect();

            // Return success response with the downloaded file
            FileInputStream fileInputStream = new FileInputStream(localFile);
            Response.ResponseBuilder responseBuilder = Response.ok(fileInputStream);
            responseBuilder.header("Content-Disposition", "attachment; filename=\"" + localFile.getName() + "\"");

            return responseBuilder.build();
        } catch (JSchException | SftpException | FileNotFoundException e) {
            e.printStackTrace();
            // Return error response
            return Response.serverError().entity("Error downloading file: " + e.getMessage()).build();
        }
    }

}
