package nl.georg.sftp.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.integration.file.FileNameGenerator;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

import java.io.File;

@Component
@Slf4j
public class OutboundFileNameGenerator implements FileNameGenerator {

    @Override
    public String generateFileName(Message<?> message) {
        if (message.getPayload() instanceof File) {
            log.info("message payload sending now " + message.getPayload());

            return ((File) message.getPayload()).getName();
        } else {
            throw new IllegalArgumentException("File expected as payload.");
        }
    }
}
