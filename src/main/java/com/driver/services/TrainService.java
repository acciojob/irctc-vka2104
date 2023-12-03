package com.driver.services;

import com.driver.EntryDto.AddTrainEntryDto;
import com.driver.EntryDto.SeatAvailabilityEntryDto;
import com.driver.model.Passenger;
import com.driver.model.Station;
import com.driver.model.Ticket;
import com.driver.model.Train;
import com.driver.repository.TrainRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class TrainService {

    @Autowired
    TrainRepository trainRepository;

    public Integer addTrain(AddTrainEntryDto trainEntryDto){

        //Add the train to the trainRepository
        //and route String logic to be taken from the Problem statement.
        //Save the train and return the trainId that is generated from the database.
        //Avoid using the lombok library
        Train train = new Train();
        String route = trainEntryDto.getStationRoute().stream().map(Station::toString).collect(Collectors.joining(","));
        train.setRoute(route);
        train.setDepartureTime(trainEntryDto.getDepartureTime());
        train.setNoOfSeats(trainEntryDto.getNoOfSeats());
        train.setBookedTickets(new ArrayList<>());
        Train savedTrain = trainRepository.save(train);
        return savedTrain.getTrainId();
    }

    public Integer calculateAvailableSeats(SeatAvailabilityEntryDto seatAvailabilityEntryDto){

        //Calculate the total seats available
        //Suppose the route is A B C D
        //And there are 2 seats avaialble in total in the train
        //and 2 tickets are booked from A to C and B to D.
        //The seat is available only between A to C and A to B. If a seat is empty between 2 station it will be counted to our final ans
        //even if that seat is booked post the destStation or before the boardingStation
        //Inshort : a train has totalNo of seats and there are tickets from and to different locations
        //We need to find out the available seats between the given 2 stations.
        Train train = trainRepository.findById(seatAvailabilityEntryDto.getTrainId()).get();
        int totalNumberOfSeats = train.getNoOfSeats();
        String[] routes = train.getRoute().split(",");
        HashMap<String, Integer> routeArr = new HashMap<>();
        for(int i = 0; i < routes.length; i++) {
            routeArr.put(routes[i], i);
        }
        int givenFromStationIdex = routeArr.get(seatAvailabilityEntryDto.getFromStation().toString());
        int givenToStationIndex = routeArr.get(seatAvailabilityEntryDto.getToStation().toString());
        for(Ticket bookedTicket: train.getBookedTickets()) {
            int existingFromStationIndex = routeArr.get(bookedTicket.getFromStation().toString());
            int existingToStationIndex = routeArr.get(bookedTicket.getToStation().toString());
            if(givenToStationIndex > existingFromStationIndex && givenFromStationIdex < existingToStationIndex) {
                totalNumberOfSeats -= bookedTicket.getPassengersList().size();
            }
        }
       return totalNumberOfSeats;
    }

    public Integer calculatePeopleBoardingAtAStation(Integer trainId,Station station) throws Exception{

        //We need to find out the number of people who will be boarding a train from a particular station
        //if the trainId is not passing through that station
        //throw new Exception("Train is not passing from this station");
        //  in a happy case we need to find out the number of such people.
        Train train = trainRepository.findById(trainId).get();
        String[] routes = train.getRoute().split(",");
        boolean isStationExist = Arrays.asList(routes).contains(station.toString());
        if(!isStationExist) throw new Exception("Train is not passing from this station");
        int count = 0;
        for(Ticket bookedTicket: train.getBookedTickets()) {
            if(bookedTicket.getFromStation().equals(station)) {
                count += bookedTicket.getPassengersList().size();
            }
        }
        return count;
    }

    public Integer calculateOldestPersonTravelling(Integer trainId){

        //Throughout the journey of the train between any 2 stations
        //We need to find out the age of the oldest person that is travelling the train
        //If there are no people travelling in that train you can return 0
        Optional<Train> trainOptional= trainRepository.findById(trainId);
        if(!trainOptional.isPresent()){
            return 0;
        }
        Train train= trainOptional.get();
        int maxAge=0;
        for(Ticket ticket: train.getBookedTickets()){
            for(Passenger passenger:ticket.getPassengersList()){
                maxAge=Math.max(maxAge, passenger.getAge());
            }
        }
        return maxAge;
    }

    public List<Integer> trainsBetweenAGivenTime(Station station, LocalTime startTime, LocalTime endTime){

        //When you are at a particular station you need to find out the number of trains that will pass through a given station
        //between a particular time frame both start time and end time included.
        //You can assume that the date change doesn't need to be done ie the travel will certainly happen with the same date (More details
        //in problem statement)
        //You can also assume the seconds and milli seconds value will be 0 in a LocalTime format.

        List<Train> trains= trainRepository.findAll();
        List<Integer> trainIds= new ArrayList<>();
        for(Train train : trains){

            if(train.getRoute().contains(station.toString())){
                String[] stations = train.getRoute().split(",");
                int idx=-1;
                for (int i=0; i<stations.length; i++){
                    if(stations[i].equals(station.toString())) {
                        idx=i;
                        break;
                    }
                }
                LocalTime time= train.getDepartureTime().plusHours(idx);
                if(!time.isAfter(endTime) && !time.isBefore(startTime)){
                    trainIds.add(train.getTrainId());
                }
            }
        }

        return trainIds;
    }

}
