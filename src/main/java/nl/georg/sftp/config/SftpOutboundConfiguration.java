package nl.georg.sftp.config;

import com.jcraft.jsch.ChannelSftp;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.annotation.Gateway;
import org.springframework.integration.annotation.MessagingGateway;
import org.springframework.integration.file.remote.session.CachingSessionFactory;
import org.springframework.integration.file.remote.session.SessionFactory;
import org.springframework.integration.sftp.session.DefaultSftpSessionFactory;

import java.io.File;

@Configuration
public class SftpOutboundConfiguration {

    @Value("${configuration.sftp.outbound.username}")
    private String username;
    @Value("${configuration.sftp.outbound.password}")
    private String password;
    @Value("${configuration.sftp.outbound.host}")
    private String host;
    @Value("${configuration.sftp.outbound.port}")
    private Integer port;

    @Bean
    public SessionFactory<ChannelSftp.LsEntry> sftpOutboundSessionFactory() {
        var factory = new DefaultSftpSessionFactory();
        factory.setHost(host);
        factory.setPort(port);
        factory.setUser(username);
        factory.setPassword(password);
        factory.setAllowUnknownKeys(true);

        return new CachingSessionFactory<>(factory);
    }

    @MessagingGateway
    public interface UploadGateway {

        @Gateway(
                requestChannel = "share"
        )
        void sendToSftp(File file);
    }
}
