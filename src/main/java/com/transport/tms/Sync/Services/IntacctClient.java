package com.transport.tms.Sync.Services;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;

@Service
public class IntacctClient {

    @Autowired
    private IntacctConfig config;

    private final String endpoint = "https://api.intacct.com/ia/xml/xmlgw.phtml";

    public String call(String xmlRequest) {

        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_XML);

        HttpEntity<String> request = new HttpEntity<>(xmlRequest, headers);

        ResponseEntity<String> response =
                restTemplate.postForEntity(endpoint, request, String.class);

        return response.getBody();
    }

    public String getSessionId() {

        String request =
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                        "<request>" +

                        "<control>" +
                        "<senderid>" + config.getSenderId() + "</senderid>" +
                        "<password>" + config.getSenderPassword() + "</password>" +
                        "<controlid>" + System.currentTimeMillis() + "</controlid>" +
                        "<uniqueid>false</uniqueid>" +
                        "<dtdversion>3.0</dtdversion>" +
                        "</control>" +

                        "<operation>" +
                        "<authentication>" +
                        "<login>" +
                        "<userid>" + config.getUserId() + "</userid>" +
                        "<companyid>" + config.getCompanyId() + "</companyid>" +
                        "<password>" + config.getUserPassword() + "</password>" +
                        "</login>" +
                        "</authentication>" +
                        "</operation>" +

                        "</request>";

        String response = call(request);
        System.out.println("LOGIN RESPONSE: " + response);
        return extractSessionId(response);
    }

    private String extractSessionId(String xml) {

        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();

            Document doc = builder.parse(new InputSource(new StringReader(xml)));

            NodeList nodes = doc.getElementsByTagName("sessionid");

            if (nodes.getLength() > 0) {
                String session = nodes.item(0).getTextContent().trim();
                System.out.println("SESSION ID: " + session);
                return session;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
}