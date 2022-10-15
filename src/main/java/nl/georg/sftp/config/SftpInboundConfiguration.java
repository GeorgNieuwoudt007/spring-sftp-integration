package nl.georg.sftp.config;

import com.jcraft.jsch.ChannelSftp;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.expression.common.LiteralExpression;
import org.springframework.integration.annotation.InboundChannelAdapter;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.annotation.Transformer;
import org.springframework.integration.core.MessageSource;
import org.springframework.integration.file.filters.AcceptAllFileListFilter;
import org.springframework.integration.file.remote.session.CachingSessionFactory;
import org.springframework.integration.file.remote.session.SessionFactory;
import org.springframework.integration.sftp.filters.SftpSimplePatternFileListFilter;
import org.springframework.integration.sftp.inbound.SftpInboundFileSynchronizer;
import org.springframework.integration.sftp.inbound.SftpStreamingMessageSource;
import org.springframework.integration.sftp.outbound.SftpMessageHandler;
import org.springframework.integration.sftp.session.DefaultSftpSessionFactory;
import org.springframework.integration.sftp.session.SftpRemoteFileTemplate;
import org.springframework.integration.transformer.StreamTransformer;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHandler;

import java.io.InputStream;

@Configuration
public class SftpInboundConfiguration {

    @Value("${configuration.sftp.file.sources.remote}")
    private String directory;
    @Value("${configuration.sftp.inbound.username}")
    private String username;
    @Value("${configuration.sftp.inbound.password}")
    private String password;
    @Value("${configuration.sftp.inbound.host}")
    private String host;
    @Value("${configuration.sftp.inbound.port}")
    private Integer port;
    @Value("${configuration.sftp.file.filter_extension}")
    private String extension;

    private static void handleMessage(Message<?> message) {
        System.out.println("PAYLOAD: " + message.getPayload());
    }

    @Bean
    public SessionFactory<ChannelSftp.LsEntry> sftpInboundSessionFactory() {
        var factory = new DefaultSftpSessionFactory(true);
        factory.setHost(host);
        factory.setPort(port);
        factory.setUser(username);
        factory.setPassword(password);
        factory.setAllowUnknownKeys(true);

        return new CachingSessionFactory<>(factory);
    }

    @Bean
    @InboundChannelAdapter(
            channel = "share"
    )
    public MessageSource<InputStream> ftpMessageSource() {
        var messageSource = new SftpStreamingMessageSource(template());
        messageSource.setRemoteDirectory(directory);
        messageSource.setFilter(new AcceptAllFileListFilter<>());
        messageSource.setMaxFetchSize(1);

        return messageSource;
    }

    @Bean
    @Transformer(
            inputChannel = "share",
            outputChannel = "share"
    )
    public org.springframework.integration.transformer.Transformer transformer() {
        return new StreamTransformer("UTF-8");
    }

    @Bean
    public SftpRemoteFileTemplate template() {
        return new SftpRemoteFileTemplate(sftpInboundSessionFactory());
    }


    @Bean(
            name = "synchronizer"
    )
    public SftpInboundFileSynchronizer synchronizer() {
        var synchronizer = new SftpInboundFileSynchronizer(sftpInboundSessionFactory());
        synchronizer.setDeleteRemoteFiles(true);
        synchronizer.setRemoteDirectory(directory);
        synchronizer.setFilter(new SftpSimplePatternFileListFilter("*." + extension));

        return synchronizer;

    }

    @Bean
    @ServiceActivator(
            inputChannel = "share"
    )
    public MessageHandler handler() {
        var handler = new SftpMessageHandler(sftpInboundSessionFactory());
        handler.setRemoteDirectoryExpression(new LiteralExpression(directory));
        handler.setFileNameGenerator(message -> "handlerContent.test");

        return handler;
    }
}
