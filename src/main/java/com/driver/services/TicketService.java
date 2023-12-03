package com.driver.services;


import com.driver.EntryDto.BookTicketEntryDto;
import com.driver.model.Passenger;
import com.driver.model.Ticket;
import com.driver.model.Train;
import com.driver.repository.PassengerRepository;
import com.driver.repository.TicketRepository;
import com.driver.repository.TrainRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Service
public class TicketService {

    @Autowired
    TicketRepository ticketRepository;

    @Autowired
    TrainRepository trainRepository;

    @Autowired
    PassengerRepository passengerRepository;


    public Integer bookTicket(BookTicketEntryDto bookTicketEntryDto)throws Exception{

        //Check for validity
        //Use bookedTickets List from the TrainRepository to get bookings done against that train
        // Incase the there are insufficient tickets
        // throw new Exception("Less tickets are available");
        //otherwise book the ticket, calculate the price and other details
        //Save the information in corresponding DB Tables
        //Fare System : Check problem statement
        //Incase the train doesn't pass through the requested stations
        //throw new Exception("Invalid stations");
        //Save the bookedTickets in the train Object
        //Also in the passenger Entity change the attribute bookedTickets by using the attribute bookingPersonId.
       //And the end return the ticketId that has come from db

        Train train= trainRepository.findById(bookTicketEntryDto.getTrainId()).get();

        // check the station availability
        String [] route= train.getRoute().split(",");
        int from=-1, to=-1;
        for(int i=0; i<route.length; i++){
            if(route[i].equals(bookTicketEntryDto.getFromStation().toString())){
                from=i;
            }
            if(route[i].equals(bookTicketEntryDto.getToStation().toString())){
                to=i;
            }
        }

        if( from==-1 || to==-1 || from>to) {
            throw new Exception("Invalid stations");
        }


        //check the seats availability
        String [] routeStations= route;
        HashMap<String,Integer> map= new HashMap<>();
        for(int i=0; i<routeStations.length; i++){
            map.put(routeStations[i], i);
        }
        int  givenFrom= map.get(bookTicketEntryDto.getFromStation().toString());
        int givenTo= map.get(bookTicketEntryDto.getToStation().toString());
        int totalseatAvailable= train.getNoOfSeats();
        for(Ticket ticket: train.getBookedTickets()){
            int existingFrom= map.get(ticket.getFromStation().toString());
            int ExistingTo= map.get(ticket.getToStation().toString());
            if(givenTo>existingFrom && givenFrom<ExistingTo){
                totalseatAvailable-=ticket.getPassengersList().size();
            }
        }

        if(totalseatAvailable<bookTicketEntryDto.getNoOfSeats()){
            throw new Exception("Less tickets are available");
        }


        // calculate totalFare
        int fareForOne= (to-from)*300;
        int totalFare=fareForOne*bookTicketEntryDto.getNoOfSeats();


        // Create passengers List
        List<Passenger> passengers= new ArrayList<>();
        for (int pasangerId: bookTicketEntryDto.getPassengerIds()){
            Passenger passenger= passengerRepository.findById(pasangerId).get();
            passengers.add(passenger);
        }


        // Create ticket entity and set all attributes
        Ticket ticket= new Ticket();
        ticket.setPassengersList(passengers);
        ticket.setFromStation(bookTicketEntryDto.getFromStation());
        ticket.setToStation(bookTicketEntryDto.getToStation());
        ticket.setTrain(train);
        ticket.setTotalFare(totalFare);

        // Set the tickets for Booking person
        Passenger bookingPerson= passengerRepository.findById(bookTicketEntryDto.getBookingPersonId()).get();
        bookingPerson.getBookedTickets().add(ticket);

        // set the tickets in train entity

        train.getBookedTickets().add(ticket);

        Ticket savedTicket= ticketRepository.save(ticket);

        return savedTicket.getTicketId();

    }
}
