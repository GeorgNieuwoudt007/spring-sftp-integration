package nl.georg.sftp.config;

import com.jcraft.jsch.ChannelSftp;
import lombok.extern.slf4j.Slf4j;
import nl.georg.sftp.handler.InboundMessageHandler;
import nl.georg.sftp.handler.OutboundFileNameGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.expression.common.LiteralExpression;
import org.springframework.integration.annotation.*;
import org.springframework.integration.core.MessageSource;
import org.springframework.integration.file.filters.AcceptOnceFileListFilter;
import org.springframework.integration.file.remote.session.CachingSessionFactory;
import org.springframework.integration.file.remote.session.SessionFactory;
import org.springframework.integration.sftp.filters.SftpSimplePatternFileListFilter;
import org.springframework.integration.sftp.inbound.SftpInboundFileSynchronizer;
import org.springframework.integration.sftp.inbound.SftpInboundFileSynchronizingMessageSource;
import org.springframework.integration.sftp.outbound.SftpMessageHandler;
import org.springframework.integration.sftp.session.DefaultSftpSessionFactory;
import org.springframework.messaging.MessageHandler;

import java.io.File;

@Configuration
@Slf4j
public class SftpConfiguration {

    // For Uploading the file on remote server, we need to create a Messaging
    // Gateway
    @Autowired
    private OutboundGateway outboundGateway;

    // Properties of Remote Host
    @Value("${configuration.sftp.inbound.host}")
    private String inboundHost;

    @Value("${configuration.sftp.inbound.port}")
    private int inboundPort;

    @Value("${configuration.sftp.inbound.username}")
    private String inboundUsername;

    // Further Addition on private key and private key paraphrase in case needed
    /*
     * @Value("${sftp.privateKey:#{null}}") private Resource sftpPrivateKey;
     *
     * @Value("${sftp.privateKeyPassphrase:}") private String
     * sftpPrivateKeyPassphrase;
     */

    @Value("${configuration.sftp.inbound.password}")
    private String inboundPassword;
    @Value("${configuration.sftp.file.sources.local}")
    private String inboundDirectory;
    // Local Directory for Download
    @Value("${configuration.sftp.file.sources.local}")
    private String inboundLocalDirectory;
    @Value("${configuration.sftp.file.filter}")
    private String inboundDownloadFilter;

    // Properties of Remote Destination
    @Value("${configuration.sftp.outbound.host}")
    private String outboundHost;
    @Value("${configuration.sftp.outbound.port}")
    private int outboundPort;
    @Value("${configuration.sftp.outbound.username}")
    private String outboundUsername;
    @Value("${configuration.sftp.outbound.password}")
    private String outboundPassword;
    @Value("${configuration.sftp.file.sources.remote}")
    private String remoteDirectory;

    /**
     * The SftpSessionFactory creates the sftp sessions.
     * This is where you define the host , user and key information for your sftp server.
     */
    // Creating session for Remote Destination SFTP server Folder
    @Bean
    public SessionFactory<ChannelSftp.LsEntry> outboundSftpSessionFactory() {
        var factory = new DefaultSftpSessionFactory(true);
        factory.setHost(outboundHost);
        factory.setPort(outboundPort);
        factory.setUser(outboundUsername);
        factory.setPassword(outboundPassword);
        factory.setAllowUnknownKeys(true);

        return new CachingSessionFactory<>(factory);
    }

    // Creating session for Source SFTP server Folder
    @Bean
    public SessionFactory<ChannelSftp.LsEntry> inboundSftpSessionFactory() {
        var factory = new DefaultSftpSessionFactory(true);
        factory.setHost(inboundHost);
        factory.setPort(inboundPort);
        factory.setUser(inboundUsername);
        factory.setPassword(inboundPassword);
        factory.setAllowUnknownKeys(true);

        return new CachingSessionFactory<>(factory);
    }

    /**
     * The SftpInboundFileSynchronizer uses the session factory that we defined above.
     * Here we set information about the remote directory to fetch files from.
     * We could also set filters here to control which files get downloaded
     */
    @Bean
    public SftpInboundFileSynchronizer inboundSftpFileSynchronizer() {
        var fileSynchronizer = new SftpInboundFileSynchronizer(inboundSftpSessionFactory());
        fileSynchronizer.setDeleteRemoteFiles(true);
        fileSynchronizer.setRemoteDirectory(inboundDirectory);
        fileSynchronizer.setFilter(new SftpSimplePatternFileListFilter(inboundDownloadFilter));

        return fileSynchronizer;
    }

    /**
     * The Message source bean uses the @InboundChannelAdapter annotation.
     * This message source connects the synchronizer we defined above to a message queue (sftpChannel).
     * The adapter will take files from the sftp server and place them in the message queue as messages
     */
    @Bean
    @InboundChannelAdapter(
            channel = "sftpChannel",
            poller = @Poller(
                    fixedDelay = "30000"
            )
    )
    public MessageSource<File> inboundSftpMessageSource() {
        var source = new SftpInboundFileSynchronizingMessageSource(inboundSftpFileSynchronizer());
        source.setLocalDirectory(new File(inboundLocalDirectory));
        source.setAutoCreateLocalDirectory(true);
        source.setLocalFilter(new AcceptOnceFileListFilter<>());

        return source;
    }

    /**
     * The message consumer is where you get to process the files that are downloaded.
     * Here we have sent it using message gateway through the upload method to remote SFTP folder
     */
    @Bean
    @ServiceActivator(
            inputChannel = "sftpChannel"
    )
    public MessageHandler inboundHandler() {
        return new InboundMessageHandler();
    }

    /**
     * Message handler for Outbound Adapter
     * so that we can send it to remote destination directory on SFTP server
     */
    @Bean
    @ServiceActivator(
            inputChannel = "toSftpChannel"
    )
    public MessageHandler outboundHandler() {
        var handler = new SftpMessageHandler(outboundSftpSessionFactory());
        handler.setRemoteDirectoryExpression(new LiteralExpression(remoteDirectory));
        handler.setFileNameGenerator(new OutboundFileNameGenerator());

        return handler;
    }

    /**
     * Gateway
     */
    @MessagingGateway
    public interface OutboundGateway {

        @Gateway(
                requestChannel = "toSftpChannel"
        )
        void send(File file);
    }
}
