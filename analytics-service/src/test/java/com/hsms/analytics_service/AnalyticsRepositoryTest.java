package com.hsms.analytics_service;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import com.hsms.analytics_service.entity.AnalyticsReport;
import com.hsms.analytics_service.repository.AnalyticsReportRepository;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
class AnalyticsReportRepositoryTest {

    @Autowired
    private AnalyticsReportRepository repository;

    @Test
    void testSaveReport() {

        AnalyticsReport report = new AnalyticsReport();
        report.setTotalBookings(20);
        report.setRevenue(15000.0);
        report.setGeneratedAt(LocalDateTime.now());

        AnalyticsReport saved = repository.save(report);

        assertNotNull(saved);
        assertNotNull(saved.getReportId());
        assertEquals(20, saved.getTotalBookings());
    }

    @Test
    void testFindById() {

        AnalyticsReport report = new AnalyticsReport();
        report.setTotalBookings(15);
        report.setRevenue(12000.0);
        report.setGeneratedAt(LocalDateTime.now());

        AnalyticsReport saved = repository.save(report);

        Optional<AnalyticsReport> result =
                repository.findById(saved.getReportId());

        assertTrue(result.isPresent());
        assertEquals(saved.getReportId(), result.get().getReportId());
    }

    @Test
    void testFindAll() {

        AnalyticsReport r1 = new AnalyticsReport();
        r1.setTotalBookings(10);
        r1.setRevenue(5000.0);
        r1.setGeneratedAt(LocalDateTime.now());

        AnalyticsReport r2 = new AnalyticsReport();
        r2.setTotalBookings(25);
        r2.setRevenue(12000.0);
        r2.setGeneratedAt(LocalDateTime.now());

        repository.save(r1);
        repository.save(r2);

        List<AnalyticsReport> reports =
                repository.findAll();

        assertFalse(reports.isEmpty());
        assertEquals(2, reports.size());
    }

    @Test
    void testDeleteReport() {

        AnalyticsReport report = new AnalyticsReport();
        report.setTotalBookings(15);
        report.setRevenue(7000.0);
        report.setGeneratedAt(LocalDateTime.now());

        AnalyticsReport saved = repository.save(report);

        repository.deleteById(saved.getReportId());

        Optional<AnalyticsReport> result =
                repository.findById(saved.getReportId());

        assertFalse(result.isPresent());
    }
}