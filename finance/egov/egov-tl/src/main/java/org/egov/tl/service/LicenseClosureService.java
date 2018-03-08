/*
 *    eGov  SmartCity eGovernance suite aims to improve the internal efficiency,transparency,
 *    accountability and the service delivery of the government  organizations.
 *
 *     Copyright (C) 2018  eGovernments Foundation
 *
 *     The updated version of eGov suite of products as by eGovernments Foundation
 *     is available at http://www.egovernments.org
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program. If not, see http://www.gnu.org/licenses/ or
 *     http://www.gnu.org/licenses/gpl.html .
 *
 *     In addition to the terms of the GPL license to be adhered to in using this
 *     program, the following additional terms are to be complied with:
 *
 *         1) All versions of this program, verbatim or modified must carry this
 *            Legal Notice.
 *            Further, all user interfaces, including but not limited to citizen facing interfaces,
 *            Urban Local Bodies interfaces, dashboards, mobile applications, of the program and any
 *            derived works should carry eGovernments Foundation logo on the top right corner.
 *
 *            For the logo, please refer http://egovernments.org/html/logo/egov_logo.png.
 *            For any further queries on attribution, including queries on brand guidelines,
 *            please contact contact@egovernments.org
 *
 *         2) Any misrepresentation of the origin of the material is prohibited. It
 *            is required that all modified versions of this material be marked in
 *            reasonable ways as different from the original version.
 *
 *         3) This license does not grant any rights to any user of the program
 *            with regards to rights under trademark law for use of the trade names
 *            or trademarks of eGovernments Foundation.
 *
 *   In case of any queries, you can reach eGovernments Foundation at contact@egovernments.org.
 *
 */

package org.egov.tl.service;

import org.egov.infra.admin.master.service.CityService;
import org.egov.infra.exception.ApplicationRuntimeException;
import org.egov.infra.filestore.entity.FileStoreMapper;
import org.egov.infra.filestore.service.FileStoreService;
import org.egov.infra.reporting.engine.ReportOutput;
import org.egov.infra.reporting.engine.ReportRequest;
import org.egov.infra.reporting.engine.ReportService;
import org.egov.infra.security.utils.SecurityUtils;
import org.egov.tl.entity.License;
import org.egov.tl.entity.LicenseDocument;
import org.egov.tl.entity.TradeLicense;
import org.egov.tl.service.es.LicenseApplicationIndexService;
import org.egov.tl.utils.LicenseNumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.egov.infra.utils.DateUtils.currentDateToDefaultDateFormat;
import static org.egov.tl.utils.Constants.AUTO;
import static org.egov.tl.utils.Constants.BUTTONAPPROVE;
import static org.egov.tl.utils.Constants.CLOSURE_LIC_APPTYPE;
import static org.egov.tl.utils.Constants.FILESTORE_MODULECODE;
import static org.egov.tl.utils.Constants.LICENSE_STATUS_ACKNOWLEDGED;
import static org.egov.tl.utils.Constants.LICENSE_STATUS_ACTIVE;
import static org.egov.tl.utils.Constants.LICENSE_STATUS_CANCELLED;
import static org.egov.tl.utils.Constants.LICENSE_STATUS_UNDERWORKFLOW;
import static org.egov.tl.utils.Constants.SIGNED_DOCUMENT_PREFIX;

@Service
@Transactional(readOnly = true)
public class LicenseClosureService extends LicenseService {

    @Autowired
    private CityService cityService;

    @Autowired
    private ReportService reportService;

    @Autowired
    private SecurityUtils securityUtils;

    @Autowired
    private FileStoreService fileStoreService;

    @Autowired
    private LicenseStatusService licenseStatusService;

    @Autowired
    private LicenseNumberUtils licenseNumberUtils;

    @Autowired
    private LicenseAppTypeService licenseAppTypeService;

    @Autowired
    private TradeLicenseSmsAndEmailService tradeLicenseSmsAndEmailService;

    @Autowired
    private LicenseApplicationIndexService licenseApplicationIndexService;

    @Autowired
    private LicenseCitizenPortalService licenseCitizenPortalService;

    @Autowired
    private LicenseClosureProcessflowService licenseClosureProcessflowService;

    public ReportOutput generateClosureEndorsementNotice(License license) {
        Map<String, Object> reportParams = new HashMap<>();
        reportParams.put("License", license);
        reportParams.put("currentDate", currentDateToDefaultDateFormat());
        reportParams.put("municipality", cityService.getMunicipalityName());
        return reportService.createReport(
                new ReportRequest("tl_closure_endorsement_notice", license, reportParams));
    }

    @Transactional
    public License generateClosureEndorsement(TradeLicense license) {
        ReportOutput reportOutput = generateClosureEndorsementNotice(license);
        if (reportOutput != null) {
            InputStream fileStream = new ByteArrayInputStream(reportOutput.getReportOutputData());
            FileStoreMapper fileStore = fileStoreService.store(fileStream,
                    SIGNED_DOCUMENT_PREFIX + license.getApplicationNumber() + ".pdf",
                    "application/pdf", FILESTORE_MODULECODE);
            license.setDigiSignedCertFileStoreId(fileStore.getFileStoreId());
            processSupportDocuments(license);
            save(license);
        }
        return license;
    }

    @Transactional
    public License approveClosure(String applicationNumber) {
        TradeLicense license = (TradeLicense) getLicenseByApplicationNumber(applicationNumber);
        license.setActive(false);
        license.setClosed(true);
        license.setStatus(licenseStatusService.getLicenseStatusByName(LICENSE_STATUS_CANCELLED));
        licenseClosureProcessflowService.processApproval(license);
        save(license);
        licenseApplicationIndexService.createOrUpdateLicenseApplicationIndex(license);
        licenseCitizenPortalService.onUpdate(license);
        tradeLicenseSmsAndEmailService.sendLicenseClosureMessage(license, BUTTONAPPROVE);
        return license;
    }

    @Transactional
    public License createClosure(TradeLicense license) {
        processSupportDocuments(license);
        licenseClosureProcessflowService.startClosureProcessflow(license);
        if (AUTO.equals(license.getApplicationNumber()))
            license.setApplicationNumber(licenseNumberUtils.generateApplicationNumber());
        license.setNewWorkflow(true);
        license.setApplicationDate(new Date());
        license.setStatus(licenseStatusService.getLicenseStatusByName(LICENSE_STATUS_ACKNOWLEDGED));
        license.setLicenseAppType(licenseAppTypeService.getLicenseAppTypeByName(CLOSURE_LIC_APPTYPE));
        save(license);
        licenseApplicationIndexService.createOrUpdateLicenseApplicationIndex(license);
        tradeLicenseSmsAndEmailService.sendLicenseClosureMessage(license, license.getWorkflowContainer().getWorkFlowAction());
        if (securityUtils.currentUserIsCitizen())
            licenseCitizenPortalService.onCreate(license);
        return license;
    }

    @Transactional
    public void cancelClosure(TradeLicense license) {
        if (license.getState().getExtraInfo() != null)
            license.setLicenseAppType(licenseAppTypeService
                    .getLicenseAppTypeByName(license.extraInfo().getOldAppType()));
        license.setStatus(licenseStatusService.getLicenseStatusByName(LICENSE_STATUS_ACTIVE));
        processSupportDocuments(license);
        licenseClosureProcessflowService.processCancellation(license);
        save(license);
        licenseApplicationIndexService.createOrUpdateLicenseApplicationIndex(license);
        licenseCitizenPortalService.onUpdate(license);
    }

    @Transactional
    public void rejectClosure(TradeLicense license) {
        processSupportDocuments(license);
        licenseClosureProcessflowService.processRejection(license);
        save(license);
        licenseApplicationIndexService.createOrUpdateLicenseApplicationIndex(license);
        licenseCitizenPortalService.onUpdate(license);
    }

    @Transactional
    public void forwardClosure(TradeLicense license) {
        processSupportDocuments(license);
        license.setStatus(licenseStatusService.getLicenseStatusByName(LICENSE_STATUS_UNDERWORKFLOW));
        licenseClosureProcessflowService.processForward(license);
        save(license);
        licenseApplicationIndexService.createOrUpdateLicenseApplicationIndex(license);
        licenseCitizenPortalService.onUpdate(license);
    }

    private void processSupportDocuments(TradeLicense license) {
        List<LicenseDocument> documents = license.getLicenseDocuments();
        for (LicenseDocument document : documents) {
            List<MultipartFile> files = document.getMultipartFiles();
            for (MultipartFile file : files) {
                try {
                    if (!file.isEmpty()) {
                        document.getFiles()
                                .add(fileStoreService.store(
                                        file.getInputStream(),
                                        file.getOriginalFilename(),
                                        file.getContentType(), "EGTL"));
                        document.setEnclosed(true);
                        document.setDocDate(license.getApplicationDate());
                    }
                } catch (IOException exp) {
                    throw new ApplicationRuntimeException("Error occurred while storing files ", exp);
                }
                document.setLicense(license);
            }
        }
        documents.removeIf(licenseDocument -> licenseDocument.getFiles().isEmpty());
        license.getDocuments().addAll(documents);
    }

}
