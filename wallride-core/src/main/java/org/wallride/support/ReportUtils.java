package org.wallride.support;

import java.io.InputStream;
import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;

public class ReportUtils {

	private static Logger logger = LoggerFactory.getLogger(ReportUtils.class);
    
	public static JasperReport compileReport(final InputStream inStream) {
		JasperReport jasper = null;
		try {
			jasper = JasperCompileManager.compileReport(inStream);
		} catch (JRException e) {
			logger.warn("Could not compile the report. {}", e);
		}
		return jasper;
	}

    public static Locale getLocale(String language) {
        Locale locale;
		String[] localeInfo = language.split("_");
		if (localeInfo.length == 2) {
			locale = new Locale(localeInfo[0], localeInfo[1]);
		} else {
			locale = new Locale(localeInfo[0]);
		}
        return locale;
    }

    public static HttpHeaders getHttpHeaders(String filename) {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_PDF);
		headers.setContentDispositionFormData(filename, filename);
		headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");
        return headers;
    }

	public static ResponseEntity<byte[]> exportReportToPdf(JasperPrint jasperPrint, HttpHeaders headers) {
		ResponseEntity<byte[]> response = null;
		try {
			byte[] report = JasperExportManager.exportReportToPdf(jasperPrint);
			response = new ResponseEntity<>(report, headers, HttpStatus.OK);
		} catch (JRException e) {
			logger.warn("Could not export the report to the PDF stream. {}", e);
			response = new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
		return response;
	}
}
