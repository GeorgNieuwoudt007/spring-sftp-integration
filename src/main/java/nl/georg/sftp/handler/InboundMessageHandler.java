package nl.georg.sftp.handler;

import lombok.extern.slf4j.Slf4j;
import nl.georg.sftp.config.SftpConfiguration.OutboundGateway;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.MessagingException;
import org.springframework.stereotype.Component;

import java.io.File;

@Component
@Slf4j
public class InboundMessageHandler implements MessageHandler {

    @Autowired
    private OutboundGateway outboundGateway;

    @Override
    public void handleMessage(Message<?> message) throws MessagingException {
        log.info("now sending the payload: {}", message.getPayload());
        outboundGateway.send((File) message.getPayload());
        log.info("sent the payload");
    }
}
