package com.driver.services;


import com.driver.model.Passenger;
import com.driver.repository.PassengerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
public class PassengerService {

    @Autowired
    PassengerRepository passengerRepository;

    public Integer addPassenger(Passenger passenger){
        //Add the passenger Object in the passengerDb and return the passegnerId that has been returned
        passenger.setBookedTickets(new ArrayList<>());
        Passenger savedPassenger = passengerRepository.save(passenger);
        return savedPassenger.getPassengerId();
    }

}
