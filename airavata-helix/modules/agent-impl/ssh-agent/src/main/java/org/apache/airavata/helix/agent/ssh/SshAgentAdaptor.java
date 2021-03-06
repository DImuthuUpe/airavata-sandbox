package org.apache.airavata.helix.agent.ssh;

import com.jcraft.jsch.*;
import org.apache.airavata.agents.api.AgentAdaptor;
import org.apache.airavata.agents.api.AgentException;
import org.apache.airavata.agents.api.CommandOutput;

import java.io.*;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static jdk.nashorn.internal.runtime.regexp.joni.Config.log;

/**
 * TODO: Class level comments please
 *
 * @author dimuthu
 * @since 1.0.0-SNAPSHOT
 */
public class SshAgentAdaptor implements AgentAdaptor {

    private Session session = null;

    public void init(Object adaptorParams) throws AgentException {

        if (adaptorParams instanceof SshAdaptorParams) {
            SshAdaptorParams params = SshAdaptorParams.class.cast(adaptorParams);
            JSch jSch = new JSch();
            try {

                if (params.getPassword() != null) {
                    this.session = jSch.getSession(params.getUserName(), params.getHostName(), params.getPort());
                    session.setPassword(params.getPassword());
                    session.setUserInfo(new SftpUserInfo(params.getPassword()));
                } else {
                    jSch.addIdentity(UUID.randomUUID().toString(), params.getPrivateKey(), params.getPublicKey(),
                            params.getPassphrase().getBytes());
                    this.session = jSch.getSession(params.getUserName(), params.getHostName(),
                            params.getPort());
                    session.setUserInfo(new DefaultUserInfo(params.getUserName(), null, params.getPassphrase()));
                }

                if (params.isStrictHostKeyChecking()) {
                    jSch.setKnownHosts(params.getKnownHostsFilePath());
                } else {
                    session.setConfig("StrictHostKeyChecking", "no");
                }
                session.connect(); // 0 connection timeout

            } catch (JSchException e) {
                throw new AgentException("Could not create ssh session for host " + params.getHostName(), e);
            }
        } else {
            throw new AgentException("Unknown parameter type to ssh initialize agent adaptor. Required SshAdaptorParams type");
        }

    }

    public CommandOutput executeCommand(String command, String workingDirectory) throws AgentException {
        StandardOutReader commandOutput = new StandardOutReader();
        try {
            ChannelExec channelExec = ((ChannelExec) session.openChannel("exec"));
            channelExec.setCommand(command);
            channelExec.setInputStream(null);
            channelExec.setErrStream(commandOutput.getStandardError());
            channelExec.connect();
            commandOutput.onOutput(channelExec);
            return commandOutput;
        } catch (JSchException e) {
            throw new AgentException(e);
        }
    }

    public void createDirectory(String path) throws AgentException {
        try {
            String command = "mkdir -p " + path;
            Channel channel = session.openChannel("exec");
            StandardOutReader stdOutReader = new StandardOutReader();

            ((ChannelExec) channel).setCommand(command);

            ((ChannelExec) channel).setErrStream(stdOutReader.getStandardError());
            try {
                channel.connect();
            } catch (JSchException e) {

                channel.disconnect();
                System.out.println("Unable to retrieve command output. Command - " + command +
                        " on server - " + session.getHost() + ":" + session.getPort() +
                        " connecting user name - "
                        + session.getUserName());
                throw new AgentException(e);
            }
            stdOutReader.onOutput(channel);
            if (stdOutReader.getStdErrorString().contains("mkdir:")) {
                throw new AgentException(stdOutReader.getStdErrorString());
            }

            channel.disconnect();
        } catch (JSchException e) {
            throw new AgentException(e);
        }
    }

    public void copyFile(String localFile, String remoteFile) throws AgentException {
        FileInputStream fis = null;
        String prefix = null;
        if (new File(localFile).isDirectory()) {
            prefix = localFile + File.separator;
        }
        boolean ptimestamp = true;

        try {
            // exec 'scp -t rfile' remotely
            String command = "scp " + (ptimestamp ? "-p" : "") + " -t " + remoteFile;
            Channel channel = session.openChannel("exec");

            StandardOutReader stdOutReader = new StandardOutReader();
            ((ChannelExec) channel).setErrStream(stdOutReader.getStandardError());
            ((ChannelExec) channel).setCommand(command);

            // get I/O streams for remote scp
            OutputStream out = channel.getOutputStream();
            InputStream in = channel.getInputStream();

            channel.connect();

            if (checkAck(in) != 0) {
                String error = "Error Reading input Stream";
                //log.error(error);
                throw new AgentException(error);
            }

            File _lfile = new File(localFile);

            if (ptimestamp) {
                command = "T" + (_lfile.lastModified() / 1000) + " 0";
                // The access time should be sent here,
                // but it is not accessible with JavaAPI ;-<
                command += (" " + (_lfile.lastModified() / 1000) + " 0\n");
                out.write(command.getBytes());
                out.flush();
                if (checkAck(in) != 0) {
                    String error = "Error Reading input Stream";
                    throw new AgentException(error);
                }
            }

            // send "C0644 filesize filename", where filename should not include '/'
            long filesize = _lfile.length();
            command = "C0644 " + filesize + " ";
            if (localFile.lastIndexOf('/') > 0) {
                command += localFile.substring(localFile.lastIndexOf('/') + 1);
            } else {
                command += localFile;
            }
            command += "\n";
            out.write(command.getBytes());
            out.flush();
            if (checkAck(in) != 0) {
                String error = "Error Reading input Stream";
                //log.error(error);
                throw new AgentException(error);
            }

            // send a content of localFile
            fis = new FileInputStream(localFile);
            byte[] buf = new byte[1024];
            while (true) {
                int len = fis.read(buf, 0, buf.length);
                if (len <= 0) break;
                out.write(buf, 0, len); //out.flush();
            }
            fis.close();
            fis = null;
            // send '\0'
            buf[0] = 0;
            out.write(buf, 0, 1);
            out.flush();
            if (checkAck(in) != 0) {
                String error = "Error Reading input Stream";
                //log.error(error);
                throw new AgentException(error);
            }
            out.close();
            stdOutReader.onOutput(channel);


            channel.disconnect();
            if (stdOutReader.getStdErrorString().contains("scp:")) {
                throw new AgentException(stdOutReader.getStdErrorString());
            }
            //since remote file is always a file  we just return the file
            //return remoteFile;
        } catch (JSchException e) {
            e.printStackTrace();
            throw new AgentException(e);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            throw new AgentException(e);
        } catch (IOException e) {
            e.printStackTrace();
            throw new AgentException(e);
        }
    }

    @Override
    public List<String> listDirectory(String path) throws AgentException {

        try {
            String command = "ls " + path;
            Channel channel = session.openChannel("exec");
            StandardOutReader stdOutReader = new StandardOutReader();

            ((ChannelExec) channel).setCommand(command);


            ((ChannelExec) channel).setErrStream(stdOutReader.getStandardError());
            try {
                channel.connect();
            } catch (JSchException e) {

                channel.disconnect();
//            session.disconnect();

                throw new AgentException("Unable to retrieve command output. Command - " + command +
                        " on server - " + session.getHost() + ":" + session.getPort() +
                        " connecting user name - "
                        + session.getUserName(), e);
            }
            stdOutReader.onOutput(channel);
            stdOutReader.getStdOutputString();
            if (stdOutReader.getStdErrorString().contains("ls:")) {
                throw new AgentException(stdOutReader.getStdErrorString());
            }
            channel.disconnect();
            return Arrays.asList(stdOutReader.getStdOutputString().split("\n"));

        } catch (JSchException e) {
            throw new AgentException(e);
        }
    }

    private static class DefaultUserInfo implements UserInfo, UIKeyboardInteractive {

        private String userName;
        private String password;
        private String passphrase;

        public DefaultUserInfo(String userName, String password, String passphrase) {
            this.userName = userName;
            this.password = password;
            this.passphrase = passphrase;
        }

        @Override
        public String getPassphrase() {
            return passphrase;
        }

        @Override
        public String getPassword() {
            return password;
        }

        @Override
        public boolean promptPassword(String s) {
            return true;
        }

        @Override
        public boolean promptPassphrase(String s) {
            return false;
        }

        @Override
        public boolean promptYesNo(String s) {
            return false;
        }

        @Override
        public void showMessage(String s) {

        }

        @Override
        public String[] promptKeyboardInteractive(String destination, String name, String instruction, String[] prompt, boolean[] echo) {
            return new String[0];
        }
    }

    class SftpUserInfo implements UserInfo {

        String password = null;

        public SftpUserInfo(String password) {
            this.password = password;
        }

        @Override
        public String getPassphrase() {
            return null;
        }

        @Override
        public String getPassword() {
            return password;
        }

        public void setPassword(String passwd) {
            password = passwd;
        }

        @Override
        public boolean promptPassphrase(String message) {
            return false;
        }

        @Override
        public boolean promptPassword(String message) {
            return false;
        }

        @Override
        public boolean promptYesNo(String message) {
            return true;
        }

        @Override
        public void showMessage(String message) {
        }
    }

    static int checkAck(InputStream in) throws IOException {
        int b = in.read();
        if (b == 0) return b;
        if (b == -1) return b;

        if (b == 1 || b == 2) {
            StringBuffer sb = new StringBuffer();
            int c;
            do {
                c = in.read();
                sb.append((char) c);
            }
            while (c != '\n');
            //FIXME: Redundant
            if (b == 1) { // error
                System.out.print(sb.toString());
            }
            if (b == 2) { // fatal error
                System.out.print(sb.toString());
            }
            //log.warn(sb.toString());
        }
        return b;
    }
}
