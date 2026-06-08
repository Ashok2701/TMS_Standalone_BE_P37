package com.transport.tms.Fleet.Controller;

import com.transport.tms.Fleet.Dto.FleetDriverVO;
import com.transport.tms.Fleet.Dto.FleetVehicleVO;
import com.transport.tms.Fleet.Dto.TrailerVO;
import com.transport.tms.Fleet.Dto.VehicleClassVO;
import com.transport.tms.Fleet.Entity.*;
import com.transport.tms.Fleet.Service.FleetService;
import com.transport.tms.GlobalException.ApplicationException;
import com.transport.tms.UserManagement.Dto.AccessTokenVO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.List;

@RestController
@RequestMapping("/api/v1/fleet")
@SuppressWarnings("unused")
public class FleetController {

    private final FleetService service;

    public FleetController(FleetService service) {
        this.service = service;
    }

    @PostMapping("/createVehicle")
    public ResponseEntity<Object> createVehicle(@RequestBody FleetVehicleVO vehicle) throws NoSuchFieldException, IllegalAccessException {
        if(vehicle !=null){
            if(!service.checkVehicleExists(vehicle.getCode())) {
                service.createVehicle(vehicle);
                return ResponseEntity.status(201).body(new HashMap<String, Object>() {{
                    put("message", "Vehicle created successfully");
                }});
            }else{
                throw new ApplicationException(400,"vehicle already exists with code:"+vehicle.getCode());
            }
        }
        throw new ApplicationException(400,"error while creating vehicle");
    }

    @GetMapping("/getAllVehicles")
    public ResponseEntity<Object> getAllVehicles() throws IllegalAccessException {
        List<FleetVehicleVO> vehicles = service.getAllVehicles1();
        return ResponseEntity.ok(vehicles);
    }

    @GetMapping("/getVehicleByCodeyve")
    public ResponseEntity<Object> getVehicleByCodeyve(@RequestParam String codeyve) throws IllegalAccessException {
        FleetVehicleVO vehicle = service.getVehicleByCodeyve(codeyve);
        if(vehicle!=null) {
            return ResponseEntity.ok(vehicle);
        }else{
            throw new ApplicationException(400,"No vehicle found with codeyve: "+codeyve);
        }
    }

    @PutMapping("/updateVehicle")
    public ResponseEntity<Object> updateVehicle(@RequestBody FleetVehicleVO vehicle) throws NoSuchFieldException, IllegalAccessException {
        if(vehicle!=null){
            if(service.checkVehicleExists(vehicle.getCode())) {
                service.updateVehicle(vehicle);
                return ResponseEntity.status(200).body(new HashMap<String, Object>() {{
                    put("message", "vehicle updated successfully");
                    put("vehicle", vehicle);
                }});
            }else{
                throw new ApplicationException(400,"vehicle doesn't exist with code: "+vehicle.getCode());
            }
        }
        throw new ApplicationException(400,"error while updating vehicle");
    }

    @DeleteMapping("/deleteVehicleByCodeyve")
    public ResponseEntity<Object> deleteVehicle(@RequestParam String codeyve){
        if(!service.checkVehicleExists(codeyve)){
            throw new ApplicationException(400,"Vehicle doesn't exist with code :"+codeyve);
        }
        String result = service.deleteVehicleByCodeyve(codeyve);
        if(result.equalsIgnoreCase("success"))
            return ResponseEntity.status(200).body("vehicle deleted successfully");
        else
            throw new ApplicationException(400,"error occurred while deleting the vehicle");
    }

    @PostMapping("/createTrailer")
    public ResponseEntity<Object> createTrailer(@RequestBody TrailerVO trailer) throws NoSuchFieldException, IllegalAccessException {
        if(trailer !=null){
            if(!service.checkTrailerExists(trailer.getTrailer())) {
                Trailer createdTrailer = service.createTrailer(trailer);
                return ResponseEntity.status(201).body(new HashMap<String, Object>() {{
                    put("message", "Trailer created successfully");
                    put("vehicle", createdTrailer);
                }});
            }else{
                throw new ApplicationException(400,"trailer already exists with code:"+trailer.getTrailer());
            }
        }
        throw new ApplicationException(400,"error while creating trailer");
    }

    @GetMapping("/getAllTrailers")
    public ResponseEntity<Object> getAllTrailers() throws IllegalAccessException {
        List<TrailerVO> trailers = service.getAllTrailers();
        return ResponseEntity.ok(trailers);
    }

    @GetMapping("/getTrailerByTrailerCode")
    public ResponseEntity<Object> getTrailerByTrailerCode(@RequestParam String trailer) throws IllegalAccessException {
        TrailerVO trailerObj = service.getTrailerByTrailerCode(trailer);
        if(trailerObj!=null) {
            return ResponseEntity.ok(trailerObj);
        }else{
            throw new ApplicationException(400,"No trailer found with code: "+trailer);
        }
    }

    @PutMapping("/updateTrailer")
    public ResponseEntity<Object> updateTrailer(@RequestBody TrailerVO trailerVO) throws NoSuchFieldException, IllegalAccessException {
        if(trailerVO!=null){
            if(service.checkTrailerExists(trailerVO.getTrailer())) {
                service.updateTrailer(trailerVO);
                return ResponseEntity.status(200).body(new HashMap<String, Object>() {{
                    put("message", "trailer updated successfully");
                    put("vehicle", trailerVO);
                }});
            }else{
                throw new ApplicationException(400,"trailer doesn't exist with code :"+trailerVO.getTrailer());
            }
        }
        throw new ApplicationException(400,"error while updating trailer");
    }

    @DeleteMapping("/deleteTrailerByTrailerCode")
    public ResponseEntity<Object> deleteTrailer(@RequestParam String trailer){
        if(!service.checkTrailerExists(trailer)){
            throw new ApplicationException(400,"Trailer doesn't exist with name :"+trailer);
        }
        String result = service.deleteTrailerByTrailerCode(trailer);
        if(result.equalsIgnoreCase("success"))
            return ResponseEntity.status(200).body("trailer deleted successfully");
        else
            throw new ApplicationException(400,"error occurred while deleting the trailer");

    }

    @PostMapping("/createVehicleClass")
    public ResponseEntity<Object> createVehicleClass(@RequestBody VehicleClassVO vehicleClass){
        if(vehicleClass !=null && !vehicleClass.getClassName().isEmpty()){
            if(!service.checkVehicleClassExists(vehicleClass.getClassName())) {
                VehClass createdVehClass = service.createVehicleClass(vehicleClass);
                return ResponseEntity.status(201).body(new HashMap<String, Object>() {{
                    put("message", "vehicleClass created successfully");
                    put("vehicle class", createdVehClass);
                }});
            }else{
                throw new ApplicationException(400,"vehicleClass already exists with class name:"+vehicleClass.getClassName());
            }
        }
        throw new ApplicationException(400,"error while creating vehicleClass");
    }

    @GetMapping("/getAllVehicleClassList")
    public ResponseEntity<Object> getAllVehicleClassList(){
        List<VehicleClassVO> vehicleClassList = service.getAllVehicleClassList();
        return ResponseEntity.ok(vehicleClassList);
    }

    @GetMapping("/getVehicleClassByClass")
    public ResponseEntity<Object> getVehicleClassByClass(@RequestParam String className){
        VehicleClassVO vehicleClass = service.getVehicleClassByClass(className);
        if(vehicleClass!=null) {
            return ResponseEntity.ok(vehicleClass);
        }else{
            throw new ApplicationException(400,"No vehicle class found with name: "+className);
        }
    }

    @PutMapping("/updateVehicleClass")
    public ResponseEntity<Object> updateVehicleClass(@RequestBody VehicleClassVO vehicleClassVO){
        if(vehicleClassVO!=null){
            if(service.checkVehicleClassExists(vehicleClassVO.getClassName())) {
                service.updateVehicleClass(vehicleClassVO);
                return ResponseEntity.status(200).body(new HashMap<String, Object>() {{
                    put("message", "vehicleClass updated successfully");
                    put("vehicleClass", vehicleClassVO);
                }});
            }
            else{
                throw new ApplicationException(400,"vehicleClass doesn't exist with class name :"+vehicleClassVO.getClassName());
            }
        }
        throw new ApplicationException(400,"error while updating vehicleClass");
    }

    @DeleteMapping("/deleteVehicleClassByClass")
    public ResponseEntity<Object> deleteVehicleClassByClass(@RequestParam String className){
        if(!service.checkVehicleClassExists(className)){
            throw new ApplicationException(400,"vehicle class doesn't exist with class name :"+className);
        }
        String result = service.deleteVehicleClassByClass(className);
        if(result.equalsIgnoreCase("success"))
            return ResponseEntity.status(200).body("vehicleClass deleted successfully");
        else
            throw new ApplicationException(400,"error occurred while deleting vehicleClass");
    }

    @PostMapping("/createDriver")
    public ResponseEntity<Object> createDriver(@RequestBody FleetDriverVO driverVO) throws NoSuchFieldException, IllegalAccessException {
        if(driverVO !=null){
            if(!service.checkDriverExists(driverVO.getDriverId())) {
                FleetDriver createdDriver = service.createDriver(driverVO);
                return ResponseEntity.status(201).body(new HashMap<String, Object>() {{
                    put("message", "driver created successfully");
                    put("driver", createdDriver);
                }});
            }else{
                throw new ApplicationException(400,"driver already exists with id:"+driverVO.getDriverId());
            }
        }
        throw new ApplicationException(400,"error while creating driver");
    }

    @GetMapping("/getAllDriversList")
    public ResponseEntity<Object> getAllDriversList() throws IllegalAccessException {
        List<FleetDriverVO> driversList = service.getAllDriversList1();
        return ResponseEntity.ok(driversList);
    }

    @GetMapping("/getDriverById")
    public ResponseEntity<Object> getDriverById(@RequestParam String driverId) throws IllegalAccessException {
        FleetDriverVO driver = service.getDriverById(driverId);
        if(driver!=null) {
            return ResponseEntity.ok(driver);
        }else{
            throw new ApplicationException(400,"No driver found with id: "+driverId);
        }
    }

    @PutMapping("/updateDriver")
    public ResponseEntity<Object> updateDriver(@RequestBody FleetDriverVO driverVO) throws NoSuchFieldException, IllegalAccessException {
        if(driverVO!=null){
            if(service.checkDriverExists(driverVO.getDriverId())) {
                service.updateDriver(driverVO);
                return ResponseEntity.status(200).body(new HashMap<String, Object>() {{
                    put("message", "driver updated successfully");
                    put("driver", driverVO);
                }});
            }
            else{
                throw new ApplicationException(400,"driver doesn't exist with the id: "+driverVO.getDriverId());
            }
        }
        throw new ApplicationException(400,"error while updating driver");
    }

    @DeleteMapping("/deleteDriverById")
    public ResponseEntity<Object> deleteDriverById(@RequestParam String driverId){
        if(!service.checkDriverExists(driverId)){
            throw new ApplicationException(400,"driver doesn't exist with id :"+driverId);
        }
        String result = service.deleteDriverById(driverId);
        if(result.equalsIgnoreCase("success"))
            return ResponseEntity.status(200).body("driver deleted successfully");
        else
            throw new ApplicationException(400,"error occurred while deleting driver");
    }

    //----------------------------------------------------------
    @GetMapping("/getCommonData")
    public ResponseEntity<Object> getCommonData(){
        return ResponseEntity.ok(service.getCommonData());
    }

    @GetMapping("/getVehicleCommonData")
    public ResponseEntity<Object> getVehicleCommonData(){
        return ResponseEntity.ok(service.getVehicleCommonData());
    }

    @GetMapping("/getVehicleClassCommonData")
    public ResponseEntity<Object> getVehicleClassCommonData(){
        return ResponseEntity.ok(service.getVehicleClassCommonData());
    }

    @GetMapping("/getTrailerCommonData")
    public ResponseEntity<Object> getTrailerCommonData(){
        return ResponseEntity.ok(service.getTrailerCommonData());
    }

    @GetMapping("/getDriverCommonData")
    public ResponseEntity<Object> getDriverCommonData(){
        return ResponseEntity.ok(service.getDriverCommonData());
    }
    //--------------------------------------------------------------

    @GetMapping("/getPostalData")
    public ResponseEntity<Object> getPostalData(@RequestParam String country){
        return ResponseEntity.ok(service.getPostalData(country));
    }

    @PostMapping("/createAllocation")
    public ResponseEntity<Object> createAllocation(@RequestBody Allocation allocation) throws NoSuchFieldException, IllegalAccessException {
        if(allocation !=null){
            if(!service.checkAllocationExists(allocation.getTransactionNumber())) {
                Allocation createdAllocation = service.createAllocation1(allocation);
                return ResponseEntity.status(201).body(new HashMap<String, Object>() {{
                    put("message", "Allocation created successfully");
                    put("allocation", createdAllocation);
                }});
            }else{
                throw new ApplicationException(500,"Allocation already exists with transaction:"+allocation.getTransactionNumber());
            }
        }
        throw new ApplicationException(500,"error while creating Allocation");
    }

    @GetMapping("/getAllAllocationsList")
    public ResponseEntity<Object> getAllAllocationsList() throws IllegalAccessException {
        List<Allocation> allocationList = service.getAllAllocationsList();
        return ResponseEntity.ok(allocationList);
    }

    @GetMapping("/getAllocationByTransactionNumber")
    public ResponseEntity<Object> getAllocationByTransactionNumber(@RequestParam String transactionNumber) throws IllegalAccessException {
        Allocation allocation = service.getAllocationByTransactionNumber(transactionNumber);
        if(allocation!=null) {
            return ResponseEntity.ok(allocation);
        }else{
            throw new ApplicationException(500,"No allocation found with transaction: "+transactionNumber);
        }
    }

    @PutMapping("/updateAllocation")
    public ResponseEntity<Object> updateAllocation(@RequestBody Allocation allocation) throws NoSuchFieldException, IllegalAccessException {
        if(allocation!=null){
            if(service.checkAllocationExists(allocation.getTransactionNumber())) {
                Allocation updatedAlloc = service.updateAllocation(allocation);
                return ResponseEntity.status(200).body(new HashMap<String, Object>() {{
                    put("message", "Allocation updated successfully");
                    put("Allocation", updatedAlloc);
                }});
            }
            else{
                throw new ApplicationException(500,"Allocation doesn't exist with the transaction: "+allocation.getTransactionNumber());
            }
        }
        throw new ApplicationException(500,"error while updating Allocation");
    }

    @DeleteMapping("/deleteAllocationByTransaction")
    public ResponseEntity<Object> deleteAllocationByTransaction(@RequestParam String transactionNumber){
        if(!service.checkAllocationExists(transactionNumber)){
            throw new ApplicationException(500,"Allocation doesn't exist with transaction :"+transactionNumber);
        }
        String result = service.deleteAllocationByTransactionNumber(transactionNumber);
        if(result.equalsIgnoreCase("success"))
            return ResponseEntity.status(200).body("Allocation deleted successfully");
        else
            throw new ApplicationException(500,"error occurred while deleting Allocation");
    }

    @GetMapping("/getAllocationCommonData")
    public ResponseEntity<Object> getAllocationCommonData(){
        return ResponseEntity.ok(service.getAllocationCommonData());
    }

    @GetMapping("/getAllImages")
    public ResponseEntity<Object> getAllImages(){
        return ResponseEntity.ok(service.getAllImages());
    }

    @PostMapping("/createTrailerType")
    public ResponseEntity<Object> createTrailerType(AccessTokenVO accessTokenVO, @RequestBody TrailerType trailerType) throws NoSuchFieldException, IllegalAccessException {
        if(trailerType !=null){
            if(!service.checkTrailerTypeExists(trailerType.getTrailerCode())) {
                TrailerType createdType = service.createTrailerType(trailerType);
                return ResponseEntity.status(201).body(new HashMap<String, Object>() {{
                    put("message", "Trailer Type created successfully");
                    put("TrailerType", createdType);
                }});
            }else{
                throw new ApplicationException(500,"TrailerType already exists with code:"+trailerType.getTrailerCode());
            }
        }
        throw new ApplicationException(500,"error while creating TrailerType");
    }

    @GetMapping("/getAllTrailerTypes")
    public ResponseEntity<Object> getAllTrailerTypes(AccessTokenVO accessTokenVO) throws IllegalAccessException {
        List<TrailerType> trailerTypes = service.getAllTrailerTypes();
        return ResponseEntity.ok(trailerTypes);
    }

    @GetMapping("/getTrailerTypeByTrailerCode")
    public ResponseEntity<Object> getTrailerTypeByTrailerCode(AccessTokenVO accessTokenVO,@RequestParam String trailerCode) throws IllegalAccessException {
        TrailerType trailerType = service.getTrailerTypeByTrailerCode(trailerCode);
        if(trailerType!=null) {
            return ResponseEntity.ok(trailerType);
        }else{
            throw new ApplicationException(500,"No TrailerType found with code: "+trailerCode);
        }
    }

    @PutMapping("/updateTrailerType")
    public ResponseEntity<Object> updateTrailerType(AccessTokenVO accessTokenVO, @RequestBody TrailerType trailerType) throws NoSuchFieldException, IllegalAccessException {
        if(trailerType!=null){
            if(service.checkTrailerTypeExists(trailerType.getTrailerCode())) {
                TrailerType updatedType = service.updateTrailerType(trailerType);
                return ResponseEntity.status(200).body(new HashMap<String, Object>() {{
                    put("message", "Trailer Type updated successfully");
                    put("Trailer Type", updatedType);
                }});
            }
            else{
                throw new ApplicationException(500,"Trailer Type doesn't exist with the code: "+trailerType.getTrailerCode());
            }
        }
        throw new ApplicationException(500,"error while updating Trailer Type");
    }

    @DeleteMapping("/deleteTrailerTypeByTrailerCode")
    public ResponseEntity<Object> deleteTrailerTypeByTrailerCode(AccessTokenVO accessTokenVO,@RequestParam String trailerCode){
        if(!service.checkTrailerTypeExists(trailerCode)){
            throw new ApplicationException(500,"Trailer Type doesn't exist with code :"+trailerCode);
        }
        String result = service.deleteTrailerTypeByTrailerCode(trailerCode);
        if(result.equalsIgnoreCase("success"))
            return ResponseEntity.status(200).body("Trailer Type deleted successfully");
        else
            throw new ApplicationException(500,"error occurred while deleting Trailer Type");
    }

    @GetMapping("/getTrailerTypeCommonData")
    public ResponseEntity<Object> getTrailerTypeCommonData(AccessTokenVO accessTokenVO){
        return ResponseEntity.ok(service.getTrailerTypeCommonData());
    }
}