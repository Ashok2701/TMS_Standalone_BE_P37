package com.transport.tms.Fleet.Repository;


import com.transport.tms.Fleet.Entity.DropdownData;
import com.transport.tms.Fleet.Entity.PostalCodeDetails;
import com.transport.tms.Fleet.Entity.StyleData;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Repository
public class CommonRepository {
    @Autowired
    private EntityManager entityManager;

    @Value("${db.schema}")
    private String dbSchema;


    public List<DropdownData> getSiteList() {
        String queryString = "select FCY_0 as value, FCYNAM_0 as label from " + dbSchema + ".FACILITY";

        Query query = entityManager.createNativeQuery(queryString);
        List<Object[]> results = query.getResultList();
        return results.stream()
                .map(result -> new DropdownData( result[0]!=null?result[0].toString():"",
                        result[1]!=null?result[1].toString():""))
                .collect(Collectors.toList());
    }

    public List<DropdownData> getCarrierList() {
        String queryString = "select BPTNUM_0, BPTNAM_0 from " + dbSchema + ".BPCARRIER";

        Query query = entityManager.createNativeQuery(queryString);
        List<Object[]> results = query.getResultList();
        return results.stream()
                .map(result -> new DropdownData( result[0]!=null?result[0].toString():"",
                        result[1]!=null?result[1].toString():""))
                .collect(Collectors.toList());
    }

    public List<DropdownData> getBusinessLineList() {
        String queryString = "select distinct IDENT2_0, TEXTE_0 from "+dbSchema+".ATEXTRA \n" +
                "where IDENT1_0='425' and IDENT2_0 in (select CODE_0 from "+dbSchema+".ATABDIV where NUMTAB_0=425)\n" +
                "and LANGUE_0 = 'ENG' and ZONE_0='LNGDES' order by IDENT2_0";

        Query query = entityManager.createNativeQuery(queryString);
        List<Object[]> results = query.getResultList();
        return results.stream()
                .map(result -> new DropdownData( result[0]!=null?result[0].toString():"",
                        result[1]!=null?result[1].toString():""))
                .collect(Collectors.toList());
    }

    public List<DropdownData> getPrimaryLanguageList() {
        String queryString = "select IDENT1_0, TEXTE_0 from tms.ATEXTRA where IDENT1_0 in (select LAN_0 from "+dbSchema+ ".TABLAN) \n" +
                "and ZONE_0='INTDES' and LANGUE_0='ENG' order by IDENT1_0";

        Query query = entityManager.createNativeQuery(queryString);
        List<Object[]> results = query.getResultList();
        return results.stream()
                .map(result -> new DropdownData( result[0]!=null?result[0].toString():"",
                        result[1]!=null?result[1].toString():""))
                .collect(Collectors.toList());
    }

    public List<StyleData> getStyleList() {
        String queryString = "select distinct DES_0, COD_0,STY_0  from "+dbSchema+".ASTYLE \n" +
                "order by COD_0";

        Query query = entityManager.createNativeQuery(queryString);
        List<Object[]> results = query.getResultList();
        return results.stream()
                .map(result -> new StyleData( result[1]!=null?result[1].toString():"",
                        result[0]!=null?result[0].toString():"",
                        result[2]!=null?result[2].toString():""))
                .collect(Collectors.toList());
    }

    public List<DropdownData> getUnAvailableList() {
        String queryString = "select IDENT1_0, TEXTE_0 from "+dbSchema+".ATEXTRA \n" +
                "where IDENT1_0 in (select UVYCOD_0 from "+dbSchema+".TABUNAVAIL)\n" +
                "and LANGUE_0='ENG' and TEXTE_0<>'' and CODFIC_0='TABUNAVAIL'\n" +
                "and ZONE_0='DESAXX' order by IDENT1_0";

        Query query = entityManager.createNativeQuery(queryString);
        List<Object[]> results = query.getResultList();
        return results.stream()
                .map(result -> new DropdownData( result[0]!=null?result[0].toString():"",
                        result[1]!=null?result[1].toString():""))
                .collect(Collectors.toList());
    }

    public List<DropdownData> getCountryList() {
        String queryString = "select distinct IDENT1_0, TEXTE_0 from "+dbSchema+".ATEXTRA where IDENT1_0 in (select CRY_0 from "+dbSchema+".TABCOUNTRY)\n" +
                "and CODFIC_0='TABCOUNTRY' and TEXTE_0<>'' and LANGUE_0='ENG' and ZONE_0='CRYDES'\n" +
                "order by IDENT1_0";

        Query query = entityManager.createNativeQuery(queryString);
        List<Object[]> results = query.getResultList();
        return results.stream()
                .map(result -> new DropdownData( result[0]!=null?result[0].toString():"",
                        result[1]!=null?result[1].toString():""))
                .collect(Collectors.toList());
    }

    public List<PostalCodeDetails> getPostalDetailsList(String country) {
        String queryString = "select CRY_0, POSCOD_0, POSCTY_0, SATCOD_0 from "+dbSchema+".POSCOD where CRY_0='"+country+"'";

        Query query = entityManager.createNativeQuery(queryString);
        List<Object[]> results = query.getResultList();
        return results.stream()
                .map(result -> new PostalCodeDetails( result[0]!=null?result[0].toString():"",
                        result[1]!=null?result[1].toString():"",
                        result[2]!=null?result[2].toString():"",
                        result[3]!=null?result[3].toString():""))
                .collect(Collectors.toList());
    }

    public List<DropdownData> getInspectionList() {
        String queryString = "select XID_0, XQDES_0 from "+dbSchema+".XINSQUEH order by XID_0";

        Query query = entityManager.createNativeQuery(queryString);
        List<Object[]> results = query.getResultList();
        return results.stream()
                .map(result -> new DropdownData( result[0]!=null?result[0].toString():"",
                        result[1]!=null?result[1].toString():""))
                .collect(Collectors.toList());
    }

    public List<DropdownData> getFixedAssetList() {
        String queryString = "select AASREF_0, AASDES1_0 from "+dbSchema+".FXDASSETS order by AASREF_0";

        Query query = entityManager.createNativeQuery(queryString);
        List<Object[]> results = query.getResultList();
        return results.stream()
                .map(result -> new DropdownData( result[0]!=null?result[0].toString():"",
                        result[1]!=null?result[1].toString():""))
                .collect(Collectors.toList());
    }


    public List<DropdownData> getVehicleFuelUnitList() {
        String queryString = "select IDENT1_0, TEXTE_0 FROM tms.ATEXTRA " +
                "where CODFIC_0 = 'TABUNIT' and LANGUE_0='ENG' and ZONE_0='DES'\n";

        Query query = entityManager.createNativeQuery(queryString);
        List<Object[]> results = query.getResultList();
        return results.stream()
                .map(result -> new DropdownData( result[0]!=null?result[0].toString():"",
                        result[1]!=null?result[1].toString():""))
                .collect(Collectors.toList());
    }

    public List<DropdownData> getDriverList() {
        String queryString = "select DRIVERID_0, DRIVER_0 from "+dbSchema+".XX10CDRIVER " +
                " order by DRIVERID_0";

        Query query = entityManager.createNativeQuery(queryString);
        List<Object[]> results = query.getResultList();
        return results.stream()
                .map(result -> new DropdownData( result[0]!=null?result[0].toString():"",
                        result[1]!=null?result[1].toString():""))
                .collect(Collectors.toList());
    }

    public List<DropdownData> getCustomerList() {
        String queryString = "select BPCNUM_0, BPCNAM_0 from "+dbSchema+".BPCUSTOMER order by BPCNUM_0";

        Query query = entityManager.createNativeQuery(queryString);
        List<Object[]> results = query.getResultList();
        return results.stream()
                .map(result -> new DropdownData( result[0]!=null?result[0].toString():"",
                        result[1]!=null?result[1].toString():""))
                .collect(Collectors.toList());
    }

    public List<DropdownData> getCategoryList() {
        String queryString = "select distinct TCLCOD_0, TEXTE_0 from "+dbSchema+".ITMCATEG i\n" +
                "join "+dbSchema+".ATEXTRA a on a.IDENT1_0 = i.TCLCOD_0\n" +
                "where a.CODFIC_0='ITMCATEG' and a.LANGUE_0='ENG' and a.ZONE_0='TCLAXX'\n" +
                "order by TCLCOD_0";

        Query query = entityManager.createNativeQuery(queryString);
        List<Object[]> results = query.getResultList();
        return results.stream()
                .map(result -> new DropdownData( result[0]!=null?result[0].toString():"",
                        result[1]!=null?result[1].toString():""))
                .collect(Collectors.toList());
    }


    public List<DropdownData> getVehicleClassList() {
        String queryString = "select CLASS_0, DES_0 from "+dbSchema+".XX10CCLASS order by CLASS_0";

        Query query = entityManager.createNativeQuery(queryString);
        List<Object[]> results = query.getResultList();
        return results.stream()
                .map(result -> new DropdownData( result[0]!=null?result[0].toString():"",
                        result[1]!=null?result[1].toString():""))
                .collect(Collectors.toList());
    }

    public List<DropdownData> getTrailerTypeList() {
        String queryString = "select XTRACOD_0, XDES_0 from "+dbSchema+".XX10CXTRA order by XTRACOD_0";

        Query query = entityManager.createNativeQuery(queryString);
        List<Object[]> results = query.getResultList();
        return results.stream()
                .map(result -> new DropdownData( result[0]!=null?result[0].toString():"",
                        result[1]!=null?result[1].toString():""))
                .collect(Collectors.toList());
    }


    public List<DropdownData> getDocumentTypeList() {
        String queryString = "select IDENT2_0, TEXTE_0 from "+dbSchema+".ATEXTRA where ZONE_0='LNGDES'\n" +
                "and IDENT1_0=1502 and IDENT2_0 in ( select CODE_0 from "+dbSchema+".ATABDIV where NUMTAB_0=1502)";

        Query query = entityManager.createNativeQuery(queryString);
        List<Object[]> results = query.getResultList();
        return results.stream()
                .map(result -> new DropdownData( result[0]!=null?result[0].toString():"",
                        result[1]!=null?result[1].toString():""))
                .collect(Collectors.toList());
    }

    public List<DropdownData> getissueAuthList() {
        String queryString = "select IDENT2_0,TEXTE_0 from "+dbSchema+".ATEXTRA where ZONE_0='LNGDES' and IDENT1_0=1503 " +
                "        and IDENT2_0 in ( select CODE_0 from "+dbSchema+".ATABDIV where NUMTAB_0=1503)";

        Query query = entityManager.createNativeQuery(queryString);
        List<Object[]> results = query.getResultList();
        return results.stream()
                .map(result -> new DropdownData( result[0]!=null?result[0].toString():"",
                        result[1]!=null?result[1].toString():""))
                .collect(Collectors.toList());
    }

    public List<DropdownData> getLocalMenuList(Integer type) {
        String queryString = "select LANNUM_0, LANMES_0 from "+dbSchema+".APLSTD  " +
                "where LANCHP_0="+type+" and LAN_0 ='ENG' and LANNUM_0<>0";

        Query query = entityManager.createNativeQuery(queryString);
        List<Object[]> results = query.getResultList();
        return results.stream()
                .map(result -> new DropdownData( result[0]!=null?result[0].toString():"",
                        result[1]!=null?result[1].toString():""))
                .collect(Collectors.toList());
    }

    public List<Map<String, Object>> getVehicleData() {
        String queryString = "select CODEYVE_0, CATEGO_0, XCODOMETER_0,FCY_0 from "+dbSchema+".XX10CVEHICUL";

        Query query = entityManager.createNativeQuery(queryString);
        List<Object[]> vehicleResults = query.getResultList();
        List<Map<String, Object>> vehicleDataList = new ArrayList<>();
        for (Object[] row : vehicleResults) {
            Map<String, Object> vehicleDataMap = new HashMap<>();
            vehicleDataMap.put("vehicle", row[0]);
            vehicleDataMap.put("vehicleClass", row[1]);
            vehicleDataMap.put("odoStart", row[2]);
            vehicleDataMap.put("site", row[3]);
            vehicleDataList.add(vehicleDataMap);
        }

        return vehicleDataList;
    }

    public List<Map<String, Object>> getDriverData() {
        String queryString = "select DRIVERID_0, DRIVER_0, MOB_0, LICETYP_0, LICENUM_0, FCY_0 from "+dbSchema+".XX10CDRIVER";

        Query query = entityManager.createNativeQuery(queryString);
        List<Object[]> driverResults = query.getResultList();
        List<Map<String, Object>> driverDataList = new ArrayList<>();
        for (Object[] row : driverResults) {
            Map<String, Object> driverDataMap = new HashMap<>();
            driverDataMap.put("driverId", row[0]);
            driverDataMap.put("driverName", row[1]);
            driverDataMap.put("mobile", row[2]);
            driverDataMap.put("licenseType", row[3]);
            driverDataMap.put("licenseNum", row[4]);
            driverDataMap.put("site",row[5]);
            driverDataList.add(driverDataMap);
        }

        return driverDataList;
    }
}
