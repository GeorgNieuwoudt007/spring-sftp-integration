//package nl.georg.sftp.config;
//
//import com.jcraft.jsch.ChannelSftp;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.integration.annotation.InboundChannelAdapter;
//import org.springframework.integration.annotation.Poller;
//import org.springframework.integration.annotation.ServiceActivator;
//import org.springframework.integration.core.MessageSource;
//import org.springframework.integration.file.filters.AcceptOnceFileListFilter;
//import org.springframework.integration.file.remote.session.CachingSessionFactory;
//import org.springframework.integration.file.remote.session.SessionFactory;
//import org.springframework.integration.sftp.filters.SftpSimplePatternFileListFilter;
//import org.springframework.integration.sftp.inbound.SftpInboundFileSynchronizer;
//import org.springframework.integration.sftp.inbound.SftpInboundFileSynchronizingMessageSource;
//import org.springframework.integration.sftp.session.DefaultSftpSessionFactory;
//import org.springframework.messaging.Message;
//import org.springframework.messaging.MessageHandler;
//import org.springframework.messaging.MessagingException;
//
//import java.io.File;
//
//@Configuration
//public class SftpInboundConfiguration {
//
//    @Value("${configuration.sftp.file.sources.remote}")
//    private String remoteDirectory;
//    @Value("${configuration.sftp.file.sources.local}")
//    private String localDirectory;
//    @Value("${configuration.sftp.inbound.username}")
//    private String username;
//    @Value("${configuration.sftp.inbound.password}")
//    private String password;
//    @Value("${configuration.sftp.inbound.host}")
//    private String host;
//    @Value("${configuration.sftp.inbound.port}")
//    private Integer port;
//    @Value("${configuration.sftp.file.filter_extension}")
//    private String extension;
//
//
//    @Bean
//    public SessionFactory<ChannelSftp.LsEntry> sftpSessionInboundFactory() {
//        var factory = new DefaultSftpSessionFactory(true);
//        factory.setHost(host);
//        factory.setPort(port);
//        factory.setUser(username);
//        factory.setPassword(password);
//        factory.setAllowUnknownKeys(true);
//
//        return new CachingSessionFactory<>(factory);
//    }
//
//    @Bean
//    public SftpInboundFileSynchronizer sftpInboundFileSynchronizer() {
//        var fileSynchronizer = new SftpInboundFileSynchronizer(sftpSessionInboundFactory());
//        fileSynchronizer.setDeleteRemoteFiles(true);
//        fileSynchronizer.setRemoteDirectory(remoteDirectory);
//        fileSynchronizer.setFilter(new SftpSimplePatternFileListFilter("*." + extension));
//
//        return fileSynchronizer;
//    }
//
//    @Bean
//    @InboundChannelAdapter(
//            channel = "inbound",
//            poller = @Poller(
//                    fixedDelay = "5000"
//            )
//    )
//    public MessageSource<File> sftpMessageSource() {
//        var source = new SftpInboundFileSynchronizingMessageSource(sftpInboundFileSynchronizer());
//        source.setLocalDirectory(new File(localDirectory));
//        source.setAutoCreateLocalDirectory(true);
//        source.setLocalFilter(new AcceptOnceFileListFilter<>());
//        source.setMaxFetchSize(1);
//
//        return source;
//    }
//
//    @Bean
//    @ServiceActivator(
//            inputChannel = "inbound"
//    )
//    public MessageHandler handler() {
//        return new MessageHandler() {
//            @Override
//            public void handleMessage(Message<?> message) throws MessagingException {
//                System.out.println("PAYLOAD: " + message.getPayload());
//            }
//        };
//    }
//}
