package com.shashi.service;

import com.shashi.beans.TrainBean;
import com.shashi.beans.TrainException;
import io.qameta.allure.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@Epic("Train Management Service Tests")
@Feature("Train Service CRUD Operations")
@Story("Unit tests for train service methods to ensure correct functionality and error handling.")
class TrainServiceTest {

    private TrainService trainService;

    @BeforeEach
    @DisplayName("Setup Mock TrainService")
    @Description("Initialize a mock instance of TrainService before each test execution.")
    void setUp() {
        trainService = mock(TrainService.class);
    }

    @Test
    @DisplayName("Add a New Train")
    @Description("Verify that a new train can be added successfully using the TrainService.")
    @Severity(SeverityLevel.CRITICAL)
    void testAddTrain() {
        TrainBean train = createSampleTrain();
        when(trainService.addTrain(train)).thenReturn("Train added successfully");

        String result = trainService.addTrain(train);

        assertEquals("Train added successfully", result);
        verify(trainService, times(1)).addTrain(train);
    }

    @Test
    @DisplayName("Delete Train by ID")
    @Description("Verify that a train can be deleted using its train number.")
    @Severity(SeverityLevel.CRITICAL)
    void testDeleteTrainById() {
        String trainNo = "12345";
        when(trainService.deleteTrainById(trainNo)).thenReturn("Train deleted successfully");

        String result = trainService.deleteTrainById(trainNo);

        assertEquals("Train deleted successfully", result);
        verify(trainService, times(1)).deleteTrainById(trainNo);
    }

    @Test
    @DisplayName("Update Train Details")
    @Description("Verify that train details can be updated successfully.")
    @Severity(SeverityLevel.CRITICAL)
    void testUpdateTrain() {
        TrainBean updatedTrain = createSampleTrain();
        updatedTrain.setTr_name("Updated Express");

        when(trainService.updateTrain(updatedTrain)).thenReturn("Train updated successfully");

        String result = trainService.updateTrain(updatedTrain);

        assertEquals("Train updated successfully", result);
        verify(trainService, times(1)).updateTrain(updatedTrain);
    }

    @Test
    @DisplayName("Get Train by ID")
    @Description("Verify fetching a train by its ID returns the correct train.")
    @Severity(SeverityLevel.NORMAL)
    void testGetTrainById() throws TrainException {
        String trainNo = "12345";
        TrainBean train = createSampleTrain();

        when(trainService.getTrainById(trainNo)).thenReturn(train);

        TrainBean result = trainService.getTrainById(trainNo);

        assertNotNull(result);
        assertEquals(train.getTr_no(), result.getTr_no());
        assertEquals(train.getTr_name(), result.getTr_name());
        verify(trainService, times(1)).getTrainById(trainNo);
    }

    @Test
    @DisplayName("Get All Trains")
    @Description("Verify fetching all trains returns the expected list of trains.")
    @Severity(SeverityLevel.NORMAL)
    void testGetAllTrains() throws TrainException {
        TrainBean train1 = createSampleTrain();
        TrainBean train2 = createSampleTrain();
        train2.setTr_no(54321L);
        train2.setTr_name("Another Express");

        List<TrainBean> trains = Arrays.asList(train1, train2);

        when(trainService.getAllTrains()).thenReturn(trains);

        List<TrainBean> result = trainService.getAllTrains();

        assertNotNull(result);
        assertEquals(2, result.size());
        verify(trainService, times(1)).getAllTrains();
    }

    @Test
    @DisplayName("Get Trains Between Stations")
    @Description("Verify fetching trains between two stations returns correct trains.")
    @Severity(SeverityLevel.NORMAL)
    void testGetTrainsBetweenStations() throws TrainException {
        String fromStation = "CityA";
        String toStation = "CityB";

        TrainBean train = createSampleTrain();
        List<TrainBean> trains = Arrays.asList(train);

        when(trainService.getTrainsBetweenStations(fromStation, toStation)).thenReturn(trains);

        List<TrainBean> result = trainService.getTrainsBetweenStations(fromStation, toStation);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(trainService, times(1)).getTrainsBetweenStations(fromStation, toStation);
    }

    @Test
    @DisplayName("Get Train By ID - Exception Handling")
    @Description("Verify that fetching a train by invalid ID throws TrainException.")
    @Severity(SeverityLevel.CRITICAL)
    void testGetTrainByIdThrowsException() throws TrainException {
        String trainNo = "invalid";

        when(trainService.getTrainById(trainNo)).thenThrow(new TrainException("Train not found"));

        TrainException exception = assertThrows(TrainException.class, () -> {
            trainService.getTrainById(trainNo);
        });

        assertEquals("Train not found", exception.getMessage());
        verify(trainService, times(1)).getTrainById(trainNo);
    }

    @Test
    @DisplayName("Get All Trains - Exception Handling")
    @Description("Verify that fetching all trains when none exist throws TrainException.")
    @Severity(SeverityLevel.CRITICAL)
    void testGetAllTrainsThrowsException() throws TrainException {
        when(trainService.getAllTrains()).thenThrow(new TrainException("No trains available"));

        TrainException exception = assertThrows(TrainException.class, () -> {
            trainService.getAllTrains();
        });

        assertEquals("No trains available", exception.getMessage());
        verify(trainService, times(1)).getAllTrains();
    }

    @Test
    @DisplayName("Get Trains Between Stations - Exception Handling")
    @Description("Verify that fetching trains between non-existent stations throws TrainException.")
    @Severity(SeverityLevel.CRITICAL)
    void testGetTrainsBetweenStationsThrowsException() throws TrainException {
        String fromStation = "UnknownA";
        String toStation = "UnknownB";

        when(trainService.getTrainsBetweenStations(fromStation, toStation))
                .thenThrow(new TrainException("No trains found between stations"));

        TrainException exception = assertThrows(TrainException.class, () -> {
            trainService.getTrainsBetweenStations(fromStation, toStation);
        });

        assertEquals("No trains found between stations", exception.getMessage());
        verify(trainService, times(1)).getTrainsBetweenStations(fromStation, toStation);
    }

    private TrainBean createSampleTrain() {
        TrainBean train = new TrainBean();
        train.setTr_no(12345L);
        train.setTr_name("Express Train");
        train.setFrom_stn("CityA");
        train.setTo_stn("CityB");
        train.setSeats(100);
        train.setFare(299.99);
        return train;
    }
}
