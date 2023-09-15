/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.gl.ceir.config.controller;

import co.glocks.AlertApplication;
import com.gl.ceir.config.configuration.ConnectionConfiguration;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.gl.ceir.config.model.app.FileUploadDownloadResponse;
import com.gl.ceir.config.service.impl.FileStorageService;
import javax.servlet.http.HttpServletRequest;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.io.IOException;
import java.sql.Connection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.json.MappingJacksonValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/sampleFile")
public class FileUploadDownloadController {

    @Autowired
    private FileStorageService fileStorageService;

    private static final Logger logger = LogManager.getLogger(FileUploadDownloadController.class);

    @PostMapping("/uploadFile")
    public FileUploadDownloadResponse uploadFile(@RequestParam("file") MultipartFile file) {

        String fileName = fileStorageService.storeFile(file);
        logger.info("fileName" + fileName);
        String fileDownloadUri = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/downloadFile/")
                .path(fileName)
                .toUriString();
        logger.info("fileDownloadUri" + fileDownloadUri);

        return new FileUploadDownloadResponse(fileName, fileDownloadUri,
                file.getContentType(), file.getSize());
    }

    @PostMapping("/uploadMultipleFiles")
    public List< FileUploadDownloadResponse> uploadMultipleFiles(@RequestParam("files") MultipartFile[] files) {
        return Arrays.asList(files)
                .stream()
                .map(file -> uploadFile(file))
                .collect(Collectors.toList());
    }

//    Downlaoder
    @GetMapping("/downloadFile/{fileName:.+}")
    public ResponseEntity< Resource> downloadFile(@PathVariable String fileName, HttpServletRequest request) {
        // Load file as Resource
        Resource resource = fileStorageService.loadFileAsResource(fileName);

        // Try to determine file's content type
        String contentType = null;
        try {
            contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
        } catch (IOException ex) {
            logger.info("Could not determine file type.");
        }

        // Fallback to the default content type if type could not be determined
        if (contentType == null) {
            contentType = "application/octet-stream";
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }

    @GetMapping("/testController")
    public MappingJacksonValue getMobileDeviceDetails(@RequestParam("user") String user, @RequestParam("pass") String pass) {
        logger.info("Going to raise  = " + pass);

        // ConnectionConfiguration connectionConfiguration = null;
        Connection conn = new ConnectionConfiguration().getConnection();
        //  AlarmApplication.raiseAlertnew("alert006", " Sample alert ", "Test  No Process", 0);
     AlertApplication.raiseAlert(conn, "alert006", " Sample alert ", "Test  No Process", 0);
        // AlertApplication.raiseAlert("alert006", " Sample alert ", "Test  No Process", 0);
        // boolean d = Alarm.raiseAlert("alert006", " Sample alert ", "Test  No Process", 0);

        logger.info("Going to raise  = ");
        return new MappingJacksonValue(null);
    }
}

//    public String getResult(String user_type, String feature, String imei, Long imei_type) {
//        String rulePass = "true";
//
//        try {
//            Connection conn = getSQlConnection();
//            BufferedWriter bw = null;
//            String expOutput = "";
//            // ArrayList<Rule> rule_details = new ArrayList<Rule>();
//            int i = imei_type.intValue();
//            String deviceIdValue = null;
//            switch (i) {
//                case 0:
//                    deviceIdValue = "IMEI";
//                    break;
//                case 1:
//                    deviceIdValue = "MEID";
//                    break;
//                case 2:
//                    deviceIdValue = "ESN";
//                    break;
//            }
//            // deviceIdValue = "IMEI"; // to be remove if another values came ,
//            List<RuleEngineMapping> ruleList
//                    = checkImeiRepository.getByFeatureAndUserTypeOrderByRuleOrder(feature, user_type);
//            logger.info("RuleList is " + ruleList);
//            for (RuleEngineMapping cim : ruleList) {
//                Rule rule = new Rule(cim.getName(), cim.getOutput(), cim.getRuleMessage());
//                // rule_details.add(rule);
//                // }
//                // logger.info("Rules Populated"); // optimse
//                // for (Rule rule : rule_details) {
//
//                String[] my_arr = {
//                    rule.rule_name,
//                    "1",
//                    "NONCDR",
//                    ((rule.rule_name.equals("IMEI_LUHN_CHECK") || rule.rule_name.equals("IMEI_LENGTH"))
//                    ? imei
//                    : imei.substring(0, 14)),
//                    "4",
//                    "5",
//                    "6",
//                    "7",
//                    "8",
//                    deviceIdValue,
//                    "",
//                    " ",
//                    " ",
//                    ""
//                };
//                logger.info("Rule : " + rule.rule_name);
//                expOutput = RuleEngineApplication.startRuleEngine(my_arr, conn, bw);
//                logger.info("Rule Output By Engine :" + expOutput + " , Expected Output : " + rule.output);
//                if (rule.output.equalsIgnoreCase(expOutput)) { // go to next rule( rule_engine _mapping )
//                    logger.info("Rule Passed");
//                } else {
//                    logger.info("Rule failed at " + rule.rule_name);
//                    rulePass = rule.rule_name;
//                    break;
//                }
//            }
//            logger.info("Conn Close");
//            conn.close();
//        } catch (Exception e) {
//            logger.error(e.getMessage(), e);
//            throw new ResourceServicesException(this.getClass().getName(), e.getMessage());
//        } finally {
//
//            // auditTrailRepository.save(new AuditTrail(checkImeiValuesEntity.getUserId(),
//            // checkImeiValuesEntity.getUserName(),
//            // Long.valueOf(checkImeiValuesEntity.getUserTypeId()),
//            // checkImeiValuesEntity.getUserType(),
//            // Long.valueOf(checkImeiValuesEntity.getFeatureId()), Features.CONSIGNMENT,
//            // "VIEW_ALL", "", "NA",
//            // checkImeiValuesEntity.getRoleType(), checkImeiValuesEntity.getPublicIp(),
//            // checkImeiValuesEntity.getBrowser()));
//            // logger.info("AUDIT : Saved view request in audit.");
//        }
//        return rulePass;
//    }
//    private Connection getSQlConnection() throws SQLException {
//        Connection c1 = null;
//        StandardServiceRegistry standardRegistry
//                = new StandardServiceRegistryBuilder().configure("hibernate.cfg.xml").build();
//
//        Metadata metadata
//                = new MetadataSources(standardRegistry)
//                        .addAnnotatedClass(CheckImeiServiceImpl.class)
//                        .buildMetadata();
//
//        SessionFactory sessionFactory = metadata.getSessionFactoryBuilder().build();
//
//        try {
//            c1 = sessionFactory.getSessionFactoryOptions().getServiceRegistry().getService(ConnectionProvider.class).getConnection();
//            logger.info("Connection for Rule " + c1);
//            logger.info(c1.getMetaData().getDatabaseProductName());
//
//        } catch (Exception e) {
//            logger.info(" Erorr " + e);
//        }
//        return c1;
//    }
//            if (checkImeiRequest.getChannel().equalsIgnoreCase("ussd") || checkImeiRequest.getChannel().equalsIgnoreCase("sms")) {
//                message = message.replace("$brandName", gsmaTacDetails.getBrand_name())
//                        .replace("$modelName", gsmaTacDetails.getModel_name())
//                        .replace("$deviceType", gsmaTacDetails.getDevice_type())
//                        .replace("$manufacturer", gsmaTacDetails.getManufacturer())
//                        .replace("$marketingName", gsmaTacDetails.getMarketing_name());
//            }
