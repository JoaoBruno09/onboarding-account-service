package com.bank.onboarding.accountservice.services;

import com.bank.onboarding.commonslib.persistence.services.CustomerRefRepoService;
import com.bank.onboarding.commonslib.utils.kafka.models.ErrorEvent;
import com.bank.onboarding.commonslib.utils.kafka.EventSeDeserializer;
import com.bank.onboarding.commonslib.utils.mappers.CustomerMapper;
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
        switch (event.key().toString()) {
            case "UPDATE_CUSTOMER_REF" -> {
                CustomerRefDTO customerRefDTO = (CustomerRefDTO) eventSeDeserializer.deserialize(event.value().toString(), CustomerRefDTO.class);
                log.info("Event received to update Customer Ref with number {}", customerRefDTO.getCustomerNumber());
                customerRefRepoService.saveCustomerRefDB(CustomerMapper.INSTANCE.toCustomerRef(customerRefDTO));
            }
            default -> {
                ErrorEvent errorEvent = (ErrorEvent) eventSeDeserializer.deserialize(event.value().toString(), ErrorEvent.class);
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
