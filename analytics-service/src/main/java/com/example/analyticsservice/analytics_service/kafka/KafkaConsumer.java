package com.example.analyticsservice.analytics_service.kafka;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import patient.events.PatientEvent;





@Service
public class KafkaConsumer {

    private static final Logger log = LoggerFactory.class(KafkaConsumer.class);

    @KafkaListener(topics ="patient", groupId ="analytics-service")
    public void consumeEvent(byte[] event) {
        try {
            PatientEvent patientEvent = PatientEvent.parseFrom(event);
            // perform any business logic related to analytics here

            log.info("Received Patient Event: [ PatientId={}, PatientName={}, PatientEmail={} ]", patientEvent.getPatientId(), patientEvent.getName(), patientEvent.getEmail());
        }
        catch(InvalidProtocolBufferException e) {
            log.error("Error deserializing event {}", e.getMessage());
        }

    }
}
