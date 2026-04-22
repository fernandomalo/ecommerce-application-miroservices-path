package com.fernando.microservices.cart_service.config;

import org.springframework.jms.JmsException;
import org.springframework.jms.support.converter.MessageConversionException;
import org.springframework.jms.support.converter.MessageConverter;
import org.springframework.stereotype.Component;

import com.fernando.microservices.common_service.events.order_events.OrderNotCreatedEvent;

import jakarta.jms.JMSException;
import jakarta.jms.Message;
import jakarta.jms.Session;
import jakarta.jms.TextMessage;
import lombok.RequiredArgsConstructor;
import tools.jackson.databind.json.JsonMapper;

@Component
@RequiredArgsConstructor
public class JacksonConverter implements MessageConverter {

    private final JsonMapper jsonMapper;

    @Override
    public Object fromMessage(Message message) throws JMSException, MessageConversionException {
        if (message instanceof TextMessage textMessage) {
            try {
                String json = textMessage.getText();
                String text = message.getStringProperty("_type");
                if (text == null) {
                    throw new JMSException("Can't identify the name class");
                }
                Class<?> clazz = Class.forName(text);
                return jsonMapper.readValue(json, clazz);
                // return jsonMapper.readValue(json,
                //     jsonMapper.getTypeFactory().constructCollectionType(List.class, CreateOrderEvent.class));
            } catch (Exception e) {
                throw new JMSException("Failed to parse JSON: " + e.getMessage());
            }
        }
        throw new JMSException("Only TextMessage is supported");
    }

    @Override
    public Message toMessage(Object object, Session session) throws JMSException, MessageConversionException {
        try {
            String json = jsonMapper.writeValueAsString(object);
            TextMessage message = session.createTextMessage(json);
            message.setStringProperty("_type", object.getClass().getName());
            return message;
        } catch (Exception e) {
            throw new JMSException("Failed to convert to JSON: " + e.getMessage());
        }
    }
    
}
