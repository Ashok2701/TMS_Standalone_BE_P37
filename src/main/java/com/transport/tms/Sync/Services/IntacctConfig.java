package com.transport.tms.Sync.Services;


import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Data
public class IntacctConfig {

    @Value("${intacct.senderId}")
    private String senderId;

    @Value("${intacct.senderPassword}")
    private String senderPassword;

    @Value("${intacct.companyId}")
    private String companyId;

    @Value("${intacct.userId}")
    private String userId;

    @Value("${intacct.userPassword}")
    private String userPassword;
}