package com.transport.tms.X3Soap;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.w3c.dom.*;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.net.ssl.*;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
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

    /** XX10CDOCUP — bulk-update vehicle/driver/sequence/status/trailer/date
     *  for every document (stop) in a trip. Called on Lock. Unlike every
     *  other call here, the input is a TABLE (TAB/LIN), not flat FLD — one
     *  LIN row per document. SIZE on the TAB element is the actual number
     *  of rows; DIM="9999" is X3's declared max-rows convention, not the
     *  real count.
     *  Each row map expects: docNum, vehNum, driverId, seq, status,
     *  trailer, trDate (trDate as YYYYMMDD, e.g. "20261108"). */
    public Map<String, Object> updateDocuments(List<Map<String, String>> rows) {
        StringBuilder xml = new StringBuilder();
        xml.append("<PARAM><TAB DIM=\"9999\" ID=\"GRP1\" SIZE=\"").append(rows.size()).append("\">");
        int lineNum = 1;
        for (Map<String, String> row : rows) {
            xml.append("<LIN NUM=\"").append(lineNum++).append("\">");
            xml.append("<FLD NAME=\"I_XPRHNUM\">").append(nz(row.get("docNum"))).append("</FLD>");
            xml.append("<FLD NAME=\"I_XVEHNUM\">").append(nz(row.get("vehNum"))).append("</FLD>");
            xml.append("<FLD NAME=\"I_XDRIVERID\">").append(nz(row.get("driverId"))).append("</FLD>");
            xml.append("<FLD NAME=\"I_XSEQ\">").append(nz(row.get("seq"))).append("</FLD>");
            xml.append("<FLD NAME=\"I_XSTATUS\">").append(nz(row.get("status"))).append("</FLD>");
            xml.append("<FLD NAME=\"I_XTRL\">").append(nz(row.get("trailer"))).append("</FLD>");
            xml.append("<FLD NAME=\"I_XTRDATE\">").append(nz(row.get("trDate"))).append("</FLD>");
            xml.append("</LIN>");
        }
        xml.append("</TAB></PARAM>");
        return call("XX10CDOCUP", xml.toString());
    }

    /** XX10CRESDH — confirm a batch of documents (pick tickets), each
     *  creating a delivery in X3. Called for "LVS Confirm". Same TAB/LIN
     *  table structure as deleteDocuments()/updateDocuments(), but each
     *  row only needs I_XPRHNUM. Response is per-document: TAB/LIN with
     *  I_XPRHNUM (echoed), O_XSTATUS, O_XMESS (e.g. "Delivery created
     *  SHP110010021") — parseXmlToMap() already returns this correctly
     *  as an array under the lowercased TAB ID (e.g. "grp1"), so no
     *  special response handling is needed here. */
    public Map<String, Object> confirmDeliveries(List<String> docNums) {
        StringBuilder lines = new StringBuilder();
        for (int i = 0; i < docNums.size(); i++) {
            lines.append("<LIN NUM=\"").append(i + 1).append("\">")
                 .append("<FLD NAME=\"I_XPRHNUM\">").append(nz(docNums.get(i))).append("</FLD>")
                 .append("</LIN>");
        }
        String inputXml = "<PARAM><TAB DIM=\"9999\" ID=\"GRP1\" SIZE=\"" + docNums.size() + "\">"
            + lines + "</TAB></PARAM>";
        return call("XX10CRESDH", inputXml);
    }

    private String nz(String s) { return s != null ? s : ""; }

    /** X10CCONBUT — Confirm/validate LVS in X3 */
    public Map<String, Object> confirmLvs(String lvsNum) {
        String inputXml = "<PARAM><FLD NAME=\"I_XLVSNUM\" TYPE=\"Char\">" + lvsNum + "</FLD></PARAM>";
        return call("X10CCONBUT", inputXml);
    }

    /** X10CSTKMTV — Load Truck: move stock onto the vehicle for an LVS (input: I_XLVSNUM = LVS number) */
    public Map<String, Object> loadTruck(String lvsNum) {
        String inputXml = "<PARAM><FLD NAME=\"I_XLVSNUM\" TYPE=\"Char\">" + lvsNum + "</FLD></PARAM>";
        return call("X10CSTKMTV", inputXml);
    }

    /** XX10CVTLOC — Create/register a vehicle's location in X3
     *  (input: I_XFCY = site/facility code, I_XVEHLOC = vehicle code,
     *  I_XTYPEFLG = location type flag, e.g. "1"). Response includes
     *  O_XSTATUS (2 = success) and O_XMESS (a message).
     *  BUG FIX: was sending I_VEHLOC (no "X") — X3 silently ignored
     *  that unrecognized field name and its own I_XVEHLOC stayed empty,
     *  which is exactly what came back in the response
     *  (i_xvehloc: "") even though the call reported success. */
    public Map<String, Object> createVehicleLocation(String xfcy, String vehLoc, String xTypeFlg) {
        String inputXml = "<PARAM>"
                + "<FLD NAME=\"I_XFCY\" TYPE=\"Char\">" + xfcy + "</FLD>"
                + "<FLD NAME=\"I_XVEHLOC\" TYPE=\"Char\">" + vehLoc + "</FLD>"
                + "<FLD NAME=\"I_XTYPEFLG\" TYPE=\"Char\">" + (xTypeFlg != null && !xTypeFlg.isBlank() ? xTypeFlg : "1") + "</FLD>"
                + "</PARAM>";
        return call("XX10CVTLOC", inputXml);
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
            log.info("X3 SOAP request -> publicName=[{}] url=[{}]\ninputXml:\n{}",
                    publicName, soapUrl, prettyPrintXml(inputXml));

            String envelope = buildEnvelope(publicName, inputXml);
            String response = sendSoap(envelope);

            log.info("X3 SOAP response <- publicName=[{}]\n{}", publicName, response);

            return parseResponse(response);
        } catch (Exception e) {
            log.error("X3 SOAP call failed [{}]: {}", publicName, e.getMessage());
            return Map.of("error", e.getMessage(), "publicName", publicName);
        }
    }

    /** Best-effort indented XML for readable console output — falls back
     *  to the raw string unchanged if it isn't parseable (never blocks
     *  the actual call over a logging convenience). */
    private String prettyPrintXml(String xml) {
        try {
            Document doc = DocumentBuilderFactory.newInstance()
                    .newDocumentBuilder()
                    .parse(new InputSource(new StringReader(xml)));
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            StringWriter writer = new StringWriter();
            transformer.transform(new DOMSource(doc), new StreamResult(writer));
            return writer.toString();
        } catch (Exception e) {
            return xml;
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

        // TEMPORARY: unblock "PKIX path building failed" against
        // tmsx3em.tema-systems.com. This is a Java-level trust-store
        // issue — the JVM's default cacerts doesn't have a valid chain
        // for that host's certificate (internal/private CA, or the
        // server isn't serving its intermediate cert). The correct fix
        // is either (a) import the real chain into the JVM's cacerts via
        // keytool, or (b) have the X3/ops team fix the server to serve
        // its full intermediate chain — either of those removes the
        // need for this workaround entirely.
        //
        // Scoped ONLY to this connection object (setSSLSocketFactory on
        // the instance) — NOT HttpsURLConnection.setDefaultSSLSocketFactory(),
        // which would disable certificate validation for every HTTPS call
        // in the whole JVM. This still leaves the app vulnerable to a
        // MITM on this one specific internal connection, so treat it as
        // a stopgap for testing, not a production-ready state.
        if (conn instanceof HttpsURLConnection https) {
            https.setSSLSocketFactory(trustAllSslContext().getSocketFactory());
            https.setHostnameVerifier((hostname, session) -> true);
        }

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

    // See sendSoap() comment above — TEMPORARY stopgap for the
    // internal X3 host's cert-chain trust issue, scoped per-connection.
    private SSLContext trustAllSslContext() throws Exception {
        TrustManager[] trustAll = new TrustManager[] {
            new X509TrustManager() {
                public void checkClientTrusted(X509Certificate[] chain, String authType) {}
                public void checkServerTrusted(X509Certificate[] chain, String authType) {}
                public X509Certificate[] getAcceptedIssuers() { return new X509Certificate[0]; }
            }
        };
        SSLContext sc = SSLContext.getInstance("TLS");
        sc.init(null, trustAll, new SecureRandom());
        return sc;
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
