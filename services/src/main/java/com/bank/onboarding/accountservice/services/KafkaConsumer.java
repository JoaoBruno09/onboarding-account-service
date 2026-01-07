package com.bank.onboarding.accountservice.services;

import com.bank.onboarding.commonslib.persistence.models.CustomerRef;
import com.bank.onboarding.commonslib.persistence.services.CustomerRefRepoService;
import com.bank.onboarding.commonslib.utils.kafka.EventSeDeserializer;
import com.bank.onboarding.commonslib.utils.kafka.models.DocUploadEvent;
import com.bank.onboarding.commonslib.utils.kafka.models.ErrorEvent;
import com.bank.onboarding.commonslib.web.dtos.account.AccountRefDTO;
import com.bank.onboarding.commonslib.web.dtos.customer.CustomerRefDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.stereotype.Service;
import org.springframework.util.backoff.FixedBackOff;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class KafkaConsumer {

    private final EventSeDeserializer eventSeDeserializer;
    private final AccountService accountService;
    private final CustomerRefRepoService customerRefRepoService;

    @KafkaListener(topics = "${spring.kafka.consumer.topic-name}",  groupId = "${spring.kafka.consumer.group-id}")
    public void consumeEvent(ConsumerRecord event){
        String eventValue = event.value().toString();
        switch (event.key().toString()) {
            case "UPDATE_CUSTOMER_REF" -> {
                CustomerRefDTO customerRefDTO = (CustomerRefDTO) eventSeDeserializer.deserialize(eventValue, CustomerRefDTO.class);
                String customerRefDTONumber = customerRefDTO.getCustomerNumber();
                log.info("Event received to update Customer Ref with number {}", customerRefDTONumber);
                CustomerRef customerRef = customerRefRepoService.findCustomerRefByCustomerNumber(customerRefDTONumber);
                if (customerRef.getCustomerNumber() == null) customerRef.setCustomerNumber(customerRefDTONumber);
                customerRef.setValid(customerRefDTO.getIsValid());
                customerRef.setAccounts(customerRefDTO.getAccounts());
                customerRefRepoService.saveCustomerRefDB(customerRef);
            }
            case "DOCS_UPLOAD" -> {
                DocUploadEvent docUploadEvent = (DocUploadEvent) eventSeDeserializer.deserialize(eventValue, DocUploadEvent.class);
                log.info("Event received to validate account docs with number {}", docUploadEvent.getAccountNumber());
                accountService.updateDocsValidOrNotValid(docUploadEvent);
            }
            default -> {
                ErrorEvent errorEvent = (ErrorEvent) eventSeDeserializer.deserialize(eventValue, ErrorEvent.class);
                log.info("Error event {} received for account number {}", errorEvent, Optional.ofNullable(errorEvent.getAccountRefDTO()).map(AccountRefDTO::getAccountNumber).orElse(""));
                accountService.handleErrorEvent(errorEvent);
            }
        }
    }

    @Bean
    public DefaultErrorHandler errorHandler() {
        return new DefaultErrorHandler(new FixedBackOff(0, 0));
    }
}
