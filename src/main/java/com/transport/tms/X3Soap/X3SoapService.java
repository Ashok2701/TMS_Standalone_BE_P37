package com.transport.tms.X3Soap;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.w3c.dom.*;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;

/**
 * Sage X3 SOAP Web Service client.
 * Mirrors CBTTL service.js but runs server-side (credentials never exposed to browser).
 *
 * WSDL: https://tmsx3em.tema-systems.com/soap-wsdl/syracuse/collaboration/syracuse/CAdxWebServiceXmlCC?wsdl
 * poolAlias: TMSNEW
 */
@Slf4j
@Service
public class X3SoapService {

    private final String soapUrl;
    private final String username;
    private final String password;
    private static final String POOL_ALIAS    = "TMSNEW";
    private static final String LANG          = "ENG";
    private static final String SOAP_ACTION   = "CAdxWebServiceXmlCC";

    public X3SoapService(String soapUrl, String username, String password) {
        this.soapUrl  = soapUrl;
        this.username = username;
        this.password = password;
    }

    // ═══════════════════════════════════════════════════════════
    // PUBLIC SERVICE METHODS
    // ═══════════════════════════════════════════════════════════

    /** X10CCONBUT — Confirm/validate LVS in X3 */
    public Map<String, Object> confirmLvs(String lvsNum) {
        String inputXml = "<PARAM><FLD NAME=\"I_XLVSNUM\" TYPE=\"Char\">" + lvsNum + "</FLD></PARAM>";
        return call("X10CCONBUT", inputXml);
    }

    /** X1CROUTDET — Get route/trip detail */
    public Map<String, Object> getRouteDetail(String vrNum) {
        String inputXml = "<PARAM><FLD NAME=\"I_XROUTE\" TYPE=\"Char\">" + vrNum + "</FLD></PARAM>";
        return call("X1CROUTDET", inputXml);
    }

    /** X1CALLDET — Get allocation details */
    public Map<String, Object> getAllocationDetails(String vrNum, String floctyp, String tloctyp, String floc, String tloc) {
        String inputXml = "<PARAM>"
            + "<FLD NAME=\"I_XROUTE\" TYPE=\"Char\">"   + vrNum   + "</FLD>"
            + "<FLD NAME=\"I_XFROMLOC\" TYPE=\"Char\">" + floctyp + "</FLD>"
            + "<FLD NAME=\"I_XTOLOC\" TYPE=\"Char\">"   + tloctyp + "</FLD>"
            + "<FLD NAME=\"I_XLOCF\" TYPE=\"Char\">"    + floc    + "</FLD>"
            + "<FLD NAME=\"I_XLOCT\" TYPE=\"Char\">"    + tloc    + "</FLD>"
            + "</PARAM>";
        return call("X1CALLDET", inputXml);
    }

    /** X1CPICALL — Submit pick allocation */
    public Map<String, Object> submitAllocation(String pickNum) {
        String inputXml = "<PARAM><FLD NAME=\"I_XPICKNUM\" TYPE=\"Char\">" + pickNum + "</FLD></PARAM>";
        return call("X1CPICALL", inputXml);
    }

    /** X1CLOTDET — Get lot details */
    public Map<String, Object> getLotDetails(String site, String productNum, String vrNum) {
        String inputXml = "<PARAM>"
            + "<FLD NAME=\"I_XFCY\" TYPE=\"Char\">"    + site       + "</FLD>"
            + "<FLD NAME=\"I_XITMREF\" TYPE=\"Char\">" + productNum + "</FLD>"
            + "<FLD NAME=\"I_XROUTE\" TYPE=\"Char\">"  + vrNum      + "</FLD>"
            + "</PARAM>";
        return call("X1CLOTDET", inputXml);
    }

    /** X1CSTASTO — Staging location allocation data */
    public Map<String, Object> getAllocatedDataByStagingLocations(String vrNum, String fromloc, String toloc, String floc, String tloc) {
        String inputXml = "<PARAM>"
            + "<FLD NAME=\"I_XROUTE\" TYPE=\"Char\">"   + vrNum   + "</FLD>"
            + "<FLD NAME=\"I_XFROMLOC\" TYPE=\"Char\">" + fromloc + "</FLD>"
            + "<FLD NAME=\"I_XTOLOC\" TYPE=\"Char\">"   + toloc   + "</FLD>"
            + "<FLD NAME=\"I_XLOCF\" TYPE=\"Char\">"    + floc    + "</FLD>"
            + "<FLD NAME=\"I_XLOCT\" TYPE=\"Char\">"    + tloc    + "</FLD>"
            + "</PARAM>";
        return call("X1CSTASTO", inputXml);
    }

    /** X1CSTALOC — Get staging locations */
    public Map<String, Object> getStagingLocations(String site) {
        String inputXml = "<PARAM><FLD NAME=\"I_XFCY\" TYPE=\"Char\">" + site + "</FLD></PARAM>";
        return call("X1CSTALOC", inputXml);
    }

    /** X1CLOCSEL — Get locations by type */
    public Map<String, Object> getLocations(String site, String floctyp, String tloctyp) {
        String inputXml = "<PARAM>"
            + "<FLD NAME=\"I_XFCY\" TYPE=\"Char\">"      + site    + "</FLD>"
            + "<FLD NAME=\"I_XLOCTYPF\" TYPE=\"Char\">"  + floctyp + "</FLD>"
            + "<FLD NAME=\"I_XLOCTYPT\" TYPE=\"Char\">"  + tloctyp + "</FLD>"
            + "</PARAM>";
        return call("X1CLOCSEL", inputXml);
    }

    /** XPCKTCKDL — Delete pick ticket documents */
    public Map<String, Object> deleteDocuments(List<String> docNums) {
        StringBuilder lines = new StringBuilder();
        for (int i = 0; i < docNums.size(); i++) {
            lines.append("<LIN NUM=\"").append(i + 1).append("\">")
                 .append("<FLD NAME=\"I_XPCKNUM\" TYPE=\"Char\">").append(docNums.get(i)).append("</FLD>")
                 .append("</LIN>");
        }
        String inputXml = "<PARAM><TAB DIM=\"99\" ID=\"GRP1\" SIZE=\"" + docNums.size() + "\">"
            + lines + "</TAB></PARAM>";
        return call("XPCKTCKDL", inputXml);
    }

    // ═══════════════════════════════════════════════════════════
    // CORE SOAP CALL — same structure as CBTTL service.js
    // ═══════════════════════════════════════════════════════════
    public Map<String, Object> call(String publicName, String inputXml) {
        try {
            String envelope = buildEnvelope(publicName, inputXml);
            String response = sendSoap(envelope);
            return parseResponse(response);
        } catch (Exception e) {
            log.error("X3 SOAP call failed [{}]: {}", publicName, e.getMessage());
            return Map.of("error", e.getMessage(), "publicName", publicName);
        }
    }

    // ═══════════════════════════════════════════════════════════
    // SOAP envelope builder — matches CBTTL exactly
    // ═══════════════════════════════════════════════════════════
    private String buildEnvelope(String publicName, String inputXml) {
        return "<soapenv:Envelope "
            + "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" "
            + "xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" "
            + "xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" "
            + "xmlns:wss=\"http://www.adonix.com/WSS\" "
            + "xmlns:soapenc=\"http://schemas.xmlsoap.org/soap/encoding/\">"
            + "<soapenv:Header/>"
            + "<soapenv:Body>"
            + "<wss:run soapenv:encodingStyle=\"http://schemas.xmlsoap.org/soap/encoding/\">"
            + "<callContext xsi:type=\"wss:CAdxCallContext\">"
            + "<codeLang xsi:type=\"xsd:string\">"  + LANG       + "</codeLang>"
            + "<poolAlias xsi:type=\"xsd:string\">" + POOL_ALIAS + "</poolAlias>"
            + "<poolId xsi:type=\"xsd:string\"></poolId>"
            + "<requestConfig xsi:type=\"xsd:string\"></requestConfig>"
            + "</callContext>"
            + "<publicName xsi:type=\"xsd:string\">" + publicName + "</publicName>"
            + "<inputXml xsi:type=\"xsd:string\"><![CDATA[" + inputXml + "]]></inputXml>"
            + "</wss:run>"
            + "</soapenv:Body>"
            + "</soapenv:Envelope>";
    }

    // ═══════════════════════════════════════════════════════════
    // HTTP POST with Basic Auth
    // ═══════════════════════════════════════════════════════════
    private String sendSoap(String envelope) throws Exception {
        URL url = new URL(soapUrl);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setDoOutput(true);
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "text/xml; charset=utf-8");
        conn.setRequestProperty("SOAPAction", SOAP_ACTION);
        conn.setConnectTimeout(30000);
        conn.setReadTimeout(60000);

        // Basic Auth — same as CBTTL btoa(username + ':' + password)
        String auth = java.util.Base64.getEncoder()
                .encodeToString((username + ":" + password).getBytes());
        conn.setRequestProperty("Authorization", "Basic " + auth);

        try (OutputStream os = conn.getOutputStream()) {
            os.write(envelope.getBytes("UTF-8"));
        }

        int status = conn.getResponseCode();
        InputStream is = status < 400 ? conn.getInputStream() : conn.getErrorStream();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(is, "UTF-8"))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) sb.append(line);
            if (status >= 400) throw new RuntimeException("HTTP " + status + ": " + sb);
            return sb.toString();
        }
    }

    // ═══════════════════════════════════════════════════════════
    // Parse SOAP response — extract resultXml content
    // ═══════════════════════════════════════════════════════════
    private Map<String, Object> parseResponse(String soapResponse) {
        try {
            Document doc = DocumentBuilderFactory.newInstance()
                    .newDocumentBuilder()
                    .parse(new InputSource(new StringReader(soapResponse)));

            // Extract resultXml (mirrors CBTTL: getElementsByTagName('resultXml')[0].innerHTML)
            NodeList resultNodes = doc.getElementsByTagName("resultXml");
            if (resultNodes.getLength() == 0) {
                return Map.of("raw", soapResponse);
            }

            String resultXml = resultNodes.item(0).getTextContent();
            // Strip CDATA markers (mirrors CBTTL: slice(9, length-3))
            resultXml = resultXml.trim();
            if (resultXml.startsWith("<![CDATA[")) {
                resultXml = resultXml.substring(9, resultXml.length() - 3);
            }

            // Parse the inner XML into a Map
            return parseXmlToMap(resultXml);

        } catch (Exception e) {
            log.error("Failed to parse SOAP response: {}", e.getMessage());
            return Map.of("raw", soapResponse, "parseError", e.getMessage());
        }
    }

    // Convert XML to nested Map (FLD, TAB, GRP, LIN elements)
    private Map<String, Object> parseXmlToMap(String xml) {
        try {
            Document doc = DocumentBuilderFactory.newInstance()
                    .newDocumentBuilder()
                    .parse(new InputSource(new StringReader(xml)));

            Map<String, Object> result = new LinkedHashMap<>();

            // Extract FLD elements → key/value pairs
            NodeList fields = doc.getElementsByTagName("FLD");
            for (int i = 0; i < fields.getLength(); i++) {
                Element el = (Element) fields.item(i);
                String name = el.getAttribute("NAME");
                String value = el.getTextContent();
                if (name != null && !name.isBlank()) {
                    result.put(name.toLowerCase(), value);
                }
            }

            // Extract TAB/LIN elements → arrays
            NodeList tabs = doc.getElementsByTagName("TAB");
            for (int t = 0; t < tabs.getLength(); t++) {
                Element tab = (Element) tabs.item(t);
                String tabId = tab.getAttribute("ID");
                List<Map<String, Object>> rows = new ArrayList<>();
                NodeList lines = tab.getElementsByTagName("LIN");
                for (int l = 0; l < lines.getLength(); l++) {
                    Element lin = (Element) lines.item(l);
                    Map<String, Object> row = new LinkedHashMap<>();
                    NodeList linFields = lin.getElementsByTagName("FLD");
                    for (int f = 0; f < linFields.getLength(); f++) {
                        Element fld = (Element) linFields.item(f);
                        row.put(fld.getAttribute("NAME").toLowerCase(), fld.getTextContent());
                    }
                    rows.add(row);
                }
                result.put(tabId != null && !tabId.isBlank() ? tabId.toLowerCase() : "rows", rows);
            }

            // Extract STATUS / MESSA (error messages)
            NodeList status = doc.getElementsByTagName("STATUS");
            if (status.getLength() > 0) result.put("status", status.item(0).getTextContent());
            NodeList messa = doc.getElementsByTagName("MESSA");
            if (messa.getLength() > 0) result.put("message", messa.item(0).getTextContent());

            return result;
        } catch (Exception e) {
            return Map.of("raw", xml, "parseError", e.getMessage());
        }
    }
}
